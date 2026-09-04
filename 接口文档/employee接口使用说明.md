# Fresh Market 后端接口使用说明（管理端员工）

> 本文档面向前端开发，描述 **员工管理** 的 6 个接口（登录 / 新增 / 分页查询 / 按id查询 / 修改 / 删除），均已**实测通过**（2026-08-31 实测，示例为真实响应）。
> 通用约定与《goods和login接口使用说明.md》一致：统一响应 `{code, msg, data}`、时间格式 `yyyy-MM-dd HH:mm`、跨域走 dev server 代理。

## 1. 鉴权

- 除登录外，员工接口**全部需要管理端 token**（`/admin/**` 拦截）。
- 请求头：`token: <登录返回的 token>`（注意是 `token`，不是用户端的 `authentication`）。
- 未带/无效 token：**HTTP 401，响应体为空**。

## 2. 接口详情

### 2.1 员工登录

`POST /admin/employee/login`（无需 token，JSON）

请求体：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码（明文传输，后端做 MD5 比对） |

```json
{ "username": "admin", "password": "123456" }
```

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "id": 1,
    "userName": "admin",
    "name": "管理员",
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJlbXBJZCI6MSwiZXhwIjoxNzg4ODg3Nzg0fQ.lMWRkh0Tc0q7_jsGIKxEX6du8UrTF_5hHWtK1jyqoWQ"
  }
}
```

失败响应（msg 为以下三者之一）：

```json
{ "code": 0, "msg": "密码错误", "data": null }
{ "code": 0, "msg": "账号不存在", "data": null }
{ "code": 0, "msg": "账号被锁定", "data": null }
```

联调测试账号：`admin / 123456`。（此接口与《goods和login接口使用说明.md》4.1 为同一接口）

### 2.2 新增员工

`POST /admin/employee/add`（需 token，JSON）

请求体：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| name | string | 是 | 姓名 |
| username | string | 是 | 登录用户名，**全局唯一** |
| password | string | 是 | 登录密码（明文传输，后端 MD5 加密存储） |
| phone | string | 是 | 手机号（11 位） |
| sex | string | 是 | 性别 0女 1男 |
| idNumber | string | 是 | 身份证号（18 位） |

```json
{
  "name": "张三",
  "username": "zhangsan",
  "password": "123456",
  "phone": "13800138000",
  "sex": "1",
  "idNumber": "110101199001011234"
}
```

成功响应（不返回新建 id，需要的话刷新列表）：

```json
{ "code": 1, "msg": null, "data": null }
```

失败响应：

```json
{ "code": 0, "msg": "手机号不能为空", "data": null }
{ "code": 0, "msg": "zhangsan已存在", "data": null }
```

**新增规则**：
- 6 个字段**实际全部必填**（数据库均 NOT NULL），缺失时返回 `"XX不能为空"`（XX 为字段中文名）。
- `username` 重复时返回 `"用户名已存在"`（提示会带上重复的用户名，如上）。
- 新员工默认**启用**状态（status=1），无需传 status。
- `createTime`/`createUser` 等审计字段由后端自动记录（操作人 = 当前登录员工）。

### 2.3 分页查询员工

`GET /admin/employee/page`（需 token）

Query 参数（全部可选，有默认值）：

| 参数 | 类型 | 默认 | 说明 |
|---|---|---|---|
| pageNum | number | 1 | 页码，从 1 开始 |
| pageSize | number | 10 | 每页条数 |
| name | string | - | 员工**姓名**模糊匹配，空/不传则不过滤 |

请求示例：

```
GET /admin/employee/page?pageNum=1&pageSize=10&name=张
```

成功响应（按 id **倒序**，最新员工在前；records 结构见 3.1）：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "total": 1,
    "records": [
      {
        "id": 2,
        "name": "张三",
        "username": "zhangsan",
        "password": null,
        "phone": "13800138000",
        "sex": "1",
        "idNumber": "110101199001011234",
        "status": 1,
        "createTime": "2026-08-31 17:16",
        "updateTime": "2026-08-31 17:16",
        "createUser": 1,
        "updateUser": 1
      }
    ]
  }
}
```

| data 字段 | 类型 | 说明 |
|---|---|---|
| total | number | 符合条件的**总记录数**（用于计算总页数） |
| records | array | 当前页员工列表（结构见 3.1） |

### 2.4 根据id查询员工

`GET /admin/employee/get`（需 token）

Query 参数：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | number | 是 | 员工 id |

请求示例：

```
GET /admin/employee/get?id=2
```

成功响应（data 为单个员工对象，结构见 3.1）：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "id": 2,
    "name": "张三",
    "username": "zhangsan",
    "password": null,
    "phone": "13800138000",
    "sex": "1",
    "idNumber": "110101199001011234",
    "status": 1,
    "createTime": "2026-08-31 17:16",
    "updateTime": "2026-08-31 17:16",
    "createUser": 1,
    "updateUser": 1
  }
}
```

id 不存在时：`{ "code": 1, "msg": null, "data": null }`（注意是成功但 data 为 null，不是报错）。

**用途**：编辑页回显数据时调用。

### 2.5 修改员工

`POST /admin/employee/update`（需 token，JSON）

请求体：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | number | 是 | 员工 id |
| name | string | 否 | 姓名 |
| phone | string | 否 | 手机号 |
| sex | string | 否 | 性别 0女 1男 |
| idNumber | string | 否 | 身份证号 |
| status | number | 否 | 1:启用 0:禁用（**启用/禁用复用本接口**） |

```json
{ "id": 3, "phone": "13900139000" }
```

成功响应（不返回数据，需要的话重新查询）：

```json
{ "code": 1, "msg": null, "data": null }
```

失败响应：

```json
{ "code": 0, "msg": "员工id不能为空", "data": null }
{ "code": 0, "msg": "不能禁用当前登录的账号", "data": null }
```

**修改规则（部分更新语义，与商品修改一致）**：
- 只修改请求体中**传了的字段**，没传的字段保持原值不被覆盖。例如只传 `{"id":3,"phone":"13900139000"}` 则仅改手机号，姓名/性别/身份证号等都不动（实测验证）。
- **启用/禁用就是改 `status`**，复用本接口（如 `{"id":3,"status":0}` 禁用）；被禁用的员工登录会被拒绝：`{"code":0,"msg":"账号被锁定"}`（实测验证）。
- **不能禁用自己**：把当前 token 对应的员工禁用会被拒绝（防止把自己锁在门外）。
- **用户名和密码不可通过本接口修改**（请求体中传了也会被忽略）。
- `updateTime`/`updateUser` 由后端自动维护，无需传。
- id 不存在时返回成功（空操作，不报错，与商品修改一致）。

### 2.6 根据id删除员工

`DELETE /admin/employee/delete`（需 token）

Query 参数：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | number | 是 | 员工 id |

请求示例：

```
DELETE /admin/employee/delete?id=2
```

成功响应：

```json
{ "code": 1, "msg": null, "data": null }
```

失败响应（删除的 id 是当前登录员工自己时）：

```json
{ "code": 0, "msg": "不能删除当前登录的账号", "data": null }
```

**删除规则**：
- **物理删除**（记录直接从库里移除），建议前端删除前弹确认框。
- 删除不存在的 id 也返回成功（不报错）。
- **不能删除自己**：删除当前 token 对应的员工会被拒绝（防止把自己删了导致系统无人可登录）。

## 3. 数据模型

### 3.1 Employee（员工）

查询类接口（分页、按 id 查）返回的都是 Employee 结构，**`password` 恒为 null**（后端返回前已清空，不会泄露密码哈希）：

| 字段 | 类型 | 说明 |
|---|---|---|
| id | number | 员工 id |
| name | string | 姓名 |
| username | string | 登录用户名（唯一） |
| password | null | 恒为 null，前端无需处理 |
| phone | string | 手机号 |
| sex | string | 性别 0女 1男 |
| idNumber | string | 身份证号 |
| status | number | 1:启用 0:禁用（禁用后该员工无法登录） |
| createTime | string \| null | 创建时间（历史老数据可能为 null） |
| updateTime | string \| null | 更新时间 |
| createUser | number \| null | 创建人员工 id |
| updateUser | number \| null | 最后修改人员工 id |

## 4. 前端对接注意事项

1. **密码字段不回显**：所有查询接口的 `password` 都是 null，编辑页不要依赖它；"修改密码"目前**无对应接口**（见第 2 条）。
2. **员工管理尚未开发的接口**：修改密码、退出登录。编辑页的保存按钮可直接对接 2.5 修改接口（姓名/手机号/性别/身份证号可改，用户名/密码不可改）；启用/禁用开关也走 2.5 传 `status`。
3. 新增员工 6 个字段全必填，前端表单建议全部标红星；`username` 唯一，重复时后端返回 `"xxx已存在"`；**用户名创建后不可改**，编辑页建议将用户名输入框禁用。
4. 列表按 id 倒序（最新在前）；姓名搜索是**模糊匹配**，仅匹配 `name` 字段，不搜用户名/手机号。
5. admin 账号（id=1）是内置管理员，**无法被删除或禁用**（删除/禁用自己都会被拒绝），前端可对 id=1 的删除、禁用按钮做隐藏处理，体验更好。
