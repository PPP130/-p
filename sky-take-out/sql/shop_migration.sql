-- =============================================
-- 多店铺点餐平台 数据库改造脚本
-- =============================================

-- 1. 创建店铺表
CREATE TABLE IF NOT EXISTS `shop` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `shop_id` VARCHAR(20) NOT NULL COMMENT '店铺编号（随机生成，如 SJ20260513A3F7）',
    `name` VARCHAR(64) NOT NULL COMMENT '店铺名称',
    `owner_name` VARCHAR(32) NOT NULL COMMENT '店主姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `address` VARCHAR(256) DEFAULT NULL COMMENT '店铺地址',
    `image` VARCHAR(256) DEFAULT NULL COMMENT '店铺图片',
    `description` VARCHAR(512) DEFAULT NULL COMMENT '店铺简介',
    `status` INT NOT NULL DEFAULT 0 COMMENT '审核状态 0待审核 1已通过 2已拒绝 3已禁用',
    `business_status` INT NOT NULL DEFAULT 1 COMMENT '营业状态 0打烊 1营业中',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_shop_id` (`shop_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺表';

-- 2. 给现有表添加 shop_id 字段（外键关联 shop.shop_id）

-- 员工表：每个店铺有自己的员工
ALTER TABLE `employee` ADD COLUMN `shop_id` VARCHAR(20) DEFAULT NULL COMMENT '所属店铺编号' AFTER `id`;
ALTER TABLE `employee` ADD INDEX `idx_shop_id` (`shop_id`);

-- 分类表：每个店铺有自己的分类
ALTER TABLE `category` ADD COLUMN `shop_id` VARCHAR(20) DEFAULT NULL COMMENT '所属店铺编号' AFTER `id`;
ALTER TABLE `category` ADD INDEX `idx_shop_id` (`shop_id`);

-- 菜品表：每个店铺有自己的菜品
ALTER TABLE `dish` ADD COLUMN `shop_id` VARCHAR(20) DEFAULT NULL COMMENT '所属店铺编号' AFTER `id`;
ALTER TABLE `dish` ADD INDEX `idx_shop_id` (`shop_id`);

-- 套餐表：每个店铺有自己的套餐
ALTER TABLE `setmeal` ADD COLUMN `shop_id` VARCHAR(20) DEFAULT NULL COMMENT '所属店铺编号' AFTER `id`;
ALTER TABLE `setmeal` ADD INDEX `idx_shop_id` (`shop_id`);

-- 订单表：每个店铺有自己的订单
ALTER TABLE `orders` ADD COLUMN `shop_id` VARCHAR(20) DEFAULT NULL COMMENT '所属店铺编号' AFTER `id`;
ALTER TABLE `orders` ADD INDEX `idx_shop_id` (`shop_id`);

-- 购物车表：用户在不同店铺的购物车隔离
ALTER TABLE `shopping_cart` ADD COLUMN `shop_id` VARCHAR(20) DEFAULT NULL COMMENT '所属店铺编号' AFTER `id`;
ALTER TABLE `shopping_cart` ADD INDEX `idx_shop_id` (`shop_id`);

-- 用户表：不加 shop_id（用户是平台级的，可以访问多个店铺）
-- 地址簿：不加 shop_id（地址是用户级别的，不归属某个店铺）
-- 菜品口味表：不加 shop_id（通过 dish_id 关联即可）
-- 套餐菜品关系表：不加 shop_id（通过 setmeal_id 关联即可）
-- 订单明细表：不加 shop_id（通过 order_id 关联即可）
