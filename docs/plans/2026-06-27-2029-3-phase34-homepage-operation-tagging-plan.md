# P34 首页运营打标（Homepage Operation Tagging）

> Plan Status: completed
> Last Reviewed: 2026-06-27
> Source: `docs/backlog/enhanced-features-roadmap.md` Phase 34；`docs/design/product-catalog.md`
> Related: 无强依赖（独立轻量增强）；同批次 `2026-06-27-2029-1`（P27）、`2026-06-27-2029-2`（P23）
> Audit: required
> Mission: mall
> Work Item: enhanced-roadmap Phase 34（首页运营打标）

> **执行顺序：** 本计划为 2026-06-27-2029 批次第 3 顺位（N=3）。P27/P23 优先（解除下游阻塞），本计划为独立轻量运营增强，不阻塞也不依赖其他 phase，作为本批次收尾的干净交付。本计划**不触及** `LitemallOrderBizModel.submit()` 价格构成（标记位与促销活动互不干扰，见设计），与 P27/P23 无价格接线交叉。

## Current Baseline

> 实读 live repo（working tree，HEAD `35d90f2` + P15/P26/P16 批次 uncommitted 改动）所得，非记忆。经独立审计复核订正（审计发现三标记位均已建模）。

**三标记位均已建模（关键，roadmap 假设过时）：**

- `LitemallGoods` 已有完整三标记位（litemall 内置字段）：
  - `isNew`（是否新品，propId=13，`app-mall.orm.xml:674`）
  - `isHot`（是否热品，propId=14，`:676`）
  - `isRecommend`（是否推荐，propId=22，`:693`）——**已存在并已 codegen 生成**（entity getter/setter / xmeta `queryable=true` / i18n / Input/OutputBean / `_gen/_LitemallGoods.view.xml:73` grid col / 默认 false）。
- roadmap 扩展字段表（`enhanced-features-roadmap.md:584`）称「LitemallGoods | 34 | newFlag, hotFlag, recommendFlag」**与实际不符（三字段均已存在，且实际命名为 isNew/isHot/isRecommend）**——属 stale，本计划闭合时订正。

**已有基础设施（标记位查询与首页楼层）：**

- `LitemallGoodsBizModel.frontListByFlags(isHot, isNew, categoryId, brandId, page, pageSize, context)`（`@BizQuery`，`LitemallGoodsBizModel.java:143`）**已存在**——按标记位查询商品。已有测试覆盖（`TestLitemallGoodsExtendedBizModel:295-310`）。
- 首页 `home.page.yaml` 已有「新品首发」(isNew=true, `:134-140`) + 「人气推荐」(isHot=true, `:175-181`) 两个楼层区块，均调 `@query:LitemallGoods__frontListByFlags`。

**缺口（本计划实际交付对象——isRecommend 已建模但全程 dormant 未接线）：**

1. **frontListByFlags 不支持 isRecommend**——现有方法签名仅 isHot/isNew，缺推荐过滤。
2. **无推荐楼层**——首页仅新品/人气两楼层，缺「推荐」第三楼层。
3. **后台 delta 视图全程隐藏 isRecommend**——`LitemallGoods.view.xml`（保留层）：
   - grid `bounded-merge`（`:9-75`）仅列 isNew(`:49`)/isHot(`:52`)，**未列 isRecommend** → bounded-merge 自动删除未列 col，运行时 grid 不展示推荐列（虽 `_gen` 基线 `:73` 有 isRecommend col）。
   - edit form layout（`:87-111`）仅 isNew/isHot（`:92`），**无 isRecommend** → 编辑页无法设置推荐标记。
   - query form（`:81-85`）仅 `id goodsSn keywords`，**无任何标记位过滤**。
4. **命名漂移**——`product-catalog.md:192` 称「三个正交标记位：newFlag/hotFlag/recommendFlag」，模型实际为 `isNew`/`isHot`/`isRecommend`（litemall 命名）。design doc 命名与模型不一致。

**前置条件已满足：** P2（商品目录管理）`done`。无下游阻塞或依赖。

**已知交叉：** `product-catalog.md:195` 明确「标记位与促销活动（满减、秒杀等）互不干扰，同一个商品可同时拥有多个标记」——标记位为运营展示位，不进价格构成，与 P15/P23/P26/P27 价格机制无交互。

## Goals

- 将已建模但 dormant 的 `isRecommend`（推荐标记）接线启用，与既有 `isNew`/`isHot` 共同构成三标记位运营能力。
- 扩展 `frontListByFlags` 支持 isRecommend 过滤（向后兼容既有调用）。
- 首页新增「推荐」楼层区块。
- 后台商品页全程暴露三标记位（grid 列 + edit 开关 + query 过滤）。
- 订正 design doc 命名漂移与 roadmap stale（对齐模型 isNew/isHot/isRecommend 实际命名）。
- 新增/扩展路径通过 `IGraphQLEngine` 测试。

## Non-Goals

- 不重命名/复制既有 `isNew`/`isHot`/`isRecommend` 为 `newFlag`/`hotFlag`/`recommendFlag`——复用 litemall 内置字段（均已建模），避免冗余与回归。design doc 命名对齐到模型（见 Phase 1 Decision）。
- 标记位的批量运营操作（批量打标/取消）——属 P36 商品运营增强范围。
- 首页楼层的运营化排序/配置（DIY 装修）——不在当前基线（roadmap 明确 DIY 装修单独规划）。
- 移动端前端（独立 roadmap）。

## Task Route

- Type: `app-layer design change`（命名漂移订正）+ `implementation-only change`（轻量）
- Owner Docs: `docs/design/product-catalog.md`（首页运营打标章节命名对齐 + 推荐位）
- Skill Selection Basis: 扩展 `@BizQuery` 方法 → `nop-backend-dev` + `nop-testing`（规则 #15）；AMIS 页面（首页楼层 + 后台 delta bounded-merge/grid/edit/query）→ `nop-frontend-dev`

## Protected Area

> 见 `docs/context/ai-autonomy-policy.md`。本计划触及以下受保护区域，**执行前须取得人工确认**：
>
> - **`model/app-mall.orm.xml`（ask-first）：** 本计划**预期不改 ORM 模型**（三标记位 isNew/isHot/isRecommend 均已建模，frontListByFlags 已存在）。本计划仅扩展 Java BizModel 方法签名（新增 @Optional 入参）+ AMIS view delta（bounded-merge 加 col / edit layout 加字段 / query 加过滤）+ docs 订正。**不触发 model ask-first，不触发 codegen 重生成，无 DDL 变更。**
> - 本计划**不**触及 `app-mall-delta`。
>
> 证据要求：无需 ask-first（无模型改动）；Java 方法新增 @Optional 入参为向后兼容（既有调用 `frontListByFlags(null,null,...)` 不受影响）。

## Infrastructure And Config Prereqs

- **基础基线锁定（前置门）：** 本批次计划建立在当前 working tree 基线之上。执行前须先将 P15/P26/P16 批次变更提交锁定为稳定 HEAD（同 P27/P23 前置门）。
- 推荐楼层商品数量等可配项 → 可硬编码默认（与现有新品/人气楼层一致 pageSize），无需新增配置；如需可配后续走 `NopSysVariable`。
- 无外部服务/端口/密钥依赖。无数据迁移（isRecommend 列已存在，存量商品默认 false）。

## Execution Plan

### Phase 1 — 设计对齐与命名漂移订正（Decision-heavy，lightweight）

Status: completed
Targets: `docs/design/product-catalog.md`
Required Skill: `none`（纯 docs 业务语义对齐；无模型/代码改动。无 skill 匹配「写设计文档」）

- Item Types: `Decision | Add`
- Prereqs: 无

- [x] **Skill loading gate:** 扫描 available skills；本阶段 docs-only，无匹配。已读 owner doc：`product-catalog.md`（首页运营打标 184-195）、`enhanced-features-roadmap.md` Phase 34 + 扩展字段表 :584。
  - Docs read: `docs/design/product-catalog.md`（首页运营打标 201-212 业务意图/规则）、`docs/backlog/enhanced-features-roadmap.md`（:428-439 Phase 34、:582-590 扩展字段表）、`model/app-mall.orm.xml`（:674 isNew/:676 isHot/:693 isRecommend 三标记位已建模核实）、`_gen/_LitemallGoods.java`（PROP_NAME_isRecommend=22 已生成核实）、`_gen/_LitemallGoods.view.xml`（:73 isRecommend col 基线存在）。
- [x] **Decision: 三标记位落地方式（命名漂移裁决）。** roadmap/design doc 称「newFlag/hotFlag/recommendFlag」，但模型已有 litemall 内置 `isNew`(propId=13)/`isHot`(propId=14)/`isRecommend`(propId=22) 三标记位（均已 codegen 生成）。**抉择：复用既有三字段（不重命名/不复制），isRecommend 为唯一 dormant 待接线字段。** 备选「按 roadmap 重命名为 newFlag/hotFlag/recommendFlag」被否（三字段均已建模且 isNew/isHot 已被 frontListByFlags + 首页楼层使用，重命名引入无谓回归）。残留风险：design doc 命名与模型不一致——由本阶段订正 design doc 对齐模型实际命名（isNew/isHot/isRecommend）。已写入 owner doc（`product-catalog.md:207-215` 字段映射表）。
- [x] **Decision: isRecommend 展示语义与接线范围。** 抉择：isRecommend 默认 false（存量商品非推荐）；推荐楼层仅展示 isRecommend=true 且 isOnSale=true 的商品（与新品/人气楼层 isOnSale 约束一致）；frontListByFlags 新增 isRecommend 过滤（向后兼容 @Optional）；后台 delta 视图 grid/edit/query 全程暴露 isRecommend（与 isNew/isHot 对称）。理由：与既有标记位语义对称，唤醒 dormant 字段。已写入 owner doc（`product-catalog.md:217-224` 业务规则）。
- [x] **Add:** 订正 `product-catalog.md:201` 首页运营打标章节——新增「字段映射」表（设计语义→模型字段 isNew/isHot/isRecommend + propId），三标记位命名对齐模型实际，补充推荐位业务规则（默认 false / isOnSale 约束 / 为你推荐第三楼层 / frontListByFlags 三标记过滤）与首页三楼层说明。

Exit Criteria:

- [x] `product-catalog.md` 首页运营打标章节命名对齐模型（isNew/isHot/isRecommend）；推荐位业务规则补全
- [x] 两个 Decision 抉择/备选/理由/残留风险已记录（三字段复用裁决 + isRecommend 接线范围）
- [x] 确认本计划无模型改动（Phase 2 仅 Java 方法扩展 + AMIS delta + docs）

### Phase 2 — 后端 + 前端：扩展 query + 推荐楼层 + 后台暴露 isRecommend（Add-heavy）

Status: completed
Targets: `app-mall-dao/.../ILitemallGoodsBiz.java`、`app-mall-service/.../LitemallGoodsBizModel.java`、`app-mall-web/.../home.page.yaml`、`app-mall-web/.../pages/LitemallGoods/LitemallGoods.view.xml`
Required Skill: `nop-backend-dev`、`nop-testing`（扩展 `@BizQuery`，规则 #15）、`nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 1（命名裁决已决）

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing` + `nop-frontend-dev`，读完各自 routing table 必读文档（列出路径）。后端每方法 selfcheck（IBiz 先声明 / @Optional @Name 向后兼容 / ErrorCode+NopException / findList 三参数）；前端文件完成后 selfcheck（未改 `_gen`、bounded-merge 用于 cols、edit layout 含字段、query form 加过滤）。
  - Docs read: `.opencode/skills/nop-backend-dev/SKILL.md`（IBiz 先声明、CrudBizModel safe API、反模式表）、`.opencode/skills/nop-testing/SKILL.md`（JunitBaseTestCase + IGraphQLEngine 模式、@NopTestConfig）、`.opencode/skills/nop-frontend-dev/SKILL.md`（bounded-merge 白名单、x:prototype、保留层 vs `_gen`）。
- [x] **Add:** 先在 IBiz 接口 `ILitemallGoodsBiz.frontListByFlags`（`app-mall-dao/.../ILitemallGoodsBiz.java:30-36`）声明新增 `@Optional @Name("isRecommend") Boolean isRecommend` 入参（**接口先声明，impl 后 @Override 跟进**——否则 7 参接口方法未实现导致编译失败；遵循项目「IBiz 先声明」约定），再在 `LitemallGoodsBizModel.frontListByFlags`（`LitemallGoodsBizModel.java:143`）impl 同步该入参，查询 isRecommend=true 且 isOnSale=true 商品。参照既有 isNew/isHot 过滤实现；**API/GraphQL 向后兼容**（`@Optional`→省略即 null 不过滤，既有调用方无行为变化），`LitemallGoodsBizModel.java:137` 的 `frontList` 包装器内部 positional Java 调用已同步新签名（`frontListByFlags(null,null,null,...)`，机械改动，无行为变化）。
- [x] **Add:** 首页 `home.page.yaml` 新增「为你推荐」第三楼层区块，调 `@query:LitemallGoods__frontListByFlags` 传 isRecommend=true（参照既有新品/人气楼层结构，pageSize=8）。
- [x] **Add:** 后台 `LitemallGoods.view.xml`（保留层）全程暴露 isRecommend：grid `bounded-merge` cols 增 `<col id="isRecommend"/>`（与 isNew/isHot 并列）；edit form layout 增 `isRecommend[是否推荐]`（与 isNew/isHot 并列）；query form layout 增三标记位过滤（`id goodsSn keywords isNew isHot isRecommend`）。
- [x] **Proof:** 扩展后的 `frontListByFlags`（isRecommend 过滤）通过 `IGraphQLEngine` 测试：`TestLitemallGoodsExtendedBizModel.testFrontListByFlagsRecommend` 新增用例（isRecommend=true 命中 / 省略 isRecommend 向后兼容 / isRecommend+isHot 组合过滤排除仅 isRecommend 项）全绿。

Exit Criteria:

- [x] frontListByFlags 支持 isRecommend 过滤（向后兼容既有调用）
- [x] 首页「为你推荐」楼层展示 isRecommend 商品
- [x] 后台商品页全程暴露三标记位（grid 列 + edit 开关 + query 过滤，含 isRecommend）
- [x] **API 测试：** 扩展 `@BizQuery` 通过 `IGraphQLEngine`
- [x] 复用既有 AMIS 三层定制模式，无新前端依赖

### Phase 3 — 验证、文档同步、日志（Proof）

Status: completed
Targets: 全模块
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: Phase 1-2

- [x] **Skill loading gate:** 加载 `nop-testing`，读 routing 必读文档（列出已读路径）。
  - Docs read: `.opencode/skills/nop-testing/SKILL.md`（JunitBaseTestCase + IGraphQLEngine 模式、@NopTestConfig、反模式表）。Skill 已于 Phase 2 加载，本阶段复用。
- [x] **Proof:** 跑 `docs/context/project-context.md` 真实验证命令（`./mvnw test -pl app-mall-service -am`：**176 测试全绿**；`./mvnw -pl app-mall-web -DskipTests compile`：BUILD SUCCESS）；全量 `./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar -T 1C` BUILD SUCCESS；更新 `docs/testing/known-good-baselines.md`（新增 Phase 34 baseline row）。
- [x] **Proof:** 订正 roadmap stale（两处命名漂移实例）——`enhanced-features-roadmap.md:584` 扩展字段表「LitemallGoods | 34 | newFlag, hotFlag, recommendFlag」+ `:435` Phase 34 交付范围「商品三标记字段（newFlag/hotFlag/recommendFlag）」均订正为「三字段均已存在（isNew/isHot/isRecommend），P34 仅接线启用推荐位」（grep 确认此为 roadmap 仅有的两处实例；闭合步骤，无模型新增）。
- [x] 更新 `docs/logs/2026/06-27.md`（逆向时间序，置顶 Phase 34 条目）。

Exit Criteria:

- [x] 全量验证通过（含本计划新增 IGraphQLEngine 测试）
- [x] `known-good-baselines.md` 更新本计划 baseline row
- [x] roadmap 扩展字段表 stale 订正；`docs/logs/` 更新

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent 对抗审计（fresh session），五轮达成共识。
  - Round 1（`ses_0f6e89775ffeJgRaxFQR6XN5oL`）：REVISE — 1 BLOCKER（**isRecommend(propId=22) 已存在**！baseline 误判第三标记缺失——草稿只 grep recommendFlag 漏了 isRecommend）+ 3 MAJOR（admin view query/edit 误标、lazy「需确认」措辞、delta bounded-merge 隐藏 isRecommend）。**整体重写**：三标记位均已建模 → 删除模型准备 phase（无 ask-first/codegen/DDL）、collapse 至 3 phase、recommendFlag→isRecommend 全局订正、命名 Decision 自洽。
  - Round 2（`ses_0f6de6f67ffebgHiY1tDcZaDOJ`）：REVISE — 1 MAJOR（frontListByFlags 在 ILitemallGoodsBiz 接口声明，加 isRecommend 须先改接口否则编译失败；Phase 2 Targets 漏接口）。修订：Targets 加 ILitemallGoodsBiz.java + 接口先声明 item。
  - Round 3（`ses_0f6d5ed1bffemOw1qQOI6dZCRi`）：REVISE — 1 MAJOR（roadmap stale 命名第二实例 @ :435 漏订正，Goal 已承诺订正 roadmap）。修订：Phase 3 + Closure Gate 补 :584 + :435 两处。
  - Round 4（`ses_0f6cf7233ffe2emCZ2B4uB7Mqc`）：PASS — M1(:435) RESOLVED，无新增（全部 baseline live；接口先声明编译正确；无模型改动准确；隐藏调用方扫描仅 :137 positional 需同步）。第 1 个 clean round。
  - Round 5（`ses_0f6cbaa3cffeMxD14lq35f1y3M`）：PASS — 第 2 个连续 clean round，共识达成。
- Evidence: 实读 live repo 核验 baseline（isNew@orm.xml:674/isHot@:676/isRecommend@:693 三者均已建模、isRecommend 全 codegen 生成但 delta dormant；frontListByFlags 接口@ILitemallGoodsBiz:30-36+impl@:143；home.page.yaml 新品/人气楼层；admin delta grid bounded-merge 删 isRecommend/edit:92 缺/query:81-85 无标记过滤；roadmap :435+:584 两处 stale）。无模型改动（仅 Java 接口+impl + AMIS delta + docs）经核验准确。

## Closure Gates

- [x] in-scope behavior is complete（isRecommend 接线启用 + query 扩展 + 推荐楼层 + 后台全程暴露 + 命名订正）
- [x] relevant docs are aligned（`product-catalog.md` 命名对齐 + roadmap 两处 stale 订正：`:584` 扩展字段表 + `:435` 交付范围）
- [x] verification has run（`./mvnw test -pl app-mall-service -am` 全绿 + app-mall-web 编译）
- [x] all new/changed `@BizQuery` methods tested via `IGraphQLEngine`（frontListByFlags isRecommend 扩展）
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed（Phase 1 `none` 含 justify）
- [x] skill loading verification: 各 phase 已扫描/加载/读必读文档/selfcheck
- [x] text consistency verified: status / phases / gates / log 一致
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

> 起草时无可延期项。批量打标/取消（属 P36 商品运营增强）、首页楼层 DIY 运营化排序（DIY 装修，roadmap 明确单独规划）在 Non-Goals 显式移出。

## Closure

<!-- Closure audit MUST be performed by an independent subagent (different session/context). 留给闭合审计代理填写。 -->

Status Note: 完成。三 Phase 全 completed；独立闭合审计 APPROVED（11/11 gate 通过）。

Closure Audit Evidence:

- Reviewer / Agent: 独立闭合审计 subagent（fresh session `ses_0f65d076bffe05szw3EA1IYZKu`，general agent，未参与实现）
- Evidence: 11/11 gate PASS against live repo——
  - GATE1 PASS：`ILitemallGoodsBiz.java:32` 接口声明 `@Optional @Name("isRecommend")`；`LitemallGoodsBizModel.java:145-162` impl @Override + 过滤；`home.page.yaml:208-247` 为你推荐楼层；`LitemallGoods.view.xml` grid `:55`/edit `:95`/query `:86`；`product-catalog.md:207-215` 字段映射。
  - GATE2 PASS：roadmap `:435`/`:584` stale 两处已订正为 isNew/isHot/isRecommend；grep 无 newFlag/hotFlag 作活标识符。
  - GATE3 PASS：plan/log/baseline 一致记录 176 全绿 + web 编译 + uber-jar。
  - GATE4 PASS：`TestLitemallGoodsExtendedBizModel.testFrontListByFlagsRecommend`（:314-397）经 IGraphQLEngine 覆盖 isRecommend 命中/向后兼容/×isHot 组合三路径。
  - GATE10 PASS：`git diff` 无 `model/app-mall.orm.xml` 改动、无 `_gen` 手改（hard constraint 守住）。
  - GATE11 PASS：接口先声明（:30-37）先于 impl @Override（:143-150）；`frontList:137` positional 3 null 同步新 8 参签名；@Optional 向后兼容。
  - GATE5-9 PASS：无 in-scope 降级；plan audit passed（5 轮）；各 phase Required Skill 齐；skill 加载/selfcheck 记录齐；text 一致。
- VERDICT: APPROVED

Follow-up:

- 批量打标运营（触发条件：P36 商品运营增强启动）。
