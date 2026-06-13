# 2026-06-13 下一阶段开发计划（Phase 12 通知系统 + Phase 13 报表与统计 + Phase 14 微信支付集成）

> Plan Status: planned
> Last Reviewed: 2026-06-13
> Source: `docs/backlog/implementation-roadmap.md` Phase 12, Phase 13, Phase 14
> Related: `docs/plans/2026-06-13-next-phase-plan.md` (completed, Phase 9/11), `docs/plans/2026-06-12-phase-7-10-interactive-coupon-content-plan.md` (completed, Phase 7/8/10)
> Audit: required

## Why One Plan

Phase 12（通知系统）、Phase 13（报表与统计）、Phase 14（微信支付集成）合并为一个执行计划，理由如下：

1. **Phase 12 和 Phase 14 存在技术耦合：** Phase 12 的通知能力会在支付确认、发货、退款等事件中触发。Phase 14 微信支付实现后，支付回调通知和退款通知需要通知系统支持。团购失败退款（从 Phase 9 deferred）也依赖 Phase 12 + Phase 14 联调
2. **三个 Phase 均需要引入新的平台模块依赖：** Phase 12 需要 `nop-integration`，Phase 13 需要 `nop-report`，Phase 14 需要微信支付 SDK。将平台依赖引入集中在一个计划中管理，避免多次独立评估
3. **共享累积 Deferred 项的归宿：** 忘记密码/密码重置（从 Phase 1 deferred）、注册赠券自动发放（从 Phase 8 deferred）、团购失败退款（从 Phase 9 deferred）均在三个 Phase 全部完成后才能完整解决
4. **共享单一 Closure Gate：** 所有 Phase 完成后一起做 closure audit
5. **Phase 14 为 Protected Area：** 微信支付集成需要 `ask-first`，如果人工暂不批准 Phase 14，Phase 12 和 Phase 13 可先行完成，Phase 14 单独延后

## Current Baseline

### 已完成的 Phase

- Phase 1 用户注册登录: done
- Phase 2 商品目录管理: done
- Phase 3 地址管理: done
- Phase 4 购物车: done
- Phase 5 订单核心流程: done
- Phase 5b 支付集成: done
- Phase 5c 退款与售后: done
- Phase 6 搜索与发现: done
- Phase 7 互动（收藏/足迹/评论）: done
- Phase 8 优惠券体系: done
- Phase 9 团购: done
- Phase 10 内容营销与反馈: done
- Phase 11 系统运营与定时任务: done

### Phase 12/13/14 现状

- **通知系统（Phase 12）：**
  - `nop-integration` 依赖**未引入**。`app-mall-service/pom.xml` 当前无 `nop-integration` 依赖
  - 已有 SMS 使用证据：`LitemallAftersaleBizModel.java` 通过 `io.nop.integration.api.sms.ISmsSender` 接口发送短信通知（售后退款成功通知）。说明 `ISmsSender` 接口已在代码中使用但平台实现尚未引入
  - 平台 `nop-sys` 的 `NopSysNoticeTemplate` 已有依赖，可用于通知模板管理
  - ORM 实体中无独立通知记录实体——通知记录的持久化需要评估是否使用平台 `NopSysNotice` 或新建实体
- **报表与统计（Phase 13）：**
  - `nop-report` 依赖**未引入**
  - 报表所需的底层数据（订单、商品、用户）均已存在
  - 后台统计看板页面需要在 AMIS view.xml 中实现
- **微信支付集成（Phase 14）：**
  - `app-mall-wx` 模块已存在，包含 `WxPayServiceImpl` 空壳 stub
  - `MockPayServiceImpl` 为当前唯一支付实现
  - `IPayService` 接口（`app-mall-api`）定义了 `PayRequestBean`/`PayRefundResponseBean` 等契约
  - **Protected Area:** `ask-first`（`ai-autonomy-policy.md` 定义）

### 累积 Deferred 项（来源：所有已完成计划）

| 来源 | 内容 | Successor |
|------|------|-----------|
| Phase 1 plan | 忘记密码/密码重置 | Phase 12（本计划 Phase 4B） |
| Phase 7/8/10 plan | 注册赠券自动发放 | 本计划 Phase 4A |
| Phase 9/11 plan | 团购失败退款 | Phase 12/14（本计划 Phase 3B） |
| Phase 9/11 plan | 专题上下架状态控制 | model-gap，下次修改模型时补充 |
| Phase 9/11 plan | 优惠券总数并发保护 | watch-only，多实例部署时处理 |
| 多个计划 | 前台 AMIS 页面定制 | 前端集中开发阶段（本计划不覆盖） |

### 已知遗留问题

- **Pre-existing test failures:** 源于 `NOP_FILE_RECORD` 表缺失等环境问题，非业务逻辑错误
- **AMIS 前台页面:** 全部延后至前端集中开发阶段
- **ISmsSender 接口已使用但实现未引入：** `LitemallAftersaleBizModel` 中通过 `@Inject ISmsSender smsSender` 注入并发送短信，但 `nop-integration` 依赖未引入。需要在 Phase 12 中评估当前编译是否通过（可能通过 `nop-file-service` 传递依赖已解决）

## Goals

1. **Phase 12 通知系统：** 引入 `nop-integration` 依赖，实现业务事件到通知消息的转换，支持支付确认通知、发货通知、后台订单提醒，集成 SMS 通道，后台通知记录
2. **Phase 13 报表与统计：** 引入 `nop-report` 依赖，定义数据集（SQL），创建报表模板，后台统计看板
3. **Phase 14 微信支付集成：** 接入微信支付替代模拟支付，实现 PayService 微信支付实现和支付回调，退款接口对接，微信支付配置管理
4. **累积 Deferred 项收尾：** 注册赠券自动发放、忘记密码/密码重置、团购失败退款

## Non-Goals

- 前台 AMIS 页面定制（延后至前端集中开发阶段）
- 专题上下架状态控制（model-gap，下次修改模型时补充）
- 优惠券总数并发保护（watch-only，多实例部署时处理）
- 微信登录/外部登录渠道
- Email 通知通道（Phase 12 仅实现 SMS，Email 延后）
- 复杂报表导出（仅实现后台看板统计）

## Task Route

- Type: `implementation-only change`（Phase 12/13）+ `architecture change`（Phase 14 支付渠道替换）
- Owner Docs: `docs/design/system-configuration.md`（Phase 12 通知、Phase 13 报表），`docs/architecture/system-baseline.md`（Phase 14 支付集成）
- Skill Selection Basis: `nop-backend-dev` (BizModel 方法)、`nop-frontend-dev` (AMIS 页面)、`nop-testing` (IGraphQLEngine 测试)

## Infrastructure And Config Prereqs

- Phase 12: 需引入 `nop-integration` Maven 依赖；SMS 供应商配置（开发环境可用 mock sender）
- Phase 13: 需引入 `nop-report` Maven 依赖
- Phase 14: 需微信支付商户号、AppID、密钥等配置；需 `ask-first` 人工确认后才能执行
- 所有 Phase: H2 数据库已配置，`nop-entropy` parent POM 可用

## Execution Plan

### Phase 1A — 平台依赖引入与验证

Status: planned
Targets: `app-mall-app/pom.xml`, `app-mall-service/pom.xml`
Required Skill: `nop-backend-dev`

- Item Types: `Add`
- Prereqs: 无

- [ ] **Skill loading gate:** Load `nop-backend-dev`. Read all mandatory docs listed in its routing table. List the docs read below.
  - Docs read: <to be filled during execution>
- [ ] **Decision — nop-integration 依赖范围：** 确定引入 `nop-integration` 的具体模块和版本
  - 需要评估：`nop-integration-core` + `nop-integration-sms`（或 `nop-integration-sms-aliyun`/`nop-integration-sms-tencent`）是否需要单独引入
  - 当前代码已使用 `io.nop.integration.api.sms.ISmsSender` 接口，说明 API 依赖已通过传递引入，但实现类可能缺失
  - **推荐：** 引入 `nop-integration-core` 到 `app-mall-service`（提供 ISmsSender 实现），开发环境使用 mock sender
  - Alternatives: 引入特定 SMS 供应商实现（aliyun/tencent）→ 开发阶段不需要真实 SMS，mock 即可
  - 残留风险：生产环境需要配置真实 SMS 供应商
- [ ] **Decision — nop-report 依赖范围：** 确定引入 `nop-report` 的具体模块
  - `nop-report-core` 提供报表引擎核心功能
  - 需要评估是否需要 `nop-report-web`（如果报表需要在前端渲染）
  - **推荐：** 引入 `nop-report-core` 到 `app-mall-service`
  - Alternatives: 使用 AMIS 原生图表组件 + SQL 数据集 → 不需要 `nop-report`，但需要更多前端定制
- [ ] **Add: 引入 nop-integration 依赖。** 在 `app-mall-service/pom.xml` 中添加 `nop-integration-core` 依赖
- [ ] **Add: 引入 nop-report 依赖。** 在 `app-mall-service/pom.xml` 或 `app-mall-app/pom.xml` 中添加 `nop-report-core` 依赖
- [ ] **Proof: 编译验证。** `./mvnw.cmd compile -DskipTests` 确认依赖引入后编译通过
- [ ] **Proof: ISmsSender 可用性验证。** 确认 `LitemallAftersaleBizModel` 中已有的 `ISmsSender` 注入在引入 `nop-integration-core` 后可正常工作

Exit Criteria:

- [ ] `nop-integration-core` 依赖引入成功
- [ ] `nop-report-core` 依赖引入成功
- [ ] 编译通过
- [ ] ISmsSender 注入可用
- [ ] `docs/logs/` updated

### Phase 1B — 通知基础设施搭建

Status: planned
Targets: `app-mall-service/`
Required Skill: `nop-backend-dev`

- Item Types: `Add-heavy`
- Prereqs: Phase 1A

- [ ] **Skill loading gate:** Load `nop-backend-dev`. Read all mandatory docs. Selfcheck after each method.
  - Docs read: <to be filled during execution>
- [ ] **Decision — 通知记录持久化方式：** 确定如何存储已发送的通知记录
  - 选项 A（使用平台 NopSysNotice）：使用平台自带的通知记录实体，无需新建实体
  - 选项 B（新建 LitemallNotification 实体）：在 ORM 模型中新建通知记录实体，包含商城特有字段（如关联订单 ID、通知类型等）
  - 选项 C（不持久化通知记录）：仅发送通知，不记录发送历史
  - **推荐选项 A：** 使用平台 `NopSysNotice` 记录通知，避免新建实体。商城特有的通知类型和关联信息可通过 `NopSysNotice` 的扩展字段（如 `params` JSON）存储
  - Alternatives: 选项 B 提供更灵活的查询和统计，但需要 ORM 模型变更（protected area）
  - 残留风险：如果 `NopSysNotice` 的字段不足以满足商城需求，可能需要后续迁移
- [ ] **Decision — SMS 通道实现：** 确定开发/测试环境的 SMS 实现
  - 选项 A（日志 Mock Sender）：实现一个仅打印日志的 `ISmsSender`，不发送真实 SMS
  - 选项 B（平台 Mock）：如果 `nop-integration-core` 自带 mock 实现，直接使用
  - **推荐选项 A：** 实现日志 mock sender，确保开发环境不依赖外部 SMS 服务
  - Alternatives: 引入 aliyun/tencent SMS SDK → 开发阶段不需要
- [ ] **Add: 创建 MallNotificationService。** 通知服务封装：
  - 注入 `ISmsSender`
  - `sendOrderPaymentNotification(orderId, userId)` — 支付确认通知
  - `sendOrderShipNotification(orderId, userId)` — 发货通知
  - `sendAdminOrderNotification(orderId)` — 后台订单提醒
  - 内部：组装通知内容、调用 SMS sender、记录通知日志
- [ ] **Add: 错误码。** 在 `AppMallErrors` 中添加：
  - `ERR_NOTIFICATION_SEND_FAILED` — 通知发送失败
- [ ] **Proof: 编译通过。** `./mvnw.cmd compile -DskipTests`

Exit Criteria:

- [ ] 通知记录持久化方式确定
- [ ] SMS 通道实现确定（开发环境 mock）
- [ ] MallNotificationService 创建，包含 3 个通知方法
- [ ] 编译通过
- [ ] `docs/logs/` updated

### Phase 2A — 业务事件通知集成

Status: planned
Targets: `app-mall-service/`
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Add`
- Prereqs: Phase 1B

- [ ] **Skill loading gate:** Load `nop-backend-dev`, `nop-testing`. Read all mandatory docs. Selfcheck after each method.
  - Docs read: <to be filled during execution>
- [ ] **Modify: 集成支付确认通知。** 在 `LitemallOrderBizModel` 的支付确认流程中，支付成功后调用 `MallNotificationService.sendOrderPaymentNotification()`
- [ ] **Modify: 集成发货通知。** 在 `LitemallOrderBizModel.ship()` 中，发货成功后调用 `MallNotificationService.sendOrderShipNotification()`
- [ ] **Modify: 集成后台订单提醒。** 在 `LitemallOrderBizModel.submit()` 中，新订单创建后调用 `MallNotificationService.sendAdminOrderNotification()`
- [ ] **Modify: 集成售后退款通知（已有 SMS）。** 确认 `LitemallAftersaleBizModel` 中已有的 SMS 通知调用与新的 `MallNotificationService` 一致，或将已有 SMS 调用迁移到 `MallNotificationService`
- [ ] **Add: 通知后台页面。** 如果使用 `NopSysNotice`，定制通知记录查看页面（或在现有后台管理中添加通知记录菜单项）
- [ ] **Proof: 测试。** 通过 IGraphQLEngine 测试验证：
  - 支付确认后触发通知
  - 发货后触发通知
  - 新订单创建后触发通知

Exit Criteria:

- [ ] 支付确认通知集成完成
- [ ] 发货通知集成完成
- [ ] 后台订单提醒集成完成
- [ ] 售后退款通知与 MallNotificationService 对齐
- [ ] 编译通过
- [ ] API 测试通过 IGraphQLEngine
- [ ] `docs/logs/` updated

### Phase 2B — 报表数据集与统计看板

Status: planned
Targets: `app-mall-service/`, `app-mall-web/`
Required Skill: `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`

- Item Types: `Add-heavy`
- Prereqs: Phase 1A（nop-report 依赖引入）

- [ ] **Skill loading gate:** Load `nop-backend-dev`, `nop-frontend-dev`, `nop-testing`. Read all mandatory docs. Selfcheck after each method.
  - Docs read: <to be filled during execution>
- [ ] **Decision — 报表实现方式：** 确定报表的技术实现路径
  - 选项 A（nop-report 报表引擎）：使用平台 `nop-report` 定义数据集和报表模板，生成报表输出
  - 选项 B（BizModel 统计方法 + AMIS 图表）：在 BizModel 中编写统计查询方法（@BizQuery），返回聚合数据，前端用 AMIS 图表组件展示
  - **推荐选项 B：** 对于后台统计看板场景，BizModel 统计方法 + AMIS 图表更轻量，不需要引入 `nop-report` 的报表模板和数据集定义复杂度。统计看板本质是聚合查询 + 图表展示，不需要报表引擎的导出和模板能力
  - Alternatives: 选项 A 更适合复杂报表（多级分组、交叉表、导出），但当前需求是简单统计看板
  - 残留风险：如果未来需要复杂报表导出，需要补充 `nop-report` 集成
  - **影响：** 如果选择选项 B，Phase 1A 中引入的 `nop-report` 依赖可以延后或移除
- [ ] **Add: 创建统计 BizModel。** 新建 `LitemallStatBizModel`（或扩展现有 BizModel），添加统计查询方法：
  - `getOrderStatistics(@Name("startDate") String startDate, @Name("endDate") String endDate)` — 订单统计（数量、金额、状态分布）
  - `getGoodsStatistics(@Name("startDate") String startDate, @Name("endDate") String endDate)` — 商品统计（销量 Top N、库存预警）
  - `getUserStatistics(@Name("startDate") String startDate, @Name("endDate") String endDate)` — 用户统计（新增用户、活跃用户）
  - 所有方法为 `@BizQuery`，需要管理员角色
- [ ] **Add: SQL 查询。** 在 `*.sql-lib.xml` 中定义统计 SQL：
  - 订单统计：按状态分组统计数量和金额，支持时间区间过滤
  - 商品统计：按销量排序 Top N，库存低于阈值的商品
  - 用户统计：按注册时间统计新增用户，按登录时间统计活跃用户
- [ ] **Add: 后台统计看板页面。** 修改或新建 AMIS view.xml 页面：
  - 订单统计区域：数量/金额/状态分布图表
  - 商品统计区域：销量排行/库存预警
  - 用户统计区域：增长曲线/活跃度
  - 时间区间选择器
- [ ] **Proof: 测试。** 通过 IGraphQLEngine 测试验证统计查询方法

Exit Criteria:

- [ ] 报表实现方式确定
- [ ] 3 个统计查询方法通过 IGraphQLEngine 测试
- [ ] 后台统计看板页面编译通过
- [ ] `docs/logs/` updated

### Phase 3A — 微信支付集成（Protected Area: ask-first）

Status: planned
Targets: `app-mall-wx/`, `app-mall-service/`
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Add-heavy`
- Prereqs: Phase 1A + 人工确认（ask-first）

- [ ] **Ask-first gate — 微信支付集成确认：** Phase 14 为 Protected Area（`ai-autonomy-policy.md` 定义 "WeChat Pay (app-mall-wx)" 为 ask-first）。**需要人工确认以下内容后才能执行：**
  1. 微信支付商户号、AppID、API 密钥等配置信息
  2. 是否已有微信支付 SDK 依赖（`wechatpay-java` 或 `wxjava-pay`）
  3. 支付回调域名和 HTTPS 配置
  4. 是否需要同时支持微信小程序支付、公众号支付、H5 支付
  - **在人工确认前，本 Phase 不可进入实现阶段。Phase 12/13 可先行完成。**
- [ ] **Skill loading gate:** Load `nop-backend-dev`, `nop-testing`. Read all mandatory docs. Selfcheck after each method.
  - Docs read: <to be filled during execution>
- [ ] **Decision — 微信支付 SDK 选型：** 确定 Java 微信支付 SDK
  - 选项 A（`wechatpay-java`）：微信支付官方 Java SDK
  - 选项 B（`wxjava`）：WxJava 社区 SDK
  - 选项 C（直接 HTTP 调用）：无 SDK，直接调用微信支付 REST API
  - **推荐选项：** 待人工确认后根据项目已有依赖和团队偏好决定
- [ ] **Add: WxPayServiceImpl 实现。** 替换 `app-mall-wx` 中的空壳 stub：
  - 实现 `IPayService` 接口的 `createPayment()`、`queryPayment()`、`refund()` 方法
  - 统一下单 → 返回支付参数
  - 支付回调处理 → 验证签名 → 更新支付状态
  - 退款接口 → 调用微信退款 API → 返回退款结果
- [ ] **Add: 微信支付配置管理。** 在 `LitemallSystem` 或 `NopSysVariable` 中管理：
  - 商户号（mchId）
  - AppID
  - API 密钥
  - 证书路径（退款需要证书）
  - 支付回调 URL
- [ ] **Add: 支付回调端点。** 在 `app-mall-wx` 中添加 REST 端点接收微信支付回调
- [ ] **Proof: 测试。** 微信支付测试需要沙箱环境或 mock，通过 `IGraphQLEngine` 测试 `WxPayServiceImpl` 的核心逻辑（签名生成、参数组装等）

Exit Criteria:

- [ ] ask-first gate 通过
- [ ] 微信支付 SDK 选型确定
- [ ] `WxPayServiceImpl` 实现完成（createPayment/queryPayment/refund）
- [ ] 支付回调端点实现
- [ ] 微信支付配置管理实现
- [ ] 编译通过
- [ ] API 测试通过 IGraphQLEngine（核心逻辑）
- [ ] `docs/logs/` updated

### Phase 3B — 团购失败退款集成

Status: planned
Targets: `app-mall-service/`
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Add`
- Prereqs: Phase 3A（微信支付集成完成）+ Phase 1B（通知系统完成）

- [ ] **Skill loading gate:** Load `nop-backend-dev`, `nop-testing`. Read all mandatory docs.
  - Docs read: <to be filled during execution>
- [ ] **Modify: 团购过期定时任务补充退款逻辑。** 在 Phase 11 已实现的 `expireGroupons()` 定时任务中，当团购过期标记为失败（status=2）后，触发参团订单的自动退款流程
  - 查询参与过期团购的所有订单（status=1 团购中 → status=2 团购失败）
  - 对每个订单调用退款逻辑（通过 `IPayService.refund()` 或复用 `LitemallAftersaleBizModel` 的退款方法）
  - 发送团购失败退款通知（通过 `MallNotificationService`）
- [ ] **Proof: 测试。** 验证团购过期后自动退款和通知逻辑

Exit Criteria:

- [ ] 团购过期后自动退款逻辑完整
- [ ] 退款通知发送
- [ ] 编译通过
- [ ] `docs/logs/` updated

### Phase 4A — 注册赠券自动发放

Status: planned
Targets: `app-mall-service/`
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Add`
- Prereqs: Phase 1A（nop-integration 可选）+ Phase 8 已完成

- [ ] **Skill loading gate:** Load `nop-backend-dev`, `nop-testing`. Read all mandatory docs. Selfcheck after each method.
  - Docs read: <to be filled during execution>
- [ ] **Modify: 注册流程 hook 优惠券自动发放。** 在 `LoginApiExBizModel.signUp()` 或 `NopAuthUserExBizModel` 的注册扩展点中：
  - 注册成功后，查询所有 type=1（注册赠券）且 status=0（可用）的优惠券规则
  - 对每个符合条件的优惠券规则，调用 `ILitemallCouponUserBiz.claimCoupon()` 为新用户自动发放
  - 发放失败不阻塞注册流程（记录日志，后续人工补偿）
- [ ] **Proof: 测试。** 通过 IGraphQLEngine 测试：
  - 注册新用户后自动获得注册赠券
  - 无注册赠券规则时注册不受影响
  - 发放失败不阻塞注册

Exit Criteria:

- [ ] 注册成功后自动发放 type=1 优惠券
- [ ] 发放失败不阻塞注册
- [ ] API 测试通过 IGraphQLEngine
- [ ] `docs/logs/` updated

### Phase 4B — 忘记密码/密码重置

Status: planned
Targets: `app-mall-service/`
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Add`
- Prereqs: Phase 1B（通知系统完成，SMS 可用）

- [ ] **Skill loading gate:** Load `nop-backend-dev`, `nop-testing`. Read all mandatory docs. Selfcheck after each method.
  - Docs read: <to be filled during execution>
- [ ] **Decision — 密码重置实现方式：** 确定密码重置的验证通道
  - 选项 A（SMS 验证码）：通过手机号发送验证码，验证后重置密码
  - 选项 B（邮件重置链接）：通过邮箱发送重置链接
  - **推荐选项 A：** 商城用户通常绑定手机号，SMS 验证码更直接
  - Alternatives: 邮件重置链接 → 需要邮件通道支持，开发阶段不如 SMS 方便
  - 残留风险：SMS 通道费用
- [ ] **Add: 扩展登录 API。** 添加密码重置相关方法：
  - `sendResetCode(@Name("mobile") String mobile)` — 发送重置验证码（通过 `ISmsSender`）
  - `resetPassword(@Name("mobile") String mobile, @Name("code") String code, @Name("newPassword") String newPassword)` — 验证码校验后重置密码
  - 两个方法均为公开访问（`@Auth(publicAccess = true)`）
- [ ] **Add: 验证码存储。** 验证码临时存储方案：
  - 使用 `NopSysVariable` 或内存缓存（开发阶段）存储验证码和过期时间
  - 验证码有效期 5 分钟
  - 同一手机号 1 分钟内不可重复发送
- [ ] **Add: 错误码。** 在 `AppMallErrors` 中添加：
  - `ERR_RESET_CODE_INVALID` — 验证码无效或已过期
  - `ERR_RESET_CODE_SEND_TOO_FREQUENT` — 发送太频繁
  - `ERR_USER_MOBILE_NOT_FOUND` — 手机号未注册
- [ ] **Proof: 测试。** 通过 IGraphQLEngine 测试验证发送验证码和重置密码流程

Exit Criteria:

- [ ] 发送重置验证码通过 SMS 发送
- [ ] 验证码校验后密码重置成功
- [ ] 验证码过期/无效被拒
- [ ] 发送频率限制生效
- [ ] API 测试通过 IGraphQLEngine
- [ ] `docs/logs/` updated

### Phase Final — 收尾与文档更新

Status: planned
Targets: 全局
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: 所有 Phase 完成

- [ ] **Proof: 全量编译和测试。** `./mvnw.cmd compile -DskipTests`
- [ ] **Add: 更新 owner docs。**
  - 确认 `docs/design/system-configuration.md` 与 Phase 12/13 实现一致
  - 确认 `docs/architecture/system-baseline.md` 中支付集成描述与 Phase 14 实现一致
- [ ] **Add: 更新 roadmap。** `docs/backlog/implementation-roadmap.md`：
  - Phase 12: `todo` → `done`
  - Phase 13: `todo` → `done`
  - Phase 14: `todo` → `done`（如果 ask-first 通过并完成）或保持 `todo`（如果人工暂不批准）
- [ ] **Add: 更新 dev log。** 在 `docs/logs/2026/06-{day}.md` 中记录

Exit Criteria:

- [ ] `./mvnw.cmd compile -DskipTests` 通过
- [ ] owner docs 与实现一致
- [ ] roadmap 状态更新
- [ ] `docs/logs/` updated

## Plan Audit

- Status: pending
- Reviewer / Agent: <independent subagent>
- Evidence: <to be filled>

## Closure Gates

- [ ] Phase 12 通知系统完成并通过测试
- [ ] Phase 13 报表与统计完成并通过测试
- [ ] Phase 14 微信支付集成完成并通过测试（或人工确认延后）
- [ ] 注册赠券自动发放完成
- [ ] 忘记密码/密码重置完成
- [ ] 团购失败退款集成完成（依赖 Phase 14）
- [ ] 所有新增 @BizMutation/@BizQuery 方法通过 IGraphQLEngine 测试
- [ ] verification `./mvnw.cmd compile -DskipTests` 通过
- [ ] roadmap 状态更新（Phase 12/13/14）
- [ ] owner docs 与实现对齐
- [ ] plan audit passed before implementation
- [ ] each phase has `Required Skill` listed, and Nop-platform phases do not write `none`
- [ ] skill loading verification completed
- [ ] text consistency verified
- [ ] closure audit was independent
- [ ] closure evidence exists in files
- [ ] no in-scope item downgraded to deferred/follow-up

## Deferred But Adjudicated

### Email 通知通道

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: Phase 12 仅实现 SMS 通知。Email 通道可后续通过 `nop-integration` 的 `IEmailSender` 接口扩展
- Successor Required: `no`（触发条件：业务需要 Email 通知时补充）

### 复杂报表导出

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 当前需求为后台统计看板。复杂报表导出（多级分组、交叉表、Excel 导出）需要 `nop-report` 的报表模板能力
- Successor Required: `no`（触发条件：运营需要可导出报表时补充）

### 前台 AMIS 页面定制

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 前台页面依赖前端框架集成和测试环境完善
- Successor Required: `yes`（前端集中开发阶段）

### 专题上下架状态控制

- Classification: `model-gap`
- Why Not Blocking Closure: LitemallTopic ORM 实体没有 status/enabled 字段。当前所有未删除的专题均视为可见
- Successor Required: `yes`（下次修改此模型时补充 status 字段）
- Model Gap Detail: LitemallTopic 缺少 status 字段。建议添加 `status` (int, dict: mall/topic-status, 0=上架 1=下架)

### 优惠券总数并发保护

- Classification: `watch-only residual`
- Why Not Blocking Closure: 当前为单线程环境，不存在并发问题
- Successor Required: `no`（触发条件：多实例部署或并发领取场景）

## Closure

Status Note: <to be filled after closure>

Closure Audit Evidence:

- Reviewer / Agent: <independent subagent>
- Evidence: <to be filled after closure>

Follow-up:

- 前端集中开发（所有前台页面）
- Email 通知通道（当业务需要时）
- 复杂报表导出（当运营需要时）
- 专题上下架状态控制（model-gap）
