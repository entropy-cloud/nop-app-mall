
    alter table litemall_ad add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_address add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_admin add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_aftersale add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_brand add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_cart add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_category add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_collect add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_comment add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_coupon add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_coupon_user add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_feedback add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_footprint add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_goods add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_goods_attribute add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_goods_product add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_goods_specification add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_groupon add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_groupon_rules add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_issue add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_keyword add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_log add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_notice add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_notice_admin add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_order add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_order_goods add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_permission add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_region add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_role add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_search_history add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_storage add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_system add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_topic add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_user add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_user_role add NOP_TENANT_ID VARCHAR(32) DEFAULT '0' NOT NULL;

alter table litemall_ad drop primary key;
alter table litemall_ad add primary key (NOP_TENANT_ID, ID);

alter table litemall_address drop primary key;
alter table litemall_address add primary key (NOP_TENANT_ID, ID);

alter table litemall_admin drop primary key;
alter table litemall_admin add primary key (NOP_TENANT_ID, ID);

alter table litemall_aftersale drop primary key;
alter table litemall_aftersale add primary key (NOP_TENANT_ID, ID);

alter table litemall_brand drop primary key;
alter table litemall_brand add primary key (NOP_TENANT_ID, ID);

alter table litemall_cart drop primary key;
alter table litemall_cart add primary key (NOP_TENANT_ID, ID);

alter table litemall_category drop primary key;
alter table litemall_category add primary key (NOP_TENANT_ID, ID);

alter table litemall_collect drop primary key;
alter table litemall_collect add primary key (NOP_TENANT_ID, ID);

alter table litemall_comment drop primary key;
alter table litemall_comment add primary key (NOP_TENANT_ID, ID);

alter table litemall_coupon drop primary key;
alter table litemall_coupon add primary key (NOP_TENANT_ID, ID);

alter table litemall_coupon_user drop primary key;
alter table litemall_coupon_user add primary key (NOP_TENANT_ID, ID);

alter table litemall_feedback drop primary key;
alter table litemall_feedback add primary key (NOP_TENANT_ID, ID);

alter table litemall_footprint drop primary key;
alter table litemall_footprint add primary key (NOP_TENANT_ID, ID);

alter table litemall_goods drop primary key;
alter table litemall_goods add primary key (NOP_TENANT_ID, ID);

alter table litemall_goods_attribute drop primary key;
alter table litemall_goods_attribute add primary key (NOP_TENANT_ID, ID);

alter table litemall_goods_product drop primary key;
alter table litemall_goods_product add primary key (NOP_TENANT_ID, ID);

alter table litemall_goods_specification drop primary key;
alter table litemall_goods_specification add primary key (NOP_TENANT_ID, ID);

alter table litemall_groupon drop primary key;
alter table litemall_groupon add primary key (NOP_TENANT_ID, ID);

alter table litemall_groupon_rules drop primary key;
alter table litemall_groupon_rules add primary key (NOP_TENANT_ID, ID);

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

alter table litemall_order drop primary key;
alter table litemall_order add primary key (NOP_TENANT_ID, ID);

alter table litemall_order_goods drop primary key;
alter table litemall_order_goods add primary key (NOP_TENANT_ID, ID);

alter table litemall_permission drop primary key;
alter table litemall_permission add primary key (NOP_TENANT_ID, ID);

alter table litemall_region drop primary key;
alter table litemall_region add primary key (NOP_TENANT_ID, ID);

alter table litemall_role drop primary key;
alter table litemall_role add primary key (NOP_TENANT_ID, ID);

alter table litemall_search_history drop primary key;
alter table litemall_search_history add primary key (NOP_TENANT_ID, ID);

alter table litemall_storage drop primary key;
alter table litemall_storage add primary key (NOP_TENANT_ID, ID);

alter table litemall_system drop primary key;
alter table litemall_system add primary key (NOP_TENANT_ID, ID);

alter table litemall_topic drop primary key;
alter table litemall_topic add primary key (NOP_TENANT_ID, ID);

alter table litemall_user drop primary key;
alter table litemall_user add primary key (NOP_TENANT_ID, ID);

alter table litemall_user_role drop primary key;
alter table litemall_user_role add primary key (NOP_TENANT_ID, USER_ID,ROLE_ID);


