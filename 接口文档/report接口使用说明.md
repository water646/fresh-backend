# Fresh Market 后端接口使用说明（管理端数据统计）

> 本文档面向前端开发，描述 **数据统计（报表）** 的 5 个接口（营业额统计 / 用户统计 / 订单统计 / 销量排名Top10 / 导出Excel报表），均已**实测通过**（2026-09-01 实测，示例为真实响应，数值取决于库内数据）。
> 通用约定与《goods和login接口使用说明.md》一致：统一响应 `{code, msg, data}`。
> 5 个接口均为管理端接口（数据看板页面用），与苍穹外卖的 ReportController 功能对齐。

## 1. 鉴权

- 全部接口**需要管理端 token**（`/admin/**` 拦截）。
- 请求头：`token: <登录返回的 token>`。
- 未带/无效 token：**HTTP 401，响应体为空**。

## 2. 统计口径（先读这个）

所有统计接口共用以下规则：

| 规则 | 说明 |
|---|---|
| 日期参数 | `begin`/`end` 格式均为 `yyyy-MM-dd`，**包含两端**（闭区间） |
| 营业额 | 只统计 **"已完成"订单（status=5）** 的实收金额合计 |
| 有效订单 | 同上，指 status=5 的订单；订单总数则统计全部状态 |
| 归属日期 | 按订单的**下单时间**归属到天 |
| 补零 | 区间内没有数据的日期，列表中补 `0`（营业额为 `0.0`，数量为 `0`），**列表长度与 dateList 一一对应** |
| 用户总量 | 截止当天的累计注册用户数（含区间开始前的存量用户） |
| 列表格式 | `dateList` 等列表字段是**逗号拼接的字符串**（如 `"80.5,0.0,18.0"`），前端需自行 `split(',')` 后喂给 ECharts |

## 3. 接口详情

### 3.1 营业额统计

`GET /admin/report/turnoverStatistics`（需 token）

Query 参数：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| begin | string | 是 | 开始日期，`yyyy-MM-dd`（含） |
| end | string | 是 | 结束日期，`yyyy-MM-dd`（含） |

```
GET /admin/report/turnoverStatistics?begin=2026-08-28&end=2026-08-30
```

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "dateList": "2026-08-28,2026-08-29,2026-08-30",
    "turnoverList": "80.5,0.0,18.0"
  }
}
```

| data 字段 | 类型 | 说明 |
|---|---|---|
| dateList | string | 日期列表，逗号分隔，与 turnoverList 一一对应 |
| turnoverList | string | 每日营业额（当天已完成订单金额合计），无订单为 `0.0` |

### 3.2 用户统计

`GET /admin/report/userStatistics`（需 token）

Query 参数：与 3.1 相同（begin / end）。

```
GET /admin/report/userStatistics?begin=2026-08-28&end=2026-08-30
```

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "dateList": "2026-08-28,2026-08-29,2026-08-30",
    "totalUserList": "6,6,7",
    "newUserList": "3,0,1"
  }
}
```

| data 字段 | 类型 | 说明 |
|---|---|---|
| dateList | string | 日期列表，逗号分隔 |
| totalUserList | string | 每日用户总量（截止当天的累计注册数） |
| newUserList | string | 每日新增用户数（当天注册数） |

### 3.3 订单统计

`GET /admin/report/ordersStatistics`（需 token）

Query 参数：与 3.1 相同（begin / end）。

```
GET /admin/report/ordersStatistics?begin=2026-08-28&end=2026-08-30
```

成功响应：

```json
{
  "code": 1,
  "msg": null,
  "data": {
    "dateList": "2026-08-28,2026-08-29,2026-08-30",
    "orderCountList": "3,0,2",
    "validOrderCountList": "2,0,1",
    "totalOrderCount": 5,
    "validOrderCount": 3,
    "orderCompletionRate": 0.6
  }
}
```

| data 字段 | 类型 | 说明 |
|---|---|---|
| dateList | string | 日期列表，逗号分隔 |
| orderCountList | string | 每日订单数（全部状态） |
| validOrderCountList | string | 每日有效订单数（已完成 status=5） |
| totalOrderCount | number | 区间内订单总数 |
| validOrderCount | number | 区间内有效订单数 |
| orderCompletionRate | number | 订单完成率 = 有效订单数 / 订单总数；**总订单为 0 时返回 0.0**（不会除零报错） |

### 3.4 销量排名 Top10

`GET /admin/report/top10`（需 token）

Query 参数：与 3.1 相同（begin / end）。

```
GET /admin/report/top10?begin=2026-08-28&end=2026-08-30
```

成功响应（按销量**倒序**，最多 10 条；区间内无已完成订单时为空数组 `[]`）：

```json
{
  "code": 1,
  "msg": null,
  "data": [
    { "name": "apple", "number": 3 },
    { "name": "banana", "number": 3 },
    { "name": "milk", "number": 1 },
    { "name": "egg", "number": 1 },
    { "name": "fish", "number": 1 }
  ]
}
```

| data 元素字段 | 类型 | 说明 |
|---|---|---|
| name | string | 商品名称 |
| number | number | 销量（区间内已完成订单中该商品的购买数量合计） |

销量相同的商品先后顺序不保证（数据库排序不指定次关键字）。

### 3.5 导出运营数据 Excel 报表

`GET /admin/report/export`（需 token）

**无参数**。统计区间由后端固定为**近 30 天（不含今天，今天数据不完整）**，例如今天 9 月 1 日导出则统计 8 月 2 日 ~ 8 月 31 日。

成功响应是一个 `.xls` 文件流（HTTP 200）：

```
Content-Type: application/vnd.ms-excel
Content-Disposition: attachment;filename=%E8%BF%90%E8%90%A5%E6%95%B0%E6%8D%AE%E6%8A%A5%E8%A1%A8.xls
```

（filename 是 URL 编码后的 `运营数据报表.xls`，浏览器/前端解码后即中文名）

文件内容为单个 sheet"运营数据"，从上到下依次是三块：

1. **概览数据**（区间合计）：营业额、用户总数、新增用户、订单总数、有效订单数、订单完成率、平均客单价（平均客单价 = 营业额 / 有效订单数）
2. **商品销量排名 Top10**：商品名称、销量
3. **订单明细**：订单号、订单状态（中文：待付款/待接单/已接单/派送中/已完成/已取消）、下单时间、收货人、手机号、地址、金额（按下单时间倒序）

## 4. 错误响应（5 个接口通用）

| 场景 | 响应 |
|---|---|
| begin/end **缺失或为空** | `{ "code": 0, "msg": "日期范围不合法", "data": null }` |
| begin **晚于** end | 同上（不会死循环也不会返回倒序列表） |
| 日期格式不是 yyyy-MM-dd | HTTP 400（参数解析失败） |
| 未带/无效 token | HTTP 401，响应体为空 |

## 5. 前端对接注意事项

1. **列表字段是逗号拼接字符串**：`dateList` / `turnoverList` / `orderCountList` 等都是 `"a,b,c"` 形式的字符串，渲染 ECharts 前先 `split(',')`，三项列表长度保证一致、下标对齐。
2. **导出接口不能用 `<a href>` / `window.open` 直接下载**：因为需要 `token` 请求头，直接跳转带不上。请用 axios/fetch 以 `responseType: 'blob'` 请求后，用 `URL.createObjectURL(blob)` + 临时 `<a>` 触发下载（文件名可从 `Content-Disposition` 取或前端写死 `运营数据报表.xls`）。
3. **导出区间固定近 30 天**，前端"导出"按钮无需传日期；如以后要支持自定义区间导出，需后端加参数（当前未开发）。
4. **数据看板典型用法**：日期选择器（今天/近7天/近30天）→ 换算成 begin/end 调 3.1~3.4；注意"近7天"如果选 `今天-6 ~ 今天`，今天的数据也是实时的。
5. **top10 并列销量**顺序不稳定，前端不要对同名商品顺序做断言。
6. 用户统计的 totalUserList 是**累计值曲线**（递增），newUserList 是**每日增量柱状图**，两者配合展示。
