# Fresh Market 后端接口使用说明（管理端商品评价）

> 本文档面向前端开发，描述管理端商品评价的 2 个接口（分页查询 + 回复）。**2026-09-09 实测通过**，示例为真实响应。
> 通用约定与《goods和login接口使用说明.md》一致：统一响应 `{code, msg, data}`、时间字段格式 `yyyy-MM-dd HH:mm`（无秒）。
> 用户端提交/查询接口见《goodsComments用户端接口使用说明.md》。评价隐藏（showOrHide）接口未实现，见第 3 章。

## 1. 鉴权

管理端评价接口**全部需要登录**（`/admin/**` 拦截）：

- 请求头：`token: <登录返回的 token>`（注意不是用户端的 `authentication` 头）。
- 未带/无效 token：**HTTP 401，响应体为空**。
- 管理端全局限流：同一员工每分钟最多 60 次接口调用，超限返回 `code: 0, msg: "操作过于频繁，请稍后再试"`。

## 2. 接口详情

### 2.1 分页查询商品评价

`GET /admin/goodsComments/page`（需 token，GET 查询参数绑定，2026-09-09 实测）

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| page | number | 是 | 页码 |
| pageSize | number | 是 | 每页条数 |
| goodsId | number | 否 | 商品 id（精确） |
| rating | number | 否 | 评分（精确，1-5），查差评传 1 |
| status | number | 否 | 状态（精确）1显示 0隐藏 |
| content | string | 否 | 评价内容关键词（模糊查询） |

成功响应（真实示例，无过滤条件时含隐藏评价）：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "total": 3,
    "records": [
      { "id": 4, "goodsId": 2, "goodsName": "香蕉", "userId": 2, "userName": "134205",
        "orderId": 999003, "orderDetailId": 999103, "rating": 3, "content": "橙子个头有点小",
        "anonymous": 0, "reply": null, "replyTime": null, "status": 1, "createTime": "2026-09-09 16:18" },
      { "id": 2, "goodsId": 2, "goodsName": "香蕉", "userId": 2, "userName": "134205",
        "orderId": 999003, "orderDetailId": 999104, "rating": 1, "content": "预置的隐藏评价",
        "anonymous": 0, "reply": null, "replyTime": null, "status": 0, "createTime": "2026-09-09 16:18" }
    ]
  }
}
```

**查询规则**：

1. **含隐藏评价**（与用户端列表不同）：全部状态都返回，按 `status` 字段区分，前端可提供筛选。
2. 带出 `goodsName`（商品被删时为 null）、`userName`（**真实昵称，匿名评价也不脱敏**——管理端可见，用于风控定位用户）。
3. 过滤条件可任意组合：goodsId / rating / status 精确匹配，content 模糊匹配；全部可选。
4. 按评价 id 倒序（最新在前）。
5. 每条带 `orderId` / `orderDetailId`，需要核对原订单时可跳转订单查询。

### 2.2 回复商品评价

`PUT /admin/goodsComments/reply`（需 token，JSON，2026-09-09 实测）

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | number | 是 | 评价 id（从 2.1 列表获取） |
| reply | string | 是 | 回复内容，最长 500 字，不能为空 |

```json
{ "id": 4, "reply": "感谢反馈，我们会向供应商反映" }
```

成功响应：`{ "code": 1, "msg": null, "data": null }`

失败响应（均为 `code: 0`）：

| msg | 场景 |
|---|---|
| `"评价不存在"` | 评价 id 无效（可能已被删除） |
| `"回复内容不能为空"` / `"回复内容不能超过500字"` | 参数校验失败 |

**回复规则**：

1. **可重复回复**：再次调用覆盖旧回复并刷新回复时间（用于修改回复措辞）。
2. 回复成功记录 `reply_time`；`update_user` 自动记录当前操作员工 id（实测 admin 回复后 updateUser=1）。
3. 回复实时对用户端可见：用户端评价列表（用户端文档 2.2）的 `reply` / `replyTime` 字段会带出，前端评价卡片直接渲染即可。
4. 对隐藏评价也能回复（不报错），但用户端看不到隐藏评价，建议先恢复显示再回复。

## 3. 联调注意事项

- **隐藏/显示评价接口未实现**：表中 status 字段与用户端过滤已就绪（status=0 用户端不可见），管理端只差一个切换接口，需要时提需求即可。
- 用户提交的评价初始即 status=1 显示，**没有先审后显流程**。
- 删除评价接口未实现，当前违规内容只能等隐藏接口上线后屏蔽。
- 评价不联动商品评分统计（goods 表无评分列），管理端报表如需平均分需等统计功能。
- 管理端列表**能看见匿名评价的真实用户**（脱敏只在用户端），注意内部数据权限。
