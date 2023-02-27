
CREATE TABLE litemall_ad(
  ID INT4 NOT NULL ,
  NAME TEXT NOT NULL ,
  LINK TEXT NOT NULL ,
  URL TEXT NOT NULL ,
  POSITION INT4  ,
  CONTENT TEXT  ,
  START_TIME TIMESTAMP  ,
  END_TIME TIMESTAMP  ,
  ENABLED BOOLEAN  ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_ad primary key (ID)
);

CREATE TABLE litemall_address(
  ID INT4 NOT NULL ,
  NAME TEXT NOT NULL ,
  USER_ID INT4 NOT NULL ,
  PROVINCE TEXT NOT NULL ,
  CITY TEXT NOT NULL ,
  COUNTY TEXT NOT NULL ,
  ADDRESS_DETAIL TEXT NOT NULL ,
  AREA_CODE CHAR(6)  ,
  POSTAL_CODE CHAR(6)  ,
  TEL TEXT NOT NULL ,
  IS_DEFAULT BOOLEAN NOT NULL ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_address primary key (ID)
);

CREATE TABLE litemall_admin(
  ID INT4 NOT NULL ,
  USERNAME TEXT NOT NULL ,
  PASSWORD TEXT NOT NULL ,
  LAST_LOGIN_IP TEXT  ,
  LAST_LOGIN_TIME TIMESTAMP  ,
  AVATAR TEXT  ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  ROLE_IDS TEXT  ,
  constraint PK_litemall_admin primary key (ID)
);

CREATE TABLE litemall_aftersale(
  ID INT4 NOT NULL ,
  AFTERSALE_SN TEXT  ,
  ORDER_ID INT4 NOT NULL ,
  USER_ID INT4 NOT NULL ,
  TYPE INT4  ,
  REASON TEXT  ,
  AMOUNT NUMERIC(10,2)  ,
  PICTURES TEXT  ,
  COMMENT TEXT  ,
  STATUS INT4  ,
  HANDLE_TIME TIMESTAMP  ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_aftersale primary key (ID)
);

CREATE TABLE litemall_brand(
  ID INT4 NOT NULL ,
  NAME TEXT NOT NULL ,
  "DESC" TEXT NOT NULL ,
  PIC_URL TEXT NOT NULL ,
  SORT_ORDER INT4  ,
  FLOOR_PRICE NUMERIC(10,2)  ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_brand primary key (ID)
);

CREATE TABLE litemall_cart(
  ID INT4 NOT NULL ,
  USER_ID INT4  ,
  GOODS_ID INT4  ,
  GOODS_SN TEXT  ,
  GOODS_NAME TEXT  ,
  PRODUCT_ID INT4  ,
  PRICE NUMERIC(10,2)  ,
  NUMBER INT4  ,
  SPECIFICATIONS TEXT  ,
  CHECKED BOOLEAN  ,
  PIC_URL TEXT  ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_cart primary key (ID)
);

CREATE TABLE litemall_category(
  ID INT4 NOT NULL ,
  NAME TEXT NOT NULL ,
  KEYWORDS TEXT NOT NULL ,
  "DESC" TEXT  ,
  PID INT4 NOT NULL ,
  ICON_URL TEXT  ,
  PIC_URL TEXT  ,
  LEVEL TEXT  ,
  SORT_ORDER INT4  ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_category primary key (ID)
);

CREATE TABLE litemall_collect(
  ID INT4 NOT NULL ,
  USER_ID INT4 NOT NULL ,
  VALUE_ID INT4 NOT NULL ,
  TYPE INT4 NOT NULL ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_collect primary key (ID)
);

CREATE TABLE litemall_comment(
  ID INT4 NOT NULL ,
  VALUE_ID INT4 NOT NULL ,
  TYPE INT4 NOT NULL ,
  CONTENT TEXT  ,
  ADMIN_CONTENT TEXT  ,
  USER_ID INT4 NOT NULL ,
  HAS_PICTURE BOOLEAN  ,
  PIC_URLS TEXT  ,
  STAR INT4  ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_comment primary key (ID)
);

CREATE TABLE litemall_coupon(
  ID INT4 NOT NULL ,
  NAME TEXT NOT NULL ,
  "DESC" TEXT  ,
  TAG TEXT  ,
  TOTAL INT4 NOT NULL ,
  DISCOUNT NUMERIC(10,2)  ,
  MIN NUMERIC(10,2)  ,
  "LIMIT" INT4  ,
  TYPE INT4  ,
  STATUS INT4  ,
  GOODS_TYPE INT4  ,
  GOODS_VALUE TEXT  ,
  CODE TEXT  ,
  TIME_TYPE INT4  ,
  DAYS INT4  ,
  START_TIME TIMESTAMP  ,
  END_TIME TIMESTAMP  ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_coupon primary key (ID)
);

CREATE TABLE litemall_coupon_user(
  ID INT4 NOT NULL ,
  USER_ID INT4 NOT NULL ,
  COUPON_ID INT4 NOT NULL ,
  STATUS INT4  ,
  USED_TIME TIMESTAMP  ,
  START_TIME TIMESTAMP  ,
  END_TIME TIMESTAMP  ,
  ORDER_ID INT4  ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_coupon_user primary key (ID)
);

CREATE TABLE litemall_feedback(
  ID INT4 NOT NULL ,
  USER_ID INT4 NOT NULL ,
  USERNAME TEXT NOT NULL ,
  MOBILE TEXT NOT NULL ,
  FEED_TYPE TEXT NOT NULL ,
  CONTENT TEXT NOT NULL ,
  STATUS INT4 NOT NULL ,
  HAS_PICTURE BOOLEAN  ,
  PIC_URLS TEXT  ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_feedback primary key (ID)
);

CREATE TABLE litemall_footprint(
  ID INT4 NOT NULL ,
  USER_ID INT4 NOT NULL ,
  GOODS_ID INT4 NOT NULL ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_footprint primary key (ID)
);

CREATE TABLE litemall_goods(
  ID INT4 NOT NULL ,
  GOODS_SN TEXT NOT NULL ,
  NAME TEXT NOT NULL ,
  CATEGORY_ID INT4  ,
  BRAND_ID INT4  ,
  GALLERY TEXT  ,
  KEYWORDS TEXT  ,
  BRIEF TEXT  ,
  IS_ON_SALE BOOLEAN  ,
  SORT_ORDER INT4  ,
  PIC_URL TEXT  ,
  SHARE_URL TEXT  ,
  IS_NEW BOOLEAN  ,
  IS_HOT BOOLEAN  ,
  UNIT TEXT  ,
  COUNTER_PRICE NUMERIC(10,2)  ,
  RETAIL_PRICE NUMERIC(10,2)  ,
  DETAIL TEXT  ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_goods primary key (ID)
);

CREATE TABLE litemall_goods_attribute(
  ID INT4 NOT NULL ,
  GOODS_ID INT4 NOT NULL ,
  ATTRIBUTE TEXT NOT NULL ,
  VALUE TEXT NOT NULL ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_goods_attribute primary key (ID)
);

CREATE TABLE litemall_goods_product(
  ID INT4 NOT NULL ,
  GOODS_ID INT4 NOT NULL ,
  SPECIFICATIONS TEXT NOT NULL ,
  PRICE NUMERIC(10,2) NOT NULL ,
  NUMBER INT4 NOT NULL ,
  URL TEXT  ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_goods_product primary key (ID)
);

CREATE TABLE litemall_goods_specification(
  ID INT4 NOT NULL ,
  GOODS_ID INT4 NOT NULL ,
  SPECIFICATION TEXT NOT NULL ,
  VALUE TEXT NOT NULL ,
  PIC_URL TEXT NOT NULL ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_goods_specification primary key (ID)
);

CREATE TABLE litemall_groupon(
  ID INT4 NOT NULL ,
  ORDER_ID INT4 NOT NULL ,
  GROUPON_ID INT4  ,
  RULES_ID INT4 NOT NULL ,
  USER_ID INT4 NOT NULL ,
  SHARE_URL TEXT  ,
  CREATOR_USER_ID INT4 NOT NULL ,
  CREATOR_USER_TIME TIMESTAMP  ,
  STATUS INT4  ,
  ADD_TIME TIMESTAMP NOT NULL ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_groupon primary key (ID)
);

CREATE TABLE litemall_groupon_rules(
  ID INT4 NOT NULL ,
  GOODS_ID INT4 NOT NULL ,
  GOODS_NAME TEXT NOT NULL ,
  PIC_URL TEXT  ,
  DISCOUNT NUMERIC(63,0) NOT NULL ,
  DISCOUNT_MEMBER INT4 NOT NULL ,
  EXPIRE_TIME TIMESTAMP  ,
  STATUS INT4  ,
  ADD_TIME TIMESTAMP NOT NULL ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_groupon_rules primary key (ID)
);

CREATE TABLE litemall_issue(
  ID INT4 NOT NULL ,
  QUESTION TEXT  ,
  ANSWER TEXT  ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_issue primary key (ID)
);

CREATE TABLE litemall_keyword(
  ID INT4 NOT NULL ,
  KEYWORD TEXT NOT NULL ,
  URL TEXT NOT NULL ,
  IS_HOT BOOLEAN NOT NULL ,
  IS_DEFAULT BOOLEAN NOT NULL ,
  SORT_ORDER INT4 NOT NULL ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_keyword primary key (ID)
);

CREATE TABLE litemall_log(
  ID INT4 NOT NULL ,
  ADMIN TEXT  ,
  IP TEXT  ,
  TYPE INT4  ,
  ACTION TEXT  ,
  STATUS BOOLEAN  ,
  RESULT TEXT  ,
  COMMENT TEXT  ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_log primary key (ID)
);

CREATE TABLE litemall_notice(
  ID INT4 NOT NULL ,
  TITLE TEXT  ,
  CONTENT TEXT  ,
  ADMIN_ID INT4  ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_notice primary key (ID)
);

CREATE TABLE litemall_notice_admin(
  ID INT4 NOT NULL ,
  NOTICE_ID INT4  ,
  NOTICE_TITLE TEXT  ,
  ADMIN_ID INT4  ,
  READ_TIME TIMESTAMP  ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_notice_admin primary key (ID)
);

CREATE TABLE litemall_order(
  ID INT4 NOT NULL ,
  USER_ID INT4 NOT NULL ,
  ORDER_SN TEXT NOT NULL ,
  ORDER_STATUS INT4 NOT NULL ,
  AFTERSALE_STATUS INT4  ,
  CONSIGNEE TEXT NOT NULL ,
  MOBILE TEXT NOT NULL ,
  ADDRESS TEXT NOT NULL ,
  MESSAGE TEXT NOT NULL ,
  GOODS_PRICE NUMERIC(10,2) NOT NULL ,
  FREIGHT_PRICE NUMERIC(10,2) NOT NULL ,
  COUPON_PRICE NUMERIC(10,2) NOT NULL ,
  INTEGRAL_PRICE NUMERIC(10,2) NOT NULL ,
  GROUPON_PRICE NUMERIC(10,2) NOT NULL ,
  ORDER_PRICE NUMERIC(10,2) NOT NULL ,
  ACTUAL_PRICE NUMERIC(10,2) NOT NULL ,
  PAY_ID TEXT  ,
  PAY_TIME TIMESTAMP  ,
  SHIP_SN TEXT  ,
  SHIP_CHANNEL TEXT  ,
  SHIP_TIME TIMESTAMP  ,
  REFUND_AMOUNT NUMERIC(10,2)  ,
  REFUND_TYPE TEXT  ,
  REFUND_CONTENT TEXT  ,
  REFUND_TIME TIMESTAMP  ,
  CONFIRM_TIME TIMESTAMP  ,
  COMMENTS INT4  ,
  END_TIME TIMESTAMP  ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_order primary key (ID)
);

CREATE TABLE litemall_order_goods(
  ID INT4 NOT NULL ,
  ORDER_ID INT4 NOT NULL ,
  GOODS_ID INT4 NOT NULL ,
  GOODS_NAME TEXT NOT NULL ,
  GOODS_SN TEXT NOT NULL ,
  PRODUCT_ID INT4 NOT NULL ,
  NUMBER INT4 NOT NULL ,
  PRICE NUMERIC(10,2) NOT NULL ,
  SPECIFICATIONS TEXT NOT NULL ,
  PIC_URL TEXT NOT NULL ,
  COMMENT INT4  ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_order_goods primary key (ID)
);

CREATE TABLE litemall_permission(
  ID INT4 NOT NULL ,
  ROLE_ID INT4  ,
  PERMISSION TEXT  ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_permission primary key (ID)
);

CREATE TABLE litemall_region(
  ID INT4 NOT NULL ,
  PID INT4 NOT NULL ,
  NAME TEXT NOT NULL ,
  TYPE INT4 NOT NULL ,
  CODE INT4 NOT NULL ,
  constraint PK_litemall_region primary key (ID)
);

CREATE TABLE litemall_role(
  ID INT4 NOT NULL ,
  NAME TEXT NOT NULL ,
  "DESC" TEXT  ,
  ENABLED BOOLEAN  ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_role primary key (ID)
);

CREATE TABLE litemall_search_history(
  ID INT4 NOT NULL ,
  USER_ID INT4 NOT NULL ,
  KEYWORD TEXT NOT NULL ,
  "FROM" TEXT NOT NULL ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_search_history primary key (ID)
);

CREATE TABLE litemall_storage(
  ID INT4 NOT NULL ,
  KEY TEXT NOT NULL ,
  NAME TEXT NOT NULL ,
  TYPE TEXT NOT NULL ,
  SIZE INT4 NOT NULL ,
  URL TEXT  ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_storage primary key (ID)
);

CREATE TABLE litemall_system(
  ID INT4 NOT NULL ,
  KEY_NAME TEXT NOT NULL ,
  KEY_VALUE TEXT NOT NULL ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_system primary key (ID)
);

CREATE TABLE litemall_topic(
  ID INT4 NOT NULL ,
  TITLE TEXT NOT NULL ,
  SUBTITLE TEXT  ,
  CONTENT TEXT  ,
  PRICE NUMERIC(10,2)  ,
  READ_COUNT TEXT  ,
  PIC_URL TEXT  ,
  SORT_ORDER INT4  ,
  GOODS TEXT  ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_topic primary key (ID)
);

CREATE TABLE litemall_user(
  ID INT4 NOT NULL ,
  USERNAME TEXT NOT NULL ,
  PASSWORD TEXT NOT NULL ,
  GENDER INT4 NOT NULL ,
  BIRTHDAY DATE  ,
  LAST_LOGIN_TIME TIMESTAMP  ,
  LAST_LOGIN_IP TEXT NOT NULL ,
  USER_LEVEL INT4  ,
  NICKNAME TEXT NOT NULL ,
  MOBILE TEXT NOT NULL ,
  AVATAR TEXT NOT NULL ,
  WEIXIN_OPENID TEXT NOT NULL ,
  SESSION_KEY TEXT NOT NULL ,
  STATUS INT4 NOT NULL ,
  ADD_TIME TIMESTAMP  ,
  UPDATE_TIME TIMESTAMP  ,
  DELETED BOOLEAN  ,
  constraint PK_litemall_user primary key (ID)
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
                    
      COMMENT ON COLUMN litemall_address.USER_ID IS '用户表的用户ID';
                    
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
                    
      COMMENT ON COLUMN litemall_aftersale.TYPE IS '售后类型，0是未收货退款，1是已收货（无需退货）退款，2用户退货退款';
                    
      COMMENT ON COLUMN litemall_aftersale.REASON IS '退款原因';
                    
      COMMENT ON COLUMN litemall_aftersale.AMOUNT IS '退款金额';
                    
      COMMENT ON COLUMN litemall_aftersale.PICTURES IS '退款凭证图片链接数组';
                    
      COMMENT ON COLUMN litemall_aftersale.COMMENT IS '退款说明';
                    
      COMMENT ON COLUMN litemall_aftersale.STATUS IS '售后状态，0是可申请，1是用户已申请，2是管理员审核通过，3是管理员退款成功，4是管理员审核拒绝，5是用户已取消';
                    
      COMMENT ON COLUMN litemall_aftersale.HANDLE_TIME IS '管理员操作时间';
                    
      COMMENT ON COLUMN litemall_aftersale.ADD_TIME IS '添加时间';
                    
      COMMENT ON COLUMN litemall_aftersale.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_aftersale.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_brand IS '品牌商表';
                
      COMMENT ON COLUMN litemall_brand.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_brand.NAME IS '品牌商名称';
                    
      COMMENT ON COLUMN litemall_brand."DESC" IS '品牌商简介';
                    
      COMMENT ON COLUMN litemall_brand.PIC_URL IS '品牌商页的品牌商图片';
                    
      COMMENT ON COLUMN litemall_brand.SORT_ORDER IS 'Sortorder';
                    
      COMMENT ON COLUMN litemall_brand.FLOOR_PRICE IS '品牌商的商品低价，仅用于页面展示';
                    
      COMMENT ON COLUMN litemall_brand.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_brand.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_brand.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_cart IS '购物车商品表';
                
      COMMENT ON COLUMN litemall_cart.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_cart.USER_ID IS '用户表的用户ID';
                    
      COMMENT ON COLUMN litemall_cart.GOODS_ID IS '商品表的商品ID';
                    
      COMMENT ON COLUMN litemall_cart.GOODS_SN IS '商品编号';
                    
      COMMENT ON COLUMN litemall_cart.GOODS_NAME IS '商品名称';
                    
      COMMENT ON COLUMN litemall_cart.PRODUCT_ID IS '商品货品表的货品ID';
                    
      COMMENT ON COLUMN litemall_cart.PRICE IS '商品货品的价格';
                    
      COMMENT ON COLUMN litemall_cart.NUMBER IS '商品货品的数量';
                    
      COMMENT ON COLUMN litemall_cart.SPECIFICATIONS IS '商品规格值列表，采用JSON数组格式';
                    
      COMMENT ON COLUMN litemall_cart.CHECKED IS '购物车中商品是否选择状态';
                    
      COMMENT ON COLUMN litemall_cart.PIC_URL IS '商品图片或者商品货品图片';
                    
      COMMENT ON COLUMN litemall_cart.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_cart.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_cart.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_category IS '类目表';
                
      COMMENT ON COLUMN litemall_category.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_category.NAME IS '类目名称';
                    
      COMMENT ON COLUMN litemall_category.KEYWORDS IS '类目关键字，以JSON数组格式';
                    
      COMMENT ON COLUMN litemall_category."DESC" IS '类目广告语介绍';
                    
      COMMENT ON COLUMN litemall_category.PID IS '父类目ID';
                    
      COMMENT ON COLUMN litemall_category.ICON_URL IS '类目图标';
                    
      COMMENT ON COLUMN litemall_category.PIC_URL IS '类目图片';
                    
      COMMENT ON COLUMN litemall_category.LEVEL IS 'Level';
                    
      COMMENT ON COLUMN litemall_category.SORT_ORDER IS '排序';
                    
      COMMENT ON COLUMN litemall_category.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_category.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_category.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_collect IS '收藏表';
                
      COMMENT ON COLUMN litemall_collect.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_collect.USER_ID IS '用户表的用户ID';
                    
      COMMENT ON COLUMN litemall_collect.VALUE_ID IS '如果type=0，则是商品ID；如果type=1，则是专题ID';
                    
      COMMENT ON COLUMN litemall_collect.TYPE IS '收藏类型，如果type=0，则是商品ID；如果type=1，则是专题ID';
                    
      COMMENT ON COLUMN litemall_collect.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_collect.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_collect.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_comment IS '评论表';
                
      COMMENT ON COLUMN litemall_comment.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_comment.VALUE_ID IS '如果type=0，则是商品评论；如果是type=1，则是专题评论。';
                    
      COMMENT ON COLUMN litemall_comment.TYPE IS '评论类型，如果type=0，则是商品评论；如果是type=1，则是专题评论；';
                    
      COMMENT ON COLUMN litemall_comment.CONTENT IS '评论内容';
                    
      COMMENT ON COLUMN litemall_comment.ADMIN_CONTENT IS '管理员回复内容';
                    
      COMMENT ON COLUMN litemall_comment.USER_ID IS '用户表的用户ID';
                    
      COMMENT ON COLUMN litemall_comment.HAS_PICTURE IS '是否含有图片';
                    
      COMMENT ON COLUMN litemall_comment.PIC_URLS IS '图片地址列表，采用JSON数组格式';
                    
      COMMENT ON COLUMN litemall_comment.STAR IS '评分， 1-5';
                    
      COMMENT ON COLUMN litemall_comment.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_comment.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_comment.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_coupon IS '优惠券信息及规则表';
                
      COMMENT ON COLUMN litemall_coupon.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_coupon.NAME IS '优惠券名称';
                    
      COMMENT ON COLUMN litemall_coupon."DESC" IS '优惠券介绍，通常是显示优惠券使用限制文字';
                    
      COMMENT ON COLUMN litemall_coupon.TAG IS '优惠券标签，例如新人专用';
                    
      COMMENT ON COLUMN litemall_coupon.TOTAL IS '优惠券数量，如果是0，则是无限量';
                    
      COMMENT ON COLUMN litemall_coupon.DISCOUNT IS '优惠金额，';
                    
      COMMENT ON COLUMN litemall_coupon.MIN IS '最少消费金额才能使用优惠券。';
                    
      COMMENT ON COLUMN litemall_coupon."LIMIT" IS '用户领券限制数量，如果是0，则是不限制；默认是1，限领一张.';
                    
      COMMENT ON COLUMN litemall_coupon.TYPE IS '优惠券赠送类型，如果是0则通用券，用户领取；如果是1，则是注册赠券；如果是2，则是优惠券码兑换；';
                    
      COMMENT ON COLUMN litemall_coupon.STATUS IS '优惠券状态，如果是0则是正常可用；如果是1则是过期; 如果是2则是下架。';
                    
      COMMENT ON COLUMN litemall_coupon.GOODS_TYPE IS '商品限制类型，如果0则全商品，如果是1则是类目限制，如果是2则是商品限制。';
                    
      COMMENT ON COLUMN litemall_coupon.GOODS_VALUE IS '商品限制值，goods_type如果是0则空集合，如果是1则是类目集合，如果是2则是商品集合。';
                    
      COMMENT ON COLUMN litemall_coupon.CODE IS '优惠券兑换码';
                    
      COMMENT ON COLUMN litemall_coupon.TIME_TYPE IS '有效时间限制，如果是0，则基于领取时间的有效天数days；如果是1，则start_time和end_time是优惠券有效期；';
                    
      COMMENT ON COLUMN litemall_coupon.DAYS IS '基于领取时间的有效天数days。';
                    
      COMMENT ON COLUMN litemall_coupon.START_TIME IS '使用券开始时间';
                    
      COMMENT ON COLUMN litemall_coupon.END_TIME IS '使用券截至时间';
                    
      COMMENT ON COLUMN litemall_coupon.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_coupon.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_coupon.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_coupon_user IS '优惠券用户使用表';
                
      COMMENT ON COLUMN litemall_coupon_user.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_coupon_user.USER_ID IS '用户ID';
                    
      COMMENT ON COLUMN litemall_coupon_user.COUPON_ID IS '优惠券ID';
                    
      COMMENT ON COLUMN litemall_coupon_user.STATUS IS '使用状态, 如果是0则未使用；如果是1则已使用；如果是2则已过期；如果是3则已经下架；';
                    
      COMMENT ON COLUMN litemall_coupon_user.USED_TIME IS '使用时间';
                    
      COMMENT ON COLUMN litemall_coupon_user.START_TIME IS '有效期开始时间';
                    
      COMMENT ON COLUMN litemall_coupon_user.END_TIME IS '有效期截至时间';
                    
      COMMENT ON COLUMN litemall_coupon_user.ORDER_ID IS '订单ID';
                    
      COMMENT ON COLUMN litemall_coupon_user.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_coupon_user.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_coupon_user.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_feedback IS '意见反馈表';
                
      COMMENT ON COLUMN litemall_feedback.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_feedback.USER_ID IS '用户表的用户ID';
                    
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
                    
      COMMENT ON COLUMN litemall_footprint.USER_ID IS '用户表的用户ID';
                    
      COMMENT ON COLUMN litemall_footprint.GOODS_ID IS '浏览商品ID';
                    
      COMMENT ON COLUMN litemall_footprint.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_footprint.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_footprint.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_goods IS '商品基本信息表';
                
      COMMENT ON COLUMN litemall_goods.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_goods.GOODS_SN IS '商品编号';
                    
      COMMENT ON COLUMN litemall_goods.NAME IS '商品名称';
                    
      COMMENT ON COLUMN litemall_goods.CATEGORY_ID IS '商品所属类目ID';
                    
      COMMENT ON COLUMN litemall_goods.BRAND_ID IS 'Brandid';
                    
      COMMENT ON COLUMN litemall_goods.GALLERY IS '商品宣传图片列表，采用JSON数组格式';
                    
      COMMENT ON COLUMN litemall_goods.KEYWORDS IS '商品关键字，采用逗号间隔';
                    
      COMMENT ON COLUMN litemall_goods.BRIEF IS '商品简介';
                    
      COMMENT ON COLUMN litemall_goods.IS_ON_SALE IS '是否上架';
                    
      COMMENT ON COLUMN litemall_goods.SORT_ORDER IS 'Sortorder';
                    
      COMMENT ON COLUMN litemall_goods.PIC_URL IS '商品页面商品图片';
                    
      COMMENT ON COLUMN litemall_goods.SHARE_URL IS '商品分享海报';
                    
      COMMENT ON COLUMN litemall_goods.IS_NEW IS '是否新品首发，如果设置则可以在新品首发页面展示';
                    
      COMMENT ON COLUMN litemall_goods.IS_HOT IS '是否人气推荐，如果设置则可以在人气推荐页面展示';
                    
      COMMENT ON COLUMN litemall_goods.UNIT IS '商品单位，例如件、盒';
                    
      COMMENT ON COLUMN litemall_goods.COUNTER_PRICE IS '专柜价格';
                    
      COMMENT ON COLUMN litemall_goods.RETAIL_PRICE IS '零售价格';
                    
      COMMENT ON COLUMN litemall_goods.DETAIL IS '商品详细介绍，是富文本格式';
                    
      COMMENT ON COLUMN litemall_goods.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_goods.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_goods.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_goods_attribute IS '商品参数表';
                
      COMMENT ON COLUMN litemall_goods_attribute.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_goods_attribute.GOODS_ID IS '商品表的商品ID';
                    
      COMMENT ON COLUMN litemall_goods_attribute.ATTRIBUTE IS '商品参数名称';
                    
      COMMENT ON COLUMN litemall_goods_attribute.VALUE IS '商品参数值';
                    
      COMMENT ON COLUMN litemall_goods_attribute.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_goods_attribute.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_goods_attribute.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_goods_product IS '商品货品表';
                
      COMMENT ON COLUMN litemall_goods_product.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_goods_product.GOODS_ID IS '商品表的商品ID';
                    
      COMMENT ON COLUMN litemall_goods_product.SPECIFICATIONS IS '商品规格值列表，采用JSON数组格式';
                    
      COMMENT ON COLUMN litemall_goods_product.PRICE IS '商品货品价格';
                    
      COMMENT ON COLUMN litemall_goods_product.NUMBER IS '商品货品数量';
                    
      COMMENT ON COLUMN litemall_goods_product.URL IS '商品货品图片';
                    
      COMMENT ON COLUMN litemall_goods_product.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_goods_product.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_goods_product.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_goods_specification IS '商品规格表';
                
      COMMENT ON COLUMN litemall_goods_specification.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_goods_specification.GOODS_ID IS '商品表的商品ID';
                    
      COMMENT ON COLUMN litemall_goods_specification.SPECIFICATION IS '商品规格名称';
                    
      COMMENT ON COLUMN litemall_goods_specification.VALUE IS '商品规格值';
                    
      COMMENT ON COLUMN litemall_goods_specification.PIC_URL IS '商品规格图片';
                    
      COMMENT ON COLUMN litemall_goods_specification.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_goods_specification.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_goods_specification.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_groupon IS '团购活动表';
                
      COMMENT ON COLUMN litemall_groupon.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_groupon.ORDER_ID IS '关联的订单ID';
                    
      COMMENT ON COLUMN litemall_groupon.GROUPON_ID IS '如果是开团用户，则groupon_id是0；如果是参团用户，则groupon_id是团购活动ID';
                    
      COMMENT ON COLUMN litemall_groupon.RULES_ID IS '团购规则ID，关联litemall_groupon_rules表ID字段';
                    
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
                    
      COMMENT ON COLUMN litemall_groupon_rules.GOODS_ID IS '商品表的商品ID';
                    
      COMMENT ON COLUMN litemall_groupon_rules.GOODS_NAME IS '商品名称';
                    
      COMMENT ON COLUMN litemall_groupon_rules.PIC_URL IS '商品图片或者商品货品图片';
                    
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
                    
      COMMENT ON COLUMN litemall_log.RESULT IS '操作结果，或者成功消息，或者失败消息';
                    
      COMMENT ON COLUMN litemall_log.COMMENT IS '补充信息';
                    
      COMMENT ON COLUMN litemall_log.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_log.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_log.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_notice IS '通知表';
                
      COMMENT ON COLUMN litemall_notice.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_notice.TITLE IS '通知标题';
                    
      COMMENT ON COLUMN litemall_notice.CONTENT IS '通知内容';
                    
      COMMENT ON COLUMN litemall_notice.ADMIN_ID IS '创建通知的管理员ID，如果是系统内置通知则是0.';
                    
      COMMENT ON COLUMN litemall_notice.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_notice.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_notice.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_notice_admin IS '通知管理员表';
                
      COMMENT ON COLUMN litemall_notice_admin.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_notice_admin.NOTICE_ID IS '通知ID';
                    
      COMMENT ON COLUMN litemall_notice_admin.NOTICE_TITLE IS '通知标题';
                    
      COMMENT ON COLUMN litemall_notice_admin.ADMIN_ID IS '接收通知的管理员ID';
                    
      COMMENT ON COLUMN litemall_notice_admin.READ_TIME IS '阅读时间，如果是NULL则是未读状态';
                    
      COMMENT ON COLUMN litemall_notice_admin.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_notice_admin.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_notice_admin.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_order IS '订单表';
                
      COMMENT ON COLUMN litemall_order.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_order.USER_ID IS '用户表的用户ID';
                    
      COMMENT ON COLUMN litemall_order.ORDER_SN IS '订单编号';
                    
      COMMENT ON COLUMN litemall_order.ORDER_STATUS IS '订单状态';
                    
      COMMENT ON COLUMN litemall_order.AFTERSALE_STATUS IS '售后状态，0是可申请，1是用户已申请，2是管理员审核通过，3是管理员退款成功，4是管理员审核拒绝，5是用户已取消';
                    
      COMMENT ON COLUMN litemall_order.CONSIGNEE IS '收货人名称';
                    
      COMMENT ON COLUMN litemall_order.MOBILE IS '收货人手机号';
                    
      COMMENT ON COLUMN litemall_order.ADDRESS IS '收货具体地址';
                    
      COMMENT ON COLUMN litemall_order.MESSAGE IS '用户订单留言';
                    
      COMMENT ON COLUMN litemall_order.GOODS_PRICE IS '商品总费用';
                    
      COMMENT ON COLUMN litemall_order.FREIGHT_PRICE IS '配送费用';
                    
      COMMENT ON COLUMN litemall_order.COUPON_PRICE IS '优惠券减免';
                    
      COMMENT ON COLUMN litemall_order.INTEGRAL_PRICE IS '用户积分减免';
                    
      COMMENT ON COLUMN litemall_order.GROUPON_PRICE IS '团购优惠价减免';
                    
      COMMENT ON COLUMN litemall_order.ORDER_PRICE IS '订单费用， = goods_price + freight_price - coupon_price';
                    
      COMMENT ON COLUMN litemall_order.ACTUAL_PRICE IS '实付费用， = order_price - integral_price';
                    
      COMMENT ON COLUMN litemall_order.PAY_ID IS '微信付款编号';
                    
      COMMENT ON COLUMN litemall_order.PAY_TIME IS '微信付款时间';
                    
      COMMENT ON COLUMN litemall_order.SHIP_SN IS '发货编号';
                    
      COMMENT ON COLUMN litemall_order.SHIP_CHANNEL IS '发货快递公司';
                    
      COMMENT ON COLUMN litemall_order.SHIP_TIME IS '发货开始时间';
                    
      COMMENT ON COLUMN litemall_order.REFUND_AMOUNT IS '实际退款金额，（有可能退款金额小于实际支付金额）';
                    
      COMMENT ON COLUMN litemall_order.REFUND_TYPE IS '退款方式';
                    
      COMMENT ON COLUMN litemall_order.REFUND_CONTENT IS '退款备注';
                    
      COMMENT ON COLUMN litemall_order.REFUND_TIME IS '退款时间';
                    
      COMMENT ON COLUMN litemall_order.CONFIRM_TIME IS '用户确认收货时间';
                    
      COMMENT ON COLUMN litemall_order.COMMENTS IS '待评价订单商品数量';
                    
      COMMENT ON COLUMN litemall_order.END_TIME IS '订单关闭时间';
                    
      COMMENT ON COLUMN litemall_order.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_order.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_order.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_order_goods IS '订单商品表';
                
      COMMENT ON COLUMN litemall_order_goods.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_order_goods.ORDER_ID IS '订单表的订单ID';
                    
      COMMENT ON COLUMN litemall_order_goods.GOODS_ID IS '商品表的商品ID';
                    
      COMMENT ON COLUMN litemall_order_goods.GOODS_NAME IS '商品名称';
                    
      COMMENT ON COLUMN litemall_order_goods.GOODS_SN IS '商品编号';
                    
      COMMENT ON COLUMN litemall_order_goods.PRODUCT_ID IS '商品货品表的货品ID';
                    
      COMMENT ON COLUMN litemall_order_goods.NUMBER IS '商品货品的购买数量';
                    
      COMMENT ON COLUMN litemall_order_goods.PRICE IS '商品货品的售价';
                    
      COMMENT ON COLUMN litemall_order_goods.SPECIFICATIONS IS '商品货品的规格列表';
                    
      COMMENT ON COLUMN litemall_order_goods.PIC_URL IS '商品货品图片或者商品图片';
                    
      COMMENT ON COLUMN litemall_order_goods.COMMENT IS '订单商品评论，如果是-1，则超期不能评价；如果是0，则可以评价；如果其他值，则是comment表里面的评论ID。';
                    
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
                    
      COMMENT ON COLUMN litemall_region.PID IS '行政区域父ID，例如区县的pid指向市，市的pid指向省，省的pid则是0';
                    
      COMMENT ON COLUMN litemall_region.NAME IS '行政区域名称';
                    
      COMMENT ON COLUMN litemall_region.TYPE IS '行政区域类型，如如1则是省， 如果是2则是市，如果是3则是区县';
                    
      COMMENT ON COLUMN litemall_region.CODE IS '行政区域编码';
                    
      COMMENT ON TABLE litemall_role IS '角色表';
                
      COMMENT ON COLUMN litemall_role.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_role.NAME IS '角色名称';
                    
      COMMENT ON COLUMN litemall_role."DESC" IS '角色描述';
                    
      COMMENT ON COLUMN litemall_role.ENABLED IS '是否启用';
                    
      COMMENT ON COLUMN litemall_role.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_role.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_role.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_search_history IS '搜索历史表';
                
      COMMENT ON COLUMN litemall_search_history.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_search_history.USER_ID IS '用户表的用户ID';
                    
      COMMENT ON COLUMN litemall_search_history.KEYWORD IS '搜索关键字';
                    
      COMMENT ON COLUMN litemall_search_history."FROM" IS '搜索来源，如pc、wx、app';
                    
      COMMENT ON COLUMN litemall_search_history.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_search_history.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_search_history.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_storage IS '文件存储表';
                
      COMMENT ON COLUMN litemall_storage.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_storage.KEY IS '文件的唯一索引';
                    
      COMMENT ON COLUMN litemall_storage.NAME IS '文件名';
                    
      COMMENT ON COLUMN litemall_storage.TYPE IS '文件类型';
                    
      COMMENT ON COLUMN litemall_storage.SIZE IS '文件大小';
                    
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
                    
      COMMENT ON COLUMN litemall_topic.CONTENT IS '专题内容，富文本格式';
                    
      COMMENT ON COLUMN litemall_topic.PRICE IS '专题相关商品最低价';
                    
      COMMENT ON COLUMN litemall_topic.READ_COUNT IS '专题阅读量';
                    
      COMMENT ON COLUMN litemall_topic.PIC_URL IS '专题图片';
                    
      COMMENT ON COLUMN litemall_topic.SORT_ORDER IS '排序';
                    
      COMMENT ON COLUMN litemall_topic.GOODS IS '专题相关商品，采用JSON数组格式';
                    
      COMMENT ON COLUMN litemall_topic.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_topic.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_topic.DELETED IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_user IS '用户表';
                
      COMMENT ON COLUMN litemall_user.ID IS 'Id';
                    
      COMMENT ON COLUMN litemall_user.USERNAME IS '用户名称';
                    
      COMMENT ON COLUMN litemall_user.PASSWORD IS '用户密码';
                    
      COMMENT ON COLUMN litemall_user.GENDER IS '性别：0 未知， 1男， 1 女';
                    
      COMMENT ON COLUMN litemall_user.BIRTHDAY IS '生日';
                    
      COMMENT ON COLUMN litemall_user.LAST_LOGIN_TIME IS '最近一次登录时间';
                    
      COMMENT ON COLUMN litemall_user.LAST_LOGIN_IP IS '最近一次登录IP地址';
                    
      COMMENT ON COLUMN litemall_user.USER_LEVEL IS '0 普通用户，1 VIP用户，2 高级VIP用户';
                    
      COMMENT ON COLUMN litemall_user.NICKNAME IS '用户昵称或网络名称';
                    
      COMMENT ON COLUMN litemall_user.MOBILE IS '用户手机号码';
                    
      COMMENT ON COLUMN litemall_user.AVATAR IS '用户头像图片';
                    
      COMMENT ON COLUMN litemall_user.WEIXIN_OPENID IS '微信登录openid';
                    
      COMMENT ON COLUMN litemall_user.SESSION_KEY IS '微信登录会话KEY';
                    
      COMMENT ON COLUMN litemall_user.STATUS IS '0 可用, 1 禁用, 2 注销';
                    
      COMMENT ON COLUMN litemall_user.ADD_TIME IS '创建时间';
                    
      COMMENT ON COLUMN litemall_user.UPDATE_TIME IS '更新时间';
                    
      COMMENT ON COLUMN litemall_user.DELETED IS '逻辑删除';
                    