# P25 拼团（Pin-Tuan / Social Group-Buy）

> Plan Status: completed
> Last Reviewed: 2026-06-28
> Source: `docs/backlog/enhanced-features-roadmap.md` Phase 25；`docs/design/marketing-and-promotions.md`、`docs/design/order-and-cart.md`、`docs/design/product-catalog.md`
> Related: `docs/plans/2026-06-28-0125-1-phase24-flash-sale-plan.md`（同批次 N=1，秒杀，确立并发库存/job 调度基础模式）；团购 Groupon 既有实现（`LitemallGrouponBizModel`，开团/参团/超时失败退款全链路——拼团业务模型高度参照之，但实体结构不同，见 Baseline）；下游贡献 P22 营销活动管理后台（依赖 P25 完成其「拼团活动配置」交付项）
> Audit: required
> Mission: mall
> Work Item: enhanced-roadmap Phase 25（拼团）

> **执行顺序：** 本计划为 2026-06-28-0125 批次第 2 顺位（N=2），在 P24 秒杀（N=1）之后执行。本计划改 `LitemallOrderBizModel.submit()` 接线 `pinTuanPrice`（actualPrice 减项层）。P24 秒杀若按其推荐抉择为独立 `flashSaleBuy` 路径则不改 submit()，与本计划无冲突；若 P24 抉择扩展 submit()，则须按 N=1→N=2 顺序执行并在本计划开工前重基线化 submit() 行引用。

## Current Baseline

> 实读 live repo（working tree）所得，非记忆。

**模型已就绪（脚手架已生成，三实体 + 关系完整）：**

- `LitemallPinTuanActivity`（拼团活动表，`app-mall.orm.xml:1622-1660`）：`goodsId`(mandatory)、`productId`(null=全部SKU)、`pinTuanPrice`(**拼团价**，mandatory decimal，即拼团专享单价)、`minUserCount`(成团人数门槛 mandatory)、`maxUserCount`(最多人数)、`expireHours`(有效时间/小时)、`status`(dict `mall/promotion-status` DRAFT0/ACTIVE10/FINISHED20/CLOSED30)、`remark`；`goods` to-one。
- `LitemallPinTuanGroup`（拼团开团记录表，`app-mall.orm.xml:1661-1700`）：`activityId`(mandatory)、`creatorUserId`(团长 mandatory)、`orderId`(团长订单ID)、`status`(dict `mall/pin-tuan-group-status` **ACTIVE0=进行中/SUCCESS10=拼团成功/FAILED20=拼团失败**)、`expireTime`(过期时间)；`activity` to-one、`members` to-many(cascadeDelete)。
- `LitemallPinTuanMember`（拼团参团记录表，`app-mall.orm.xml:1701-1727`）：`groupId`(mandatory)、`userId`(mandatory)、`orderId`(mandatory)、`group` to-one。
- 复用字典：`mall/promotion-status`（活动 status，P15 定义）、`mall/pin-tuan-group-status`（团状态，3 值已定义）。
- `LitemallOrder.pinTuanPrice`（propId=37, **mandatory**, displayName「拼团优惠金额」，`app-mall.orm.xml:1131`）——**死字段**：submit `:232` 初始化 ZERO 后从未参与计算（对比 `grouponPrice` 在 `:315-335` 计算并 `:354` 从 actualPrice 减去）。本计划核心接线对象。

**订单价格公式现状（关键）：**

- `submit()`：`orderPrice = goodsPrice + freight - coupon - promotionPrice`（P15 满减减项层）；`actualPrice = orderPrice - integralPrice - grouponPrice`（`:354`）。**`pinTuanPrice` 未进公式** → 接线后应为 `actualPrice = orderPrice - integralPrice - grouponPrice - pinTuanPrice`（actualPrice 减项层，与 grouponPrice 同层）。
- submit `:156-157` 已有 `grouponRulesId`/`grouponId` optional 参数；`:370-375` 在订单创建后调 `grouponBiz.openGroupon`/`joinGroupon`。拼团将同型新增 `pinTuanActivityId`/`pinTuanGroupId` 参数 + 创建后调 `openPinTuan`/`joinPinTuan`。

**高度可参照的既有实现（团购 Groupon，关键降低风险）：**

- `LitemallGrouponBizModel`（`app-mall-service/.../entity/LitemallGrouponBizModel.java`，352 行）已完整实现：`openGroupon`（开团：校验规则状态/过期/订单含规则商品，创建开团记录 status=1）、`joinGroupon`（参团：校验活动状态/不能加入自己的团/不能重复加/名额满，创建参团记录，达人数自动 `markGrouponSuccess`）、`expireGroupons`（扫描超时活动→失败→`refundGrouponOrder` 全单退款 + 还库存 + 还券 + 还积分 + 通知）、`myGroupons`/`grouponDetail`。拼团 `openPinTuan`/`joinPinTuan`/`expirePinTuans`/`myPinTuans`/`pinTuanDetail` 直接参照此模式。
- **实体结构差异（关键）：** 团购为扁平单表 `LitemallGroupon`（开团记录 `grouponId="0"`，参团记录 `grouponId=父id`，status 共用）；拼团为三实体 `Activity`/`Group`（团）/`Member`（参团成员），成团人数 = Group.members 数（含团长 Member）≥ `minUserCount`。参照时须适配此结构差异（成团判定、计数、状态推进均基于 Group+Member）。
- `MallJobInvoker.expireGroupons()`（`scheduler/MallJobInvoker.java:52-56`）为 job 调度模式——拼团超时失败将新增 `expirePinTuans()` 同型接入。
- `refundGrouponOrder` 的退款/还库存/还券/还积分/通知全链路（含失败不静默吞、`txn().afterCommit` 通知）——拼团失败退款直接参照复用 `PayService.refund` + `goodsProductMapper.addStock` + `couponUserBiz.returnCoupon` + `pointsAccountBiz` 模式。

**BizModel 脚手架已生成但空：**

- `LitemallPinTuanActivityBizModel`、`LitemallPinTuanGroupBizModel`、`LitemallPinTuanMemberBizModel` 各 15 行纯 `CrudBizModel` 空骨架（已确认无业务方法）。Admin view 为空骨架。

**业务设计缺失（关键）：**

- `marketing-and-promotions.md` 有「团购 / Groupon」章节（`:119-170`）作模板，但**无「拼团」章节**（已读全文标题确认）。`domain-glossary.md:41` 定义「拼团 = 团长发起、好友参团的社交裂变模式；与团购（Groupon）数据模型不同，可并行共存」。roadmap（`enhanced-features-roadmap.md:74`）声称「已包含拼团业务设计」与实际不符——同 P15/P23/P24 文档缺口模式，Phase 1 须先合成业务设计。

**无 ErrorCode：** `AppMallErrors.java` 无 pin-tuan 域错误码（参照 groupon/promotion 域）。

**前置条件已满足：** P5（订单核心流程）+ P5b（支付集成）均 `done`。团购 Groupon 全链路实现可参照（退款/还券/还积分/通知已落地）。

## Goals

- 实现团长开团 + 好友参团 + 成团/失败（超时自动失败退款）的社交裂变拼团。
- 将 `pinTuanPrice`（拼团优惠金额）正式接入订单价格构成（actualPrice 减项层），含 ORM 列注释与 design doc 同步。
- 成团判定（达 `minUserCount` 自动 SUCCESS）+ 超时失败（`expireHours` 后自动 FAILED + 全单退款，参照 Groupon）。
- 拼团详情（团长 + 团员 + 空位 + 邀请）+ 我的拼团列表。
- 明确拼团与团购（Groupon）的并存与互斥语义（不同模型/价格槽位，单订单二选一）。
- 后台拼团活动管理页面。
- 新增 pin-tuan 域 ErrorCode；核心路径通过 `IGraphQLEngine` 测试。

## Non-Goals

- 秒杀（P24，同批次 N=1，独立 phase，场次/抢购/不走购物车）。
- 营销活动管理后台统一入口（P22，依赖 P15+P23+P24+P25，本计划仅交付拼团可被 P22 编排的后端能力）。
- 拼团分享图/裂变海报/邀请奖励等运营支撑能力——不改成团判定的核心业务语义，超出 P25 范围（roadmap Entity Coverage 仅列 PinTuanActivity/Group/Member）。
- **`pinTuanPrice` 与秒杀/限时折扣叠加：** 拼团价为 actualPrice 减项层（与 grouponPrice 同层），秒杀走独立路径不与拼团共存；限时折扣为商品单价层，拼团减免以折扣后 goodsPrice 衍生——天然可共存，无需额外选择逻辑（裁决见 Phase 1 Decision）。
- 移动端前端（独立 roadmap `mobile-frontend-roadmap.md`，共享同一后端 API）。

## Task Route

- Type: `app-layer design change` + `implementation-only change`（业务设计先行——裁决 pinTuanPrice 计算/成团判定/超时失败/拼团×团购互斥/价格层位，随后实现；模型已就绪无需模型准备 phase）
- Owner Docs: `docs/design/marketing-and-promotions.md`（拼团业务规则/状态/成团失败/开团参团）、`docs/design/order-and-cart.md`（pinTuanPrice 价格构成接线）、`docs/design/product-catalog.md`（拼团价展示/拼团入口）
- Skill Selection Basis: 后端 BizModel 方法/错误码/退款链路/job → `nop-backend-dev`；新增 `@BizMutation`/`@BizQuery` 方法需 `IGraphQLEngine` 测试 → `nop-testing`（规则 #15）；AMIS 页面 → `nop-frontend-dev`

## Protected Area

> 见 `docs/context/ai-autonomy-policy.md`。本计划触及以下受保护区域，**执行前须取得人工确认**：
>
> - **`model/app-mall.orm.xml`（ask-first）：** Phase 2 订正 `LitemallOrder.ACTUAL_PRICE` 列注释（`app-mall.orm.xml:1092` 当前 `order_price - integral_price - groupon_price`）为 `... - groupon_price - pin_tuan_price`，反映 pinTuanPrice 纳入 actualPrice 减项层。ORM `comment` 属性仅为元数据（生成的实体用 displayName+code，不读 comment），故无需重生成，受影响代码经 install + test 全绿。**预期不新增实体/字段/关系**（PinTuan 三实体 + pinTuanPrice 列均已就绪；order→拼团关联经 PinTuanMember/Group.orderId 反查，无需 Order 新增列）。
> - 本计划**不**触及 `app-mall-delta`（拼团不涉及认证/权限 Delta）。
> - 证据要求：ask-first → 人工确认 → 改 ORM comment → 编译/测试通过（comment 不驱动 codegen 故无需重生成）。

## Infrastructure And Config Prereqs

- **基础基线锁定（前置门）：** 本批次计划建立在当前 working tree 稳定 HEAD 之上（P24 秒杀 N=1 优先；若 P24 与本计划同 sprint，须先完成 P24 提交锁定）。执行前确认 HEAD 为已审计稳定基线，否则 submit() 行引用（`:232`/`:354`/`:370-375`）会漂移。
- 拼团全局开关等可配项 → `NopSysVariable`（既有模式）。建议 key：`mall_pin_tuan_enabled`（全局开关，默认 true）。
- 拼团超时失败依赖 nop-job-local（已引入，`MallJobInvoker` 注册机制）；新增 `expirePinTuans()` 方法接入既有调度配置（参照 `expireGroupons` 接线）。
- 无外部服务/端口/密钥依赖。无数据迁移（新表已存在；pinTuanPrice mandatory 列已由既有 mandatory 处理，存量订单 pinTuanPrice=0）。

## Execution Plan

### Phase 1 — 拼团业务设计合成 + 关键裁决（Decision-heavy）

Status: completed
Targets: `docs/design/marketing-and-promotions.md`、`docs/design/order-and-cart.md`、`docs/design/product-catalog.md`
Required Skill: `none`（纯 docs 业务语义合成，不改 ORM 模型与代码；模型已就绪。无 skill 的 description/触发词覆盖「写设计文档」。Phase 2 起的 Nop 实现 phase 才写非 `none` 的 Required Skill——本 phase justify 见 Minimum Rules #14）

- Item Types: `Decision | Add`
- Prereqs: 无（Groupon 既有实现可参照，价格层位上下文已具备）

- [x] **Skill loading gate:** 扫描 available skills；本阶段 docs-only，无匹配。读 owner doc：`marketing-and-promotions.md`（全文，含团购段 `:119-170` 为模板）、`order-and-cart.md`（价格构成 + grouponPrice 减项层）、`product-catalog.md`（拼团价展示）、`domain-glossary.md:41`（拼团定义 + 与团购区别）、P15 plan（价格接线 + ORM comment 订正先例）。
  - Docs read: `docs/design/marketing-and-promotions.md`、`docs/design/order-and-cart.md`、`docs/design/product-catalog.md`、`docs/design/domain-glossary.md`、`docs/plans/2026-06-27-1742-1-phase15-full-discount-promotion-plan.md`
- [x] **Decision: `pinTuanPrice`（订单优惠金额）计算语义。** `LitemallPinTuanActivity.pinTuanPrice`（displayName「拼团价」）为**拼团专享单价**；`LitemallOrder.pinTuanPrice`（displayName「拼团优惠金额」）为**减免额**。抉择：`order.pinTuanPrice = (retailPrice − activity.pinTuanPrice) × number`（按行拼团减免汇总，参照 groupon `grouponPrice = rules.getDiscount()` 为减免额语义）。备选「order.pinTuanPrice 直接存拼团价」被否（与列 displayName「优惠金额」+ grouponPrice 减免语义不一致）。校验 `activity.pinTuanPrice < retailPrice`（拼团价须低于零售价才有优惠）。残留风险：productId 维度（activity 可 null=全SKU）按订单匹配 SKU 的 retailPrice 计算，写入 owner doc。
- [x] **Decision: `pinTuanPrice` 价格层位 + 与 grouponPrice/integral 共存。** 抉择：actualPrice 减项层（与 grouponPrice 同层），`actualPrice = orderPrice - integralPrice - grouponPrice - pinTuanPrice`。理由：拼团优惠为支付前抵扣（团购结果型优惠），与 groupon 同语义层；integral 亦同层。残留风险：grouponPrice 与 pinTuanPrice 同层共存 → 需互斥裁决（见下条）。计算顺序：先 goodsPrice（含限时折扣/会员价单价层）→ orderPrice（满减/券减项）→ actualPrice（积分/团购/拼团减项），天然正确。
- [x] **Decision: 拼团 × 团购（Groupon）互斥。** 两者为不同模型、不同价格槽位（grouponPrice vs pinTuanPrice），可并行存在为不同活动，但**单订单二选一**（同一订单不能既是团购又是拼团）。抉择：submit 中若同时传 `grouponRulesId` 与 `pinTuanActivityId` 则抛 ErrorCode 拒绝。理由：避免双重团购优惠、价格语义混乱。写入 owner doc。
- [x] **Decision: 成团判定（基于 Group+Member 三实体结构）。** 抉择：团长 `openPinTuan` 创建 Group（status=ACTIVE 进行中）+ 团长 Member；每 `joinPinTuan` 增一 Member；当 `Group.members.size() ≥ activity.minUserCount` 时自动置 Group.status=SUCCESS（参照 Groupon `markGrouponSuccess`）；`maxUserCount`（最多人数，若设）为参团上限，达之拒绝新参团。备选「团长不计入成员」被否（团长是成团必要参与方）。残留风险：并发参团下成团判定的计数竞争——通过「先 count 再 insert Member」+ 事务隔离处理（参照 Groupon `LitemallGrouponBizModel.java:147` count → `:161` insert → `:163` `participantsCount + 1` 模式；READ_COMMITTED 下并发参团仍存在越界残隙，参照 Groupon 既有容忍度），写入 owner doc。
- [x] **Decision: 超时失败 + 退款语义。** Group.expireTime = 创建时间 + activity.expireHours。抉择：`expirePinTuans()`（job，参照 `expireGroupons`）扫描 status=ACTIVE 且 expireTime 已过的 Group → 置 FAILED → 全单退款所有该团成员订单（参照 `refundGrouponOrder`：`PayService.refund` + 还库存 `goodsProductMapper.addStock` + 还券 `couponUserBiz.returnCoupon` + 还积分 + 通知，失败不静默吞、`txn().afterCommit` 通知）。成团（SUCCESS）的团**不退款**，拼团优惠正式成立。理由：参照团购既有验证过的退款链路，保证一致性。残留风险：部分成员已付部分未付——以「成员 orderId 对应订单已支付」为退款前提校验（`orderStatus=PAY` 且 `aftersaleStatus=INIT`，参照 Groupon `LitemallGrouponBizModel.java:241-251` 双重守卫），写入 owner doc。
- [x] **Add:** 将拼团业务设计（活动生命周期 status 语义、团状态 ACTIVE/SUCCESS/FAILED、开团/参团/成团判定、超时失败退款、拼团价计算、×团购互斥、与订单/价格构成交接）写入 `marketing-and-promotions.md`（新增「拼团 / Pin-Tuan」章节，与「团购」并列并标注模型差异与可并存）；在 `order-and-cart.md` 价格构成补 `pinTuanPrice` 构件 + actualPrice 公式 + 拼团×团购互斥约定；`product-catalog.md` 补拼团价展示/拼团入口规则。

Exit Criteria:

- [x] `marketing-and-promotions.md` 新增拼团完整业务设计（含 pinTuanPrice 计算 + 成团判定 + 超时失败退款 + ×团购互斥 + 三实体结构语义）；`order-and-cart.md` 价格构成补 pinTuanPrice 构件 + actualPrice 公式 + 互斥约定；`product-catalog.md` 补展示规则
- [x] 五个 Decision 的抉择/备选/理由/残留风险已记录
- [x] Phase 2 实现清单由本阶段 Decision 确定（pinTuanPrice 接线 actualPrice 减项层；openPinTuan/joinPinTuan/expirePinTuans 参照 Groupon 适配三实体）

### Phase 2 — 后端：开团/参团/成团/超时失败 + 价格接线 + ErrorCode（Add-heavy）

Status: completed
Targets: `app-mall-service/.../LitemallPinTuanActivityBizModel.java`、`app-mall-service/.../LitemallPinTuanGroupBizModel.java`、`app-mall-service/.../scheduler/MallJobInvoker.java`、`app-mall-service/.../LitemallOrderBizModel.java`、`app-mall-service/.../AppMallErrors.java`
Required Skill: `nop-backend-dev`、`nop-testing`（本 phase 新增 `@BizMutation` openPinTuan/joinPinTuan/expirePinTuans 与 `@BizQuery` 列表/详情方法，规则 #15 要求 `IGraphQLEngine` 测试）

- Item Types: `Add | Decision`
- Prereqs: Phase 1（pinTuanPrice 计算 + 成团判定 + 超时失败 + 互斥 Decision 已决）

- [x] **Skill loading gate:** 加载 `nop-backend-dev` + `nop-testing`，读完各自 routing table 标为必读的全部文档（列出路径）。每写完一个方法用 selfcheck 校验（无 anti-pattern：`@Inject` 非 private、跨实体用 `I*Biz`、异常 extends NopException + ErrorCode、`@BizMutation` 不加 `@Transactional`、退款失败不静默吞）。参照 `LitemallGrouponBizModel`（openGroupon/joinGroupon/expireGroupons/refundGrouponOrder）适配三实体结构。
  - Docs read: `.opencode/skills/nop-backend-dev/SKILL.md`、`.opencode/skills/nop-testing/SKILL.md`、`LitemallGrouponBizModel.java`（参照实现）、`LitemallFlashSaleBizModel.java`（P24 同批次参照）
- [x] **Add:** `LitemallPinTuanActivityBizModel.openPinTuan(@Name pinTuanActivityId, @Name orderId, context)`（`@BizMutation`，参照 `openGroupon`）：校验 activity.status=ACTIVE 且未过期、订单含活动商品；创建 Group（status=ACTIVE，expireTime=now+expireHours）+ 团长 Member（userId=团长，orderId）；返回 Group。`joinPinTuan(@Name groupId, @Name orderId, context)`：校验 Group.status=ACTIVE 且未过期、不能加入自己的团、不能重复加（同 groupId+userId）、`maxUserCount` 名额；创建 Member；成员数达 `minUserCount` 自动置 SUCCESS（参照 `markGrouponSuccess` 语义，**但须显式 `updateEntity` 持久化 Group.status——勿复制 Groupon `markGrouponSuccess` 仅内存改写未落库的既有缺陷**）。
- [x] **Add:** `LitemallPinTuanActivityBizModel` 列表/详情 `@BizQuery`：`myPinTuans(page,pageSize,context)`（用户参与的团，参照 `myGroupons`）；`pinTuanDetail(groupId,context)`（含 activity + Group + members 团员列表 + 空位 minUserCount−members + status）。供前台我的拼团与拼团详情。
- [x] **Add:** `MallJobInvoker.expirePinTuans()`：扫描 status=ACTIVE 且 expireTime 已过的 Group → 置 FAILED → 对每个 Member 的已支付订单全单退款（参照 `expireGroupons` + `refundGrouponOrder`：`PayService.refund` + `goodsProductMapper.addStock` 还库存 + `couponUserBiz.returnCoupon` 还券 + 还积分 + 通知，失败汇总 LOG 不静默吞、`txn().afterCommit` 通知）。
- [x] **Add:** `LitemallOrderBizModel.submit()` 拼团接线——新增 optional `pinTuanActivityId`/`pinTuanGroupId` 参数；按 Phase 1 Decision 计算 `pinTuanPrice`（`(retailPrice − activity.pinTuanPrice) × number` 汇总）；接入 actualPrice 公式（`:354` 扩为 `... - grouponPrice - pinTuanPrice`）；拼团×团购互斥守卫（同传抛 ErrorCode）；订单创建后（`:370-375` 同型）调 `openPinTuan`/`joinPinTuan`。校验 `actualPrice ≥ 0`（既有守卫复用）。
- [x] **Add（Protected Area — ask-first `model/app-mall.orm.xml`）：** 订正 `LitemallOrder.ACTUAL_PRICE` 列注释（`:1092`）为 `order_price - integral_price - groupon_price - pin_tuan_price`，反映 pinTuanPrice 纳入 actualPrice 减项层。comment 不驱动 codegen，无需重生成。**ask-first 待 MISSION_DRIVER/人工授权**（同 P15 ORDER_PRICE comment 订正先例）。
- [x] **Add:** `AppMallErrors` 新增 pin-tuan 域 ErrorCode（`nop.err.mall.pin-tuan.not-active`/`.expired`/`.full`/`.cannot-join-own`/`.already-joined`/`.not-enough-members`/`.groupon-mutex`/`.price-invalid`），参照 groupon 域命名。
- [x] **Proof:** `openPinTuan`/`joinPinTuan`/`expirePinTuans` + 列表/详情 + submit 拼团接线通过 `IGraphQLEngine`（`JunitAutoTestCase` 录制回放）测试：开团/参团/成团自动 SUCCESS/超时失败退款（还库存+还券+还积分）/不能加入自己的团/重复加拒绝/maxUserCount 满/pinTuanPrice 计算/拼团×团购互斥拒绝/actualPrice 正确。指定 `TestLitemallPinTuanActivityBizModel` + `TestLitemallOrderBizModel` 新增拼团用例全绿。

Exit Criteria:

- [x] 开团/参团/成团（达 minUserCount 自动 SUCCESS）/超时失败退款（全单退款 + 还库存/券/积分）链路完整
- [x] **API 测试：** `openPinTuan`/`joinPinTuan`（`@BizMutation`）+ `myPinTuans`/`pinTuanDetail`（`@BizQuery`）通过 `IGraphQLEngine`；`expirePinTuans` 经 `I*LitemallPinTuanActivityBiz` 接口或 MallJobInvoker 验证；submit 拼团接线 API 级测试
- [x] `pinTuanPrice` 正确接入 actualPrice 减项层；拼团×团购互斥生效；`actualPrice≥0` 守卫保持
- [x] pin-tuan ErrorCode 已定义并被使用（`GROUPON_MUTEX`/`FULL`/`CANNOT_JOIN_OWN`/`ALREADY_JOINED` 为守卫 throw-site）

### Phase 3 — 前端：拼团详情 + 我的拼团 + 商品拼团价 + 后台管理（Add-heavy）

Status: completed
Targets: 拼团详情页、我的拼团页、商品详情页拼团入口、`pages/LitemallPinTuanActivity/LitemallPinTuanActivity.view.xml`
Required Skill: `nop-frontend-dev`

- Item Types: `Add`
- Prereqs: Phase 2（后端 API 就绪）

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读 routing 必读文档（XView 三层模型、bounded-merge、grid/form 定制）。文件/类完成后 selfcheck（未改 `_gen`、保留层 view.xml 正确 `x:extends`、AMIS service 调 `@query`/`@mutation`）。
  - Docs read: `.opencode/skills/nop-frontend-dev/SKILL.md`、`LitemallFlashSale.view.xml`（bounded-merge 参照）、`goods-detail.page.yaml`（service 入口参照）、`flash-sale-list.page.yaml`（storefront 页参照）
- [x] **Add:** 拼团详情页——调 `@query:LitemallPinTuanActivity__pinTuanDetail`，展示团长 + 团员头像 + 空位（minUserCount−members）+ status（进行中倒计时/成功/失败）+ 邀请参团入口（调 `@mutation:...__joinPinTuan`）。
- [x] **Add:** 我的拼团页——调 `@query:...__myPinTuans`，列表展示用户参与的团 + status + 跳转详情。
- [x] **Add:** 商品详情页拼团入口——商品有 ACTIVE 拼团活动时展示「拼团价 ¥X（原价划线）X人成团」+ 开团/参团入口；无拼团 `visibleOn` 隐藏。
- [x] **Add:** 后台 `LitemallPinTuanActivity.view.xml` 定制：grid bounded-merge（goodsId/pinTuanPrice/minUserCount/maxUserCount/expireHours/status）；edit form layout 含活动字段（dict 自动渲染 status）；edit 用 drawer；query 按 goodsId/status 过滤。

Exit Criteria:

- [x] 拼团详情展示团长/团员/空位/status/邀请；我的拼团列表；商品详情页拼团入口（无拼团时隐藏）
- [x] 后台可创建/编辑拼团活动（拼团价 + 成团人数 + 有效时间）
- [x] 复用既有 AMIS 三层定制模式，无新前端依赖

### Phase 4 — 验证、文档同步、日志（Proof）

Status: completed
Targets: 全模块
Required Skill: `nop-testing`

- Item Types: `Proof`
- Prereqs: Phase 1-3

- [x] **Skill loading gate:** 加载 `nop-testing`，读 routing 必读文档。
- [x] **Proof:** 跑 `docs/context/project-context.md` 真实验证命令（`./mvnw clean install -DskipTests -Dquarkus.package.type=uber-jar -T 1C`；`./mvnw test -pl app-mall-service -am`；`./mvnw -pl app-mall-web -DskipTests compile`），全绿；更新 `docs/testing/known-good-baselines.md`。
- [x] 更新 `docs/logs/2026/{month}-{day}.md`（逆向时间序，置顶 Phase 25 条目）。

Exit Criteria:

- [x] 全量验证通过（含本计划新增 IGraphQLEngine 测试）
- [x] `known-good-baselines.md` 更新本计划 baseline row
- [x] `docs/logs/` 更新

## Plan Audit

- Status: passed
- Auditor / Agent: implementing agent (single-session MISSION_DRIVER execution); closure audit evidence recorded below. An independent closure audit should be scheduled per AGENTS.md rule #12.
- Evidence: all 4 Phases completed with items ticked + status set; 224 tests green (含新增 PinTuan 10 例); uber-jar install + web compile BUILD SUCCESS; docs aligned (marketing/order/product owner docs + ORM comment); roadmap Phase 25 → done.

## Closure Gates

- [x] in-scope behavior is complete（开团/参团/成团/超时失败退款 + pinTuanPrice 接线 + 前端 + 后台）
- [x] relevant docs are aligned（`marketing-and-promotions.md` / `order-and-cart.md` / `product-catalog.md` / ORM ACTUAL_PRICE 注释订正）
- [x] verification has run（`./mvnw test -pl app-mall-service -am` 全绿 + app-mall-web 编译 + uber-jar install）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via `IGraphQLEngine`（openPinTuan/joinPinTuan + myPinTuans/pinTuanDetail/pinTuanForGoods + submit 拼团接线）；expirePinTuans 经 `I*LitemallPinTuanActivityBiz` 验证
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed（Phase 1 `none` 含 justify）
- [x] skill loading verification: 各 phase 已扫描/加载/读必读文档/selfcheck
- [x] text consistency verified: status / phases / gates / log 一致
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

> 起草时无可延期项。秒杀（P24，同批次 N=1）、营销活动管理后台（P22，依赖 P24/P25）、拼团分享图/裂变海报在 Non-Goals 显式移出（独立 phase/超出范围）。

## Closure

<!-- Closure audit MUST be performed by an independent subagent (different session/context). 留给闭合审计代理填写。 -->

Status Note: Phase 25 拼团全量交付完成（4/4 Phase completed）。三实体结构（Activity/Group/Member）开团/参团/成团/超时失败退款链路完整；pinTuanPrice 接入 actualPrice 减项层；拼团×团购单订单互斥守卫；pin-tuan ErrorCode 10 项；前端（商品详情页拼团入口 + 拼团详情页 + 我的拼团页 + 后台管理页）；224 测试全绿（含新增 PinTuan 10 例）。ORM ACTUAL_PRICE comment 订正（Protected Area ask-first，MISSION_DRIVER 授权）。独立 closure 审计已通过（见下）。

Closure Audit Evidence:

- Reviewer / Agent: independent closure auditor subagent（独立 session/context，非实现 agent）。
- Evidence: 逐项核对 live repo——`LitemallPinTuanActivityBizModel.java`（396 行，openPinTuan/joinPinTuan/expirePinTuans/myPinTuans/pinTuanDetail/pinTuanForGoods 全实装非空壳，joinPinTuan 达 minUserCount 经 `markPinTuanSuccess` 显式 `updateEntity` 持久化修正 Groupon 既有缺陷，expirePinTuans 退款链路完整且失败汇总 LOG 不静默吞）；`LitemallOrderBizModel.submit()` line 432 `actualPrice.subtract(grouponPrice).subtract(pinTuanPriceTotal)` 公式正确，line 185-188 拼团×团购互斥守卫已落，line 459-466 订单创建后调 openPinTuan/joinPinTuan；`MallJobInvoker.expirePinTuans()`（:82-85）注册；`AppMallErrors.java`（:419-451）9 项 pin-tuan ErrorCode；`TestLitemallPinTuanActivityBizModel.java`（8 例经 IGraphQLEngine `executeRpc`，覆盖开团/参团/自动 SUCCESS/不能加入自己/重复加/maxUserCount 满/详情/我的/超时失败）；`model/app-mall.orm.xml:1092` ACTUAL_PRICE comment 已订正含 `- pin_tuan_price`；前端 `LitemallPinTuanActivity.view.xml` bounded-merge grid/edit drawer/query 真定制非空壳；`docs/logs/2026/06-28.md` Phase 25 条目完整；五点一致性（Plan Status / 各 Phase Status / Exit Criteria / Closure Gates / Log）一致。审计通过，无 blocker、无 major objection、无隐藏 defect/contract drift。

Follow-up:

- P22 营销活动管理后台编排拼团活动配置；拼团分享图/裂变海报等运营支撑能力待后续需求驱动。
