"""
工具函数模块 - 定义Agent可调用的工具
"""
import base64
import requests as req
from langchain.tools import tool
from langchain_community.tools import DuckDuckGoSearchRun
from app.database import query_sql
from app.knowledge import search_knowledge
from app.config import config


# ============ 菜品相关工具 ============

@tool
def query_my_dishes(shop_id: str, keyword: str = "") -> str:
    """查询本店的菜品列表，可以按关键词搜索菜品名称。

    Args:
        shop_id: 店铺编号
        keyword: 搜索关键词（可选，如"鸡"、"肉"等）
    """
    if keyword:
        sql = """
        SELECT name, price, status, description
        FROM dish
        WHERE shop_id = %s AND name LIKE %s
        ORDER BY create_time DESC
        LIMIT 20
        """
        result = query_sql(sql, (shop_id, f"%{keyword}%"))
    else:
        sql = """
        SELECT name, price, status, description
        FROM dish
        WHERE shop_id = %s
        ORDER BY create_time DESC
        LIMIT 20
        """
        result = query_sql(sql, (shop_id,))

    if not result:
        return "未找到相关菜品"

    # 格式化输出
    lines = ["本店菜品列表："]
    for i, dish in enumerate(result, 1):
        status_text = "在售" if dish["status"] == 1 else "已停售"
        lines.append(f"{i}. {dish['name']} - ¥{dish['price']}（{status_text}）")
    return "\n".join(lines)


@tool
def query_competitor_prices(dish_name: str, my_shop_id: str) -> str:
    """查询其他店铺同类菜品的价格，用于竞争分析和定价参考。

    Args:
        dish_name: 菜品名称（如"宫保鸡丁"）
        my_shop_id: 自己的店铺编号（用于排除自己）
    """
    sql = """
    SELECT s.name as shop_name, d.name as dish_name, d.price
    FROM dish d
    JOIN shop s ON d.shop_id = s.shop_id
    WHERE d.name LIKE %s
      AND d.shop_id != %s
      AND d.status = 1
      AND s.status = 1
    ORDER BY d.price ASC
    LIMIT 10
    """
    result = query_sql(sql, (f"%{dish_name}%", my_shop_id))

    if not result:
        return f"未找到其他店铺的「{dish_name}」菜品"

    # 计算均价
    avg_price = sum(r["price"] for r in result) / len(result)

    lines = [f"其他店铺的「{dish_name}」价格：", ""]
    lines.append("| 店铺名称 | 价格 |")
    lines.append("|---------|------|")
    for r in result:
        lines.append(f"| {r['shop_name']} | ¥{r['price']} |")
    lines.append("")
    lines.append(f"平台均价：¥{avg_price:.1f}")

    return "\n".join(lines)


@tool
def query_dish_status_monitor(shop_id: str) -> str:
    """监控菜品状态，发现滞销、停售等问题菜品。

    分析本店所有菜品的销售情况，找出：
    - 在售但近30天零销量的菜品（滞销预警）
    - 已停售的菜品
    - 各菜品的销量和营业额排名

    当用户问"哪些菜卖不动"、"菜品情况怎么样"、"需要调整什么菜品"时使用此工具。

    Args:
        shop_id: 店铺编号
    """
    # 查询所有菜品及其近30天销量（左连接，保留零销量的菜品）
    sql = """
    SELECT
        d.id,
        d.name,
        d.price,
        d.status,
        COALESCE(SUM(od_count.order_count), 0) AS total_sold,
        COALESCE(SUM(od_count.order_amount), 0) AS total_revenue
    FROM dish d
    LEFT JOIN (
        SELECT od.dish_id, COUNT(*) AS order_count, SUM(od.amount) AS order_amount
        FROM order_detail od
        JOIN orders o ON od.order_id = o.id
        WHERE o.shop_id = %s
          AND o.status = 5
          AND o.order_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
        GROUP BY od.dish_id
    ) od_count ON d.id = od_count.dish_id
    WHERE d.shop_id = %s
    GROUP BY d.id, d.name, d.price, d.status
    ORDER BY total_sold ASC
    """
    result = query_sql(sql, (shop_id, shop_id))

    if not result:
        return "本店暂无菜品数据"

    # 分类统计
    stopped = []       # 已停售
    slow_movers = []   # 在售但零销量
    normal = []        # 正常销售

    for dish in result:
        if dish["status"] == 0:
            stopped.append(dish)
        elif dish["total_sold"] == 0:
            slow_movers.append(dish)
        else:
            normal.append(dish)

    lines = ["菜品状态监控报告：", ""]

    # 滞销预警（在售但零销量）
    if slow_movers:
        lines.append(f"**滞销预警（在售但近30天零销量，共{len(slow_movers)}道）：**")
        for d in slow_movers:
            lines.append(f"  - {d['name']}（¥{d['price']}）- 建议考虑调整价格或停售")
        lines.append("")

    # 已停售
    if stopped:
        lines.append(f"**已停售（共{len(stopped)}道）：**")
        for d in stopped:
            lines.append(f"  - {d['name']}（¥{d['price']}）")
        lines.append("")

    # 正常销售
    if normal:
        normal_sorted = sorted(normal, key=lambda x: x["total_sold"], reverse=True)
        lines.append(f"**正常销售（共{len(normal_sorted)}道）：**")
        lines.append("| 菜品 | 价格 | 30天销量 | 30天营收 |")
        lines.append("|------|------|----------|----------|")
        for d in normal_sorted:
            lines.append(f"| {d['name']} | ¥{d['price']} | {d['total_sold']}份 | ¥{d['total_revenue']:.1f} |")

    # 汇总
    lines.append("")
    lines.append(f"汇总：共{len(result)}道菜，{len(normal)}道正常销售，{len(slow_movers)}道滞销预警，{len(stopped)}道已停售")

    return "\n".join(lines)


# ============ 订单相关工具 ============

@tool
def query_today_orders(shop_id: str) -> str:
    """查询今天本店的订单统计，包括订单数和营业额。

    Args:
        shop_id: 店铺编号
    """
    sql = """
    SELECT
        COUNT(*) as order_count,
        COALESCE(SUM(amount), 0) as total_amount,
        COALESCE(AVG(amount), 0) as avg_amount
    FROM orders
    WHERE shop_id = %s
      AND DATE(order_time) = CURDATE()
      AND status = 5
    """
    result = query_sql(sql, (shop_id,))

    if not result or result[0]["order_count"] == 0:
        return "今天暂无已完成的订单"

    data = result[0]
    return f"""今日营业数据：
- 完成订单：{data['order_count']}单
- 总营业额：¥{data['total_amount']:.1f}
- 平均客单价：¥{data['avg_amount']:.1f}"""


@tool
def query_order_trend(shop_id: str, days: int = 7) -> str:
    """查询最近几天的订单趋势，用于分析经营状况。

    Args:
        shop_id: 店铺编号
        days: 查询最近几天（默认7天）
    """
    sql = """
    SELECT
        DATE(order_time) as date,
        COUNT(*) as order_count,
        COALESCE(SUM(amount), 0) as total_amount
    FROM orders
    WHERE shop_id = %s
      AND order_time >= DATE_SUB(CURDATE(), INTERVAL %s DAY)
      AND status = 5
    GROUP BY DATE(order_time)
    ORDER BY date ASC
    """
    result = query_sql(sql, (shop_id, days))

    if not result:
        return f"最近{days}天暂无订单数据"

    lines = [f"最近{days}天订单趋势：", ""]
    lines.append("| 日期 | 订单数 | 营业额 |")
    lines.append("|------|--------|--------|")
    for r in result:
        lines.append(f"| {r['date']} | {r['order_count']}单 | ¥{r['total_amount']:.1f} |")

    # 计算总计
    total_orders = sum(r["order_count"] for r in result)
    total_amount = sum(r["total_amount"] for r in result)
    lines.append("")
    lines.append(f"合计：{total_orders}单，¥{total_amount:.1f}")

    return "\n".join(lines)


# ============ 店铺相关工具 ============

@tool
def query_shop_info(shop_id: str) -> str:
    """查询本店的基本信息。

    Args:
        shop_id: 店铺编号
    """
    sql = """
    SELECT name, owner_name, phone, address, description, status, business_status
    FROM shop
    WHERE shop_id = %s
    """
    result = query_sql(sql, (shop_id,))

    if not result:
        return "未找到店铺信息"

    shop = result[0]
    status_map = {0: "待审核", 1: "已通过", 2: "已拒绝", 3: "已禁用"}
    business_map = {0: "打烊", 1: "营业中"}

    return f"""店铺信息：
- 店铺名称：{shop['name']}
- 店主：{shop['owner_name']}
- 电话：{shop['phone']}
- 地址：{shop['address']}
- 简介：{shop['description'] or '暂无'}
- 审核状态：{status_map.get(shop['status'], '未知')}
- 营业状态：{business_map.get(shop['business_status'], '未知')}"""


@tool
def query_hot_dishes(shop_id: str, limit: int = 10) -> str:
    """查询本店热销菜品排行。

    Args:
        shop_id: 店铺编号
        limit: 显示前几个（默认10个）
    """
    sql = """
    SELECT d.name, d.price, COUNT(od.id) as order_count
    FROM order_detail od
    JOIN dish d ON od.dish_id = d.id
    JOIN orders o ON od.order_id = o.id
    WHERE o.shop_id = %s
      AND o.status = 5
      AND o.order_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
    GROUP BY d.id, d.name, d.price
    ORDER BY order_count DESC
    LIMIT %s
    """
    result = query_sql(sql, (shop_id, limit))

    if not result:
        return "暂无销售数据"

    lines = ["本店热销菜品TOP排行（近30天）：", ""]
    lines.append("| 排名 | 菜品 | 价格 | 销量 |")
    lines.append("|------|------|------|------|")
    for i, r in enumerate(result, 1):
        lines.append(f"| {i} | {r['name']} | ¥{r['price']} | {r['order_count']}份 |")

    return "\n".join(lines)


# ============ 联网搜索工具 ============

web_search = DuckDuckGoSearchRun()


# ============ 知识库相关工具 ============

@tool
def search_business_knowledge(query: str) -> str:
    """检索餐饮经营知识库，获取经营建议、营销技巧、菜品管理等知识。

    当用户询问经营建议、营销方法、菜品管理等非数据查询类问题时使用此工具。

    Args:
        query: 查询内容（如"怎么提高销量"、"菜品定价"、"营销活动"等）
    """
    results = search_knowledge(query, n_results=3)

    if not results:
        return "知识库中没有找到相关信息，请根据已有的店铺数据回答，不要编造内容"

    if results[0] == "知识库为空，请先添加知识文档":
        return results[0]

    lines = ["根据知识库为您找到以下信息：", ""]
    for i, text in enumerate(results, 1):
        lines.append(f"【参考{i}】")
        lines.append(text)
        lines.append("")

    return "\n".join(lines)


# ============ Java后端API工具 ============

def _java_headers(token: str) -> dict:
    """构造Java后端请求头"""
    return {"token": token, "Content-Type": "application/json"}


def _create_java_tools(token: str, ctx_image_base64: str = "") -> list:
    """创建需要JWT认证的Java后端API工具（通过闭包注入token和图片）"""
    base = config.JAVA_API_BASE_URL

    @tool
    def api_list_categories() -> str:
        """查询本店的菜品分类列表，用于新增或修改菜品时选择分类。

        返回分类ID和名称，新增菜品时需要提供categoryId。
        """
        resp = req.get(
            f"{base}/admin/category/list",
            headers=_java_headers(token),
            params={"type": 1},
            timeout=10,
        )
        if resp.status_code == 401:
            return "认证已过期，请重新登录后再试"
        data = resp.json()
        if data.get("code") != 1:
            return f"查询失败：{data.get('msg', '未知错误')}"
        categories = data.get("data", [])
        if not categories:
            return "暂无菜品分类，请先在管理端创建分类"
        lines = ["本店菜品分类："]
        for c in categories:
            lines.append(f"  - ID:{c['id']} {c['name']}")
        return "\n".join(lines)

    @tool
    def api_query_dish_page(name: str = "", status: int = -1, page: int = 1, page_size: int = 20) -> str:
        """分页查询本店菜品列表（从Java后端获取实时数据）。

        Args:
            name: 菜品名称关键词（可选）
            status: 菜品状态，1=起售 0=停售，-1=全部（默认）
            page: 页码（默认1）
            page_size: 每页条数（默认20）
        """
        params = {"page": page, "pageSize": page_size}
        if name:
            params["name"] = name
        if status >= 0:
            params["status"] = status
        resp = req.get(
            f"{base}/admin/dish/page",
            headers=_java_headers(token),
            params=params,
            timeout=10,
        )
        if resp.status_code == 401:
            return "认证已过期，请重新登录后再试"
        data = resp.json()
        if data.get("code") != 1:
            return f"查询失败：{data.get('msg', '未知错误')}"
        result = data.get("data", {})
        records = result.get("records", [])
        total = result.get("total", 0)
        if not records:
            return f"未找到菜品（共{total}条）"
        lines = [f"菜品列表（共{total}条，第{page}页）："]
        lines.append("| ID | 菜品 | 价格 | 状态 | 分类 |")
        lines.append("|------|------|------|------|------|")
        for d in records:
            st = "在售" if d.get("status") == 1 else "停售"
            lines.append(f"| {d['id']} | {d['name']} | ¥{d['price']} | {st} | {d.get('categoryName', '-')} |")
        return "\n".join(lines)

    @tool
    def api_get_dish_detail(dish_id: int) -> str:
        """查询单个菜品的详细信息（含口味和分类）。

        Args:
            dish_id: 菜品ID
        """
        resp = req.get(
            f"{base}/admin/dish/{dish_id}",
            headers=_java_headers(token),
            timeout=10,
        )
        if resp.status_code == 401:
            return "认证已过期，请重新登录后再试"
        data = resp.json()
        if data.get("code") != 1:
            return f"查询失败：{data.get('msg', '未知错误')}"
        d = data.get("data", {})
        if not d:
            return "菜品不存在"
        st = "在售" if d.get("status") == 1 else "停售"
        lines = [
            f"菜品详情：",
            f"  ID: {d['id']}",
            f"  名称: {d['name']}",
            f"  价格: ¥{d['price']}",
            f"  状态: {st}",
            f"  分类: {d.get('categoryName', '-')}",
            f"  描述: {d.get('description', '无')}",
        ]
        flavors = d.get("flavors", [])
        if flavors:
            lines.append("  口味：")
            for f in flavors:
                lines.append(f"    - {f['name']}: {f['value']}")
        return "\n".join(lines)

    @tool
    def api_add_dish(name: str, category_id: int, price: float, image: str, description: str = "", flavors: str = "") -> str:
        """[内部工具] 新增菜品，需要已有的category_id和image_url。请勿直接使用此工具，新增菜品请使用api_create_dish_smart。

        Args:
            name: 菜品名称（如"红烧肉"）
            category_id: 分类ID（必须是数字，先用api_list_categories查询可用分类获取ID）
            price: 菜品价格（单位：元）
            image: 菜品图片URL（通过api_upload_image上传获得）
            description: 菜品描述（可选）
            flavors: 口味选项，格式为JSON数组字符串，如'[{"name":"辣度","value":"不辣,微辣,中辣"}]'（可选）
        """
        body = {
            "name": name,
            "categoryId": category_id,
            "price": price,
            "image": image,
            "description": description,
            "status": 1,
        }
        if flavors:
            import json
            try:
                body["flavors"] = json.loads(flavors)
            except json.JSONDecodeError:
                body["flavors"] = []
        else:
            body["flavors"] = []
        resp = req.post(
            f"{base}/admin/dish",
            headers=_java_headers(token),
            json=body,
            timeout=15,
        )
        if resp.status_code == 401:
            return "认证已过期，请重新登录后再试"
        data = resp.json()
        if data.get("code") != 1:
            return f"新增失败：{data.get('msg', '未知错误')}"
        return f"菜品「{name}」已成功上架！价格¥{price}，分类ID:{category_id}"

    @tool
    def api_update_dish(dish_id: int, name: str = "", category_id: int = 0, price: float = 0, image: str = "", description: str = "", status: int = -1, flavors: str = "") -> str:
        """修改已有菜品的信息。只需传入要修改的字段，不传的字段保持不变。

        Args:
            dish_id: 要修改的菜品ID（必填）
            name: 新菜品名称（可选）
            category_id: 新分类ID（可选，数字）
            price: 新价格（可选，0表示不修改）
            image: 新图片URL（可选）
            description: 新描述（可选）
            status: 新状态，1=起售 0=停售，-1=不修改（默认）
            flavors: 新口味选项JSON字符串（可选）
        """
        body = {"id": dish_id}
        if name:
            body["name"] = name
        if category_id > 0:
            body["categoryId"] = category_id
        if price > 0:
            body["price"] = price
        if image:
            body["image"] = image
        if description:
            body["description"] = description
        if status >= 0:
            body["status"] = status
        if flavors:
            import json
            try:
                body["flavors"] = json.loads(flavors)
            except json.JSONDecodeError:
                body["flavors"] = []
        resp = req.put(
            f"{base}/admin/dish",
            headers=_java_headers(token),
            json=body,
            timeout=15,
        )
        if resp.status_code == 401:
            return "认证已过期，请重新登录后再试"
        data = resp.json()
        if data.get("code") != 1:
            return f"修改失败：{data.get('msg', '未知错误')}"
        return f"菜品ID:{dish_id}已成功更新"

    @tool
    def api_delete_dish(dish_ids: str) -> str:
        """删除本店菜品（支持批量删除）。

        Args:
            dish_ids: 要删除的菜品ID，多个用逗号分隔（如"1,2,3"）
        """
        resp = req.delete(
            f"{base}/admin/dish",
            headers=_java_headers(token),
            params={"ids": dish_ids},
            timeout=10,
        )
        if resp.status_code == 401:
            return "认证已过期，请重新登录后再试"
        data = resp.json()
        if data.get("code") != 1:
            return f"删除失败：{data.get('msg', '未知错误')}"
        return f"菜品ID:{dish_ids}已成功删除"

    @tool
    def api_toggle_dish_status(dish_id: int, status: int) -> str:
        """上架或下架菜品。

        Args:
            dish_id: 菜品ID
            status: 目标状态，1=上架（起售），0=下架（停售）
        """
        resp = req.post(
            f"{base}/admin/dish/status/{status}",
            headers=_java_headers(token),
            params={"id": dish_id},
            timeout=10,
        )
        if resp.status_code == 401:
            return "认证已过期，请重新登录后再试"
        data = resp.json()
        if data.get("code") != 1:
            return f"操作失败：{data.get('msg', '未知错误')}"
        action = "上架" if status == 1 else "下架"
        return f"菜品ID:{dish_id}已{action}"

    @tool
    def api_upload_image(image_base64: str, filename: str = "dish.jpg") -> str:
        """上传菜品图片到服务器，返回图片URL。

        Args:
            image_base64: 图片的Base64编码字符串
            filename: 文件名（如"dish.jpg"、"photo.png"）
        """
        try:
            image_bytes = base64.b64decode(image_base64)
        except Exception:
            return "图片解码失败，请提供有效的Base64编码"
        ext = filename.rsplit(".", 1)[-1] if "." in filename else "jpg"
        resp = req.post(
            f"{base}/admin/common/upload",
            headers={"token": token},
            files={"file": (filename, image_bytes, f"image/{ext}")},
            timeout=30,
        )
        if resp.status_code != 200:
            return f"上传失败，HTTP状态码：{resp.status_code}"
        data = resp.json()
        if data.get("code") != 1:
            return f"上传失败：{data.get('msg', '未知错误')}"
        url = data.get("data", "")
        return f"图片上传成功，URL：{url}"

    @tool
    def api_create_dish_smart(name: str, price: float, category_name: str = "", image_base64: str = "", image_url: str = "", description: str = "", flavors: str = "") -> str:
        """一站式新增菜品：自动查询分类、上传图片、创建菜品。

        只需提供菜品名称和价格。如果用户发送了图片，图片会自动注入，直接调用此工具即可，不需要先确认图片是否存在。

        Args:
            name: 菜品名称（如"红烧肉"、"宫保鸡丁"）
            price: 菜品价格（单位：元）
            category_name: 菜品分类名称（如"硬菜"、"家常菜"），不传则自动选择第一个分类
            image_base64: 菜品图片的Base64编码（与image_url二选一）
            image_url: 菜品图片URL（如果已有图片URL，直接传入，与image_base64二选一）
            description: 菜品描述（可选）
            flavors: 口味选项JSON字符串，如'[{"name":"辣度","value":"不辣,微辣,中辣"}]'（可选）
        """
        # 第一步：查询分类列表
        cat_resp = req.get(
            f"{base}/admin/category/list",
            headers=_java_headers(token),
            params={"type": 1},
            timeout=10,
        )
        if cat_resp.status_code == 401:
            return "认证已过期，请重新登录后再试"
        cat_data = cat_resp.json()
        if cat_data.get("code") != 1:
            return f"查询分类失败：{cat_data.get('msg', '未知错误')}"
        categories = cat_data.get("data", [])
        if not categories:
            return "暂无菜品分类，请先在管理端创建分类后再添加菜品"

        # 匹配分类
        category_id = categories[0]["id"]  # 默认用第一个
        if category_name:
            matched = [c for c in categories if category_name in c["name"]]
            if matched:
                category_id = matched[0]["id"]
            else:
                cat_list = "、".join([c["name"] for c in categories])
                return f"未找到分类「{category_name}」，可用分类：{cat_list}"

        # 第二步：处理图片（优先使用参数，其次使用上下文中注入的图片）
        effective_base64 = image_base64 or ctx_image_base64
        print(f"[api_create_dish_smart] image_base64 param: {len(image_base64)} chars, ctx_image_base64: {len(ctx_image_base64)} chars, effective: {len(effective_base64)} chars")
        final_image_url = image_url
        if effective_base64 and not image_url:
            try:
                image_bytes = base64.b64decode(effective_base64)
            except Exception:
                return "图片解码失败，请提供有效的Base64编码"
            upload_resp = req.post(
                f"{base}/admin/common/upload",
                headers={"token": token},
                files={"file": ("dish.jpg", image_bytes, "image/jpeg")},
                timeout=30,
            )
            if upload_resp.status_code != 200:
                return f"图片上传失败，HTTP状态码：{upload_resp.status_code}"
            upload_data = upload_resp.json()
            if upload_data.get("code") != 1:
                return f"图片上传失败：{upload_data.get('msg', '未知错误')}"
            final_image_url = upload_data.get("data", "")

        # 第三步：创建菜品（图片可选）
        body = {
            "name": name,
            "categoryId": category_id,
            "price": price,
            "image": final_image_url,
            "description": description or f"{name}，新鲜美味",
            "status": 1,
            "flavors": [],
        }
        if flavors:
            import json
            try:
                body["flavors"] = json.loads(flavors)
            except json.JSONDecodeError:
                pass

        create_resp = req.post(
            f"{base}/admin/dish",
            headers=_java_headers(token),
            json=body,
            timeout=15,
        )
        if create_resp.status_code == 401:
            return "认证已过期，请重新登录后再试"
        create_data = create_resp.json()
        if create_data.get("code") != 1:
            return f"创建菜品失败：{create_data.get('msg', '未知错误')}"

        # 找到匹配的分类名
        cat_display = categories[0]["name"]
        if category_name:
            matched = [c for c in categories if category_name in c["name"]]
            if matched:
                cat_display = matched[0]["name"]

        return f"菜品「{name}」上架成功！\n- 价格：¥{price}\n- 分类：{cat_display}\n- 图片：已上传\n- 状态：在售"

    return [
        api_list_categories,
        api_query_dish_page,
        api_get_dish_detail,
        api_update_dish,
        api_delete_dish,
        api_toggle_dish_status,
        api_upload_image,
        api_create_dish_smart,
    ]


# 所有工具列表，供Agent使用
def get_all_tools(token: str = "", image_base64: str = ""):
    """获取所有可用工具，有token时额外包含Java API操作工具"""
    tools = [
        query_my_dishes,
        query_competitor_prices,
        query_today_orders,
        query_order_trend,
        query_shop_info,
        query_hot_dishes,
        query_dish_status_monitor,
        search_business_knowledge,
        web_search,
    ]
    if token:
        tools.extend(_create_java_tools(token, image_base64))
    return tools
