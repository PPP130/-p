"""
FastAPI入口文件 - 提供HTTP接口
"""
import json
import asyncio
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from contextlib import asynccontextmanager
from sse_starlette.sse import EventSourceResponse

from app.agent import chat, chat_stream
from app.session import create_session, get_session, delete_session
from app.database import (
    init_chat_records_table,
    query_chat_history,
    query_session_history,
    delete_chat_records,
    delete_shop_chat_records,
    query_sessions_list,
    batch_delete_sessions,
)
from app.prompts import WELCOME_MESSAGE
from app.config import config
from app.knowledge import async_build_knowledge_base, get_build_status


@asynccontextmanager
async def lifespan(app: FastAPI):
    """应用生命周期管理"""
    # 初始化聊天记录表（快速，同步执行）
    init_chat_records_table()

    # 异步构建知识库（不阻塞启动）
    asyncio.create_task(async_build_knowledge_base())
    print("服务启动完成（知识库后台构建中...）")

    yield
    print("服务关闭")


app = FastAPI(
    title="餐饮AI客服服务",
    description="基于LangChain Agent的智能餐饮数据助手",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ============ 请求/响应模型 ============

class ChatRequest(BaseModel):
    """聊天请求"""
    shop_id: str           # 店铺编号
    message: str           # 用户消息
    session_id: str = None # 会话ID（首次不传，后续必传）
    logged_in: bool = False # 是否已登录
    token: str = ""        # JWT token（登录用户携带，用于调用Java后端API）
    image_base64: str = "" # 图片Base64编码（可选，用于菜品图片上传）


class ChatResponse(BaseModel):
    """聊天响应"""
    reply: str             # AI回复
    session_id: str        # 会话ID（首次返回，后续请求带上）
    success: bool = True


class HealthResponse(BaseModel):
    """健康检查响应"""
    status: str
    service: str


# ============ API接口 ============

@app.get("/api/health", response_model=HealthResponse)
async def health_check():
    """健康检查接口"""
    return HealthResponse(
        status="ok",
        service="ai-customer-service",
    )


@app.get("/api/welcome")
async def get_welcome():
    """获取欢迎消息"""
    return {"message": WELCOME_MESSAGE}


@app.get("/api/knowledge/status")
async def knowledge_status():
    """
    查询知识库状态

    返回知识库中的知识条数和来源文件列表，
    可用于判断知识库是否构建完成。
    """
    status = get_build_status()
    return {
        "ready": status["count"] > 0,
        "count": status["count"],
        "sources": status["sources"],
    }


@app.post("/api/chat", response_model=ChatResponse)
async def chat_endpoint(request: ChatRequest):
    """
    聊天接口

    首次调用：只传 shop_id + message + logged_in，返回 session_id
    后续调用：传 shop_id + message + session_id，续接上下文
    """
    try:
        # 首次调用，创建新会话
        if request.session_id is None:
            session_id = create_session(request.shop_id, request.logged_in, request.token)
        else:
            # 校验会话是否存在
            session = get_session(request.session_id)
            if session is None:
                raise HTTPException(status_code=404, detail="会话不存在或已过期")
            if session["shop_id"] != request.shop_id:
                raise HTTPException(status_code=403, detail="会话与店铺不匹配")
            session_id = request.session_id

        reply = chat(session_id=session_id, message=request.message, token=request.token, image_base64=request.image_base64)

        return ChatResponse(reply=reply, session_id=session_id, success=True)

    except HTTPException:
        raise
    except Exception as e:
        print(f"Agent处理出错: {e}")
        raise HTTPException(status_code=500, detail=f"AI处理出错: {str(e)}")


@app.post("/api/chat/stream")
async def chat_stream_endpoint(request: ChatRequest):
    """
    流式聊天接口（SSE）

    返回 Server-Sent Events 流，包含以下事件类型：
    - token: 流式输出的文本片段
    - tool_call: Agent调用的工具信息
    - done: 完成信号，包含完整回复
    - error: 错误信息
    """
    try:
        # 首次调用，创建新会话
        if request.session_id is None:
            session_id = create_session(request.shop_id, request.logged_in, request.token)
        else:
            # 校验会话是否存在
            session = get_session(request.session_id)
            if session is None:
                raise HTTPException(status_code=404, detail="会话不存在或已过期")
            if session["shop_id"] != request.shop_id:
                raise HTTPException(status_code=403, detail="会话与店铺不匹配")
            session_id = request.session_id

        async def event_generator():
            # 首先发送 session_id
            yield {
                "event": "session",
                "data": json.dumps({"session_id": session_id}),
            }

            # 流式输出Agent响应
            for chunk in chat_stream(session_id=session_id, message=request.message, token=request.token, image_base64=request.image_base64):
                event_type = chunk["type"]
                yield {
                    "event": event_type,
                    "data": json.dumps(chunk, ensure_ascii=False),
                }

        return EventSourceResponse(event_generator())

    except HTTPException:
        raise
    except Exception as e:
        print(f"Agent处理出错: {e}")
        raise HTTPException(status_code=500, detail=f"AI处理出错: {str(e)}")


@app.delete("/api/chat/{session_id}")
async def delete_chat(session_id: str):
    """删除会话（同时删除Redis和MySQL记录）"""
    session = get_session(session_id)
    redis_deleted = delete_session(session_id)

    # 如果是登录用户，同时删除MySQL记录
    mysql_deleted = 0
    if session and session.get("logged_in"):
        mysql_deleted = delete_chat_records(session_id)

    if not redis_deleted and mysql_deleted == 0:
        raise HTTPException(status_code=404, detail="会话不存在")
    return {
        "success": True,
        "message": f"会话已删除（Redis: {'是' if redis_deleted else '否'}, MySQL: {mysql_deleted}条记录）",
    }


@app.get("/api/chat/history/{session_id}")
async def get_chat_history(session_id: str):
    """获取会话的对话历史（从Redis）"""
    session = get_session(session_id)
    if session is None:
        raise HTTPException(status_code=404, detail="会话不存在或已过期")
    return {
        "session_id": session_id,
        "shop_id": session["shop_id"],
        "logged_in": session.get("logged_in", False),
        "messages": session["history"],
    }


@app.get("/api/chat/records/{shop_id}")
async def get_chat_records(shop_id: str, limit: int = 50):
    """查询店铺的聊天记录（从MySQL，登录用户专属）"""
    records = query_chat_history(shop_id, limit)
    return {
        "shop_id": shop_id,
        "total": len(records),
        "records": records,
    }


@app.get("/api/chat/sessions")
async def get_sessions_list(shop_id: str, page: int = 1, page_size: int = 20):
    """查询店铺的会话列表（按session_id分组，返回摘要）"""
    result = query_sessions_list(shop_id, page, page_size)
    return result


class BatchDeleteRequest(BaseModel):
    """批量删除请求"""
    ids: list[str]


@app.delete("/api/chat/sessions/batch")
async def batch_delete_sessions_endpoint(request: BatchDeleteRequest):
    """批量删除会话"""
    if not request.ids:
        raise HTTPException(status_code=400, detail="ids不能为空")
    deleted = batch_delete_sessions(request.ids)
    return {
        "success": True,
        "message": f"已删除 {deleted} 条记录",
    }


@app.delete("/api/chat/sessions/{session_id}")
async def delete_session_endpoint(session_id: str):
    """删除单个会话（同时删除Redis和MySQL记录）"""
    session = get_session(session_id)
    redis_deleted = delete_session(session_id)

    # 如果是登录用户，同时删除MySQL记录
    mysql_deleted = 0
    if session and session.get("logged_in"):
        mysql_deleted = delete_chat_records(session_id)

    if not redis_deleted and mysql_deleted == 0:
        raise HTTPException(status_code=404, detail="会话不存在")
    return {
        "success": True,
        "message": f"会话已删除（Redis: {'是' if redis_deleted else '否'}, MySQL: {mysql_deleted}条记录）",
    }


@app.get("/api/chat/records/session/{session_id}")
async def get_session_records(session_id: str):
    """查询单个会话的聊天记录（从MySQL）"""
    records = query_session_history(session_id)
    return {
        "session_id": session_id,
        "total": len(records),
        "records": records,
    }


@app.delete("/api/chat/records/{shop_id}")
async def delete_shop_records(shop_id: str):
    """删除店铺的所有聊天记录（MySQL）"""
    deleted = delete_shop_chat_records(shop_id)
    return {
        "success": True,
        "message": f"已删除 {deleted} 条记录",
    }


# ============ 启动入口 ============

if __name__ == "__main__":
    import uvicorn

    print(f"启动AI客服服务，端口: {config.SERVER_PORT}")
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=config.SERVER_PORT,
        reload=True,
    )
