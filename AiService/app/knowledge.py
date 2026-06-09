"""
知识库模块 - 文档加载、向量化、检索
使用 ChromaDB 作为向量数据库
"""
import os
import asyncio
from typing import List
import chromadb
from chromadb.utils import embedding_functions
from langchain_text_splitters import RecursiveCharacterTextSplitter

# 知识库目录
KNOWLEDGE_DIR = os.path.join(os.path.dirname(os.path.dirname(__file__)), "knowledge")
CHROMA_DIR = os.path.join(os.path.dirname(os.path.dirname(__file__)), "chroma_db")

# 创建 ChromaDB 客户端
chroma_client = chromadb.PersistentClient(path=CHROMA_DIR)

# 使用默认的嵌入函数（中文支持较好）
ef = embedding_functions.DefaultEmbeddingFunction()


def get_or_create_collection(name: str = "restaurant_knowledge"):
    """获取或创建知识库集合"""
    return chroma_client.get_or_create_collection(
        name=name,
        embedding_function=ef,
        metadata={"hnsw:space": "cosine"}
    )


def load_documents_from_dir(doc_dir: str = None) -> List[dict]:
    """
    从目录加载文档文件，使用 RecursiveCharacterTextSplitter 分片

    支持 .txt 和 .md 文件
    """
    if doc_dir is None:
        doc_dir = os.path.join(KNOWLEDGE_DIR, "docs")

    documents = []

    if not os.path.exists(doc_dir):
        print(f"知识库目录不存在: {doc_dir}")
        return documents

    # 初始化分片器：中文友好分隔符，每块200字符，重叠50字符
    splitter = RecursiveCharacterTextSplitter(
        chunk_size=200,
        chunk_overlap=50,
        separators=["\n\n", "\n", "。", "！", "？", "；", "，", " ", ""],
    )

    for filename in os.listdir(doc_dir):
        filepath = os.path.join(doc_dir, filename)

        if filename.endswith(('.txt', '.md')):
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()

            # 使用 RecursiveCharacterTextSplitter 分片
            chunks = splitter.split_text(content)

            for i, chunk in enumerate(chunks):
                chunk = chunk.strip()
                if chunk:
                    documents.append({
                        "id": f"{filename}_{i}",
                        "text": chunk,
                        "metadata": {
                            "source": filename,
                            "chunk": i,
                        }
                    })

    return documents


def get_build_status() -> dict:
    """
    获取知识库构建状态

    Returns:
        {"count": 知识条数, "sources": 文件来源列表}
    """
    collection = get_or_create_collection()
    count = collection.count()

    sources = []
    if count > 0:
        data = collection.get()
        sources = list(set(m.get("source", "") for m in data["metadatas"]))

    return {"count": count, "sources": sources}


def build_knowledge_base():
    """
    构建知识库：加载文档并存入向量数据库（支持增量更新）
    """
    collection = get_or_create_collection()

    # 加载本地文档
    documents = load_documents_from_dir()

    if not documents:
        if collection.count() == 0:
            print("没有找到知识文档，请在 knowledge/docs/ 目录下添加 .txt 或 .md 文件")
        else:
            print(f"知识库已有 {collection.count()} 条数据，本地无文档")
        return

    # 获取已有文档ID
    existing_ids = set()
    if collection.count() > 0:
        existing_data = collection.get()
        existing_ids = set(existing_data["ids"])

    # 筛选新增文档
    new_docs = [doc for doc in documents if doc["id"] not in existing_ids]

    # 筛选已删除的文档（本地文件已不存在）
    current_ids = {doc["id"] for doc in documents}
    deleted_ids = [doc_id for doc_id in existing_ids if doc_id not in current_ids]

    # 删除已移除的文档
    if deleted_ids:
        collection.delete(ids=deleted_ids)
        print(f"已删除 {len(deleted_ids)} 条过期知识")

    # 添加新文档
    if new_docs:
        collection.add(
            ids=[doc["id"] for doc in new_docs],
            documents=[doc["text"] for doc in new_docs],
            metadatas=[doc["metadata"] for doc in new_docs],
        )
        print(f"新增 {len(new_docs)} 条知识")
    else:
        print("知识库无变化")

    print(f"知识库当前共 {collection.count()} 条知识")


async def async_build_knowledge_base():
    """
    异步构建知识库（不阻塞主线程）

    在线程池中执行同步的构建操作，避免阻塞事件循环。
    """
    loop = asyncio.get_event_loop()
    await loop.run_in_executor(None, build_knowledge_base)


def search_knowledge(query: str, n_results: int = 3, threshold: float = 0.25) -> List[str]:
    """
    检索知识库

    Args:
        query: 查询文本
        n_results: 返回结果数量
        threshold: 余弦距离阈值（越小越严格，0.25约等于相似度>0.75）

    Returns:
        相关知识列表
    """
    collection = get_or_create_collection()

    if collection.count() == 0:
        return ["知识库为空，请先添加知识文档"]

    results = collection.query(
        query_texts=[query],
        n_results=n_results,
    )

    # 过滤低相似度结果
    documents = []
    if results and results["documents"] and results["distances"]:
        for doc, dist in zip(results["documents"][0], results["distances"][0]):
            if dist <= threshold:
                documents.append(doc)

    if not documents:
        return []

    return documents


# 测试代码
if __name__ == "__main__":
    print("构建知识库...")
    build_knowledge_base()

    print("\n测试检索...")
    results = search_knowledge("怎么提高菜品销量")
    for i, r in enumerate(results, 1):
        print(f"{i}. {r[:100]}...")
