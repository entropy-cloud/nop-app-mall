-- App-mall indexes generated from model/app-mall.orm.xml
-- This file is maintained separately from _create_app-mall.sql because the platform
-- _create_app.sql.xgen template (ddl.xlib CreateTables) does not emit CREATE INDEX.
-- Run AFTER _create_app-mall.sql during manual production deployment.
-- Source of truth: model/app-mall.orm.xml <index> definitions (31 indexes).
-- Columns: lowercase (PostgreSQL)

CREATE INDEX idx_address_userId ON litemall_address (user_id);
CREATE INDEX idx_aftersale_orderId ON litemall_aftersale (order_id);
CREATE INDEX idx_aftersale_userId ON litemall_aftersale (user_id);
CREATE INDEX idx_cart_userId ON litemall_cart (user_id);
CREATE INDEX idx_cart_goodsId ON litemall_cart (goods_id);
CREATE INDEX idx_category_pid ON litemall_category (pid);
CREATE INDEX idx_collect_userId ON litemall_collect (user_id);
CREATE INDEX idx_comment_userId ON litemall_comment (user_id);
CREATE INDEX idx_couponUser_userId ON litemall_coupon_user (user_id);
CREATE INDEX idx_couponUser_orderId ON litemall_coupon_user (order_id);
CREATE INDEX idx_couponUser_couponId ON litemall_coupon_user (coupon_id);
CREATE INDEX idx_feedback_userId ON litemall_feedback (user_id);
CREATE INDEX idx_footprint_userId ON litemall_footprint (user_id);
CREATE INDEX idx_footprint_goodsId ON litemall_footprint (goods_id);
CREATE INDEX idx_goods_categoryId ON litemall_goods (category_id);
CREATE INDEX idx_goods_brandId ON litemall_goods (brand_id);
CREATE INDEX idx_attr_goodsId ON litemall_goods_attribute (goods_id);
CREATE INDEX idx_product_goodsId ON litemall_goods_product (goods_id);
CREATE INDEX idx_spec_goodsId ON litemall_goods_specification (goods_id);
CREATE INDEX idx_groupon_orderId ON litemall_groupon (order_id);
CREATE INDEX idx_groupon_grouponRulesId ON litemall_groupon (rules_id);
CREATE INDEX idx_groupon_userId ON litemall_groupon (user_id);
CREATE INDEX idx_grouponRules_goodsId ON litemall_groupon_rules (goods_id);
CREATE INDEX idx_order_userId ON litemall_order (user_id);
CREATE INDEX idx_order_status ON litemall_order (order_status);
CREATE INDEX idx_orderGoods_orderId ON litemall_order_goods (order_id);
CREATE INDEX idx_orderGoods_goodsId ON litemall_order_goods (goods_id);
CREATE INDEX idx_orderGoods_productId ON litemall_order_goods (product_id);
CREATE INDEX idx_region_pid ON litemall_region (pid);
CREATE INDEX idx_searchHistory_userId ON litemall_search_history (user_id);
CREATE INDEX idx_resetCode_mobile ON litemall_reset_code (mobile);
