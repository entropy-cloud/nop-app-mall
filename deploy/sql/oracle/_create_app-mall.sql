
CREATE TABLE litemall_ad(
  ID INTEGER NOT NULL ,
  NAME VARCHAR2(63) NOT NULL ,
  LINK VARCHAR2(255) NOT NULL ,
  URL VARCHAR2(255) NOT NULL ,
  POSITION INTEGER  ,
  CONTENT VARCHAR2(255)  ,
  START_TIME DATE  ,
  END_TIME DATE  ,
  ENABLED CHAR(1)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_ad primary key (ID)
);

CREATE TABLE litemall_address(
  ID INTEGER NOT NULL ,
  NAME VARCHAR2(63) NOT NULL ,
  USER_ID VARCHAR2(50) NOT NULL ,
  PROVINCE VARCHAR2(63) NOT NULL ,
  CITY VARCHAR2(63) NOT NULL ,
  COUNTY VARCHAR2(63) NOT NULL ,
  ADDRESS_DETAIL VARCHAR2(127) NOT NULL ,
  AREA_CODE CHAR(6)  ,
  POSTAL_CODE CHAR(6)  ,
  TEL VARCHAR2(20) NOT NULL ,
  IS_DEFAULT CHAR(1) NOT NULL ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_address primary key (ID)
);

CREATE TABLE litemall_brand(
  ID INTEGER NOT NULL ,
  NAME VARCHAR2(255) NOT NULL ,
  PIC_URL VARCHAR2(255) NOT NULL ,
  "DESC" VARCHAR2(255) NOT NULL ,
  SORT_ORDER INTEGER  ,
  FLOOR_PRICE NUMBER(10,2)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_brand primary key (ID)
);

CREATE TABLE litemall_category(
  ID INTEGER NOT NULL ,
  NAME VARCHAR2(63) NOT NULL ,
  ICON_URL VARCHAR2(255)  ,
  PIC_URL VARCHAR2(255)  ,
  KEYWORDS VARCHAR2(1023)  ,
  "DESC" VARCHAR2(255)  ,
  "LEVEL" VARCHAR2(255) NOT NULL ,
  PID INTEGER  ,
  SORT_ORDER INTEGER  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_category primary key (ID)
);

CREATE TABLE litemall_collect(
  ID INTEGER NOT NULL ,
  USER_ID VARCHAR2(50) NOT NULL ,
  VALUE_ID INTEGER NOT NULL ,
  TYPE INTEGER NOT NULL ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_collect primary key (ID)
);

CREATE TABLE litemall_topic(
  ID INTEGER NOT NULL ,
  TITLE VARCHAR2(255) NOT NULL ,
  SUBTITLE VARCHAR2(255)  ,
  CONTENT CLOB  ,
  PRICE NUMBER(10,2)  ,
  READ_COUNT INTEGER  ,
  PIC_URL VARCHAR2(255)  ,
  SORT_ORDER INTEGER  ,
  GOODS VARCHAR2(1023)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  STATUS INTEGER default 0   ,
  constraint PK_litemall_topic primary key (ID)
);

CREATE TABLE litemall_coupon(
  ID INTEGER NOT NULL ,
  NAME VARCHAR2(63) NOT NULL ,
  "DESC" VARCHAR2(127)  ,
  TAG VARCHAR2(63)  ,
  TOTAL INTEGER NOT NULL ,
  DISCOUNT NUMBER(10,2)  ,
  MIN NUMBER(10,2)  ,
  LIMIT INTEGER  ,
  TYPE INTEGER  ,
  STATUS INTEGER  ,
  GOODS_TYPE INTEGER  ,
  GOODS_VALUE VARCHAR2(1023)  ,
  CODE VARCHAR2(63)  ,
  TIME_TYPE INTEGER  ,
  DAYS INTEGER  ,
  START_TIME DATE  ,
  END_TIME DATE  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_coupon primary key (ID)
);

CREATE TABLE litemall_feedback(
  ID INTEGER NOT NULL ,
  USER_ID VARCHAR2(50) NOT NULL ,
  USERNAME VARCHAR2(63) NOT NULL ,
  MOBILE VARCHAR2(20) NOT NULL ,
  FEED_TYPE VARCHAR2(63) NOT NULL ,
  CONTENT VARCHAR2(1023) NOT NULL ,
  STATUS INTEGER NOT NULL ,
  HAS_PICTURE CHAR(1)  ,
  PIC_URLS VARCHAR2(1023)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_feedback primary key (ID)
);

CREATE TABLE litemall_issue(
  ID INTEGER NOT NULL ,
  QUESTION VARCHAR2(255)  ,
  ANSWER VARCHAR2(255)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_issue primary key (ID)
);

CREATE TABLE litemall_keyword(
  ID INTEGER NOT NULL ,
  KEYWORD VARCHAR2(127) NOT NULL ,
  URL VARCHAR2(255) NOT NULL ,
  IS_HOT CHAR(1) NOT NULL ,
  IS_DEFAULT CHAR(1) NOT NULL ,
  SORT_ORDER INTEGER NOT NULL ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_keyword primary key (ID)
);

CREATE TABLE litemall_log(
  ID INTEGER NOT NULL ,
  ADMIN VARCHAR2(45)  ,
  IP VARCHAR2(45)  ,
  TYPE INTEGER  ,
  ACTION VARCHAR2(45)  ,
  STATUS CHAR(1)  ,
  RESULT VARCHAR2(127)  ,
  "COMMENT" VARCHAR2(255)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_log primary key (ID)
);

CREATE TABLE litemall_notice(
  ID INTEGER NOT NULL ,
  TITLE VARCHAR2(63)  ,
  CONTENT VARCHAR2(511)  ,
  ADMIN_ID INTEGER  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_notice primary key (ID)
);

CREATE TABLE litemall_notice_admin(
  ID INTEGER NOT NULL ,
  NOTICE_ID INTEGER  ,
  NOTICE_TITLE VARCHAR2(63)  ,
  ADMIN_ID INTEGER  ,
  READ_TIME DATE  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_notice_admin primary key (ID)
);

CREATE TABLE litemall_pickup_store(
  ID INTEGER NOT NULL ,
  NAME VARCHAR2(127) NOT NULL ,
  ADDRESS VARCHAR2(255) NOT NULL ,
  CONTACT VARCHAR2(50) NOT NULL ,
  PHONE VARCHAR2(20) NOT NULL ,
  LATITUDE NUMBER(10,6)  ,
  LONGITUDE NUMBER(10,6)  ,
  OPENING_HOURS VARCHAR2(100)  ,
  STATUS INTEGER  ,
  REMARK VARCHAR2(255)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_pickup_store primary key (ID)
);

CREATE TABLE litemall_region(
  ID INTEGER NOT NULL ,
  PID INTEGER NOT NULL ,
  NAME VARCHAR2(120) NOT NULL ,
  TYPE INTEGER NOT NULL ,
  CODE INTEGER NOT NULL ,
  constraint PK_litemall_region primary key (ID)
);

CREATE TABLE litemall_search_history(
  ID INTEGER NOT NULL ,
  USER_ID VARCHAR2(50) NOT NULL ,
  KEYWORD VARCHAR2(63) NOT NULL ,
  "FROM" VARCHAR2(63) NOT NULL ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_search_history primary key (ID)
);

CREATE TABLE litemall_storage(
  ID INTEGER NOT NULL ,
  KEY VARCHAR2(63) NOT NULL ,
  NAME VARCHAR2(255) NOT NULL ,
  TYPE VARCHAR2(20) NOT NULL ,
  "SIZE" INTEGER NOT NULL ,
  URL VARCHAR2(255)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_storage primary key (ID)
);

CREATE TABLE litemall_system(
  ID INTEGER NOT NULL ,
  KEY_NAME VARCHAR2(255) NOT NULL ,
  KEY_VALUE VARCHAR2(255) NOT NULL ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_system primary key (ID)
);

CREATE TABLE litemall_reset_code(
  ID VARCHAR2(50)  ,
  MOBILE VARCHAR2(50)  ,
  CODE VARCHAR2(10)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_reset_code primary key (ID)
);

CREATE TABLE litemall_promotion_activity(
  ID INTEGER NOT NULL ,
  NAME VARCHAR2(63) NOT NULL ,
  DISCOUNT_TYPE INTEGER  ,
  START_TIME DATE  ,
  END_TIME DATE  ,
  STATUS INTEGER  ,
  GOODS_SCOPE INTEGER  ,
  GOODS_SCOPE_VALUE VARCHAR2(1023)  ,
  PRIORITY INTEGER  ,
  MAX_PER_USER INTEGER  ,
  REMARK VARCHAR2(255)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_promotion_activity primary key (ID)
);

CREATE TABLE litemall_check_in_rule(
  ID INTEGER NOT NULL ,
  DAY_SEQ INTEGER NOT NULL ,
  POINT_REWARD INTEGER  ,
  RESET_CYCLE INTEGER  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  constraint PK_litemall_check_in_rule primary key (ID)
);

CREATE TABLE litemall_check_in_record(
  ID INTEGER NOT NULL ,
  USER_ID INTEGER NOT NULL ,
  CHECK_IN_DATE DATE NOT NULL ,
  CONSECUTIVE_DAYS INTEGER  ,
  POINTS_EARNED INTEGER  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_check_in_record primary key (ID)
);

CREATE TABLE litemall_points_account(
  ID INTEGER NOT NULL ,
  USER_ID INTEGER NOT NULL ,
  BALANCE INTEGER  ,
  TOTAL_EARNED INTEGER  ,
  TOTAL_SPENT INTEGER  ,
  VERSION INTEGER NOT NULL ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  constraint PK_litemall_points_account primary key (ID)
);

CREATE TABLE litemall_wallet(
  ID INTEGER NOT NULL ,
  USER_ID INTEGER NOT NULL ,
  BALANCE NUMBER(10,2)  ,
  TOTAL_RECHARGE NUMBER(10,2)  ,
  TOTAL_SPENT NUMBER(10,2)  ,
  VERSION INTEGER NOT NULL ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  constraint PK_litemall_wallet primary key (ID)
);

CREATE TABLE litemall_user_message(
  ID INTEGER NOT NULL ,
  USER_ID INTEGER NOT NULL ,
  MSG_TYPE INTEGER NOT NULL ,
  TITLE VARCHAR2(127) NOT NULL ,
  CONTENT VARCHAR2(1023)  ,
  IS_READ CHAR(1)  ,
  READ_TIME DATE  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_user_message primary key (ID)
);

CREATE TABLE litemall_material_category(
  ID INTEGER NOT NULL ,
  NAME VARCHAR2(127) NOT NULL ,
  PARENT_ID INTEGER  ,
  SORT_ORDER INTEGER  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_material_category primary key (ID)
);

CREATE TABLE litemall_goods(
  ID INTEGER NOT NULL ,
  GOODS_SN VARCHAR2(63) NOT NULL ,
  NAME VARCHAR2(127) NOT NULL ,
  CATEGORY_ID INTEGER  ,
  BRAND_ID INTEGER  ,
  GALLERY VARCHAR2(1023)  ,
  KEYWORDS VARCHAR2(255)  ,
  BRIEF VARCHAR2(255)  ,
  IS_ON_SALE CHAR(1)  ,
  SORT_ORDER INTEGER  ,
  PIC_URL VARCHAR2(255)  ,
  SHARE_URL VARCHAR2(255)  ,
  IS_NEW CHAR(1)  ,
  IS_HOT CHAR(1)  ,
  UNIT VARCHAR2(31)  ,
  COUNTER_PRICE NUMBER(10,2)  ,
  RETAIL_PRICE NUMBER(10,2)  ,
  DETAIL CLOB  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  IS_RECOMMEND CHAR(1)  ,
  VIDEO_URL VARCHAR2(255)  ,
  constraint PK_litemall_goods primary key (ID)
);

CREATE TABLE litemall_coupon_user(
  ID INTEGER NOT NULL ,
  USER_ID VARCHAR2(50) NOT NULL ,
  COUPON_ID INTEGER NOT NULL ,
  STATUS INTEGER  ,
  USED_TIME DATE  ,
  START_TIME DATE  ,
  END_TIME DATE  ,
  ORDER_ID INTEGER  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_coupon_user primary key (ID)
);

CREATE TABLE litemall_order(
  ID INTEGER NOT NULL ,
  USER_ID VARCHAR2(50) NOT NULL ,
  ORDER_SN VARCHAR2(63) NOT NULL ,
  ORDER_STATUS INTEGER NOT NULL ,
  AFTERSALE_STATUS INTEGER  ,
  CONSIGNEE VARCHAR2(63) NOT NULL ,
  MOBILE VARCHAR2(63) NOT NULL ,
  ADDRESS VARCHAR2(127) NOT NULL ,
  MESSAGE VARCHAR2(512) NOT NULL ,
  GOODS_PRICE NUMBER(10,2) NOT NULL ,
  FREIGHT_PRICE NUMBER(10,2) NOT NULL ,
  COUPON_PRICE NUMBER(10,2) NOT NULL ,
  INTEGRAL_PRICE NUMBER(10,2) NOT NULL ,
  GROUPON_PRICE NUMBER(10,2) NOT NULL ,
  ORDER_PRICE NUMBER(10,2) NOT NULL ,
  ACTUAL_PRICE NUMBER(10,2) NOT NULL ,
  PAY_ID VARCHAR2(63)  ,
  PAY_TIME DATE  ,
  SHIP_SN VARCHAR2(63)  ,
  SHIP_CHANNEL VARCHAR2(63)  ,
  SHIP_TIME DATE  ,
  REFUND_AMOUNT NUMBER(10,2)  ,
  REFUND_TYPE VARCHAR2(63)  ,
  REFUND_CONTENT VARCHAR2(127)  ,
  REFUND_TIME DATE  ,
  CONFIRM_TIME DATE  ,
  COMMENTS INTEGER  ,
  END_TIME DATE  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  DELIVERY_TYPE INTEGER  ,
  PICKUP_STORE_ID INTEGER  ,
  PICKUP_CODE VARCHAR2(50)  ,
  PICKUP_TIME DATE  ,
  PROMOTION_PRICE NUMBER(10,2) NOT NULL ,
  PIN_TUAN_PRICE NUMBER(10,2) NOT NULL ,
  PAY_CHANNEL INTEGER  ,
  WALLET_PAY_AMOUNT NUMBER(10,2)  ,
  ADMIN_REMARK VARCHAR2(511)  ,
  constraint PK_litemall_order primary key (ID)
);

CREATE TABLE litemall_promotion_tier(
  ID INTEGER NOT NULL ,
  ACTIVITY_ID INTEGER NOT NULL ,
  MEET_AMOUNT NUMBER(10,2) NOT NULL ,
  DISCOUNT_VALUE NUMBER(10,2) NOT NULL ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_promotion_tier primary key (ID)
);

CREATE TABLE litemall_points_flow(
  ID INTEGER NOT NULL ,
  ACCOUNT_ID INTEGER NOT NULL ,
  USER_ID INTEGER NOT NULL ,
  CHANGE_TYPE INTEGER NOT NULL ,
  CHANGE_AMOUNT INTEGER NOT NULL ,
  BALANCE_AFTER INTEGER NOT NULL ,
  SOURCE_TYPE VARCHAR2(50)  ,
  SOURCE_ID VARCHAR2(50)  ,
  REMARK VARCHAR2(255)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  constraint PK_litemall_points_flow primary key (ID)
);

CREATE TABLE litemall_wallet_flow(
  ID INTEGER NOT NULL ,
  WALLET_ID INTEGER NOT NULL ,
  CHANGE_TYPE INTEGER NOT NULL ,
  CHANGE_AMOUNT NUMBER(10,2) NOT NULL ,
  BALANCE_AFTER NUMBER(10,2)  ,
  SOURCE_TYPE VARCHAR2(50)  ,
  SOURCE_ID VARCHAR2(50)  ,
  REMARK VARCHAR2(255)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  constraint PK_litemall_wallet_flow primary key (ID)
);

CREATE TABLE litemall_recharge(
  ID INTEGER NOT NULL ,
  USER_ID INTEGER NOT NULL ,
  WALLET_ID INTEGER NOT NULL ,
  AMOUNT NUMBER(10,2) NOT NULL ,
  GIFT_AMOUNT NUMBER(10,2)  ,
  PAY_CHANNEL INTEGER  ,
  PAY_STATUS INTEGER  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_recharge primary key (ID)
);

CREATE TABLE litemall_material(
  ID INTEGER NOT NULL ,
  NAME VARCHAR2(255) NOT NULL ,
  URL VARCHAR2(255) NOT NULL ,
  FILE_TYPE VARCHAR2(50)  ,
  FILE_SIZE INTEGER  ,
  CATEGORY_ID INTEGER  ,
  TAG VARCHAR2(255)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_material primary key (ID)
);

CREATE TABLE litemall_cart(
  ID INTEGER NOT NULL ,
  USER_ID VARCHAR2(50)  ,
  GOODS_ID INTEGER  ,
  GOODS_SN VARCHAR2(63)  ,
  GOODS_NAME VARCHAR2(127)  ,
  PRODUCT_ID INTEGER  ,
  PRICE NUMBER(10,2)  ,
  "NUMBER" INTEGER  ,
  SPECIFICATIONS VARCHAR2(1023)  ,
  CHECKED CHAR(1)  ,
  PIC_URL VARCHAR2(255)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_cart primary key (ID)
);

CREATE TABLE litemall_comment(
  ID INTEGER NOT NULL ,
  VALUE_ID INTEGER NOT NULL ,
  TYPE INTEGER NOT NULL ,
  CONTENT VARCHAR2(1023)  ,
  ADMIN_CONTENT VARCHAR2(511)  ,
  USER_ID VARCHAR2(50) NOT NULL ,
  HAS_PICTURE CHAR(1)  ,
  PIC_URLS VARCHAR2(1023)  ,
  STAR INTEGER  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  PROS VARCHAR2(1023)  ,
  CONS VARCHAR2(1023)  ,
  SEMANTIC_RATING INTEGER  ,
  constraint PK_litemall_comment primary key (ID)
);

CREATE TABLE litemall_footprint(
  ID INTEGER NOT NULL ,
  USER_ID VARCHAR2(50) NOT NULL ,
  GOODS_ID INTEGER NOT NULL ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_footprint primary key (ID)
);

CREATE TABLE litemall_goods_attribute(
  ID INTEGER NOT NULL ,
  GOODS_ID INTEGER NOT NULL ,
  ATTRIBUTE VARCHAR2(255) NOT NULL ,
  VALUE VARCHAR2(255) NOT NULL ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_goods_attribute primary key (ID)
);

CREATE TABLE litemall_goods_product(
  ID INTEGER NOT NULL ,
  GOODS_ID INTEGER NOT NULL ,
  SPECIFICATIONS VARCHAR2(1023) NOT NULL ,
  PRICE NUMBER(10,2) NOT NULL ,
  "NUMBER" INTEGER NOT NULL ,
  URL VARCHAR2(255)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  SAFE_STOCK INTEGER  ,
  constraint PK_litemall_goods_product primary key (ID)
);

CREATE TABLE litemall_goods_specification(
  ID INTEGER NOT NULL ,
  GOODS_ID INTEGER NOT NULL ,
  SPECIFICATION VARCHAR2(255) NOT NULL ,
  VALUE VARCHAR2(255) NOT NULL ,
  PIC_URL VARCHAR2(255) NOT NULL ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_goods_specification primary key (ID)
);

CREATE TABLE litemall_groupon_rules(
  ID INTEGER NOT NULL ,
  GOODS_ID INTEGER NOT NULL ,
  GOODS_NAME VARCHAR2(127) NOT NULL ,
  PIC_URL VARCHAR2(255)  ,
  DISCOUNT NUMBER(10,2) NOT NULL ,
  DISCOUNT_MEMBER INTEGER NOT NULL ,
  EXPIRE_TIME DATE  ,
  STATUS INTEGER  ,
  ADD_TIME DATE NOT NULL ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_groupon_rules primary key (ID)
);

CREATE TABLE litemall_time_discount(
  ID INTEGER NOT NULL ,
  GOODS_ID INTEGER NOT NULL ,
  PRODUCT_ID INTEGER  ,
  DISCOUNT_TYPE INTEGER  ,
  DISCOUNT_VALUE NUMBER(10,2)  ,
  START_TIME DATE  ,
  END_TIME DATE  ,
  STATUS INTEGER  ,
  STOCK_LIMIT INTEGER  ,
  REMARK VARCHAR2(255)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_time_discount primary key (ID)
);

CREATE TABLE litemall_flash_sale(
  ID INTEGER NOT NULL ,
  GOODS_ID INTEGER NOT NULL ,
  PRODUCT_ID INTEGER  ,
  FLASH_PRICE NUMBER(10,2) NOT NULL ,
  TOTAL_STOCK INTEGER  ,
  MAX_PER_USER INTEGER  ,
  MAX_PER_ORDER INTEGER  ,
  STATUS INTEGER  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_flash_sale primary key (ID)
);

CREATE TABLE litemall_pin_tuan_activity(
  ID INTEGER NOT NULL ,
  GOODS_ID INTEGER NOT NULL ,
  PRODUCT_ID INTEGER  ,
  PIN_TUAN_PRICE NUMBER(10,2) NOT NULL ,
  MIN_USER_COUNT INTEGER NOT NULL ,
  MAX_USER_COUNT INTEGER  ,
  EXPIRE_HOURS INTEGER  ,
  STATUS INTEGER  ,
  REMARK VARCHAR2(255)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_pin_tuan_activity primary key (ID)
);

CREATE TABLE litemall_aftersale(
  ID INTEGER NOT NULL ,
  AFTERSALE_SN VARCHAR2(63)  ,
  ORDER_ID INTEGER NOT NULL ,
  USER_ID VARCHAR2(50) NOT NULL ,
  TYPE INTEGER  ,
  REASON VARCHAR2(31)  ,
  AMOUNT NUMBER(10,2)  ,
  PICTURES VARCHAR2(1023)  ,
  "COMMENT" VARCHAR2(511)  ,
  STATUS INTEGER  ,
  HANDLE_TIME DATE  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  ORDER_ITEM_ID INTEGER  ,
  PROCESS_NOTE VARCHAR2(511)  ,
  PROCESS_TIME DATE  ,
  constraint PK_litemall_aftersale primary key (ID)
);

CREATE TABLE litemall_groupon(
  ID INTEGER NOT NULL ,
  ORDER_ID INTEGER NOT NULL ,
  GROUPON_ID INTEGER  ,
  RULES_ID INTEGER NOT NULL ,
  USER_ID VARCHAR2(50) NOT NULL ,
  SHARE_URL VARCHAR2(255)  ,
  CREATOR_USER_ID VARCHAR2(50) NOT NULL ,
  CREATOR_USER_TIME DATE  ,
  STATUS INTEGER  ,
  ADD_TIME DATE NOT NULL ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_groupon primary key (ID)
);

CREATE TABLE litemall_order_goods(
  ID INTEGER NOT NULL ,
  ORDER_ID INTEGER NOT NULL ,
  GOODS_ID INTEGER NOT NULL ,
  GOODS_NAME VARCHAR2(127) NOT NULL ,
  GOODS_SN VARCHAR2(63) NOT NULL ,
  PRODUCT_ID INTEGER NOT NULL ,
  "NUMBER" INTEGER NOT NULL ,
  PRICE NUMBER(10,2) NOT NULL ,
  SPECIFICATIONS VARCHAR2(1023) NOT NULL ,
  PIC_URL VARCHAR2(255) NOT NULL ,
  "COMMENT" INTEGER  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  ACTUAL_PAY_AMOUNT NUMBER(10,2)  ,
  constraint PK_litemall_order_goods primary key (ID)
);

CREATE TABLE litemall_flash_sale_session(
  ID INTEGER NOT NULL ,
  FLASH_SALE_ID INTEGER NOT NULL ,
  SESSION_START DATE NOT NULL ,
  SESSION_END DATE NOT NULL ,
  SESSION_STOCK INTEGER  ,
  SESSION_STATUS INTEGER  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_flash_sale_session primary key (ID)
);

CREATE TABLE litemall_pin_tuan_group(
  ID INTEGER NOT NULL ,
  ACTIVITY_ID INTEGER NOT NULL ,
  CREATOR_USER_ID INTEGER NOT NULL ,
  ORDER_ID INTEGER  ,
  STATUS INTEGER  ,
  EXPIRE_TIME DATE  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_pin_tuan_group primary key (ID)
);

CREATE TABLE litemall_pin_tuan_member(
  ID INTEGER NOT NULL ,
  GROUP_ID INTEGER NOT NULL ,
  USER_ID INTEGER NOT NULL ,
  ORDER_ID INTEGER NOT NULL ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_pin_tuan_member primary key (ID)
);


      COMMENT ON TABLE litemall_ad IS '广告表';
                
      COMMENT ON COLUMN litemall_ad.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_ad.NAME IS '广告标题';
                    
      COMMENT ON COLUMN litemall_ad.LINK IS '所广告的商品页面或者活动页面链接地址';
                    
      COMMENT ON COLUMN litemall_ad.URL IS '广告宣传图片';
                    
      COMMENT ON COLUMN litemall_ad.POSITION IS '广告位置：1则是首页';
                    
      COMMENT ON COLUMN litemall_ad.CONTENT IS '活动内容';
                    
      COMMENT ON COLUMN litemall_ad.START_TIME IS '广告开始时间';
                    
      COMMENT ON COLUMN litemall_ad.END_TIME IS '广告结束时间';
                    
      COMMENT ON COLUMN litemall_ad.ENABLED IS '是否启动';
                    
      COMMENT ON COLUMN litemall_ad.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_ad.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_ad.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_address IS '收货地址表';
                
      COMMENT ON COLUMN litemall_address.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_address.NAME IS '收货人名称';
                    
      COMMENT ON COLUMN litemall_address.USER_ID IS '用户ID';
                    
      COMMENT ON COLUMN litemall_address.PROVINCE IS '行政区域表的省ID';
                    
      COMMENT ON COLUMN litemall_address.CITY IS '行政区域表的市ID';
                    
      COMMENT ON COLUMN litemall_address.COUNTY IS '行政区域表的区县ID';
                    
      COMMENT ON COLUMN litemall_address.ADDRESS_DETAIL IS '详细收货地址';
                    
      COMMENT ON COLUMN litemall_address.AREA_CODE IS '地区编码';
                    
      COMMENT ON COLUMN litemall_address.POSTAL_CODE IS '邮政编码';
                    
      COMMENT ON COLUMN litemall_address.TEL IS '手机号码';
                    
      COMMENT ON COLUMN litemall_address.IS_DEFAULT IS '是否默认地址';
                    
      COMMENT ON COLUMN litemall_address.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_address.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_address.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_brand IS '品牌商表';
                
      COMMENT ON COLUMN litemall_brand.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_brand.NAME IS '品牌商名称';
                    
      COMMENT ON COLUMN litemall_brand.PIC_URL IS '品牌商图片';
                    
      COMMENT ON COLUMN litemall_brand."DESC" IS '品牌商简介';
                    
      COMMENT ON COLUMN litemall_brand.SORT_ORDER IS '排序';
                    
      COMMENT ON COLUMN litemall_brand.FLOOR_PRICE IS '底价';
                    
      COMMENT ON COLUMN litemall_brand.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_brand.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_brand.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_category IS '类目表';
                
      COMMENT ON COLUMN litemall_category.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_category.NAME IS '类目名称';
                    
      COMMENT ON COLUMN litemall_category.ICON_URL IS '类目图标';
                    
      COMMENT ON COLUMN litemall_category.PIC_URL IS '类目图片';
                    
      COMMENT ON COLUMN litemall_category.KEYWORDS IS '类目关键字';
                    
      COMMENT ON COLUMN litemall_category."DESC" IS '简介';
                    
      COMMENT ON COLUMN litemall_category."LEVEL" IS '级别';
                    
      COMMENT ON COLUMN litemall_category.PID IS '父类目ID';
                    
      COMMENT ON COLUMN litemall_category.SORT_ORDER IS '排序';
                    
      COMMENT ON COLUMN litemall_category.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_category.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_category.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_collect IS '收藏表';
                
      COMMENT ON COLUMN litemall_collect.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_collect.USER_ID IS '用户ID';
                    
      COMMENT ON COLUMN litemall_collect.VALUE_ID IS '如果type=0，则是商品ID；如果type=1，则是专题ID';
                    
      COMMENT ON COLUMN litemall_collect.TYPE IS '收藏类型';
                    
      COMMENT ON COLUMN litemall_collect.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_collect.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_collect.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_topic IS '专题表';
                
      COMMENT ON COLUMN litemall_topic.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_topic.TITLE IS '专题标题';
                    
      COMMENT ON COLUMN litemall_topic.SUBTITLE IS '专题子标题';
                    
      COMMENT ON COLUMN litemall_topic.CONTENT IS '专题内容';
                    
      COMMENT ON COLUMN litemall_topic.PRICE IS '专题相关商品最低价';
                    
      COMMENT ON COLUMN litemall_topic.READ_COUNT IS '专题阅读量';
                    
      COMMENT ON COLUMN litemall_topic.PIC_URL IS '专题图片';
                    
      COMMENT ON COLUMN litemall_topic.SORT_ORDER IS '排序';
                    
      COMMENT ON COLUMN litemall_topic.GOODS IS '专题相关商品';
                    
      COMMENT ON COLUMN litemall_topic.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_topic.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_topic.DELETED IS '逻辑删除';
                    
      COMMENT ON COLUMN litemall_topic.STATUS IS '上下架状态';
                    
      COMMENT ON TABLE litemall_coupon IS '优惠券信息及规则表';
                
      COMMENT ON COLUMN litemall_coupon.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_coupon.NAME IS '优惠券名称';
                    
      COMMENT ON COLUMN litemall_coupon."DESC" IS '优惠券介绍';
                    
      COMMENT ON COLUMN litemall_coupon.TAG IS '优惠券标签';
                    
      COMMENT ON COLUMN litemall_coupon.TOTAL IS '优惠券数量';
                    
      COMMENT ON COLUMN litemall_coupon.DISCOUNT IS '优惠金额，';
                    
      COMMENT ON COLUMN litemall_coupon.MIN IS '最少消费金额';
                    
      COMMENT ON COLUMN litemall_coupon.LIMIT IS '用户领券限制数量';
                    
      COMMENT ON COLUMN litemall_coupon.TYPE IS '优惠券赠送类型';
                    
      COMMENT ON COLUMN litemall_coupon.STATUS IS '优惠券状态';
                    
      COMMENT ON COLUMN litemall_coupon.GOODS_TYPE IS '商品限制类型';
                    
      COMMENT ON COLUMN litemall_coupon.GOODS_VALUE IS '商品限制值';
                    
      COMMENT ON COLUMN litemall_coupon.CODE IS '优惠券兑换码';
                    
      COMMENT ON COLUMN litemall_coupon.TIME_TYPE IS '有效时间限制';
                    
      COMMENT ON COLUMN litemall_coupon.DAYS IS '基于领取时间的有效天数days。';
                    
      COMMENT ON COLUMN litemall_coupon.START_TIME IS '使用券开始时间';
                    
      COMMENT ON COLUMN litemall_coupon.END_TIME IS '使用券截至时间';
                    
      COMMENT ON COLUMN litemall_coupon.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_coupon.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_coupon.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_feedback IS '意见反馈表';
                
      COMMENT ON COLUMN litemall_feedback.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_feedback.USER_ID IS '用户ID';
                    
      COMMENT ON COLUMN litemall_feedback.USERNAME IS '用户名称';
                    
      COMMENT ON COLUMN litemall_feedback.MOBILE IS '手机号';
                    
      COMMENT ON COLUMN litemall_feedback.FEED_TYPE IS '反馈类型';
                    
      COMMENT ON COLUMN litemall_feedback.CONTENT IS '反馈内容';
                    
      COMMENT ON COLUMN litemall_feedback.STATUS IS '状态';
                    
      COMMENT ON COLUMN litemall_feedback.HAS_PICTURE IS '是否含有图片';
                    
      COMMENT ON COLUMN litemall_feedback.PIC_URLS IS '图片地址列表，采用JSON数组格式';
                    
      COMMENT ON COLUMN litemall_feedback.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_feedback.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_feedback.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_issue IS '常见问题表';
                
      COMMENT ON COLUMN litemall_issue.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_issue.QUESTION IS '问题标题';
                    
      COMMENT ON COLUMN litemall_issue.ANSWER IS '问题答案';
                    
      COMMENT ON COLUMN litemall_issue.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_issue.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_issue.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_keyword IS '关键字表';
                
      COMMENT ON COLUMN litemall_keyword.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_keyword.KEYWORD IS '关键字';
                    
      COMMENT ON COLUMN litemall_keyword.URL IS '关键字的跳转链接';
                    
      COMMENT ON COLUMN litemall_keyword.IS_HOT IS '是否是热门关键字';
                    
      COMMENT ON COLUMN litemall_keyword.IS_DEFAULT IS '是否是默认关键字';
                    
      COMMENT ON COLUMN litemall_keyword.SORT_ORDER IS '排序';
                    
      COMMENT ON COLUMN litemall_keyword.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_keyword.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_keyword.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_log IS '操作日志表';
                
      COMMENT ON COLUMN litemall_log.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_log.ADMIN IS '管理员';
                    
      COMMENT ON COLUMN litemall_log.IP IS '管理员地址';
                    
      COMMENT ON COLUMN litemall_log.TYPE IS '操作分类';
                    
      COMMENT ON COLUMN litemall_log.ACTION IS '操作动作';
                    
      COMMENT ON COLUMN litemall_log.STATUS IS '操作状态';
                    
      COMMENT ON COLUMN litemall_log.RESULT IS '操作结果/消息';
                    
      COMMENT ON COLUMN litemall_log."COMMENT" IS '补充信息';
                    
      COMMENT ON COLUMN litemall_log.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_log.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_log.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_notice IS '通知表';
                
      COMMENT ON COLUMN litemall_notice.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_notice.TITLE IS '通知标题';
                    
      COMMENT ON COLUMN litemall_notice.CONTENT IS '通知内容';
                    
      COMMENT ON COLUMN litemall_notice.ADMIN_ID IS '创建通知的管理员ID';
                    
      COMMENT ON COLUMN litemall_notice.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_notice.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_notice.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_notice_admin IS '通知管理员表';
                
      COMMENT ON COLUMN litemall_notice_admin.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_notice_admin.NOTICE_ID IS '通知ID';
                    
      COMMENT ON COLUMN litemall_notice_admin.NOTICE_TITLE IS '通知标题';
                    
      COMMENT ON COLUMN litemall_notice_admin.ADMIN_ID IS '管理员ID';
                    
      COMMENT ON COLUMN litemall_notice_admin.READ_TIME IS '阅读时间';
                    
      COMMENT ON COLUMN litemall_notice_admin.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_notice_admin.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_notice_admin.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_pickup_store IS '自提门店表';
                
      COMMENT ON COLUMN litemall_pickup_store.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_pickup_store.NAME IS '门店名称';
                    
      COMMENT ON COLUMN litemall_pickup_store.ADDRESS IS '门店地址';
                    
      COMMENT ON COLUMN litemall_pickup_store.CONTACT IS '联系人';
                    
      COMMENT ON COLUMN litemall_pickup_store.PHONE IS '联系电话';
                    
      COMMENT ON COLUMN litemall_pickup_store.LATITUDE IS '纬度';
                    
      COMMENT ON COLUMN litemall_pickup_store.LONGITUDE IS '经度';
                    
      COMMENT ON COLUMN litemall_pickup_store.OPENING_HOURS IS '营业时间';
                    
      COMMENT ON COLUMN litemall_pickup_store.STATUS IS '启用状态';
                    
      COMMENT ON COLUMN litemall_pickup_store.REMARK IS '备注';
                    
      COMMENT ON COLUMN litemall_pickup_store.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_pickup_store.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_pickup_store.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_region IS '行政区域表';
                
      COMMENT ON COLUMN litemall_region.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_region.PID IS '行政区域父ID';
                    
      COMMENT ON COLUMN litemall_region.NAME IS '行政区域名称';
                    
      COMMENT ON COLUMN litemall_region.TYPE IS '行政区域类型';
                    
      COMMENT ON COLUMN litemall_region.CODE IS '行政区域编码';
                    
      COMMENT ON TABLE litemall_search_history IS '搜索历史表';
                
      COMMENT ON COLUMN litemall_search_history.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_search_history.USER_ID IS '用户ID';
                    
      COMMENT ON COLUMN litemall_search_history.KEYWORD IS '搜索关键字';
                    
      COMMENT ON COLUMN litemall_search_history."FROM" IS '搜索来源';
                    
      COMMENT ON COLUMN litemall_search_history.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_search_history.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_search_history.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_storage IS '文件存储表';
                
      COMMENT ON COLUMN litemall_storage.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_storage.KEY IS '文件的唯一索引';
                    
      COMMENT ON COLUMN litemall_storage.NAME IS '文件名';
                    
      COMMENT ON COLUMN litemall_storage.TYPE IS '文件类型';
                    
      COMMENT ON COLUMN litemall_storage."SIZE" IS '文件大小';
                    
      COMMENT ON COLUMN litemall_storage.URL IS '文件访问链接';
                    
      COMMENT ON COLUMN litemall_storage.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_storage.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_storage.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_system IS '系统配置表';
                
      COMMENT ON COLUMN litemall_system.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_system.KEY_NAME IS '系统配置名';
                    
      COMMENT ON COLUMN litemall_system.KEY_VALUE IS '系统配置值';
                    
      COMMENT ON COLUMN litemall_system.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_system.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_system.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_reset_code IS '密码重置验证码';
                
      COMMENT ON COLUMN litemall_reset_code.MOBILE IS '手机号';
                    
      COMMENT ON COLUMN litemall_reset_code.CODE IS '验证码';
                    
      COMMENT ON COLUMN litemall_reset_code.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_reset_code.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_reset_code.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_promotion_activity IS '促销活动表（满减送）';
                
      COMMENT ON COLUMN litemall_promotion_activity.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_promotion_activity.NAME IS '活动名称';
                    
      COMMENT ON COLUMN litemall_promotion_activity.DISCOUNT_TYPE IS '折扣类型';
                    
      COMMENT ON COLUMN litemall_promotion_activity.START_TIME IS '开始时间';
                    
      COMMENT ON COLUMN litemall_promotion_activity.END_TIME IS '结束时间';
                    
      COMMENT ON COLUMN litemall_promotion_activity.STATUS IS '活动状态';
                    
      COMMENT ON COLUMN litemall_promotion_activity.GOODS_SCOPE IS '商品范围类型';
                    
      COMMENT ON COLUMN litemall_promotion_activity.GOODS_SCOPE_VALUE IS '商品范围值(JSON)';
                    
      COMMENT ON COLUMN litemall_promotion_activity.PRIORITY IS '优先级';
                    
      COMMENT ON COLUMN litemall_promotion_activity.MAX_PER_USER IS '每人限参与次数(0不限)';
                    
      COMMENT ON COLUMN litemall_promotion_activity.REMARK IS '备注';
                    
      COMMENT ON COLUMN litemall_promotion_activity.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_promotion_activity.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_promotion_activity.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_check_in_rule IS '签到规则表';
                
      COMMENT ON COLUMN litemall_check_in_rule.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_check_in_rule.DAY_SEQ IS '连续第N天';
                    
      COMMENT ON COLUMN litemall_check_in_rule.POINT_REWARD IS '奖励积分数';
                    
      COMMENT ON COLUMN litemall_check_in_rule.RESET_CYCLE IS '重置周期（天数，0不重置）';
                    
      COMMENT ON COLUMN litemall_check_in_rule.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_check_in_rule.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON TABLE litemall_check_in_record IS '签到记录表';
                
      COMMENT ON COLUMN litemall_check_in_record.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_check_in_record.USER_ID IS '用户ID';
                    
      COMMENT ON COLUMN litemall_check_in_record.CHECK_IN_DATE IS '签到日期';
                    
      COMMENT ON COLUMN litemall_check_in_record.CONSECUTIVE_DAYS IS '连续签到天数';
                    
      COMMENT ON COLUMN litemall_check_in_record.POINTS_EARNED IS '获得积分';
                    
      COMMENT ON COLUMN litemall_check_in_record.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_check_in_record.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_check_in_record.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_points_account IS '积分账户表';
                
      COMMENT ON COLUMN litemall_points_account.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_points_account.USER_ID IS '用户ID';
                    
      COMMENT ON COLUMN litemall_points_account.BALANCE IS '当前可用积分';
                    
      COMMENT ON COLUMN litemall_points_account.TOTAL_EARNED IS '累计获得';
                    
      COMMENT ON COLUMN litemall_points_account.TOTAL_SPENT IS '累计消耗';
                    
      COMMENT ON COLUMN litemall_points_account.VERSION IS '数据版本';
                    
      COMMENT ON COLUMN litemall_points_account.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_points_account.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON TABLE litemall_wallet IS '钱包账户表';
                
      COMMENT ON COLUMN litemall_wallet.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_wallet.USER_ID IS '用户ID';
                    
      COMMENT ON COLUMN litemall_wallet.BALANCE IS '可用余额';
                    
      COMMENT ON COLUMN litemall_wallet.TOTAL_RECHARGE IS '累计充值';
                    
      COMMENT ON COLUMN litemall_wallet.TOTAL_SPENT IS '累计消费';
                    
      COMMENT ON COLUMN litemall_wallet.VERSION IS '数据版本';
                    
      COMMENT ON COLUMN litemall_wallet.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_wallet.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON TABLE litemall_user_message IS '用户站内信表';
                
      COMMENT ON COLUMN litemall_user_message.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_user_message.USER_ID IS '用户ID';
                    
      COMMENT ON COLUMN litemall_user_message.MSG_TYPE IS '消息类型';
                    
      COMMENT ON COLUMN litemall_user_message.TITLE IS '标题';
                    
      COMMENT ON COLUMN litemall_user_message.CONTENT IS '内容';
                    
      COMMENT ON COLUMN litemall_user_message.IS_READ IS '是否已读';
                    
      COMMENT ON COLUMN litemall_user_message.READ_TIME IS '阅读时间';
                    
      COMMENT ON COLUMN litemall_user_message.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_user_message.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_user_message.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_material_category IS '素材分类表';
                
      COMMENT ON COLUMN litemall_material_category.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_material_category.NAME IS '分类名称';
                    
      COMMENT ON COLUMN litemall_material_category.PARENT_ID IS '父分类ID';
                    
      COMMENT ON COLUMN litemall_material_category.SORT_ORDER IS '排序';
                    
      COMMENT ON COLUMN litemall_material_category.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_material_category.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_material_category.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_goods IS '商品基本信息';
                
      COMMENT ON COLUMN litemall_goods.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_goods.GOODS_SN IS '商品编号';
                    
      COMMENT ON COLUMN litemall_goods.NAME IS '商品名称';
                    
      COMMENT ON COLUMN litemall_goods.CATEGORY_ID IS '商品所属类目ID';
                    
      COMMENT ON COLUMN litemall_goods.BRAND_ID IS '品牌ID';
                    
      COMMENT ON COLUMN litemall_goods.GALLERY IS '商品宣传图片列表';
                    
      COMMENT ON COLUMN litemall_goods.KEYWORDS IS '商品关键字';
                    
      COMMENT ON COLUMN litemall_goods.BRIEF IS '商品简介';
                    
      COMMENT ON COLUMN litemall_goods.IS_ON_SALE IS '是否在售';
                    
      COMMENT ON COLUMN litemall_goods.SORT_ORDER IS '排序顺序';
                    
      COMMENT ON COLUMN litemall_goods.PIC_URL IS '商品图片';
                    
      COMMENT ON COLUMN litemall_goods.SHARE_URL IS '商品分享海报';
                    
      COMMENT ON COLUMN litemall_goods.IS_NEW IS '是否新品';
                    
      COMMENT ON COLUMN litemall_goods.IS_HOT IS '是否热品';
                    
      COMMENT ON COLUMN litemall_goods.UNIT IS '商品单位';
                    
      COMMENT ON COLUMN litemall_goods.COUNTER_PRICE IS '市场售价';
                    
      COMMENT ON COLUMN litemall_goods.RETAIL_PRICE IS '当前价格';
                    
      COMMENT ON COLUMN litemall_goods.DETAIL IS '详情';
                    
      COMMENT ON COLUMN litemall_goods.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_goods.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_goods.DELETED IS '逻辑删除';
                    
      COMMENT ON COLUMN litemall_goods.IS_RECOMMEND IS '是否推荐';
                    
      COMMENT ON COLUMN litemall_goods.VIDEO_URL IS '商品视频链接';
                    
      COMMENT ON TABLE litemall_coupon_user IS '优惠券用户使用表';
                
      COMMENT ON COLUMN litemall_coupon_user.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_coupon_user.USER_ID IS '用户ID';
                    
      COMMENT ON COLUMN litemall_coupon_user.COUPON_ID IS '优惠券ID';
                    
      COMMENT ON COLUMN litemall_coupon_user.STATUS IS '使用状态';
                    
      COMMENT ON COLUMN litemall_coupon_user.USED_TIME IS '使用时间';
                    
      COMMENT ON COLUMN litemall_coupon_user.START_TIME IS '有效期开始时间';
                    
      COMMENT ON COLUMN litemall_coupon_user.END_TIME IS '有效期截至时间';
                    
      COMMENT ON COLUMN litemall_coupon_user.ORDER_ID IS '订单ID';
                    
      COMMENT ON COLUMN litemall_coupon_user.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_coupon_user.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_coupon_user.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_order IS '订单表';
                
      COMMENT ON COLUMN litemall_order.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_order.USER_ID IS '用户ID';
                    
      COMMENT ON COLUMN litemall_order.ORDER_SN IS '订单编号';
                    
      COMMENT ON COLUMN litemall_order.ORDER_STATUS IS '订单状态';
                    
      COMMENT ON COLUMN litemall_order.AFTERSALE_STATUS IS '售后状态';
                    
      COMMENT ON COLUMN litemall_order.CONSIGNEE IS '收货人名称';
                    
      COMMENT ON COLUMN litemall_order.MOBILE IS '收货人手机号';
                    
      COMMENT ON COLUMN litemall_order.ADDRESS IS '收货具体地址';
                    
      COMMENT ON COLUMN litemall_order.MESSAGE IS '用户订单留言';
                    
      COMMENT ON COLUMN litemall_order.GOODS_PRICE IS '商品总费用';
                    
      COMMENT ON COLUMN litemall_order.FREIGHT_PRICE IS '配送费用';
                    
      COMMENT ON COLUMN litemall_order.COUPON_PRICE IS '优惠券减免';
                    
      COMMENT ON COLUMN litemall_order.INTEGRAL_PRICE IS '用户积分减免';
                    
      COMMENT ON COLUMN litemall_order.GROUPON_PRICE IS '团购优惠价减免';
                    
      COMMENT ON COLUMN litemall_order.ORDER_PRICE IS '订单费用';
                    
      COMMENT ON COLUMN litemall_order.ACTUAL_PRICE IS '实付费用';
                    
      COMMENT ON COLUMN litemall_order.PAY_ID IS '微信付款编号';
                    
      COMMENT ON COLUMN litemall_order.PAY_TIME IS '微信付款时间';
                    
      COMMENT ON COLUMN litemall_order.SHIP_SN IS '发货编号';
                    
      COMMENT ON COLUMN litemall_order.SHIP_CHANNEL IS '发货快递公司';
                    
      COMMENT ON COLUMN litemall_order.SHIP_TIME IS '发货开始时间';
                    
      COMMENT ON COLUMN litemall_order.REFUND_AMOUNT IS '实际退款金额';
                    
      COMMENT ON COLUMN litemall_order.REFUND_TYPE IS '退款方式';
                    
      COMMENT ON COLUMN litemall_order.REFUND_CONTENT IS '退款备注';
                    
      COMMENT ON COLUMN litemall_order.REFUND_TIME IS '退款时间';
                    
      COMMENT ON COLUMN litemall_order.CONFIRM_TIME IS '用户确认收货时间';
                    
      COMMENT ON COLUMN litemall_order.COMMENTS IS '待评价订单商品数量';
                    
      COMMENT ON COLUMN litemall_order.END_TIME IS '订单关闭时间';
                    
      COMMENT ON COLUMN litemall_order.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_order.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_order.DELETED IS '逻辑删除';
                    
      COMMENT ON COLUMN litemall_order.DELIVERY_TYPE IS '配送方式';
                    
      COMMENT ON COLUMN litemall_order.PICKUP_STORE_ID IS '自提门店ID';
                    
      COMMENT ON COLUMN litemall_order.PICKUP_CODE IS '自提核销码';
                    
      COMMENT ON COLUMN litemall_order.PICKUP_TIME IS '核销时间';
                    
      COMMENT ON COLUMN litemall_order.PROMOTION_PRICE IS '促销优惠金额（满减/限时折扣）';
                    
      COMMENT ON COLUMN litemall_order.PIN_TUAN_PRICE IS '拼团优惠金额';
                    
      COMMENT ON COLUMN litemall_order.PAY_CHANNEL IS '支付通道';
                    
      COMMENT ON COLUMN litemall_order.WALLET_PAY_AMOUNT IS '余额支付金额';
                    
      COMMENT ON COLUMN litemall_order.ADMIN_REMARK IS '运营备注';
                    
      COMMENT ON TABLE litemall_promotion_tier IS '满减档位表';
                
      COMMENT ON COLUMN litemall_promotion_tier.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_promotion_tier.ACTIVITY_ID IS '促销活动ID';
                    
      COMMENT ON COLUMN litemall_promotion_tier.MEET_AMOUNT IS '满足金额';
                    
      COMMENT ON COLUMN litemall_promotion_tier.DISCOUNT_VALUE IS '减免值（金额或折扣率）';
                    
      COMMENT ON COLUMN litemall_promotion_tier.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_promotion_tier.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_promotion_tier.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_points_flow IS '积分流水表';
                
      COMMENT ON COLUMN litemall_points_flow.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_points_flow.ACCOUNT_ID IS '积分账户ID';
                    
      COMMENT ON COLUMN litemall_points_flow.USER_ID IS '用户ID';
                    
      COMMENT ON COLUMN litemall_points_flow.CHANGE_TYPE IS '变动类型';
                    
      COMMENT ON COLUMN litemall_points_flow.CHANGE_AMOUNT IS '变动数量';
                    
      COMMENT ON COLUMN litemall_points_flow.BALANCE_AFTER IS '变动后余额';
                    
      COMMENT ON COLUMN litemall_points_flow.SOURCE_TYPE IS '来源类型';
                    
      COMMENT ON COLUMN litemall_points_flow.SOURCE_ID IS '来源业务ID';
                    
      COMMENT ON COLUMN litemall_points_flow.REMARK IS '备注';
                    
      COMMENT ON COLUMN litemall_points_flow.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_points_flow.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON TABLE litemall_wallet_flow IS '钱包流水表';
                
      COMMENT ON COLUMN litemall_wallet_flow.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_wallet_flow.WALLET_ID IS '钱包ID';
                    
      COMMENT ON COLUMN litemall_wallet_flow.CHANGE_TYPE IS '变动类型';
                    
      COMMENT ON COLUMN litemall_wallet_flow.CHANGE_AMOUNT IS '变动金额';
                    
      COMMENT ON COLUMN litemall_wallet_flow.BALANCE_AFTER IS '变动后余额';
                    
      COMMENT ON COLUMN litemall_wallet_flow.SOURCE_TYPE IS '来源类型';
                    
      COMMENT ON COLUMN litemall_wallet_flow.SOURCE_ID IS '来源业务ID';
                    
      COMMENT ON COLUMN litemall_wallet_flow.REMARK IS '备注';
                    
      COMMENT ON COLUMN litemall_wallet_flow.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_wallet_flow.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON TABLE litemall_recharge IS '充值记录表';
                
      COMMENT ON COLUMN litemall_recharge.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_recharge.USER_ID IS '用户ID';
                    
      COMMENT ON COLUMN litemall_recharge.WALLET_ID IS '钱包ID';
                    
      COMMENT ON COLUMN litemall_recharge.AMOUNT IS '充值金额';
                    
      COMMENT ON COLUMN litemall_recharge.GIFT_AMOUNT IS '赠送金额';
                    
      COMMENT ON COLUMN litemall_recharge.PAY_CHANNEL IS '支付渠道';
                    
      COMMENT ON COLUMN litemall_recharge.PAY_STATUS IS '支付状态';
                    
      COMMENT ON COLUMN litemall_recharge.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_recharge.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_recharge.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_material IS '素材资源表';
                
      COMMENT ON COLUMN litemall_material.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_material.NAME IS '文件名';
                    
      COMMENT ON COLUMN litemall_material.URL IS '访问链接';
                    
      COMMENT ON COLUMN litemall_material.FILE_TYPE IS '文件类型(image/video)';
                    
      COMMENT ON COLUMN litemall_material.FILE_SIZE IS '文件大小(bytes)';
                    
      COMMENT ON COLUMN litemall_material.CATEGORY_ID IS '分类ID';
                    
      COMMENT ON COLUMN litemall_material.TAG IS '标签';
                    
      COMMENT ON COLUMN litemall_material.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_material.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_material.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_cart IS '购物车商品表';
                
      COMMENT ON COLUMN litemall_cart.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_cart.USER_ID IS '用户ID';
                    
      COMMENT ON COLUMN litemall_cart.GOODS_ID IS '商品ID';
                    
      COMMENT ON COLUMN litemall_cart.GOODS_SN IS '商品编号';
                    
      COMMENT ON COLUMN litemall_cart.GOODS_NAME IS '商品名称';
                    
      COMMENT ON COLUMN litemall_cart.PRODUCT_ID IS '商品货品表的货品ID';
                    
      COMMENT ON COLUMN litemall_cart.PRICE IS '商品货品的价格';
                    
      COMMENT ON COLUMN litemall_cart."NUMBER" IS '商品货品的数量';
                    
      COMMENT ON COLUMN litemall_cart.SPECIFICATIONS IS '商品规格值列表';
                    
      COMMENT ON COLUMN litemall_cart.CHECKED IS '购物车中商品是否选择状态';
                    
      COMMENT ON COLUMN litemall_cart.PIC_URL IS '商品图片或者商品货品图片';
                    
      COMMENT ON COLUMN litemall_cart.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_cart.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_cart.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_comment IS '评论表';
                
      COMMENT ON COLUMN litemall_comment.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_comment.VALUE_ID IS '如果type=0，则是商品评论；如果是type=1，则是专题评论。';
                    
      COMMENT ON COLUMN litemall_comment.TYPE IS '评论类型';
                    
      COMMENT ON COLUMN litemall_comment.CONTENT IS '评论内容';
                    
      COMMENT ON COLUMN litemall_comment.ADMIN_CONTENT IS '管理员回复内容';
                    
      COMMENT ON COLUMN litemall_comment.USER_ID IS '用户ID';
                    
      COMMENT ON COLUMN litemall_comment.HAS_PICTURE IS '是否含有图片';
                    
      COMMENT ON COLUMN litemall_comment.PIC_URLS IS '图片地址列表，采用JSON数组格式';
                    
      COMMENT ON COLUMN litemall_comment.STAR IS '评分， 1-5';
                    
      COMMENT ON COLUMN litemall_comment.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_comment.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_comment.DELETED IS '逻辑删除';
                    
      COMMENT ON COLUMN litemall_comment.PROS IS '优点列表(JSON)';
                    
      COMMENT ON COLUMN litemall_comment.CONS IS '缺点列表(JSON)';
                    
      COMMENT ON COLUMN litemall_comment.SEMANTIC_RATING IS '语义评级(1-5)';
                    
      COMMENT ON TABLE litemall_footprint IS '用户浏览足迹表';
                
      COMMENT ON COLUMN litemall_footprint.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_footprint.USER_ID IS '用户ID';
                    
      COMMENT ON COLUMN litemall_footprint.GOODS_ID IS '浏览商品ID';
                    
      COMMENT ON COLUMN litemall_footprint.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_footprint.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_footprint.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_goods_attribute IS '商品参数表';
                
      COMMENT ON COLUMN litemall_goods_attribute.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_goods_attribute.GOODS_ID IS '商品ID';
                    
      COMMENT ON COLUMN litemall_goods_attribute.ATTRIBUTE IS '商品参数名称';
                    
      COMMENT ON COLUMN litemall_goods_attribute.VALUE IS '商品参数值';
                    
      COMMENT ON COLUMN litemall_goods_attribute.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_goods_attribute.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_goods_attribute.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_goods_product IS '商品货品表';
                
      COMMENT ON COLUMN litemall_goods_product.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_goods_product.GOODS_ID IS '商品ID';
                    
      COMMENT ON COLUMN litemall_goods_product.SPECIFICATIONS IS '商品规格值';
                    
      COMMENT ON COLUMN litemall_goods_product.PRICE IS '商品货品价格';
                    
      COMMENT ON COLUMN litemall_goods_product."NUMBER" IS '商品货品数量';
                    
      COMMENT ON COLUMN litemall_goods_product.URL IS '商品货品图片';
                    
      COMMENT ON COLUMN litemall_goods_product.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_goods_product.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_goods_product.DELETED IS '逻辑删除';
                    
      COMMENT ON COLUMN litemall_goods_product.SAFE_STOCK IS '安全库存预警线';
                    
      COMMENT ON TABLE litemall_goods_specification IS '商品规格表';
                
      COMMENT ON COLUMN litemall_goods_specification.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_goods_specification.GOODS_ID IS '商品ID';
                    
      COMMENT ON COLUMN litemall_goods_specification.SPECIFICATION IS '商品规格名称';
                    
      COMMENT ON COLUMN litemall_goods_specification.VALUE IS '商品规格值';
                    
      COMMENT ON COLUMN litemall_goods_specification.PIC_URL IS '商品规格图片';
                    
      COMMENT ON COLUMN litemall_goods_specification.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_goods_specification.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_goods_specification.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_groupon_rules IS '团购规则表';
                
      COMMENT ON COLUMN litemall_groupon_rules.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_groupon_rules.GOODS_ID IS '商品ID';
                    
      COMMENT ON COLUMN litemall_groupon_rules.GOODS_NAME IS '商品名称';
                    
      COMMENT ON COLUMN litemall_groupon_rules.PIC_URL IS '商品/货品图片';
                    
      COMMENT ON COLUMN litemall_groupon_rules.DISCOUNT IS '优惠金额';
                    
      COMMENT ON COLUMN litemall_groupon_rules.DISCOUNT_MEMBER IS '达到优惠条件的人数';
                    
      COMMENT ON COLUMN litemall_groupon_rules.EXPIRE_TIME IS '团购过期时间';
                    
      COMMENT ON COLUMN litemall_groupon_rules.STATUS IS '团购规则状态，正常上线则0，到期自动下线则1，管理手动下线则2';
                    
      COMMENT ON COLUMN litemall_groupon_rules.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_groupon_rules.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_groupon_rules.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_time_discount IS '限时折扣表';
                
      COMMENT ON COLUMN litemall_time_discount.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_time_discount.GOODS_ID IS '商品ID';
                    
      COMMENT ON COLUMN litemall_time_discount.PRODUCT_ID IS 'SKU ID（null表示全部SKU）';
                    
      COMMENT ON COLUMN litemall_time_discount.DISCOUNT_TYPE IS '折扣类型';
                    
      COMMENT ON COLUMN litemall_time_discount.DISCOUNT_VALUE IS '折扣值（金额或折扣率）';
                    
      COMMENT ON COLUMN litemall_time_discount.START_TIME IS '开始时间';
                    
      COMMENT ON COLUMN litemall_time_discount.END_TIME IS '结束时间';
                    
      COMMENT ON COLUMN litemall_time_discount.STATUS IS '状态';
                    
      COMMENT ON COLUMN litemall_time_discount.STOCK_LIMIT IS '折扣库存（0不限）';
                    
      COMMENT ON COLUMN litemall_time_discount.REMARK IS '备注';
                    
      COMMENT ON COLUMN litemall_time_discount.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_time_discount.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_time_discount.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_flash_sale IS '秒杀活动表';
                
      COMMENT ON COLUMN litemall_flash_sale.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_flash_sale.GOODS_ID IS '商品ID';
                    
      COMMENT ON COLUMN litemall_flash_sale.PRODUCT_ID IS 'SKU ID（null表示全部SKU）';
                    
      COMMENT ON COLUMN litemall_flash_sale.FLASH_PRICE IS '秒杀价';
                    
      COMMENT ON COLUMN litemall_flash_sale.TOTAL_STOCK IS '活动总库存';
                    
      COMMENT ON COLUMN litemall_flash_sale.MAX_PER_USER IS '每人限购';
                    
      COMMENT ON COLUMN litemall_flash_sale.MAX_PER_ORDER IS '每单限购';
                    
      COMMENT ON COLUMN litemall_flash_sale.STATUS IS '状态';
                    
      COMMENT ON COLUMN litemall_flash_sale.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_flash_sale.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_flash_sale.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_pin_tuan_activity IS '拼团活动表';
                
      COMMENT ON COLUMN litemall_pin_tuan_activity.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_pin_tuan_activity.GOODS_ID IS '商品ID';
                    
      COMMENT ON COLUMN litemall_pin_tuan_activity.PRODUCT_ID IS 'SKU ID（null表示全部SKU）';
                    
      COMMENT ON COLUMN litemall_pin_tuan_activity.PIN_TUAN_PRICE IS '拼团价';
                    
      COMMENT ON COLUMN litemall_pin_tuan_activity.MIN_USER_COUNT IS '成团人数门槛';
                    
      COMMENT ON COLUMN litemall_pin_tuan_activity.MAX_USER_COUNT IS '最多人数';
                    
      COMMENT ON COLUMN litemall_pin_tuan_activity.EXPIRE_HOURS IS '有效时间（小时）';
                    
      COMMENT ON COLUMN litemall_pin_tuan_activity.STATUS IS '状态';
                    
      COMMENT ON COLUMN litemall_pin_tuan_activity.REMARK IS '备注';
                    
      COMMENT ON COLUMN litemall_pin_tuan_activity.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_pin_tuan_activity.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_pin_tuan_activity.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_aftersale IS '售后表';
                
      COMMENT ON COLUMN litemall_aftersale.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_aftersale.AFTERSALE_SN IS '售后编号';
                    
      COMMENT ON COLUMN litemall_aftersale.ORDER_ID IS '订单ID';
                    
      COMMENT ON COLUMN litemall_aftersale.USER_ID IS '用户ID';
                    
      COMMENT ON COLUMN litemall_aftersale.TYPE IS '售后类型';
                    
      COMMENT ON COLUMN litemall_aftersale.REASON IS '退款原因';
                    
      COMMENT ON COLUMN litemall_aftersale.AMOUNT IS '退款金额';
                    
      COMMENT ON COLUMN litemall_aftersale.PICTURES IS '退款凭证图片链接数组';
                    
      COMMENT ON COLUMN litemall_aftersale."COMMENT" IS '退款说明';
                    
      COMMENT ON COLUMN litemall_aftersale.STATUS IS '售后状态';
                    
      COMMENT ON COLUMN litemall_aftersale.HANDLE_TIME IS '管理员操作时间';
                    
      COMMENT ON COLUMN litemall_aftersale.ADD_TIME IS '添加时间';
                    
      COMMENT ON COLUMN litemall_aftersale.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_aftersale.DELETED IS '逻辑删除';
                    
      COMMENT ON COLUMN litemall_aftersale.ORDER_ITEM_ID IS '订单商品项ID';
                    
      COMMENT ON COLUMN litemall_aftersale.PROCESS_NOTE IS '处理备注';
                    
      COMMENT ON COLUMN litemall_aftersale.PROCESS_TIME IS '处理时间';
                    
      COMMENT ON TABLE litemall_groupon IS '团购活动表';
                
      COMMENT ON COLUMN litemall_groupon.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_groupon.ORDER_ID IS '订单ID';
                    
      COMMENT ON COLUMN litemall_groupon.GROUPON_ID IS '如果是开团用户，则groupon_id是0；如果是参团用户，则groupon_id是团购活动ID';
                    
      COMMENT ON COLUMN litemall_groupon.RULES_ID IS '团购规则ID';
                    
      COMMENT ON COLUMN litemall_groupon.USER_ID IS '用户ID';
                    
      COMMENT ON COLUMN litemall_groupon.SHARE_URL IS '团购分享图片地址';
                    
      COMMENT ON COLUMN litemall_groupon.CREATOR_USER_ID IS '开团用户ID';
                    
      COMMENT ON COLUMN litemall_groupon.CREATOR_USER_TIME IS '开团时间';
                    
      COMMENT ON COLUMN litemall_groupon.STATUS IS '团购活动状态';
                    
      COMMENT ON COLUMN litemall_groupon.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_groupon.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_groupon.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_order_goods IS '订单商品表';
                
      COMMENT ON COLUMN litemall_order_goods.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_order_goods.ORDER_ID IS '订单ID';
                    
      COMMENT ON COLUMN litemall_order_goods.GOODS_ID IS '商品ID';
                    
      COMMENT ON COLUMN litemall_order_goods.GOODS_NAME IS '商品名称';
                    
      COMMENT ON COLUMN litemall_order_goods.GOODS_SN IS '商品编号';
                    
      COMMENT ON COLUMN litemall_order_goods.PRODUCT_ID IS '货品ID';
                    
      COMMENT ON COLUMN litemall_order_goods."NUMBER" IS '购买数量';
                    
      COMMENT ON COLUMN litemall_order_goods.PRICE IS '售价';
                    
      COMMENT ON COLUMN litemall_order_goods.SPECIFICATIONS IS '规格列表';
                    
      COMMENT ON COLUMN litemall_order_goods.PIC_URL IS '商品/货品图片';
                    
      COMMENT ON COLUMN litemall_order_goods."COMMENT" IS '订单商品评论，如果是-1，则超期不能评价；如果是0，则可以评价；如果其他值，则是comment表里面的评论ID。';
                    
      COMMENT ON COLUMN litemall_order_goods.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_order_goods.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_order_goods.DELETED IS '逻辑删除';
                    
      COMMENT ON COLUMN litemall_order_goods.ACTUAL_PAY_AMOUNT IS '实付金额';
                    
      COMMENT ON TABLE litemall_flash_sale_session IS '秒杀场次表';
                
      COMMENT ON COLUMN litemall_flash_sale_session.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_flash_sale_session.FLASH_SALE_ID IS '秒杀活动ID';
                    
      COMMENT ON COLUMN litemall_flash_sale_session.SESSION_START IS '场次开始时间';
                    
      COMMENT ON COLUMN litemall_flash_sale_session.SESSION_END IS '场次结束时间';
                    
      COMMENT ON COLUMN litemall_flash_sale_session.SESSION_STOCK IS '场次库存';
                    
      COMMENT ON COLUMN litemall_flash_sale_session.SESSION_STATUS IS '场次状态（0未开始/1进行中/2已结束）';
                    
      COMMENT ON COLUMN litemall_flash_sale_session.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_flash_sale_session.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_flash_sale_session.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_pin_tuan_group IS '拼团开团记录表';
                
      COMMENT ON COLUMN litemall_pin_tuan_group.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_pin_tuan_group.ACTIVITY_ID IS '拼团活动ID';
                    
      COMMENT ON COLUMN litemall_pin_tuan_group.CREATOR_USER_ID IS '团长用户ID';
                    
      COMMENT ON COLUMN litemall_pin_tuan_group.ORDER_ID IS '团长的订单ID';
                    
      COMMENT ON COLUMN litemall_pin_tuan_group.STATUS IS '拼团状态';
                    
      COMMENT ON COLUMN litemall_pin_tuan_group.EXPIRE_TIME IS '过期时间';
                    
      COMMENT ON COLUMN litemall_pin_tuan_group.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_pin_tuan_group.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_pin_tuan_group.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_pin_tuan_member IS '拼团参团记录表';
                
      COMMENT ON COLUMN litemall_pin_tuan_member.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_pin_tuan_member.GROUP_ID IS '拼团团ID';
                    
      COMMENT ON COLUMN litemall_pin_tuan_member.USER_ID IS '用户ID';
                    
      COMMENT ON COLUMN litemall_pin_tuan_member.ORDER_ID IS '订单ID';
                    
      COMMENT ON COLUMN litemall_pin_tuan_member.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_pin_tuan_member.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_pin_tuan_member.DELETED IS '逻辑删除';
                    
