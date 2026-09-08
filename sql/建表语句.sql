DROP TABLE if EXISTS `goods`;

CREATE TABLE goods (
    id INT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    category_id INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    image VARCHAR(200),
    description VARCHAR(500),
    `status` TINYINT NOT NULL DEFAULT 1,
    `stock` int(11) NOT NULL,
    stock_mode int(1) NOT NULL,

    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_user INT,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_user INT
);

drop table if EXISTS category;

CREATE TABLE category (
    id INT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `status` TINYINT NOT NULL DEFAULT 1,
    `sort` INT NOT NULL DEFAULT 0,

    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_user INT,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_user INT
);

DROP TABLE if EXISTS `employee`;

  CREATE TABLE `employee` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `name`        varchar(32)  NOT NULL COMMENT '姓名',
    `username`    varchar(32)  NOT NULL COMMENT '用户名',
    `password`    varchar(64)  NOT NULL COMMENT '密码',
    `phone`       varchar(11)  NOT NULL COMMENT '手机号',
    `sex`         varchar(2)   NOT NULL COMMENT '性别',
    `id_number`   varchar(18)  NOT NULL COMMENT '身份证号',
    `status`      int          NOT NULL DEFAULT 1 COMMENT '状态 0:禁用，1:启用',
    `create_time` datetime              DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime              DEFAULT NULL COMMENT '更新时间',
    `create_user` bigint               DEFAULT NULL COMMENT '创建人id',
    `update_user` bigint               DEFAULT NULL COMMENT '修改人id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='员工表';
	
	
	DROP TABLE if EXISTS orders;
	
	CREATE TABLE `orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    
    `number` VARCHAR(50) NOT NULL COMMENT '订单号',
    
    `status` INT NOT NULL DEFAULT 1 COMMENT '订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消',
    
    `user_id` BIGINT NOT NULL COMMENT '下单用户id',
    
    `address_book_id` BIGINT NOT NULL COMMENT '地址id',
    
    `order_time` DATETIME NOT NULL COMMENT '下单时间',
    
    `checkout_time` DATETIME DEFAULT NULL COMMENT '结账时间',
    
    `pay_method` INT DEFAULT NULL COMMENT '支付方式 1微信 2支付宝',
    
    `pay_status` INT NOT NULL DEFAULT 0 COMMENT '支付状态 0未支付 1已支付 2退款',
    
    `amount` DECIMAL(10,2) NOT NULL COMMENT '实收金额',
    
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    
    `user_name` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    
    `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
    
    `consignee` VARCHAR(50) DEFAULT NULL COMMENT '收货人',
    
    `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '订单取消原因',
    
    `rejection_reason` VARCHAR(255) DEFAULT NULL COMMENT '订单拒绝原因',
    
    `cancel_time` DATETIME DEFAULT NULL COMMENT '订单取消时间',
    
    `estimated_delivery_time` DATETIME DEFAULT NULL COMMENT '预计送达时间',
    
    `delivery_status` INT DEFAULT NULL COMMENT '配送状态 1立即送出 0选择具体时间',
    
    `delivery_time` DATETIME DEFAULT NULL COMMENT '送达时间',
    
    PRIMARY KEY (`id`),
    
    UNIQUE KEY `uk_number` (`number`),
    
    KEY `idx_user_id` (`user_id`),
    KEY `idx_address_book_id` (`address_book_id`),
    KEY `idx_status` (`status`),
    KEY `idx_order_time` (`order_time`)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';


DROP TABLE IF EXISTS `shopping_cart`;

CREATE TABLE `shopping_cart` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',

    `name` VARCHAR(100) NOT NULL COMMENT '商品名称',

    `user_id` BIGINT NOT NULL COMMENT '用户id',

    `goods_id` BIGINT NOT NULL COMMENT '商品id',

    `number` INT NOT NULL DEFAULT 1 COMMENT '商品数量',

    `amount` DECIMAL(10,2) NOT NULL COMMENT '商品金额',

    `image` VARCHAR(255) DEFAULT NULL COMMENT '商品图片',

    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    PRIMARY KEY (`id`),

    KEY `idx_user_id` (`user_id`),

    KEY `idx_goods_id` (`goods_id`),

    KEY `idx_user_goods` (`user_id`, `goods_id`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='购物车表';

CREATE TABLE `order_detail` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',

    `name` VARCHAR(100) NOT NULL COMMENT '商品名称',

    `order_id` BIGINT NOT NULL COMMENT '订单id',

    `goods_id` BIGINT NOT NULL COMMENT '商品id',

    `number` INT NOT NULL DEFAULT 1 COMMENT '商品数量',

    `amount` DECIMAL(10,2) NOT NULL COMMENT '商品金额',

    `image` VARCHAR(255) DEFAULT NULL COMMENT '商品图片',

    PRIMARY KEY (`id`),

    KEY `idx_order_id` (`order_id`),

    KEY `idx_goods_id` (`goods_id`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表';

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',

    `name` VARCHAR(50) DEFAULT NULL COMMENT '姓名',

    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',

    `sex` VARCHAR(10) DEFAULT NULL COMMENT '性别',

    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',

    `id_number` VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
		
		`status` INT DEFAULT 1 COMMENT '禁用/启用状态',

    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    PRIMARY KEY (`id`),

    UNIQUE KEY `uk_phone` (`phone`),

    UNIQUE KEY `uk_id_number` (`id_number`)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci
  COMMENT='用户表';
	
	DROP TABLE if EXISTS address_book;
	
	CREATE TABLE `address_book` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',

    `user_id` BIGINT NOT NULL COMMENT '用户id',

    `consignee` VARCHAR(50) NOT NULL COMMENT '收货人',

    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',

    `sex` VARCHAR(10) DEFAULT NULL COMMENT '性别 0女 1男',

    `detail` VARCHAR(255) NOT NULL COMMENT '详细地址',

    `label` VARCHAR(50) DEFAULT NULL COMMENT '标签',

    PRIMARY KEY (`id`),

    KEY `idx_user_id` (`user_id`)

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci
COMMENT='地址簿表';

DROP TABLE if EXISTS seckill_goods;

CREATE TABLE seckill_goods (
    id INT PRIMARY KEY AUTO_INCREMENT,

    name VARCHAR(20) not null,

    seckill_price DECIMAL(10,2) NOT NULL,

    stock INT NOT NULL,

    limit_num INT NOT NULL DEFAULT 1,

    start_time DATETIME NOT NULL,

    end_time DATETIME NOT NULL,

    status TINYINT NOT NULL DEFAULT 0,

    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_user INT,

    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_user INT,

    INDEX idx_name (name),
    INDEX idx_time_status (start_time, end_time, status)

);

DROP TABLE IF EXISTS `seckill_orders`;

CREATE TABLE `seckill_orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    
    `number` VARCHAR(50) NOT NULL COMMENT '订单号',
		
		`seckill_goods_id` BIGINT NOT NULL COMMENT '商品id',
    
    `status` INT NOT NULL DEFAULT 1 COMMENT '订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消',
    
    `user_id` BIGINT NOT NULL COMMENT '下单用户id',
    
    `address_book_id` BIGINT NOT NULL COMMENT '地址id',
    
    `order_time` DATETIME NOT NULL COMMENT '下单时间',
    
    `checkout_time` DATETIME DEFAULT NULL COMMENT '结账时间',
    
    `pay_method` INT DEFAULT NULL COMMENT '支付方式 1微信 2支付宝',
    
    `pay_status` INT NOT NULL DEFAULT 0 COMMENT '支付状态 0未支付 1已支付 2退款',
    
    `amount` DECIMAL(10,2) NOT NULL COMMENT '实收金额',
    
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    
    `user_name` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    
    `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
    
    `consignee` VARCHAR(50) DEFAULT NULL COMMENT '收货人',
    
    `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '订单取消原因',
    
    `rejection_reason` VARCHAR(255) DEFAULT NULL COMMENT '订单拒绝原因',
    
    `cancel_time` DATETIME DEFAULT NULL COMMENT '订单取消时间',
    
    `estimated_delivery_time` DATETIME DEFAULT NULL COMMENT '预计送达时间',
    
    `delivery_status` INT DEFAULT NULL COMMENT '配送状态 1立即送出 0选择具体时间',
    
    `delivery_time` DATETIME DEFAULT NULL COMMENT '送达时间',
    
    PRIMARY KEY (`id`),
    
    UNIQUE KEY `uk_number` (`number`),
    
    KEY `idx_user_id` (`user_id`),
    KEY `idx_address_book_id` (`address_book_id`),
    KEY `idx_status` (`status`),
    KEY `idx_order_time` (`order_time`)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='秒杀订单表';
