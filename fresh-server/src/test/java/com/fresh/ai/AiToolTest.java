package com.fresh.ai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AI 工具模块集成测试：验证三个工具的 SQL 在真实数据库上可执行、白名单分发与错误回传
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AiToolTest {

    @Autowired
    private ToolRegistry toolRegistry;

    /**
     * 用例1：商品销量查询两渠道 SQL 可执行，返回结构完整（只看结构不校验具体数值）
     */
    @Test
    public void 商品销量查询_两渠道合并返回() {
        String result = toolRegistry.execute("query_goods_sales",
                Map.of("goodsName", "甜橙", "beginDate", "2026-09-01", "endDate", "2026-09-07"));
        assertFalse(result.contains("\"error\""));
        assertTrue(result.contains("totalQuantity"));
        assertTrue(result.contains("totalAmount"));
        assertTrue(result.contains("items"));
    }

    /**
     * 用例2：订单统计两渠道聚合 SQL 可执行
     */
    @Test
    public void 订单统计_两渠道聚合返回() {
        String result = toolRegistry.execute("query_order_stats",
                Map.of("beginDate", "2026-09-01", "endDate", "2026-09-07"));
        assertFalse(result.contains("\"error\""));
        assertTrue(result.contains("normal"));
        assertTrue(result.contains("seckill"));
        assertTrue(result.contains("turnover"));
    }

    /**
     * 用例3：商品多条件过滤查询——价格区间+排序、分类+库存上限、无条件全量三条路径
     */
    @Test
    public void 商品查询_价格分类库存条件组合() {
        String byPrice = toolRegistry.execute("query_goods",
                Map.of("minPrice", 20, "sortBy", "price", "sortOrder", "desc"));
        assertFalse(byPrice.contains("\"error\""));
        assertTrue(byPrice.contains("matchedCount"));

        String byCategory = toolRegistry.execute("query_goods",
                Map.of("category", "肉", "maxStock", 30, "sortBy", "stock"));
        assertFalse(byCategory.contains("\"error\""));
        assertTrue(byCategory.contains("category"));

        String all = toolRegistry.execute("query_goods", Map.of());
        assertFalse(all.contains("\"error\""));
    }

    /**
     * 用例4：订单多条件明细查询——状态+日期范围+金额区间+排序、订单号关键词、无条件三条路径
     */
    @Test
    public void 订单查询_多条件组合明细() {
        String byStatus = toolRegistry.execute("query_order_status",
                Map.of("status", 6, "beginDate", "2026-09-01", "endDate", "2026-09-07",
                        "minAmount", 0, "sortBy", "amount", "sortOrder", "desc"));
        assertFalse(byStatus.contains("\"error\""));
        assertTrue(byStatus.contains("matchedCount"));
        assertTrue(byStatus.contains("cancelReason"));

        String byNumber = toolRegistry.execute("query_order_status", Map.of("number", "2026"));
        assertFalse(byNumber.contains("\"error\""));
        assertTrue(byNumber.contains("orderTime"));

        String all = toolRegistry.execute("query_order_status", Map.of());
        assertFalse(all.contains("\"error\""));
    }

    /**
     * 用例5：错误回传——白名单外的命令拒绝、非法日期参数给出明确提示
     */
    @Test
    public void 异常路径_未知工具与非法参数() {
        String unknown = toolRegistry.execute("rm_rf_server", Map.of());
        assertTrue(unknown.contains("error"));

        String badDate = toolRegistry.execute("query_order_stats",
                Map.of("beginDate", "2026/09/01"));
        assertTrue(badDate.contains("日期格式应为 yyyy-MM-dd"));

        //排序字段不在白名单：明确报错
        String badSort = toolRegistry.execute("query_goods",
                Map.of("sortBy", "weight"));
        assertTrue(badSort.contains("sortBy"));
    }
}
