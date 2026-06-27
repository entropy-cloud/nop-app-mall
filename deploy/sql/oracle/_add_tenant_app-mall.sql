
    alter table litemall_ad add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_address add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_brand add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_category add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_collect add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_topic add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_coupon add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_feedback add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_issue add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_keyword add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_log add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_notice add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_notice_admin add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_pickup_store add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_region add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_search_history add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_storage add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_system add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_reset_code add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_promotion_activity add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_member_level add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_check_in_rule add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_check_in_record add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_points_account add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_wallet add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_user_message add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_material_category add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_goods add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_coupon_user add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_order add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_promotion_tier add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_points_flow add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_wallet_flow add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_recharge add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_material add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_cart add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_comment add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_footprint add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_goods_attribute add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_goods_product add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_goods_specification add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_groupon_rules add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_time_discount add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_flash_sale add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_pin_tuan_activity add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_aftersale add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_groupon add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_order_goods add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_flash_sale_session add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_pin_tuan_group add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_pin_tuan_member add NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_ad drop constraint PK_litemall_ad;
alter table litemall_ad add constraint PK_litemall_ad primary key (NOP_TENANT_ID, ID);

alter table litemall_address drop constraint PK_litemall_address;
alter table litemall_address add constraint PK_litemall_address primary key (NOP_TENANT_ID, ID);

alter table litemall_brand drop constraint PK_litemall_brand;
alter table litemall_brand add constraint PK_litemall_brand primary key (NOP_TENANT_ID, ID);

alter table litemall_category drop constraint PK_litemall_category;
alter table litemall_category add constraint PK_litemall_category primary key (NOP_TENANT_ID, ID);

alter table litemall_collect drop constraint PK_litemall_collect;
alter table litemall_collect add constraint PK_litemall_collect primary key (NOP_TENANT_ID, ID);

alter table litemall_topic drop constraint PK_litemall_topic;
alter table litemall_topic add constraint PK_litemall_topic primary key (NOP_TENANT_ID, ID);

alter table litemall_coupon drop constraint PK_litemall_coupon;
alter table litemall_coupon add constraint PK_litemall_coupon primary key (NOP_TENANT_ID, ID);

alter table litemall_feedback drop constraint PK_litemall_feedback;
alter table litemall_feedback add constraint PK_litemall_feedback primary key (NOP_TENANT_ID, ID);

alter table litemall_issue drop constraint PK_litemall_issue;
alter table litemall_issue add constraint PK_litemall_issue primary key (NOP_TENANT_ID, ID);

alter table litemall_keyword drop constraint PK_litemall_keyword;
alter table litemall_keyword add constraint PK_litemall_keyword primary key (NOP_TENANT_ID, ID);

alter table litemall_log drop constraint PK_litemall_log;
alter table litemall_log add constraint PK_litemall_log primary key (NOP_TENANT_ID, ID);

alter table litemall_notice drop constraint PK_litemall_notice;
alter table litemall_notice add constraint PK_litemall_notice primary key (NOP_TENANT_ID, ID);

alter table litemall_notice_admin drop constraint PK_litemall_notice_admin;
alter table litemall_notice_admin add constraint PK_litemall_notice_admin primary key (NOP_TENANT_ID, ID);

alter table litemall_pickup_store drop constraint PK_litemall_pickup_store;
alter table litemall_pickup_store add constraint PK_litemall_pickup_store primary key (NOP_TENANT_ID, ID);

alter table litemall_region drop constraint PK_litemall_region;
alter table litemall_region add constraint PK_litemall_region primary key (NOP_TENANT_ID, ID);

alter table litemall_search_history drop constraint PK_litemall_search_history;
alter table litemall_search_history add constraint PK_litemall_search_history primary key (NOP_TENANT_ID, ID);

alter table litemall_storage drop constraint PK_litemall_storage;
alter table litemall_storage add constraint PK_litemall_storage primary key (NOP_TENANT_ID, ID);

alter table litemall_system drop constraint PK_litemall_system;
alter table litemall_system add constraint PK_litemall_system primary key (NOP_TENANT_ID, ID);

alter table litemall_reset_code drop constraint PK_litemall_reset_code;
alter table litemall_reset_code add constraint PK_litemall_reset_code primary key (NOP_TENANT_ID, ID);

alter table litemall_promotion_activity drop constraint PK_litemall_promotion_activity;
alter table litemall_promotion_activity add constraint PK_litemall_promotion_activity primary key (NOP_TENANT_ID, ID);

alter table litemall_member_level drop constraint PK_litemall_member_level;
alter table litemall_member_level add constraint PK_litemall_member_level primary key (NOP_TENANT_ID, ID);

alter table litemall_check_in_rule drop constraint PK_litemall_check_in_rule;
alter table litemall_check_in_rule add constraint PK_litemall_check_in_rule primary key (NOP_TENANT_ID, ID);

alter table litemall_check_in_record drop constraint PK_litemall_check_in_record;
alter table litemall_check_in_record add constraint PK_litemall_check_in_record primary key (NOP_TENANT_ID, ID);

alter table litemall_points_account drop constraint PK_litemall_points_account;
alter table litemall_points_account add constraint PK_litemall_points_account primary key (NOP_TENANT_ID, ID);

alter table litemall_wallet drop constraint PK_litemall_wallet;
alter table litemall_wallet add constraint PK_litemall_wallet primary key (NOP_TENANT_ID, ID);

alter table litemall_user_message drop constraint PK_litemall_user_message;
alter table litemall_user_message add constraint PK_litemall_user_message primary key (NOP_TENANT_ID, ID);

alter table litemall_material_category drop constraint PK_litemall_material_category;
alter table litemall_material_category add constraint PK_litemall_material_category primary key (NOP_TENANT_ID, ID);

alter table litemall_goods drop constraint PK_litemall_goods;
alter table litemall_goods add constraint PK_litemall_goods primary key (NOP_TENANT_ID, ID);

alter table litemall_coupon_user drop constraint PK_litemall_coupon_user;
alter table litemall_coupon_user add constraint PK_litemall_coupon_user primary key (NOP_TENANT_ID, ID);

alter table litemall_order drop constraint PK_litemall_order;
alter table litemall_order add constraint PK_litemall_order primary key (NOP_TENANT_ID, ID);

alter table litemall_promotion_tier drop constraint PK_litemall_promotion_tier;
alter table litemall_promotion_tier add constraint PK_litemall_promotion_tier primary key (NOP_TENANT_ID, ID);

alter table litemall_points_flow drop constraint PK_litemall_points_flow;
alter table litemall_points_flow add constraint PK_litemall_points_flow primary key (NOP_TENANT_ID, ID);

alter table litemall_wallet_flow drop constraint PK_litemall_wallet_flow;
alter table litemall_wallet_flow add constraint PK_litemall_wallet_flow primary key (NOP_TENANT_ID, ID);

alter table litemall_recharge drop constraint PK_litemall_recharge;
alter table litemall_recharge add constraint PK_litemall_recharge primary key (NOP_TENANT_ID, ID);

alter table litemall_material drop constraint PK_litemall_material;
alter table litemall_material add constraint PK_litemall_material primary key (NOP_TENANT_ID, ID);

alter table litemall_cart drop constraint PK_litemall_cart;
alter table litemall_cart add constraint PK_litemall_cart primary key (NOP_TENANT_ID, ID);

alter table litemall_comment drop constraint PK_litemall_comment;
alter table litemall_comment add constraint PK_litemall_comment primary key (NOP_TENANT_ID, ID);

alter table litemall_footprint drop constraint PK_litemall_footprint;
alter table litemall_footprint add constraint PK_litemall_footprint primary key (NOP_TENANT_ID, ID);

alter table litemall_goods_attribute drop constraint PK_litemall_goods_attribute;
alter table litemall_goods_attribute add constraint PK_litemall_goods_attribute primary key (NOP_TENANT_ID, ID);

alter table litemall_goods_product drop constraint PK_litemall_goods_product;
alter table litemall_goods_product add constraint PK_litemall_goods_product primary key (NOP_TENANT_ID, ID);

alter table litemall_goods_specification drop constraint PK_litemall_goods_specification;
alter table litemall_goods_specification add constraint PK_litemall_goods_specification primary key (NOP_TENANT_ID, ID);

alter table litemall_groupon_rules drop constraint PK_litemall_groupon_rules;
alter table litemall_groupon_rules add constraint PK_litemall_groupon_rules primary key (NOP_TENANT_ID, ID);

alter table litemall_time_discount drop constraint PK_litemall_time_discount;
alter table litemall_time_discount add constraint PK_litemall_time_discount primary key (NOP_TENANT_ID, ID);

alter table litemall_flash_sale drop constraint PK_litemall_flash_sale;
alter table litemall_flash_sale add constraint PK_litemall_flash_sale primary key (NOP_TENANT_ID, ID);

alter table litemall_pin_tuan_activity drop constraint PK_litemall_pin_tuan_activity;
alter table litemall_pin_tuan_activity add constraint PK_litemall_pin_tuan_activity primary key (NOP_TENANT_ID, ID);

alter table litemall_aftersale drop constraint PK_litemall_aftersale;
alter table litemall_aftersale add constraint PK_litemall_aftersale primary key (NOP_TENANT_ID, ID);

alter table litemall_groupon drop constraint PK_litemall_groupon;
alter table litemall_groupon add constraint PK_litemall_groupon primary key (NOP_TENANT_ID, ID);

alter table litemall_order_goods drop constraint PK_litemall_order_goods;
alter table litemall_order_goods add constraint PK_litemall_order_goods primary key (NOP_TENANT_ID, ID);

alter table litemall_flash_sale_session drop constraint PK_litemall_flash_sale_session;
alter table litemall_flash_sale_session add constraint PK_litemall_flash_sale_session primary key (NOP_TENANT_ID, ID);

alter table litemall_pin_tuan_group drop constraint PK_litemall_pin_tuan_group;
alter table litemall_pin_tuan_group add constraint PK_litemall_pin_tuan_group primary key (NOP_TENANT_ID, ID);

alter table litemall_pin_tuan_member drop constraint PK_litemall_pin_tuan_member;
alter table litemall_pin_tuan_member add constraint PK_litemall_pin_tuan_member primary key (NOP_TENANT_ID, ID);


