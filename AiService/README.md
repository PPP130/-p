# 餐饮AI客服服务

基于 LangChain Agent 的智能餐饮数据助手，为商家提供数据查询和经营分析服务。

## 功能特性

- 查询本店菜品信息和价格
- 查询同行同类菜品定价（竞争分析）
- 查看今日订单和营业额
- 分析销售趋势
- 查询热销菜品排行

## 技术栈

- **Web框架**: FastAPI
- **LLM框架**: LangChain
- **LLM**: OpenAI GPT-4o-mini（可替换为其他模型）
- **数据库**: MySQL（连接sky_take_out数据库）

## 快速开始

### 1. 安装依赖

```bash
# 创建虚拟环境（推荐）
python -m venv venv
venv\Scripts\activate  # Windows
# source venv/bin/activate  # Mac/Linux

# 安装依赖
pip install -r requirements.txt
```

### 2. 配置环境变量

```bash
# 复制配置模板
cp .env.example .env

# 编辑 .env 文件，填入你的配置
# - OPENAI_API_KEY: OpenAI API密钥
# - DB_PASSWORD: MySQL密码
```

### 3. 启动服务

```bash
# 方式1: 直接运行
python -m app.main

# 方式2: 使用uvicorn
uvicorn app.main:app --reload --port 8000
```

### 4. 测试接口

```bash
# 健康检查
curl http://localhost:8000/api/health

# 聊天测试
curl -X POST http://localhost:8000/api/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"shop_id\": \"你的店铺ID\", \"message\": \"我有哪些菜品？\"}"
```

## API文档

启动服务后访问: http://localhost:8000/docs

## 项目结构

```
AiService/
├── app/
│   ├── __init__.py      # Python包标识
│   ├── main.py          # FastAPI入口
│   ├── config.py        # 配置管理
│   ├── database.py      # 数据库连接
│   ├── tools.py         # Agent工具函数
│   ├── agent.py         # Agent核心逻辑
│   └── prompts.py       # 提示词模板
├── knowledge/           # 知识库（RAG用）
├── requirements.txt     # Python依赖
├── .env.example         # 环境变量模板
└── README.md            # 项目说明
```
