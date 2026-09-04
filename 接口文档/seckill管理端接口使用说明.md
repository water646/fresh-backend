# Fresh Market 后端接口使用说明（管理端秒杀商品）

> 本文档面向前端开发，描述管理端**秒杀商品**的 5 个接口（2.1–2.5）。全部接口已于 **2026-09-03 实测通过**，示例为真实响应。秒杀**订单**管理接口见《seckillOrders接口使用说明.md》。
> 通用约定与《goods和login接口使用说明.md》一致：统一响应 `{code, msg, data}`、时间字段格式 `yyyy-MM-dd HH:mm`（无秒）、跨域走 dev server 代理。

## 1. 鉴权

秒杀管理端接口**全部需要管理员登录**（`/admin/**` 拦截）：

- 请求头：`token: <管理端登录返回的 token>`
- 未带/无效 token：**HTTP 401，响应体为空**。

## 2. 秒杀商品管理（/admin/seckill）

秒杀商品状态：`0 禁用`、`1 启用`（启停开关）。**是否处于秒杀时段由 `startTime`/`endTime` 决定，与 status 无关**——未开始/进行中/已结束不是 status 的取值，前端根据时间自行展示倒计时/状态。

### 2.1 新增秒杀商品

`POST /admin/seckill/save`（需 token，JSON）

请求体：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| name | string | 是 | 秒杀商品名称 |
| seckillPrice | number | 是 | 秒杀价格（元） |
| stock | number | 是 | 秒杀库存 |
| limitNum | number | 否 | 每人限购数量（表默认 1） |
| startTime | string | 是 | 开始时间，格式 `yyyy-MM-dd HH:mm` |
| endTime | string | 是 | 结束时间，格式 `yyyy-MM-dd HH:mm` |

```json
{ "name": "秒杀测试车厘子", "seckillPrice": 19.90, "stock": 50, "limitNum": 1,
  "startTime": "2026-09-03 14:00", "endTime": "2026-09-03 16:00" }
```

成功响应：`{ "code": 1, "msg": null, "data": null }`

**规则**：
- 后端强制初始状态为 **0 禁用**（即使传了 status 也会被忽略），需通过 2.4 修改 `status=1` 启用后用户端才能秒杀。
- ⚠️ **新增只写数据库，不会预热秒杀库存缓存**。用户端秒杀依赖 Redis 库存（见 2.4），**新增后必须再调用一次 2.4 修改接口（任意字段即可）触发预热，否则秒杀一律返回"库存不足"**。

### 2.2 分页查询秒杀商品

`GET /admin/seckill/page`（需 token）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| page | number | 是 | 页码 |
| pageSize | number | 是 | 每页条数 |
| name | string | 否 | 商品名称（模糊查询） |
| status | number | 否 | 状态 0禁用 1启用 |

成功响应（真实示例）：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "total": 1,
    "records": [
      { "id": 10, "name": "限时秒杀红富士苹果", "seckillPrice": 5.99, "stock": 100,
        "limitNum": 2, "startTime": "2026-09-03 00:00", "endTime": "2026-09-03 23:59",
        "status": 1, "createTime": "2026-09-03 11:56", "createUser": 1,
        "updateTime": "2026-09-03 11:56", "updateUser": 1 }
    ]
  }
}
```

结果按创建时间倒序。

### 2.3 根据id查询秒杀商品

`GET /admin/seckill/get`（需 token）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | number | 是 | 秒杀商品 id |

成功响应：data 为商品对象（字段同 2.2 records 内的结构）；id 不存在时 `data: null`。

### 2.4 修改秒杀商品（含缓存预热）

`PUT /admin/seckill/update`（需 token，JSON）

请求体：`id`（**必填**，number）+ 以下任意字段（部分更新，没传的不改）：name / seckillPrice / stock / limitNum / startTime / endTime / status。

```json
{ "id": 10, "stock": 60, "status": 1 }
```

成功响应：`{ "code": 1, "msg": null, "data": null }`

**规则**：
- 部分更新：只修改传了的字段。启用/停用秒杀商品也是走本接口改 `status`（1 启用 / 0 禁用，禁用后用户端秒杀会被拦截，返回"秒杀商品已禁用"）。
- **副作用（重要）**：每次修改都会把该商品在数据库中的最新 stock 写入 Redis 秒杀库存缓存（key `fresh:seckill:stock:<id>`）。这是秒杀扣减的直接数据源——**新增商品后（2.1）、每次调整库存后，都必须调用本接口一次**，秒杀才能按新库存进行。
- ⚠️ **秒杀进行中慎用本接口**：秒杀时 Redis 库存实时扣减、数据库库存随订单落库异步扣减（略滞后于 Redis），此时调本接口会把（滞后的）数据库值写回 Redis，出现库存"回补"，存在短暂超卖窗口。调整库存请在秒杀时段外操作。
- 时间格式 `yyyy-MM-dd HH:mm`。

### 2.5 删除秒杀商品

`DELETE /admin/seckill/delete`（需 token）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | number | 是 | 秒杀商品 id |

成功响应：`{ "code": 1, "msg": null, "data": null }`（id 不存在也返回成功，幂等）。

⚠️ 删除不清理 Redis 中的库存/限购标记（`fresh:seckill:stock:<id>`、`fresh:seckill:order:<id>`）。若之后新增商品复用了同一 id，需注意先调 2.4 覆盖库存缓存。

## 3. 秒杀订单管理（/admin/seckillOrders）

分页查询 / 按id查询 / 修改 / 删除共 4 个接口，**见《seckillOrders接口使用说明.md》**（2026-09-03 同批实测通过）。

## 4. 联调注意事项

1. **缓存预热链路**：秒杀能否下单取决于 Redis 库存 `fresh:seckill:stock:<id>`，只有 2.4 修改接口会写它。新增商品后忘调 2.4 → 用户端秒杀一直返回"库存不足"。
2. **库存有两份，扣减有时差**：秒杀下单先实时扣 Redis 库存；订单经 MQ 落库时在同一事务里扣数据库库存（略滞后）。管理端 2.2 分页展示的是数据库值，秒杀刚开始时短暂偏大属正常，以 Redis 为准。
3. 商品删除后其历史订单仍在（管理端订单分页可见，`seckillGoodsName` 显示为 null），删除商品不会连带删订单。
4. 用户端秒杀相关接口见《seckill用户端接口使用说明.md》。
