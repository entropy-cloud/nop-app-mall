
CREATE TABLE litemall_ad(
  id INT4 NOT NULL ,
  name VARCHAR(63) NOT NULL ,
  link VARCHAR(255) NOT NULL ,
  url VARCHAR(255) NOT NULL ,
  position INT4  ,
  content VARCHAR(255)  ,
  start_time TIMESTAMP  ,
  end_time TIMESTAMP  ,
  enabled BOOLEAN  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_ad primary key (id)
);

CREATE TABLE litemall_address(
  id INT4 NOT NULL ,
  name VARCHAR(63) NOT NULL ,
  user_id VARCHAR(50) NOT NULL ,
  province VARCHAR(63) NOT NULL ,
  city VARCHAR(63) NOT NULL ,
  county VARCHAR(63) NOT NULL ,
  address_detail VARCHAR(127) NOT NULL ,
  area_code CHAR(6)  ,
  postal_code CHAR(6)  ,
  tel VARCHAR(20) NOT NULL ,
  is_default BOOLEAN NOT NULL ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_address primary key (id)
);

CREATE TABLE litemall_brand(
  id INT4 NOT NULL ,
  name VARCHAR(255) NOT NULL ,
  pic_url VARCHAR(255) NOT NULL ,
  "desc" VARCHAR(255) NOT NULL ,
  sort_order INT4  ,
  floor_price NUMERIC(10,2)  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_brand primary key (id)
);

CREATE TABLE litemall_category(
  id INT4 NOT NULL ,
  name VARCHAR(63) NOT NULL ,
  icon_url VARCHAR(255)  ,
  pic_url VARCHAR(255)  ,
  keywords VARCHAR(1023)  ,
  "desc" VARCHAR(255)  ,
  level VARCHAR(255) NOT NULL ,
  pid INT4  ,
  sort_order INT4  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_category primary key (id)
);

CREATE TABLE litemall_collect(
  id INT4 NOT NULL ,
  user_id VARCHAR(50) NOT NULL ,
  value_id INT4 NOT NULL ,
  type INT4 NOT NULL ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_collect primary key (id)
);

CREATE TABLE litemall_topic(
  id INT4 NOT NULL ,
  title VARCHAR(255) NOT NULL ,
  subtitle VARCHAR(255)  ,
  content TEXT  ,
  price NUMERIC(10,2)  ,
  read_count INT4  ,
  pic_url VARCHAR(255)  ,
  sort_order INT4  ,
  goods VARCHAR(1023)  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  status INT4 default 0   ,
  constraint PK_litemall_topic primary key (id)
);

CREATE TABLE litemall_coupon(
  id INT4 NOT NULL ,
  name VARCHAR(63) NOT NULL ,
  "desc" VARCHAR(127)  ,
  tag VARCHAR(63)  ,
  total INT4 NOT NULL ,
  discount NUMERIC(10,2)  ,
  min NUMERIC(10,2)  ,
  "limit" INT4  ,
  type INT4  ,
  status INT4  ,
  goods_type INT4  ,
  goods_value VARCHAR(1023)  ,
  code VARCHAR(63)  ,
  time_type INT4  ,
  days INT4  ,
  start_time TIMESTAMP  ,
  end_time TIMESTAMP  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_coupon primary key (id)
);

CREATE TABLE litemall_feedback(
  id INT4 NOT NULL ,
  user_id VARCHAR(50) NOT NULL ,
  username VARCHAR(63) NOT NULL ,
  mobile VARCHAR(20) NOT NULL ,
  feed_type VARCHAR(63) NOT NULL ,
  content VARCHAR(1023) NOT NULL ,
  status INT4 NOT NULL ,
  has_picture BOOLEAN  ,
  pic_urls VARCHAR(1023)  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_feedback primary key (id)
);

CREATE TABLE litemall_issue(
  id INT4 NOT NULL ,
  question VARCHAR(255)  ,
  answer VARCHAR(255)  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_issue primary key (id)
);

CREATE TABLE litemall_keyword(
  id INT4 NOT NULL ,
  keyword VARCHAR(127) NOT NULL ,
  url VARCHAR(255) NOT NULL ,
  is_hot BOOLEAN NOT NULL ,
  is_default BOOLEAN NOT NULL ,
  sort_order INT4 NOT NULL ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_keyword primary key (id)
);

CREATE TABLE litemall_log(
  id INT4 NOT NULL ,
  admin VARCHAR(45)  ,
  ip VARCHAR(45)  ,
  type INT4  ,
  action VARCHAR(45)  ,
  status BOOLEAN  ,
  result VARCHAR(127)  ,
  comment VARCHAR(255)  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_log primary key (id)
);

CREATE TABLE litemall_notice(
  id INT4 NOT NULL ,
  title VARCHAR(63)  ,
  content VARCHAR(511)  ,
  admin_id INT4  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_notice primary key (id)
);

CREATE TABLE litemall_notice_admin(
  id INT4 NOT NULL ,
  notice_id INT4  ,
  notice_title VARCHAR(63)  ,
  admin_id INT4  ,
  read_time TIMESTAMP  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_notice_admin primary key (id)
);

CREATE TABLE litemall_pickup_store(
  id INT4 NOT NULL ,
  name VARCHAR(127) NOT NULL ,
  address VARCHAR(255) NOT NULL ,
  contact VARCHAR(50) NOT NULL ,
  phone VARCHAR(20) NOT NULL ,
  latitude NUMERIC(10,6)  ,
  longitude NUMERIC(10,6)  ,
  opening_hours VARCHAR(100)  ,
  status INT4  ,
  remark VARCHAR(255)  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_pickup_store primary key (id)
);

CREATE TABLE litemall_region(
  id INT4 NOT NULL ,
  pid INT4 NOT NULL ,
  name VARCHAR(120) NOT NULL ,
  type INT4 NOT NULL ,
  code INT4 NOT NULL ,
  constraint PK_litemall_region primary key (id)
);

CREATE TABLE litemall_search_history(
  id INT4 NOT NULL ,
  user_id VARCHAR(50) NOT NULL ,
  keyword VARCHAR(63) NOT NULL ,
  "from" VARCHAR(63) NOT NULL ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_search_history primary key (id)
);

CREATE TABLE litemall_storage(
  id INT4 NOT NULL ,
  key VARCHAR(63) NOT NULL ,
  name VARCHAR(255) NOT NULL ,
  type VARCHAR(20) NOT NULL ,
  size INT4 NOT NULL ,
  url VARCHAR(255)  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_storage primary key (id)
);

CREATE TABLE litemall_system(
  id INT4 NOT NULL ,
  key_name VARCHAR(255) NOT NULL ,
  key_value VARCHAR(255) NOT NULL ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_system primary key (id)
);

CREATE TABLE litemall_reset_code(
  id VARCHAR(50)  ,
  mobile VARCHAR(50)  ,
  code VARCHAR(10)  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_reset_code primary key (id)
);

CREATE TABLE litemall_promotion_activity(
  id INT4 NOT NULL ,
  name VARCHAR(63) NOT NULL ,
  discount_type INT4  ,
  start_time TIMESTAMP  ,
  end_time TIMESTAMP  ,
  status INT4  ,
  goods_scope INT4  ,
  goods_scope_value VARCHAR(1023)  ,
  priority INT4  ,
  max_per_user INT4  ,
  remark VARCHAR(255)  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_promotion_activity primary key (id)
);

CREATE TABLE litemall_check_in_rule(
  id INT4 NOT NULL ,
  day_seq INT4 NOT NULL ,
  point_reward INT4  ,
  reset_cycle INT4  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  constraint PK_litemall_check_in_rule primary key (id)
);

CREATE TABLE litemall_check_in_record(
  id INT4 NOT NULL ,
  user_id INT4 NOT NULL ,
  check_in_date DATE NOT NULL ,
  consecutive_days INT4  ,
  points_earned INT4  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_check_in_record primary key (id)
);

CREATE TABLE litemall_points_account(
  id INT4 NOT NULL ,
  user_id INT4 NOT NULL ,
  balance INT4  ,
  total_earned INT4  ,
  total_spent INT4  ,
  version INT4 NOT NULL ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  constraint PK_litemall_points_account primary key (id)
);

CREATE TABLE litemall_wallet(
  id INT4 NOT NULL ,
  user_id INT4 NOT NULL ,
  balance NUMERIC(10,2)  ,
  total_recharge NUMERIC(10,2)  ,
  total_spent NUMERIC(10,2)  ,
  version INT4 NOT NULL ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  constraint PK_litemall_wallet primary key (id)
);

CREATE TABLE litemall_user_message(
  id INT4 NOT NULL ,
  user_id INT4 NOT NULL ,
  msg_type INT4 NOT NULL ,
  title VARCHAR(127) NOT NULL ,
  content VARCHAR(1023)  ,
  is_read BOOLEAN  ,
  read_time TIMESTAMP  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_user_message primary key (id)
);

CREATE TABLE litemall_material_category(
  id INT4 NOT NULL ,
  name VARCHAR(127) NOT NULL ,
  parent_id INT4  ,
  sort_order INT4  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_material_category primary key (id)
);

CREATE TABLE litemall_goods(
  id INT4 NOT NULL ,
  goods_sn VARCHAR(63) NOT NULL ,
  name VARCHAR(127) NOT NULL ,
  category_id INT4  ,
  brand_id INT4  ,
  gallery VARCHAR(1023)  ,
  keywords VARCHAR(255)  ,
  brief VARCHAR(255)  ,
  is_on_sale BOOLEAN  ,
  sort_order INT4  ,
  pic_url VARCHAR(255)  ,
  share_url VARCHAR(255)  ,
  is_new BOOLEAN  ,
  is_hot BOOLEAN  ,
  unit VARCHAR(31)  ,
  counter_price NUMERIC(10,2)  ,
  retail_price NUMERIC(10,2)  ,
  detail TEXT  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  is_recommend BOOLEAN  ,
  video_url VARCHAR(255)  ,
  constraint PK_litemall_goods primary key (id)
);

CREATE TABLE litemall_coupon_user(
  id INT4 NOT NULL ,
  user_id VARCHAR(50) NOT NULL ,
  coupon_id INT4 NOT NULL ,
  status INT4  ,
  used_time TIMESTAMP  ,
  start_time TIMESTAMP  ,
  end_time TIMESTAMP  ,
  order_id INT4  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_coupon_user primary key (id)
);

CREATE TABLE litemall_order(
  id INT4 NOT NULL ,
  user_id VARCHAR(50) NOT NULL ,
  order_sn VARCHAR(63) NOT NULL ,
  order_status INT4 NOT NULL ,
  aftersale_status INT4  ,
  consignee VARCHAR(63) NOT NULL ,
  mobile VARCHAR(63) NOT NULL ,
  address VARCHAR(127) NOT NULL ,
  message VARCHAR(512) NOT NULL ,
  goods_price NUMERIC(10,2) NOT NULL ,
  freight_price NUMERIC(10,2) NOT NULL ,
  coupon_price NUMERIC(10,2) NOT NULL ,
  integral_price NUMERIC(10,2) NOT NULL ,
  groupon_price NUMERIC(10,2) NOT NULL ,
  order_price NUMERIC(10,2) NOT NULL ,
  actual_price NUMERIC(10,2) NOT NULL ,
  pay_id VARCHAR(63)  ,
  pay_time TIMESTAMP  ,
  ship_sn VARCHAR(63)  ,
  ship_channel VARCHAR(63)  ,
  ship_time TIMESTAMP  ,
  refund_amount NUMERIC(10,2)  ,
  refund_type VARCHAR(63)  ,
  refund_content VARCHAR(127)  ,
  refund_time TIMESTAMP  ,
  confirm_time TIMESTAMP  ,
  comments INT4  ,
  end_time TIMESTAMP  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  delivery_type INT4  ,
  pickup_store_id INT4  ,
  pickup_code VARCHAR(50)  ,
  pickup_time TIMESTAMP  ,
  promotion_price NUMERIC(10,2) NOT NULL ,
  pin_tuan_price NUMERIC(10,2) NOT NULL ,
  pay_channel INT4  ,
  wallet_pay_amount NUMERIC(10,2)  ,
  admin_remark VARCHAR(511)  ,
  constraint PK_litemall_order primary key (id)
);

CREATE TABLE litemall_promotion_tier(
  id INT4 NOT NULL ,
  activity_id INT4 NOT NULL ,
  meet_amount NUMERIC(10,2) NOT NULL ,
  discount_value NUMERIC(10,2) NOT NULL ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_promotion_tier primary key (id)
);

CREATE TABLE litemall_points_flow(
  id INT4 NOT NULL ,
  account_id INT4 NOT NULL ,
  user_id INT4 NOT NULL ,
  change_type INT4 NOT NULL ,
  change_amount INT4 NOT NULL ,
  balance_after INT4 NOT NULL ,
  source_type VARCHAR(50)  ,
  source_id VARCHAR(50)  ,
  remark VARCHAR(255)  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  constraint PK_litemall_points_flow primary key (id)
);

CREATE TABLE litemall_wallet_flow(
  id INT4 NOT NULL ,
  wallet_id INT4 NOT NULL ,
  change_type INT4 NOT NULL ,
  change_amount NUMERIC(10,2) NOT NULL ,
  balance_after NUMERIC(10,2)  ,
  source_type VARCHAR(50)  ,
  source_id VARCHAR(50)  ,
  remark VARCHAR(255)  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  constraint PK_litemall_wallet_flow primary key (id)
);

CREATE TABLE litemall_recharge(
  id INT4 NOT NULL ,
  user_id INT4 NOT NULL ,
  wallet_id INT4 NOT NULL ,
  amount NUMERIC(10,2) NOT NULL ,
  gift_amount NUMERIC(10,2)  ,
  pay_channel INT4  ,
  pay_status INT4  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_recharge primary key (id)
);

CREATE TABLE litemall_material(
  id INT4 NOT NULL ,
  name VARCHAR(255) NOT NULL ,
  url VARCHAR(255) NOT NULL ,
  file_type VARCHAR(50)  ,
  file_size INT4  ,
  category_id INT4  ,
  tag VARCHAR(255)  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_material primary key (id)
);

CREATE TABLE litemall_cart(
  id INT4 NOT NULL ,
  user_id VARCHAR(50)  ,
  goods_id INT4  ,
  goods_sn VARCHAR(63)  ,
  goods_name VARCHAR(127)  ,
  product_id INT4  ,
  price NUMERIC(10,2)  ,
  number INT4  ,
  specifications VARCHAR(1023)  ,
  checked BOOLEAN  ,
  pic_url VARCHAR(255)  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_cart primary key (id)
);

CREATE TABLE litemall_comment(
  id INT4 NOT NULL ,
  value_id INT4 NOT NULL ,
  type INT4 NOT NULL ,
  content VARCHAR(1023)  ,
  admin_content VARCHAR(511)  ,
  user_id VARCHAR(50) NOT NULL ,
  has_picture BOOLEAN  ,
  pic_urls VARCHAR(1023)  ,
  star INT4  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  pros VARCHAR(1023)  ,
  cons VARCHAR(1023)  ,
  semantic_rating INT4  ,
  constraint PK_litemall_comment primary key (id)
);

CREATE TABLE litemall_footprint(
  id INT4 NOT NULL ,
  user_id VARCHAR(50) NOT NULL ,
  goods_id INT4 NOT NULL ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_footprint primary key (id)
);

CREATE TABLE litemall_goods_attribute(
  id INT4 NOT NULL ,
  goods_id INT4 NOT NULL ,
  attribute VARCHAR(255) NOT NULL ,
  value VARCHAR(255) NOT NULL ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_goods_attribute primary key (id)
);

CREATE TABLE litemall_goods_product(
  id INT4 NOT NULL ,
  goods_id INT4 NOT NULL ,
  specifications VARCHAR(1023) NOT NULL ,
  price NUMERIC(10,2) NOT NULL ,
  number INT4 NOT NULL ,
  url VARCHAR(255)  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  safe_stock INT4  ,
  constraint PK_litemall_goods_product primary key (id)
);

CREATE TABLE litemall_goods_specification(
  id INT4 NOT NULL ,
  goods_id INT4 NOT NULL ,
  specification VARCHAR(255) NOT NULL ,
  value VARCHAR(255) NOT NULL ,
  pic_url VARCHAR(255) NOT NULL ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_goods_specification primary key (id)
);

CREATE TABLE litemall_groupon_rules(
  id INT4 NOT NULL ,
  goods_id INT4 NOT NULL ,
  goods_name VARCHAR(127) NOT NULL ,
  pic_url VARCHAR(255)  ,
  discount NUMERIC(10,2) NOT NULL ,
  discount_member INT4 NOT NULL ,
  expire_time TIMESTAMP  ,
  status INT4  ,
  add_time TIMESTAMP NOT NULL ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_groupon_rules primary key (id)
);

CREATE TABLE litemall_time_discount(
  id INT4 NOT NULL ,
  goods_id INT4 NOT NULL ,
  product_id INT4  ,
  discount_type INT4  ,
  discount_value NUMERIC(10,2)  ,
  start_time TIMESTAMP  ,
  end_time TIMESTAMP  ,
  status INT4  ,
  stock_limit INT4  ,
  remark VARCHAR(255)  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_time_discount primary key (id)
);

CREATE TABLE litemall_flash_sale(
  id INT4 NOT NULL ,
  goods_id INT4 NOT NULL ,
  product_id INT4  ,
  flash_price NUMERIC(10,2) NOT NULL ,
  total_stock INT4  ,
  max_per_user INT4  ,
  max_per_order INT4  ,
  status INT4  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_flash_sale primary key (id)
);

CREATE TABLE litemall_pin_tuan_activity(
  id INT4 NOT NULL ,
  goods_id INT4 NOT NULL ,
  product_id INT4  ,
  pin_tuan_price NUMERIC(10,2) NOT NULL ,
  min_user_count INT4 NOT NULL ,
  max_user_count INT4  ,
  expire_hours INT4  ,
  status INT4  ,
  remark VARCHAR(255)  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_pin_tuan_activity primary key (id)
);

CREATE TABLE litemall_aftersale(
  id INT4 NOT NULL ,
  aftersale_sn VARCHAR(63)  ,
  order_id INT4 NOT NULL ,
  user_id VARCHAR(50) NOT NULL ,
  type INT4  ,
  reason VARCHAR(31)  ,
  amount NUMERIC(10,2)  ,
  pictures VARCHAR(1023)  ,
  comment VARCHAR(511)  ,
  status INT4  ,
  handle_time TIMESTAMP  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  order_item_id INT4  ,
  process_note VARCHAR(511)  ,
  process_time TIMESTAMP  ,
  constraint PK_litemall_aftersale primary key (id)
);

CREATE TABLE litemall_groupon(
  id INT4 NOT NULL ,
  order_id INT4 NOT NULL ,
  groupon_id INT4  ,
  rules_id INT4 NOT NULL ,
  user_id VARCHAR(50) NOT NULL ,
  share_url VARCHAR(255)  ,
  creator_user_id VARCHAR(50) NOT NULL ,
  creator_user_time TIMESTAMP  ,
  status INT4  ,
  add_time TIMESTAMP NOT NULL ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_groupon primary key (id)
);

CREATE TABLE litemall_order_goods(
  id INT4 NOT NULL ,
  order_id INT4 NOT NULL ,
  goods_id INT4 NOT NULL ,
  goods_name VARCHAR(127) NOT NULL ,
  goods_sn VARCHAR(63) NOT NULL ,
  product_id INT4 NOT NULL ,
  number INT4 NOT NULL ,
  price NUMERIC(10,2) NOT NULL ,
  specifications VARCHAR(1023) NOT NULL ,
  pic_url VARCHAR(255) NOT NULL ,
  comment INT4  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  actual_pay_amount NUMERIC(10,2)  ,
  constraint PK_litemall_order_goods primary key (id)
);

CREATE TABLE litemall_flash_sale_session(
  id INT4 NOT NULL ,
  flash_sale_id INT4 NOT NULL ,
  session_start TIMESTAMP NOT NULL ,
  session_end TIMESTAMP NOT NULL ,
  session_stock INT4  ,
  session_status INT4  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_flash_sale_session primary key (id)
);

CREATE TABLE litemall_pin_tuan_group(
  id INT4 NOT NULL ,
  activity_id INT4 NOT NULL ,
  creator_user_id INT4 NOT NULL ,
  order_id INT4  ,
  status INT4  ,
  expire_time TIMESTAMP  ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_pin_tuan_group primary key (id)
);

CREATE TABLE litemall_pin_tuan_member(
  id INT4 NOT NULL ,
  group_id INT4 NOT NULL ,
  user_id INT4 NOT NULL ,
  order_id INT4 NOT NULL ,
  add_time TIMESTAMP  ,
  update_time TIMESTAMP  ,
  deleted BOOLEAN  ,
  constraint PK_litemall_pin_tuan_member primary key (id)
);


      COMMENT ON TABLE litemall_ad IS '广告表';
                
      COMMENT ON COLUMN litemall_ad.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_ad.name IS '广告标题';
                    
      COMMENT ON COLUMN litemall_ad.link IS '所广告的商品页面或者活动页面链接地址';
                    
      COMMENT ON COLUMN litemall_ad.url IS '广告宣传图片';
                    
      COMMENT ON COLUMN litemall_ad.position IS '广告位置：1则是首页';
                    
      COMMENT ON COLUMN litemall_ad.content IS '活动内容';
                    
      COMMENT ON COLUMN litemall_ad.start_time IS '广告开始时间';
                    
      COMMENT ON COLUMN litemall_ad.end_time IS '广告结束时间';
                    
      COMMENT ON COLUMN litemall_ad.enabled IS '是否启动';
                    
      COMMENT ON COLUMN litemall_ad.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_ad.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_ad.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_address IS '收货地址表';
                
      COMMENT ON COLUMN litemall_address.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_address.name IS '收货人名称';
                    
      COMMENT ON COLUMN litemall_address.user_id IS '用户ID';
                    
      COMMENT ON COLUMN litemall_address.province IS '行政区域表的省ID';
                    
      COMMENT ON COLUMN litemall_address.city IS '行政区域表的市ID';
                    
      COMMENT ON COLUMN litemall_address.county IS '行政区域表的区县ID';
                    
      COMMENT ON COLUMN litemall_address.address_detail IS '详细收货地址';
                    
      COMMENT ON COLUMN litemall_address.area_code IS '地区编码';
                    
      COMMENT ON COLUMN litemall_address.postal_code IS '邮政编码';
                    
      COMMENT ON COLUMN litemall_address.tel IS '手机号码';
                    
      COMMENT ON COLUMN litemall_address.is_default IS '是否默认地址';
                    
      COMMENT ON COLUMN litemall_address.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_address.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_address.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_brand IS '品牌商表';
                
      COMMENT ON COLUMN litemall_brand.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_brand.name IS '品牌商名称';
                    
      COMMENT ON COLUMN litemall_brand.pic_url IS '品牌商图片';
                    
      COMMENT ON COLUMN litemall_brand."desc" IS '品牌商简介';
                    
      COMMENT ON COLUMN litemall_brand.sort_order IS '排序';
                    
      COMMENT ON COLUMN litemall_brand.floor_price IS '底价';
                    
      COMMENT ON COLUMN litemall_brand.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_brand.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_brand.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_category IS '类目表';
                
      COMMENT ON COLUMN litemall_category.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_category.name IS '类目名称';
                    
      COMMENT ON COLUMN litemall_category.icon_url IS '类目图标';
                    
      COMMENT ON COLUMN litemall_category.pic_url IS '类目图片';
                    
      COMMENT ON COLUMN litemall_category.keywords IS '类目关键字';
                    
      COMMENT ON COLUMN litemall_category."desc" IS '简介';
                    
      COMMENT ON COLUMN litemall_category.level IS '级别';
                    
      COMMENT ON COLUMN litemall_category.pid IS '父类目ID';
                    
      COMMENT ON COLUMN litemall_category.sort_order IS '排序';
                    
      COMMENT ON COLUMN litemall_category.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_category.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_category.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_collect IS '收藏表';
                
      COMMENT ON COLUMN litemall_collect.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_collect.user_id IS '用户ID';
                    
      COMMENT ON COLUMN litemall_collect.value_id IS '如果type=0，则是商品ID；如果type=1，则是专题ID';
                    
      COMMENT ON COLUMN litemall_collect.type IS '收藏类型';
                    
      COMMENT ON COLUMN litemall_collect.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_collect.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_collect.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_topic IS '专题表';
                
      COMMENT ON COLUMN litemall_topic.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_topic.title IS '专题标题';
                    
      COMMENT ON COLUMN litemall_topic.subtitle IS '专题子标题';
                    
      COMMENT ON COLUMN litemall_topic.content IS '专题内容';
                    
      COMMENT ON COLUMN litemall_topic.price IS '专题相关商品最低价';
                    
      COMMENT ON COLUMN litemall_topic.read_count IS '专题阅读量';
                    
      COMMENT ON COLUMN litemall_topic.pic_url IS '专题图片';
                    
      COMMENT ON COLUMN litemall_topic.sort_order IS '排序';
                    
      COMMENT ON COLUMN litemall_topic.goods IS '专题相关商品';
                    
      COMMENT ON COLUMN litemall_topic.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_topic.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_topic.deleted IS '逻辑删除';
                    
      COMMENT ON COLUMN litemall_topic.status IS '上下架状态';
                    
      COMMENT ON TABLE litemall_coupon IS '优惠券信息及规则表';
                
      COMMENT ON COLUMN litemall_coupon.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_coupon.name IS '优惠券名称';
                    
      COMMENT ON COLUMN litemall_coupon."desc" IS '优惠券介绍';
                    
      COMMENT ON COLUMN litemall_coupon.tag IS '优惠券标签';
                    
      COMMENT ON COLUMN litemall_coupon.total IS '优惠券数量';
                    
      COMMENT ON COLUMN litemall_coupon.discount IS '优惠金额，';
                    
      COMMENT ON COLUMN litemall_coupon.min IS '最少消费金额';
                    
      COMMENT ON COLUMN litemall_coupon."limit" IS '用户领券限制数量';
                    
      COMMENT ON COLUMN litemall_coupon.type IS '优惠券赠送类型';
                    
      COMMENT ON COLUMN litemall_coupon.status IS '优惠券状态';
                    
      COMMENT ON COLUMN litemall_coupon.goods_type IS '商品限制类型';
                    
      COMMENT ON COLUMN litemall_coupon.goods_value IS '商品限制值';
                    
      COMMENT ON COLUMN litemall_coupon.code IS '优惠券兑换码';
                    
      COMMENT ON COLUMN litemall_coupon.time_type IS '有效时间限制';
                    
      COMMENT ON COLUMN litemall_coupon.days IS '基于领取时间的有效天数days。';
                    
      COMMENT ON COLUMN litemall_coupon.start_time IS '使用券开始时间';
                    
      COMMENT ON COLUMN litemall_coupon.end_time IS '使用券截至时间';
                    
      COMMENT ON COLUMN litemall_coupon.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_coupon.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_coupon.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_feedback IS '意见反馈表';
                
      COMMENT ON COLUMN litemall_feedback.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_feedback.user_id IS '用户ID';
                    
      COMMENT ON COLUMN litemall_feedback.username IS '用户名称';
                    
      COMMENT ON COLUMN litemall_feedback.mobile IS '手机号';
                    
      COMMENT ON COLUMN litemall_feedback.feed_type IS '反馈类型';
                    
      COMMENT ON COLUMN litemall_feedback.content IS '反馈内容';
                    
      COMMENT ON COLUMN litemall_feedback.status IS '状态';
                    
      COMMENT ON COLUMN litemall_feedback.has_picture IS '是否含有图片';
                    
      COMMENT ON COLUMN litemall_feedback.pic_urls IS '图片地址列表，采用JSON数组格式';
                    
      COMMENT ON COLUMN litemall_feedback.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_feedback.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_feedback.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_issue IS '常见问题表';
                
      COMMENT ON COLUMN litemall_issue.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_issue.question IS '问题标题';
                    
      COMMENT ON COLUMN litemall_issue.answer IS '问题答案';
                    
      COMMENT ON COLUMN litemall_issue.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_issue.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_issue.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_keyword IS '关键字表';
                
      COMMENT ON COLUMN litemall_keyword.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_keyword.keyword IS '关键字';
                    
      COMMENT ON COLUMN litemall_keyword.url IS '关键字的跳转链接';
                    
      COMMENT ON COLUMN litemall_keyword.is_hot IS '是否是热门关键字';
                    
      COMMENT ON COLUMN litemall_keyword.is_default IS '是否是默认关键字';
                    
      COMMENT ON COLUMN litemall_keyword.sort_order IS '排序';
                    
      COMMENT ON COLUMN litemall_keyword.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_keyword.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_keyword.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_log IS '操作日志表';
                
      COMMENT ON COLUMN litemall_log.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_log.admin IS '管理员';
                    
      COMMENT ON COLUMN litemall_log.ip IS '管理员地址';
                    
      COMMENT ON COLUMN litemall_log.type IS '操作分类';
                    
      COMMENT ON COLUMN litemall_log.action IS '操作动作';
                    
      COMMENT ON COLUMN litemall_log.status IS '操作状态';
                    
      COMMENT ON COLUMN litemall_log.result IS '操作结果/消息';
                    
      COMMENT ON COLUMN litemall_log.comment IS '补充信息';
                    
      COMMENT ON COLUMN litemall_log.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_log.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_log.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_notice IS '通知表';
                
      COMMENT ON COLUMN litemall_notice.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_notice.title IS '通知标题';
                    
      COMMENT ON COLUMN litemall_notice.content IS '通知内容';
                    
      COMMENT ON COLUMN litemall_notice.admin_id IS '创建通知的管理员ID';
                    
      COMMENT ON COLUMN litemall_notice.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_notice.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_notice.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_notice_admin IS '通知管理员表';
                
      COMMENT ON COLUMN litemall_notice_admin.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_notice_admin.notice_id IS '通知ID';
                    
      COMMENT ON COLUMN litemall_notice_admin.notice_title IS '通知标题';
                    
      COMMENT ON COLUMN litemall_notice_admin.admin_id IS '管理员ID';
                    
      COMMENT ON COLUMN litemall_notice_admin.read_time IS '阅读时间';
                    
      COMMENT ON COLUMN litemall_notice_admin.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_notice_admin.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_notice_admin.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_pickup_store IS '自提门店表';
                
      COMMENT ON COLUMN litemall_pickup_store.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_pickup_store.name IS '门店名称';
                    
      COMMENT ON COLUMN litemall_pickup_store.address IS '门店地址';
                    
      COMMENT ON COLUMN litemall_pickup_store.contact IS '联系人';
                    
      COMMENT ON COLUMN litemall_pickup_store.phone IS '联系电话';
                    
      COMMENT ON COLUMN litemall_pickup_store.latitude IS '纬度';
                    
      COMMENT ON COLUMN litemall_pickup_store.longitude IS '经度';
                    
      COMMENT ON COLUMN litemall_pickup_store.opening_hours IS '营业时间';
                    
      COMMENT ON COLUMN litemall_pickup_store.status IS '启用状态';
                    
      COMMENT ON COLUMN litemall_pickup_store.remark IS '备注';
                    
      COMMENT ON COLUMN litemall_pickup_store.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_pickup_store.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_pickup_store.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_region IS '行政区域表';
                
      COMMENT ON COLUMN litemall_region.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_region.pid IS '行政区域父ID';
                    
      COMMENT ON COLUMN litemall_region.name IS '行政区域名称';
                    
      COMMENT ON COLUMN litemall_region.type IS '行政区域类型';
                    
      COMMENT ON COLUMN litemall_region.code IS '行政区域编码';
                    
      COMMENT ON TABLE litemall_search_history IS '搜索历史表';
                
      COMMENT ON COLUMN litemall_search_history.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_search_history.user_id IS '用户ID';
                    
      COMMENT ON COLUMN litemall_search_history.keyword IS '搜索关键字';
                    
      COMMENT ON COLUMN litemall_search_history."from" IS '搜索来源';
                    
      COMMENT ON COLUMN litemall_search_history.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_search_history.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_search_history.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_storage IS '文件存储表';
                
      COMMENT ON COLUMN litemall_storage.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_storage.key IS '文件的唯一索引';
                    
      COMMENT ON COLUMN litemall_storage.name IS '文件名';
                    
      COMMENT ON COLUMN litemall_storage.type IS '文件类型';
                    
      COMMENT ON COLUMN litemall_storage.size IS '文件大小';
                    
      COMMENT ON COLUMN litemall_storage.url IS '文件访问链接';
                    
      COMMENT ON COLUMN litemall_storage.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_storage.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_storage.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_system IS '系统配置表';
                
      COMMENT ON COLUMN litemall_system.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_system.key_name IS '系统配置名';
                    
      COMMENT ON COLUMN litemall_system.key_value IS '系统配置值';
                    
      COMMENT ON COLUMN litemall_system.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_system.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_system.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_reset_code IS '密码重置验证码';
                
      COMMENT ON COLUMN litemall_reset_code.mobile IS '手机号';
                    
      COMMENT ON COLUMN litemall_reset_code.code IS '验证码';
                    
      COMMENT ON COLUMN litemall_reset_code.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_reset_code.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_reset_code.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_promotion_activity IS '促销活动表（满减送）';
                
      COMMENT ON COLUMN litemall_promotion_activity.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_promotion_activity.name IS '活动名称';
                    
      COMMENT ON COLUMN litemall_promotion_activity.discount_type IS '折扣类型';
                    
      COMMENT ON COLUMN litemall_promotion_activity.start_time IS '开始时间';
                    
      COMMENT ON COLUMN litemall_promotion_activity.end_time IS '结束时间';
                    
      COMMENT ON COLUMN litemall_promotion_activity.status IS '活动状态';
                    
      COMMENT ON COLUMN litemall_promotion_activity.goods_scope IS '商品范围类型';
                    
      COMMENT ON COLUMN litemall_promotion_activity.goods_scope_value IS '商品范围值(JSON)';
                    
      COMMENT ON COLUMN litemall_promotion_activity.priority IS '优先级';
                    
      COMMENT ON COLUMN litemall_promotion_activity.max_per_user IS '每人限参与次数(0不限)';
                    
      COMMENT ON COLUMN litemall_promotion_activity.remark IS '备注';
                    
      COMMENT ON COLUMN litemall_promotion_activity.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_promotion_activity.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_promotion_activity.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_check_in_rule IS '签到规则表';
                
      COMMENT ON COLUMN litemall_check_in_rule.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_check_in_rule.day_seq IS '连续第N天';
                    
      COMMENT ON COLUMN litemall_check_in_rule.point_reward IS '奖励积分数';
                    
      COMMENT ON COLUMN litemall_check_in_rule.reset_cycle IS '重置周期（天数，0不重置）';
                    
      COMMENT ON COLUMN litemall_check_in_rule.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_check_in_rule.update_time IS '更新时间';
                    
      COMMENT ON TABLE litemall_check_in_record IS '签到记录表';
                
      COMMENT ON COLUMN litemall_check_in_record.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_check_in_record.user_id IS '用户ID';
                    
      COMMENT ON COLUMN litemall_check_in_record.check_in_date IS '签到日期';
                    
      COMMENT ON COLUMN litemall_check_in_record.consecutive_days IS '连续签到天数';
                    
      COMMENT ON COLUMN litemall_check_in_record.points_earned IS '获得积分';
                    
      COMMENT ON COLUMN litemall_check_in_record.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_check_in_record.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_check_in_record.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_points_account IS '积分账户表';
                
      COMMENT ON COLUMN litemall_points_account.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_points_account.user_id IS '用户ID';
                    
      COMMENT ON COLUMN litemall_points_account.balance IS '当前可用积分';
                    
      COMMENT ON COLUMN litemall_points_account.total_earned IS '累计获得';
                    
      COMMENT ON COLUMN litemall_points_account.total_spent IS '累计消耗';
                    
      COMMENT ON COLUMN litemall_points_account.version IS '数据版本';
                    
      COMMENT ON COLUMN litemall_points_account.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_points_account.update_time IS '更新时间';
                    
      COMMENT ON TABLE litemall_wallet IS '钱包账户表';
                
      COMMENT ON COLUMN litemall_wallet.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_wallet.user_id IS '用户ID';
                    
      COMMENT ON COLUMN litemall_wallet.balance IS '可用余额';
                    
      COMMENT ON COLUMN litemall_wallet.total_recharge IS '累计充值';
                    
      COMMENT ON COLUMN litemall_wallet.total_spent IS '累计消费';
                    
      COMMENT ON COLUMN litemall_wallet.version IS '数据版本';
                    
      COMMENT ON COLUMN litemall_wallet.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_wallet.update_time IS '更新时间';
                    
      COMMENT ON TABLE litemall_user_message IS '用户站内信表';
                
      COMMENT ON COLUMN litemall_user_message.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_user_message.user_id IS '用户ID';
                    
      COMMENT ON COLUMN litemall_user_message.msg_type IS '消息类型';
                    
      COMMENT ON COLUMN litemall_user_message.title IS '标题';
                    
      COMMENT ON COLUMN litemall_user_message.content IS '内容';
                    
      COMMENT ON COLUMN litemall_user_message.is_read IS '是否已读';
                    
      COMMENT ON COLUMN litemall_user_message.read_time IS '阅读时间';
                    
      COMMENT ON COLUMN litemall_user_message.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_user_message.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_user_message.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_material_category IS '素材分类表';
                
      COMMENT ON COLUMN litemall_material_category.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_material_category.name IS '分类名称';
                    
      COMMENT ON COLUMN litemall_material_category.parent_id IS '父分类ID';
                    
      COMMENT ON COLUMN litemall_material_category.sort_order IS '排序';
                    
      COMMENT ON COLUMN litemall_material_category.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_material_category.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_material_category.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_goods IS '商品基本信息';
                
      COMMENT ON COLUMN litemall_goods.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_goods.goods_sn IS '商品编号';
                    
      COMMENT ON COLUMN litemall_goods.name IS '商品名称';
                    
      COMMENT ON COLUMN litemall_goods.category_id IS '商品所属类目ID';
                    
      COMMENT ON COLUMN litemall_goods.brand_id IS '品牌ID';
                    
      COMMENT ON COLUMN litemall_goods.gallery IS '商品宣传图片列表';
                    
      COMMENT ON COLUMN litemall_goods.keywords IS '商品关键字';
                    
      COMMENT ON COLUMN litemall_goods.brief IS '商品简介';
                    
      COMMENT ON COLUMN litemall_goods.is_on_sale IS '是否在售';
                    
      COMMENT ON COLUMN litemall_goods.sort_order IS '排序顺序';
                    
      COMMENT ON COLUMN litemall_goods.pic_url IS '商品图片';
                    
      COMMENT ON COLUMN litemall_goods.share_url IS '商品分享海报';
                    
      COMMENT ON COLUMN litemall_goods.is_new IS '是否新品';
                    
      COMMENT ON COLUMN litemall_goods.is_hot IS '是否热品';
                    
      COMMENT ON COLUMN litemall_goods.unit IS '商品单位';
                    
      COMMENT ON COLUMN litemall_goods.counter_price IS '市场售价';
                    
      COMMENT ON COLUMN litemall_goods.retail_price IS '当前价格';
                    
      COMMENT ON COLUMN litemall_goods.detail IS '详情';
                    
      COMMENT ON COLUMN litemall_goods.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_goods.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_goods.deleted IS '逻辑删除';
                    
      COMMENT ON COLUMN litemall_goods.is_recommend IS '是否推荐';
                    
      COMMENT ON COLUMN litemall_goods.video_url IS '商品视频链接';
                    
      COMMENT ON TABLE litemall_coupon_user IS '优惠券用户使用表';
                
      COMMENT ON COLUMN litemall_coupon_user.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_coupon_user.user_id IS '用户ID';
                    
      COMMENT ON COLUMN litemall_coupon_user.coupon_id IS '优惠券ID';
                    
      COMMENT ON COLUMN litemall_coupon_user.status IS '使用状态';
                    
      COMMENT ON COLUMN litemall_coupon_user.used_time IS '使用时间';
                    
      COMMENT ON COLUMN litemall_coupon_user.start_time IS '有效期开始时间';
                    
      COMMENT ON COLUMN litemall_coupon_user.end_time IS '有效期截至时间';
                    
      COMMENT ON COLUMN litemall_coupon_user.order_id IS '订单ID';
                    
      COMMENT ON COLUMN litemall_coupon_user.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_coupon_user.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_coupon_user.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_order IS '订单表';
                
      COMMENT ON COLUMN litemall_order.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_order.user_id IS '用户ID';
                    
      COMMENT ON COLUMN litemall_order.order_sn IS '订单编号';
                    
      COMMENT ON COLUMN litemall_order.order_status IS '订单状态';
                    
      COMMENT ON COLUMN litemall_order.aftersale_status IS '售后状态';
                    
      COMMENT ON COLUMN litemall_order.consignee IS '收货人名称';
                    
      COMMENT ON COLUMN litemall_order.mobile IS '收货人手机号';
                    
      COMMENT ON COLUMN litemall_order.address IS '收货具体地址';
                    
      COMMENT ON COLUMN litemall_order.message IS '用户订单留言';
                    
      COMMENT ON COLUMN litemall_order.goods_price IS '商品总费用';
                    
      COMMENT ON COLUMN litemall_order.freight_price IS '配送费用';
                    
      COMMENT ON COLUMN litemall_order.coupon_price IS '优惠券减免';
                    
      COMMENT ON COLUMN litemall_order.integral_price IS '用户积分减免';
                    
      COMMENT ON COLUMN litemall_order.groupon_price IS '团购优惠价减免';
                    
      COMMENT ON COLUMN litemall_order.order_price IS '订单费用';
                    
      COMMENT ON COLUMN litemall_order.actual_price IS '实付费用';
                    
      COMMENT ON COLUMN litemall_order.pay_id IS '微信付款编号';
                    
      COMMENT ON COLUMN litemall_order.pay_time IS '微信付款时间';
                    
      COMMENT ON COLUMN litemall_order.ship_sn IS '发货编号';
                    
      COMMENT ON COLUMN litemall_order.ship_channel IS '发货快递公司';
                    
      COMMENT ON COLUMN litemall_order.ship_time IS '发货开始时间';
                    
      COMMENT ON COLUMN litemall_order.refund_amount IS '实际退款金额';
                    
      COMMENT ON COLUMN litemall_order.refund_type IS '退款方式';
                    
      COMMENT ON COLUMN litemall_order.refund_content IS '退款备注';
                    
      COMMENT ON COLUMN litemall_order.refund_time IS '退款时间';
                    
      COMMENT ON COLUMN litemall_order.confirm_time IS '用户确认收货时间';
                    
      COMMENT ON COLUMN litemall_order.comments IS '待评价订单商品数量';
                    
      COMMENT ON COLUMN litemall_order.end_time IS '订单关闭时间';
                    
      COMMENT ON COLUMN litemall_order.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_order.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_order.deleted IS '逻辑删除';
                    
      COMMENT ON COLUMN litemall_order.delivery_type IS '配送方式';
                    
      COMMENT ON COLUMN litemall_order.pickup_store_id IS '自提门店ID';
                    
      COMMENT ON COLUMN litemall_order.pickup_code IS '自提核销码';
                    
      COMMENT ON COLUMN litemall_order.pickup_time IS '核销时间';
                    
      COMMENT ON COLUMN litemall_order.promotion_price IS '促销优惠金额（满减/限时折扣）';
                    
      COMMENT ON COLUMN litemall_order.pin_tuan_price IS '拼团优惠金额';
                    
      COMMENT ON COLUMN litemall_order.pay_channel IS '支付通道';
                    
      COMMENT ON COLUMN litemall_order.wallet_pay_amount IS '余额支付金额';
                    
      COMMENT ON COLUMN litemall_order.admin_remark IS '运营备注';
                    
      COMMENT ON TABLE litemall_promotion_tier IS '满减档位表';
                
      COMMENT ON COLUMN litemall_promotion_tier.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_promotion_tier.activity_id IS '促销活动ID';
                    
      COMMENT ON COLUMN litemall_promotion_tier.meet_amount IS '满足金额';
                    
      COMMENT ON COLUMN litemall_promotion_tier.discount_value IS '减免值（金额或折扣率）';
                    
      COMMENT ON COLUMN litemall_promotion_tier.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_promotion_tier.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_promotion_tier.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_points_flow IS '积分流水表';
                
      COMMENT ON COLUMN litemall_points_flow.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_points_flow.account_id IS '积分账户ID';
                    
      COMMENT ON COLUMN litemall_points_flow.user_id IS '用户ID';
                    
      COMMENT ON COLUMN litemall_points_flow.change_type IS '变动类型';
                    
      COMMENT ON COLUMN litemall_points_flow.change_amount IS '变动数量';
                    
      COMMENT ON COLUMN litemall_points_flow.balance_after IS '变动后余额';
                    
      COMMENT ON COLUMN litemall_points_flow.source_type IS '来源类型';
                    
      COMMENT ON COLUMN litemall_points_flow.source_id IS '来源业务ID';
                    
      COMMENT ON COLUMN litemall_points_flow.remark IS '备注';
                    
      COMMENT ON COLUMN litemall_points_flow.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_points_flow.update_time IS '更新时间';
                    
      COMMENT ON TABLE litemall_wallet_flow IS '钱包流水表';
                
      COMMENT ON COLUMN litemall_wallet_flow.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_wallet_flow.wallet_id IS '钱包ID';
                    
      COMMENT ON COLUMN litemall_wallet_flow.change_type IS '变动类型';
                    
      COMMENT ON COLUMN litemall_wallet_flow.change_amount IS '变动金额';
                    
      COMMENT ON COLUMN litemall_wallet_flow.balance_after IS '变动后余额';
                    
      COMMENT ON COLUMN litemall_wallet_flow.source_type IS '来源类型';
                    
      COMMENT ON COLUMN litemall_wallet_flow.source_id IS '来源业务ID';
                    
      COMMENT ON COLUMN litemall_wallet_flow.remark IS '备注';
                    
      COMMENT ON COLUMN litemall_wallet_flow.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_wallet_flow.update_time IS '更新时间';
                    
      COMMENT ON TABLE litemall_recharge IS '充值记录表';
                
      COMMENT ON COLUMN litemall_recharge.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_recharge.user_id IS '用户ID';
                    
      COMMENT ON COLUMN litemall_recharge.wallet_id IS '钱包ID';
                    
      COMMENT ON COLUMN litemall_recharge.amount IS '充值金额';
                    
      COMMENT ON COLUMN litemall_recharge.gift_amount IS '赠送金额';
                    
      COMMENT ON COLUMN litemall_recharge.pay_channel IS '支付渠道';
                    
      COMMENT ON COLUMN litemall_recharge.pay_status IS '支付状态';
                    
      COMMENT ON COLUMN litemall_recharge.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_recharge.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_recharge.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_material IS '素材资源表';
                
      COMMENT ON COLUMN litemall_material.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_material.name IS '文件名';
                    
      COMMENT ON COLUMN litemall_material.url IS '访问链接';
                    
      COMMENT ON COLUMN litemall_material.file_type IS '文件类型(image/video)';
                    
      COMMENT ON COLUMN litemall_material.file_size IS '文件大小(bytes)';
                    
      COMMENT ON COLUMN litemall_material.category_id IS '分类ID';
                    
      COMMENT ON COLUMN litemall_material.tag IS '标签';
                    
      COMMENT ON COLUMN litemall_material.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_material.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_material.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_cart IS '购物车商品表';
                
      COMMENT ON COLUMN litemall_cart.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_cart.user_id IS '用户ID';
                    
      COMMENT ON COLUMN litemall_cart.goods_id IS '商品ID';
                    
      COMMENT ON COLUMN litemall_cart.goods_sn IS '商品编号';
                    
      COMMENT ON COLUMN litemall_cart.goods_name IS '商品名称';
                    
      COMMENT ON COLUMN litemall_cart.product_id IS '商品货品表的货品ID';
                    
      COMMENT ON COLUMN litemall_cart.price IS '商品货品的价格';
                    
      COMMENT ON COLUMN litemall_cart.number IS '商品货品的数量';
                    
      COMMENT ON COLUMN litemall_cart.specifications IS '商品规格值列表';
                    
      COMMENT ON COLUMN litemall_cart.checked IS '购物车中商品是否选择状态';
                    
      COMMENT ON COLUMN litemall_cart.pic_url IS '商品图片或者商品货品图片';
                    
      COMMENT ON COLUMN litemall_cart.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_cart.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_cart.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_comment IS '评论表';
                
      COMMENT ON COLUMN litemall_comment.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_comment.value_id IS '如果type=0，则是商品评论；如果是type=1，则是专题评论。';
                    
      COMMENT ON COLUMN litemall_comment.type IS '评论类型';
                    
      COMMENT ON COLUMN litemall_comment.content IS '评论内容';
                    
      COMMENT ON COLUMN litemall_comment.admin_content IS '管理员回复内容';
                    
      COMMENT ON COLUMN litemall_comment.user_id IS '用户ID';
                    
      COMMENT ON COLUMN litemall_comment.has_picture IS '是否含有图片';
                    
      COMMENT ON COLUMN litemall_comment.pic_urls IS '图片地址列表，采用JSON数组格式';
                    
      COMMENT ON COLUMN litemall_comment.star IS '评分， 1-5';
                    
      COMMENT ON COLUMN litemall_comment.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_comment.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_comment.deleted IS '逻辑删除';
                    
      COMMENT ON COLUMN litemall_comment.pros IS '优点列表(JSON)';
                    
      COMMENT ON COLUMN litemall_comment.cons IS '缺点列表(JSON)';
                    
      COMMENT ON COLUMN litemall_comment.semantic_rating IS '语义评级(1-5)';
                    
      COMMENT ON TABLE litemall_footprint IS '用户浏览足迹表';
                
      COMMENT ON COLUMN litemall_footprint.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_footprint.user_id IS '用户ID';
                    
      COMMENT ON COLUMN litemall_footprint.goods_id IS '浏览商品ID';
                    
      COMMENT ON COLUMN litemall_footprint.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_footprint.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_footprint.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_goods_attribute IS '商品参数表';
                
      COMMENT ON COLUMN litemall_goods_attribute.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_goods_attribute.goods_id IS '商品ID';
                    
      COMMENT ON COLUMN litemall_goods_attribute.attribute IS '商品参数名称';
                    
      COMMENT ON COLUMN litemall_goods_attribute.value IS '商品参数值';
                    
      COMMENT ON COLUMN litemall_goods_attribute.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_goods_attribute.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_goods_attribute.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_goods_product IS '商品货品表';
                
      COMMENT ON COLUMN litemall_goods_product.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_goods_product.goods_id IS '商品ID';
                    
      COMMENT ON COLUMN litemall_goods_product.specifications IS '商品规格值';
                    
      COMMENT ON COLUMN litemall_goods_product.price IS '商品货品价格';
                    
      COMMENT ON COLUMN litemall_goods_product.number IS '商品货品数量';
                    
      COMMENT ON COLUMN litemall_goods_product.url IS '商品货品图片';
                    
      COMMENT ON COLUMN litemall_goods_product.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_goods_product.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_goods_product.deleted IS '逻辑删除';
                    
      COMMENT ON COLUMN litemall_goods_product.safe_stock IS '安全库存预警线';
                    
      COMMENT ON TABLE litemall_goods_specification IS '商品规格表';
                
      COMMENT ON COLUMN litemall_goods_specification.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_goods_specification.goods_id IS '商品ID';
                    
      COMMENT ON COLUMN litemall_goods_specification.specification IS '商品规格名称';
                    
      COMMENT ON COLUMN litemall_goods_specification.value IS '商品规格值';
                    
      COMMENT ON COLUMN litemall_goods_specification.pic_url IS '商品规格图片';
                    
      COMMENT ON COLUMN litemall_goods_specification.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_goods_specification.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_goods_specification.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_groupon_rules IS '团购规则表';
                
      COMMENT ON COLUMN litemall_groupon_rules.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_groupon_rules.goods_id IS '商品ID';
                    
      COMMENT ON COLUMN litemall_groupon_rules.goods_name IS '商品名称';
                    
      COMMENT ON COLUMN litemall_groupon_rules.pic_url IS '商品/货品图片';
                    
      COMMENT ON COLUMN litemall_groupon_rules.discount IS '优惠金额';
                    
      COMMENT ON COLUMN litemall_groupon_rules.discount_member IS '达到优惠条件的人数';
                    
      COMMENT ON COLUMN litemall_groupon_rules.expire_time IS '团购过期时间';
                    
      COMMENT ON COLUMN litemall_groupon_rules.status IS '团购规则状态，正常上线则0，到期自动下线则1，管理手动下线则2';
                    
      COMMENT ON COLUMN litemall_groupon_rules.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_groupon_rules.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_groupon_rules.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_time_discount IS '限时折扣表';
                
      COMMENT ON COLUMN litemall_time_discount.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_time_discount.goods_id IS '商品ID';
                    
      COMMENT ON COLUMN litemall_time_discount.product_id IS 'SKU ID（null表示全部SKU）';
                    
      COMMENT ON COLUMN litemall_time_discount.discount_type IS '折扣类型';
                    
      COMMENT ON COLUMN litemall_time_discount.discount_value IS '折扣值（金额或折扣率）';
                    
      COMMENT ON COLUMN litemall_time_discount.start_time IS '开始时间';
                    
      COMMENT ON COLUMN litemall_time_discount.end_time IS '结束时间';
                    
      COMMENT ON COLUMN litemall_time_discount.status IS '状态';
                    
      COMMENT ON COLUMN litemall_time_discount.stock_limit IS '折扣库存（0不限）';
                    
      COMMENT ON COLUMN litemall_time_discount.remark IS '备注';
                    
      COMMENT ON COLUMN litemall_time_discount.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_time_discount.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_time_discount.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_flash_sale IS '秒杀活动表';
                
      COMMENT ON COLUMN litemall_flash_sale.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_flash_sale.goods_id IS '商品ID';
                    
      COMMENT ON COLUMN litemall_flash_sale.product_id IS 'SKU ID（null表示全部SKU）';
                    
      COMMENT ON COLUMN litemall_flash_sale.flash_price IS '秒杀价';
                    
      COMMENT ON COLUMN litemall_flash_sale.total_stock IS '活动总库存';
                    
      COMMENT ON COLUMN litemall_flash_sale.max_per_user IS '每人限购';
                    
      COMMENT ON COLUMN litemall_flash_sale.max_per_order IS '每单限购';
                    
      COMMENT ON COLUMN litemall_flash_sale.status IS '状态';
                    
      COMMENT ON COLUMN litemall_flash_sale.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_flash_sale.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_flash_sale.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_pin_tuan_activity IS '拼团活动表';
                
      COMMENT ON COLUMN litemall_pin_tuan_activity.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_pin_tuan_activity.goods_id IS '商品ID';
                    
      COMMENT ON COLUMN litemall_pin_tuan_activity.product_id IS 'SKU ID（null表示全部SKU）';
                    
      COMMENT ON COLUMN litemall_pin_tuan_activity.pin_tuan_price IS '拼团价';
                    
      COMMENT ON COLUMN litemall_pin_tuan_activity.min_user_count IS '成团人数门槛';
                    
      COMMENT ON COLUMN litemall_pin_tuan_activity.max_user_count IS '最多人数';
                    
      COMMENT ON COLUMN litemall_pin_tuan_activity.expire_hours IS '有效时间（小时）';
                    
      COMMENT ON COLUMN litemall_pin_tuan_activity.status IS '状态';
                    
      COMMENT ON COLUMN litemall_pin_tuan_activity.remark IS '备注';
                    
      COMMENT ON COLUMN litemall_pin_tuan_activity.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_pin_tuan_activity.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_pin_tuan_activity.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_aftersale IS '售后表';
                
      COMMENT ON COLUMN litemall_aftersale.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_aftersale.aftersale_sn IS '售后编号';
                    
      COMMENT ON COLUMN litemall_aftersale.order_id IS '订单ID';
                    
      COMMENT ON COLUMN litemall_aftersale.user_id IS '用户ID';
                    
      COMMENT ON COLUMN litemall_aftersale.type IS '售后类型';
                    
      COMMENT ON COLUMN litemall_aftersale.reason IS '退款原因';
                    
      COMMENT ON COLUMN litemall_aftersale.amount IS '退款金额';
                    
      COMMENT ON COLUMN litemall_aftersale.pictures IS '退款凭证图片链接数组';
                    
      COMMENT ON COLUMN litemall_aftersale.comment IS '退款说明';
                    
      COMMENT ON COLUMN litemall_aftersale.status IS '售后状态';
                    
      COMMENT ON COLUMN litemall_aftersale.handle_time IS '管理员操作时间';
                    
      COMMENT ON COLUMN litemall_aftersale.add_time IS '添加时间';
                    
      COMMENT ON COLUMN litemall_aftersale.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_aftersale.deleted IS '逻辑删除';
                    
      COMMENT ON COLUMN litemall_aftersale.order_item_id IS '订单商品项ID';
                    
      COMMENT ON COLUMN litemall_aftersale.process_note IS '处理备注';
                    
      COMMENT ON COLUMN litemall_aftersale.process_time IS '处理时间';
                    
      COMMENT ON TABLE litemall_groupon IS '团购活动表';
                
      COMMENT ON COLUMN litemall_groupon.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_groupon.order_id IS '订单ID';
                    
      COMMENT ON COLUMN litemall_groupon.groupon_id IS '如果是开团用户，则groupon_id是0；如果是参团用户，则groupon_id是团购活动ID';
                    
      COMMENT ON COLUMN litemall_groupon.rules_id IS '团购规则ID';
                    
      COMMENT ON COLUMN litemall_groupon.user_id IS '用户ID';
                    
      COMMENT ON COLUMN litemall_groupon.share_url IS '团购分享图片地址';
                    
      COMMENT ON COLUMN litemall_groupon.creator_user_id IS '开团用户ID';
                    
      COMMENT ON COLUMN litemall_groupon.creator_user_time IS '开团时间';
                    
      COMMENT ON COLUMN litemall_groupon.status IS '团购活动状态';
                    
      COMMENT ON COLUMN litemall_groupon.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_groupon.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_groupon.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_order_goods IS '订单商品表';
                
      COMMENT ON COLUMN litemall_order_goods.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_order_goods.order_id IS '订单ID';
                    
      COMMENT ON COLUMN litemall_order_goods.goods_id IS '商品ID';
                    
      COMMENT ON COLUMN litemall_order_goods.goods_name IS '商品名称';
                    
      COMMENT ON COLUMN litemall_order_goods.goods_sn IS '商品编号';
                    
      COMMENT ON COLUMN litemall_order_goods.product_id IS '货品ID';
                    
      COMMENT ON COLUMN litemall_order_goods.number IS '购买数量';
                    
      COMMENT ON COLUMN litemall_order_goods.price IS '售价';
                    
      COMMENT ON COLUMN litemall_order_goods.specifications IS '规格列表';
                    
      COMMENT ON COLUMN litemall_order_goods.pic_url IS '商品/货品图片';
                    
      COMMENT ON COLUMN litemall_order_goods.comment IS '订单商品评论，如果是-1，则超期不能评价；如果是0，则可以评价；如果其他值，则是comment表里面的评论ID。';
                    
      COMMENT ON COLUMN litemall_order_goods.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_order_goods.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_order_goods.deleted IS '逻辑删除';
                    
      COMMENT ON COLUMN litemall_order_goods.actual_pay_amount IS '实付金额';
                    
      COMMENT ON TABLE litemall_flash_sale_session IS '秒杀场次表';
                
      COMMENT ON COLUMN litemall_flash_sale_session.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_flash_sale_session.flash_sale_id IS '秒杀活动ID';
                    
      COMMENT ON COLUMN litemall_flash_sale_session.session_start IS '场次开始时间';
                    
      COMMENT ON COLUMN litemall_flash_sale_session.session_end IS '场次结束时间';
                    
      COMMENT ON COLUMN litemall_flash_sale_session.session_stock IS '场次库存';
                    
      COMMENT ON COLUMN litemall_flash_sale_session.session_status IS '场次状态（0未开始/1进行中/2已结束）';
                    
      COMMENT ON COLUMN litemall_flash_sale_session.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_flash_sale_session.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_flash_sale_session.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_pin_tuan_group IS '拼团开团记录表';
                
      COMMENT ON COLUMN litemall_pin_tuan_group.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_pin_tuan_group.activity_id IS '拼团活动ID';
                    
      COMMENT ON COLUMN litemall_pin_tuan_group.creator_user_id IS '团长用户ID';
                    
      COMMENT ON COLUMN litemall_pin_tuan_group.order_id IS '团长的订单ID';
                    
      COMMENT ON COLUMN litemall_pin_tuan_group.status IS '拼团状态';
                    
      COMMENT ON COLUMN litemall_pin_tuan_group.expire_time IS '过期时间';
                    
      COMMENT ON COLUMN litemall_pin_tuan_group.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_pin_tuan_group.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_pin_tuan_group.deleted IS '逻辑删除';
                    
      COMMENT ON TABLE litemall_pin_tuan_member IS '拼团参团记录表';
                
      COMMENT ON COLUMN litemall_pin_tuan_member.id IS 'Id';
                    
      COMMENT ON COLUMN litemall_pin_tuan_member.group_id IS '拼团团ID';
                    
      COMMENT ON COLUMN litemall_pin_tuan_member.user_id IS '用户ID';
                    
      COMMENT ON COLUMN litemall_pin_tuan_member.order_id IS '订单ID';
                    
      COMMENT ON COLUMN litemall_pin_tuan_member.add_time IS '创建时间';
                    
      COMMENT ON COLUMN litemall_pin_tuan_member.update_time IS '更新时间';
                    
      COMMENT ON COLUMN litemall_pin_tuan_member.deleted IS '逻辑删除';
                    
