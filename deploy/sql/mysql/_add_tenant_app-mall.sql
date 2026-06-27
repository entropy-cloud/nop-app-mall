
    alter table litemall_ad add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_address add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_brand add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_category add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_collect add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_topic add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_coupon add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_feedback add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_issue add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_keyword add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_log add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_notice add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_notice_admin add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_pickup_store add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_region add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_search_history add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_storage add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_system add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_reset_code add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_promotion_activity add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_check_in_rule add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_check_in_record add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_points_account add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_wallet add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_user_message add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_material_category add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_goods add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_coupon_user add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_order add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_promotion_tier add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_points_flow add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_wallet_flow add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_recharge add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_material add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_cart add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_comment add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_footprint add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_goods_attribute add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_goods_product add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_goods_specification add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_groupon_rules add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_time_discount add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_flash_sale add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_pin_tuan_activity add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_aftersale add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_groupon add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_order_goods add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_flash_sale_session add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_pin_tuan_group add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_pin_tuan_member add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_ad drop primary key;
alter table litemall_ad add primary key (NOP_TENANT_ID, ID);

alter table litemall_address drop primary key;
alter table litemall_address add primary key (NOP_TENANT_ID, ID);

alter table litemall_brand drop primary key;
alter table litemall_brand add primary key (NOP_TENANT_ID, ID);

alter table litemall_category drop primary key;
alter table litemall_category add primary key (NOP_TENANT_ID, ID);

alter table litemall_collect drop primary key;
alter table litemall_collect add primary key (NOP_TENANT_ID, ID);

alter table litemall_topic drop primary key;
alter table litemall_topic add primary key (NOP_TENANT_ID, ID);

alter table litemall_coupon drop primary key;
alter table litemall_coupon add primary key (NOP_TENANT_ID, ID);

alter table litemall_feedback drop primary key;
alter table litemall_feedback add primary key (NOP_TENANT_ID, ID);

alter table litemall_issue drop primary key;
alter table litemall_issue add primary key (NOP_TENANT_ID, ID);

alter table litemall_keyword drop primary key;
alter table litemall_keyword add primary key (NOP_TENANT_ID, ID);

alter table litemall_log drop primary key;
alter table litemall_log add primary key (NOP_TENANT_ID, ID);

alter table litemall_notice drop primary key;
alter table litemall_notice add primary key (NOP_TENANT_ID, ID);

alter table litemall_notice_admin drop primary key;
alter table litemall_notice_admin add primary key (NOP_TENANT_ID, ID);

alter table litemall_pickup_store drop primary key;
alter table litemall_pickup_store add primary key (NOP_TENANT_ID, ID);

alter table litemall_region drop primary key;
alter table litemall_region add primary key (NOP_TENANT_ID, ID);

alter table litemall_search_history drop primary key;
alter table litemall_search_history add primary key (NOP_TENANT_ID, ID);

alter table litemall_storage drop primary key;
alter table litemall_storage add primary key (NOP_TENANT_ID, ID);

alter table litemall_system drop primary key;
alter table litemall_system add primary key (NOP_TENANT_ID, ID);

alter table litemall_reset_code drop primary key;
alter table litemall_reset_code add primary key (NOP_TENANT_ID, ID);

alter table litemall_promotion_activity drop primary key;
alter table litemall_promotion_activity add primary key (NOP_TENANT_ID, ID);

alter table litemall_check_in_rule drop primary key;
alter table litemall_check_in_rule add primary key (NOP_TENANT_ID, ID);

alter table litemall_check_in_record drop primary key;
alter table litemall_check_in_record add primary key (NOP_TENANT_ID, ID);

alter table litemall_points_account drop primary key;
alter table litemall_points_account add primary key (NOP_TENANT_ID, ID);

alter table litemall_wallet drop primary key;
alter table litemall_wallet add primary key (NOP_TENANT_ID, ID);

alter table litemall_user_message drop primary key;
alter table litemall_user_message add primary key (NOP_TENANT_ID, ID);

alter table litemall_material_category drop primary key;
alter table litemall_material_category add primary key (NOP_TENANT_ID, ID);

alter table litemall_goods drop primary key;
alter table litemall_goods add primary key (NOP_TENANT_ID, ID);

alter table litemall_coupon_user drop primary key;
alter table litemall_coupon_user add primary key (NOP_TENANT_ID, ID);

alter table litemall_order drop primary key;
alter table litemall_order add primary key (NOP_TENANT_ID, ID);

alter table litemall_promotion_tier drop primary key;
alter table litemall_promotion_tier add primary key (NOP_TENANT_ID, ID);

alter table litemall_points_flow drop primary key;
alter table litemall_points_flow add primary key (NOP_TENANT_ID, ID);

alter table litemall_wallet_flow drop primary key;
alter table litemall_wallet_flow add primary key (NOP_TENANT_ID, ID);

alter table litemall_recharge drop primary key;
alter table litemall_recharge add primary key (NOP_TENANT_ID, ID);

alter table litemall_material drop primary key;
alter table litemall_material add primary key (NOP_TENANT_ID, ID);

alter table litemall_cart drop primary key;
alter table litemall_cart add primary key (NOP_TENANT_ID, ID);

alter table litemall_comment drop primary key;
alter table litemall_comment add primary key (NOP_TENANT_ID, ID);

alter table litemall_footprint drop primary key;
alter table litemall_footprint add primary key (NOP_TENANT_ID, ID);

alter table litemall_goods_attribute drop primary key;
alter table litemall_goods_attribute add primary key (NOP_TENANT_ID, ID);

alter table litemall_goods_product drop primary key;
alter table litemall_goods_product add primary key (NOP_TENANT_ID, ID);

alter table litemall_goods_specification drop primary key;
alter table litemall_goods_specification add primary key (NOP_TENANT_ID, ID);

alter table litemall_groupon_rules drop primary key;
alter table litemall_groupon_rules add primary key (NOP_TENANT_ID, ID);

alter table litemall_time_discount drop primary key;
alter table litemall_time_discount add primary key (NOP_TENANT_ID, ID);

alter table litemall_flash_sale drop primary key;
alter table litemall_flash_sale add primary key (NOP_TENANT_ID, ID);

alter table litemall_pin_tuan_activity drop primary key;
alter table litemall_pin_tuan_activity add primary key (NOP_TENANT_ID, ID);

alter table litemall_aftersale drop primary key;
alter table litemall_aftersale add primary key (NOP_TENANT_ID, ID);

alter table litemall_groupon drop primary key;
alter table litemall_groupon add primary key (NOP_TENANT_ID, ID);

alter table litemall_order_goods drop primary key;
alter table litemall_order_goods add primary key (NOP_TENANT_ID, ID);

alter table litemall_flash_sale_session drop primary key;
alter table litemall_flash_sale_session add primary key (NOP_TENANT_ID, ID);

alter table litemall_pin_tuan_group drop primary key;
alter table litemall_pin_tuan_group add primary key (NOP_TENANT_ID, ID);

alter table litemall_pin_tuan_member drop primary key;
alter table litemall_pin_tuan_member add primary key (NOP_TENANT_ID, ID);


