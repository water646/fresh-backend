# Fresh Market 后端接口使用说明（C端用户）

> 本文档面向前端开发，描述 **C端（`/user/**`）** 已实现并**实测通过**的 9 个接口（2026-08-28 / 09-02 实测，示例为真实响应）。
> 通用约定（统一响应、时间格式、跨域代理）与《goods和login接口使用说明.md》一致，此处只列要点和**与管理端不同的地方**。

## 1. 基本信息

| 项目 | 说明 |
|---|---|
| Base URL | `http://localhost:8080` |
| 在线接口文档 | http://localhost:8080/doc.html （knife4j，分组"**用户端接口**"） |
| 统一响应 | `{ "code": 1, "msg": null, "data": ... }`，**1 成功 / 0 失败** |
| 数据格式 | JSON，UTF-8 |

## 2. 鉴权（与管理端不同，注意！）

1. C端接口的 **token 请求头名是 `authentication`**（管理端是 `token`，两者不同）：
   ```
   authentication: eyJhbGciOiJIUzI1NiJ9...
   ```
2. 拦截范围：`/user/**`；放行 `sendMsg`、`login`（登录本身无需 token）。
3. token 缺失/无效/过期：**HTTP 401，响应体为空**（不是 JSON），前端拦截器统一处理。（401 只针对**真实存在**的接口；访问不存在的路径是 404。）
4. token 获取方式：调登录接口（3.2），有效期约 8 天，无刷新机制。

## 3. 接口详情

### 3.1 发送登录验证码

`POST /user/user/sendMsg`（无需 token，JSON）

请求体：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| phone | string | 是 | 手机号，需符合大陆手机号格式（`1`开头、第二位`3-9`、共11位） |

```json
{ "phone": "13800138000" }
```

成功响应：

```json
{ "code": 1, "msg": null, "data": null }
```

手机号不合法时：

```json
{ "code": 0, "msg": "手机号格式不正确", "data": null }
```

**验证码机制说明**：
- 后端生成 **6 位数字验证码**，存入 Redis，**有效期 5 分钟**，过期自动失效。
- 每次调用都会**覆盖**旧验证码（同一手机号以最后一次为准）。
- **暂无短信服务商**：验证码不会真发到手机，而是打印在**后端控制台日志**里（格式：`为手机号 xxx 生成登录验证码：xxxxxx`）。联调时请后端同学看日志报码。
- 验证码不通过接口返回给前端，前端只负责"输入手机号 → 点获取验证码 → 收码填码"的交互。

### 3.2 用户登录

`POST /user/user/login`（无需 token，JSON）

请求体：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| phone | string | 是 | 手机号（同 3.1 的格式要求） |
| code | string | 是 | 6 位验证码（3.1 获取） |

```json
{ "phone": "13800138001", "code": "747113" }
```

成功响应（data 为用户 id + token）：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "id": 1,
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3ODc5NzQxMDksInVzZXJJZCI6MX0.Cz9Fyxt2Q65KYEf-4_lNJJ6TYtKggEivKSX2PNApMTU"
  }
}
```

失败响应（msg 为以下之一）：

```json
{ "code": 0, "msg": "验证码错误", "data": null }
{ "code": 0, "msg": "验证码已失效，请重新获取", "data": null }
{ "code": 0, "msg": "账号被锁定", "data": null }
{ "code": 0, "msg": "手机号格式不正确", "data": null }
```

**登录规则说明**：
- **验证码只能用一次**：校验成功后立即作废，重复提交同一验证码会返回"验证码已失效"。
- **自动注册**：手机号首次登录会自动创建用户（无需单独注册流程），返回新用户 id；之后每次登录返回同一 id。
- **禁用拦截**：`status=0` 的用户登录返回"账号被锁定"。
- token 请存入 localStorage，后续请求放入 `authentication` 请求头（见第 2 节）。

### 3.3 分页查询在售商品

`GET /user/goods/page`（**需 token**）

Query 参数（全部可选，有默认值）：

| 参数 | 类型 | 默认 | 说明 |
|---|---|---|---|
| pageNum | number | 1 | 页码，从 1 开始 |
| pageSize | number | 10 | 每页条数 |
| name | string | - | 商品名称**模糊**匹配 |
| categoryId | number | - | 分类 id **精确**匹配（配合 3.5 分类列表） |

```
GET /user/goods/page?pageNum=1&pageSize=10&categoryId=1
```

成功响应（**只返回在售商品**，records 结构为 GoodsVO，见管理端文档 5.1，含 `categoryName`）：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "total": 3,
    "records": [
      {
        "id": 1,
        "name": "apple",
        "categoryId": 1,
        "categoryName": "水果",
        "price": 9.90,
        "image": "",
        "description": "fresh",
        "status": 1,
        "stock": 99
      }
    ]
  }
}
```

注意：**下架（status=0）商品不会出现在结果里**，status 值恒为 1，前端可直接忽略该字段。

### 3.4 根据 id 查询在售商品详情

`GET /user/goods/get`（**需 token**）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | number | 是 | 商品 id |

成功响应（data 为 GoodsVO）：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "id": 1,
    "name": "apple",
    "categoryId": 1,
    "categoryName": "水果",
    "price": 9.90,
    "image": "",
    "description": "fresh",
    "status": 1,
    "stock": 99
  }
}
```

id 不存在**或商品已下架**时：`{ "code": 1, "msg": null, "data": null }`。

### 3.5 查询所有启用的分类

`GET /user/category/list`（**需 token**，不分页）

成功响应（按 sort、id 升序，**只含启用分类**；字段说明见管理端《category接口使用说明.md》第 2 节）：

```json
{
  "code": 1,
  "msg": null,
  "data": [
    { "id": 1, "name": "水果", "status": 1, "sort": 1, "createTime": "2026-08-28 13:48", "createUser": null, "updateTime": "2026-08-28 14:10", "updateUser": 1 },
    { "id": 2, "name": "蔬菜", "status": 1, "sort": 2, "createTime": "2026-08-28 13:48", "createUser": null, "updateTime": "2026-08-28 13:48", "updateUser": null }
  ]
}
```

用途：首页分类栏 / 商品筛选标签，拿 `id` 传给 3.3 的 `categoryId`。

### 3.6 查询所有在售商品

`GET /user/goods/list`（**需 token**，不分页、无参数）

成功响应（**所有在售商品**，按 id 升序，records 结构同 3.3 的 GoodsVO）：

```json
{
  "code": 1,
  "msg": null,
  "data": [
    { "id": 1, "name": "apple", "categoryId": 1, "categoryName": "水果", "price": 9.90, "image": "", "description": "fresh", "status": 1, "stock": 99 },
    { "id": 2, "name": "banana", "categoryId": 1, "categoryName": "水果", "price": 5.50, "image": "", "description": "fresh banana", "status": 1, "stock": 50 },
    { "id": 5, "name": "VS Code", "categoryId": 2, "categoryName": "蔬菜", "price": 500.00, "image": "http://localhost:8080/uploads/2026/08/28/195ae395e58e440b9ae5463bb20ecafe.png", "description": "fresh VS Code", "status": 1, "stock": 1 }
  ]
}
```

与 3.3 的选择：数据量小（几十条）直接用本接口一次拉全；商品多了（几百条以上）建议改用 3.3 的分页接口，需要时后端可给本接口加 categoryId 过滤参数。

### 3.6b 按分类查询在售商品（带缓存）

`GET /user/goods/listByCategory`（**需 token**，不分页）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| categoryId | number | 否 | 分类 id **精确**匹配；**不传或传 0 时返回全部在售商品**（等价 3.6） |

```
GET /user/goods/listByCategory?categoryId=2
```

成功响应（该分类下的在售商品，按 id 升序，结构同 3.3 的 GoodsVO；2026-09-02 实测）：

```json
{
  "code": 1,
  "msg": null,
  "data": [
    { "id": 3, "name": "onion", "categoryId": 2, "categoryName": "蔬菜", "price": 4.99, "image": "", "description": "fresh", "status": 1, "stock": 30 },
    { "id": 17, "name": "沙地西红柿", "categoryId": 2, "categoryName": "蔬菜", "price": 6.50, "image": null, "description": "内蒙古沙地产区，沙瓤起砂，生吃做菜皆宜，约500g/份", "status": 1, "stock": 50 }
  ]
}
```

| 场景 | 响应（实测） |
|---|---|
| categoryId 存在（如 1/2） | 该分类在售商品列表 |
| categoryId 不存在（如 999） | `"data": []`（空数组，不是 null） |
| 不传 categoryId | 全部在售商品（与 3.6 相同） |
| 未带/无效 token | HTTP 401，响应体为空 |

**缓存机制（重要）**：本接口的结果缓存在 Redis（key 为 `goods:分类id`），**管理端新增/修改/删除商品时后端会自动删除对应分类的缓存**，C 端下次查询即拿到最新数据——实测改价后立即返回新价格，前端**无需**为数据新鲜度做任何额外处理。

**与 3.3 / 3.6 的选择**：首页分类页签切换用本接口最合适（无分页开销且走缓存）；需要分页或搜索时用 3.3；一次性拉全量用 3.6。

`GET /user/user/get`（**需 token**，无参数，返回的是 token 对应的用户）

成功响应（data 为 UserVO，**不含身份证号、状态等敏感字段**；未完善资料的字段为 null）：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "id": 8,
    "name": "Jerry",
    "phone": "13800138007",
    "sex": null,
    "avatar": null
  }
}
```

### 3.8 修改当前登录用户信息

`POST /user/user/update`（**需 token**，JSON；改的是 token 对应的用户，无需传 id）

请求体（**部分更新**：只改传了的字段，没传的保持原值）：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| name | string | 否 | 姓名 |
| sex | string | 否 | 性别（自由文本，建议 "男"/"女"） |
| avatar | string | 否 | 头像 URL（可先用管理端文档 4.7 的上传接口传图） |
| idNumber | string | 否 | 身份证号（唯一，与他人重复会返回"已存在"错误） |

```json
{ "name": "Tom", "sex": "male", "avatar": "http://localhost:8080/uploads/a.png", "idNumber": "110101199001011234" }
```

成功响应：

```json
{ "code": 1, "msg": null, "data": null }
```

**不可修改**：手机号（登录凭证）、用户 id、状态（禁用由管理端控制）。改完后可用 3.7 查看最新信息（注意 `idNumber` 不会回显）。

## 4. 数据模型

### 4.1 UserLoginVO（登录返回）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | number | 用户 id |
| token | string | jwt 令牌，放入 `authentication` 请求头 |

### 4.2 UserVO（当前用户信息，3.7 返回）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | number | 用户 id |
| name | string \| null | 姓名 |
| phone | string | 手机号 |
| sex | string \| null | 性别 |
| avatar | string \| null | 头像 URL |

用户表核心字段：`id`、`name`、`phone`、`sex`、`avatar`、`idNumber`、`status`（1:启用 0:禁用，禁用不允许登录；`idNumber`/`status` 不对C端回显）。收货地址不在用户表上，见第 5 节说明。

## 5. 当前后端能力边界（C端）

- 已通：登录链路（验证码 + 登录 + token）、**商品浏览**（在售商品分页/详情/全量/**按分类列表（带缓存）**）、**分类列表**。
- C端无商品搜索之外的筛选（如价格区间）、无轮播图/推荐位等运营接口。
- **购物车已开发**（增/查/删），见《shoppingCart接口使用说明.md》；订单、地址簿等接口未开发。收货地址将放在**地址簿**（`address_book` 表，含省市区三级 + 详细地址 + 默认地址标记，一个用户可有多条），实体类已建好，接口待开发；下单时从地址簿选择，订单表的 `addressBookId` 即对应此表。
- 跨域：与管理端相同，后端未配 CORS，开发时请用 Vite 等 dev server 代理（配置见《goods和login接口使用说明.md》第 6 节）。
