# 应用总览

## 目的

说明当前稳定支持的应用级产品基线。

## 主要界面或页面

- 管理后台（侧边栏导航的后台实体管理页面）
- 商城前台（AMIS 页面）：
  - 首页（广告轮播、分类导航、新品推荐、人气推荐、公告）
  - 分类浏览页（分类树侧边栏、品牌筛选、排序、商品卡片网格）
  - 商品详情页（商品图片、SKU 选择、加购、评论区、收藏）
  - 购物车页（商品列表、数量修改、勾选、结算预览）
  - 结算页（地址选择、优惠券选择、价格汇总、订单提交）
  - 订单结果页（订单号展示、支付按钮）
- 用户中心（AMIS 页面）：
  - 登录/注册页（登录、注册、密码重置）
  - 个人中心首页（用户信息、订单状态入口、功能菜单）
  - 订单列表页（按状态分类、订单操作）
  - 订单详情页（全量订单信息、价格明细）
  - 收货地址管理页（CRUD、默认地址切换）

## 主要导航模型

- 后台：采用侧边栏导航，按用户管理、商品管理、订单管理、营销/内容运营、系统管理等分组组织
- 商城：围绕分类、搜索/发现、购物车和用户中心组织前台导航

## 主要用户角色

- 超级管理员：拥有全系统访问权限
- 管理员：在分配职责范围内执行店铺运营
- 商城用户：负责浏览、下单购买和订单自助管理

## 核心业务流程

- 商品管理：分类 -> 品牌 -> 商品 -> 商品规格项（SKU） -> 前台展示 -> [product-catalog.md](product-catalog.md)
- 订单流程：购物车 -> 结算 -> 支付 -> 发货 -> 完成 -> 符合条件时退款/售后 -> [order-and-cart.md](order-and-cart.md)
- 用户管理：注册 -> 资料 -> 地址 -> 订单 -> [user-and-address.md](user-and-address.md)
- 营销与内容运营：优惠券、团购、互动能力、营销内容 -> [marketing-and-promotions.md](marketing-and-promotions.md)
- 系统运营：配置、存储、通知、运营任务、日志、报表 -> [system-configuration.md](system-configuration.md)

## 关键领域区域

- 身份与访问：User、Address、Admin、Role、Permission
- 商品目录：Category、Brand、Goods、GoodsProduct（SKU）、GoodsSpecification、GoodsAttribute
- 交易领域：Cart、Order、OrderGoods、支付状态、发货状态、退款与售后状态
- 营销与互动：Coupon、CouponUser、GrouponRules、Groupon、Comment、Collect、Footprint、SearchHistory、Keyword
- 内容与运营：Topic、Ad、Issue、Feedback、Notice、Storage、Region、配置、管理员日志、统计

## 集成点

- 支付能力，包括按配置启用的微信支付
- 面向商品、品牌、头像和内容素材的文件存储能力
- 平台认证与授权能力
- 面向用户消息和运营消息的通知投递能力

## 边界

- 本文件负责应用层的界面范围、角色、流程和领域区域说明。
- 持久化实体、字段和字典定义以 `model/*.orm.xml` 为准。
- 技术实现细节属于 `docs/architecture/`。
- 实施顺序属于 `docs/backlog/` 或计划文件，不属于本总览。

## 规则

保持本文件稳定且面向产品。如果某个功能改变了应用支持基线，应在同一次变更中更新本文件或更窄的 owner doc。

不要在这里重复 `docs/architecture/project-vision.md` 中的长期产品愿景，也不要重复 `docs/backlog/` 中的实施顺序。
