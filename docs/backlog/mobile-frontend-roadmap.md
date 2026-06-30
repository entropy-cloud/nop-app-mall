# Mobile Frontend Roadmap

> Last Updated: 2026-06-22
> Source: `docs/design/*.md`, `docs/analysis/2026-06-21-mobile-mall-functional-design-analysis.md`
> Tech Stack: **nop-chaos-flux** (React 19 + JSON-described mobile UI components)

## Purpose

本文定义基于 `nop-chaos-flux` 的移动端商城的阶段划分和全局状态索引。它独立于 `docs/backlog/enhanced-features-roadmap.md`（后端 + AMIS Web/Admin），**不重复后端 API 工作**——所有移动端页面消费已存在的 backend GraphQL APIs（Phase 1-14 已交付）。

**核心用途：** AI 读完本文后即可知道哪些移动端页面/能力尚未实现、哪些已有计划、哪些已经完成，无需重新遍历项目文档和代码。

## Phase Status

> **状态更新只改这里，不改 Phase Details 中的状态行。**

- M1. 项目脚手架 & 基础设施: `done`
- M2. 首页 & 分类导航: `done`
- M3. 商品详情 & 购物车: `todo`
- M4. 地址 & 订单: `todo`
- M5. 支付 & 售后: `todo`
- M6. 个人中心 & 互动: `todo`
- M7. 营销活动: `todo`
- M8. 扩展功能: `todo`
- M9. 运营工具 & 体验增强: `todo`

## Status Values

| Status | 含义 |
|--------|------|
| `todo` | 尚未开始，无对应 plan |
| `planned` | 已有对应 execution plan |
| `done` | 已完成并通过 closure audit |

## Current Baseline

**已有后端 API（移动端可直接消费）：**
- 用户注册/登录/登出/密码重置（LoginApi + NopAuthUser）
- 商品分类树、商品列表、商品详情、搜索（LitemallGoods / LitemallCategory / LitemallKeyword）
- 购物车完整 CRUD（LitemallCart）
- 订单提交/支付/取消/确认/删除/列表（LitemallOrder）
- 地址 CRUD + 默认地址（LitemallAddress）
- 收藏/足迹/评论（LitemallCollect / LitemallFootprint / LitemallComment）
- 优惠券领取/兑换/列表（LitemallCoupon + LitemallCouponUser）
- 团购规则列表/详情/我的团购（LitemallGrouponRules + LitemallGroupon）
- 售后申请/取消/列表/详情（LitemallAftersale）
- 反馈提交（LitemallFeedback）
- 广告/专题/FAQ 公开列表（LitemallAd / LitemallTopic / LitemallIssue）
- 系统配置查询（LitemallSystem）

**移动端未开始：**
- 无移动端前端代码
- nop-chaos-flux 项目脚手架未创建
- 所有 mobile pages 待实现

## Phases

### Mobile Phase 1 - 项目脚手架 & 基础设施

| # | Phase | 依赖后端 |
|---|-------|----------|
| M1 | 项目脚手架 & 基础设施 | Phase 1（用户认证） |

**交付范围：**
- nop-chaos-flux 项目初始化（Vite + React 19 + Zustand）
- 路由框架（页面路由、Tab 导航、页面栈）
- 登录/注册/忘记密码页面
- Token 管理 + 请求拦截 + 自动续期
- 半游客模式（未登录浏览 + 关键操作拦截跳登录）
- 全局状态管理（用户信息、购物车角标）

### Mobile Phase 2 - 首页 & 分类导航

| # | Phase | 依赖后端 |
|---|-------|----------|
| M2 | 首页 & 分类导航 | Phase 2（商品目录） |

**交付范围：**
- 首页（Banner 轮播 + 快捷入口 + 新品/热销推荐 + 专题入口）
- 分类页（一级分类 tab + 二级分类列表 + 商品网格）
- 搜索入口 + 搜索页（热门关键词 + 历史记录 + 搜索结果）
- 品牌列表/详情

### Mobile Phase 3 - 商品详情 & 购物车

| # | Phase | 依赖后端 |
|---|-------|----------|
| M3 | 商品详情 & 购物车 | Phase 2 + Phase 4 |

**交付范围：**
- 商品详情页（轮播、价格、SKU 选择、评价摘要、详情图文）
- 收藏/取消收藏
- 加入购物车
- 购物车页（列表/勾选/数量调整/删除/清空/结算入口）
- 足迹记录

### Mobile Phase 4 - 地址 & 订单

| # | Phase | 依赖后端 |
|---|-------|----------|
| M4 | 地址 & 订单 | Phase 3 + Phase 5 |

**交付范围：**
- 地址管理（列表/新增/编辑/删除/默认设置）
- 地区级联选择（Region）
- 结算页（地址选择 + 商品清单 + 优惠券选择 + 运费 + 提交）
- 订单列表（全部/待付款/待发货/待收货/待评价 Tab）
- 订单详情 + 取消订单 + 确认收货 + 删除订单

### Mobile Phase 5 - 支付 & 售后

| # | Phase | 依赖后端 |
|---|-------|----------|
| M5 | 支付 & 售后 | Phase 5b + Phase 5c |

**交付范围：**
- 支付收银台（模拟支付 + 微信支付 Native 调起）
- 支付结果页
- 售后申请（仅退款/退货退款）
- 售后列表 + 售后详情 + 撤回售后
- 售后进度时间线

### Mobile Phase 6 - 个人中心 & 互动

| # | Phase | 依赖后端 |
|---|-------|----------|
| M6 | 个人中心 & 互动 | Phase 1 + Phase 7 |

**交付范围：**
- 个人中心页（用户信息 + 订单聚合入口 + 功能入口聚合）
- 个人资料编辑（昵称/头像/性别/手机）
- 修改密码
- 收藏列表
- 足迹列表（含清空）
- 评价入口 + 评价提交 + 我的评价

### Mobile Phase 7 - 营销活动

| # | Phase | 依赖后端 |
|---|-------|----------|
| M7 | 营销活动 | Phase 8 + Phase 9 + Phase 15-38 后端 |

**交付范围：**
- 领券中心 + 我的优惠券
- 兑换码兑换
- 团购列表 + 团购详情 + 我的团购
- 限时折扣列表 + 秒杀场次（依赖增强后端完成）
- 满减送信息展示
- 签到页（连续签到 + 积分奖励，依赖增强后端完成）

### Mobile Phase 8 - 扩展功能

| # | Phase | 依赖后端 |
|---|-------|----------|
| M8 | 扩展功能 | Phase 12 + Phase 15-38 后端 |

**交付范围：**
- 消息中心（订单消息/营销消息/系统消息列表 + 未读徽章）
- 积分账户（余额 + 收支流水 + 积分商城入口）
- 钱包余额与充值（余额 + 充值 + 流水，依赖增强后端完成）
- 反馈提交页
- FAQ 页
- 联系客服入口

### Mobile Phase 9 - 运营工具 & 体验增强

| # | Phase | 依赖后端 |
|---|-------|----------|
| M9 | 运营工具 & 体验增强 | 通用 |

**交付范围：**
- 商品详情页面分享功能（海报/链接）
- 首页运营打标（新品/热销/推荐）
- 商品评价结构化展示（好评率 + 标签云 + 有图筛选，依赖增强后端）
- 库存语义化展示（充足/紧张/缺货）
- 通知管理（推送权限引导）

## Backend API Dependency Matrix

| 移动端 Phase | 依赖的后端 Phase | 后端 API 状态 |
|-------------|-----------------|---------------|
| M1 | Phase 1 | ✅ done |
| M2 | Phase 2, Phase 10 | ✅ done |
| M3 | Phase 2, Phase 4, Phase 7 | ✅ done |
| M4 | Phase 3, Phase 5 | ✅ done |
| M5 | Phase 5b, Phase 5c | ✅ done |
| M6 | Phase 1, Phase 7 | ✅ done |
| M7 | Phase 8, Phase 9 + Phase 15-38 | ⏳ 部分待增强 |
| M8 | Phase 12 + Phase 15-38 | ⏳ 部分待增强 |
| M9 | 通用 | ✅ 无阻塞 |

## Rule

- 本文档只覆盖移动端页面/组件/路由工作，不涉及后端 API 新增
- 后端 API 缺口由 `docs/backlog/enhanced-features-roadmap.md` 负责
- 持久化模型以 `model/*.orm.xml` 为准
- 组件风格遵循 nop-chaos-flux 的 JSON 描述规范
- 阶段状态变更只需更新 Phase Status 列表（本文档顶部）
- 每个 `planned`/`in-progress` 阶段由对应 execution plan 负责细节

## nop-chaos-flux Reference

移动端实现**必须直接复用** nop-chaos-flux 的 mobile 机制，不自建移动端 UI 栈。

- **仓库位置：** `~/app/nop-chaos-flux-wt/nop-chaos-flux-master/`
- **开发指南：** `flux-guide/`
  - `README.md`（核心架构 + 文件索引）、`01-quickstart.md`（17 常用代码段）、`02-reference.md`（表达式/API/事件/Action Algebra）
  - `flux-types/`（所有组件 TS 接口，字段知识源）、`design-patterns/`（业务场景 cookbook）
  - `mobile/`（移动端原生组件专题：`README.md` + pull-refresh / infinite-scroll / swipe-cell / countdown / notice-bar）
- **移动端组件包：** `packages/flux-renderers-mobile/`
- **核心原则（来自 `flux-guide/mobile/README.md`）：**
  - 事件驱动、请求下沉——组件不持有数据请求逻辑，统一走 action/data-source 层
  - 与 CRUD/Page 集成（page.pullRefresh → pull-refresh → infinite-scroll → list/cards）
  - M0 触摸基线：最小可交互区域 44×44px
- M1 启动脚手架前，实现者须先通读 `flux-guide/README.md` 与 `flux-guide/mobile/README.md`
