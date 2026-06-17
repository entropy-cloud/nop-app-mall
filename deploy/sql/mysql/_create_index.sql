-- App-mall indexes generated from model/app-mall.orm.xml
-- This file is maintained separately from _create_app-mall.sql because the platform
-- _create_app.sql.xgen template (ddl.xlib CreateTables) does not emit CREATE INDEX.
-- Run AFTER _create_app-mall.sql during manual production deployment.
-- Source of truth: model/app-mall.orm.xml <index> definitions (31 indexes).
-- Columns: UPPERCASE (MySQL/Oracle)

CREATE INDEX idx_address_userId ON litemall_address (USER_ID);
CREATE INDEX idx_aftersale_orderId ON litemall_aftersale (ORDER_ID);
CREATE INDEX idx_aftersale_userId ON litemall_aftersale (USER_ID);
CREATE INDEX idx_cart_userId ON litemall_cart (USER_ID);
CREATE INDEX idx_cart_goodsId ON litemall_cart (GOODS_ID);
CREATE INDEX idx_category_pid ON litemall_category (PID);
CREATE INDEX idx_collect_userId ON litemall_collect (USER_ID);
CREATE INDEX idx_comment_userId ON litemall_comment (USER_ID);
CREATE INDEX idx_couponUser_userId ON litemall_coupon_user (USER_ID);
CREATE INDEX idx_couponUser_orderId ON litemall_coupon_user (ORDER_ID);
CREATE INDEX idx_couponUser_couponId ON litemall_coupon_user (COUPON_ID);
CREATE INDEX idx_feedback_userId ON litemall_feedback (USER_ID);
CREATE INDEX idx_footprint_userId ON litemall_footprint (USER_ID);
CREATE INDEX idx_footprint_goodsId ON litemall_footprint (GOODS_ID);
CREATE INDEX idx_goods_categoryId ON litemall_goods (CATEGORY_ID);
CREATE INDEX idx_goods_brandId ON litemall_goods (BRAND_ID);
CREATE INDEX idx_attr_goodsId ON litemall_goods_attribute (GOODS_ID);
CREATE INDEX idx_product_goodsId ON litemall_goods_product (GOODS_ID);
CREATE INDEX idx_spec_goodsId ON litemall_goods_specification (GOODS_ID);
CREATE INDEX idx_groupon_orderId ON litemall_groupon (ORDER_ID);
CREATE INDEX idx_groupon_grouponRulesId ON litemall_groupon (RULES_ID);
CREATE INDEX idx_groupon_userId ON litemall_groupon (USER_ID);
CREATE INDEX idx_grouponRules_goodsId ON litemall_groupon_rules (GOODS_ID);
CREATE INDEX idx_order_userId ON litemall_order (USER_ID);
CREATE INDEX idx_order_status ON litemall_order (ORDER_STATUS);
CREATE INDEX idx_orderGoods_orderId ON litemall_order_goods (ORDER_ID);
CREATE INDEX idx_orderGoods_goodsId ON litemall_order_goods (GOODS_ID);
CREATE INDEX idx_orderGoods_productId ON litemall_order_goods (PRODUCT_ID);
CREATE INDEX idx_region_pid ON litemall_region (PID);
CREATE INDEX idx_searchHistory_userId ON litemall_search_history (USER_ID);
CREATE INDEX idx_resetCode_mobile ON litemall_reset_code (MOBILE);
