# Fresh Market 后端接口使用说明（管理端秒杀订单）

> 本文档面向前端开发，描述管理端秒杀订单的 4 个接口（分页查询 / 按id查询 / 修改 / 删除）。全部接口已于 **2026-09-03 实测通过**，示例为真实响应。
> 通用约定与《goods和login接口使用说明.md》一致：统一响应 `{code, msg, data}`、响应时间字段格式 `yyyy-MM-dd HH:mm`（无秒）、跨域走 dev server 代理。

## 1. 鉴权

秒杀订单接口**全部需要管理员登录**（`/admin/**` 拦截）：

- 请求头：`token: <管理端登录返回的 token>`
- 未带/无效 token：**HTTP 401，响应体为空**。

## 2. 数据结构：SeckillOrdersVO

分页与详情接口返回的数据均在订单字段基础上多带一个 `seckillGoodsName`：

| 字段 | 类型 | 说明 |
|---|---|---|
| id | number | 秒杀订单 id |
| number | string | 订单号（`日期 + 8位序号`，如 `2026090300000101`） |
| seckillGoodsId | number | 秒杀商品 id |
| status | number | 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 |
| userId | number | 下单用户 id |
| addressBookId | number | 收货地址 id（当前秒杀下单不填，恒为 null） |
| orderTime | string | 下单时间 `yyyy-MM-dd HH:mm` |
| checkoutTime | string | 结账时间 |
| payMethod | number | 支付方式 1微信 2支付宝 |
| payStatus | number | 支付状态 0未支付 1已支付 2退款 |
| amount | number | 实收金额（元） |
| remark | string | 备注 |
| userName | string | 用户名 |
| phone | string | 手机号 |
| address | string | 地址 |
| consignee | string | 收货人 |
| cancelReason | string | 订单取消原因 |
| rejectionReason | string | 订单拒绝原因 |
| cancelTime | string | 取消时间 |
| estimatedDeliveryTime | string | 预计送达时间 |
| deliveryStatus | number | 配送状态 1立即送出 0选择具体时间 |
| deliveryTime | string | 送达时间 |
| **seckillGoodsName** | string | **秒杀商品名称（联表带出）。商品已被删除时为 `null`，前端按"商品已删除"兜底展示** |

## 3. 接口详情

### 3.1 分页查询秒杀订单

`GET /admin/seckillOrders/page`（需 token）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| page | number | 是 | 页码 |
| pageSize | number | 是 | 每页条数 |
| number | string | 否 | 订单号（模糊查询） |
| status | number | 否 | 订单状态（精确） |
| phone | string | 否 | 手机号（模糊查询） |
| beginTime | string | 否 | 下单时间范围-开始，格式 `yyyy-MM-dd HH:mm:ss`（含） |
| endTime | string | 否 | 下单时间范围-结束，格式 `yyyy-MM-dd HH:mm:ss`（含） |

> ⚠️ 注意：本接口的**请求**时间参数带秒（GET 参数绑定），与**响应**时间字段的无秒格式（`yyyy-MM-dd HH:mm`）不同。

```json
{ "page": 1, "pageSize": 2, "status": 2, "phone": "13800000001",
  "beginTime": "2026-09-03 00:00:00", "endTime": "2026-09-03 23:59:59" }
```

成功响应（真实示例，pageSize=2；完整字段见第 2 章表格，此处省略部分 null 字段）：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "total": 2,
    "records": [
      { "id": 4, "number": "2026090400000104", "seckillGoodsId": 10, "status": 2,
        "userId": 1001, "orderTime": "2026-09-03 10:00", "payMethod": 2, "payStatus": 1,
        "amount": 11.98, "userName": "测试用户A", "phone": "13800000001",
        "seckillGoodsName": "限时秒杀红富士苹果" }
    ]
  }
}
```

**规则**：
- 各过滤条件可任意组合，全部可选。
- 结果按订单 id 倒序（最新在前）。
- 实测各条件命中数：`number=0101`（模糊）→ 1 条；`status=2` → 2 条；`phone`（模糊）→ 2 条；时间范围 2026-09-03 全天 → 3 条。

### 3.2 根据id查询秒杀订单

`GET /admin/seckillOrders/get`（需 token）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | number | 是 | 秒杀订单 id |

成功响应（真实示例，含全部字段）：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "id": 1, "number": "2026090300000101", "seckillGoodsId": 10, "status": 2,
    "userId": 1001, "addressBookId": null,
    "orderTime": "2026-09-03 09:15", "checkoutTime": null,
    "payMethod": 1, "payStatus": 1, "amount": 5.99,
    "remark": "测试订单1", "userName": "测试用户A", "phone": "13800000001",
    "address": "北京市朝阳区1号", "consignee": "张三",
    "cancelReason": null, "rejectionReason": null, "cancelTime": null,
    "estimatedDeliveryTime": "2026-09-03 12:00", "deliveryStatus": 1, "deliveryTime": null,
    "seckillGoodsName": "限时秒杀红富士苹果"
  }
}
```

id 不存在时：`{ "code": 1, "msg": null, "data": null }`。

### 3.3 修改秒杀订单

`PUT /admin/seckillOrders/update`（需 token，JSON）

请求体：`id`（**必填**，number）+ 以下任意字段（**部分更新**：只改传了的字段，没传的保持原值）：

| 字段 | 类型 | 说明 |
|---|---|---|
| status | number | 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 |
| payStatus | number | 支付状态 0未支付 1已支付 2退款 |
| payMethod | number | 支付方式 1微信 2支付宝 |
| amount | number | 实收金额 |
| remark | string | 备注 |
| userName | string | 用户名 |
| phone | string | 手机号 |
| address | string | 地址 |
| consignee | string | 收货人 |
| cancelReason | string | 订单取消原因 |
| rejectionReason | string | 订单拒绝原因 |
| estimatedDeliveryTime | string | 预计送达时间，格式 `yyyy-MM-dd HH:mm` |
| deliveryStatus | number | 配送状态 1立即送出 0选择具体时间 |

```json
{ "id": 1, "status": 5, "remark": "管理员修改备注", "estimatedDeliveryTime": "2026-09-03 15:30" }
```

成功响应：`{ "code": 1, "msg": null, "data": null }`

实测部分更新行为：只传 `{ "id": 4, "amount": 99.99 }` 时，仅 amount 变化，status/remark 等其余字段保持原值。

### 3.4 删除秒杀订单

`DELETE /admin/seckillOrders/delete`（需 token）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | number | 是 | 秒杀订单 id |

成功响应：`{ "code": 1, "msg": null, "data": null }`

**规则**：物理删除；id 不存在也返回成功（幂等）。删除后分页 total 相应减少。

## 4. 联调注意事项

1. **秒杀订单与普通订单是两张表**：本组接口只查秒杀订单（`seckill_orders`），与《orders接口使用说明.md》的普通订单互不相通，订单号各自独立生成。
2. **订单是异步落库的**：用户秒杀成功后订单经 RabbitMQ 异步写入，**同时原子扣减该商品数据库库存**（`stock-1`，带 `stock > 0` 条件防负数；扣库存与存订单在同一事务，数据库库存不足时订单不落库）。正常毫秒级但非实时，刚秒杀完立即查分页可能短暂查不到，前端注意刷新时机。
3. **商品删除不影响订单**：秒杀商品被管理端删除后，其历史订单仍正常返回，`seckillGoodsName` 为 `null`，前端按"商品已删除"兜底展示。
4. 秒杀订单由用户端下单产生（初始状态 1 待付款），订单号、用户信息在下单时生成；管理端修改接口是主要的补录/纠错入口（如补充收货人、地址、取消原因等）。
