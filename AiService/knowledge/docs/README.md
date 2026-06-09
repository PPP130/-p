# Sky Take-out 苍穹外卖

多门店外卖点餐平台，基于 Spring Boot 构建，支持管理后台（PC端）和用户端（微信小程序）双端交互。

## 项目简介

苍穹外卖是一个面向餐饮行业的多门店外卖管理系统，从单店模式扩展为多门店加盟模式，包含店铺注册审批、门店数据隔离、订单全流程管理等功能。管理端提供店铺运营、菜品管理、订单处理、数据报表等能力；用户端通过微信小程序完成点餐、支付、订单追踪等操作。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 开发语言 | Java | 8 |
| 应用框架 | Spring Boot | 2.7.3 |
| ORM框架 | MyBatis | 2.2.0 |
| 数据库 | MySQL | - |
| 连接池 | Druid | 1.2.1 |
| 缓存 | Redis | - |
| 接口文档 | Knife4j (Swagger) | 3.0.2 |
| 认证鉴权 | JWT (jjwt) | 0.9.1 |
| 文件存储 | 阿里云 OSS | 3.10.2 |
| 移动支付 | 微信支付 | 0.4.8 |
| 实时通信 | WebSocket | - |
| 分页插件 | PageHelper | 1.3.0 |
| Excel导出 | Apache POI | 3.16 |
| 构建工具 | Maven | - |

## 项目结构

```
sky-take-out/
├── sky-common          # 公共模块 - 工具类、常量、异常、配置属性
│   ├── constant/       # 常量定义
│   ├── context/        # ThreadLocal上下文（当前用户/店铺ID）
│   ├── exception/      # 自定义业务异常
│   ├── properties/     # 配置属性类（JWT、OSS、微信）
│   ├── result/         # 统一响应封装 Result<T>、PageResult
│   └── utils/          # 工具类（JWT、OSS、HTTP、微信支付）
│
├── sky-pojo            # 数据对象模块 - 实体、DTO、VO
│   ├── entity/         # 数据库实体类（11张表）
│   ├── dto/            # 请求数据传输对象
│   └── vo/             # 响应视图对象
│
├── sky-server          # 主服务模块 - 业务逻辑实现
│   ├── controller/
│   │   ├── admin/      # 管理端接口（9个Controller）
│   │   └── user/       # 用户端接口（8个Controller）
│   ├── service/        # 业务服务层
│   ├── mapper/         # MyBatis数据访问层
│   ├── config/         # 配置类（MVC、Redis、OSS、WebSocket）
│   ├── interceptor/    # JWT拦截器（管理端/用户端）
│   ├── annotation/     # 自定义注解 @AutoFill
│   ├── aspect/         # AOP切面（自动填充审计字段）
│   ├── handler/        # 全局异常处理
│   └── websocket/      # WebSocket服务（订单实时推送）
│
└── sql/                # 数据库迁移脚本
```

## 核心功能

### 管理端（PC后台）

- **员工管理** — 员工登录登出、增删改查、启停用、密码修改，员工关联门店
- **门店管理** — 门店注册申请、审批（通过/驳回）、启停用、营业状态切换，门店数据隔离
- **分类管理** — 菜品分类和套餐分类的增删改查、启停用
- **菜品管理** — 菜品及口味的增删改查、批量删除、启停售，支持Redis缓存
- **套餐管理** — 套餐及关联菜品的增删改查、批量删除、启停售，支持Redis缓存
- **订单管理** — 订单搜索、查看详情、接单/拒单/取消/派送/完成，各状态订单数量统计
- **数据统计** — 营业额统计、订单统计、用户增长统计、销量Top10、营业数据概览、Excel报表导出
- **工作台** — 今日数据总览、菜品/套餐/订单概况

### 用户端（微信小程序）

- **微信登录** — 基于 wx.login 获取 openid，自动注册新用户，JWT鉴权
- **点餐浏览** — 按分类查看菜品（含口味）、套餐及详情
- **购物车** — 添加/减少/清空商品，按门店隔离
- **下单支付** — 从购物车提交订单、微信支付、订单状态追踪
- **订单操作** — 查看历史订单、订单详情、再来一单、催单（WebSocket推送）、取消订单
- **地址管理** — 收货地址增删改查、设置默认地址

## 数据库设计

系统包含以下核心数据表：

| 表名 | 说明 | 备注 |
|------|------|------|
| `employee` | 员工表 | 关联shop_id |
| `shop` | 门店表 | 多门店核心表 |
| `category` | 分类表 | 1=菜品分类，2=套餐分类，关联shop_id |
| `dish` | 菜品表 | 关联shop_id |
| `dish_flavor` | 菜品口味表 | 关联dish_id |
| `setmeal` | 套餐表 | 关联shop_id |
| `setmeal_dish` | 套餐菜品关系表 | 关联setmeal_id |
| `orders` | 订单表 | 关联shop_id，含6种状态流转 |
| `order_detail` | 订单明细表 | 关联order_id |
| `shopping_cart` | 购物车表 | 关联shop_id |
| `address_book` | 用户地址表 | 平台级，不关联门店 |
| `user` | 用户表 | 平台级，存储微信openid |

## 关键技术设计

### 双端JWT鉴权

管理端和用户端使用独立的JWT密钥和Token传递方式：
- 管理端：请求头 `token`，密钥 `itcast`，有效期2小时
- 用户端：请求头 `authentication`，密钥 `itheima`，有效期2小时

### ThreadLocal上下文传递

通过 `BaseContext` 在 ThreadLocal 中存储当前请求的员工ID和门店ID，由JWT拦截器设置，请求结束后清理，实现服务层和数据访问层的透明上下文传递。

### AOP自动填充

自定义 `@AutoFill` 注解配合 `AutoFillAspect` 切面，在插入/更新操作前通过反射自动填充 `createTime`、`updateTime`、`createUser`、`updateUser` 审计字段。

### Redis缓存策略

- 菜品分类查询和套餐分类查询结果缓存至Redis（TTL 1小时）
- 菜品/套餐的增删改及状态变更时通过 `@CacheEvict` 主动清除缓存
- 门店营业状态存储在Redis中，支持快速查询

### 多门店数据隔离

核心业务表（员工、分类、菜品、套餐、订单、购物车）均添加 `shop_id` 字段，通过ThreadLocal中的门店ID实现数据隔离。用户表和地址表为平台级，用户可在多个门店下单。

### WebSocket实时推送

`WebSocketServer` 维护在线管理端连接，支持新订单通知和用户催单消息的实时推送。

## 快速开始

### 环境要求

- JDK 8+
- Maven 3.6+
- MySQL 5.7+
- Redis 6.0+

### 启动步骤

1. **初始化数据库**

   ```bash
   # 创建数据库
   mysql -u root -p -e "CREATE DATABASE sky_take_out DEFAULT CHARACTER SET utf8mb4;"

   # 导入基础表结构（根据实际SQL文件）
   mysql -u root -p sky_take_out < sql/sky.sql

   # 执行多门店迁移脚本
   mysql -u root -p sky_take_out < sql/shop_migration.sql
   ```

2. **修改配置**

   编辑 `sky-server/src/main/resources/application-dev.yml`，配置数据库连接、Redis地址、阿里云OSS密钥、微信小程序AppID和密钥。

3. **启动应用**

   ```bash
   mvn clean package -DskipTests
   java -jar sky-server/target/sky-server.jar
   ```

4. **访问接口文档**

   启动后访问 Knife4j 文档：`http://localhost:8080/doc.html`

## 订单状态流转

```
待付款(1) → 待接单(2) → 已接单(3) → 配送中(4) → 已完成(5)
                ↓                        ↑
            已取消(6) ←───────────────────┘
```

- 用户端可操作：提交订单、支付、取消（待接单前）、催单
- 管理端可操作：接单、拒单、取消、派送、完成

## 接口总览

### 管理端接口（前缀 `/admin`）

| 模块 | 路径 | 说明 |
|------|------|------|
| 员工 | `/admin/employee` | 登录登出、CRUD、启停用、改密码 |
| 门店 | `/admin/shop` | 注册、审批、启停用、营业状态 |
| 分类 | `/admin/category` | CRUD、启停用 |
| 菜品 | `/admin/dish` | CRUD、批量删除、启停售 |
| 套餐 | `/admin/setmeal` | CRUD、批量删除、启停售 |
| 订单 | `/admin/order` | 搜索、详情、接单/拒单/取消/派送/完成、统计 |
| 文件 | `/admin/common` | 图片上传至OSS |
| 报表 | `/admin/report` | 营业额/订单/用户统计、Top10、Excel导出 |
| 工作台 | `/admin/workspace` | 今日数据、菜品/套餐/订单概况 |

### 用户端接口（前缀 `/user`）

| 模块 | 路径 | 说明 |
|------|------|------|
| 用户 | `/user/user` | 微信登录 |
| 菜品 | `/user/dish` | 按分类查询菜品列表 |
| 套餐 | `/user/setmeal` | 按分类查询套餐、套餐详情 |
| 分类 | `/user/category` | 按类型查询分类 |
| 购物车 | `/user/shoppingCart` | 查看/添加/减少/清空 |
| 订单 | `/user/order` | 提交/历史/详情/再来一单/催单/取消/支付 |
| 地址 | `/user/addressBook` | CRUD、默认地址 |
| 门店 | `/user/shop` | 营业状态查询 |

## 模块依赖关系

```
sky-server  ──→  sky-pojo  ──→  sky-common
   (业务逻辑)      (数据对象)      (公共基础)
```
