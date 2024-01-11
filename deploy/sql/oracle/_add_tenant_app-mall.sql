
    alter table litemall_ad add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_address add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_admin add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_aftersale add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_brand add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_cart add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_category add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_collect add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_comment add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_coupon add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_coupon_user add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_feedback add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_footprint add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_goods add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_goods_attribute add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_goods_product add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_goods_specification add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_groupon add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_groupon_rules add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_issue add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_keyword add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_log add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_notice add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_notice_admin add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_order add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_order_goods add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_permission add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_region add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_role add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_search_history add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_storage add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_system add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_topic add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_user add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_user_role add column NOP_TENANT_ID VARCHAR2(32) DEFAULT '0' NOT NULL;

alter table litemall_ad drop constraint PK_litemall_ad;
alter table litemall_ad add constraint PK_litemall_ad primary key (NOP_TENANT_ID, ID);

alter table litemall_address drop constraint PK_litemall_address;
alter table litemall_address add constraint PK_litemall_address primary key (NOP_TENANT_ID, ID);

alter table litemall_admin drop constraint PK_litemall_admin;
alter table litemall_admin add constraint PK_litemall_admin primary key (NOP_TENANT_ID, ID);

alter table litemall_aftersale drop constraint PK_litemall_aftersale;
alter table litemall_aftersale add constraint PK_litemall_aftersale primary key (NOP_TENANT_ID, ID);

alter table litemall_brand drop constraint PK_litemall_brand;
alter table litemall_brand add constraint PK_litemall_brand primary key (NOP_TENANT_ID, ID);

alter table litemall_cart drop constraint PK_litemall_cart;
alter table litemall_cart add constraint PK_litemall_cart primary key (NOP_TENANT_ID, ID);

alter table litemall_category drop constraint PK_litemall_category;
alter table litemall_category add constraint PK_litemall_category primary key (NOP_TENANT_ID, ID);

alter table litemall_collect drop constraint PK_litemall_collect;
alter table litemall_collect add constraint PK_litemall_collect primary key (NOP_TENANT_ID, ID);

alter table litemall_comment drop constraint PK_litemall_comment;
alter table litemall_comment add constraint PK_litemall_comment primary key (NOP_TENANT_ID, ID);

alter table litemall_coupon drop constraint PK_litemall_coupon;
alter table litemall_coupon add constraint PK_litemall_coupon primary key (NOP_TENANT_ID, ID);

alter table litemall_coupon_user drop constraint PK_litemall_coupon_user;
alter table litemall_coupon_user add constraint PK_litemall_coupon_user primary key (NOP_TENANT_ID, ID);

alter table litemall_feedback drop constraint PK_litemall_feedback;
alter table litemall_feedback add constraint PK_litemall_feedback primary key (NOP_TENANT_ID, ID);

alter table litemall_footprint drop constraint PK_litemall_footprint;
alter table litemall_footprint add constraint PK_litemall_footprint primary key (NOP_TENANT_ID, ID);

alter table litemall_goods drop constraint PK_litemall_goods;
alter table litemall_goods add constraint PK_litemall_goods primary key (NOP_TENANT_ID, ID);

alter table litemall_goods_attribute drop constraint PK_litemall_goods_attribute;
alter table litemall_goods_attribute add constraint PK_litemall_goods_attribute primary key (NOP_TENANT_ID, ID);

alter table litemall_goods_product drop constraint PK_litemall_goods_product;
alter table litemall_goods_product add constraint PK_litemall_goods_product primary key (NOP_TENANT_ID, ID);

alter table litemall_goods_specification drop constraint PK_litemall_goods_specification;
alter table litemall_goods_specification add constraint PK_litemall_goods_specification primary key (NOP_TENANT_ID, ID);

alter table litemall_groupon drop constraint PK_litemall_groupon;
alter table litemall_groupon add constraint PK_litemall_groupon primary key (NOP_TENANT_ID, ID);

alter table litemall_groupon_rules drop constraint PK_litemall_groupon_rules;
alter table litemall_groupon_rules add constraint PK_litemall_groupon_rules primary key (NOP_TENANT_ID, ID);

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

alter table litemall_order drop constraint PK_litemall_order;
alter table litemall_order add constraint PK_litemall_order primary key (NOP_TENANT_ID, ID);

alter table litemall_order_goods drop constraint PK_litemall_order_goods;
alter table litemall_order_goods add constraint PK_litemall_order_goods primary key (NOP_TENANT_ID, ID);

alter table litemall_permission drop constraint PK_litemall_permission;
alter table litemall_permission add constraint PK_litemall_permission primary key (NOP_TENANT_ID, ID);

alter table litemall_region drop constraint PK_litemall_region;
alter table litemall_region add constraint PK_litemall_region primary key (NOP_TENANT_ID, ID);

alter table litemall_role drop constraint PK_litemall_role;
alter table litemall_role add constraint PK_litemall_role primary key (NOP_TENANT_ID, ID);

alter table litemall_search_history drop constraint PK_litemall_search_history;
alter table litemall_search_history add constraint PK_litemall_search_history primary key (NOP_TENANT_ID, ID);

alter table litemall_storage drop constraint PK_litemall_storage;
alter table litemall_storage add constraint PK_litemall_storage primary key (NOP_TENANT_ID, ID);

alter table litemall_system drop constraint PK_litemall_system;
alter table litemall_system add constraint PK_litemall_system primary key (NOP_TENANT_ID, ID);

alter table litemall_topic drop constraint PK_litemall_topic;
alter table litemall_topic add constraint PK_litemall_topic primary key (NOP_TENANT_ID, ID);

alter table litemall_user drop constraint PK_litemall_user;
alter table litemall_user add constraint PK_litemall_user primary key (NOP_TENANT_ID, ID);

alter table litemall_user_role drop constraint PK_litemall_user_role;
alter table litemall_user_role add constraint PK_litemall_user_role primary key (NOP_TENANT_ID, USER_ID,ROLE_ID);


