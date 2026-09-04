# Fresh Market 后端接口使用说明（分类管理）

> 本文档面向前端开发，描述分类（category）的 5 个管理端接口，均已**实测通过**（2026-08-28 实测，示例为真实响应）。
> 通用约定（Base URL、统一响应、鉴权、时间格式）与《goods和login接口使用说明.md》完全一致，此处只列要点。

## 1. 通用约定（与 goods 文档一致）

| 项目 | 说明 |
|---|---|
| Base URL | `http://localhost:8080` |
| 统一响应 | `{ "code": 1, "msg": null, "data": ... }`，**1 成功 / 0 失败** |
| 鉴权 | 所有 `/admin/**` 接口需登录后带请求头 `token: xxx`（登录方式见 goods 文档 4.1）；缺失/过期返回 **HTTP 401 且响应体为空** |
| 时间格式 | `yyyy-MM-dd HH:mm`（无秒） |

## 2. 数据模型：Category（分类）

查询接口直接返回 Category 实体（无 VO）：

| 字段 | 类型 | 说明 |
|---|---|---|
| id | number | 主键，自增 |
| name | string | 分类名称，**全局唯一** |
| status | number | 1:启用 0:禁用 |
| sort | number | 排序，数字越小越靠前（列表按 sort、id 升序返回） |
| createTime | string | 创建时间 |
| updateTime | string | 更新时间 |
| createUser / updateUser | number | 操作人 id，前端一般不用展示 |

## 3. 接口详情

### 3.1 新增分类

`POST /admin/category/add`（需 token，JSON）

请求体：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| name | string | 是 | 分类名称，唯一 |
| sort | number | 否 | 排序，不传默认 0 |
| status | number | 否 | 1:启用 0:禁用，不传默认 1 |

```json
{ "name": "vegetable", "sort": 2, "status": 1 }
```

成功响应（不返回新建 id，需要的话刷新列表）：

```json
{ "code": 1, "msg": null, "data": null }
```

名称重复时：

```json
{ "code": 0, "msg": "vegetable已存在", "data": null }
```

### 3.2 分页查询分类

`GET /admin/category/page`（需 token）

Query 参数（均可选）：

| 参数 | 类型 | 默认 | 说明 |
|---|---|---|---|
| pageNum | number | 1 | 页码 |
| pageSize | number | 10 | 每页条数 |
| name | string | - | 分类名称**模糊**匹配 |

```
GET /admin/category/page?pageNum=1&pageSize=10
```

成功响应（records 按 sort、id 升序）：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "total": 2,
    "records": [
      {
        "id": 1,
        "name": "水果",
        "status": 1,
        "sort": 1,
        "createTime": "2026-08-28 13:48",
        "createUser": 0,
        "updateTime": "2026-08-28 13:48",
        "updateUser": 0
      },
      {
        "id": 2,
        "name": "蔬菜",
        "status": 1,
        "sort": 2,
        "createTime": "2026-08-28 13:48",
        "createUser": 0,
        "updateTime": "2026-08-28 13:48",
        "updateUser": 0
      }
    ]
  }
}
```

### 3.3 根据 id 查询分类

`GET /admin/category/get`（需 token）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | number | 是 | 分类 id |

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "id": 3,
    "name": "meat",
    "status": 1,
    "sort": 3,
    "createTime": "2026-08-28 14:01",
    "createUser": 1,
    "updateTime": "2026-08-28 14:01",
    "updateUser": 1
  }
}
```

id 不存在时：`{ "code": 1, "msg": null, "data": null }`。

### 3.4 修改分类

`POST /admin/category/update`（需 token，JSON）

请求体（同新增，另加必填 `id`；未传字段不被覆盖）：

```json
{ "id": 3, "name": "meat", "sort": 3 }
```

成功响应：

```json
{ "code": 1, "msg": null, "data": null }
```

改名与其他分类重名时：`{ "code": 0, "msg": "xxx已存在", "data": null }`。`updateTime/updateUser` 后端自动维护。

### 3.5 根据 id 删除分类

`DELETE /admin/category/delete`（需 token）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | number | 是 | 分类 id |

成功响应：

```json
{ "code": 1, "msg": null, "data": null }
```

**删除保护**：分类下还有商品时不允许删除：

```json
{ "code": 0, "msg": "当前分类下存在商品，无法删除", "data": null }
```

（物理删除；删除不存在的 id 也返回成功。）

## 4. 前端对接注意事项

1. **分类下拉**：商品页/商品表单的分类选择器可用本接口拉数据（`pageSize` 传大一些如 100 即相当于全量；不分页的全量 list 接口如需要可反馈后端补充）。商品的查询接口（goods 文档 4.2/4.4）已联表返回 `categoryName`，列表展示分类名不需要再查一次。
2. **新增商品时的 categoryId** 请使用本组接口返回的真实 id，不再写死。
3. 分类 `status=0`（禁用）目前**不拦截**商品引用，仅作为展示标记。
4. 删除分类前建议先在前端处理 goods 的归属（后端已做兜底拦截，见 3.5）。
