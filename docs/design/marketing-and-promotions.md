# 营销与促销业务设计

## 目的

说明优惠券、团购、内容营销和互动能力的业务设计。

## 边界

- 本文档负责营销相关界面的业务语义和功能规则。
- 持久化模型结构、字段集和字典定义以 `model/app-mall.orm.xml` 为准。
- 技术实现策略属于 `docs/architecture/`。

## 优惠券体系

### 业务意图

- 优惠券提供基于规则的优惠能力，可被领取、发放或核销。
- 优惠券是否可用可能取决于最低消费金额、商品适用范围和有效期。

### 业务规则

- 每张优惠券实例只有一个业务含义，且每次发放实例只能使用一次。
- 优惠券的领取和使用次数可以按用户限制。
- 优惠券有效期可以相对领取时间计算，也可以固定为某个日期区间。
- 优惠券适用范围可以覆盖全场商品、指定分类或指定商品。
- 对于满足条件后又被取消或退款的订单，应按既定订单策略恢复优惠券可用性。

### 券种与资格

- 优惠券至少区分通用领取券、注册赠券和兑换码券三类业务来源。
- 通用领取券面向主动领券场景，注册赠券面向用户注册后的自动发放，兑换码券面向显式兑换场景。
- 同一券规则的领券资格、限领次数和有效期口径应保持一致，不能因入口不同而改变同一券的业务含义。
- 用户是否可领取某张券，取决于券状态、剩余库存、用户限领次数以及该券当前是否允许发放。

### 使用与失效

- 只有满足金额门槛、商品范围和有效期条件的优惠券才能在结算时生效。
- 优惠券验证通过后，影响的是订单优惠金额，不改变商品原价语义。
- 优惠券一旦核销，应从“可使用”转为“已使用”语义；订单取消或符合恢复条件的退款后，可按既定规则回到可用语义。
- 已下架、已过期或不满足商品范围条件的优惠券，不得在结算时继续生效。

### 管理员与用户动作

- 管理员可以创建、调整、上下架优惠券规则，并控制发放方式和适用范围。
- 商城用户可以查看可领取优惠券、主动领券、兑换兑换码券，并在结算时选择符合条件的优惠券。
- 用户看到的“可用优惠券”应以当前订单上下文重新校验，而不是只基于历史领券结果静态展示。

### 生命周期

- 管理员创建优惠券规则。
- 用户通过主动领取、注册赠送或兑换流程获得优惠券。
- 用户在结算时使用符合条件的优惠券。
- 已使用、已过期或已下架的优惠券状态会失去继续使用资格。

### 与订单的关系

- 优惠券资格和发放逻辑由本文件负责。
- 优惠券最终如何进入订单价格构成、如何与退款/取消联动恢复，由 `order-and-cart.md` 负责引用结果，不在本文件重复维护订单价格公式。

### 领券入口展示规则（P32）

业务意图：用户进入商品详情页时，应能看到当前商品可领取的优惠券，减少跳转、提升领券转化。

**入口分布（Decision A：详情页展示「本商品可用券」列表）：**

- **领券中心页**（`coupon-center.page.yaml`）：聚合所有公开券与兑换码入口，承担「全量券浏览」职能。
- **商品详情页领券入口**（`goods-detail.page.yaml`）：按 `goodsType`/`goodsValue` 商品范围过滤，只展示「本商品可用」的券 + 当前用户已领/可领状态 + 一键领取按钮。
- **抉择 A**（详情页直接展示可用券列表）vs **备选 B**（仅展示「有券可领」徽标 + 跳转领券中心）。**A 胜**——减少跳转、提升转化。**残留风险：** 商品详情页增加一次查询调用；若券量大需在 API 侧分页/限数（当前基线按 pageSize 限数）。

**`listCouponsForGoods(goodsId)` 鉴权与状态模型（Decision A）：**

- API 鉴权：`@Auth(publicAccess=true)`（与 `listAvailableCoupons` 一致），匿名用户可查看商品可用券。
- **`claimedByMe` 标记在匿名上下文（userId=null）下恒为 false**（未登录视为未领），登录后才查 `LitemallCouponUser`。
- **`claimable` 标记**受 `limit`（用户限领）约束：登录用户按已领数与 `limit` 推算（精确）；匿名用户按总量推算（非用户级精确），仅作展示，实际领取时以 `claimCoupon` 的强校验为准。
- **抉择 A**（一次查询返回 `claimedByMe` + `claimable`）vs **备选 B**（前端二次查询「我的券」）。**A 胜**——一次查询性能优。**残留风险：** 匿名→登录切换时已领状态需前端刷新；`claimable` 在匿名下非精确。

**商品范围过滤语义（与兑换侧自洽）：**

- `goodsType=0`（ALL）：本商品始终命中。
- `goodsType=1`（CATEGORY）：经 `ILitemallGoodsBiz` 取商品 `categoryId`，与 `goodsValue`（逗号分隔的分类 ID 列表）匹配。
- `goodsType=2`（GOODS）：本商品 `id` 在 `goodsValue`（逗号分隔的商品 ID 列表）中。
- 公开券（`type=0`）、上架券（`status=0`）、未过期（`endTime` 为空或晚于当前）才进入候选池。

### DIY 投放配置语义（P32）

业务意图：运营在后台完成「指定商品范围 + 限领 + 有效期」的优惠券投放配置，无需开发介入。

**配置维度：**

| 字段 | 字典/类型 | 配置语义 |
| ---- | --------- | -------- |
| `goodsType` | `mall/coupon-goods-type`（0=ALL/1=CATEGORY/2=GOODS） | 商品范围类型 |
| `goodsValue` | 字符串（逗号分隔 ID 列表） | 商品范围值：`goodsType=1` 时为分类 ID 列表；`goodsType=2` 时为商品 ID 列表；`goodsType=0` 时忽略 |
| `limit` | int（0=不限，默认 1） | 用户领券限制数量 |
| `tag` | 字符串（如「新人专用」） | 人群标签，**纯展示**，不参与会员级路由（当前无会员级范围机制，会员专属券自动发放属 P26 successor） |
| `type` | `mall/coupon-type`（0=COMMON/1=REGISTER/2=EXCHANGE） | 优惠券赠送类型 |
| `timeType` | `mall/coupon-time-type`（0=DAYS/1=RANGE） | 有效期类型 |
| `days` | int | 当 `timeType=0` 时生效：领后 N 天有效 |
| `startTime` / `endTime` | datetime | 当 `timeType=1` 时生效：固定时间段 |

**抉择与残留风险（Decision A：固化既有字典语义）：**

- **抉择**：本计划固化 `goodsType`/`limit`/`tag`/`timeType` 的运营配置语义，写入字典与后台表单。**备选「引入会员级（member-level）范围机制」被否**——超出 P32 范围（属 P26 successor），且需新增模型字段。
- **残留风险 1**：`tag` 当前为纯展示，不参与会员级路由；会员专属券仍需 P26 successor 自动发放能力。
- **残留风险 2**：DIY 投放仅支持商品/分类维度（无品牌、无人群标签路由），更复杂的人群定向投放不在当前基线。
- **残留风险 3**：`limit=0`（不限领）与 `total=0`（无限量）叠加时存在被刷风险，由运营在配置时合理设置上限控制（非系统强约束）。

### 分类范围券兑换自洽（P32 修复）

**修复前缺陷：** `LitemallCouponUserBizModel.selectCouponForOrder` 以 `goodsType != 0` 作「是否限范围」标志，未区分 CATEGORY(1) vs GOODS(2)；`parseGoodsValue` 只做逗号分割成 ID 列表，随后将商品 `goodsId` 与该列表逐项比较。对 `goodsType=1`，`goodsValue` 存的是**分类 ID**，却被当**商品 ID** 比较 → 永不命中 → 抛 `ERR_COUPON_GOODS_NOT_MATCH`。

**修复后语义：**

- `goodsType=0`（ALL）：不校验商品范围。
- `goodsType=1`（CATEGORY）：经 `ILitemallGoodsBiz` 取订单各商品的 `categoryId` 集合，与 `goodsValue` 分类 ID 列表取交集；存在交集即命中。
- `goodsType=2`（GOODS）：保持既有逻辑，订单每个 `goodsId` 都必须在 `goodsValue` 商品 ID 列表中。
- 展示侧（`listCouponsForGoods`）与兑换侧（`selectCouponForOrder`）使用同一套 CATEGORY 匹配逻辑，确保用户在详情页看到的可用券，在结算时不会被错误拒绝。

## 团购 / Groupon

### 业务意图

- 团购用于鼓励多个用户共同购买同一商品。
- 由一名用户开团，其他用户在有效期内参团。

### 业务规则

- 团购规则绑定到具体商品。
- 团购是否成功取决于截止时间前是否达到规定参团人数。
- 用户不能以跟团身份加入自己发起的团。
- 用户不能重复加入同一个有效团。
- 团购优惠只有在团购业务条件成立时才影响订单价格。

### 规则状态与活动状态

- 团购规则有独立于具体参团记录的规则状态，用于表达是否正常上线、到期失效或被管理员下线。
- 团购活动有独立于规则状态的业务状态。持久化状态已通过 `mall/groupon-status` 字典声明为四种：开团未支付、开团中、开团失败、开团成功（对应 `LitemallGroupon.status` 的 0/1/2/3，`ext:dict="mall/groupon-status"`）。其中 `3=开团成功` 由后端在参团人数达标时自动置位，并在前台四态展示中渲染。
- 团购规则是否可参与，取决于规则状态、过期时间和商品绑定关系。
- 某次团购活动是否还能继续参团，取决于活动状态、剩余名额、过期时间和用户资格。

### 开团与参团语义

- 开团是用户基于某条团购规则发起的一次新的团购活动。
- 参团是其他用户加入某次已存在的团购活动，而不是重新创建团购规则。
- 开团用户在支付成功后进入“开团中”语义，后续参团用户在满足资格时加入该活动。
- 团购成功与否由有效支付参与者数量决定，而不是只由下单意图决定。

### 结果与失效

- 团购成功后，团购活动进入开团成功状态，团购优惠正式成立，并由订单侧价格语义消费该结果。
- 团购失败或超时后，该次活动失去继续参团资格，但不自动改变团购规则本身是否仍可供后续新活动使用。
- 团购分享图、开团入口等属于团购活动的运营支撑能力，不改变团购成功判定的核心业务语义。

### 管理员与用户动作

- 管理员可以创建、上下架和维护团购规则。
- 商城用户可以浏览有效团购规则、发起开团、参与他人团购并查看团购结果。
- 系统可以在支付成功、活动过期等关键节点自动检查团购状态并推进业务结果。

### 生命周期

- 管理员定义团购规则。
- 用户通过下单发起团购。
- 其他用户在过期前参团。
- 当足够数量的有效参与者完成支付时，团购成功；否则超时结束。

### 与订单的关系

- 团购资格、规则状态和活动状态由本文件负责。
- 团购成功后如何作用于订单价格、支付和后续订单结果，由 `order-and-cart.md` 负责引用团购结果，不在本文件重复维护订单主流程。

## 积分体系 / Points System

### 业务意图

- 积分是商城用户的一种虚拟资产，通过指定行为获取，可在结算时抵扣订单金额或（后续）在积分商城兑换商品/服务。
- 积分账户与流水的持久化语义、并发安全、changeType × sourceType 分类法由 `wallet-and-assets.md` 负责；本文件负责**获取规则、抵扣规则与上限、与商城兑换的交接语义**。

### 获取规则 / Earn Rules

- **购物赠送**（主触发源，已落地）：用户确认收货（`LitemallOrderBizModel.confirm`）时，系统按 `mall_points_earn_per_yuan`（每元赠 X 积分，默认 1）× 订单 `actualPrice` 计算应赠积分并调用账户 earn API。**备选「支付即赠送」被否**——退款刷分风险（确认收货前退款的订单不发积分，已由 P16 item 级退款守卫覆盖）。
- 赠送为幂等：同一 `orderId` 不重复赠送，按 `sourceType=order-confirm-earn` + `sourceId=orderId` 查重。
- 签到得积分（P28，已落地，见本章「签到 / Daily Check-In」）、评价得积分（P33，已落地，见 `docs/design/product-catalog.md` 结构化评价章节「评价得积分联动」段）、分享得积分（不在基线）作为接入源复用同一账户 earn API。
  - **评价得积分交接确认：** 评价提交（`LitemallCommentBizModel.submitComment`）成功后，按 `mall_points_comment_reward` 配置（默认 `0` 关闭）发放固定积分。复用 P27 `earnPoints`，`sourceType="comment-reward"`、`sourceId=comment.id`，幂等继承 `(sourceType, sourceId)` 查重。

### 获取规则配置项

获取规则为全局比例型，通过 `NopSysVariable`（`LitemallSystem` keyName/keyValue）配置，**不新增积分规则实体**：

| 配置项 key | 含义 | 默认值 |
| ---------- | ---- | ------ |
| `mall_points_earn_per_yuan` | 购物赠送：每实付 1 元赠 N 积分 | 1 |
| `mall_points_comment_reward` | 评价奖励：每次成功评价固定发放 N 积分（0=关闭） | 0 |
| `mall_points_to_yuan_ratio` | 兑换比例：X 积分 = ¥1（结算抵扣换算） | 100 |
| `mall_points_deduct_max_ratio` | 抵扣上限：占 `orderPrice` 的最大比例 | 0.3 |

签到规则（P28）已有独立的 CheckInRule 实体按天配置，不与本全局配置冲突。

### 抵扣规则 / Deduct Rules

- 用户在结算页勾选使用 N 积分 → `integralPrice = N / mall_points_to_yuan_ratio`（兑换比例换算为金额）。
- **抵扣上限：** `min(用户可用积分对应金额, orderPrice × mall_points_deduct_max_ratio)`。理由：兑换比例 + 比例上限为电商惯例，防全额积分抵扣侵蚀营收；`orderPrice` 为基准（与 coupon/promotion 同层基准）。**备选「固定金额上限」被否**——不随订单规模伸缩。
- 抵扣作用于 `actualPrice` 减项层（见 `order-and-cart.md` 价格构成公式），与满减 `promotionPrice`（`orderPrice` 减项层）、券 `couponPrice`（`orderPrice` 减项层）、团购 `grouponPrice`（`actualPrice` 减项层）的层位关系由 `order-and-cart.md` 维护。
- 抵扣扣减用户积分：调用账户 spend API（`changeType=SPEND`, `sourceType=order-deduct`, `sourceId=orderId`）。

### 取消 / 退款返还语义

与 P16 售后 Decision 对称（订单级才返还）：

- **积分抵扣返还：** 订单取消或整单退款时，返还用户已扣的抵扣积分（调用 earn API，`changeType=EARN`, `sourceType=refund-return`, `sourceId=orderId`）。item 级部分退款**不返还抵扣积分**（积分抵扣为订单级构件，同券恢复 Decision）。
- **购物赠送积分不追回：** 若订单已确认收货（已赠送积分）后发生售后退款，**不追回赠送积分**（简化语义，残留风险记录于计划 Deferred）。理由：避免 item 级碎片化与跨账户追回的复杂度。

### 积分商城兑换（交接）

- **本计划不实现积分商城兑换**（纯积分或积分+现金）——需积分商品目录（积分商品实体/兑换价）未建模，属独立子特性。本计划建立积分账户/流水/抵扣/获取 API 基座，积分商城兑换作为 successor（触发条件：积分商品目录建模需求出现）。
- 未来积分商城兑换将复用账户 spend API（`changeType=SPEND`, `sourceType=mall-exchange`, `sourceId=exchangeOrderId`）。

### 积分有效期（交接）

- 积分有效期与自动过期属 model-gap（模型无有效期字段/过期批次，且批量过期需 nop-job-local 定时编排），账户/流水语义由 `wallet-and-assets.md` 持有，本文件不展开。

### 管理员动作

- 管理员可通过后台对用户积分账户手工调账（加/扣），调用 `adjustPoints`（`changeType=EARN` 或 `SPEND`, `sourceType=admin-adjust`），并产生对应流水。
- 管理员可查询用户积分账户与流水。

### 与订单的关系

- 积分获取资格、抵扣规则、上限配置、返还语义由本文件负责。
- 积分如何接入订单价格构成（`integralPrice` 作用于 `actualPrice` 减项层）由 `order-and-cart.md` 价格语义负责引用结果，不在本文件重复维护订单价格公式。

## 签到 / Daily Check-In

### 业务意图

- 签到是商城日活运营工具：用户每日主动签到，按规则表（`LitemallCheckInRule`）获得对应档位积分，连续签到天数越长奖励越高，用于提升用户黏性与积分发放频次。
- 签到积分联动积分账户（P27 `earnPoints`，`sourceType=check-in`），不重复造积分写入逻辑。

### 业务规则

- 用户每日最多签到一次（以 `checkInDate` 当日为粒度）。
- 签到按规则表档位匹配发放积分，写入 `LitemallCheckInRecord` 并联动积分账户写一条 `EARN` 流水。
- 签到规则由运营在后台维护（`daySeq`/`pointReward`/`resetCycle`），可随时增删档位。
- 签到记录与积分流水一一对应（`sourceId=CheckInRecord.id`），幂等性继承积分账户的 `(sourceType, sourceId)` 查重。

### 连续天数算法（Decision A：即时计算）

**抉择：** 签到即时计算 `consecutiveDays`，无需定时任务扫描。

- 算法：查该用户**昨日** `CheckInRecord`，存在则 `consecutiveDays = prev.consecutiveDays + 1`；不存在（断签/首签）则 `consecutiveDays = 1`。
- 备选 B（定时任务每日扫描）被否——即时计算无延迟、无额外调度负担、与签到原子事务一致；定时扫描反而引入跨事务一致性问题。
- 该抉择消解了 roadmap 中「签到周期重置需引入 nop-job」的陈述——Phase 11 已引入 `nop-job-local`，但本特性按 Decision A 不依赖它。

### 周期重置语义（Decision A：循环发奖）

**抉择：** `resetCycle` 语义为「循环发奖」。

- `resetCycle=0`：不循环，按 `daySeq` 线性发奖，达到规则表最大 `daySeq` 后保持顶档。
- `resetCycle>0`：达到 `resetCycle` 天后下一次签到 `consecutiveDays` 归 1 循环（即 `consecutiveDays = ((prev + 1 - 1) % resetCycle) + 1`，当 `prev + 1 > resetCycle` 时归 1）。
- 备选 B（周期满当天发顶档后自然停）被否——循环发奖更符合签到运营意图（持续激励日活），自然停会使用户在满周期后失去签到动机。
- 残留风险：循环重置使用户永远拿低档，需运营结合 `resetCycle` 与档位梯度合理配置；属运营决策非缺陷。

### 防同日重复签到（Decision A：应用层查）

**抉择：** 应用层查 `(userId, checkInDate=今日)`，存在则拒绝（`ERR_CHECK_IN_ALREADY_TODAY`）。

- 备选 B（DB 唯一键 `(userId, checkInDate)`）作为 successor（model-gap，触发条件见对应计划 Deferred）。理由：模型当前无该唯一键，应用层查询足以保证正确性，签到非高并发路径；DB 唯一键为强一致兜底，非阻塞。

### 奖励档位匹配（Decision B：阶梯累进）

**抉择：** 取 `≤consecutiveDays` 的最大 `daySeq` 档的 `pointReward`。

- 例：规则表有 `daySeq=1→5`、`daySeq=3→15`、`daySeq=7→50`。用户 `consecutiveDays=5` 时命中 `daySeq=3` 档（15 积分）；`consecutiveDays=7` 时命中 `daySeq=7` 档（50 积分）。
- 备选 A（精确匹配 `daySeq`）被否——阶梯累进符合运营习惯（连续签到奖励递增），断签后从低档重启；精确匹配会导致无对应 `daySeq` 时无奖励。
- 无规则兜底（分两档）：
  - **规则表完全为空**（管理员未配置任何档位）：签到拒绝（`ERR_CHECK_IN_RULE_MISSING`）——属系统未配置的 fail-fast，提示运营补配规则，避免所有用户签到均得 0 积分的静默异常。
  - **规则存在但无 `≤consecutiveDays` 的档位**（如规则从 `daySeq=3` 起，用户 `consecutiveDays=1`）：签到成功但不发积分（`pointReward=0`），记录仍写入（保留签到行为轨迹，次日达档即发奖）。

### 积分联动（Decision：复用 P27 earnPoints）

- 签到写入 `CheckInRecord` 后调用 `pointsAccountBiz.earnPoints(userId, pointReward, changeType=EARN, sourceType="check-in", sourceId=record.id, remark)`。
- `sourceType` 复用 P27 已部署常量 `SOURCE_TYPE_CHECK_IN = "check-in"`（`LitemallPointsAccountBizModel.java:39`）。
- 幂等性继承 P27 `(sourceType, sourceId)` 查重（`LitemallPointsAccountBizModel.java:88-94`）：同一 `CheckInRecord.id` 不会重复发积分。与 P27 PointsFlow `(sourceType, sourceId)` DB 唯一键 deferred 一致（同一 model-gap）。
- `pointReward=0` 时不调 `earnPoints`（避免无意义流水），签到记录仍写入。

### 状态语义

- `LitemallCheckInRecord` 为追加型记录，无状态机（签到即终态）。
- `LitemallCheckInRule` 为配置表，无状态字段（运营增删档位即时生效）。

### 管理员与用户动作

- 商城用户：在个人中心查看签到状态（今日是否已签、连续天数、累计天数、未来奖励预览），点击「立即签到」领取当日积分。
- 管理员：在后台维护签到规则（`daySeq`/`pointReward`/`resetCycle`），无审批流。

### 生命周期

- 管理员配置签到规则档位。
- 用户每日进入个人中心签到入口，查看状态并签到。
- 系统即时计算连续天数、匹配档位、写记录、发积分。
- 用户次日再签，连续天数 +1（断签则归 1）；周期满按 `resetCycle` 循环。

### 与积分账户的关系

- 签到仅为积分**获取触发源**之一，账户/流水/抵扣语义由 `wallet-and-assets.md` 持有。
- 签到写入的 PointsFlow `changeType=EARN`、`sourceType=check-in`、`sourceId=CheckInRecord.id`，与购物赠送、退款返还等同属积分账户分类法（见 `wallet-and-assets.md` changeType × sourceType 表）。

### 已知约束

- 防重签 DB 唯一键为 model-gap（见上 Decision A 与对应计划 Deferred）。
- 签到补签/请假超出 P28 范围。
- 签到提醒推送（站内信/消息中心属 P35），本特性仅发积分。

## 满减送 / Full-Discount Promotion

### 业务意图

- 满减提供基于订单金额门槛自动触发的多档优惠能力（如「满 200 减 30」「满 500 打 9 折」）。
- 与优惠券不同，满减由系统在结算时根据当前订单金额与活动规则自动判定并应用，无需用户领取或选择。
- 满减是订单提交时自动触发的非逆转优惠，不占用用户额度、不产生可恢复的实例。

### 业务规则

- 一个满减活动可配置多个档位（tier），每个档位定义一个满足金额门槛 `meetAmount` 与对应的减免值 `discountValue`。
- 减免方式由活动 `discountType` 决定：减金额（0，`discountValue` 为减免额）或打折（10，`discountValue` 为折扣率，如 `0.9` 表示 9 折）。
- 满减门槛以订单商品金额（`goodsPrice`）为判定基准；同一订单只命中并应用一个档位（最优档位），不累加多档。
- 商品范围由活动 `goodsScope` 决定：全商品（0，始终可参与）、指定分类（10，订单含该分类下商品即可参与）、指定商品（20，订单含指定商品即可参与）。范围值存放于 `goodsScopeValue`（JSON 数组：分类 ID 或商品 ID）。
- 满减与优惠券默认可叠加（满减与优惠券同时生效）；是否允许叠加由全局配置控制（见下「叠加策略」）。

### 多档自动最优策略

- 满减为系统自动触发，区别于优惠券的用户主动选择。
- 系统自动选取满足门槛的、优惠额最大化的档位（最优档位）。
- 若订单金额未达到任何档位门槛，则该活动不产生优惠（promotion 为 0）。

### 叠加策略

- 默认允许满减与优惠券叠加（两者同时作用于订单价格）。
- 是否允许叠加由全局配置项 `mall_promotion_coupon_stacking` 控制（值为 `true`/`1` 表示允许叠加，默认允许）。
- 当配置为禁止叠加时，若订单同时命中满减与使用了优惠券，则满减优先生效、优惠券选择被拒绝（满减为自动机制，优先级高于用户选择）。

### 活动状态语义

满减活动状态由 `mall/promotion-status` 字典定义：

| 业务状态 | 状态码 | 含义 |
| -------- | ------ | ---- |
| 草稿 | 0 | 活动已创建但未上架，结算时不参与匹配 |
| 进行中 | 10 | 活动已上架且在有效时间窗内，结算时参与匹配 |
| 已结束 | 20 | 活动到达结束时间或被运营置为结束，不再参与匹配 |
| 已关闭 | 30 | 活动被强制下线，不再参与匹配 |

- 只有状态为「进行中」(10) 且当前时间在 `startTime`/`endTime` 时间窗内的活动才会在结算时被匹配。
- 活动优先级由 `priority` 字段决定；当多个活动同时命中时，取优先级最高（数值大者）的活动生效。

### 管理员与用户动作

- 管理员可以创建、编辑、上下架满减活动，配置多档规则与商品范围。
- 满减对用户透明且自动生效：用户无需领取、选择或操作；结算页展示命中活动的优惠分项与明细。

### 生命周期

- 管理员创建满减活动（草稿），配置档位与商品范围。
- 管理员将活动上架（进行中），进入结算匹配池。
- 结算时系统自动判定最优档位并应用优惠，写入订单 `promotionPrice`。
- 活动到达结束时间或被运营下架（已结束/已关闭），退出结算匹配池。

### 与订单的关系

- 满减资格、档位规则、商品范围与活动状态由本文件负责。
- 满减如何接入订单价格构成（`promotionPrice` 作用于 `orderPrice` 减项层）由 `order-and-cart.md` 价格语义负责引用结果，不在本文件重复维护订单价格公式。

### 与取消 / 退款的关系

- 满减为订单提交时自动触发的非逆转优惠，不产生可恢复的实例，因此取消或退款时无需（也无从）单独回滚满减。
- 取消/退款额受扣减后的 `actualPrice` 约束，已通过满减降低的实付金额即为退款上限基准。
- 后续若实现订单项级退款（P16），需按行分摊满减优惠；该分摊机制由 P16 负责，本文件仅定义订单级满减语义。

### 已知约束

- `maxPerUser`（每人限参与次数）字段已存在于模型中，但当前无用户参与记录实体，满减计算为纯计算不落库计数，故 `maxPerUser` 暂不强一致执行（属 model-gap，见对应计划 Deferred 项）。
- 「送」（赠品）能力不在当前支持范围：档位表仅有 `meetAmount`/`discountValue`，无赠品字段。

## 限时折扣 / Time-Limited Discount

### 业务意图

- 限时折扣提供商品级或 SKU 级的短期降价促销能力（如「原价 99，限时 3 天 79」），按时间窗自动生效，到期自动失效。
- 与满减（订单级门槛优惠）不同，限时折扣直接降低 SKU 临时促销价（单价变更），是 SKU 维度的促销，而非订单维度的优惠。
- 限时折扣对用户透明且自动生效：用户无需领取、选择或操作；命中折扣的 SKU 自动按促销价成交。

### 业务规则

- 一条限时折扣记录绑定到具体商品（`goodsId`），可选绑定到具体 SKU（`productId`，为 null 时表示该商品全部 SKU 均参与）。
- 折扣方式由 `discountType` 决定，与满减共用 `mall/discount-type` 字典，承载同一编码语义：
  - 减金额/直降（`0`，`discountValue` 为直降额）：`promoPrice = retailPrice - discountValue`。
  - 打折（`10`，`discountValue` 为小数支付率，如 `0.9` 表示 9 折）：`promoPrice = retailPrice × discountValue`。
- 计算后促销价必须大于 0（折扣过度保护）：若 `promoPrice <= 0`，该折扣不生效，SKU 按原价成交。
- 折扣按时间窗（`startTime`/`endTime`）自动生效与失效；只有状态为「进行中」(10) 且当前时间在时间窗内的折扣才参与匹配。

### 价格层位（裁决 P15 遗留点）

- **限时折扣作用于商品单价层（goodsPrice 汇总层），而非订单级优惠减项层。** 限时折扣本质是 SKU 临时促销价（单价变更），命中折扣的 SKU 行单价取 `min(retailPrice, vipPrice, timeDiscountPrice)`，该行单价计入 `goodsPrice` 汇总。
- **限时折扣不复用 `promotionPrice` 槽位**：`promotionPrice` 仅供满减（订单级优惠，`orderPrice` 减项层）使用。限时折扣通过降低行单价间接降低 `goodsPrice`，不进入 `promotionPrice`。
- 该裁决消解了 P15 满减遗留的「限时折扣与满减并存时复用 promotionPrice 槽位需选择逻辑」问题：两者作用层位不同，天然分离。

### 与会员价（vipPrice）的层内取舍

- 限时折扣与会员价同处商品单价层。命中折扣的 SKU 行单价取三者最低：`min(retailPrice, vipPrice, timeDiscountPrice)`，使用户享受最优价。
- 抉择取「最低价」（备选「互斥：限时折扣优先覆盖会员价」被否——损害会员权益）。残留风险：取最低可能叠加侵蚀毛利，由运营配置折扣力度控制。

### 与满减的共存

- 限时折扣降低商品单价（`goodsPrice` 汇总层）→ 满减门槛以折扣后 `goodsPrice` 判定 → 两者天然可共存，满减自动按折扣后金额判档，**无需额外选择逻辑**。
- 计算顺序天然正确：先计算限时折扣 + 会员价后的行单价 → 汇总 `goodsPrice` → 据 `goodsPrice` 判定满减门槛与最优档位 → 再计算 coupon / orderPrice。
- 残留风险：限时折扣拉低 `goodsPrice` 可能使订单跌出满减门槛，符合商业直觉（折扣后金额判满减）。

### 多重折扣匹配（同 SKU 多活动）

- 同一 SKU 同一时刻只命中一个生效限时折扣，不叠加多个折扣。
- 多活动命中时取对用户最优（`promoPrice` 最低）的折扣；并列（相同 `promoPrice`）时取最新 `startTime`（限时折扣无 `priority` 字段，tiebreaker 仅用既有字段，不新增模型字段）。
- 理由：避免多重折扣叠加失控；参照满减「同订单只命中一档」原则。

### 折扣库存（stockLimit）强制

- `stockLimit` 表示折扣库存（0 表示不限）。`stockLimit > 0` 时强制：订单提交时对该折扣原子扣减活动库存，售罄后该 SKU 恢复原价；`stockLimit = 0` 时不限（仅受商品库存约束）。
- 扣减为原子条件 UPDATE（并发安全），售罄（剩余 < 请求数）时该折扣不生效或抛错（订单提交路径抛 `SOLD_OUT` 错误码，由事务回滚保证一致）。

### 活动状态语义

限时折扣活动状态复用 `mall/promotion-status` 字典（与满减一致）：

| 业务状态 | 状态码 | 含义 |
| -------- | ------ | ---- |
| 草稿 | 0 | 活动已创建但未上架，不参与匹配 |
| 进行中 | 10 | 活动已上架且在有效时间窗内，参与匹配 |
| 已结束 | 20 | 活动到达结束时间或被运营置为结束，不再参与匹配 |
| 已关闭 | 30 | 活动被强制下线，不再参与匹配 |

- 只有状态为「进行中」(10) 且当前时间在 `startTime`/`endTime` 时间窗内的折扣才会在结算/详情时被匹配。

### 管理员与用户动作

- 管理员可以创建、编辑、上下架限时折扣活动，配置商品/SKU 维度、折扣类型、折扣值、时间窗与折扣库存。
- 限时折扣对用户透明且自动生效：详情页展示促销横幅（促销价 + 直降金额 + 倒计时 + 剩余库存进度），列表页展示促销价与限时标签。

### 生命周期

- 管理员创建限时折扣活动（草稿），配置商品/SKU、折扣方式、时间窗与库存。
- 管理员将活动上架（进行中），进入匹配池。
- 时间窗内命中折扣的 SKU 自动按促销价成交，行单价计入 `goodsPrice`；`stockLimit` 随成交递减。
- 活动到达结束时间或被运营下架（已结束/已关闭），退出匹配池；售罄的 SKU 恢复原价。

### 与订单的关系

- 限时折扣资格、折扣计算、库存强制的业务语义由本文件负责。
- 限时折扣如何接入订单价格构成（作用于商品单价层，降低 `goodsPrice` 汇总，不进 `promotionPrice`）由 `order-and-cart.md` 价格语义负责引用结果，不在本文件重复维护订单价格公式。

### 与取消 / 退款的关系

- 限时折扣为提交时自动触发的非逆转价格，不产生可恢复的实例，因此取消或退款时无需（也无从）单独回滚限时折扣。
- 退款额受扣减后的行金额（`number × price`，`price` 已含限时折扣单价）约束。
- 取消/退款时是否返还已扣减的折扣库存（`stockLimit`）：当前不返还（简化语义，折扣库存为促销稀缺资源，不随订单取消回流），残留风险由运营在活动库存配置时预留余量控制。

### 已知约束

- 限时折扣无 `priority` 字段，多活动命中时 tiebreaker 用「最优价 + 最新 startTime」（不新增模型字段）。
- 限时折扣不涉场次/抢购/限购/不走购物车直接购买（这些属秒杀 P24 能力）。

## 搜索与互动能力

### 搜索关键字

- 搜索关键字用于把运营希望暴露的搜索入口组织成可消费的检索提示能力。
- 热门关键字用于表达当前优先推荐的搜索入口，默认关键字用于表达用户尚未输入时的默认引导词。
- 关键字可以关联跳转意图，但跳转的业务目的仍是引导用户进入商品或营销内容发现路径，而不是替代真实搜索结果页语义。
- 关键字是否对用户可见，取决于启用状态、排序和运营配置结果。
- 关键字治理属于运营可配置能力，不直接改变商品、订单或优惠规则本身。

### 搜索历史

- 记录用户最近的搜索行为。
- 支持查看和清空搜索历史。
- 搜索历史属于用户侧最近行为记录，不应与平台配置的热门/默认关键字混淆。

### 收藏

- 用户可以收藏商品和专题类营销内容。
- 详情页等界面应能查询收藏状态。

### 浏览足迹

- 记录用户最近浏览历史，方便回看。
- 支持查看和清空足迹记录。

### 评论 / 评价

#### 业务意图

- 评论用于把收货后的商品体验转化为可展示的用户反馈。
- 评论既服务后续用户决策，也服务商家了解履约和商品体验结果。

#### 业务规则

- 只有已完成收货的订单商品才能进入可评价状态。
- 每个订单商品只能评价一次。
- 评价资格受时间窗口限制；超过窗口后，该订单商品失去继续评价资格。
- 评价内容应表达具体商品或履约体验，不应替代售后申请。
- 评论一旦对外展示，其业务含义应保持与原订单商品快照一致，而不是跟随后续商品资料变化而重写。

#### 评分与展示

- 评分采用稳定的星级语义，用于表达用户满意度。
- 评论列表默认以面向其他用户消费的可见内容为主，不要求暴露后台处理细节。
- 评论可以附带与评价相关的补充内容，但不应改变“一个订单商品一次正式评价”的基本语义。

#### 参与者动作

- 商城用户可以在资格窗口内提交评价并查看自己的评价结果。
- 管理员可以查看评论、执行必要的审核/回复等运营动作，但不应改变评论与订单商品之间的业务归属关系。

#### 与其他流程的关系

- 评价资格边界由 `order-and-cart.md` 中的收货完成语义决定。
- 评论不替代退款或售后；用户对履约不满需要进一步处理时，应进入售后流程而不是通过评论直接改变订单状态。

## 内容营销

### 专题 / 特别内容

- 管理员可以发布主题化营销内容，引导用户发现商品。
- 专题有上架（status=0）和下架（status=1）两种状态，管理员通过上下架控制前台可见性。

### 业务规则

- 专题是一类由运营主动策划的内容对象，用于围绕商品、场景或活动组织内容消费路径。
- 专题可以关联商品，但专题本身不是商品，也不替代商品详情页。
- 专题的阅读量、封面和关联商品属于专题内容语义的一部分。
- 前台专题列表（`frontList`）仅展示上架（status=0）的专题。
- 专题下线或内容调整不应改变已完成订单的商品语义。

### 广告

- 管理员可以管理横幅类广告位。
- 广告可受时间窗口和启停状态控制。

### 业务规则

- 广告用于把用户流量引导到商品页、专题页或其他营销入口。
- 广告位置、时间窗口和启停状态共同决定广告当前是否对用户可见。
- 广告内容失效后应停止展示，不应继续作为有效营销入口暴露给用户。

### 常见问题与反馈

- FAQ 与反馈能力用于支持前台引导和客户沟通。

### FAQ 业务规则

- FAQ 负责承载高频问题与标准答复，优先服务于售前、下单、配送、售后等高频咨询场景。
- FAQ 属于静态内容能力，不直接改变订单、退款或售后状态。

### 反馈业务规则

- 反馈用于承载用户主动提交的问题、建议或异常说明。
- 反馈与评论不同：评论面向商品体验展示，反馈面向运营处理和客户沟通。
- 用户反馈可以进入后台处理流程，但不应直接替代售后申请、退款审批或工单状态机。

### 管理员与用户动作

- 管理员可以维护专题、广告、FAQ、关键字以及反馈处理结果。
- 商城用户可以浏览专题和广告入口、消费 FAQ 内容，并提交反馈。
- 用户提交反馈后，应能形成可追踪的后台处理对象，但其处理结果不直接改写订单或售后主状态。

## 跨域引用

营销域与订单主线存在多个交接点：

| 交接点 | 方向 | 目标文档 | 说明 |
|--------|------|---------|------|
| 优惠券价格构件 | → 出 | `order-and-cart.md` | 结算时校验可用券，影响 coupon price；取消/退款后按规则恢复 |
| 满减价格构件 | → 出 | `order-and-cart.md` | 结算时自动判定最优档位，影响 promotion price（orderPrice 减项层）；自动触发、不可恢复 |
| 限时折扣价格构件 | → 出 | `order-and-cart.md` | 命中折扣的 SKU 行单价取 min(retail,vip,timeDiscount)，作用于商品单价层（降低 goodsPrice 汇总），不进 promotion price；自动触发、不可恢复 |
| 秒杀价格构件 | → 出 | `order-and-cart.md` | 秒杀走独立 `flashSaleBuy` 路径，秒杀价（flashPrice）为成交单价（商品单价层）；不走购物车、不挂券、不与其他促销叠加；场次状态由 nop-job 翻转 |
| 积分抵扣构件 | → 出 | `order-and-cart.md` | 结算勾选积分抵扣，影响 integral price（actualPrice 减项层）；取消/整单退款返还，item 级不返还 |
| 团购上下文透传 | → 出 | `order-and-cart.md` | grouponRulesId/grouponId 经加购→结算透传；团购超时触发订单 204 |
| 团购优惠结果 | → 出 | `order-and-cart.md` | 团购成功后由订单侧价格语义消费 |
| 拼团价格构件 | → 出 | `order-and-cart.md` | 结算拼团下单，影响 pinTuan price（actualPrice 减项层，与 grouponPrice 同层）；拼团×团购单订单互斥；超时失败全单退款 |
| 评价资格 | ← 入 | `order-and-cart.md` | 收货完成是评价资格边界 |
| 售后不替代评价 | ← 入 | `order-and-cart.md` | 履约不满应进入售后流程，而非通过评论改订单状态 |
| 优惠券/团购/评价过期任务 | → 出 | `system-configuration.md` | 进入定时运营任务 |
| 营销事件通知/统计 | → 出 | `system-configuration.md` | 营销事件进入通知或报表时由系统配置域消费 |

全局流程视图见 `flow-overview.md`。

## 秒杀 / Flash Sale (Seckill)

### 业务意图

- 秒杀是按场次组织的短期、限量、低价强促销形式。其本质区别于限时折扣（常规降价促销）：秒杀以**场次**（session）为组织单位，每个场次有独立时间窗与库存；用户在抢购窗口内集中下单，库存抢完即止。
- 秒杀**不走购物车、直接购买**：用户在秒杀活动页或商品详情页点击「立即抢购」直接进入下单，不进入购物车流程，订单为单 SKU 单行的极简下单路径。
- 秒杀订单**不支持优惠券**、**默认不与任何其他促销叠加**（满减 / 限时折扣 / 会员价 / 积分抵扣 / 团购），秒杀价（`flashPrice`）即成交单价。

### 业务规则

- 一条秒杀活动（`LitemallFlashSale`）绑定到具体商品（`goodsId`），可选绑定到具体 SKU（`productId`，null 表示该商品的全部 SKU 均参与）。
- 一个秒杀活动可配置多个场次（`LitemallFlashSaleSession`），每场有独立的开始时间（`sessionStart`）、结束时间（`sessionEnd`）与场次库存（`sessionStock`）。
- 秒杀价 `flashPrice` 为成交单价（商品单价层），优先于会员价（`vipPrice`）与限时折扣价（秒杀场景例外）。
- 限购：每单限购 `maxPerOrder`（在 `flashSaleBuy` 中以请求数量上限校验）；每人限购 `maxPerUser` 的强一致执行依赖 `Order↔FlashSaleSession` 关联列（当前为 model-gap，见「已知约束」）。

### 下单路径（Decision A：独立 flashSaleBuy 路径）

**抉择：** 秒杀走独立的 `LitemallFlashSaleBizModel.flashSaleBuy()` `@BizMutation` 路径，**不进** `LitemallOrderBizModel.submit()`。

- 备选 B（扩展 `submit()` 增加可选 `flashSaleSessionId` 参数并分支）被否——会污染 submit() 主流程价格公式，与 P25 拼团的 submit() 改动耦合，且违背「不走购物车直接购买」的独立语义。
- **理由：** roadmap 明确「秒杀不走购物车直接购买」「秒杀订单不支持优惠券」，独立路径最干净表达此语义；与 P25 拼团（改 submit() 接线 pinTuanPrice）解耦无跨计划耦合。
- **下单步骤：** 校验场次状态、活动状态、商品在售、`maxPerOrder` 上限 → 原子扣减 `sessionStock`（售罄抛 ErrorCode）+ 复用既有 `goodsProductMapper.reduceStock` 扣减商品库存 → 以 `flashPrice` 为成交单价创建单行 OrderGoods 的订单（couponPrice / promotionPrice / integralPrice / grouponPrice / pinTuanPrice 全置 ZERO，不挂券、不判满减、不抵扣积分）。

### 限购执行路径（Decision B：maxPerOrder + maxPerUser model-gap）

- `maxPerOrder`（每单限购）：在 `flashSaleBuy` 中以请求数量上限校验，**无需模型改动**。
- `maxPerUser`（每人限购）：强一致执行需按用户累计计数其对该场次的秒杀成交订单数，依赖 `Order↔FlashSaleSession` 关联列（如 `LitemallOrder.flashSaleSessionId`）。
  - 备选 Alt A（新增关联列，触发 ORM 重生成，标记为 Protected Area ask-first）：可强一致查询计数 + 服务 P22 秒杀效果报表。
  - 备选 Alt B（**采纳**）：`maxPerUser` 延后为 model-gap，本计划仅执行 `maxPerOrder` + `sessionStock` 原子扣减；`maxPerUser` 强一致需求触发时由 successor 计划落地关联列。
  - **理由：** ORM 模型（`model/app-mall.orm.xml`）为 Protected Area（ask-first），无人工批准不可改；参照 P15 满减 `maxPerUser` 同样的 model-gap 先例。秒杀「限量」语义已由场次库存原子扣减保证，限购为运营增强项，非阻塞。

### 场次状态（sessionStatus）切换（Decision A：nop-job 持久化翻转）

**抉择：** `MallJobInvoker.switchFlashSaleSessions()` 定时扫描所有场次，按 `sessionStart` / `sessionEnd` 翻转 `sessionStatus`：

| sessionStatus | 含义 | 翻转条件 |
| ------------- | ---- | -------- |
| 0 | 未开始 | 当前时间 < `sessionStart` |
| 1 | 进行中 | `sessionStart` ≤ 当前时间 ≤ `sessionEnd` |
| 2 | 已结束 | 当前时间 > `sessionEnd` |

- 备选 B（读取时实时计算、不持久化翻转）被否——roadmap 明确「秒杀场次状态切换依赖 nop-job」；持久化状态使列表查询、前台展示、库存守卫（进行中才允许扣库存）一致，避免每次读取重复计算时间窗。
- job 频率与场次边界的精度差（秒级误差）不影响业务正确性：抢购边界由 `flashSaleBuy` 内的 `sessionStart ≤ now ≤ sessionEnd` 强校验兜底，job 翻转仅服务于列表展示与查询性能。

### 并发库存扣减语义（Decision A：场次库存为权威扣减单位）

- **`sessionStock` 为权威扣减单位**：通过新增 `LitemallFlashSaleSessionMapper.reduceFlashSaleSessionStock(sessionId, n)` 条件 UPDATE 原子扣减（参照 P23 `reduceTimeDiscountStock`：`UPDATE ... SET session_stock = session_stock - n WHERE id = ? AND session_stock >= n`，affectedRows = 0 视为售罄抛 ErrorCode `ERR_FLASH_SALE_SOLD_OUT`），事务回滚保证一致。
- 同时复用既有 `goodsProductMapper.reduceStock` 扣减商品库存（双库存双扣减）。
- `sessionStock = 0` 或 `null` 语义抉择：**0/null 均视为不限**（与 P23 `stockLimit = 0` 语义一致），运营需为「限量」场次显式配置正整数。
- **`totalStock` 为非规范化元数据**：≈ 各场次 `sessionStock` 之和，由后台配置/维护，仅作展示用（如列表显示「全场剩余 X 件」聚合），**不作为跨场次硬上限原子扣减**。理由：秒杀的并发竞争发生在单场次内，跨场次硬上限会引入分布式计数复杂度且无业务必要；单场次原子扣减已保证并发安全。
- **取消/退款库存回流语义：** 秒杀场次库存**不随订单取消/退款回流**（继承 P23 限时折扣先例——促销库存为活动稀缺资源，回流会破坏「限量」语义并引发超卖风险）。残留风险由运营在活动库存配置时预留余量控制。

### 不与其他促销叠加（Decision A：默认不叠加）

- 秒杀为独立直接购买路径，秒杀价（`flashPrice`）即成交单价（商品单价层），**默认不与满减 / 限时折扣 / 会员价 / 优惠券 / 积分抵扣 / 团购叠加**。
- **会员价（vipPrice）不叠加：** 秒杀场景下秒杀价优于会员价（秒杀例外规则），写入 owner doc 与代码（`flashSaleBuy` 不读取 `vipPrice`、不读取 `LitemallTimeDiscount`、不读取 `LitemallPromotionActivity`、不接入积分 spend / 团购）。
- **理由：** 秒杀本质为「限量低价强促销」，叠加会侵蚀毛利且违背「不支持优惠券」要求；用户享秒杀价即为最优价。残留风险：会员感知问题由运营在配置秒杀价时合理考虑（建议秒杀价 ≤ 会员价）。

### 活动状态语义

秒杀活动状态复用 `mall/promotion-status` 字典（与满减、限时折扣一致）：

| 业务状态 | 状态码 | 含义 |
| -------- | ------ | ---- |
| 草稿 | 0 | 活动已创建但未上架，不参与匹配 |
| 进行中 | 10 | 活动已上架；具体是否在抢购窗口由其场次的 `sessionStatus` 与时间窗决定 |
| 已结束 | 20 | 活动到达结束或被运营置为结束，所有场次不再可抢 |
| 已关闭 | 30 | 活动被强制下线，所有场次不再可抢 |

- 场次可抢的条件：活动 `status = 进行中(10)` **且** 场次 `sessionStatus = 进行中(1)` **且** 当前时间在 `sessionStart ≤ now ≤ sessionEnd` 时间窗内。
- 场次的 `sessionStatus` 由 nop-job 翻转，但 `flashSaleBuy` 仍以时间窗为兜底强校验，避免 job 边界误差。

### 管理员与用户动作

- 管理员可以创建、编辑、上下架秒杀活动，配置商品/SKU、秒杀价、限购（`maxPerUser` / `maxPerOrder`）、活动总库存，以及多个场次（每场的开始/结束时间与场次库存）。
- 商城用户可以浏览秒杀活动列表（按 `sessionStatus` 分区为「即将开始 / 进行中 / 已结束」+ 倒计时 + 抢购进度），在商品详情页查看秒杀横幅（秒杀价 + 倒计时 + 抢购进度条），点击「立即抢购」直接进入下单（不走加购）。

### 生命周期

- 管理员创建秒杀活动（草稿），配置商品/SKU、秒杀价、限购与库存。
- 管理员为该活动配置一个或多个场次（每场独立的开始/结束时间与场次库存）。
- 管理员将活动上架（进行中）；场次 `sessionStatus` 由 nop-job 按 `sessionStart` / `sessionEnd` 翻转（0 未开始 → 1 进行中 → 2 已结束）。
- 场次进行中（`sessionStatus = 1`）时，用户可发起 `flashSaleBuy`：原子扣减场次库存（售罄抛错）+ 扣减商品库存，以秒杀价创建单行订单。
- 场次到达结束时间或库存售罄，场次退出可抢状态；活动到达结束时间或被运营下架（已结束/已关闭），所有场次退出可抢状态。

### 与订单的关系

- 秒杀资格、场次状态、库存扣减、限购的业务语义由本文件负责。
- 秒杀如何接入订单（独立 `flashSaleBuy` 路径，秒杀价为成交单价，不进 submit() 的 promotionPrice / coupon / integral / groupon 槽位）由 `order-and-cart.md` 价格语义负责引用结果，不在本文件重复维护订单主流程价格公式。

### 与取消 / 退款的关系

- 秒杀场次库存**不随订单取消/退款回流**（同限时折扣先例）。退款额受订单 `actualPrice` 约束。
- 秒杀订单的售后仍走 `LitemallAftersale` 流程（item 级售后适用），退款额以秒杀价成交单价计算的行金额为上限。

### 已知约束

- **`maxPerUser` 强一致执行为 model-gap**：缺 `Order↔FlashSaleSession` 关联列，本计划仅执行 `maxPerOrder`。`maxPerUser` 强一致执行（含按场次计数查询）由 successor 计划落地关联列后实现，触发条件：限购强一致需求或 P22 秒杀效果报表。参照 P15 满减 `maxPerUser` 同样的 model-gap 先例。
- 秒杀场次库存无 DB 唯一键约束（`session_stock` 为可减为 0 的普通整数列），并发安全完全依赖应用层条件 UPDATE，已足够。

## 拼团 / Pin-Tuan（社交裂变拼团）

### 业务意图

- 拼团是一种社交裂变型促销：团长（发起人）发起拼团活动，邀请好友参团，在有效时间内凑满足团人数即以拼团专享价成交，否则超时失败并退款。
- 拼团与团购（Groupon）为**不同数据模型、不同价格槽位**的两种活动，可并行存在为不同活动，但**单订单二选一**（同一订单不能既是团购又是拼团）。两者并存不冲突（详见「与团购的区别与并存」）。

### 业务规则

- 一条拼团活动（`LitemallPinTuanActivity`）绑定到具体商品（`goodsId`），可选绑定到具体 SKU（`productId`，null 表示该商品全部 SKU 均参与）。
- 拼团活动配置拼团专享单价（`pinTuanPrice`，低于零售价）、成团人数门槛（`minUserCount`）、最多人数上限（`maxUserCount`，可选）、有效时间（`expireHours`，小时）。
- 拼团活动状态 `status` 复用 `mall/promotion-status` 字典（草稿 0 / 进行中 10 / 已结束 20 / 已关闭 30）。只有「进行中」(10) 的活动才允许开团/参团。
- 用户不能以参团身份加入自己发起的团（不能加入自己的团）。
- 用户不能重复加入同一个进行中的团。
- 拼团优惠（`pinTuanPrice` 减免额）只有在拼团业务条件成立（成团 SUCCESS）时才正式生效；超时失败时全额退款。

### 三实体结构（区别于团购扁平单表）

拼团采用三实体结构，区别于团购（Groupon）的扁平单表：

| 实体 | 职责 | 关键字段 |
| ---- | ---- | -------- |
| `LitemallPinTuanActivity` | 拼团活动配置（运营维护） | `goodsId`、`productId`、`pinTuanPrice`（拼团专享单价）、`minUserCount`、`maxUserCount`、`expireHours`、`status` |
| `LitemallPinTuanGroup` | 一次开团记录（团长发起） | `activityId`、`creatorUserId`、`orderId`（团长订单）、`status`（团状态）、`expireTime`（过期时间） |
| `LitemallPinTuanMember` | 参团成员记录 | `groupId`、`userId`、`orderId` |

成团人数 = Group 的 members 数（含团长 Member）≥ `activity.minUserCount` 时自动成团。

### 团状态语义

团状态（`LitemallPinTuanGroup.status`）由 `mall/pin-tuan-group-status` 字典定义：

| 团状态 | 状态码 | 含义 |
| ------ | ------ | ---- |
| 进行中 | 0 | 团已开，等待参团凑齐人数 |
| 拼团成功 | 10 | 成团人数达 `minUserCount`，拼团优惠正式成立 |
| 拼团失败 | 20 | 超时未凑齐人数，全员退款 |

- 团长 `openPinTuan` 创建 Group（status=进行中）+ 团长 Member。
- 每次有效 `joinPinTuan` 增加一个 Member；当成员数达 `minUserCount` 时系统自动将 Group.status 置为「拼团成功」(10)。
- `expirePinTuans`（定时任务）扫描 status=进行中 且 `expireTime` 已过的 Group，置为「拼团失败」(20) 并对全员已支付订单退款。

### pinTuanPrice 计算语义（Decision）

- **`LitemallPinTuanActivity.pinTuanPrice`**（displayName「拼团价」）为**拼团专享单价**；**`LitemallOrder.pinTuanPrice`**（displayName「拼团优惠金额」）为**减免额**。
- 计算公式：`order.pinTuanPrice = (retailPrice − activity.pinTuanPrice) × number`（按订单匹配 SKU 的行拼团减免汇总）。
- 校验 `activity.pinTuanPrice < retailPrice`（拼团价须低于零售价才有优惠，否则抛 ErrorCode `nop.err.mall.pin-tuan.price-invalid`）。
- 参照团购 `grouponPrice = rules.getDiscount()` 为减免额语义，`order.pinTuanPrice` 同样为减免额（非单价），保持一致。
- 当 `activity.productId = null`（全 SKU）时，按订单匹配该 `goodsId` 的 SKU 行的 retailPrice 计算。

### 价格层位与共存（Decision）

- `pinTuanPrice` 作用于 **actualPrice 减项层**（与 `grouponPrice`、`integralPrice` 同层）：`actualPrice = orderPrice - integralPrice - grouponPrice - pinTuanPrice`。
- 计算顺序天然正确：先 goodsPrice（含限时折扣/会员价单价层）→ orderPrice（满减/券减项）→ actualPrice（积分/团购/拼团减项）。
- 限时折扣为商品单价层，拼团减免以折扣后 goodsPrice 衍生——天然可共存，无需额外选择逻辑。

### 拼团 × 团购（Groupon）互斥（Decision）

- 拼团与团购为不同模型、不同价格槽位（`grouponPrice` vs `pinTuanPrice`），可并行存在为不同活动。
- **单订单二选一**：同一订单不能既是团购又是拼团。订单提交时若同时传 `grouponRulesId` 与 `pinTuanActivityId`，则抛 ErrorCode `nop.err.mall.pin-tuan.groupon-mutex` 拒绝。
- 理由：避免双重团购类优惠、价格语义混乱。

### 成团判定（Decision）

- 抉择：团长 `openPinTuan` 创建 Group（status=进行中）+ 团长 Member；每 `joinPinTuan` 增一 Member；当 `Group.members.size() ≥ activity.minUserCount` 时自动置 Group.status=SUCCESS。
- `maxUserCount`（最多人数，若设）为参团上限，达之拒绝新参团（抛 ErrorCode `nop.err.mall.pin-tuan.full`）。
- 团长计入成员数（团长是成团必要参与方）。
- 残留风险：并发参团下成团判定的计数竞争——通过「先 count 再 insert Member」+ 事务隔离处理（参照团购既有容忍度，READ_COMMITTED 下并发参团仍存在越界残隙，由 `maxUserCount` 上限兜底）。

### 超时失败 + 退款语义（Decision）

- `Group.expireTime = 创建时间 + activity.expireHours`。
- `expirePinTuans()`（job，参照 `expireGroupons`）扫描 status=进行中 且 `expireTime` 已过的 Group → 置 FAILED → 对每个 Member 的已支付订单全单退款。
- 退款链路（参照 `refundGrouponOrder`）：`PayService.refund` + 还库存 `goodsProductMapper.addStock` + 还券 `couponUserBiz.returnCoupon` + 还积分（`sourceType=refund-return`）+ 通知。退款失败不静默吞（汇总 LOG 等待人工干预），通知在 `txn().afterCommit` 执行。
- 成团（SUCCESS）的团**不退款**，拼团优惠正式成立。
- 退款前提校验：成员 orderId 对应订单已支付（`orderStatus=PAY` 且 `aftersaleStatus=INIT`），参照团购双重守卫。

### 管理员与用户动作

- 管理员可以创建、编辑、上下架拼团活动，配置拼团价、成团人数门槛、最多人数、有效时间。
- 商城用户可以浏览有效拼团活动、发起开团、参与他人拼团、查看拼团详情与我的拼团列表。
- 系统在成员数达标时自动成团，在超时未成团时自动失败退款。

### 生命周期

- 管理员创建拼团活动（草稿），配置拼团价与人数门槛。
- 管理员将活动上架（进行中），进入可开团池。
- 用户通过下单发起开团（创建 Group + 团长 Member）。
- 其他用户在过期前参团（创建 Member）。
- 成员数达 `minUserCount` 自动成团（SUCCESS），拼团优惠正式成立。
- 超时未凑齐人数，Group 自动失败（FAILED），全员退款。

### 与订单的关系

- 拼团资格、活动状态、成团判定、超时失败的业务语义由本文件负责。
- 拼团如何接入订单价格构成（`pinTuanPrice` 作用于 actualPrice 减项层）由 `order-and-cart.md` 价格语义负责引用结果，不在本文件重复维护订单价格公式。

### 与取消 / 退款的关系

- 拼团超时失败触发全单退款（还库存/券/积分/通知），参照团购既有退款链路。

### 站内信投递交接（P35）

- 团购失败退款（`expireGroupons`）与拼团失败退款（`expirePinTuans`）两个用户面向事件在触发 SMS 通知的同一 `txn().afterCommit` 钩子内**额外写入站内信**（`msgType=ORDER`），由 `MallNotificationService.sendUserMessage` 落地到 `LitemallUserMessage`。
- userId 由宿主 BizModel 从退款订单上下文传入通知方法（签名扩为 `(orderSn,mobile,userId)`），站内信归属校验、已读/未读语义见 `system-configuration.md`「站内信/消息中心」。
- 退款额受订单 `actualPrice` 约束。

### 已知约束

- 并发参团成团计数的越界残隙由 `maxUserCount` 上限兜底（参照团购容忍度，非强一致 DB 约束）。
- 拼团分享图/裂变海报/邀请奖励等运营支撑能力超出 P25 范围（不改成团判定核心业务语义）。

## 不在范围内

- 阶梯价和会员等级价不属于当前支持基线。
- 满减的「送」（赠品）能力不在当前支持范围（档位表无赠品字段）。

## 营销活动管理后台（P22）

### 业务意图

- 提供满减 / 限时折扣 / 秒杀 / 拼团 4 类活动的**统一管理入口**，运营可从后台侧边栏到达各类活动管理页，并对每类活动执行上下架动作。
- 提供营销活动效果分析（满减聚合 GMV、优惠券核销、拼团效果）与活动日历（排期展示 + 冲突提示）。
- 各类活动的**玩法本身**（满减档位、秒杀场次、拼团成团）由各自章节定义；本节只定义**统一管理面、上下架动作、效果统计口径、活动日历冲突口径**。

### 后台菜单结构

- 独立 TOPM `marketing-manage`（营销活动管理，orderNo 408），与 `promotion-manage`（推广：广告/专题/团购）业务域分离，避免污染推广管理。
- 子项：营销总览 / 活动日历 / 效果分析 + 4 类活动管理入口（满减/限时折扣/秒杀/拼团）。运营侧边栏直达，消除孤立管理页。
- 优惠券管理继续挂在 `promotion-manage`（与原有一致，非本计划结果面迁移）。

### 上下架动作（状态切换）

满减 / 限时折扣 / 秒杀 / 拼团共用 `mall/promotion-status` 字典（0 草稿 / 10 进行中 / 20 已结束 / 30 已关闭），各实体提供 `publishActivity` / `unpublishActivity` `@BizMutation`：

| 动作 | 目标状态 | 允许的来源状态 | 非法来源（拒绝，`ERR_PROMOTION_STATUS_TRANSITION_INVALID`） |
| ---- | -------- | -------------- | ---------------------------------------------------------- |
| `publishActivity` | 10 进行中 | 草稿(0)、已关闭(30) | 进行中(10)（已上架）、已结束(20)（已结束不可重新上架） |
| `unpublishActivity` | 30 已关闭 | 进行中(10)、已结束(20) | 已关闭(30)（已下架）、草稿(0)（草稿无需下架） |

- 上下架只翻转 `status`；前台结算/抢购匹配仍以 `status=10` 且时间窗为强校验（与各玩法章节一致）。
- 复用 `publishCoupon`/`publishRules` pattern：`requireEntity` + 置 `status` + 返回（事务由 `@BizMutation` 包裹）。

### 效果分析口径

| 指标面 | `@BizQuery` | 口径 |
| ------ | ---------- | ---- |
| 满减效果 | `getPromotionEffectiveness(startDate?, endDate?)` | **聚合口径**：`promotionPrice>0` 的订单的参与单数、`SUM(orderPrice)` GMV、`SUM(promotionPrice)` 优惠额。按活动归因需用户参与记录实体（model-gap，见下「已知约束」） |
| 优惠券核销 | `getCouponUsageStatistics(couponId?, startDate?, endDate?)` | 领取数（CouponUser 总数）、已使用数（status=1）、核销率、拉动 GMV（used 关联订单 `actualPrice` 之和） |
| 拼团效果 | `getPinTuanEffectiveness(activityId?, startDate?, endDate?)` | 开团数（Group 总数）、成团数（status=10）、成团率、参与人数（distinct member.userId）、GMV（member→order `actualPrice` 之和，按 group.addTime 时间窗） |
| 秒杀按场次效果 | — | **model-gap**：需 `Order.flashSaleSessionId` 关联列（ORM ask-first，见下「已知约束」） |

- 效果统计 `@BizQuery` 经 `@SqlLibMapper`（`LitemallMarketing.sql-lib.xml`）实现聚合；时间窗由调用方传 `startDate`/`endDate`（空时兜底全量）。
- 报表面向管理员后台，与 `system-configuration.md` 报表与统计章节一致。

### 活动日历与冲突检测口径

- 活动日历按时间轴展示**进行中(10)**的满减/限时折扣/秒杀/拼团活动，前端聚合 4 类 `findPage`（status=10）列表后按 `startTime` 排序展示，无需后端日历 API。
- **冲突判定口径：** 同一 goodsId 在同一时段被 ≥2 个**进行中(10)**活动命中即标冲突：
  - 满减 `goodsScope=ALL`（全商品）：与任何同时段 goodsId 活动都潜在冲突。
  - 满减 `goodsScope=CATEGORY`：经其分类下商品集与同时段 goodsId 活动求交集判定。
  - 满减 `goodsScope=GOODS`、限时折扣、秒杀、拼团：均按 goodsId（可选 productId）直接比对。
- 冲突在前端按 4 类列表的 goodsId + 时间窗集合判定，无需后端冲突 API。

### 已知约束（ORM model-gap，待 ask-first 授权）

- **满减 `maxPerUser` 强一致 + 按活动效果归因**：需新增 `LitemallPromotionUsage` 实体（userId/promotionActivityId/orderId）记录参与。当前满减为纯计算不落库，`maxPerUser` 暂不强一致执行；满减效果仅能按时间窗聚合，不可按活动归因。
- **秒杀 `maxPerUser` 强一致 + 按场次效果**：需 `LitemallOrder.flashSaleSessionId` 关联列。当前秒杀仅执行 `maxPerOrder`，`maxPerUser` 强一致与按场次 GMV/售罄率/限购命中数暂不可得。
- 上述两项为 ORM `model/*.orm.xml` 改动（Protected Area ask-first），授权落地后由 successor 计划实施（见 P22 计划 `Deferred But Adjudicated`）。
