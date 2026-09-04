package com.fresh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.fresh.annotation.AutoFill;
import com.fresh.entity.Goods;
import com.fresh.enumeration.OperationType;
import com.fresh.vo.GoodsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品 Mapper：继承 MyBatis-Plus 的 BaseMapper，自带单表 CRUD
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {

    /**
     * 插入一条商品数据
     * 重新声明 BaseMapper 的 insert 并标注 @AutoFill，
     * 让 AutoFillAspect 在插入前填充 createTime/createUser 等公共字段
     */
    @Override
    @AutoFill(OperationType.INSERT)
    int insert(Goods goods);

    /**
     * 根据 id 修改商品
     * 同样重写声明并标注 @AutoFill，让切面在更新前填充 updateTime/updateUser；
     * 注意必须保留 @Param(Constants.ENTITY)（即 "et"），MP 注入的 update SQL 通过 #{et.字段} 取值
     */
    @Override
    @AutoFill(OperationType.UPDATE)
    int updateById(@Param(Constants.ENTITY) Goods goods);

    /**
     * 联表分页查询商品（LEFT JOIN category 带出分类名称，分类被删时 categoryName 为 null）
     * 配合 PageHelper.startPage 使用，返回的 List 实际是 Page 对象；
     * status 为 null 时不过滤状态，C端传 1 只查在售商品
     */
    @Select("<script>" +
            "SELECT g.*, c.name AS categoryName FROM goods g " +
            "LEFT JOIN category c ON g.category_id = c.id " +
            "<where>" +
            "<if test='name != null and name != \"\"'>AND g.name LIKE CONCAT('%', #{name}, '%') </if>" +
            "<if test='categoryId != null and categoryId != 0'>AND g.category_id = #{categoryId} </if>" +
            "<if test='status != null'>AND g.status = #{status} </if>" +
            "</where>" +
            " ORDER BY g.id" +
            "</script>")
    List<GoodsVO> listWithCategory(@Param("name") String name, @Param("categoryId") Integer categoryId,
                                   @Param("status") Integer status);

    /**
     * 根据 id 联表查询单个商品（带分类名称）
     */
    @Select("SELECT g.*, c.name AS categoryName FROM goods g " +
            "LEFT JOIN category c ON g.category_id = c.id WHERE g.id = #{id}")
    GoodsVO getByIdWithCategory(Long id);
}
