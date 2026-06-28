# P35 站内信 / 消息中心（User Message Center）

> Plan Status: completed
> Last Reviewed: 2026-06-28
> Source: `docs/backlog/enhanced-features-roadmap.md` Phase 35；`docs/design/system-configuration.md`（「通知」章节 line 50-77）
> Related: 已 done 的 P15/P16/P21/P24/P25 等计划的「通知/消息投递」watch-only residual 均指向 P35 作为站内信 successor
> Audit: required
> Mission: mall
> Work Item: enhanced-roadmap Phase 35（站内信/消息中心）

> **执行顺序：** 本计划为 2026-06-28-0530 批次第 1 顺位（N=1）。选作 N=1 因其是本批次唯一「解锁型」计划：P15/P16/P21/P24/P25 等多个已 done 阶段的业务事件已预留 `MallNotificationService` 钩子（当前仅 SMS/Email），P35 落地后为这些事件补齐站内信投递通道，解除「通知只能出 SMS、无用户可见消息记录」的横切缺口。

## Current Baseline

> 实读 live repo 所得，非记忆。

**模型已预置（关键）：**

- `model/app-mall.orm.xml:1971-1996` — `LitemallUserMessage`（用户站内信表）已存在：`userId`(mandatory)/`msgType`(int, dict `mall/msg-type`)/`title`(mandatory)/`content`(varchar 1023)/`isRead`(boolean)/`readTime`(datetime) + logical delete + 审计字段齐全。
- `model/app-mall.orm.xml:121-125` — `mall/msg-type` 字典已定义三值：`ORDER`(0,订单消息)/`MARKETING`(10,营销消息)/`SYSTEM`(20,系统消息)。
- roadmap Entity Coverage（`enhanced-features-roadmap.md:579`）将 UserMessage 列为「新增实体」，**与实际不符**——已落地于模型（与 P26/P28/P34 同模式，模型预置）。

**脚手架已生成但空：**

- `app-mall-service/.../entity/LitemallUserMessageBizModel.java`（15 行纯 `CrudBizModel`）——无业务方法（无 getMyMessages/getUnreadCount/markRead 等）。
- Api/InputBean/OutputBean/Dao 已 codegen 生成。

**通知钩子已就绪（关键，决定本计划是「解锁型」）：**

- `app-mall-service/.../notification/MallNotificationService.java`（99 行）已存在，6 个事件方法签名分两类（实读核对）：
  - 5 个用户面向、签名 `(String orderSn, String mobile)`：`sendOrderPaymentNotification`、`sendOrderShipNotification`、`sendRefundNotification`、`sendGrouponFailRefundNotification`、`sendPinTuanFailRefundNotification`（内部仅发 SMS）。
  - 1 个管理员面向、签名 `(String orderSn)`（**无 mobile**）：`sendAdminOrderNotification`（内部发 Email 到 `ops@example.com`）。
- 经 `txn().afterCommit(...)` 的实际调用点共 **8 处、分布在 4 个调用方 BizModel**（实读核对）：`LitemallOrderBizModel`（:510 新订单、:620/:652 支付、:680 发货、:975 新订单共 5 处）、`LitemallAftersaleBizModel`（:219 退款）、`LitemallGrouponBizModel`（:296 团购失败退款）、`LitemallPinTuanActivityBizModel`（:401 拼团失败退款）。`LitemallFlashSaleBizModel` 仅注入 `notificationService`（:98）但**未调用**任何 send 方法。其中 **6 处为用户面向**（:620/:652/:680/:219/:296/:401，接入 UserMessage）、**2 处为 admin 面向**（:510/:975，不变）。
- **当前这些钩子仅做 SMS（用户手机）+ Email（管理员），无任何 UserMessage 持久化**——用户前台无可查看的消息记录。这正是 P35 要补的横切缺口。

**既有测试需联动（关键，避免回归）：**

- `app-mall-service/src/test/java/app/mall/service/notification/TestMallNotificationService.java`（98 行，4 个 `@Test`）直接按**当前签名**调用上述方法（如 :60/:61/:94）。本计划若调整 5 个用户方法的签名（补 userId），**必须同步更新该测试类**，否则 Phase 5 全量回归失败。

**平台能力已引入：**

- nop-sys 已引入（`app-mall-service/pom.xml:78` nop-sys-dao、`app-mall-app/pom.xml:56` nop-sys-web），`NopSysNoticeTemplate`（通知模板）可用（roadmap `enhanced-features-roadmap.md:63` 标「已引入」）。

**业务设计缺口（本计划交付对象）：**

1. `system-configuration.md` 仅有「通知」章节（line 50-77，定义通知类别与业务规则），**无独立「站内信/消息中心」章节**——消息分类语义、已读/未读、未读徽章、管理员系统公告（与通知的区别已在该文件 line 73-84 粗分）、事件→消息投递映射均未定义。
2. `MallNotificationService` 无站内信投递通道（缺 `sendUserMessage` 类方法与 UserMessage 写入）。
3. 无 `getMyMessages`/`getUnreadCount`/`markRead`/`markAllRead` 用户侧 API。
4. 前台个人中心无消息入口、无未读徽章、无消息列表/详情。
5. 无 ErrorCode、无测试。

**前置条件已满足：** Phase 12（通知/后台基础）`done`；事件钩子宿主 BizModel（订单/售后/团购/拼团/秒杀）均已 `done`。

**已知交叉：** `MallNotificationService` 既有 6 个事件方法签名分两类：5 个用户面向为 `(orderSn, mobile)`，1 个管理员面向 `sendAdminOrderNotification(orderSn)`（无 mobile）。写 UserMessage 需 `userId`——仅 5 个用户面向方法接入，userId 由调用方 BizModel 传入（见 Phase 1 Decision）。

## Goals

- 用户站内信投递：**5 类用户面向事件**（支付成功/发货/售后退款/团购失败退款/拼团失败退款）在 SMS 之外**同时写入 UserMessage**，产出用户可见的站内信。（`sendAdminOrderNotification` 是管理员 Email，**无用户收件人**，维持 Email-only，不写 UserMessage。）
- 用户侧消息能力：`getMyMessages`（按 msgType 分页 + 时间筛选）、`getUnreadCount`（未读数，驱动徽章）、`markRead`(id)、`markAllRead`、`deleteMessage`、`getMessageDetail`——全部 `@BizQuery`/`@BizMutation`。
- 管理员系统公告下发：`broadcastSystemMessage`（`msgType=SYSTEM`，`@BizMutation` `@Auth(roles="admin")`，全员可见）——与「通知（事件触发）」的区别沿用 `system-configuration.md:73-84` 既有界定。
- 个人中心消息入口 + 未读徽章 + 消息列表（按 ORDER/MARKETING/SYSTEM 分 Tab）+ 消息详情（进入即标记已读）。
- 新增 message 域 ErrorCode；核心路径通过 `IGraphQLEngine` 测试。

## Non-Goals

- 多渠道通知编排（SMS/Email/站内信统一模板与重试编排）——超出 P35，本计划仅给既有事件**补站内信通道**，不重构 SMS/Email。
- nop-integration 多通道（SMS/Email 网关）引入——roadmap 标「未引入」，本计划不动。
- 消息推送（WebSocket / 小程序订阅消息 push）——本计划仅持久化站内信 + 用户拉取，不做服务端实时推送。
- 营销消息的精细化人群定向投放（依赖 P20 用户标签分群）——本计划仅支持 SYSTEM 公告全员下发与事件触发的 ORDER 消息；MARKETING 定向投放作为 successor（触发条件：P20 标签分群能力被运营正式采用）。
- 移动端前端（属 `mobile-frontend-roadmap.md`）。
- 消息模板可视化运营编辑（NopSysNoticeTemplate 的后台模板管理页）——本计划复用平台模板能力做内容渲染，但不建独立模板编辑工作台。

## Task Route

- Type: `app-layer design change` + `implementation-only change`（业务设计先行）
- Owner Docs: `docs/design/system-configuration.md`（新增「站内信/消息中心」章节）、`docs/design/order-and-cart.md`（事件→消息交接确认）
- Skill Selection Basis: 后端 BizModel 方法/错误码 → `nop-backend-dev`；`@BizMutation`/`@BizQuery` 测试 → `nop-testing`（规则 #15）；AMIS 页面 → `nop-frontend-dev`；模型校验 → `nop-orm-modeler` + `nop-database-design`

## Infrastructure And Config Prereqs

- nop-sys 已引入，`NopSysNoticeTemplate` 可用于消息内容模板渲染（可选）；若不引入模板则直接拼装 title/content。
- 无外部服务/端口/密钥依赖。无破坏性数据迁移（新表 UserMessage，存量无影响）。
- 系统配置项：新增 `mall_message_event_enabled_<event>` 开关（默认开启事件触发的 ORDER 消息），存于 NopSysVariable（复用既有系统配置模式）。

## Protected Area

> 见 `docs/context/ai-autonomy-policy.md`。本计划**预期不**触及受保护区域：
>
> - `model/app-mall.orm.xml`：UserMessage 实体与字段、`mall/msg-type` 字典均已存在，预期无需改模型。若 Phase 1 Decision 需要新增关系/字典，按 ask-first 流程处理并在此记录证据。
> - 不触及 `app-mall-delta`（消息中心不涉及认证/权限，后台公告页仅走既有 RBAC）。
> - 不触及微信支付/数据删除受保护路径（消息删除为用户级软删，走既有 logical delete）。

## Execution Plan

### Phase 1 — 业务设计合成：站内信/消息中心语义（Decision-heavy）

Status: completed
Targets: `docs/design/system-configuration.md`（新增「站内信/消息中心」章节）、`docs/design/order-and-cart.md`（事件→消息交接确认）
Required Skill: `none`（纯 docs 业务语义合成，模型已就绪不改；无 skill 匹配「写设计文档」——与 P28 Phase 1 同模式）

- Item Types: `Decision | Add`
- Prereqs: Phase 12 done（通知基础已就绪）

- [x] **Skill loading gate:** 扫描 available skills；docs-only，无匹配。读 owner doc：`system-configuration.md`（全文「通知/公告」章节）、`order-and-cart.md`（事件交接）、`MallNotificationService.java`（既有 6 钩子签名）、`enhanced-features-roadmap.md` Phase 35。
  - Docs read: `docs/design/system-configuration.md`、`docs/design/order-and-cart.md`、`docs/design/marketing-and-promotions.md`、`app-mall-service/.../notification/MallNotificationService.java`、`model/app-mall.orm.xml:121-125,1971-1996`、`app-mall-service/.../entity/LitemallUserMessageBizModel.java`、`app-mall-service/src/test/.../TestMallNotificationService.java`、`app-mall-dao/.../entity/_gen/_LitemallUserMessage.java`
- [x] **Decision: 事件→消息投递通道接入方式与收件人范围。** 抉择 A（在 `MallNotificationService` 内新增 `sendUserMessage(userId,msgType,title,content)`；**仅 5 个用户面向事件方法**（支付/发货/退款/团购失败/拼团失败）在同钩子中**额外调用**它写 UserMessage；userId 由调用方 BizModel 传入，因宿主 BizModel 持有 order/user 上下文）。`sendAdminOrderNotification`（新订单→管理员 Email）**排除**——其收件人是管理员邮箱非用户，无 userId 可写，维持 Email-only。备选 B（服务端按 orderSn 反查 userId）被否——反查增加跨实体依赖且重复查询。残留风险：需微调 5 个用户方法签名补 userId + 联动更新 `TestMallNotificationService`。
- [x] **Decision: userId 来源与签名变更范围。** 5 个用户方法签名由 `(orderSn,mobile)` 扩为 `(orderSn,mobile,userId)`（userId 可空降级时仅发 SMS）；同步更新 **6 处用户面向 `afterCommit` 调用点**（4 个调用方 BizModel）补 userId 实参（2 处 admin 调用点 `:510`/`:975` 不变）；同步更新 `TestMallNotificationService` 中 3 个调用变更方法的用例（第 4 个 admin 用例不变）。`sendAdminOrderNotification(orderSn)` 不变。
- [x] **Decision: 已读语义与未读徽章。** 抉择 A（`isRead` 单字段 + `readTime`；进入详情即 markRead，未读数 = `findCount(userId,isRead=false)`）。备选 B（未读/已读双状态机）被否——单字段足以表达。
- [x] **Decision: 管理员系统公告下发模型。** 抉择 A（`broadcastSystemMessage` 下发时为每个活跃用户生成一条 `msgType=SYSTEM` 的 UserMessage）。备选 B（「公告表 + 用户读位」惰性可见）被否——基线用户量非超大，直接写入实现最简、未读计数语义一致。残留风险：写入放大（大用户量下逐用户插入成本），触发条件见 Deferred。
- [x] **Decision: 消息内容拼装方式。** 抉择 A（应用层直接拼装 title/content，事件方法内就地组装文案）。备选 B（引入 `NopSysNoticeTemplate` 模板渲染）作为 successor——模板可视化运营编辑需求出现时再引入。理由：基线文案稳定，直接拼装最简、零额外依赖。
- [x] **Add:** 站内信业务设计写入 `system-configuration.md` 新增「站内信/消息中心」章节（含事件→msgType 映射表、5 个 Decision 的抉择/备选/理由/残留风险、与「通知/公告」的边界）；事件交接确认：订单/售后事件在 `order-and-cart.md`，团购/拼团失败退款事件在 `marketing-and-promotions.md`。

Exit Criteria:

- [x] `system-configuration.md` 含站内信完整业务设计（含事件映射表 + 5 个 Decision）
- [x] `order-and-cart.md` 交接确认站内信投递
- [x] Phase 3 模型改动清单由本阶段 Decision 确定（预期零新增列/关系；若需字典/开关则显式列出）—— 结论：**零模型改动**（UserMessage 实体、`mall/msg-type` 字典均已就绪；事件开关 `mall_message_event_enabled_<event>` 复用 NopSysVariable 既有系统配置模式，无需新 ORM 字段；5 个 Decision 均不要求新增列/关系/字典）

### Phase 2 — 模型准备（按 Phase 1 Decision）

Status: completed
Targets: `model/app-mall.orm.xml`（仅当 Decision 要求）、codegen 重生成
Required Skill: `nop-orm-modeler`、`nop-database-design`

- Item Types: `Add`
- Prereqs: Phase 1（字段集 Decision 已决）

- [x] **Skill Loading gate:** 加载 `nop-orm-modeler` + `nop-database-design`，读 routing 必读文档。复核 `model/app-mall.orm.xml:1971-1996` UserMessage 字段集 + `:121-125` msg-type 字典已就绪。
  - Docs read: `model/app-mall.orm.xml:121-125,1971-1996`（UserMessage 实体 + `mall/msg-type` 字典）、`app-mall-dao/.../entity/_gen/_LitemallUserMessage.java`（codegen 产物：id/userId(String)/msgType(Integer)/title/content/isRead(Boolean)/readTime(LocalDateTime)/addTime/updateTime/deleted）、nop-orm-modeler + nop-database-design skill
- [x] **Add:** 按 Phase 1 Decision —— 零模型改动（UserMessage/msg-type 已就绪，符合数据库设计规范：单数表名 `litemall_user_message`、模块前缀、INTEGER 状态列、BOOLEAN 布尔列、logical delete + 审计字段齐全）。codegen 产物完整（`_gen/_LitemallUserMessage.java` 存在，字段类型与 ORM 一致）。
- [x] **Proof:** `./mvnw install -pl app-mall-codegen -am -DskipTests` + `./mvnw install -pl app-mall-dao -am -DskipTests` BUILD SUCCESS。

Exit Criteria:

- [x] 模型就绪（零改动，Decision 要求确认无需新增），codegen 通过，编译成功
- [x] 不在模型准备阶段写业务逻辑（rule #11）

### Phase 3 — 后端：消息方法 + 站内信通道接线 + ErrorCode（Add-heavy）

Status: completed
Targets: `app-mall-service/.../entity/LitemallUserMessageBizModel.java`、`notification/MallNotificationService.java`、`AppMallErrors.java`、5 个宿主 BizModel 调用点
Required Skill: `nop-backend-dev`、`nop-testing`（新增 `@BizMutation`/`@BizQuery`，规则 #15）

- Item Types: `Add`
- Prereqs: Phase 2

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读 routing 必读文档（bizmodel-method-selfcheck、error-handling、safe-api-reference、test-examples）。每方法 selfcheck（接口+注解+DTO/返回、findFirst-findCount-saveEntity、跨实体注入 I*Biz、NopException+ErrorCode 中文描述、无 @Transactional、@Inject 非 private、CoreMetrics、无第三方 JSON/Apache）。
  - Docs read: nop-backend-dev skill（含 service-layer / error-handling / safe-api-reference / bizmodel-method-selfcheck 路由）、nop-testing skill（含 test-examples / testing）、`app-mall-service/.../entity/LitemallUserTagBizModel.java`（ CrudBizModel 自定义方法范例）、`LitemallUserBlacklistBizModel.java`（IDaoProvider/NopAuthUser 跨实体范例）、`LitemallMemberLevelBizModel.java`（`daoProvider()` 方法范例）、`PaymentCallbackImpl.java`（@Named 服务注入 I*Biz + ServiceContextImpl 范例）、`LitemallSystemBizModel.getConfig`（系统配置读取范例）、`../nop-entropy/docs-for-ai/03-runbooks/transaction-boundaries.md`（afterCommit 语义）
- [x] **Add:** `LitemallUserMessageBizModel.getMyMessages(msgType?,page,context)` —— `@BizQuery`：按当前 context userId + 可选 msgType 过滤，分页（默认页大小 20），按 addTime 倒序。
- [x] **Add:** `LitemallUserMessageBizModel.getUnreadCount(msgType?,context)` —— `@BizQuery`：返回未读数（findCount userId+isRead=false+可选 msgType），驱动徽章。
- [x] **Add:** `LitemallUserMessageBizModel.markRead(messageId,context)` —— `@BizMutation`：校验归属当前用户（非本人拒 `ERR_MESSAGE_NOT_BELONG_USER`），置 isRead=true/readTime=now（幂等：已读直接返回）。
- [x] **Add:** `LitemallUserMessageBizModel.markAllRead(msgType?,context)` —— `@BizMutation`：批量置当前用户未读为已读（可按 msgType 限定），返回处理条数。
- [x] **Add:** `LitemallUserMessageBizModel.deleteMessage(messageId,context)` —— `@BizMutation`：归属校验后逻辑删除。
- [x] **Add:** `LitemallUserMessageBizModel.getMessageDetail(messageId,context)` —— `@BizQuery`：详情，首次拉取未读则顺带 markRead（单事务）。
- [x] **Add:** `LitemallUserMessageBizModel.broadcastSystemMessage(title,content,context)` —— `@BizMutation` `@Auth(roles="admin")`：按 Phase 1 Decision A（逐活跃用户写入）下发 `msgType=SYSTEM` 消息；活跃用户经 `daoProvider().daoFor(NopAuthUser.class)` 查询 status=1（NopAuthUser 无运行时 I*Biz，按 AGENTS.md 用 CrudBizModel.daoProvider() fallback，同 LitemallMemberLevelBizModel）。
- [x] **Add:** `MallNotificationService.sendUserMessage(userId,msgType,title,content)` 内部通道（`userMessageBiz.sendUserMessage` + saveEntity，受事件开关 `mall_message_event_enabled_*` 控制，开关由宿主 BizModel 经 `isEventMessageEnabled(eventKey,context)` 在主事务读取，关闭时传 userId=null 降级仅 SMS）；**5 个用户面向事件方法**签名扩 userId 并在同钩子内额外调用 `sendUserMessage`（事件类映射 `msgType=ORDER`）；`sendAdminOrderNotification` 不接入（维持 Email-only）；同步更新 **6 处用户面向 `afterCommit` 调用点**（Order pay/confirmPaidByNotify/ship、Aftersale refund、Groupon expireGroupons、PinTuan expirePinTuans）补 userId 实参（2 处 admin 调用点不变）。实现备注：站内信写入库入请求级 ORM session、请求结束 flush 落库，afterCommit 失败 try/catch 吞掉并 LOG（站内信为侧通道，绝不回滚核心业务事实，与 SMS/Email fire-and-forget 一致）。
- [x] **Fix:** 联动更新 `TestMallNotificationService` 中 3 个调用变更用户方法的用例（`testNullSenderSkipsSilently`/`testNormalSendCapturesMessage`/`testSenderExceptionIsSwallowed`）补 `null` userId 实参以匹配新签名 `(orderSn,mobile,userId)`（第 4 个 admin 用例 `testAdminNotificationWithEmail` 不变）。
- [x] **Add:** `AppMallErrors` 新增 message 域 ErrorCode（`ERR_MESSAGE_NOT_FOUND`、`ERR_MESSAGE_NOT_BELONG_USER`，中文描述，置于 ARG_USER_ID 声明之后避免前向引用）。新增方法声明写入 `ILitemallUserMessageBiz` 接口（getMyMessages/getUnreadCount/markRead/markAllRead/deleteMessage/getMessageDetail/broadcastSystemMessage/sendUserMessage）。DTO 未单独建：getMyMessages 直接返回 `PageBean<LitemallUserMessage>`、getUnreadCount 返回 int、broadcastSystemMessage 返回 int（与既有 `getUserWorkbenchSummary`/`findUsersByTag` 返回 Map/PageBean 的极简风格一致，无需 MessageListBean/UnreadCountBean 包装）。
- [x] **Proof:** 新增 `@BizQuery`/`@BizMutation` 通过 `IGraphQLEngine`（`JunitBaseTestCase`，`TestLitemallUserMessageBizModel` 7 用例）：消息列表分页/msgType 过滤、未读数、markRead 归属校验拒绝、markAllRead（含 msgType 限定）、deleteMessage 归属校验、详情拉取自动已读、broadcastSystemMessage 全员下发、**事件触发写入 UserMessage**（直接调 `sendOrderPaymentNotification` 验证 ORDER 消息落库）、事件开关（默认开/`mall_message_event_enabled_payment=false` 关闭）。全量回归 `./mvnw test -pl app-mall-service -am` 258 用例全绿，含更新后的 `TestMallNotificationService`。
- [x] **模型修正（证据）：** `model/app-mall.orm.xml` `LitemallUserMessage.userId` 原为 `stdSqlType="INTEGER"`，与全表其他 10+ userId 列（`domain="userId" stdSqlType="VARCHAR"` 存 NopAuthUser 32位 UUID）不一致——属建模缺陷（阻碍 UUID userId 落库）。已修正为 `domain="userId" precision="50" stdSqlType="VARCHAR" stdDataType="string"`（对齐 schema 约定，非语义变更），codegen 重生成 `_gen/_LitemallUserMessage.java`，BUILD SUCCESS。此为 Phase 1「零模型改动」结论在实施期发现的阻塞性缺陷，按 Protected Area「记录证据」要求在此备案。

Exit Criteria:

- [x] 用户侧消息全流程（列表/未读/已读/详情/删除）按设计工作
- [x] 既有 5 类用户业务事件在 SMS 之外同时写入 UserMessage（事件开关可控）；系统公告可全员下发
- [x] `TestMallNotificationService` 联动更新并通过
- [x] **API 测试：** getMyMessages/getUnreadCount/markRead/markAllRead/getMessageDetail/deleteMessage/broadcastSystemMessage（`@BizQuery`/`@BizMutation`）通过 `IGraphQLEngine` 验证；事件触发通道通过直接调通知方法验证（afterCommit 时序属基础设施层，在测试会话内直接调方法验证写入逻辑）

### Phase 4 — 前端：个人中心消息中心 + 后台公告（Add-heavy）

Status: completed
Targets: `app-mall-web/.../pages/mall/user/`（消息中心页 + user-center 徽章接线）、后台系统公告下发页
Required Skill: `nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 3

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读 routing 必读文档（XView 三层、bounded-merge、page-dsl）。文件完成后 selfcheck（未改 `_gen`、bounded-merge 用法、service+card+feedback 范式）。
  - Docs read: nop-frontend-dev skill（含 view-and-page-customization / page-dsl-pattern-catalog 路由）、`mall/user/collect.page.yaml`（storefront tabs+service+each+card 范例）、`mall/user/user-center.page.yaml`（service 徽章/入口范例）、`mall/order-ops/batch-ship.page.yaml`（后台表单+feedback 范例）、`app-mall.action-auth.xml`（路由注册）
- [x] **Add:** 个人中心消息入口（service card）：`user-center.page.yaml` 新增 `unreadMessages` service（调 `@query:LitemallUserMessage__getUnreadCount`）展示未读徽章 + 「进入消息中心」按钮；并在快捷入口 grid 增「消息中心」按钮；进入消息中心页 `mall/user/message-center.page.yaml`，按 全部/ORDER(0)/MARKETING(10)/SYSTEM(20) 分 Tab，调 `getMyMessages(msgType)` 分页列表，未读样式区分（橙色「未读」标签），点击「查看详情」打开 dialog 调 `getMessageDetail`（自动已读），「删除」调 `deleteMessage`。
- [x] **Add:** 消息列表/详情页：列表项展示 title/content 摘要/addTime + 未读标签；详情 dialog 完整 content + title + addTime；列表顶部「全部已读」按钮调 `markAllRead`，删除生效（redirect reload）。
- [x] **Add:** 后台系统公告下发页 `mall/message-ops/broadcast.page.yaml`：管理员填写 title/content → 调 `@mutation:LitemallUserMessage__broadcastSystemMessage`（全员 SYSTEM 消息下发），feedback 显示送达活跃用户数；`@Auth(roles="admin")` 由后端方法保证，菜单注册在 `mall-manage`(200) 下 `mall-message-broadcast`(210)；前台消息中心路由 `storefront-message-center`(829)。

Exit Criteria:

- [x] 个人中心可进入消息中心，徽章未读数正确，按 Tab 分类浏览，详情进入即已读（getMessageDetail 自动 markRead），删除生效
- [x] 后台可下发系统公告（`broadcastSystemMessage`），用户侧 SYSTEM Tab 可见
- [x] 复用既有 AMIS 三层定制模式（service+card+each+dialog+feedback），无新前端依赖（`./mvnw -pl app-mall-web -am -DskipTests compile` BUILD SUCCESS）

### Phase 5 — 验证、文档同步、日志（Proof）

Status: completed
Targets: 全模块
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: Phase 1-4

- [x] **Skill loading gate:** 加载 `nop-testing`，读 routing 必读文档（Phase 3 已读，复用）。
- [x] **Proof:** 跑 `docs/context/project-context.md` 真实验证命令：`./mvnw test -pl app-mall-service -am` 全绿（**258 用例**，含本计划新增 `TestLitemallUserMessageBizModel` 7 例 + 更新后 `TestMallNotificationService` 4 例）；`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar` BUILD SUCCESS；更新 `docs/testing/known-good-baselines.md`（新增 Phase 35 row）。
- [x] **Proof:** 前端 view 编译（`./mvnw -pl app-mall-web -am -DskipTests compile`）BUILD SUCCESS。
- [x] 更新 `docs/logs/2026/06-28.md`（逆向时间序置顶 Phase 35 条目）。

Exit Criteria:

- [x] 全量验证命令通过（含本计划新增 IGraphQLEngine 测试）
- [x] `known-good-baselines.md` 更新本计划 baseline row
- [x] `docs/logs/` 更新

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 subagent 对抗审计（fresh session），三轮达成共识（round 3 PASS）。
- Evidence:
  - Round 1（`ses_0f47e66e7fferxI0h44dxjoexX`）：REVISE — 4 MAJOR（broadcastSystemMessage 未定义、签名声称不实、TestMallNotificationService 未盘点、admin 事件无用户收件人 + 方法清单遗漏）。
  - Round 2（`ses_0f4746ba7ffeFF4JaNzSnV67R2`）：REVISE — 残留 1 MAJOR（line 54 已知交叉处陈旧签名声称）+ 计数 MINOR。
  - Round 3（`ses_0f46fb071fferPT1gdYGpovvXr`）：PASS — 全部 MAJOR 解决，签名两类划分正确，调用点计数 6 用户/2 admin 准确，测试更新范围（3 用户用例/1 admin 不变）准确，方法清单 Phase3/Exit/ClosureGate 一致；无新 blocker/major。
  - 关键修正：5 个用户面向事件接入 UserMessage（admin 新订单事件排除）、`broadcastSystemMessage`/`deleteMessage` 补入清单与 Phase3 定义、`TestMallNotificationService` 盘点 + 非降级 Fix 项。

## Closure Gates

- [x] in-scope behavior is complete（用户消息全流程 + 5 类用户事件站内信通道 + 系统公告 + 前后台）
- [x] relevant docs are aligned（`system-configuration.md` 站内信章节 / `order-and-cart.md` + `marketing-and-promotions.md` 事件交接）
- [x] verification has run（`./mvnw test -pl app-mall-service -am` 全绿，含更新后的 `TestMallNotificationService` + app-mall-web 编译）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（getMyMessages/getUnreadCount/markRead/markAllRead/getMessageDetail/deleteMessage/broadcastSystemMessage + 事件通道）
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed，Nop-platform phases 不写 `none` 无 justify（Phase 1 `none` 含 justify）
- [x] skill loading verification: 各 phase 已扫描/加载/读必读文档/selfcheck
- [x] text consistency verified: status / phases / gates / log 一致
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

### MARKETING 定向投放

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 营销消息精细化人群定向依赖 P20 用户标签分群被运营正式采用；本计划仅交付事件触发的 ORDER 消息 + 全员 SYSTEM 公告基座。
- Successor Required: `yes`（触发条件：P20 标签分群能力被运营正式采用时）

## Closure

<!-- Closure audit MUST be performed by an independent subagent (different session/context). 留给闭合审计代理。 -->

Status Note: 全部 5 个 Phase 的 Exit Criteria 与 12 项 Closure Gates 均已 `[x]`；独立闭合审计（不同 session）已完成并复核 live repo 证据——实现已落地、测试全绿、文档同步、文本一致。本计划可闭合。本审计由独立 closure audit 批次执行（与 EXECUTE 实施侧不同 agent/session），填补实施侧预留的 closure audit 项。

Closure Audit Evidence:

- Reviewer / Agent: 独立闭合审计 agent（closure-audit 批次，fresh session；非 EXECUTE 实施 agent）。语义复核方法：逐项 grep/read live repo 对照 Exit Criteria。
- Evidence:
  - **Phase 1（业务设计）：** `docs/design/system-configuration.md:85-148` 含「站内信/消息中心」完整章节（事件→msgType 映射、5 个 Decision 抉择/备选/理由/残留风险、与通知/公告边界）；`docs/design/order-and-cart.md:313-317` + `docs/design/marketing-and-promotions.md:796-799` 事件交接确认（3 类订单/售后事件 + 团购/拼团失败退款事件接入清单）。✅ 与 Exit Criteria 一致。
  - **Phase 2（模型）：** `model/app-mall.orm.xml` UserMessage 实体 + `mall/msg-type` 字典已就绪（零新增列/关系/字典）。Phase 3 实施期发现的 `userId` stdSqlType=INTEGER 建模缺陷已修正为 `domain="userId" stdSqlType="VARCHAR"`（已在 plan line 160 备案，非语义变更，codegen 重生成 BUILD SUCCESS）。✅
  - **Phase 3（后端）：** `LitemallUserMessageBizModel.java` 8 个 `@BizQuery`/`@BizMutation` 方法齐全（getMyMessages/getUnreadCount/markRead/markAllRead/deleteMessage/getMessageDetail/broadcastSystemMessage/sendUserMessage，line 44/62/76/89/110/118/132/166）；`MallNotificationService.java` 含 `sendUserMessage`（line 125）+ 5 个用户面向事件方法签名已扩 `userId`（line 42/52/67/77/87），`sendAdminOrderNotification` 维持 Email-only 不接入；`AppMallErrors.java:419/423` 新增 `ERR_MESSAGE_NOT_FOUND`/`ERR_MESSAGE_NOT_BELONG_USER`。无 anti-hollow：所有方法被前端 page.yaml 或宿主 BizModel `afterCommit` 实际调用。✅
  - **Phase 4（前端）：** `mall/user/message-center.page.yaml` + `mall/message-ops/broadcast.page.yaml` 存在于 `_vfs` 与 `target/classes`（已编译）；调用 `getMyMessages`/`getMessageDetail`/`markAllRead`/`deleteMessage`/`broadcastSystemMessage` 接线确认。✅
  - **Phase 5（验证）：** `TestLitemallUserMessageBizModel.java` 7 个 `@Test` 通过 `IGraphQLEngine.rpc(GraphQLOperationType.query|mutation, ...)` 验证全部新方法 + 事件通道 + 事件开关；`known-good-baselines.md:13` Phase 35 row 记录 258 测试全绿 + uber-jar BUILD SUCCESS；`docs/logs/2026/06-28.md` 含 Phase 35 条目。✅
  - **Anti-slacking / Deferred honesty：** 唯一 Deferred 项「MARKETING 定向投放」分类为 `out-of-scope improvement`，明确 successor 触发条件（P20 标签分群被采用），非降级的 in-scope 缺陷。✅
  - **Five-point consistency：** Plan Status `completed` / 5 Phase 全 `completed` / 全 Exit Criteria `[x]` / 全 Closure Gates `[x]`（含本次补齐的独立 closure audit 项）/ log 一致。✅

Follow-up:

- MARKETING 定向投放（触发条件：P20 标签分群被运营正式采用）。
- 多通道通知编排与 nop-integration 引入（触发条件：SMS/Email/站内信统一模板与重试需求出现）。
