# Fresh Market 后端接口使用说明（C端订单）

> 本文档面向前端开发，描述 **订单相关** 的 10 个接口 + **商家端 WebSocket 来单提醒**：2.1–2.3 与第 5 章来单提醒已**实测通过**（2026-08-31 / 09-01 实测，示例为真实响应）；2.4–2.10 为后续补充开发，**尚未实测**，联调时以实际响应为准。
> 通用约定与《user端接口使用说明.md》一致：统一响应 `{code, msg, data}`、时间格式 `yyyy-MM-dd HH:mm`、跨域走 dev server 代理。

## 1. 鉴权

订单接口**全部需要登录**（`/user/**` 拦截）：

- 请求头：`authentication: <登录返回的 token>`（注意不是管理端的 `token` 头）。
- 未带/无效 token：**HTTP 401，响应体为空**。
- **订单按登录用户隔离**：下单自动绑定当前用户；查询只能查到自己的订单，查他人订单一律返回"订单不存在"。

## 2. 接口详情

### 2.1 提交订单

`POST /user/orders/submit`（需 token，JSON）

请求体：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| addressBookId | number | 是 | 收货地址 id（地址簿接口拿到的 id，须是当前用户自己的地址） |
| payMethod | number | 是 | 支付方式 1微信 2支付宝 |
| amount | number | 是 | 实收金额（单位元） |
| deliveryStatus | number | 否 | 配送状态 1立即送出 0选择具体时间 |
| remark | string | 否 | 订单备注 |

```json
{ "addressBookId": 1, "payMethod": 1, "remark": "放门口即可", "deliveryStatus": 1, "amount": 18.70 }
```

成功响应（data 为订单对象，含自增 `id` 与后端生成的 `number`，支付/查详情都要用）：

```json
{
  "code": 1,
  "msg": null,
  "data": { "id": 1, "number": "2026083100000001", "status": 1, "payStatus": 0, ... }
}
```

失败响应：

```json
{ "code": 0, "msg": "收货地址不存在", "data": null }
```

**下单规则**：
- 订单内容 = **当前用户购物车的全部条目**，无需（也不能）逐条传商品；每个购物车条目生成一条订单明细（商品名称/图片/单价为下单时快照）。
- 订单号由后端生成（`日期 + 8位自增序号`，如 `2026083100000001`），前端无需传。
- 下单后订单初始状态为 **1待付款**，需再调 2.2 完成支付。
- ⚠️ **当前下单后不会清空购物车**：再次下单会把同样商品再下一遍，前端注意做提示或防重复点击。

### 2.2 订单支付

`PUT /user/orders/payment`（需 token，JSON）

请求体：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| orderNumber | string | 是 | 订单号 |
| payMethod | number | 是 | 支付方式 1微信 2支付宝 |

```json
{ "orderNumber": "2026083100000001", "payMethod": 1 }
```

成功响应：

```json
{ "code": 1, "msg": null, "data": null }
```

失败响应（对已支付/已取消订单再次发起支付）：

```json
{ "code": 0, "msg": "订单已取消或已支付", "data": null }
```

**支付规则**：
- 支付成功后：订单状态 `1待付款 → 2待接单`、支付状态 `0未支付 → 1已支付`、并记录结账时间 `checkoutTime`（用 2.3 可查到变化）。
- 支付成功会**通过 WebSocket 向商家端推送来单提醒**（2026-09-01 实测），消息格式与前端对接方式见第 5 章。
- 后端有分布式锁防并发重复支付，同一订单同时发起多次支付时，未抢到锁的请求返回 `{"code": 0, "msg": "请勿重复支付"}`。
- 目前为模拟支付（未接微信/支付宝），调接口即支付成功。

### 2.3 查询订单详情（含订单明细）

`GET /user/orders/detail`（需 token）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | number | 是 | 订单 id |

成功响应（真实示例，已支付订单）：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "id": 1,
    "number": "2026083100000001",
    "status": 2,
    "orderTime": "2026-08-31 12:08",
    "checkoutTime": "2026-08-31 12:09",
    "payMethod": 1,
    "payStatus": 1,
    "amount": 18.70,
    "remark": "放门口即可",
    "consignee": "啊大大",
    "phone": "13232323232",
    "address": "分分分",
    "cancelReason": null,
    "rejectionReason": null,
    "cancelTime": null,
    "estimatedDeliveryTime": "2026-08-31 12:38",
    "deliveryStatus": 1,
    "deliveryTime": null,
    "orderDetailList": [
      { "id": 10, "name": "apple", "orderId": 1, "goodsId": 1, "number": 1, "amount": 9.90, "image": "" },
      { "id": 11, "name": "banana", "orderId": 1, "goodsId": 2, "number": 1, "amount": 5.50, "image": "" },
      { "id": 12, "name": "apple", "orderId": 1, "goodsId": 1, "number": 4, "amount": 3.30, "image": "" },
      { "id": 13, "name": "banana", "orderId": 1, "goodsId": 2, "number": 2, "amount": 2.75, "image": "" }
    ]
  }
}
```

待付款订单的区别：`status` 为 1、`payStatus` 为 0、`checkoutTime` 为 null。

失败响应（订单不存在**或不属于当前用户**时同样提示，不泄露他人订单）：

```json
{ "code": 0, "msg": "订单不存在", "data": null }
```

### 2.4 查询历史订单列表

`GET /user/orders/list`（需 token）
返回一个包含和2.3相同单元的列表


### 2.5 管理端分页查询订单

`GET /admin/orders/page`（需 token）

查询可选参数:
private int page;
private int pageSize;
private String number;
private String phone;
private Integer status;
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
private LocalDateTime beginTime;
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
private LocalDateTime endTime;
private Long userId;

返回结果:
PageResult对象:
private long total; //总记录数
private List records; //当前页数据集合

### 2.6 管理端接单

`GET /admin/orders/comfirm?id={id}`（需 token）
传入订单id即可把订单的status从2变为3

### 2.7 管理端派送

`GET /admin/orders/delivery?id={id}`（需 token）
传入订单id即可把订单的status从3变为4

### 2.8 管理端完成

`GET /admin/orders/finish?id={id}`（需 token）
传入订单id即可把订单的status从4变为5

### 2.9 管理端取消订单

`GET /admin/orders/cancel?id={id}`（需 token）
传入订单id即可把订单的status从1变为6

### 2.10 用户取消订单

`GET /user/orders/cancel?id={id}`（需 token）
传入订单id即可把订单的status从1变为6







## 3. 数据模型

### 3.1 OrderVO（订单详情）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | number | 订单 id |
| number | string | 订单号 |
| status | number | 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 |
| orderTime | string | 下单时间 |
| checkoutTime | string \| null | 结账时间（未支付为 null） |
| payMethod | number | 支付方式 1微信 2支付宝 |
| payStatus | number | 支付状态 0未支付 1已支付 2退款 |
| amount | number | 实收金额（单位元，下单时提交的金额） |
| remark | string \| null | 备注 |
| consignee | string | 收货人（下单时地址快照） |
| phone | string | 收货手机号（下单时地址快照） |
| address | string | 收货地址（下单时地址快照） |
| cancelReason | string \| null | 取消原因 |
| rejectionReason | string \| null | 拒单原因 |
| cancelTime | string \| null | 取消时间 |
| estimatedDeliveryTime | string | 预计送达时间 |
| deliveryStatus | number | 配送状态 1立即送出 0选择具体时间 |
| deliveryTime | string \| null | 送达时间 |
| orderDetailList | array | 订单明细列表，见 3.2 |

### 3.2 OrderDetail（订单明细）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | number | 明细条目 id |
| name | string | 商品名称（下单时快照，不随商品改名变化） |
| orderId | number | 所属订单 id |
| goodsId | number | 商品 id（跳商品详情用它） |
| number | number | 购买数量 |
| amount | number | **单价**（下单时快照，单位元；小计 = amount × number） |
| image | string \| null | 商品图片（下单时快照） |

## 4. 前端对接注意事项

1. **合计金额请前端计算展示**：`amount` 是单价，订单实收 `amount` 为下单时前端传入的值；明细合计 = Σ(amount × number)，建议下单前算准再传。
2. **同一商品可能出现多条明细**（购物车不同时期加入的同一商品是独立条目，快照单价可能不同），展示时可按条渲染或前端自行合并。
3. 订单状态流转：`1待付款 --支付--> 2待接单`（实测）；接单/派送/完成/取消的接口见 2.6–2.10（未实测）。
4. 历史订单列表见 2.4，管理端订单分页见 2.5；下单接口此前不返回订单号（2.4 上线前记录，现可用列表接口获取）。
5. 收货人/手机号/地址是**下单瞬间的快照**，之后修改地址簿不影响已下订单。

## 5. 商家端 WebSocket 来单提醒（管理端前端对接）

用户支付成功后，后端会通过 WebSocket 向**所有已连接的商家端**实时推送来单提醒（2026-09-01 实测通过，与苍穹外卖同款方案）。

### 5.1 建立连接

```
ws://localhost:8080/ws/{sid}
```

| 项 | 说明 |
|---|---|
| sid | 任意唯一字符串，用于标识一个客户端连接（如随机数/时间戳），仅作会话标识用 |
| 连接时机 | **商家登录成功后立即建立**，整个后台会话期间保持长连接（不是进订单页才连） |
| 鉴权 | **连接不需要 token**（`/ws/**` 不在 JWT 拦截范围内），但业务上应放在登录成功之后再连 |
| 多端 | 每个打开的商家端页面各建一条连接，推送时**全部收到**（群发） |

前端参考代码：

```js
// 登录接口返回成功后再执行
const sid = Math.random().toString(36).substr(2);
const ws = new WebSocket(`ws://localhost:8080/ws/${sid}`);

ws.onmessage = (event) => {
  const msg = JSON.parse(event.data);
  if (msg.type === 1) {
    // 来单提醒：弹窗提示 + 语音播报 + 刷新待接单列表
    notify(`新订单：${msg.content}`);
    playVoice();
    refreshOrderList(); // 可拉 /admin/orders/page?status=2
  }
};

// 页面关闭前主动断开，防止服务端向失效连接发送
window.onbeforeunload = () => ws.close();
```

### 5.2 推送消息格式

服务端推送的是 **JSON 字符串**（text 帧），字段约定与苍穹外卖一致：

| 字段 | 类型 | 说明 |
|---|---|---|
| type | number | 消息类型：**1 来单提醒**；2 客户催单（接口暂未开发） |
| orderId | number | 订单 id（查订单详情用它） |
| content | string | 提示文案，如 `订单号：2026083100000001` |

实测示例（支付成功后商家端收到）：

```json
{"orderId":17,"type":1,"content":"订单号：WSTEST001"}
```

### 5.3 触发时机与注意事项

1. **触发点**：`PUT /user/orders/payment` 支付成功（订单 `1待付款 → 2待接单`）后**立即推送**，商家端无需轮询。
2. **断线重连**：WebSocket 是长连接，网络波动会断开，前端建议做 `onclose` 时延时重连（如 3~5 秒退避），否则来单提醒会静默丢失。
3. **仅推送在线提醒，不落库**：推送是一次性的，断线期间的订单不会补推；商家端刷新订单列表（2.5 分页接口）可兜底看到全部待接单订单。
4. 催单（type=2）后端字段已预留，催单接口开发后前端只需在 `onmessage` 里补 `type === 2` 分支，无需改连接逻辑。
