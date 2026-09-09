package com.fresh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.fresh.annotation.AutoFill;
import com.fresh.entity.GoodsComments;
import com.fresh.enumeration.OperationType;
import com.fresh.vo.GoodsCommentsAdminVO;
import com.fresh.vo.GoodsCommentsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品评价 Mapper：继承 MyBatis-Plus 的 BaseMapper，自带单表 CRUD
 */
@Mapper
public interface GoodsCommentsMapper extends BaseMapper<GoodsComments> {

    /**
     * 插入一条评价
     * 重新声明 BaseMapper 的 insert 并标注 @AutoFill，
     * 让 AutoFillAspect 在插入前填充 createTime/createUser 等公共字段
     */
    @Override
    @AutoFill(OperationType.INSERT)
    int insert(GoodsComments goodsComments);

    /**
     * 按实体主键更新非空字段（管理端回复用）
     * 重新声明 BaseMapper 的 updateById 并标注 @AutoFill，
     * 让 AutoFillAspect 在更新前填充 updateTime/updateUser 等公共字段
     */
    @Override
    @AutoFill(OperationType.UPDATE)
    int updateById(@Param(Constants.ENTITY) GoodsComments goodsComments);

    /**
     * 用户端：按商品查显示中的评价（LEFT JOIN user 带出昵称/头像）
     * 匿名评价在 SQL 里脱敏：userName 显示"匿名用户"、userAvatar 置空
     * 配合 PageHelper.startPage 使用，返回的 List 实际是 Page 对象；按评价 id 倒序（最新在前）
     */
    @Select("SELECT c.id, c.rating, c.content, c.anonymous, c.reply, c.reply_time, c.create_time, " +
            "CASE WHEN c.anonymous = 1 THEN '匿名用户' ELSE u.name END AS userName, " +
            "CASE WHEN c.anonymous = 1 THEN NULL ELSE u.avatar END AS userAvatar " +
            "FROM goods_comments c LEFT JOIN `user` u ON c.user_id = u.id " +
            "WHERE c.goods_id = #{goodsId} AND c.status = 1 " +
            "ORDER BY c.id DESC")
    List<GoodsCommentsVO> listForUser(@Param("goodsId") Long goodsId);

    /**
     * 管理端：全量分页查询评价（LEFT JOIN goods/user 带出商品名与真实昵称，不受匿名脱敏影响）
     * 条件全部可选：goodsId 精确、rating 精确、status 精确、content 模糊；
     * 配合 PageHelper.startPage 使用，返回的 List 实际是 Page 对象；按评价 id 倒序
     */
    @Select("<script>" +
            "SELECT c.id, c.goods_id, g.name AS goodsName, c.user_id, u.name AS userName, " +
            "c.order_id, c.order_detail_id, c.rating, c.content, c.anonymous, " +
            "c.reply, c.reply_time, c.status, c.create_time " +
            "FROM goods_comments c " +
            "LEFT JOIN goods g ON c.goods_id = g.id " +
            "LEFT JOIN `user` u ON c.user_id = u.id " +
            "<where>" +
            "<if test='goodsId != null'>AND c.goods_id = #{goodsId} </if>" +
            "<if test='rating != null'>AND c.rating = #{rating} </if>" +
            "<if test='status != null'>AND c.status = #{status} </if>" +
            "<if test='content != null and content != \"\"'>AND c.content LIKE CONCAT('%', #{content}, '%') </if>" +
            "</where>" +
            " ORDER BY c.id DESC" +
            "</script>")
    List<GoodsCommentsAdminVO> listForAdmin(@Param("goodsId") Long goodsId,
                                            @Param("rating") Integer rating,
                                            @Param("status") Integer status,
                                            @Param("content") String content);
}
