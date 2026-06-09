"""
数据库连接模块 - 封装MySQL查询
"""
import pymysql
from typing import List, Dict, Any, Optional
from app.config import config


def get_connection() -> pymysql.Connection:
    """获取数据库连接"""
    return pymysql.connect(
        host=config.DB_HOST,
        port=config.DB_PORT,
        user=config.DB_USER,
        password=config.DB_PASSWORD,
        database=config.DB_NAME,
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor,
    )


def query_sql(sql: str, params: Optional[tuple] = None) -> List[Dict[str, Any]]:
    """
    执行查询SQL，返回结果列表

    Args:
        sql: SQL语句，用 %s 作为参数占位符
        params: 参数元组

    Returns:
        查询结果列表，每行是一个字典
    """
    conn = get_connection()
    try:
        with conn.cursor() as cursor:
            cursor.execute(sql, params)
            result = cursor.fetchall()
            return result
    finally:
        conn.close()


def execute_sql(sql: str, params: Optional[tuple] = None) -> int:
    """
    执行写入SQL（INSERT/UPDATE/DELETE）

    Args:
        sql: SQL语句
        params: 参数元组

    Returns:
        受影响的行数
    """
    conn = get_connection()
    try:
        with conn.cursor() as cursor:
            affected = cursor.execute(sql, params)
        conn.commit()
        return affected
    finally:
        conn.close()


# ============ 聊天记录 ============

def init_chat_records_table():
    """创建聊天记录表（如果不存在）"""
    sql = """
    CREATE TABLE IF NOT EXISTS chat_records (
        id INT AUTO_INCREMENT PRIMARY KEY,
        session_id VARCHAR(36) NOT NULL,
        shop_id VARCHAR(50) NOT NULL,
        role VARCHAR(10) NOT NULL,
        content TEXT NOT NULL,
        create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
        INDEX idx_session_id (session_id),
        INDEX idx_shop_id (shop_id)
    )
    """
    execute_sql(sql)


def save_chat_record(session_id: str, shop_id: str, role: str, content: str):
    """
    保存单条聊天记录到MySQL

    Args:
        session_id: 会话ID
        shop_id: 店铺编号
        role: 角色（user/assistant）
        content: 消息内容
    """
    sql = """
    INSERT INTO chat_records (session_id, shop_id, role, content)
    VALUES (%s, %s, %s, %s)
    """
    execute_sql(sql, (session_id, shop_id, role, content))


def query_chat_history(shop_id: str, limit: int = 50) -> list:
    """
    查询店铺的聊天历史（按时间倒序）

    Args:
        shop_id: 店铺编号
        limit: 返回条数
    """
    sql = """
    SELECT session_id, role, content, create_time
    FROM chat_records
    WHERE shop_id = %s
    ORDER BY create_time DESC
    LIMIT %s
    """
    return query_sql(sql, (shop_id, limit))


def query_session_history(session_id: str) -> list:
    """
    查询单个会话的聊天历史

    Args:
        session_id: 会话ID
    """
    sql = """
    SELECT role, content, create_time
    FROM chat_records
    WHERE session_id = %s
    ORDER BY create_time ASC
    """
    return query_sql(sql, (session_id,))


def delete_chat_records(session_id: str) -> int:
    """
    删除会话的所有聊天记录

    Args:
        session_id: 会话ID
    """
    sql = "DELETE FROM chat_records WHERE session_id = %s"
    return execute_sql(sql, (session_id,))


def delete_shop_chat_records(shop_id: str) -> int:
    """
    删除店铺的所有聊天记录

    Args:
        shop_id: 店铺编号
    """
    sql = "DELETE FROM chat_records WHERE shop_id = %s"
    return execute_sql(sql, (shop_id,))


def query_sessions_list(shop_id: str, page: int = 1, page_size: int = 20) -> dict:
    """
    查询店铺的会话列表（按session_id分组，返回摘要信息）

    Args:
        shop_id: 店铺编号
        page: 页码（从1开始）
        page_size: 每页条数

    Returns:
        {"sessions": [...], "total": 总会话数}
    """
    # 查询总会话数
    count_sql = """
    SELECT COUNT(DISTINCT session_id) as total
    FROM chat_records
    WHERE shop_id = %s
    """
    total_result = query_sql(count_sql, (shop_id,))
    total = total_result[0]["total"] if total_result else 0

    # 查询会话列表（每个会话取第一条user消息作为标题，最后一条消息作为预览）
    offset = (page - 1) * page_size
    sql = """
    SELECT
        t.session_id,
        t.title,
        t.last_message,
        t.message_count,
        t.updated_at
    FROM (
        SELECT
            session_id,
            -- 取第一条用户消息作为标题
            (SELECT content FROM chat_records cr2
             WHERE cr2.session_id = cr1.session_id AND cr2.role = 'user'
             ORDER BY cr2.create_time ASC LIMIT 1) AS title,
            -- 取最后一条消息作为预览
            (SELECT content FROM chat_records cr3
             WHERE cr3.session_id = cr1.session_id
             ORDER BY cr3.create_time DESC LIMIT 1) AS last_message,
            -- 消息总数
            COUNT(*) AS message_count,
            -- 最后更新时间
            MAX(create_time) AS updated_at
        FROM chat_records cr1
        WHERE shop_id = %s
        GROUP BY session_id
    ) t
    ORDER BY t.updated_at DESC
    LIMIT %s OFFSET %s
    """
    sessions = query_sql(sql, (shop_id, page_size, offset))

    # 格式化结果
    result = []
    for s in sessions:
        result.append({
            "id": s["session_id"],
            "title": (s["title"] or "")[:50],  # 截取前50字符作为标题
            "lastMessage": s["last_message"] or "",
            "messageCount": s["message_count"],
            "updatedAt": s["updated_at"].isoformat() if s["updated_at"] else None,
        })

    return {"sessions": result, "total": total}


def batch_delete_sessions(session_ids: list) -> int:
    """
    批量删除多个会话的聊天记录

    Args:
        session_ids: 会话ID列表

    Returns:
        删除的记录数
    """
    if not session_ids:
        return 0
    placeholders = ",".join(["%s"] * len(session_ids))
    sql = f"DELETE FROM chat_records WHERE session_id IN ({placeholders})"
    return execute_sql(sql, tuple(session_ids))


# ============ 测试连接 ============
if __name__ == "__main__":
    try:
        result = query_sql("SELECT COUNT(*) as count FROM shop")
        print(f"数据库连接成功！店铺数量：{result[0]['count']}")

        print("初始化聊天记录表...")
        init_chat_records_table()
        print("聊天记录表初始化完成")
    except Exception as e:
        print(f"数据库连接失败：{e}")
