package com.fresh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fresh.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车 Mapper：继承 MyBatis-Plus 的 BaseMapper，自带单表 CRUD
 * 注意：shopping_cart 表没有 create_user/update_user 等公共字段，
 * 不要给 insert/updateById 标注 @AutoFill（切面反射调用不存在的 setter 会报错），
 * create_time 由数据库 DEFAULT CURRENT_TIMESTAMP 自动填充
 */
@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
}
