"""
会话管理模块 - 基于Redis的临时会话存储
登录用户7天过期，未登录用户1天过期
"""
import json
import uuid
import redis
from typing import Optional
from app.config import config

# Redis客户端
redis_client = redis.from_url(config.REDIS_URL, decode_responses=True)

# 会话过期时间（秒）
SESSION_TTL_LOGGED_IN = 604800   # 7天
SESSION_TTL_ANONYMOUS = 86400    # 1天


def create_session(shop_id: str, logged_in: bool = False, token: str = "") -> str:
    """
    创建新会话，返回session_id

    Args:
        shop_id: 店铺编号
        logged_in: 是否已登录
        token: JWT token（登录用户携带，用于调用Java后端API）
    """
    session_id = str(uuid.uuid4())
    ttl = SESSION_TTL_LOGGED_IN if logged_in else SESSION_TTL_ANONYMOUS
    key = f"session:{session_id}"
    redis_client.set(key, json.dumps({
        "shop_id": shop_id,
        "logged_in": logged_in,
        "token": token,
        "history": [],
    }), ex=ttl)
    return session_id


def get_session(session_id: str) -> Optional[dict]:
    """
    获取会话数据

    Args:
        session_id: 会话ID

    Returns:
        {"shop_id": ..., "logged_in": ..., "history": [...]} 或 None
    """
    key = f"session:{session_id}"
    data = redis_client.get(key)
    if data is None:
        return None
    return json.loads(data)


def save_session(session_id: str, shop_id: str, history: list, logged_in: bool = False, token: str = ""):
    """
    保存会话数据（自动续期）

    Args:
        session_id: 会话ID
        shop_id: 店铺编号
        history: 对话历史 [{"role": "user", "content": ...}, ...]
        logged_in: 是否已登录
        token: JWT token
    """
    ttl = SESSION_TTL_LOGGED_IN if logged_in else SESSION_TTL_ANONYMOUS
    key = f"session:{session_id}"
    redis_client.set(key, json.dumps({
        "shop_id": shop_id,
        "logged_in": logged_in,
        "token": token,
        "history": history,
    }), ex=ttl)


def delete_session(session_id: str) -> bool:
    """
    删除会话

    Args:
        session_id: 会话ID
    """
    key = f"session:{session_id}"
    return redis_client.delete(key) > 0
