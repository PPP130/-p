"""
Agent核心模块 - 组装LLM + Tools + Prompt
使用Redis管理对话记忆，登录用户同步写入MySQL
"""
import threading
from queue import Queue, Empty
from typing import Any, Generator
from functools import lru_cache
from langchain_openai import ChatOpenAI
from langchain_classic.agents import AgentExecutor, create_openai_tools_agent
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.callbacks import BaseCallbackHandler
from langchain_classic.memory import ConversationBufferWindowMemory

from app.config import config
from app.tools import get_all_tools
from app.prompts import SYSTEM_PROMPT
from app.session import get_session, save_session
from app.database import save_chat_record


class _StreamHandler(BaseCallbackHandler):
    """流式输出回调处理器，通过队列将token传递给生成器"""

    def __init__(self, queue: Queue):
        self.queue = queue
        self._tools_called = False   # 是否已经调用过工具
        self._streaming_final = False  # 是否正在流式输出最终回复

    def on_chat_model_start(self, *args, **kwargs):
        """LLM开始生成"""
        if self._tools_called:
            # 工具调用过后的下一次LLM生成 = 最终回复
            self._streaming_final = True

    def on_agent_action(self, *args, **kwargs):
        """Agent决定调用工具，说明这次LLM输出是工具选择，不是最终回复"""
        self._tools_called = True
        self._streaming_final = False

    def on_llm_new_token(self, token: str, **kwargs):
        """每个新token到达时调用"""
        if self._streaming_final or not self._tools_called:
            # 两种情况放行：1)正在生成最终回复 2)简单问题没调用工具
            self.queue.put(token)

    def on_llm_error(self, error: BaseException, **kwargs):
        self.queue.put(None)  # 错误信号


_SENTINEL = None  # 结束信号


@lru_cache(maxsize=1)
def get_llm() -> ChatOpenAI:
    """
    获取LLM实例（全局单例，缓存复用）

    使用 lru_cache 确保整个应用生命周期内只创建一个 LLM 实例，
    避免每次请求都新建 HTTP 客户端连接。
    """
    print("创建 LLM 实例（仅首次调用时打印）")
    return ChatOpenAI(
        model=config.LLM_MODEL,
        temperature=0,
        api_key=config.OPENAI_API_KEY,
        base_url=config.OPENAI_BASE_URL,
        streaming=True,
    )


def get_tools(token: str = "", image_base64: str = "") -> list:
    """
    获取工具列表

    Args:
        token: JWT token，用于调用Java后端API
        image_base64: 图片Base64编码，注入到需要图片的工具中
    """
    return get_all_tools(token, image_base64)


def build_memory(history: list, k: int = 10) -> ConversationBufferWindowMemory:
    """
    从历史记录构建记忆对象

    Args:
        history: [{"role": "user", "content": ...}, ...]
        k: 保留最近k轮
    """
    memory = ConversationBufferWindowMemory(
        k=k,
        memory_key="chat_history",
        return_messages=True,
        output_key="output",
    )
    for msg in history:
        if msg["role"] == "user":
            memory.chat_memory.add_user_message(msg["content"])
        elif msg["role"] == "assistant":
            memory.chat_memory.add_ai_message(msg["content"])
    return memory


def create_agent(shop_id: str, memory: ConversationBufferWindowMemory, token: str = "", image_base64: str = "") -> AgentExecutor:
    """
    创建一个绑定到特定店铺的Agent

    使用缓存的 LLM 实例，工具根据 token 动态生成（有 token 时包含Java API操作工具）。
    """
    llm = get_llm()                        # 复用缓存的 LLM
    tools = get_tools(token, image_base64) # 根据token和图片决定工具集
    print(f"[Agent] 已加载 {len(tools)} 个工具（token={'有' if token else '无'}, 图片={'有' if image_base64 else '无'}）")

    prompt = ChatPromptTemplate.from_messages([
        ("system", SYSTEM_PROMPT.format(shop_id=shop_id)),
        MessagesPlaceholder(variable_name="chat_history"),
        ("human", "{input}"),
        MessagesPlaceholder(variable_name="agent_scratchpad"),
    ])

    agent = create_openai_tools_agent(llm, tools, prompt)

    executor = AgentExecutor(
        agent=agent,
        tools=tools,
        memory=memory,
        verbose=True,
        max_iterations=10,
        handle_parsing_errors=True,
    )

    return executor


def chat(session_id: str, message: str, token: str = "", image_base64: str = "") -> str:
    """
    与Agent对话（基于Redis会话，登录用户同步写入MySQL）

    Args:
        session_id: 会话ID
        message: 用户消息
        token: JWT token
        image_base64: 图片Base64编码

    Returns:
        AI回复内容
    """
    # 从Redis加载会话
    session = get_session(session_id)
    if session is None:
        raise ValueError("会话不存在或已过期")

    shop_id = session["shop_id"]
    history = session["history"]
    logged_in = session.get("logged_in", False)
    session_token = token or session.get("token", "")

    # 从历史记录构建记忆
    memory = build_memory(history)

    # 创建Agent并执行（有图片时注入到工具中）
    agent = create_agent(shop_id, memory, session_token, image_base64)
    result = agent.invoke({"input": message})

    reply = result["output"]

    # 更新历史记录并写回Redis
    history.append({"role": "user", "content": message})
    history.append({"role": "assistant", "content": reply})
    save_session(session_id, shop_id, history, logged_in, session_token)

    # 登录用户同步写入MySQL
    if logged_in:
        save_chat_record(session_id, shop_id, "user", message)
        save_chat_record(session_id, shop_id, "assistant", reply)

    return reply


def chat_stream(session_id: str, message: str, token: str = "", image_base64: str = "") -> Generator[str, None, None]:
    """
    与Agent流式对话（基于Redis会话，登录用户同步写入MySQL）

    Args:
        session_id: 会话ID
        message: 用户消息
        token: JWT token
        image_base64: 图片Base64编码

    Yields:
        流式输出的文本片段和控制信号
    """
    # 从Redis加载会话
    session = get_session(session_id)
    if session is None:
        yield {"type": "error", "content": "会话不存在或已过期"}
        return

    shop_id = session["shop_id"]
    history = session["history"]
    logged_in = session.get("logged_in", False)
    session_token = token or session.get("token", "")

    # 从历史记录构建记忆
    memory = build_memory(history)

    print(f"[chat_stream] 收到请求 - session_id={session_id}, token={'有' if session_token else '无'}, image_base64={'有(' + str(len(image_base64)) + '字符)' if image_base64 else '无'}")

    # 创建流式回调和队列
    queue: Queue = Queue()
    handler = _StreamHandler(queue)

    # 创建Agent（不注入回调，回调在invoke时通过config传入）
    agent_executor = create_agent(shop_id, memory, session_token, image_base64)

    # 在后台线程运行Agent，主线程从队列读取token
    result_box = {}
    error_box = {}

    def _run():
        try:
            result = agent_executor.invoke(
                {"input": message},
                config={"callbacks": [handler]},
            )
            result_box["output"] = result["output"]
        except Exception as e:
            error_box["error"] = e
        finally:
            queue.put(_SENTINEL)  # 发送结束信号

    thread = threading.Thread(target=_run, daemon=True)
    thread.start()

    # 主线程逐个读取token并yield
    full_reply = ""
    try:
        while True:
            token = queue.get(timeout=120)  # 最多等2分钟
            if token is _SENTINEL:
                break
            full_reply += token
            yield {"type": "token", "content": token}
    except Empty:
        yield {"type": "error", "content": "AI响应超时"}
        return

    # 检查后台线程是否有错误
    if "error" in error_box:
        yield {"type": "error", "content": f"Agent处理出错: {error_box['error']}"}
        return

    # 用后台线程的结果（更可靠）
    if "output" in result_box:
        full_reply = result_box["output"]

    # 更新历史记录并写回Redis
    history.append({"role": "user", "content": message})
    history.append({"role": "assistant", "content": full_reply})
    save_session(session_id, shop_id, history, logged_in, session_token)

    # 登录用户同步写入MySQL
    if logged_in:
        save_chat_record(session_id, shop_id, "user", message)
        save_chat_record(session_id, shop_id, "assistant", full_reply)

    # 发送完成信号
    yield {"type": "done", "content": full_reply}
