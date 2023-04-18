
CREATE TABLE litemall_ad(
  ID INTEGER NOT NULL ,
  NAME VARCHAR2(63) NOT NULL ,
  LINK VARCHAR2(255) NOT NULL ,
  URL VARCHAR2(255) NOT NULL ,
  POSITION SMALLINT  ,
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
  USER_ID INTEGER NOT NULL ,
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

CREATE TABLE litemall_admin(
  ID INTEGER NOT NULL ,
  USERNAME VARCHAR2(63) NOT NULL ,
  PASSWORD VARCHAR2(63) NOT NULL ,
  LAST_LOGIN_IP VARCHAR2(63)  ,
  LAST_LOGIN_TIME DATE  ,
  AVATAR VARCHAR2(255)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  ROLE_IDS VARCHAR2(127)  ,
  constraint PK_litemall_admin primary key (ID)
);

CREATE TABLE litemall_aftersale(
  ID INTEGER NOT NULL ,
  AFTERSALE_SN VARCHAR2(63)  ,
  ORDER_ID INTEGER NOT NULL ,
  USER_ID INTEGER NOT NULL ,
  TYPE SMALLINT  ,
  REASON VARCHAR2(31)  ,
  AMOUNT NUMBER(10,2)  ,
  PICTURES VARCHAR2(1023)  ,
  "COMMENT" VARCHAR2(511)  ,
  STATUS SMALLINT  ,
  HANDLE_TIME DATE  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_aftersale primary key (ID)
);

CREATE TABLE litemall_cart(
  ID INTEGER NOT NULL ,
  USER_ID INTEGER  ,
  GOODS_ID INTEGER  ,
  GOODS_SN VARCHAR2(63)  ,
  GOODS_NAME VARCHAR2(127)  ,
  PRODUCT_ID INTEGER  ,
  PRICE NUMBER(10,2)  ,
  "NUMBER" SMALLINT  ,
  SPECIFICATIONS VARCHAR2(1023)  ,
  CHECKED CHAR(1)  ,
  PIC_URL VARCHAR2(255)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_cart primary key (ID)
);

CREATE TABLE litemall_collect(
  ID INTEGER NOT NULL ,
  USER_ID INTEGER NOT NULL ,
  VALUE_ID INTEGER NOT NULL ,
  TYPE SMALLINT NOT NULL ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_collect primary key (ID)
);

CREATE TABLE litemall_comment(
  ID INTEGER NOT NULL ,
  VALUE_ID INTEGER NOT NULL ,
  TYPE SMALLINT NOT NULL ,
  CONTENT VARCHAR2(1023)  ,
  ADMIN_CONTENT VARCHAR2(511)  ,
  USER_ID INTEGER NOT NULL ,
  HAS_PICTURE CHAR(1)  ,
  PIC_URLS VARCHAR2(1023)  ,
  STAR SMALLINT  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_comment primary key (ID)
);

CREATE TABLE litemall_coupon_user(
  ID INTEGER NOT NULL ,
  USER_ID INTEGER NOT NULL ,
  COUPON_ID INTEGER NOT NULL ,
  STATUS SMALLINT  ,
  USED_TIME DATE  ,
  START_TIME DATE  ,
  END_TIME DATE  ,
  ORDER_ID INTEGER  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_coupon_user primary key (ID)
);

CREATE TABLE litemall_feedback(
  ID INTEGER NOT NULL ,
  USER_ID INTEGER NOT NULL ,
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

CREATE TABLE litemall_footprint(
  ID INTEGER NOT NULL ,
  USER_ID INTEGER NOT NULL ,
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

CREATE TABLE litemall_groupon(
  ID INTEGER NOT NULL ,
  ORDER_ID INTEGER NOT NULL ,
  GROUPON_ID INTEGER  ,
  RULES_ID INTEGER NOT NULL ,
  USER_ID INTEGER NOT NULL ,
  SHARE_URL VARCHAR2(255)  ,
  CREATOR_USER_ID INTEGER NOT NULL ,
  CREATOR_USER_TIME DATE  ,
  STATUS SMALLINT  ,
  ADD_TIME DATE NOT NULL ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_groupon primary key (ID)
);

CREATE TABLE litemall_groupon_rules(
  ID INTEGER NOT NULL ,
  GOODS_ID INTEGER NOT NULL ,
  GOODS_NAME VARCHAR2(127) NOT NULL ,
  PIC_URL VARCHAR2(255)  ,
  DISCOUNT NUMBER(63,0) NOT NULL ,
  DISCOUNT_MEMBER INTEGER NOT NULL ,
  EXPIRE_TIME DATE  ,
  STATUS SMALLINT  ,
  ADD_TIME DATE NOT NULL ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_groupon_rules primary key (ID)
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

CREATE TABLE litemall_order_goods(
  ID INTEGER NOT NULL ,
  ORDER_ID INTEGER NOT NULL ,
  GOODS_ID INTEGER NOT NULL ,
  GOODS_NAME VARCHAR2(127) NOT NULL ,
  GOODS_SN VARCHAR2(63) NOT NULL ,
  PRODUCT_ID INTEGER NOT NULL ,
  "NUMBER" SMALLINT NOT NULL ,
  PRICE NUMBER(10,2) NOT NULL ,
  SPECIFICATIONS VARCHAR2(1023) NOT NULL ,
  PIC_URL VARCHAR2(255) NOT NULL ,
  "COMMENT" INTEGER  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_order_goods primary key (ID)
);

CREATE TABLE litemall_permission(
  ID INTEGER NOT NULL ,
  ROLE_ID INTEGER  ,
  PERMISSION VARCHAR2(63)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_permission primary key (ID)
);

CREATE TABLE litemall_region(
  ID INTEGER NOT NULL ,
  PID INTEGER NOT NULL ,
  NAME VARCHAR2(120) NOT NULL ,
  TYPE SMALLINT NOT NULL ,
  CODE INTEGER NOT NULL ,
  constraint PK_litemall_region primary key (ID)
);

CREATE TABLE litemall_search_history(
  ID INTEGER NOT NULL ,
  USER_ID INTEGER NOT NULL ,
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

CREATE TABLE litemall_topic(
  ID INTEGER NOT NULL ,
  TITLE VARCHAR2(255) NOT NULL ,
  SUBTITLE VARCHAR2(255)  ,
  CONTENT CLOB  ,
  PRICE NUMBER(10,2)  ,
  READ_COUNT VARCHAR2(255)  ,
  PIC_URL VARCHAR2(255)  ,
  SORT_ORDER INTEGER  ,
  GOODS VARCHAR2(1023)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_topic primary key (ID)
);

CREATE TABLE litemall_user_role(
  USER_ID VARCHAR2(32) NOT NULL ,
  ROLE_ID VARCHAR2(50) NOT NULL ,
  VERSION INTEGER NOT NULL ,
  CREATED_BY VARCHAR2(50) NOT NULL ,
  CREATE_TIME DATE NOT NULL ,
  UPDATED_BY VARCHAR2(50) NOT NULL ,
  UPDATE_TIME DATE NOT NULL ,
  REMARK VARCHAR2(200)  ,
  constraint PK_litemall_user_role primary key (USER_ID,ROLE_ID)
);

CREATE TABLE litemall_coupon(
  ID INTEGER NOT NULL ,
  NAME VARCHAR2(63) NOT NULL ,
  "DESC" VARCHAR2(127)  ,
  TAG VARCHAR2(63)  ,
  TOTAL INTEGER NOT NULL ,
  DISCOUNT NUMBER(10,2)  ,
  MIN NUMBER(10,2)  ,
  LIMIT SMALLINT  ,
  TYPE SMALLINT  ,
  STATUS SMALLINT  ,
  GOODS_TYPE SMALLINT  ,
  GOODS_VALUE VARCHAR2(1023)  ,
  CODE VARCHAR2(63)  ,
  TIME_TYPE SMALLINT  ,
  DAYS SMALLINT  ,
  START_TIME DATE  ,
  END_TIME DATE  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_coupon primary key (ID)
);

CREATE TABLE litemall_order(
  ID INTEGER NOT NULL ,
  USER_ID INTEGER NOT NULL ,
  ORDER_SN VARCHAR2(63) NOT NULL ,
  ORDER_STATUS SMALLINT NOT NULL ,
  AFTERSALE_STATUS SMALLINT  ,
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
  COMMENTS SMALLINT  ,
  END_TIME DATE  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_order primary key (ID)
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
  constraint PK_litemall_goods_product primary key (ID)
);

CREATE TABLE litemall_role(
  ID INTEGER NOT NULL ,
  NAME VARCHAR2(63) NOT NULL ,
  "DESC" VARCHAR2(1023)  ,
  ENABLED CHAR(1)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_role primary key (ID)
);

CREATE TABLE litemall_user(
  ID INTEGER NOT NULL ,
  USERNAME VARCHAR2(63) NOT NULL ,
  PASSWORD VARCHAR2(63) NOT NULL ,
  GENDER SMALLINT NOT NULL ,
  BIRTHDAY DATE  ,
  LAST_LOGIN_TIME DATE  ,
  LAST_LOGIN_IP VARCHAR2(63) NOT NULL ,
  USER_LEVEL SMALLINT  ,
  NICKNAME VARCHAR2(63) NOT NULL ,
  MOBILE VARCHAR2(20) NOT NULL ,
  AVATAR VARCHAR2(255) NOT NULL ,
  WEIXIN_OPENID VARCHAR2(63) NOT NULL ,
  SESSION_KEY VARCHAR2(100) NOT NULL ,
  STATUS SMALLINT NOT NULL ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_user primary key (ID)
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
  SORT_ORDER SMALLINT  ,
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
  constraint PK_litemall_goods primary key (ID)
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
  SORT_ORDER SMALLINT  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_category primary key (ID)
);

CREATE TABLE litemall_brand(
  ID INTEGER NOT NULL ,
  NAME VARCHAR2(255) NOT NULL ,
  PIC_URL VARCHAR2(255) NOT NULL ,
  "DESC" VARCHAR2(255) NOT NULL ,
  SORT_ORDER SMALLINT  ,
  FLOOR_PRICE NUMBER(10,2)  ,
  ADD_TIME DATE  ,
  UPDATE_TIME DATE  ,
  DELETED CHAR(1)  ,
  constraint PK_litemall_brand primary key (ID)
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
                    
      COMMENT ON TABLE litemall_admin IS '管理员表';
                
      COMMENT ON COLUMN litemall_admin.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_admin.USERNAME IS '管理员名称';
                    
      COMMENT ON COLUMN litemall_admin.PASSWORD IS '管理员密码';
                    
      COMMENT ON COLUMN litemall_admin.LAST_LOGIN_IP IS '最近一次登录IP地址';
                    
      COMMENT ON COLUMN litemall_admin.LAST_LOGIN_TIME IS '最近一次登录时间';
                    
      COMMENT ON COLUMN litemall_admin.AVATAR IS '头像图片';
                    
      COMMENT ON COLUMN litemall_admin.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_admin.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_admin.DELETED IS '逻辑删除';
                    
      COMMENT ON COLUMN litemall_admin.ROLE_IDS IS '角色列表';
                    
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
                    
      COMMENT ON TABLE litemall_collect IS '收藏表';
                
      COMMENT ON COLUMN litemall_collect.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_collect.USER_ID IS '用户ID';
                    
      COMMENT ON COLUMN litemall_collect.VALUE_ID IS '如果type=0，则是商品ID；如果type=1，则是专题ID';
                    
      COMMENT ON COLUMN litemall_collect.TYPE IS '收藏类型';
                    
      COMMENT ON COLUMN litemall_collect.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_collect.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_collect.DELETED IS '逻辑删除';
                    
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
                    
      COMMENT ON TABLE litemall_goods_specification IS '商品规格表';
                
      COMMENT ON COLUMN litemall_goods_specification.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_goods_specification.GOODS_ID IS '商品ID';
                    
      COMMENT ON COLUMN litemall_goods_specification.SPECIFICATION IS '商品规格名称';
                    
      COMMENT ON COLUMN litemall_goods_specification.VALUE IS '商品规格值';
                    
      COMMENT ON COLUMN litemall_goods_specification.PIC_URL IS '商品规格图片';
                    
      COMMENT ON COLUMN litemall_goods_specification.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_goods_specification.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_goods_specification.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_groupon IS '团购活动表';
                
      COMMENT ON COLUMN litemall_groupon.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_groupon.ORDER_ID IS '订单ID';
                    
      COMMENT ON COLUMN litemall_groupon.GROUPON_ID IS '如果是开团用户，则groupon_id是0；如果是参团用户，则groupon_id是团购活动ID';
                    
      COMMENT ON COLUMN litemall_groupon.RULES_ID IS '团购规则ID';
                    
      COMMENT ON COLUMN litemall_groupon.USER_ID IS '用户ID';
                    
      COMMENT ON COLUMN litemall_groupon.SHARE_URL IS '团购分享图片地址';
                    
      COMMENT ON COLUMN litemall_groupon.CREATOR_USER_ID IS '开团用户ID';
                    
      COMMENT ON COLUMN litemall_groupon.CREATOR_USER_TIME IS '开团时间';
                    
      COMMENT ON COLUMN litemall_groupon.STATUS IS '团购活动状态，开团未支付则0，开团中则1，开团失败则2';
                    
      COMMENT ON COLUMN litemall_groupon.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_groupon.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_groupon.DELETED IS '逻辑删除';
                    
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
                    
      COMMENT ON TABLE litemall_permission IS '权限表';
                
      COMMENT ON COLUMN litemall_permission.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_permission.ROLE_ID IS '角色ID';
                    
      COMMENT ON COLUMN litemall_permission.PERMISSION IS '权限';
                    
      COMMENT ON COLUMN litemall_permission.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_permission.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_permission.DELETED IS '逻辑删除';
                    
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
                    
      COMMENT ON TABLE litemall_user_role IS '用户角色';
                
      COMMENT ON COLUMN litemall_user_role.USER_ID IS '用户ID';
                    
      COMMENT ON COLUMN litemall_user_role.ROLE_ID IS '角色ID';
                    
      COMMENT ON COLUMN litemall_user_role.VERSION IS '数据版本';
                    
      COMMENT ON COLUMN litemall_user_role.CREATED_BY IS '创建人';
                    
      COMMENT ON COLUMN litemall_user_role.CREATE_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_user_role.UPDATED_BY IS '修改人';
                    
      COMMENT ON COLUMN litemall_user_role.UPDATE_TIME IS '修改时间';
                    
      COMMENT ON COLUMN litemall_user_role.REMARK IS '备注';
                    
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
                    
      COMMENT ON TABLE litemall_role IS '角色表';
                
      COMMENT ON COLUMN litemall_role.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_role.NAME IS '角色名称';
                    
      COMMENT ON COLUMN litemall_role."DESC" IS '角色描述';
                    
      COMMENT ON COLUMN litemall_role.ENABLED IS '是否启用';
                    
      COMMENT ON COLUMN litemall_role.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_role.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_role.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_user IS '用户表';
                
      COMMENT ON COLUMN litemall_user.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_user.USERNAME IS '用户名称';
                    
      COMMENT ON COLUMN litemall_user.PASSWORD IS '用户密码';
                    
      COMMENT ON COLUMN litemall_user.GENDER IS '性别';
                    
      COMMENT ON COLUMN litemall_user.BIRTHDAY IS '生日';
                    
      COMMENT ON COLUMN litemall_user.LAST_LOGIN_TIME IS '最近一次登录时间';
                    
      COMMENT ON COLUMN litemall_user.LAST_LOGIN_IP IS '最近一次登录IP地址';
                    
      COMMENT ON COLUMN litemall_user.USER_LEVEL IS '用户等级';
                    
      COMMENT ON COLUMN litemall_user.NICKNAME IS '用户昵称或网络名称';
                    
      COMMENT ON COLUMN litemall_user.MOBILE IS '用户手机号码';
                    
      COMMENT ON COLUMN litemall_user.AVATAR IS '用户头像图片';
                    
      COMMENT ON COLUMN litemall_user.WEIXIN_OPENID IS '微信登录openid';
                    
      COMMENT ON COLUMN litemall_user.SESSION_KEY IS '微信登录会话KEY';
                    
      COMMENT ON COLUMN litemall_user.STATUS IS '用户状态';
                    
      COMMENT ON COLUMN litemall_user.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_user.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_user.DELETED IS '逻辑删除';
                    
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
                    
