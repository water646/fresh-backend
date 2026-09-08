package com.fresh.ai.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fresh.ai.AiTool;
import com.fresh.entity.Category;
import com.fresh.entity.Goods;
import com.fresh.mapper.CategoryMapper;
import com.fresh.mapper.GoodsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 工具：多条件组合查询商品列表（goods 表，不含秒杀商品）
 * 过滤维度：名称/分类关键词、价格区间、在售状态、库存上限；支持按单价或库存排序
 */
@Component
@Slf4j
public class QueryGoodsTool implements AiTool {

    /** sortBy / sortOrder 允许的取值（白名单） */
    private static final Set<String> SORT_BY = Set.of("price", "stock");
    private static final Set<String> SORT_ORDER = Set.of("asc", "desc");

    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public String name() {
        return "query_goods";
    }

    @Override
    public String description() {
        return "查询商品列表（普通商品，不含秒杀商品），支持多条件组合过滤：名称关键词、分类名、价格区间、在售状态、库存上限，"
                + "并可按单价或库存排序。例如：\"单价大于20元的商品\"传 minPrice=20；\"20到50元的肉类\"传 minPrice=20、maxPrice=50、category=肉类；"
                + "\"库存不足10件的在售商品\"传 status=1、maxStock=10、sortBy=stock。"
                + "返回字段：id、name、category（分类名）、price（单价，元）、stock（库存）、status（1在售 0下架）。";
    }

    @Override
    public Map<String, Object> inputSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("goodsName", Map.of("type", "string", "description", "商品名关键词，模糊匹配，可不传"));
        schema.put("category", Map.of("type", "string", "description", "分类名关键词，如 肉类/水果，模糊匹配，可不传"));
        schema.put("minPrice", Map.of("type", "number", "description", "最低单价（元），可不传"));
        schema.put("maxPrice", Map.of("type", "number", "description", "最高单价（元），可不传"));
        schema.put("status", Map.of("type", "integer", "description", "在售状态 1在售 0下架，可不传查全部"));
        schema.put("maxStock", Map.of("type", "integer", "description", "库存上限，查低库存商品用，可不传"));
        schema.put("sortBy", Map.of("type", "string", "description", "排序字段 price 或 stock，可不传（默认按 id）",
                "enum", List.of("price", "stock")));
        schema.put("sortOrder", Map.of("type", "string", "description", "排序方向 asc 或 desc，可不传（默认 asc）",
                "enum", List.of("asc", "desc")));
        schema.put("limit", Map.of("type", "integer", "description", "返回条数，默认 20，最大 50"));
        return schema;
    }

    @Override
    public List<String> requiredParams() {
        return List.of();
    }

    @Override
    public Object execute(Map<String, Object> args) {
        String goodsName = text(args.get("goodsName"));
        String category = text(args.get("category"));
        BigDecimal minPrice = decimal(args.get("minPrice"), "minPrice");
        BigDecimal maxPrice = decimal(args.get("maxPrice"), "maxPrice");
        Integer status = integer(args.get("status"), "status");
        Integer maxStock = integer(args.get("maxStock"), "maxStock");
        String sortBy = text(args.get("sortBy"));
        String sortOrder = text(args.get("sortOrder"));
        int limit = clamp(integer(args.get("limit"), "limit"), 20, 1, 50);
        if (sortBy != null && !SORT_BY.contains(sortBy)) {
            throw new IllegalArgumentException("sortBy 只支持 price/stock：" + sortBy);
        }
        if (sortOrder != null && !SORT_ORDER.contains(sortOrder)) {
            throw new IllegalArgumentException("sortOrder 只支持 asc/desc：" + sortOrder);
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice 不能大于 maxPrice");
        }

        //分类关键词先解析成分类 id 集合（goods 表只存 categoryId）
        List<Long> categoryIds = null;
        if (category != null) {
            categoryIds = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                            .like(Category::getName, category))
                    .stream().map(Category::getId).collect(Collectors.toList());
            if (categoryIds.isEmpty()) {
                //分类不存在，直接返回空结果，避免生成 IN () 非法 SQL
                return emptyResult(goodsName, category);
            }
        }

        //动态过滤（各条件可选，MP 条件构造器按"条件成立才拼接"处理）
        LambdaQueryWrapper<Goods> qw = new LambdaQueryWrapper<>();
        qw.like(goodsName != null, Goods::getName, goodsName)
                .in(categoryIds != null, Goods::getCategoryId, categoryIds)
                .ge(minPrice != null, Goods::getPrice, minPrice)
                .le(maxPrice != null, Goods::getPrice, maxPrice)
                .eq(status != null, Goods::getStatus, status)
                .le(maxStock != null, Goods::getStock, maxStock);

        //命中总数（未截断），再加排序与条数上限
        Long matchedCount = goodsMapper.selectCount(qw);
        boolean asc = !"desc".equals(sortOrder);
        if ("price".equals(sortBy)) {
            qw.orderBy(true, asc, Goods::getPrice);
        } else if ("stock".equals(sortBy)) {
            qw.orderBy(true, asc, Goods::getStock);
        } else {
            qw.orderByAsc(Goods::getId);
        }
        qw.last("LIMIT " + limit); //limit 已收敛为 1~50 的整数，无注入风险

        List<Goods> goodsList = goodsMapper.selectList(qw);
        Map<Long, String> categoryNameById = categoryMapper.selectList(null)
                .stream().collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));

        //返回列白名单：只暴露该资源域需要的事实，image/description/审计字段不出库
        List<Map<String, Object>> items = new ArrayList<>();
        for (Goods goods : goodsList) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", goods.getId());
            item.put("name", goods.getName());
            item.put("category", categoryNameById.get(goods.getCategoryId()));
            item.put("price", goods.getPrice());
            item.put("stock", goods.getStock());
            item.put("status", goods.getStatus());
            items.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("goodsNameLike", goodsName);
        result.put("categoryLike", category);
        result.put("matchedCount", matchedCount);
        result.put("returnedCount", items.size());
        result.put("items", items);
        return result;
    }

    /**
     * 分类不存在时的空结果
     */
    private static Map<String, Object> emptyResult(String goodsName, String category) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("goodsNameLike", goodsName);
        result.put("categoryLike", category);
        result.put("matchedCount", 0L);
        result.put("returnedCount", 0);
        result.put("items", new ArrayList<>());
        return result;
    }

    /**
     * 取可选字符串参数，空白视为未传
     */
    private static String text(Object value) {
        if (value == null) {
            return null;
        }
        String s = String.valueOf(value).trim();
        return s.isEmpty() ? null : s;
    }

    /**
     * 取可选数字参数（价格区间用）
     */
    private static BigDecimal decimal(Object value, String name) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " 应为数字：" + value);
        }
    }

    /**
     * 取可选整数参数
     */
    private static Integer integer(Object value, String name) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " 应为整数：" + value);
        }
    }

    /**
     * 整数收敛到 [min, max] 区间（缺省取默认值）
     */
    private static int clamp(Integer value, int defaultValue, int min, int max) {
        if (value == null) {
            return defaultValue;
        }
        return Math.max(min, Math.min(max, value));
    }

}
