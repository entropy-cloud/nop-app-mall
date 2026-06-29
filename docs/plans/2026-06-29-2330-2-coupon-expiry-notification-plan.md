# Coupon Expiry Pre-Warning Notification

> Plan Status: completed
> Mission: mall
> Work Item: coupon-expiry-notification（关闭 deferred「其他业务事件站内信补全——优惠券即将过期」）
> Last Reviewed: 2026-06-29
> Source: `docs/plans/2026-06-29-1921-3-deferred-notification-triggers-plan.md:133` → `Deferred But Adjudicated → 其他业务事件站内信补全（优惠券即将过期/拼团成功/评价奖励等）`
> Related: `docs/plans/2026-06-29-1921-3-deferred-notification-triggers-plan.md`（业务事件通知触发补全，已 done——积分过期预警 + 自提核销成功通知已闭环，本计划为其 deferred successor 续作优惠券即将过期）
> Audit: required

## Current Baseline

> 实读 live repo 所得，非记忆。

**优惠券过期无前置预警（确认的 gap）：**

- `LitemallCouponUserBizModel.expireCoupons`（`:305`，`@BizMutation`）每日 job 将 `status=0`（未使用）且 `endTime<now` 的用户券标记为 `status=2`（过期）。**过期之后**才变更状态，**过期之前不主动提醒用户**。
- 用户仅在「我的优惠券」页面拉取列表时自行发现即将过期的券，无主动推送。
- 源 deferred（`2026-06-29-1921-3:133`）明确将「优惠券即将过期」列为未接线的事件通知 successor。

**通知基建已就绪（复用对象）：**

- `MallNotificationService.sendUserMessage(userId, msgType, title, content)`（`MallNotificationService.java:125`）——系统上下文写 UserMessage，失败 LOG 且吞（站内信为旁路）。
- `isEventMessageEnabled(eventKey, context)`（`:106`）——事件开关门控（config key=`mall_message_event_enabled_{eventKey}`，默认 enabled）。
- msg-type 字典（`model/app-mall.orm.xml:122`）：ORDER=0 / MARKETING=10 / SYSTEM=20。
- **直接先例：积分过期前置预警**（`LitemallPointsAccountBizModel.sendPointsExpiryReminders`，`:sendPointsExpiryReminders`，`@BizMutation` + 每日 job）——每日扫描近 N 天到期的积分批次 → 按 userId 聚合 → 幂等查 UserMessage 当日同标题 → 推一条聚合 SYSTEM 站内信。本计划为完全同模式移植到优惠券域。

**CouponUser 模型已就绪：**

- `LitemallCouponUser`：`userId`(2)、`couponId`(3)、`status`(4, 0=未使用/1=已使用/2=已过期)、`usedTime`(5)、`startTime`(6)、`endTime`(7)、`orderId`(8)、`addTime`(9)。
- `coupon` to-one 关系（`:584`）→ `LitemallCoupon.name`(propId 2) 可经关系 getter 访问。
- 索引：`idx_couponUser_userId`（`:592`）。

**调度基建已就绪：**

- `MallJobInvoker`（11 job 方法）+ `scheduler.yaml`（10 job 配置；`expirePinTuans` 方法存在但未注册——既有不一致，非本计划范围）模式统一。`sendPointsExpiryReminders`（`:132`，每日 86400000ms）为本计划的直接模板。

**前置条件已满足：** P8（优惠券体系）done、P32（优惠券体系增强）done、P35（站内信中心）done。

## Goals

- 每日扫描近 N 天（可配置）即将过期的未使用用户券，向对应用户主动推 MARKETING 站内信提醒（从「拉取式发现」升级为「主动推送 reminder」）。
- 复用积分过期预警同模式：按 userId 聚合、幂等（每日每用户至多一条聚合消息）、事件开关门控。
- 新增定时任务（`scheduler.yaml` + `MallJobInvoker` 入口 + BizModel 方法）。
- 关闭 deferred 条目。

## Non-Goals

- 过期**之后**的通知（`expireCoupons` job 已将状态变更为过期，用户可在「我的优惠券」过期 Tab 查看；本计划只做**过期前**预警）。
- SMS/Email 多渠道推送（`nop-integration` 通道未引入，为独立基建 successor）。
- 拼团成功 / 评价奖励等其他事件通知（各自 successor，非本计划结果面）。
- 站内信中心 UI 改动（P35 已交付）。

## Task Route

- Type: `implementation-only change`（业务设计已在 `system-configuration.md` 消息中心章节覆盖；本计划为 deferred 的执行 slice，接线既有 sendUserMessage 基座）
- Owner Docs: `docs/design/system-configuration.md`（站内信/消息中心章节，事件→msgType 映射表新增「优惠券即将过期」）、`docs/design/marketing-and-promotions.md`（优惠券章节「过期预警」）
- Skill Selection Basis: 后端 BizModel + 调度 job 接线 → `nop-backend-dev` + `nop-testing`；无 ORM/前端改动

## Infrastructure And Config Prereqs

- 无新外部服务/端口/密钥。无 Protected Area 触发（不动 ORM、不动支付/合规）。
- 新增 config key：`mall_coupon_expiry_remind_days`（预警提前天数，默认 3）、`mall_message_event_enabled_coupon_expiry_remind`（事件开关）。

## Decision Points (to resolve in-phase)

- **D1 — 预警 msgType：** 抉择 **MARKETING(10)**（优惠券过期预警为营销促活，驱动用户消费用券）。备选 SYSTEM(20)（与积分过期预警一致）。抉择 MARKETING——优惠券本身为营销资产，预警目的为促转化，归营销消息分类更合理。残留风险：若运营希望归系统提醒则改 SYSTEM（config 不变，仅 msgType 值）。
- **D2 — 预警频率与幂等：** 抉择 **每日一次、每用户至多一条**（job 每日，扫描窗口内未使用券，按 userId 去重发一条聚合消息「您有 N 张优惠券即将过期」）。幂等：注入 `ILitemallUserMessageBiz`，按 `userId + msgType(MARKETING) + title(优惠券即将过期) + addTime(today)` 查询当日是否已存在，存在则跳过（镜像积分过期预警 D2 幂等模式 `2026-06-29-1921-3:49`）。
- **D3 — 消息内容粒度：** 抉择 **聚合摘要**（「您有 N 张优惠券将于 X 月 X 日过期，请尽快使用」，不逐张列出券名）。备选逐张列出（消息过长）。残留风险：用户需进「我的优惠券」查看明细，消息内不含券名列表。
- **D4 — 查询窗口机制：** `doFindListByQueryDirectly` + `FilterBeans.eq(status, 0)` + `FilterBeans.ge(endTime, now)` + `FilterBeans.le(endTime, now+days)`，endTime ASC，limit 500（参照 `expireCoupons:308-311` 模板扩展 ge 下界）。按 userId LinkedHashMap 聚合（Σ count + 最早 endTime，迭代顺序保留 endTime ASC）。**NULL endTime：** `endTime` 列非 mandatory（`app-mall.orm.xml:573`），NULL 行被 `ge/le` SQL 语义自动排除（安全），与积分批次 `expireTime` 总有值不同——此处靠 filter 语义保证。

## Execution Plan

### Phase 1 - 优惠券过期前置预警推送（调度 job + 站内信）

Status: completed
Targets: `app-mall-service/.../entity/LitemallCouponUserBizModel.java`、`app-mall-api/.../ILitemallCouponUserBiz.java`、`app-mall-service/.../scheduler/MallJobInvoker.java`、`app-mall-app/.../scheduler.yaml`
Required Skill: `nop-backend-dev`, `nop-testing`

- Item Types: `Add | Decision`
- Prereqs: P35 站内信中心 done（已满足）

- [x] **Skill loading gate:** 扫描 available skills，加载 `nop-backend-dev` + `nop-testing`。读完 routing table 必读文档（含调度 job 接线、跨实体调用 IBiz-first、`@BizMutation` 事务边界、错误处理、bizmodel-method-selfcheck）。列已读文档路径如下。每写完一个方法用 skill selfcheck 校验。
  - Docs read: `.opencode/skills/nop-backend-dev/SKILL.md`、`.opencode/skills/nop-testing/SKILL.md`、`nop-entropy/docs-for-ai/04-reference/bizmodel-method-selfcheck.md`（19 项逐方法自检）；直接先例 `app-mall-service/.../entity/LitemallPointsAccountBizModel.java`（`sendPointsExpiryReminders` 聚合/幂等/事件开关模式）+ 其测试 `TestLitemallPointsAccountBizModel.java`；`MallNotificationService.java`（`sendUserMessage`/`isEventMessageEnabled`）、`MallJobInvoker.java`（`sendPointsExpiryReminders` job 模板）、`scheduler.yaml`（`send-points-expiry-reminders` job 模板）；实体 `_LitemallCouponUser`（propId 映射 userId(2)/couponId(3)/status(4)/usedTime(5)/startTime(6)/endTime(7)/orderId(8)/addTime(9)）、`_AppMallDaoConstants`（MSG_TYPE_MARKETING=10）、`ILitemallSystemBiz.getConfig`、`ILitemallUserMessageBiz.sendUserMessage`（5-arg）。
- [x] **Decision D1/D2/D3/D4：** 确认 MARKETING msgType + 每日每用户一条聚合 + 幂等查 UserMessage + 窗口查询机制 + remindDays config 缺省 3，记录备选与残留风险。
- [x] **Add（注入）：** `LitemallCouponUserBizModel` 注入 `MallNotificationService` + `ILitemallUserMessageBiz` + `ILitemallSystemBiz`（均非 private，IBiz-first）。
- [x] **Add（预警方法）：** `ILitemallCouponUserBiz` + `LitemallCouponUserBizModel` 新增 `sendCouponExpiryReminders(IServiceContext context)` `@BizMutation`：事件开关门控（`coupon_expiry_remind`）→ 读 `mall_coupon_expiry_remind_days`（缺省 3）→ 窗口查询（D4）→ 按 userId 聚合（Σ count + 最早 endTime）→ 幂等检查 → `notificationService.sendUserMessage(uid, MARKETING, "优惠券即将过期", "您有 N 张优惠券将于 X 月 X 日过期，请尽快使用")`。
- [x] **Add（job）：** `MallJobInvoker.sendCouponExpiryReminders()`（参照 `sendPointsExpiryReminders:132` 模式：`new ServiceContextImpl()` → `couponUserBiz.sendCouponExpiryReminders(context)` → LOG.info）。
- [x] **Add（scheduler）：** `scheduler.yaml` 注册 `send-coupon-expiry-reminders` job（repeatInterval=86400000 每日，参照 send-points-expiry-reminders）。
- [x] **Proof：** `TestLitemallCouponUserBizModel` 新增 IGraphQLEngine 测试（`LitemallCouponUser__sendCouponExpiryReminders` mutation）：(a) 多张即将过期券→一条聚合 MARKETING 站内信；(b) 窗口外/已使用/已过期券不发；(c) 同用户多张券聚合；(d) 幂等（重跑当日不重复）；(e) 事件开关关闭不发；(f) remindDays 配置覆盖。
  - 验证命令：`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar`（BUILD SUCCESS 全 10 模块）+ `./mvnw test -pl app-mall-service -am`（**516 测试 0 失败**，`TestLitemallCouponUserBizModel` 16 测试 0 失败 +5 新增）

Exit Criteria:

- [x] 即将过期的未使用券触发每用户每日一条聚合 MARKETING 站内信
- [x] 窗口外/已使用/已过期券不触发；幂等（重跑不重复）；事件开关可控
- [x] **API 测试：** `sendCouponExpiryReminders` `@BizMutation` 通过 `IGraphQLEngine` 测试（`JunitAutoTestCase` 录制回放）
- [x] owner doc `system-configuration.md` 事件→msgType 映射表新增「优惠券即将过期」（MARKETING）+ `marketing-and-promotions.md` 优惠券章节「过期预警」
- [x] deferred 源条目标注「已由 successor 关闭」
- [x] `docs/logs/` updated

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent（fresh sessions `ses_0ec39d2bcffe03XxRsBAFWB9QL` round-1, `ses_0ec29c2e3ffeB854lgjzrtb5sv` round-2）
- Evidence: Round-1 REVISE（3 blockers: B1 CouponUser propId 映射错误 / B2 LitemallCoupon.name propId 错误 / B3 endTime NULL 未说明）→ 全部修订（propId 修正为 userId(2)/couponId(3)/status(4)/usedTime(5)/startTime(6)/endTime(7)/orderId(8)/addTime(9)；name propId=2；D4 补 NULL endTime 安全说明）→ Round-2 PASS（"all three assigned blockers resolved correctly"，propId 经 `app-mall.orm.xml:565-577` 与 `:516` 复核确认）。其余全部设计项（precedent fidelity / dependency / skill / anti-slacking / Decision / Proof）round-1 即验证通过。

## Closure Gates

- [x] in-scope behavior is complete
- [x] relevant docs are aligned
- [x] verification has run（`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` + `./mvnw test -pl app-mall-service -am`）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（`sendCouponExpiryReminders`）
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed，Nop-platform phases do not write `none` without justification
- [x] skill loading verification: each phase scanned available skills, loaded all matching skills, read ALL mandatory docs listed in skill routing tables, and selfchecked after each method/class
- [x] text consistency verified: status / phases / gates / log all agree
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

### 拼团成功 / 评价奖励等其他事件通知

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 源 deferred（`2026-06-29-1921-3:133`）列出多个事件（优惠券即将过期/拼团成功/评价奖励等）。本计划仅交付优惠券过期预警，其余事件为各自特性的独立 successor，非本计划结果面。
- Successor Required: `yes`（触发条件：各特性要求事件→站内信接线时，按已建立模式扩展）

## Closure

<!-- IMPORTANT: Closure audit MUST be performed by an independent subagent (different session/context).
     Do NOT fill this section yourself — leave it for the dedicated closure auditor. -->

Status Note: 优惠券即将过期前置预警已完整交付并通过独立闭合审计。所有 Phase 1 执行项与 Exit Criteria 已 `[x]`，live repo 实证齐全：BizModel 方法实体逻辑落地（非空壳）、job 调度接线、scheduler 注册、5 IGraphQLEngine 测试覆盖、owner docs + 日志 + deferred 源标注同步。

Closure Audit Evidence:

- Reviewer / Agent: 独立闭合审计 subagent（fresh session，非实施 agent）
- Evidence:
  - **Anti-Hollow / 实装核验：** `LitemallCouponUserBizModel.sendCouponExpiryReminders`（`:349-405`，`@BizMutation`）实体逻辑完整——事件开关门控（`isEventMessageEnabled(EVENT_KEY_COUPON_EXPIRY_REMIND)`）→ 读 `mall_coupon_expiry_remind_days`（缺省 3）→ 窗口查询（`status=0 && ge(endTime,now) && le(endTime,now+remindDays)`，endTime ASC，limit 500）→ 按 userId LinkedHashMap 聚合（Σ 张数 + 最早 endTime）→ 幂等 `hasTodayExpiryReminder` → `notificationService.sendUserMessage(uid, MARKETING, "优惠券即将过期", ...)`。非空壳、无 `return null` 占位、无吞异常。
  - **接线核验：** `MallJobInvoker.sendCouponExpiryReminders()`（`:149-153`）`new ServiceContextImpl()` → `couponUserBiz.sendCouponExpiryReminders(context)` → LOG.info，运行时可达；`scheduler.yaml`（`:124-133`）注册 `send-coupon-expiry-reminders` job（repeatInterval=86400000 每日）→ 入口 `MallJobInvoker.sendCouponExpiryReminders`，调度链完整。
  - **接口契约：** `ILitemallCouponUserBiz.sendCouponExpiryReminders(IServiceContext)`（`:57-58`，`@BizMutation`）声明与 impl 签名一致。
  - **API 测试核验（IGraphQLEngine）：** `TestLitemallCouponUserBizModel`（`:320-400`）helper `sendCouponExpiryReminders()` 经 `graphQLEngine.newRpcContext(mutation, "LitemallCouponUser__sendCouponExpiryReminders", ...)` 调用 GraphQL，5 测试覆盖 (a) 聚合→一条 MARKETING、(b) 窗口外/已使用/已过期跳过、(d) 幂等重跑、(e) 事件开关关闭、(f) remindDays 配置覆盖。非实体级纯逻辑测试替代。
  - **Docs sync：** `system-configuration.md:171` 事件→msgType 映射表新增「优惠券即将过期」（MARKETING）+ `:174` 归类说明；`marketing-and-promotions.md:55-66` 优惠券章节「过期前置预警」子节（窗口/聚合/msgType/幂等/配置/调度）齐全；`docs/logs/2026/06-29.md` 顶部 Phase 1 日志完整。
  - **Deferred 关闭：** 源 `2026-06-29-1921-3:138` 标注「Successor Closed（部分）」——优惠券即将过期已交付，剩余拼团成功/评价奖励为各自 successor。
  - **一致性：** Plan Status=completed / Phase 1 Status=completed / 11 Closure Gates 全 `[x]` / Exit Criteria 全 `[x]` / 日志 — 全部一致。
  - **验证基线（实施期记录，日志 `06-29.md:19`）：** `./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS（全 10 模块）；`./mvnw test -pl app-mall-service -am` 516 测试 0 失败（`TestLitemallCouponUserBizModel` 16 测试 0 失败 +5 新增）。

Follow-up:

- 拼团成功 / 评价奖励等其他事件通知见 Deferred But Adjudicated（含触发条件）。
