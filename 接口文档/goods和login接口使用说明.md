# Fresh Market 后端接口使用说明（管理端）

> 本文档面向前端开发，描述后端当前已实现并**实测通过**的 8 个管理端接口。
> 所有示例响应均为真实返回（2026-08-28 / 09-02 实测）。

## 1. 基本信息

| 项目 | 说明 |
|---|---|
| Base URL | `http://localhost:8080` |
| 在线接口文档 | http://localhost:8080/doc.html （knife4j，分组"管理端接口"） |
| 数据格式 | JSON，UTF-8 |
| 时间字段格式 | 字符串 `yyyy-MM-dd HH:mm`（如 `"2026-08-27 15:48"`，无秒） |
| 金额字段 | 数字，单位**元**，两位小数（如 `9.90`） |

## 2. 统一响应结构

除 HTTP 401 外，所有接口返回统一信封：

```json
{ "code": 1, "msg": null, "data": ... }
```

| 字段 | 类型 | 说明 |
|---|---|---|
| code | number | **1 成功；0 失败** |
| msg | string | 失败原因（成功时为 null） |
| data | object/null | 业务数据，无数据时为 null |

## 3. 鉴权

1. 先调登录接口（4.1）拿到 `token`（JWT）。
2. 之后**所有 `/admin/**` 接口**在请求头携带：
   ```
   token: eyJhbGciOiJIUzI1NiJ9...
   ```
   注意请求头名就是小写的 `token`（不是 Authorization）。
3. token 缺失/无效/过期时：**HTTP 401，响应体为空**（不是 JSON！）。前端应在 axios/fetch 拦截器里统一处理 401 → 清除本地 token 并跳转登录页。
4. token 有效期约 8 天，无需做刷新逻辑。

## 4. 接口详情

### 4.1 员工登录

`POST /admin/employee/login`（无需 token）

请求体：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码（明文传输，后端做 MD5 比对） |

```json
{ "username": "admin", "password": "123456" }
```

成功响应（data 为登录用户信息 + 令牌）：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "id": 1,
    "userName": "admin",
    "name": "管理员",
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJlbXBJZCI6MX0.xxxxx"
  }
}
```

失败响应（msg 为以下三者之一）：

```json
{ "code": 0, "msg": "密码错误", "data": null }
{ "code": 0, "msg": "账号不存在", "data": null }
{ "code": 0, "msg": "账号被锁定", "data": null }
```

联调测试账号：`admin / 123456`。

### 4.2 分页查询商品

`GET /admin/goods/page`（需 token）

Query 参数（全部可选，有默认值）：

| 参数 | 类型 | 默认 | 说明 |
|---|---|---|---|
| pageNum | number | 1 | 页码，从 1 开始 |
| pageSize | number | 10 | 每页条数 |
| name | string | - | 商品名称**模糊**匹配，空/不传则不过滤 |
| categoryId | number | - | 分类 id **精确**匹配，不传则不过滤 |

请求示例：

```
GET /admin/goods/page?pageNum=1&pageSize=10&name=苹果
```

成功响应（records 为 GoodsVO，见 5.1）：

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

| data 字段 | 类型 | 说明 |
|---|---|---|
| total | number | 符合条件的**总记录数**（用于计算总页数） |
| records | array | 当前页商品列表（GoodsVO 结构见 5.1） |

### 4.3 新增商品

`POST /admin/goods/add`（需 token，`Content-Type: application/json`）

请求体：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| name | string | 是 | 商品名称（≤50 字） |
| categoryId | number | 是 | 所属分类 id（目前库里只有 id=1） |
| price | number | 是 | 价格，单位元，两位小数（如 `9.90`） |
| image | string | 否 | 图片 URL（暂无上传接口，可先留空） |
| description | string | 否 | 描述（≤500 字） |
| status | number | 否 | 1:在售 0:下架，建议默认传 1 |
| stock | number | 是 | 库存数量 |
| stockMode | number | 是 | 库存模式 |

```json
{
  "name": "banana",
  "categoryId": 1,
  "price": 5.50,
  "description": "fresh banana",
  "status": 1,
  "stock": 50,
  "stockMode": 1
}
```

成功响应（不返回新建 id，需要的话刷新列表）：

```json
{ "code": 1, "msg": null, "data": null }
```

### 4.4 根据 id 查询商品

`GET /admin/goods/get`（需 token）

Query 参数：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | number | 是 | 商品 id |

请求示例：

```
GET /admin/goods/get?id=1
```

成功响应（data 为单个 GoodsVO 对象，结构见 5.1）：

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

id 不存在时：`{ "code": 1, "msg": null, "data": null }`（注意是成功但 data 为 null，不是报错）。

**用途**：编辑页回显数据时调用。

### 4.5 修改商品

`POST /admin/goods/update`（需 token，`Content-Type: application/json`）

请求体字段与新增（4.3）相同，另加必填的 `id`：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | number | 是 | 商品 id |
| 其余字段 | - | 否 | 同 4.3 的新增字段 |

```json
{
  "id": 1,
  "name": "red apple",
  "categoryId": 1,
  "price": 29.90,
  "description": "big fresh",
  "status": 1,
  "stock": 50,
  "stockMode": 1
}
```

成功响应：

```json
{ "code": 1, "msg": null, "data": null }
```

**部分更新语义（重要）**：只修改请求体中**传了的字段**，没传的字段保持原值不被覆盖。例如只传 `{"id":1,"price":19.90}` 则仅改价格。`updateTime/updateUser` 由后端自动维护，无需传。商品的**上下架**也可以只传 `{"id":x,"status":0/1}` 走本接口，但更推荐用 4.8 专用的起售/停售接口。

### 4.6 根据 id 删除商品

`DELETE /admin/goods/delete`（需 token）

Query 参数：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | number | 是 | 商品 id |

请求示例：

```
DELETE /admin/goods/delete?id=3
```

成功响应：

```json
{ "code": 1, "msg": null, "data": null }
```

注意：**物理删除**（记录直接从库里移除，非逻辑删除）；删除不存在的 id 也返回成功（不报错）。建议前端删除前弹确认框。

### 4.7 文件上传（通用）

`POST /admin/common/upload`（需 token，`multipart/form-data`）

表单字段：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| file | file | 是 | 上传的文件 |

限制：**仅支持图片**（jpg / jpeg / png / gif / bmp / webp），单文件 ≤ 10MB。

请求示例：

```
POST /admin/common/upload
Content-Type: multipart/form-data

file: (二进制文件)
```

成功响应（data 为文件的完整访问 url）：

```json
{
  "code": 1,
  "msg": null,
  "data": "http://localhost:8080/uploads/2026/08/28/1b1cdb7e819d44a38c15963f83947357.png"
}
```

失败响应：

```json
{ "code": 0, "msg": "文件类型不允许，仅支持图片：[jpg, bmp, gif, png, jpeg, webp]", "data": null }
```

**使用说明**：
- 文件保存在后端所在机器的本地磁盘（按 `年/月/日` 分目录，UUID 重命名，原始文件名不保留）。
- 返回的 url 是**绝对地址**（host:port 与后端一致），`<img :src="url">` 可直接显示——`<img>` 加载图片不受跨域限制，无需走 dev server 代理；但要传给 axios 之外的场景注意别拼错域名端口。
- 该 url 可直接存入商品的 `image` 字段。
- **url 的访问不需要 token**（`/uploads/**` 是静态资源，不在鉴权范围内）。
- 后端换端口/换机器部署后，**历史 url 会失效**（文件还在磁盘上，但地址里的 host:port 变了）。

### 4.8 商品起售/停售

`PUT /admin/goods/startOrStop`（需 token，`Content-Type: application/json`）

请求体（只需要 2 个字段，其余字段传了也会被忽略）：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | number | 是 | 商品 id |
| status | number | 是 | **1 起售 / 0 停售**（只传这两个值） |

```json
{ "id": 12, "status": 0 }
```

成功响应：

```json
{ "code": 1, "msg": null, "data": null }
```

**规则（2026-09-02 实测）**：
- **停售立即对 C 端生效**：C 端的商品分页、详情（`get` 返回 `data: null`）、全量、按分类列表（含 Redis 缓存，后端自动清除）都查不到该商品；起售同理立即恢复，前端无需做任何延迟处理。
- 与 4.5 修改接口只传 `status` 效果等价，但本接口只动 `status` 一个字段，语义更明确，**上下架请优先用本接口**。
- 不存在的 id 静默返回成功（空操作，不报错，与修改/删除一致）。
- `updateTime` 由数据库自动维护，无需传。

## 5. 数据模型

### 5.1 GoodsVO（商品查询返回）

查询类接口（分页、按 id 查）返回的都是 **GoodsVO**——联表带出分类名称，且不含 `stockMode` 和审计字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| id | number | 主键 |
| name | string | 商品名称 |
| categoryId | number | 分类 id |
| categoryName | string \| null | **分类名称**（联表查询；分类被删除时为 null） |
| price | number | 价格（元） |
| image | string \| null | 图片地址（上传接口返回的 url） |
| description | string \| null | 描述 |
| status | number | 1:在售 0:下架 |
| stock | number | 库存 |

注意：新增（4.3）/修改（4.5）的**请求体**仍含 `stockMode` 字段，只是查询响应里不返回。

### 5.2 EmployeeLoginVO（登录返回）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | number | 员工 id |
| userName | string | 用户名（注意是 `userName`，n 大写） |
| name | string | 姓名 |
| token | string | JWT 令牌 |

## 6. 前端对接注意事项（重要）

1. **后端没有配置 CORS**。开发服务器（如 Vite 的 5173 端口）直接请求 8080 会被浏览器拦截，请用 dev server 代理：

   ```js
   // vite.config.js
   export default {
     server: {
       proxy: {
         '/api': {
           target: 'http://localhost:8080',
           changeOrigin: true,
           rewrite: path => path.replace(/^\/api/, '')
         }
       }
     }
   }
   ```

   请求时用 `/api/admin/goods/page` 这样的路径。（或者反馈后端，让后端加 CORS 配置。）

2. **401 响应体为空**，不能按 JSON 解析，拦截器里要先判断 HTTP 状态码。

3. 建议的 axios 封装约定：
   - 请求拦截器：从 localStorage 取 token 注入 `token` 请求头；
   - 响应拦截器：HTTP 401 → 清 token 跳登录；`code === 0` → 用 `msg` 弹错误提示；`code === 1` → 取 `data`。

4. 后端启动依赖本机 MySQL / Redis / RabbitMQ，联调前确认后端已在 8080 端口运行。

## 7. 当前后端能力边界（避免前端踩空）

- **category（分类）已有完整 CRUD**，见《category接口使用说明.md》：商品页的分类筛选下拉可用 `/admin/category/page` 拉真实数据。
- **employee 已支持 登录 / 新增 / 分页查询 / 按id查询 / 修改（含启用/禁用）/ 删除**，见《employee接口使用说明.md》；修改密码和退出接口尚未开发（前端删除本地 token 即可退出）。
- **report（数据统计）已支持 营业额统计 / 用户统计 / 订单统计 / 销量排名Top10 / 导出Excel报表**，见《report接口使用说明.md》；数据看板页可直接对接。
- **WebSocket 来单提醒已支持**（用户支付成功后实时推送到商家端，登录后连 `ws://8080/ws/{sid}`），消息格式与前端对接方式见《orders接口使用说明.md》第 5 章。
- 商品已支持**新增 / 分页查询 / 按 id 查 / 修改 / 删除 / 起售停售（4.8，上下架专用接口）**。
- 文件上传已支持（4.7），但仅限图片、存后端本地磁盘；OSS 云存储待密钥配置后启用，届时 url 规则会变。
- 批量删除、按分类统计等暂无，需要可反馈后端补充。
