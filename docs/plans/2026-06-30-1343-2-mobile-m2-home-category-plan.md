# mobile-m2 首页 & 分类导航

> Plan Status: active
> Last Reviewed: 2026-06-30
> Source: `docs/backlog/mobile-frontend-roadmap.md` Mobile Phase 2；`docs/analysis/2026-06-21-mobile-mall-functional-design-analysis.md`（首页 feed 节奏、运营打标三楼层、搜索历史模式）
> Related: 前置 `2026-06-30-1343-1-mobile-m1-scaffold-infra-plan.md`（M1 必须先完成）；后续 `2026-06-30-1343-3-mobile-m3-product-detail-cart-plan.md`（M3 消费本计划商品列表入口）
> Audit: required

## Current Baseline

**前置（M1 交付后成立）：**
- `apps/mall-mobile/` 可 dev 启动，渲染器/env/路由/Tab/全局状态/auth/半游客就绪（见 M1 Closure）。
- 4 个 Tab 占位骨架（首页/分类/购物车/我的）待填充实体内容。

**后端 API（已 done，本计划消费）：**
- 广告/Banner：`LitemallAd` 公开列表（`position=1` 首页 banner）。
- 商品分类树：`LitemallCategory` 多级树查询。
- 商品列表/新品/热销/推荐：`LitemallGoods` 列表（按 `isNew`/`isHot`/分类筛选 + 分页）。
- 专题：`LitemallTopic` 公开列表 + 详情。
- 关键词：`LitemallKeyword`（热门关键词）；搜索历史客户端持久（设计分析 §2.1：本地历史最多 N 条去重可清空）。
- 品牌：`LitemallBrand` 列表 + 详情。

**移动端组件就绪：** `flux-renderers-mobile`（pull-refresh / infinite-scroll / countdown / notice-bar）+ flux `loop`/`cards`/`carousel` 类渲染器（`flux-guide/01-quickstart.md` §13 loop、`design-patterns/cards.md`、`mobile/` 集成范式）。

**Gap：** 首页/分类/搜索/品牌均为占位骨架，无实体数据消费与交互。

## Goals

- 首页：Banner 轮播 + 快捷入口 + 新品/热销推荐楼层 + 专题入口，消费既有 API，支持下拉刷新。
- 分类页：一级分类 tab + 二级分类列表 + 商品网格（触底加载更多）。
- 搜索入口 + 搜索页：热门关键词 + 本地历史（去重/清空）+ 搜索结果分页。
- 品牌列表 + 品牌详情（品牌商品列表）。

## Non-Goals

- 商品详情页（轮播/SKU/评价）—— M3。
- 购物车/加购 —— M3。
- 后端改动 —— 基本纯消费既有 API；**唯一例外：Brand 公开访问**（`LitemallBrandBizModel` 为裸 `CrudBizModel`，无 `@Auth(publicAccess=true)` 方法，匿名浏览会 401），需补最小公开查询方法（对齐 Ad/Topic/Category peer 模式，见 Phase 4）。
- 营销价拼接到列表卡片、库存语义化 —— M9（依赖增强后端）。
- 首页运营打标（badge 装饰 / 新品·热销·推荐视觉标记）—— M9（本计划仅按 `isNew`/`isHot` 字段**筛选**楼层商品，与 M9 的打标展示正交）。
- DIY 可视化装修 —— 远期（设计分析 M-21，P2 独立大特性）。
- 结构化评价展示（好评率/标签云/有图筛选）—— M9（后端 `getCommentSummary` 已就绪，M9 负责展示 UI）。

## Task Route

- Type: `implementation-only change`（业务设计已在 `docs/design/product-catalog.md`、`docs/design/app-overview.md` 落地）
- Owner Docs: `docs/design/product-catalog.md`（分类/商品/品牌/搜索语义）、`docs/design/app-overview.md`（首页结构）、`docs/backlog/mobile-frontend-roadmap.md`（M2 范围 + mobile 组件复用强制）
- Skill Selection Basis: 见各 phase。总体为 nop-chaos-flux 移动端，非 AMIS。

## Infrastructure And Config Prereqs

- M1 已交付（app 启动 + env fetcher 命中后端 GraphQL）。
- 本地后端运行（同 M1）。
- 无新增密钥/迁移。

## Execution Plan

### Phase 1 - 首页（Banner + 快捷入口 + 推荐 + 专题）

Status: planned
Targets: `src/pages/home/`、首页 schema 片段
Required Skill: `none`（nop-chaos-flux 移动端，非 AMIS；`nop-frontend-dev` 触发词 view.xml/AMIS 不匹配。方法源：`flux-guide/mobile/README.md` page→pull-refresh→infinite-scroll→list 组合、`flux-guide/design-patterns/cards.md`、`flux-guide/01-quickstart.md` §9 data-source/§13 loop）

- Item Types: `Add | Decision`
- Prereqs: M1 完成

- [ ] **Skill loading gate:** 通读 `flux-guide/mobile/README.md`（事件驱动/请求下沉/组合层级）、`flux-guide/design-patterns/cards.md`（卡片列表）、`flux-guide/01-quickstart.md` §9（data-source）+ §13（loop）+ §2（page+initApi）；复阅 `docs/design/app-overview.md` 首页结构 + `docs/design/product-catalog.md`。列出已读路径。
  - Docs read: <执行时填入>
- [ ] **Decision D1（首页数据编排方式）：** 抉择——首页用 `data-source`（命名数据源）分别拉 banner/新品/热销/专题，楼层组件消费各 source（请求下沉，符合 `mobile/README.md` §1）。备选（单一 initApi 聚合）——否决理由：后端无首页聚合 API，前端多 data-source 更贴合既有 GraphQL 契约且独立可刷新。残留风险：首屏多请求——可接受（移动端常见）。
- [ ] **Add:** Banner 轮播（`LitemallAd.listActiveAds` 返回全部启用广告，**`position==1` 过滤在前端楼层完成**——后端不做 position 过滤）+ 快捷入口网格 + 新品/热销两推荐楼层（`LitemallGoods` isNew/isHot 分页）+ 专题入口（`LitemallTopic`）。
- [ ] **Add:** 首页 `page.pullRefresh` + pull-refresh 下拉刷新各 data-source。**推荐楼层为首屏首页 + 下拉刷新重载（不做触底 infinite-scroll）；触底加载归 M3 商品列表/分类商品网格。**
- [ ] **Add:** 骨架屏（加载中）+ 空态（无 banner / 无新品热销 / 无专题）+ 错误态 + 重试，覆盖首页各楼层。
- [ ] **Proof:** vitest（data-source 接线 + 楼层渲染 mock + position==1 过滤）；手动烟测首页加载/下拉刷新/空错态；`typecheck`+`build` 通过。

Exit Criteria:

- [ ] 首页四模块（banner/快捷入口/推荐/专题）消费既有 API 渲染正确，下拉刷新可用，position==1 前端过滤生效
- [ ] 列表/网格 M0 触摸基线（≥44×44px）满足
- [ ] 骨架/空/错/重试态覆盖首页各楼层
- [ ] No owner-doc update required（无业务语义变更）
- [ ] `docs/logs/` 更新

### Phase 2 - 分类页（一级 tab + 二级列表 + 商品网格）

Status: planned
Targets: `src/pages/category/`
Required Skill: `none`（理由同 Phase 1；方法源：`flux-guide/design-patterns/tabs.md`、`flux-guide/mobile/infinite-scroll.md`、`flux-guide/design-patterns/cards.md`）

- Item Types: `Add`
- Prereqs: Phase 1

- [ ] **Skill loading gate:** 通读 `flux-guide/design-patterns/tabs.md`（Tab 布局）、`flux-guide/mobile/infinite-scroll.md`（触底加载）、`flux-guide/mobile/README.md` 组合范式。列出已读路径。
  - Docs read: <执行时填入>
- [ ] **Add:** 一级分类 tab（左侧 rail 或顶部，`LitemallCategory` 一级）+ 二级分类列表 + 商品网格（按 categoryId 筛选 `LitemallGoods` 分页）。
- [ ] **Add:** infinite-scroll 触底加载更多（`finishedText`/`errorText` 配置）；分类切换重置分页。
- [ ] **Add:** 骨架屏 + 空态（该分类无商品）+ 错误态 + 重试。
- [ ] **Proof:** vitest（分类切换 + 分页 mock）；手动烟测；`typecheck`+`build`。

Exit Criteria:

- [ ] 分类树消费 + tab 切换 + 触底分页正确
- [ ] 分类 tab / 商品网格交互元素满足 M0 44×44px 触摸基线
- [ ] 骨架/空/错/重试态覆盖分类商品网格
- [ ] No owner-doc update required
- [ ] `docs/logs/` 更新

### Phase 3 - 搜索（入口 + 搜索页 + 历史）

Status: planned
Targets: `src/pages/search/`
Required Skill: `none`（理由同 Phase 1；方法源：`flux-guide/design-patterns/form.md` 输入+防抖、`flux-guide/mobile/infinite-scroll.md`、`docs/design/product-catalog.md` 搜索语义）

- Item Types: `Add | Decision`
- Prereqs: Phase 2

- [ ] **Skill loading gate:** 通读 `flux-guide/design-patterns/form.md`、`flux-guide/mobile/infinite-scroll.md`、`flux-guide/02-reference.md`（表达式/防抖）；复阅 `docs/design/product-catalog.md` 搜索。列出已读路径。
  - Docs read: <执行时填入>
- [ ] **Decision D2（搜索历史存储）：** 抉择——搜索历史客户端 localStorage（去重、**上限 10 条**、置顶、可清空），对齐设计分析 §2.1（:「本地历史（最多 10 条，置顶去重，可清空，localStorage 持久化）」）。备选（后端持久）——否决理由：后端无用户搜索历史 API，且 c-shopping/芋道均为本地存储模式。残留风险：无（多端不同步可接受，MVP 级）。
- [ ] **Add:** 搜索入口（首页/分类页）+ 搜索页：热门关键词（`LitemallKeyword`）+ 本地历史（去重/上限 10 条/清空）+ 输入防抖触发结果分页（`LitemallGoods` 关键词搜索 + infinite-scroll `finishedText`/`errorText`）。
- [ ] **Add:** 搜索骨架屏 + 空态（无历史/无结果）+ 错误态 + 重试。
- [ ] **Proof:** vitest（防抖触发 + 历史去重/上限/清空 mock）；手动烟测；`typecheck`+`build`。

Exit Criteria:

- [ ] 搜索（热词/历史/防抖/结果分页）消费既有 API 正确
- [ ] 搜索历史 localStorage 去重/上限 10 条/置顶/清空有 vitest 覆盖
- [ ] 搜索交互元素满足 M0 44×44px 触摸基线
- [ ] 骨架/空/错/重试态覆盖搜索结果
- [ ] No owner-doc update required
- [ ] `docs/logs/` 更新

### Phase 4 - 品牌公开访问（后端补齐）+ 品牌列表/详情（前端）

Status: planned
Targets: `app-mall-service/src/main/java/app/mall/service/entity/LitemallBrandBizModel.java`（新增 `frontList`/`frontDetail` `@Auth(publicAccess=true)`）、`src/pages/brand/`（前端）
Required Skill: `nop-backend-dev` + `nop-testing`（后端新增 `@BizQuery` 公开方法，须按规则 #15 经 `IGraphQLEngine` 测试）；前端品牌页 `none`（nop-chaos-flux，非 AMIS）

- Item Types: `Add | Decision | Proof`
- Prereqs: Phase 1（前端壳就绪）

- [ ] **Skill loading gate:** 后端——加载 `nop-backend-dev` skill，通读其 routing table 必读文档（CrudBizModel 公开查询模式、`@Auth(publicAccess=true)` 约定）+ peer 范本 `LitemallTopicBizModel.frontList/frontDetail`（`app-mall-service/.../entity/LitemallTopicBizModel.java`）；加载 `nop-testing` skill，通读 `IGraphQLEngine` 录制回放测试约定。前端——通读 `flux-guide/design-patterns/cards.md`、`flux-guide/mobile/infinite-scroll.md`。列出已读路径。
  - Docs read: <执行时填入>
- [ ] **Decision D3（Brand 公开访问缺口处置）：** 抉择——审计发现 `LitemallBrandBizModel` 为裸 `CrudBizModel`（无 `publicAccess` 方法，匿名浏览 401），与 M2 半游客品牌浏览目标冲突；新增 `frontList`（分页，支持分页参数）/`frontDetail(id)` 两个 `@BizQuery @Auth(publicAccess=true)` 方法，**完全对齐 `LitemallTopicBizModel.frontList/frontDetail` peer 模式**（无新业务语义、无 ORM 改动）。备选（a 牌品牌页强制登录）——否决理由：破坏半游客浏览体验、与 Ad/Topic/Category 公开 peer 不对称。备选（b 延后品牌到独立 successor）——否决理由：品牌属 M2 浏览结果面，拆出制造跨计划依赖。残留风险：无（最小公开查询，peer 已验证模式）。
- [ ] **Add:** 后端 `LitemallBrandBizModel.frontList`/`frontDetail`（`@BizQuery @Auth(publicAccess=true)`，仿 Topic peer；按规则 #11 不绕过 `I*Biz`/管道）。
- [ ] **Proof:** 后端新增 `@BizQuery` 方法经 `IGraphQLEngine` 测试（`JunitAutoTestCase` 录制回放：frontList 分页 + frontDetail 存在/不存在；`@Auth(publicAccess=true)` 匿名可调）。
- [ ] **Add:** 前端品牌列表（消费 `frontList`）+ 品牌详情（`frontDetail` + 品牌商品列表分页）+ 骨架/空/错/重试态。
- [ ] **Proof:** 前端 vitest（品牌列表/详情 mock）+ 手动烟测；`pnpm typecheck`+`build`；后端 `./mvnw test`。

Exit Criteria:

- [ ] 后端 `frontList`/`frontDetail` 落地且 `@Auth(publicAccess=true)`，经 `IGraphQLEngine` 测试（匿名可调 + 分页 + 详情存在/不存在）
- [ ] 前端品牌列表/详情消费新公开 API 渲染正确，骨架/空/错/重试态覆盖
- [ ] 品牌交互元素满足 M0 44×44px 触摸基线
- [ ] owner doc 更新：`docs/design/product-catalog.md` 若声明品牌公开访问契约则同步（若未声明则 No owner-doc update required）
- [ ] `docs/logs/` 更新

## Plan Audit

- Status: passed
- Auditor / Agent: MISSION_DRIVER plan-review pass（独立审核，fresh session）
- Evidence: 格式合规（全部必需 section + 字段 + Phase 结构）；Exit Criteria 可测；Goals/Non-Goals 边界清晰无 scope creep；Required Skill 符合规则 #14/#15（Phase 1-3 前端 `none`+非 AMIS 理由充分；Phase 4 后端 `nop-backend-dev`+`nop-testing`，新增 `@BizQuery` 经 `IGraphQLEngine` 测试）；Decisions D1/D2/D3 均记录抉择/备选/残留风险；Deferred 项均带 Successor 触发条件；无 anti-slacking 违禁词。发现 2 处 Minor 已修正：① Phase 4 Exit Criteria 重复的 `` `docs/logs/` 更新 `` 行去重；② Phase 4 Item Types 去掉无对应 item 的 `Fix`（frontList/frontDetail 为净新增方法属 `Add`）。无 Blocker/Major。

## Closure Gates

- [ ] in-scope behavior is complete
- [ ] relevant docs are aligned（Phase 4 后端公开访问若涉 owner doc 契约声明则同步）
- [ ] verification has run（前端 `pnpm --filter @nop-chaos/mall-mobile typecheck`+`build`+`test` + 手动 e2e 烟测；Phase 4 后端 `./mvnw test`）
- [ ] Phase 4 新增 `@BizQuery`（`frontList`/`frontDetail`）通过 `IGraphQLEngine` 测试（`JunitAutoTestCase` 录制回放）；前端消费页无新增后端方法故 IGraphQLEngine 不适用
- [ ] no in-scope item downgraded to deferred/follow-up
- [ ] plan audit passed before implementation
- [ ] each phase has `Required Skill`（Phase 1-3 前端 `none`+非 AMIS 理由；Phase 4 后端 `nop-backend-dev`+`nop-testing`、前端 `none`，均符合规则 #14/#15）
- [ ] skill loading verification: flux-guide 路由文档 + Phase 4 nop-backend-dev/nop-testing routing 必读文档已读并列路径
- [ ] text consistency verified
- [ ] closure audit performed by different agent/session
- [ ] closure evidence exists in files

## Deferred But Adjudicated

### 营销价拼接到列表卡片 / 库存语义化

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 属 M9 范围且部分依赖增强后端（设计分析 M-18，P2）。
- Successor Required: `yes`（触发条件：M9 启动时）

### 首页运营打标（badge 装饰）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 本计划仅按 `isNew`/`isHot` 字段**筛选**楼层商品（数据选取），与 M9 的运营打标**视觉展示**（badge/角标）正交；打标展示属 M9。
- Successor Required: `yes`（触发条件：M9 启动时）

### 首页 DIY 可视化装修

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 实现复杂度极高，设计分析 M-21 列为 P2 独立大特性；M1-M2 固定结构为其过渡方案。
- Successor Required: `yes`（触发条件：DIY 装修独立立项时）

## Closure

<!-- 闭合审计须由独立 subagent 执行，此处留空。 -->

Status Note: <待闭合填写>

Closure Audit Evidence:

- Reviewer / Agent: <独立 reviewer>
- Evidence: <task id / 记录>

Follow-up:

- 营销价拼接/库存语义化（见 Deferred，M9 启动时）
- 首页 DIY 装修（见 Deferred，独立立项时）
