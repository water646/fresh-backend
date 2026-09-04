# Fresh Market 后端接口使用说明（C端购物车）

> 本文档面向前端开发，描述 **C端购物车** 的 3 个接口，均已**实测通过**（2026-08-28 实测，示例为真实响应）。
> 通用约定与《user端接口使用说明.md》一致：统一响应 `{code, msg, data}`、时间格式 `yyyy-MM-dd HH:mm`、跨域走 dev server 代理。

## 1. 鉴权

购物车接口**全部需要登录**（`/user/**` 拦截）：

- 请求头：`authentication: <登录返回的 token>`（注意不是管理端的 `token` 头）。
- 未带/无效 token：**HTTP 401，响应体为空**。
- **购物车按登录用户隔离**：所有接口只操作当前登录用户自己的数据，无需（也不能）传 userId。

## 2. 接口详情

### 2.1 添加商品到购物车

`POST /user/shoppingCart/add`（需 token，JSON）

请求体：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| goodsId | number | 是 | 商品id（3.2/3.6 商品接口拿到的 id） |
| number | number | 否 | 添加数量，不传默认 1（传 0 或负数也按 1） |

```json
{ "goodsId": 1, "number": 3 }
```

成功响应：

```json
{ "code": 1, "msg": null, "data": null }
```

失败响应：

```json
{ "code": 0, "msg": "商品不存在或已下架", "data": null }
```

**加购规则**：
- **同一商品累加数量，不新增条目**：已存在时数量相加（如已有 1 个再加 3 个变 4 个）。
- 加入时保存商品**快照**（名称/图片/单价），之后商品改价或改名不影响购物车里已显示的旧数据，以最新查询为准展示时建议重新拉商品信息。
- 添加前请确保商品在售（下架商品会被拒绝）。

### 2.2 查询当前用户的购物车列表

`GET /user/shoppingCart/list`（需 token，无参数）

成功响应（按加入顺序，没有数据时 `data` 为空数组 `[]`）：

```json
{
  "code": 1,
  "msg": null,
  "data": [
    {
      "id": 1,
      "name": "apple",
      "userId": 5,
      "goodsId": 1,
      "number": 4,
      "amount": 9.90,
      "image": "",
      "createTime": "2026-08-28 16:33"
    },
    {
      "id": 2,
      "name": "banana",
      "userId": 5,
      "goodsId": 2,
      "number": 2,
      "amount": 5.50,
      "image": "",
      "createTime": "2026-08-28 16:33"
    }
  ]
}
```

### 2.3 删除购物车中的一条数据

`DELETE /user/shoppingCart/delete`（需 token）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | number | 是 | 购物车**条目** id（2.2 返回的 `id`，不是商品id） |

成功响应：

```json
{ "code": 1, "msg": null, "data": null }
```

失败响应（id 不存在**或不属于当前用户**时同样提示，不泄露他人数据是否存在）：

```json
{ "code": 0, "msg": "购物车数据不存在", "data": null }
```

## 3. 数据模型：ShoppingCart（购物车条目）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | number | 购物车条目 id（删除时用它） |
| name | string | 商品名称（加入时快照） |
| userId | number | 归属用户 id（即当前登录用户，展示时可忽略） |
| goodsId | number | 商品 id（跳商品详情用它） |
| number | number | 数量 |
| amount | number | **单价**（加入时快照，单位元；小计 = amount × number） |
| image | string \| null | 商品图片（加入时快照） |
| createTime | string | 加入时间 `yyyy-MM-dd HH:mm` |

## 4. 前端对接注意事项

1. **小计/合计请前端计算**：`amount` 是单价，后端不返回小计字段；合计 = Σ(amount × number)。
2. 目前**没有**"修改数量"和"清空购物车"接口：减数量可用 2.3 删除后重新加购实现；如需直接的改数量/清空接口，反馈后端补充。
3. 结算下单接口已上线，见《orders接口使用说明.md》；注意当前**下单后不会清空购物车**，重复下单会包含同样的商品。
