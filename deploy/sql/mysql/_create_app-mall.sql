
CREATE TABLE litemall_ad(
  ID INTEGER NOT NULL    COMMENT 'Id',
  NAME VARCHAR(63) NOT NULL    COMMENT '广告标题',
  LINK VARCHAR(255) NOT NULL    COMMENT '所广告的商品页面或者活动页面链接地址',
  URL VARCHAR(255) NOT NULL    COMMENT '广告宣传图片',
  POSITION TINYINT     COMMENT '广告位置：1则是首页',
  CONTENT VARCHAR(255)     COMMENT '活动内容',
  START_TIME DATETIME     COMMENT '广告开始时间',
  END_TIME DATETIME     COMMENT '广告结束时间',
  ENABLED BOOLEAN     COMMENT '是否启动',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_ad primary key (ID)
);

CREATE TABLE litemall_address(
  ID INTEGER NOT NULL    COMMENT 'Id',
  NAME VARCHAR(63) NOT NULL    COMMENT '收货人名称',
  USER_ID INTEGER NOT NULL    COMMENT '用户表的用户ID',
  PROVINCE VARCHAR(63) NOT NULL    COMMENT '行政区域表的省ID',
  CITY VARCHAR(63) NOT NULL    COMMENT '行政区域表的市ID',
  COUNTY VARCHAR(63) NOT NULL    COMMENT '行政区域表的区县ID',
  ADDRESS_DETAIL VARCHAR(127) NOT NULL    COMMENT '详细收货地址',
  AREA_CODE CHAR(6)     COMMENT '地区编码',
  POSTAL_CODE CHAR(6)     COMMENT '邮政编码',
  TEL VARCHAR(20) NOT NULL    COMMENT '手机号码',
  IS_DEFAULT BOOLEAN NOT NULL    COMMENT '是否默认地址',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_address primary key (ID)
);

CREATE TABLE litemall_admin(
  ID INTEGER NOT NULL    COMMENT 'Id',
  USERNAME VARCHAR(63) NOT NULL    COMMENT '管理员名称',
  PASSWORD VARCHAR(63) NOT NULL    COMMENT '管理员密码',
  LAST_LOGIN_IP VARCHAR(63)     COMMENT '最近一次登录IP地址',
  LAST_LOGIN_TIME DATETIME     COMMENT '最近一次登录时间',
  AVATAR VARCHAR(255)     COMMENT '头像图片',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  ROLE_IDS VARCHAR(127)     COMMENT '角色列表',
  constraint PK_litemall_admin primary key (ID)
);

CREATE TABLE litemall_aftersale(
  ID INTEGER NOT NULL    COMMENT 'Id',
  AFTERSALE_SN VARCHAR(63)     COMMENT '售后编号',
  ORDER_ID INTEGER NOT NULL    COMMENT '订单ID',
  USER_ID INTEGER NOT NULL    COMMENT '用户ID',
  TYPE SMALLINT     COMMENT '售后类型，0是未收货退款，1是已收货（无需退货）退款，2用户退货退款',
  REASON VARCHAR(31)     COMMENT '退款原因',
  AMOUNT DECIMAL(10,2)     COMMENT '退款金额',
  PICTURES VARCHAR(1023)     COMMENT '退款凭证图片链接数组',
  COMMENT VARCHAR(511)     COMMENT '退款说明',
  STATUS SMALLINT     COMMENT '售后状态，0是可申请，1是用户已申请，2是管理员审核通过，3是管理员退款成功，4是管理员审核拒绝，5是用户已取消',
  HANDLE_TIME DATETIME     COMMENT '管理员操作时间',
  ADD_TIME DATETIME     COMMENT '添加时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_aftersale primary key (ID)
);

CREATE TABLE litemall_brand(
  ID INTEGER NOT NULL    COMMENT 'Id',
  NAME VARCHAR(255) NOT NULL    COMMENT '品牌商名称',
  `DESC` VARCHAR(255) NOT NULL    COMMENT '品牌商简介',
  PIC_URL VARCHAR(255) NOT NULL    COMMENT '品牌商页的品牌商图片',
  SORT_ORDER TINYINT     COMMENT 'Sortorder',
  FLOOR_PRICE DECIMAL(10,2)     COMMENT '品牌商的商品低价，仅用于页面展示',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_brand primary key (ID)
);

CREATE TABLE litemall_cart(
  ID INTEGER NOT NULL    COMMENT 'Id',
  USER_ID INTEGER     COMMENT '用户表的用户ID',
  GOODS_ID INTEGER     COMMENT '商品表的商品ID',
  GOODS_SN VARCHAR(63)     COMMENT '商品编号',
  GOODS_NAME VARCHAR(127)     COMMENT '商品名称',
  PRODUCT_ID INTEGER     COMMENT '商品货品表的货品ID',
  PRICE DECIMAL(10,2)     COMMENT '商品货品的价格',
  NUMBER SMALLINT     COMMENT '商品货品的数量',
  SPECIFICATIONS VARCHAR(1023)     COMMENT '商品规格值列表，采用JSON数组格式',
  CHECKED BOOLEAN     COMMENT '购物车中商品是否选择状态',
  PIC_URL VARCHAR(255)     COMMENT '商品图片或者商品货品图片',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_cart primary key (ID)
);

CREATE TABLE litemall_category(
  ID INTEGER NOT NULL    COMMENT 'Id',
  NAME VARCHAR(63) NOT NULL    COMMENT '类目名称',
  KEYWORDS VARCHAR(1023) NOT NULL    COMMENT '类目关键字，以JSON数组格式',
  `DESC` VARCHAR(255)     COMMENT '类目广告语介绍',
  PID INTEGER NOT NULL    COMMENT '父类目ID',
  ICON_URL VARCHAR(255)     COMMENT '类目图标',
  PIC_URL VARCHAR(255)     COMMENT '类目图片',
  LEVEL VARCHAR(255)     COMMENT 'Level',
  SORT_ORDER TINYINT     COMMENT '排序',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_category primary key (ID)
);

CREATE TABLE litemall_collect(
  ID INTEGER NOT NULL    COMMENT 'Id',
  USER_ID INTEGER NOT NULL    COMMENT '用户表的用户ID',
  VALUE_ID INTEGER NOT NULL    COMMENT '如果type=0，则是商品ID；如果type=1，则是专题ID',
  TYPE TINYINT NOT NULL    COMMENT '收藏类型，如果type=0，则是商品ID；如果type=1，则是专题ID',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_collect primary key (ID)
);

CREATE TABLE litemall_comment(
  ID INTEGER NOT NULL    COMMENT 'Id',
  VALUE_ID INTEGER NOT NULL    COMMENT '如果type=0，则是商品评论；如果是type=1，则是专题评论。',
  TYPE TINYINT NOT NULL    COMMENT '评论类型，如果type=0，则是商品评论；如果是type=1，则是专题评论；',
  CONTENT VARCHAR(1023)     COMMENT '评论内容',
  ADMIN_CONTENT VARCHAR(511)     COMMENT '管理员回复内容',
  USER_ID INTEGER NOT NULL    COMMENT '用户表的用户ID',
  HAS_PICTURE BOOLEAN     COMMENT '是否含有图片',
  PIC_URLS VARCHAR(1023)     COMMENT '图片地址列表，采用JSON数组格式',
  STAR SMALLINT     COMMENT '评分， 1-5',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_comment primary key (ID)
);

CREATE TABLE litemall_coupon(
  ID INTEGER NOT NULL    COMMENT 'Id',
  NAME VARCHAR(63) NOT NULL    COMMENT '优惠券名称',
  `DESC` VARCHAR(127)     COMMENT '优惠券介绍，通常是显示优惠券使用限制文字',
  TAG VARCHAR(63)     COMMENT '优惠券标签，例如新人专用',
  TOTAL INTEGER NOT NULL    COMMENT '优惠券数量，如果是0，则是无限量',
  DISCOUNT DECIMAL(10,2)     COMMENT '优惠金额，',
  MIN DECIMAL(10,2)     COMMENT '最少消费金额才能使用优惠券。',
  `LIMIT` SMALLINT     COMMENT '用户领券限制数量，如果是0，则是不限制；默认是1，限领一张.',
  TYPE SMALLINT     COMMENT '优惠券赠送类型，如果是0则通用券，用户领取；如果是1，则是注册赠券；如果是2，则是优惠券码兑换；',
  STATUS SMALLINT     COMMENT '优惠券状态，如果是0则是正常可用；如果是1则是过期; 如果是2则是下架。',
  GOODS_TYPE SMALLINT     COMMENT '商品限制类型，如果0则全商品，如果是1则是类目限制，如果是2则是商品限制。',
  GOODS_VALUE VARCHAR(1023)     COMMENT '商品限制值，goods_type如果是0则空集合，如果是1则是类目集合，如果是2则是商品集合。',
  CODE VARCHAR(63)     COMMENT '优惠券兑换码',
  TIME_TYPE SMALLINT     COMMENT '有效时间限制，如果是0，则基于领取时间的有效天数days；如果是1，则start_time和end_time是优惠券有效期；',
  DAYS SMALLINT     COMMENT '基于领取时间的有效天数days。',
  START_TIME DATETIME     COMMENT '使用券开始时间',
  END_TIME DATETIME     COMMENT '使用券截至时间',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_coupon primary key (ID)
);

CREATE TABLE litemall_coupon_user(
  ID INTEGER NOT NULL    COMMENT 'Id',
  USER_ID INTEGER NOT NULL    COMMENT '用户ID',
  COUPON_ID INTEGER NOT NULL    COMMENT '优惠券ID',
  STATUS SMALLINT     COMMENT '使用状态, 如果是0则未使用；如果是1则已使用；如果是2则已过期；如果是3则已经下架；',
  USED_TIME DATETIME     COMMENT '使用时间',
  START_TIME DATETIME     COMMENT '有效期开始时间',
  END_TIME DATETIME     COMMENT '有效期截至时间',
  ORDER_ID INTEGER     COMMENT '订单ID',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_coupon_user primary key (ID)
);

CREATE TABLE litemall_feedback(
  ID INTEGER NOT NULL    COMMENT 'Id',
  USER_ID INTEGER NOT NULL    COMMENT '用户表的用户ID',
  USERNAME VARCHAR(63) NOT NULL    COMMENT '用户名称',
  MOBILE VARCHAR(20) NOT NULL    COMMENT '手机号',
  FEED_TYPE VARCHAR(63) NOT NULL    COMMENT '反馈类型',
  CONTENT VARCHAR(1023) NOT NULL    COMMENT '反馈内容',
  STATUS INTEGER NOT NULL    COMMENT '状态',
  HAS_PICTURE BOOLEAN     COMMENT '是否含有图片',
  PIC_URLS VARCHAR(1023)     COMMENT '图片地址列表，采用JSON数组格式',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_feedback primary key (ID)
);

CREATE TABLE litemall_footprint(
  ID INTEGER NOT NULL    COMMENT 'Id',
  USER_ID INTEGER NOT NULL    COMMENT '用户表的用户ID',
  GOODS_ID INTEGER NOT NULL    COMMENT '浏览商品ID',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_footprint primary key (ID)
);

CREATE TABLE litemall_goods(
  ID INTEGER NOT NULL    COMMENT 'Id',
  GOODS_SN VARCHAR(63) NOT NULL    COMMENT '商品编号',
  NAME VARCHAR(127) NOT NULL    COMMENT '商品名称',
  CATEGORY_ID INTEGER     COMMENT '商品所属类目ID',
  BRAND_ID INTEGER     COMMENT 'Brandid',
  GALLERY VARCHAR(1023)     COMMENT '商品宣传图片列表，采用JSON数组格式',
  KEYWORDS VARCHAR(255)     COMMENT '商品关键字，采用逗号间隔',
  BRIEF VARCHAR(255)     COMMENT '商品简介',
  IS_ON_SALE BOOLEAN     COMMENT '是否上架',
  SORT_ORDER SMALLINT     COMMENT 'Sortorder',
  PIC_URL VARCHAR(255)     COMMENT '商品页面商品图片',
  SHARE_URL VARCHAR(255)     COMMENT '商品分享海报',
  IS_NEW BOOLEAN     COMMENT '是否新品首发，如果设置则可以在新品首发页面展示',
  IS_HOT BOOLEAN     COMMENT '是否人气推荐，如果设置则可以在人气推荐页面展示',
  UNIT VARCHAR(31)     COMMENT '商品单位，例如件、盒',
  COUNTER_PRICE DECIMAL(10,2)     COMMENT '专柜价格',
  RETAIL_PRICE DECIMAL(10,2)     COMMENT '零售价格',
  DETAIL TEXT     COMMENT '商品详细介绍，是富文本格式',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_goods primary key (ID)
);

CREATE TABLE litemall_goods_attribute(
  ID INTEGER NOT NULL    COMMENT 'Id',
  GOODS_ID INTEGER NOT NULL    COMMENT '商品表的商品ID',
  ATTRIBUTE VARCHAR(255) NOT NULL    COMMENT '商品参数名称',
  VALUE VARCHAR(255) NOT NULL    COMMENT '商品参数值',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_goods_attribute primary key (ID)
);

CREATE TABLE litemall_goods_product(
  ID INTEGER NOT NULL    COMMENT 'Id',
  GOODS_ID INTEGER NOT NULL    COMMENT '商品表的商品ID',
  SPECIFICATIONS VARCHAR(1023) NOT NULL    COMMENT '商品规格值列表，采用JSON数组格式',
  PRICE DECIMAL(10,2) NOT NULL    COMMENT '商品货品价格',
  NUMBER INTEGER NOT NULL    COMMENT '商品货品数量',
  URL VARCHAR(125)     COMMENT '商品货品图片',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_goods_product primary key (ID)
);

CREATE TABLE litemall_goods_specification(
  ID INTEGER NOT NULL    COMMENT 'Id',
  GOODS_ID INTEGER NOT NULL    COMMENT '商品表的商品ID',
  SPECIFICATION VARCHAR(255) NOT NULL    COMMENT '商品规格名称',
  VALUE VARCHAR(255) NOT NULL    COMMENT '商品规格值',
  PIC_URL VARCHAR(255) NOT NULL    COMMENT '商品规格图片',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_goods_specification primary key (ID)
);

CREATE TABLE litemall_groupon(
  ID INTEGER NOT NULL    COMMENT 'Id',
  ORDER_ID INTEGER NOT NULL    COMMENT '关联的订单ID',
  GROUPON_ID INTEGER     COMMENT '如果是开团用户，则groupon_id是0；如果是参团用户，则groupon_id是团购活动ID',
  RULES_ID INTEGER NOT NULL    COMMENT '团购规则ID，关联litemall_groupon_rules表ID字段',
  USER_ID INTEGER NOT NULL    COMMENT '用户ID',
  SHARE_URL VARCHAR(255)     COMMENT '团购分享图片地址',
  CREATOR_USER_ID INTEGER NOT NULL    COMMENT '开团用户ID',
  CREATOR_USER_TIME DATETIME     COMMENT '开团时间',
  STATUS SMALLINT     COMMENT '团购活动状态，开团未支付则0，开团中则1，开团失败则2',
  ADD_TIME DATETIME NOT NULL    COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_groupon primary key (ID)
);

CREATE TABLE litemall_groupon_rules(
  ID INTEGER NOT NULL    COMMENT 'Id',
  GOODS_ID INTEGER NOT NULL    COMMENT '商品表的商品ID',
  GOODS_NAME VARCHAR(127) NOT NULL    COMMENT '商品名称',
  PIC_URL VARCHAR(255)     COMMENT '商品图片或者商品货品图片',
  DISCOUNT DECIMAL(63,0) NOT NULL    COMMENT '优惠金额',
  DISCOUNT_MEMBER INTEGER NOT NULL    COMMENT '达到优惠条件的人数',
  EXPIRE_TIME DATETIME     COMMENT '团购过期时间',
  STATUS SMALLINT     COMMENT '团购规则状态，正常上线则0，到期自动下线则1，管理手动下线则2',
  ADD_TIME DATETIME NOT NULL    COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_groupon_rules primary key (ID)
);

CREATE TABLE litemall_issue(
  ID INTEGER NOT NULL    COMMENT 'Id',
  QUESTION VARCHAR(255)     COMMENT '问题标题',
  ANSWER VARCHAR(255)     COMMENT '问题答案',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_issue primary key (ID)
);

CREATE TABLE litemall_keyword(
  ID INTEGER NOT NULL    COMMENT 'Id',
  KEYWORD VARCHAR(127) NOT NULL    COMMENT '关键字',
  URL VARCHAR(255) NOT NULL    COMMENT '关键字的跳转链接',
  IS_HOT BOOLEAN NOT NULL    COMMENT '是否是热门关键字',
  IS_DEFAULT BOOLEAN NOT NULL    COMMENT '是否是默认关键字',
  SORT_ORDER INTEGER NOT NULL    COMMENT '排序',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_keyword primary key (ID)
);

CREATE TABLE litemall_log(
  ID INTEGER NOT NULL    COMMENT 'Id',
  ADMIN VARCHAR(45)     COMMENT '管理员',
  IP VARCHAR(45)     COMMENT '管理员地址',
  TYPE INTEGER     COMMENT '操作分类',
  ACTION VARCHAR(45)     COMMENT '操作动作',
  STATUS BOOLEAN     COMMENT '操作状态',
  RESULT VARCHAR(127)     COMMENT '操作结果，或者成功消息，或者失败消息',
  COMMENT VARCHAR(255)     COMMENT '补充信息',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_log primary key (ID)
);

CREATE TABLE litemall_notice(
  ID INTEGER NOT NULL    COMMENT 'Id',
  TITLE VARCHAR(63)     COMMENT '通知标题',
  CONTENT VARCHAR(511)     COMMENT '通知内容',
  ADMIN_ID INTEGER     COMMENT '创建通知的管理员ID，如果是系统内置通知则是0.',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_notice primary key (ID)
);

CREATE TABLE litemall_notice_admin(
  ID INTEGER NOT NULL    COMMENT 'Id',
  NOTICE_ID INTEGER     COMMENT '通知ID',
  NOTICE_TITLE VARCHAR(63)     COMMENT '通知标题',
  ADMIN_ID INTEGER     COMMENT '接收通知的管理员ID',
  READ_TIME DATETIME     COMMENT '阅读时间，如果是NULL则是未读状态',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_notice_admin primary key (ID)
);

CREATE TABLE litemall_order(
  ID INTEGER NOT NULL    COMMENT 'Id',
  USER_ID INTEGER NOT NULL    COMMENT '用户表的用户ID',
  ORDER_SN VARCHAR(63) NOT NULL    COMMENT '订单编号',
  ORDER_STATUS SMALLINT NOT NULL    COMMENT '订单状态',
  AFTERSALE_STATUS SMALLINT     COMMENT '售后状态，0是可申请，1是用户已申请，2是管理员审核通过，3是管理员退款成功，4是管理员审核拒绝，5是用户已取消',
  CONSIGNEE VARCHAR(63) NOT NULL    COMMENT '收货人名称',
  MOBILE VARCHAR(63) NOT NULL    COMMENT '收货人手机号',
  ADDRESS VARCHAR(127) NOT NULL    COMMENT '收货具体地址',
  MESSAGE VARCHAR(512) NOT NULL    COMMENT '用户订单留言',
  GOODS_PRICE DECIMAL(10,2) NOT NULL    COMMENT '商品总费用',
  FREIGHT_PRICE DECIMAL(10,2) NOT NULL    COMMENT '配送费用',
  COUPON_PRICE DECIMAL(10,2) NOT NULL    COMMENT '优惠券减免',
  INTEGRAL_PRICE DECIMAL(10,2) NOT NULL    COMMENT '用户积分减免',
  GROUPON_PRICE DECIMAL(10,2) NOT NULL    COMMENT '团购优惠价减免',
  ORDER_PRICE DECIMAL(10,2) NOT NULL    COMMENT '订单费用， = goods_price + freight_price - coupon_price',
  ACTUAL_PRICE DECIMAL(10,2) NOT NULL    COMMENT '实付费用， = order_price - integral_price',
  PAY_ID VARCHAR(63)     COMMENT '微信付款编号',
  PAY_TIME DATETIME     COMMENT '微信付款时间',
  SHIP_SN VARCHAR(63)     COMMENT '发货编号',
  SHIP_CHANNEL VARCHAR(63)     COMMENT '发货快递公司',
  SHIP_TIME DATETIME     COMMENT '发货开始时间',
  REFUND_AMOUNT DECIMAL(10,2)     COMMENT '实际退款金额，（有可能退款金额小于实际支付金额）',
  REFUND_TYPE VARCHAR(63)     COMMENT '退款方式',
  REFUND_CONTENT VARCHAR(127)     COMMENT '退款备注',
  REFUND_TIME DATETIME     COMMENT '退款时间',
  CONFIRM_TIME DATETIME     COMMENT '用户确认收货时间',
  COMMENTS SMALLINT     COMMENT '待评价订单商品数量',
  END_TIME DATETIME     COMMENT '订单关闭时间',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_order primary key (ID)
);

CREATE TABLE litemall_order_goods(
  ID INTEGER NOT NULL    COMMENT 'Id',
  ORDER_ID INTEGER NOT NULL    COMMENT '订单表的订单ID',
  GOODS_ID INTEGER NOT NULL    COMMENT '商品表的商品ID',
  GOODS_NAME VARCHAR(127) NOT NULL    COMMENT '商品名称',
  GOODS_SN VARCHAR(63) NOT NULL    COMMENT '商品编号',
  PRODUCT_ID INTEGER NOT NULL    COMMENT '商品货品表的货品ID',
  NUMBER SMALLINT NOT NULL    COMMENT '商品货品的购买数量',
  PRICE DECIMAL(10,2) NOT NULL    COMMENT '商品货品的售价',
  SPECIFICATIONS VARCHAR(1023) NOT NULL    COMMENT '商品货品的规格列表',
  PIC_URL VARCHAR(255) NOT NULL    COMMENT '商品货品图片或者商品图片',
  COMMENT INTEGER     COMMENT '订单商品评论，如果是-1，则超期不能评价；如果是0，则可以评价；如果其他值，则是comment表里面的评论ID。',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_order_goods primary key (ID)
);

CREATE TABLE litemall_permission(
  ID INTEGER NOT NULL    COMMENT 'Id',
  ROLE_ID INTEGER     COMMENT '角色ID',
  PERMISSION VARCHAR(63)     COMMENT '权限',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_permission primary key (ID)
);

CREATE TABLE litemall_region(
  ID INTEGER NOT NULL    COMMENT 'Id',
  PID INTEGER NOT NULL    COMMENT '行政区域父ID，例如区县的pid指向市，市的pid指向省，省的pid则是0',
  NAME VARCHAR(120) NOT NULL    COMMENT '行政区域名称',
  TYPE TINYINT NOT NULL    COMMENT '行政区域类型，如如1则是省， 如果是2则是市，如果是3则是区县',
  CODE INTEGER NOT NULL    COMMENT '行政区域编码',
  constraint PK_litemall_region primary key (ID)
);

CREATE TABLE litemall_role(
  ID INTEGER NOT NULL    COMMENT 'Id',
  NAME VARCHAR(63) NOT NULL    COMMENT '角色名称',
  `DESC` VARCHAR(1023)     COMMENT '角色描述',
  ENABLED BOOLEAN     COMMENT '是否启用',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_role primary key (ID)
);

CREATE TABLE litemall_search_history(
  ID INTEGER NOT NULL    COMMENT 'Id',
  USER_ID INTEGER NOT NULL    COMMENT '用户表的用户ID',
  KEYWORD VARCHAR(63) NOT NULL    COMMENT '搜索关键字',
  `FROM` VARCHAR(63) NOT NULL    COMMENT '搜索来源，如pc、wx、app',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_search_history primary key (ID)
);

CREATE TABLE litemall_storage(
  ID INTEGER NOT NULL    COMMENT 'Id',
  `KEY` VARCHAR(63) NOT NULL    COMMENT '文件的唯一索引',
  NAME VARCHAR(255) NOT NULL    COMMENT '文件名',
  TYPE VARCHAR(20) NOT NULL    COMMENT '文件类型',
  SIZE INTEGER NOT NULL    COMMENT '文件大小',
  URL VARCHAR(255)     COMMENT '文件访问链接',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_storage primary key (ID)
);

CREATE TABLE litemall_system(
  ID INTEGER NOT NULL    COMMENT 'Id',
  KEY_NAME VARCHAR(255) NOT NULL    COMMENT '系统配置名',
  KEY_VALUE VARCHAR(255) NOT NULL    COMMENT '系统配置值',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_system primary key (ID)
);

CREATE TABLE litemall_topic(
  ID INTEGER NOT NULL    COMMENT 'Id',
  TITLE VARCHAR(255) NOT NULL    COMMENT '专题标题',
  SUBTITLE VARCHAR(255)     COMMENT '专题子标题',
  CONTENT TEXT     COMMENT '专题内容，富文本格式',
  PRICE DECIMAL(10,2)     COMMENT '专题相关商品最低价',
  READ_COUNT VARCHAR(255)     COMMENT '专题阅读量',
  PIC_URL VARCHAR(255)     COMMENT '专题图片',
  SORT_ORDER INTEGER     COMMENT '排序',
  GOODS VARCHAR(1023)     COMMENT '专题相关商品，采用JSON数组格式',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_topic primary key (ID)
);

CREATE TABLE litemall_user(
  ID INTEGER NOT NULL    COMMENT 'Id',
  USERNAME VARCHAR(63) NOT NULL    COMMENT '用户名称',
  PASSWORD VARCHAR(63) NOT NULL    COMMENT '用户密码',
  GENDER TINYINT NOT NULL    COMMENT '性别：0 未知， 1男， 1 女',
  BIRTHDAY DATE     COMMENT '生日',
  LAST_LOGIN_TIME DATETIME     COMMENT '最近一次登录时间',
  LAST_LOGIN_IP VARCHAR(63) NOT NULL    COMMENT '最近一次登录IP地址',
  USER_LEVEL TINYINT     COMMENT '0 普通用户，1 VIP用户，2 高级VIP用户',
  NICKNAME VARCHAR(63) NOT NULL    COMMENT '用户昵称或网络名称',
  MOBILE VARCHAR(20) NOT NULL    COMMENT '用户手机号码',
  AVATAR VARCHAR(255) NOT NULL    COMMENT '用户头像图片',
  WEIXIN_OPENID VARCHAR(63) NOT NULL    COMMENT '微信登录openid',
  SESSION_KEY VARCHAR(100) NOT NULL    COMMENT '微信登录会话KEY',
  STATUS TINYINT NOT NULL    COMMENT '0 可用, 1 禁用, 2 注销',
  ADD_TIME DATETIME     COMMENT '创建时间',
  UPDATE_TIME DATETIME     COMMENT '更新时间',
  DELETED BOOLEAN     COMMENT '逻辑删除',
  constraint PK_litemall_user primary key (ID)
);


   ALTER TABLE litemall_ad COMMENT '广告表';
                
   ALTER TABLE litemall_address COMMENT '收货地址表';
                
   ALTER TABLE litemall_admin COMMENT '管理员表';
                
   ALTER TABLE litemall_aftersale COMMENT '售后表';
                
   ALTER TABLE litemall_brand COMMENT '品牌商表';
                
   ALTER TABLE litemall_cart COMMENT '购物车商品表';
                
   ALTER TABLE litemall_category COMMENT '类目表';
                
   ALTER TABLE litemall_collect COMMENT '收藏表';
                
   ALTER TABLE litemall_comment COMMENT '评论表';
                
   ALTER TABLE litemall_coupon COMMENT '优惠券信息及规则表';
                
   ALTER TABLE litemall_coupon_user COMMENT '优惠券用户使用表';
                
   ALTER TABLE litemall_feedback COMMENT '意见反馈表';
                
   ALTER TABLE litemall_footprint COMMENT '用户浏览足迹表';
                
   ALTER TABLE litemall_goods COMMENT '商品基本信息表';
                
   ALTER TABLE litemall_goods_attribute COMMENT '商品参数表';
                
   ALTER TABLE litemall_goods_product COMMENT '商品货品表';
                
   ALTER TABLE litemall_goods_specification COMMENT '商品规格表';
                
   ALTER TABLE litemall_groupon COMMENT '团购活动表';
                
   ALTER TABLE litemall_groupon_rules COMMENT '团购规则表';
                
   ALTER TABLE litemall_issue COMMENT '常见问题表';
                
   ALTER TABLE litemall_keyword COMMENT '关键字表';
                
   ALTER TABLE litemall_log COMMENT '操作日志表';
                
   ALTER TABLE litemall_notice COMMENT '通知表';
                
   ALTER TABLE litemall_notice_admin COMMENT '通知管理员表';
                
   ALTER TABLE litemall_order COMMENT '订单表';
                
   ALTER TABLE litemall_order_goods COMMENT '订单商品表';
                
   ALTER TABLE litemall_permission COMMENT '权限表';
                
   ALTER TABLE litemall_region COMMENT '行政区域表';
                
   ALTER TABLE litemall_role COMMENT '角色表';
                
   ALTER TABLE litemall_search_history COMMENT '搜索历史表';
                
   ALTER TABLE litemall_storage COMMENT '文件存储表';
                
   ALTER TABLE litemall_system COMMENT '系统配置表';
                
   ALTER TABLE litemall_topic COMMENT '专题表';
                
   ALTER TABLE litemall_user COMMENT '用户表';
                
