# 维度 08：AMIS 页面层

> 审计日期：2026-06-16 | 审计对象：整个项目 | Round 1 初审 + 主 agent 复核

## 第 1 轮（初审）发现（按严重程度）

### [维度08-1] Admin 订单管理页缺少发货按钮及状态分组 — P1
- **文件**: `LitemallOrder/LitemallOrder.view.xml:51-60`
- **证据**: rowActions 仅 row-view-button；ship() 方法完整实现但前端无按钮调用；缺 orderStatus tab 分组
- **风险**: 管理员无法后台发货，订单卡在 201 无法推进 301
- **建议**: 增 ship-button 调 @mutation:LitemallOrder__ship + 状态 tab

### [维度08-2] Admin 商品管理页缺少上架/下架按钮 — P1
- **文件**: `LitemallGoods/LitemallGoods.view.xml:157-181`
- **现状**: onSale()/offSale() 完整实现，前端无按钮；isOnSale 仅展示列

### [维度08-3] Storefront 订单实付金额计算错误（系统性，3 页面） — P1
- **文件**: `order-detail.page.yaml:177`、`order-list.page.yaml:85,150,192`、`order-result.page.yaml:47`
- **证据**: 用 `goodsPrice+freightPrice-couponPrice`，忽略 grouponPrice/integralPrice；实体有 actualPrice 字段
- **风险**: 团购订单显示金额高于实际扣款
- **建议**: 四处替换为 `${data.actualPrice}`

### [维度08-4] Storefront 订单状态映射不完整，4 种状态显示"未知" — P1
- **文件**: `order-list.page.yaml:58`、`order-detail.page.yaml:44`
- **现状**: 只覆盖 101/102/201/301/>=401；103/202/203/204 命中"未知"

### [维度08-5] 结算页优惠券选择器 labelField 指向不存在的字段 — P1
- **文件**: `checkout/checkout.page.yaml:111-117`
- **证据**: `labelField: description`；CouponUser 无 description 字段（Coupon 的是 desc）
- **风险**: 下拉标签全空，用户无法区分券
- **建议**: 后端增计算字段或改 labelTpl

### [维度08-6] Admin 售后退款按钮配置矛盾（batch=true + 单 id） — P1
- **文件**: `LitemallAftersale.view.xml:69-79`
- **证据**: refund-button 在 rowActions 内但 batch="true"，data 用单数 `$id`
- **风险**: 退款按钮点击后 $id 无值致 NPE/NOT_FOUND；涉及资金操作
- **建议**: 移除 batch="true" 或改 batchRefund(Set ids)

### [维度08-7] Admin 营销管理菜单（优惠券/广告/专题/团购）全部注释掉 — P1
- **文件**: `auth/app-mall.action-auth.xml:80-101`
- **现状**: promotion-manage 顶级菜单被 XML 注释；仅 test-orm-app-mall 可访问
- **风险**: 正式部署移除 test 菜单后管理员无法管理营销

### [维度08-8] Admin 团购规则/活动 view.xml 空壳，缺发布/状态管理按钮 — P1
- **文件**: `LitemallGrouponRules.view.xml:1-20`、`LitemallGroupon.view.xml:1-19`
- **现状**: publishRules/unpublishRules 实现完整但无按钮；无状态 tab

### [维度08-9] 个人中心订单状态计数全部硬编码为 0 — P2
- **文件**: `user-center.page.yaml:56,65,74,83`

### [维度08-10] Admin 菜单"意见反馈"指向错误实体（LitemallIssue 而非 Feedback） — P2
- **文件**: `app-mall.action-auth.xml:24-26`

### [维度08-11] 售后类型 label 映射与 ORM 常量语义不匹配 — P2
- **文件**: `aftersale-list.page.yaml:86`、`aftersale-apply.page.yaml:83-87`
- **现状**: type 1 标"退货退款"（实际无需退货）；申请表单缺 type 2（真正退货退款）→ refund 的库存回补永不触发

### [维度08-12] 团购活动详情页 visibleOn 嵌入 tpl 字符串永不生效 — P2
- **文件**: `groupon-activity-detail.page.yaml:154-155`

### [维度08-13] 团购规则详情页用 listAvailableRules+客户端过滤替代按 id 查询 — P2
- **文件**: `groupon-rules-detail.page.yaml:49-63`

### [维度08-14] 首页"人气推荐"未按 isHot 过滤 — P2
- **文件**: `home.page.yaml:175-182`

### [维度08-15] 商品评论管理页功能完整但 admin 菜单注释掉 — P3
- **文件**: `app-mall.action-auth.xml:74-76`

## 维度复核结论
主 agent 复核：08-3 金额公式与 09-10 价格一致性呼应；08-11 售后类型与 09-05 GOODS_MISS 库存回补缺陷呼应（前端缺 type 2 选项→refund 库存回补永不触发，前后端双重缺陷）；08-7 菜单注释经 action-auth live 核验。15 项保留。

## 最终保留项

| 编号 | 严重 | 文件 | 一句话摘要 |
|------|------|------|-----------|
| 08-1 | P1 | LitemallOrder.view.xml:51 | 缺发货按钮/状态分组 |
| 08-2 | P1 | LitemallGoods.view.xml:157 | 缺上下架按钮 |
| 08-3 | P1 | order-detail/list/result | 实付金额公式错（3 页面） |
| 08-4 | P1 | order-list/detail:58 | 4 种订单状态显示未知 |
| 08-5 | P1 | checkout.page.yaml:111 | 优惠券 labelField 不存在 |
| 08-6 | P1 | LitemallAftersale.view.xml:69 | 退款按钮 batch+单 id 矛盾 |
| 08-7 | P1 | action-auth.xml:80 | 营销菜单全注释 |
| 08-8 | P1 | Groupon/GrouponRules.view.xml | 团购页空壳 |
| 08-9 | P2 | user-center.page.yaml | 状态计数硬编码 0 |
| 08-10 | P2 | action-auth.xml:24 | 意见反馈指向错误实体 |
| 08-11 | P2 | aftersale-apply:83 | 售后类型映射错+缺 type2 |
| 08-12 | P2 | groupon-activity-detail:154 | visibleOn 嵌 tpl 无效 |
| 08-13 | P2 | groupon-rules-detail:49 | 客户端过滤替代按 id 查 |
| 08-14 | P2 | home.page.yaml:175 | 人气推荐未按 isHot 过滤 |
| 08-15 | P3 | action-auth.xml:74 | 评论菜单注释 |

## 维度评级：Moderate

Storefront 25 页全部注册、三层模型合规、API 引用方法全部存在、admin/storefront 分离正确、售后三 tab 是正面案例。但 8 个 P1 集中在 admin 面单业务按钮缺失（发货/上下架/团购发布）+ 系统性前端计算错误（金额公式/状态映射）+ 核心菜单注释致功能不可达。修复 P1 后可升至 Good。
