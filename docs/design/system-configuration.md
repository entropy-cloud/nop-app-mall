# 系统配置业务设计

## 目的

说明 `nop-app-mall` 中由业务管理的配置项、文件存储预期、运营任务、通知能力、报表统计以及管理员操作记录。

## 边界

- 本文档负责面向业务的配置分类和运营行为。
- 持久化配置键、实体、字段集和字典定义在 `model/*.orm.xml` 或平台自带的系统配置模型中。
- 技术调度、存储和集成策略属于 `docs/architecture/`。

## 系统配置

### 业务角色

系统配置用于保存可编辑的运营设置，这些设置会改变商城行为，但不会重定义产品范围。

### 当前支持的配置分类

- 首页各区块展示数量
- 运费金额和包邮门槛
- 订单超时与自动收货时长
- 每用户地址数量上限
- 商城身份信息与联系方式
- 商品库存语义化阈值与文案（库存语义三档展示，见 `product-catalog.md`「库存语义化」）

### 业务规则

- 支持的配置项由应用基线预先定义。
- 配置变更应尽快对新进入评估的业务流程生效。
- 运营配置只能由具备相应职责的管理角色编辑。

### 商品库存语义化配置（P38）

库存语义化（`product-catalog.md`「库存语义化」）把纯数字库存按后台可配阈值映射为三档语义（充足/紧张/缺货），各档文案与色值均可后台覆盖，未配置时使用代码兜底默认。配置键存于 `LitemallSystem`（keyName/keyValue），由 `LitemallGoodsBizModel.getStockSemantic` 经 `ILitemallSystemBiz.getConfig()` 读取。

| 配置键（keyName） | 语义 | 兜底默认 |
| ---- | ---- | ---- |
| `mall_stock_threshold_tight` | 紧张档上限阈值（聚合库存 ≤ 该值且 > 0 → 紧张；> 该值 → 充足；= 0 → 缺货） | 10 |
| `mall_stock_label_sufficient` | 充足档展示文案 | 库存充足 |
| `mall_stock_label_tight` | 紧张档展示文案，支持 `{n}` 占位符替换为聚合库存数 | 仅剩 {n} |
| `mall_stock_label_out` | 缺货档展示文案 | 已售罄 |
| `mall_stock_color_sufficient` | 充足档色值（青色安心提示） | #17a2b8 |
| `mall_stock_color_tight` | 紧张档色值（红色紧迫提示） | #dc3545 |
| `mall_stock_color_out` | 缺货档色值 | #999999 |

业务规则：

- 商品级语义按其全部 SKU 的 `number` **求和**判定档位（求和更符合「该商品是否还可买」的用户心智）；单 SKU 缺货由详情页 SKU 下拉单独标识。
- 阈值边界归属：聚合库存 = 阈值 → 紧张；阈值 + 1 → 充足。
- 阈值非法（负数/非数字）时回退兜底默认；文案/色值留空时回退兜底默认。

## 文件存储

### 业务角色

- 文件存储用于承载商品图片、品牌图片、头像和其他商城素材。

### 业务规则

- 上传后的素材必须能被应用正常读取和访问。
- 允许上传的分类和大小限制由系统统一控制。
- 存储后端的选择不应改变上传素材的业务含义。

### 业务基线

- 只要能保证素材语义和可取回性，本地存储可作为支持方案。
- 云存储属于基础设施选择，不应改变素材的业务语义。

## 通知

### 业务角色

- 通知用于把订单、退款、履约等业务事件转化为用户或运营侧可感知的消息结果。
- 通知关注的是“何时通知谁、传达什么业务结果”，而不是底层采用哪种技术通道。

### 通知类别

通知类别包括：

- 支付确认
- 发货更新
- 后台订单提醒

### 业务规则

- 通知应由明确的业务事件触发，而不是由页面展示逻辑临时拼装。
- 同一类通知应保持稳定的业务语义，例如支付确认只表达“订单付款结果已成立”，不混入运营公告语义。
- 用户通知与后台运营提醒应区分接收对象和触发条件。
- 某个通知通道不可用时，不应改变订单、退款、发货等核心业务事实本身。
- 是否启用某个通知通道、模板内容如何配置、失败后是否允许重试，属于运营配置和技术实现共同约束的范围。

### 与公告的区别

- 通知由业务事件触发，强调结果送达。
- 公告由管理员主动发布，强调内容传播。
- 同一条业务事实可以触发通知，但不应自动等同为公告。

## 公告

- 公告是管理员编写并展示给用户或运营人员的系统或运营类消息。
- 公告是一种内容对象。
- 公告不同于支付确认、发货更新这类由事件触发的通知消息。

## 站内信 / 消息中心

### 业务角色

- 站内信是面向单个商城用户的、持久化在用户侧的站内消息。它把订单/退款/履约等业务事件的结果落成用户可在「消息中心」查看的记录，并为管理员提供系统公告下发能力。
- 站内信关注「用户能随时回看哪些业务结果与系统公告」，区别于「通知」关注送达、「公告」关注内容传播——三者关系：业务事件既触发通知（SMS/Email 等外部通道），也写入站内信（站内可回看记录）；管理员公告通过站内信的 SYSTEM 类别下发为全员可见的消息。
- 站内信是**拉取式**能力：消息持久化到 `LitemallUserMessage`，用户前台主动拉取，不做服务端实时推送（WebSocket / 小程序订阅消息 push 超出本基线）。

### 消息分类与事件映射

消息类型由 `mall/msg-type` 字典定义（`model/app-mall.orm.xml` 持有），映射关系：

| msgType（字典码） | 业务语义 | 产生方式 | 触发事件 |
| ----------------- | -------- | -------- | -------- |
| `ORDER`(0, 订单消息) | 订单/履约/退款类业务结果 | 业务事件触发，由 `MallNotificationService` 在事件钩子内写入 | 支付成功、发货、售后退款、团购失败退款、拼团失败退款 |
| `MARKETING`(10, 营销消息) | 营销活动类消息 | 本基线不主动产生（事件不映射到此类别） | 定向投放依赖 P20 标签分群，作为 successor |
| `SYSTEM`(20, 系统消息) | 管理员系统公告 | `broadcastSystemMessage` 下发，全员可见 | 管理员后台主动发布 |

事件→站内信通道接入清单（P35 落地）：

| 事件 | 宿主 BizModel 钩子 | 收件人 | msgType | 接入 |
| ---- | ------------------ | ------ | ------- | ---- |
| 支付成功（`pay` / `confirmPaidByNotify`） | `LitemallOrderBizModel` | 订单用户 | ORDER | 写 UserMessage（同时保留 SMS） |
| 发货（`ship`） | `LitemallOrderBizModel` | 订单用户 | ORDER | 写 UserMessage（同时保留 SMS） |
| 售后退款（`refund`） | `LitemallAftersaleBizModel` | 订单用户 | ORDER | 写 UserMessage（同时保留 SMS） |
| 团购失败退款（`expireGroupons`） | `LitemallGrouponBizModel` | 订单用户 | ORDER | 写 UserMessage（同时保留 SMS） |
| 拼团失败退款（`expirePinTuans`） | `LitemallPinTuanActivityBizModel` | 订单用户 | ORDER | 写 UserMessage（同时保留 SMS） |
| 新订单（管理员提醒） | `LitemallOrderBizModel` | 管理员邮箱 | — | **不写 UserMessage**（收件人是管理员，无 userId；维持 Email-only） |

> 退款类事件（售后/团购失败/拼团失败）统一映射到 `ORDER` 类别，因它们都是订单交易链路的结果；`MARKETING` 仅用于营销定向投放（successor）。

### 用户侧能力

| 动作 | BizModel 方法 | 语义 |
| ---- | ------------- | ---- |
| 我的消息列表 | `LitemallUserMessage__getMyMessages` | 按当前用户 userId + 可选 msgType 过滤，分页，按 addTime 倒序 |
| 未读数 | `LitemallUserMessage__getUnreadCount` | 当前用户未读消息数（驱动个人中心徽章），可按 msgType 过滤 |
| 标记已读 | `LitemallUserMessage__markRead` | 校验归属当前用户，置 `isRead=true`/`readTime=now` |
| 全部已读 | `LitemallUserMessage__markAllRead` | 批量置当前用户未读为已读（可按 msgType 限定） |
| 删除消息 | `LitemallUserMessage__deleteMessage` | 归属校验后逻辑删除（走既有 logical delete） |
| 消息详情 | `LitemallUserMessage__getMessageDetail` | 拉取详情，首次拉取未读则顺带标记已读（单事务） |
| 系统公告下发 | `LitemallUserMessage__broadcastSystemMessage` | `@Auth(roles="admin")`，下发 `msgType=SYSTEM` 消息 |

### 业务规则

- 站内信写入与 SMS/Email 外部通道**并列触发、互不阻断**：业务事件钩子在 `txn().afterCommit` 内同时调用 SMS/Email 发送与站内信写入；任一通道失败不应回滚核心业务事实（订单/退款/发货状态）。
- 用户只能查看、已读、删除**归属自己**的站内信；归属校验不通过抛 `nop.err.mall.message.not-belong-user`。
- 管理员系统公告（`broadcastSystemMessage`）按「逐活跃用户写入一条 `msgType=SYSTEM` 的 UserMessage」模型实现，仅管理员可达（`@Auth(roles="admin")`）。
- 事件触发的 ORDER 消息受开关 `mall_message_event_enabled_<event>` 控制（存于系统配置，默认开启），允许运营关闭某类事件站内信。
- 站内信内容（title/content）由应用层在事件方法内**直接拼装**（不引入 `NopSysNoticeTemplate` 模板渲染）；模板可视化运营编辑作为 successor。

### 关键设计抉择（Decision）

1. **事件→消息投递通道接入方式与收件人范围。** 抉择 A（在 `MallNotificationService` 内新增 `sendUserMessage(userId,msgType,title,content)`；仅 5 个用户面向事件方法在同钩子中额外调用它写 UserMessage；userId 由调用方 BizModel 传入，因宿主 BizModel 持有 order/user 上下文）。备选 B（服务端按 orderSn 反查 userId）被否——反查增加跨实体依赖且重复查询。残留风险：需微调 5 个用户方法签名补 userId + 联动更新 `TestMallNotificationService`。
2. **userId 来源与签名变更范围。** 5 个用户方法签名由 `(orderSn,mobile)` 扩为 `(orderSn,mobile,userId)`（userId 可空降级时仅发 SMS）；同步更新 6 处用户面向 `afterCommit` 调用点（4 个调用方 BizModel）补 userId 实参（2 处 admin 调用点不变）；同步更新 `TestMallNotificationService` 中 3 个调用变更方法的用例（第 4 个 admin 用例不变）。`sendAdminOrderNotification(orderSn)` 不变。
3. **已读语义与未读徽章。** 抉择 A（`isRead` 单字段 + `readTime`；进入详情即 markRead，未读数 = `findCount(userId,isRead=false)`）。备选 B（未读/已读双状态机）被否——单字段足以表达。
4. **管理员系统公告下发模型。** 抉择 A（`broadcastSystemMessage` 下发时为每个活跃用户生成一条 `msgType=SYSTEM` 的 UserMessage）。备选 B（「公告表 + 用户读位」惰性可见）被否——基线用户量非超大，直接写入实现最简、未读计数语义一致。残留风险：写入放大（大用户量下逐用户插入成本），触发条件见各计划 Deferred。
5. **消息内容拼装方式。** 抉择 A（应用层直接拼装 title/content，事件方法内就地组装文案）。备选 B（引入 `NopSysNoticeTemplate` 模板渲染）作为 successor——模板可视化运营编辑需求出现时再引入。理由：基线文案稳定，直接拼装最简、零额外依赖。

### 与通知/公告的边界

- **通知（Notification）**：业务事件触发的外部通道送达（SMS/Email），强调「结果已送达」，不可回看。
- **站内信（UserMessage）**：业务事件触发或管理员下发，持久化在用户侧，强调「用户可随时回看」。
- **公告（Announcement）**：管理员主动发布的内容对象；在本基线中，面向全体用户的公告**通过站内信的 SYSTEM 类别下发**，二者在「全员系统消息」语义上合流；事件触发的通知**不**等同公告（沿用本文件「通知」章节 line 73-77 既有界定）。

## 优惠券 DIY 投放配置

### 业务角色

- 优惠券 DIY 投放配置由运营在后台管理（`LitemallCoupon.view.xml`），通过表单编辑优惠券的商品范围、限领数量、人群标签与有效期，无需开发介入。

### 配置维度

| 字段 | 字典/类型 | 配置语义 |
| ---- | --------- | -------- |
| `name` / `desc` | 字符串 | 优惠券名称与介绍（展示文案） |
| `tag` | 字符串 | 人群标签（如「新人专用」），**纯展示**，不参与会员级路由 |
| `type` | `mall/coupon-type` | 赠送类型（0=通用领取/1=注册赠/2=兑换码） |
| `discount` / `min` | decimal | 优惠金额与最低消费门槛 |
| `total` | int（0=无限） | 优惠券总数量 |
| `limit` | int（0=不限，默认 1） | 用户领券限制数量 |
| `goodsType` | `mall/coupon-goods-type` | 商品范围类型（0=ALL/1=CATEGORY/2=GOODS） |
| `goodsValue` | 字符串（逗号分隔 ID 列表） | 商品范围值：分类 ID 或商品 ID 列表 |
| `code` | 字符串 | 兑换码（type=2 时使用） |
| `timeType` | `mall/coupon-time-type` | 有效期类型（0=DAYS/1=RANGE） |
| `days` | int | timeType=0 时：领后 N 天有效 |
| `startTime` / `endTime` | datetime | timeType=1 时：固定时间段 |
| `status` | `mall/coupon-status` | 优惠券状态（0=正常/1=过期/2=下架），由 publish/unpublish 动作切换 |

### 业务规则

- 后台 Coupon 表单（`edit`/`add`/`view`）需充分暴露 `goodsType`/`goodsValue`/`limit`/`tag`/`timeType`/`days`/`startTime`-`endTime` 等投放配置字段，使运营可独立完成 DIY 配置。
- 后台列表（grid）应包含 `goodsType`/`goodsValue` 列，便于运营浏览已有投放范围。
- 业务语义（包括 `goodsType`/`timeType`/`limit` 等字段的取值含义与分类范围兑换自洽）由 `marketing-and-promotions.md` 持有，本文件仅持有「后台可配置性」语义。
- 上下架动作（`publishCoupon`/`unpublishCoupon`）切换 `status`：上架=0（正常）、下架=2（下架）；过期=1 由系统定时任务（`expireCoupons`）扫描置位。
- 兑换码（`code`）字段仅在 `type=2`（EXCHANGE）时有业务含义；其他类型应留空。

### 管理员动作

- 管理员可以创建、编辑优惠券投放配置（商品范围、限领、有效期、标签等）。
- 管理员可以上下架优惠券（控制 `status`）。
- 管理员可以查看优惠券列表（含商品范围列），跟踪已有投放。

### 与其他 Owner Docs 的关系

- 优惠券业务规则（领取/使用/范围匹配语义）由 `marketing-and-promotions.md` 持有。
- 字段定义与字典以 `model/app-mall.orm.xml` 为准（受保护区域，改动须人工确认）。

## 定时运营任务

### 业务基线

- 自动取消超时未支付订单
- 自动确认超时未收货订单

### 扩展运营任务

- 优惠券过期处理
- 团购过期处理
- 评价窗口过期处理

### 技术装配

- 调度引擎采用 `nop-job-local`（本地内存调度器，非分布式）
- 5 个定时任务通过 `_vfs/nop/job/conf/scheduler.yaml` 注册，绑定 `MallJobInvoker` 的 5 个方法
- `application.yaml` 配置 `nop.job.scheduler.config-path` 指向调度配置
- 调度频率：cancelExpiredOrders=15min, confirmExpiredOrders=1h, expireCoupons=1h, expireGroupons=30min, expireCommentWindow=1h

## 字典维护

### 业务角色

- 商城业务字典（售后类型、售后状态、售后原因等）由 `model/app-mall.orm.xml` 的 `<dicts>` 段声明，作为前台展示与后台校验的稳定来源。

### 售后原因字典（mall/aftersale-reason）

- 售后原因字典化：用户发起售后时，退款原因从 `mall/aftersale-reason` 字典选项中选择，而非自由文本。
- 字典在 `model/app-mall.orm.xml` 声明，`LitemallAftersale.reason` 列通过 `ext:dict="mall/aftersale-reason"` 绑定该字典。
- 申请时后端校验 `reason` 必须为字典内已定义的选项；历史记录的文本 `reason` 保持兼容（校验仅作用于新增/变更）。
- 运营如需调整原因选项，通过修改 ORM 模型字典并重新生成落地（受保护区域，改动须人工确认，见 `docs/context/ai-autonomy-policy.md`）。

## 管理员操作日志

### 业务角色

- 记录重要管理员动作，满足审计和排障需要。

### 记录范围

- 商品管理
- 发货、退款等订单操作
- 用户和管理员管理
- 系统配置变更
- 后续营销运营动作

## 报表与统计

### 业务角色

- 报表与统计用于把订单、商品、用户和运营活动汇总为管理后台可消费的经营信息。
- 报表关注业务口径、筛选维度、统计周期和使用场景，不负责底层报表引擎实现方式。

### 当前支持的业务视角

报表区域包括：

- 订单数量与收入
- 待处理订单工作量
- 商品与库存汇总
- 用户增长与活跃度

### 技术装配

- 采用 AMIS chart 组件 + 现有 GraphQL 统计 API（`getOrderStatistics`/`getGoodsSalesRanking`/`getUserStatistics`），未引入 nop-report 引擎
- P18 经营看板重做新增 4 个 `@BizQuery`（`getDashboardMetrics`/`getSalesTrend`/`getRealtimeOrders`/`getTodoAggregation`，挂载于 `LitemallOrderBizModel`），支撑指标卡 / 趋势图 / 实时订单流 / 待办聚合 4 区块
- P19 报表体系扩展新增销售漏斗 / 商品分析 / 用户分析 / 订单分析 / 营销分析（优惠券）5 大主题域 `@BizQuery`（同挂 `LitemallOrderBizModel`），经 SQL-lib 聚合 + AMIS chart 消费
- 后台统计看板页面：`app-mall-web/.../mall/stat/stat-dashboard.page.yaml`（经营看板）+ `mall/stat/stat-funnel.page.yaml`（销售漏斗）+ `mall/stat/stat-product.page.yaml`（商品分析）+ `mall/stat/stat-user.page.yaml`（用户分析）+ `mall/stat/stat-order.page.yaml`（订单分析）
- 后台菜单入口：`stat-manage`（`app-mall.action-auth.xml` 已开放）
- 导出方式：CSV 兜底（前端 AMIS 导出，零新依赖）。nop-report xlsx/pdf 引擎为 successor（复杂模板化报表需求出现时引入）。

### 经营看板指标口径（P18）

经营看板为拉取式刷新，所有指标基于稳定业务状态聚合，口径如下：

- **今日 GMV**：当日支付订单（`orderStatus ≥ 201`，含已支付及之后各态）`actualPrice` 之和；时间窗口按 `payTime`（支付时间）归属当日。
- **订单数**：当日支付订单数（同 GMV 口径的订单计数）。
- **客单价（AOV）**：GMV / 订单数。
- **UV（登录用户）**：当日有足迹行为（`LitemallFootprint`）的去重 `userId` 数（下单用户必有前置浏览足迹，故以足迹为 UV 主信号，与下单用户天然重合）。匿名访客未持久化，不含匿名浏览（model-gap successor）。
- **转化率**：当日支付用户数 / 当日 UV。
- **退货率**：当日售后已完成退款单数（`LitemallAftersale.status = 3 REFUND`，按 `addTime` 归属当日）/ 当日支付订单数。
- **同环比**：日环比 = (今日 GMV − 昨日 GMV) / 昨日 GMV；周同比 = (今日 GMV − 上周同日 GMV) / 上周同日 GMV；对比期为 0 时返回空（前端显示 `--`）。
- **销售趋势**：按 `granularity`(hour/day/week/month) + 时间区间分组聚合 GMV 与订单数，按 `payTime` 归属；时序点含零值补齐（连续图表）。
- **实时订单流**：最近 N 条订单（默认 20），按 `addTime` 倒序。
- **待办聚合**：
  - 待发货 = 订单 `orderStatus = 201` 计数；
  - 待退款 = 订单 `orderStatus = 202`（退款中）计数；
  - 售后待审核 = `LitemallAftersale.status = 1`（REQUEST 用户已申请）计数；
  - 库存预警 = 在售商品中聚合库存（`SUM(LitemallGoodsProduct.number)`）≤ 阈值的商品计数，阈值复用全局配置 `mall_stock_threshold_tight`（默认 10，经 `ILitemallSystemBiz.getConfig()` 读取），明细含商品名 / 聚合库存。

### 销售漏斗指标口径（P19）

销售漏斗为同口径同期对比（非跨期留存），5 段按时间区间聚合，口径如下：

- **浏览（view）**：期间 `LitemallFootprint` 去重商品数（`COUNT(DISTINCT goodsId)`），按 `addTime` 归属期间。匿名访客未持久化，不含匿名浏览。
- **加购（cart）**：期间 `LitemallCart` 新增条数（`COUNT(*)`），按 `addTime` 归属期间。
- **下单（order）**：期间下单商品件数（`SUM(LitemallOrderGoods.number)`），排除已取消订单（`orderStatus NOT IN (102,103)`），按订单 `addTime` 归属期间。
- **支付（pay）**：期间支付商品件数（`SUM(LitemallOrderGoods.number)`），仅含已支付及之后各态订单（`orderStatus ≥ 201`），按订单 `payTime` 归属期间。
- **复购（repurchase）**：期间 ≥ 2 单支付用户数（按 `userId` 分组 `COUNT(*) ≥ 2`），按 `payTime` 归属期间。
- **转化率**：各相邻段比值（cart/view、order/cart、pay/order、repurchase/pay）+ 整体 pay/view，分母为 0 时返回 0。

### 商品分析指标口径（P19）

商品分析聚合销量排行、加购排行、滞销品与动销率，支持类目筛选（`categoryId` 可选），口径如下：

- **销量排行**：期间下单商品件数降序前 N（复用 `getGoodsSalesRanking` 口径，扩展类目筛选），排除已取消订单。
- **加购排行**：期间 `LitemallCart` 按 `goodsId` 聚合 `COUNT(*)` 降序前 N。
- **滞销品**：期间在售（`isOnSale=1`）但零销量的商品前 N（`NOT EXISTS` 期间有销量的子查询）。
- **动销率**：有销量商品数 / 在售商品数；有销量商品数 = 期间 `COUNT(DISTINCT goodsId)`（排除已取消订单）；在售商品数 = `isOnSale=1` 商品计数。

### 用户分析指标口径（P19）

用户分析包含留存、RFM、生命周期、复购率 4 类，以**支付订单**为行为事件，口径如下：

- **留存（次留/7 留/30 留）**：以用户在分析期间内的**首次支付日**（D0）分组形成 cohort，D+N 留存 = 该 cohort 用户在 D0+N 当天有支付行为的用户数 / cohort 用户数。留存事件为支付订单（`orderStatus ≥ 201`），按 `payTime` 归属。每 cohort 返回 cohortSize / d1 / d7 / d30 计数 + 留存率。
- **RFM 分层**：R=最近支付距今天数（越小越优）、F=期间支付订单数、M=期间支付金额。按三分位（中位数）分高/低，组合成 8 类：重要价值 / 重要保持 / 重要发展 / 重要挽留 / 一般价值 / 一般保持 / 一般发展 / 一般挽留。阈值采用当批数据中位数（非后台可配，后续可扩展为配置项）。
- **生命周期**：按支付 recency 派生 4 类——新客（首次支付在分析期间）/活跃（期间有支付但首单不在期间）/沉睡（历史有支付但期间无，且距上次支付 < 流失线）/流失（距上次支付 ≥ 流失线）。流失线默认 90 天（`churnDays` 参数可调）。基于全量历史支付数据（非仅期间）派生。
- **复购率时序**：按天分组，复购率 = 当天 ≥ 2 单支付用户数 / 当天支付用户数。返回每日 paidUsers / repurchaseUsers / rate 三元组时序。

### 订单分析与营销分析指标口径（P19）

订单分析聚合客单价分布、支付方式占比、退货原因占比；营销分析覆盖优惠券核销率与拉动 GMV，口径如下：

- **客单价分布**：期间支付订单（`orderStatus ≥ 201`，按 `payTime` 归属）按 `actualPrice` 分段计数（0-50 / 50-100 / 100-200 / 200-500 / 500+），返回各段订单数。
- **支付方式占比**：期间支付订单按 `payChannel` 聚合（1=微信支付、2=余额支付、`walletPayAmount > 0`=混合支付、其他），返回各方式订单数。当前以微信支付为主（多通道 P30 后丰富）。
- **退货原因占比**：期间售后单（`LitemallAftersale.status ≥ 1`，按 `addTime` 归属）按 `reason` 字典聚合，空值归"未填写"，返回各原因售后单数。
- **优惠券分析**：期间领取券数（`LitemallCouponUser` 按 `addTime` 归属）/ 核销数（`status=1`）/ 核销率（核销数/领取数）/ 拉动 GMV（核销券关联订单 `actualPrice` 之和）。
- **导出方式（E1）**：CSV 兜底（前端 AMIS 导出按钮，零新依赖）。nop-report xlsx/pdf 引擎为 successor（复杂模板化报表需求出现时引入）。

### 业务规则

- 报表口径应与对应业务领域中的正式状态语义保持一致，例如订单收入、退款完成、待处理订单等统计必须使用稳定业务状态，而不是临时页面口径。
- 报表默认面向管理员和运营角色，不直接面向普通商城用户。
- 报表既可以表现为后台统计看板，也可以表现为筛选后导出或定时报送的结果。
- 报表输出可以按时间区间、业务状态、商品/类目、用户增长等维度过滤，但具体字段清单和技术模板不在本设计 owner doc 中展开。
- 报表生成失败不应改变原始业务数据；它属于观察和经营分析能力，而不是交易主流程的一部分。

## 营销活动管理后台

### 业务角色

- 营销活动管理后台为运营提供满减 / 限时折扣 / 秒杀 / 拼团 4 类活动的统一管理入口、上下架动作、活动日历与效果分析。

### 后台菜单结构

- 独立 TOPM `marketing-manage`（营销活动管理，orderNo 408），与 `promotion-manage`（推广：广告/专题/团购规则/团购活动）业务域分离。
- 子项：营销总览（`marketing-overview.page.yaml`）、活动日历（`marketing-calendar.page.yaml`）、效果分析（`marketing-effect.page.yaml`）、满减活动、限时折扣、秒杀活动、拼团活动。
- 优惠券管理继续挂在 `promotion-manage`（与原有一致）。

### 运营动作

- 运营可对满减/限时折扣/秒杀/拼团每类活动执行上下架（`publishActivity`/`unpublishActivity`），状态切换规则与冲突/效果口径由 `marketing-and-promotions.md`「营销活动管理后台」章节持有。
- 效果分析看板消费满减聚合 GMV、优惠券核销、拼团效果统计 `@BizQuery`；秒杀按场次效果待 ORM 授权补齐。

### 与其他 Owner Docs 的关系

- 营销活动业务语义（玩法、状态字典、上下架规则、效果口径、冲突口径）由 `marketing-and-promotions.md` 持有。
- 本文件仅持有「后台菜单结构与运营可达性」语义。

## 用户运营工作台

### 业务角色

- 用户运营工作台为运营提供单用户全貌聚合视图与行级运营动作，把用户管理从只读列表升级为可操作工具（P20）。

### 运营动作

| 动作 | BizModel 方法 | 语义 |
| ---- | ------------- | ---- |
| 封禁 / 解禁 | `LitemallUserBlacklist__banUser` / `unbanUser` | 杠杆 `NopAuthUser.status`（0=禁用、1=正常）并同步写 `LitemallUserBlacklist`（记录原因/操作员），二者保持一致。封禁用户登录由平台 status 机制拒绝，下单由订单 `submit` 的 `ERR_USER_BANNED` 守卫拒绝 |
| 手工调级 | `LitemallMemberLevel__setUserLevel` | 运营直接设定 `userLevel`（0/1/2），校验目标值在 `mall/user-level` 字典内，变更写入管理员操作日志（不进积分流水） |
| 手工发券 | `LitemallCouponUser__dispatchCoupon` | 包装既有 `claimCouponForUser`，复用全部 total/limit/status 校验，记录运营操作日志。关闭 P26/P32 自动发券 deferred 的手动路径 |
| 手工加积分 | `LitemallPointsAccount__adjustPoints` | 既有方法，用户工作台提供用户级直达入口（积分账户页已接线，本工作台补用户维度入口） |

### 用户详情聚合查询

- `LitemallUserBlacklist__getUserWorkbenchSummary(userId)`：聚合该用户的订单数/累计消费、积分余额、优惠券数（按状态）、足迹数、反馈数、当前等级/标签/黑名单态，供用户详情聚合页消费。跨实体经各自 `I*Biz`。

### 用户标签与分群

- `LitemallUserTag__addUserTag` / `removeUserTag`：运营对用户打标/去标，`(userId, tag)` 去重。
- `LitemallUserTag__findUsersByTag`：按标签分群查询。本基线为简单标签集合分群，非算法画像/RFM（后者归 P19 报表扩展 successor）。

### 权限与审计

- 所有运营动作标注 `@Auth(roles="admin")`，仅管理员可达。
- 封禁/调级/发券写入 `LitemallLog` 管理员操作日志，满足审计排障需要。

### 后台菜单结构

- `mall-user-manage`(100) 下新增子项：
  - `mall-user-workbench`(101) 用户运营工作台 → 平台 `NopAuthUser` 列表（经 app-mall-delta 增加行级运营动作：封禁/解禁/调级/发券/加积分/查看详情）。
  - `mall-user-tag`(107) 用户标签管理 → `LitemallUserTag` CRUD + 「按标签分群」入口。
  - `mall-user-blacklist`(108) 用户黑名单管理 → `LitemallUserBlacklist` CRUD。
- 用户详情聚合页 `mall/user-ops/user-detail.page.yaml`：经工作台行「详情」按钮（带 userId）进入，消费 `getUserWorkbenchSummary`，不在菜单直接暴露。

### 与其他 Owner Docs 的关系

- 标签/黑名单建模决策与会员等级/权益手工发放语义见 `user-and-address.md`「用户标签与黑名单」「会员等级体系」。
- 封禁状态对下单的影响见 `order-and-cart.md`。

## 订单运营工作台

### 业务角色

- 订单运营工作台为运营提供订单批量发货、改价/改运费、改地址、订单标记、orderSn 模糊搜索与异常监控能力（P21）。
- 把订单管理从「单行发货 + 只读列表」升级为可批量操作的运营工具。

### 运营动作

| 动作 | BizModel 方法 | 语义 |
| ---- | ------------- | ---- |
| 改价 / 改运费 | `LitemallOrder__modifyOrderPrice` | 按构件层分级守卫：改运费恒允许（待支付态）、改商品价仅纯商品订单允许。安全策略见 `order-and-cart.md`「改价/改运费安全策略」 |
| 批量发货 | `LitemallOrder__batchShip` | Excel 导入运单号批量发货，复用 `ship` 单行逻辑，部分失败不阻断成功行 |
| 改地址 | `LitemallOrder__changeOrderAddress` | 仅发货前可改，新地址校验归属同一用户 |
| 订单标记 | `LitemallOrder__markOrder` | 写既有 `adminRemark` 字段（surface 既有列，无 ORM 改动） |
| 超期未发货监控 | `LitemallOrder__getOverdueUnshippedOrders` | `status=201` 已支付未发货且 addTime 早于 cutoff（默认 168h） |
| 超期未支付监控 | `LitemallOrder__getOverdueUnpaidOrders` | `status=101` 待支付且 addTime 早于 cutoff（默认 30min） |

### 权限与审计

- 所有运营动作标注 `@Auth(roles="admin")`，仅管理员可达。
- 改价/改地址/标记/批量发货写入 `LitemallLog` 管理员操作日志。

### 异常监控口径

- 超期未发货 cutoff 默认 168 小时（7 天），与系统配置的自动收货时长对齐；超期未支付 cutoff 默认 30 分钟，与订单超时分钟数对齐。
- 异常监控查询与 `cancelExpiredOrders`/`confirmExpiredOrders` 调度翻转职责互补：调度负责翻转状态，工作台负责暴露逾期集合供运营审视。运营动作记录见「管理员操作日志」记录范围。

### 后台菜单结构

- `mall-manage`(200) 下新增子项：
  - `mall-order-batch-ship` 批量发货 → `mall/order-ops/batch-ship.page.yaml`
  - `mall-order-exception` 异常监控 → `mall/order-ops/order-exception.page.yaml`
- `mall-order-manage`(204) 订单管理保留既有列表，surface `adminRemark` 列 + 行级运营动作（改价/改地址/标记，按状态 `visibleOn`）。

### 与其他 Owner Docs 的关系

- 改价安全策略、改地址/标记/批量发货/异常监控的业务语义与状态守卫见 `order-and-cart.md`「订单运营工作台」。
- 本文件仅持有「后台菜单结构与运营可达性」语义。

## 商品运营工作台（P36）

### 业务角色

- 商品运营工作台为运营提供商品批量运营（改价/改库存/上下架）、批量导入导出（xlsx via ExcelHelper）、库存预警与评论运营能力。
- 把商品管理从「单行 CRUD + 单行上下架」升级为可批量操作的运营工具，评论管理从「只能删除」升级为可批量回复/后置审核。

### 运营动作

| 动作 | BizModel 方法 | 语义 |
| ---- | ------------- | ---- |
| 批量改价 | `LitemallGoods__batchUpdatePrice` | goodsId→retailPrice 列表，逐行直接设 `retailPrice`（不经 update 管道，与单条 CRUD 的 `syncRetailPrice` 派生口径隔离），部分失败不阻断成功行 |
| 批量改库存 | `LitemallGoods__batchUpdateStock` | productId→number 列表，经 `ILitemallGoodsProductBiz` 跨实体设 `number`（禁止 `daoProvider().daoFor()` 绕过），部分失败不阻断 |
| 批量上架 | `LitemallGoods__batchOnSale` | goodsId 列表，复用 `onSale` 单行逻辑（含上架 SKU 守卫），部分失败不阻断 |
| 批量下架 | `LitemallGoods__batchOffSale` | goodsId 列表，复用 `offSale` 单行逻辑，部分失败不阻断 |
| 导出商品 | `LitemallGoods__exportGoods` | 按筛选条件导出 CSV（xlsx 写出为 successor），含 SKU 关键字段 |
| 导入商品 | `LitemallGoods__importGoods` | xlsx via `ExcelHelper.readSheet`（复用 batchShip 先例），按 goodsSn 匹配新增/更新，含字段校验 + 错误报告 |
| 库存预警 | `LitemallGoods__getStockWarningList` | SKU 粒度低库存列表，阈值 = per-SKU `safeStock`（非空且 >0）否则全局 `mall_stock_threshold_tight`，按库存升序 |
| 批量回复评论 | `LitemallComment__batchAdminReply` | commentId→adminContent 列表，逐行写 `adminContent`，复用 `adminReply` 校验，结果报告 |
| 批量评论审核 | `LitemallComment__batchModerateComments` | commentId 列表 + action=hide/restore，置 `deleted`（后置 Moderation，复用既有字段），结果报告 |
| 评论工作台列表 | `LitemallComment__getCommentReviewList` | 支持按 star/hasPicture/关键字/时间筛选 + 分页，供工作台消费 |

### 批量操作事务边界与部分失败策略（Decision G1）

- 逐行独立处理 + 聚合结果报告（复刻 `LitemallOrder__batchShip` 先例：catch-and-continue + `BatchShipResultBean`/`BatchGoodsResultBean` 聚合）。
- 单行失败记入错误列表不回滚其他行，返回 `{successCount, failedCount, failures:[{id, reason}]}` 形式（每行一条 `BatchGoodsResultBean`）。
- 调用方需读结果报告处理失败行。

### 导入导出格式（Decision G2）

- 导入用 xlsx，复用平台 `ExcelHelper.readSheet`（本仓库已在 `batchShip` 落地，无新依赖）。
- 导出按可用写出入口抉择：本基线无平台 xlsx 写出 helper，走 CSV 兜底（数据导出能力不缺；模板化 xlsx 导出为 successor，可随 nop-report 引入续作）。

### 库存预警阈值来源（Decision G3）

- 优先用既有 per-SKU `LitemallGoodsProduct.safeStock`（`orm.xml:806`，已存在）；`safeStock` 为空/0 时回退全局配置 `mall_stock_threshold_tight`（P38 已落地）。
- 返回 `thresholdSource`（`safeStock` / `global`）标注每行阈值来源，运营可据此逐步填充 `safeStock` 过渡到 per-SKU。
- 历史 SKU 的 `safeStock` 多为空，初期预警依赖全局回退阈值。

### 评论审核模型（Decision G4）

- 后置 Moderation：工作台列已发布评论，提供批量下架（置 `deleted=true`）/恢复（`deleted=false`）/批量回复（写 `adminContent`），复用既有字段，无 ORM 改动。
- 前置审核状态机（pending→approved/rejected）需新 `status` 字段，为 model-gap successor（见计划 Deferred）。

### 权限与审计

- 所有批量/导入导出/评论审核动作标注 `@Auth(roles="admin")`，仅管理员/运营可达。
- 批量动作写入 `LitemallLog` 管理员操作日志（共 N 行，成功 M 行）。

### 后台菜单结构

- `goods-manage`(300) 下新增子项：
  - `mall-goods-batch` 批量运营 → `mall/goods-ops/goods-batch.page.yaml`
  - `mall-goods-io` 导入导出 → `mall/goods-ops/goods-io.page.yaml`
  - `mall-stock-warning` 库存预警 → `mall/goods-ops/stock-warning.page.yaml`
  - `mall-comment-review` 评论工作台 → `mall/goods-ops/comment-review.page.yaml`

### 与其他 Owner Docs 的关系

- 商品批量运营语义边界（批量改价与单条 CRUD `syncRetailPrice` 派生口径隔离、跨实体经 `I*Biz`）见 `product-catalog.md`。
- 库存语义化阈值复用 P38 全局配置族（`mall_stock_threshold_*`），见本文件「库存语义化」段。
- 本文件仅持有「后台菜单结构、运营可达性、批量口径」语义。

## 自提门店管理（P31）

### 业务角色

- 自提门店管理为运营提供自提门店（`LitemallPickupStore`）的 CRUD 与启停能力，以及门店核销工作台。
- 配送方式扩展/自提核销的业务语义（状态流转、四项隔离、核销码、售后路径）见 `order-and-cart.md`「配送方式扩展/自提核销」。

### 运营动作

| 动作 | BizModel 方法 | 语义 |
| ---- | ------------- | ---- |
| 门店 CRUD | `LitemallPickupStore__save/update/delete` | 标准 `CrudBizModel`，门店仅作提货点，不持有独立库存 |
| 门店启停 | 门店 `status`（0=启用/1=停用） | 停用门店不再出现在结算页 `listActiveStores`，但不影响已下单的自提订单核销 |
| 启用门店列表 | `LitemallPickupStore__listActiveStores` | `@BizQuery`：返回 status=启用的门店（含经纬度/营业时间），结算页选店消费者 |
| 自提核销 | `LitemallOrder__verifyPickupOrder` | `@BizMutation @Auth(roles="admin")`：按 pickupCode 核销，推进 401 + 复制 confirm 副作用 |

### 权限与审计

- 门店 CRUD 复用既有 RBAC（管理员可达）；核销工作台标注 `@Auth(roles="admin")`，仅管理员/门店员可达。
- 本基线不引入门店员独立角色（roadmap 未列门店员 RBAC）。

### 后台菜单结构

- `mall-manage`(200) 下新增子项：
  - `mall-pickup-store` 自提门店管理 → `LitemallPickupStore/LitemallPickupStore.view.xml`
  - `mall-pickup-verify` 自提核销工作台 → `mall/order-ops/pickup-verify.page.yaml`

### 与其他 Owner Docs 的关系

- 自提下单分支、核销状态流转、四项隔离、售后路径的业务语义见 `order-and-cart.md`「配送方式扩展/自提核销」。
- 本文件仅持有「后台菜单结构、门店 CRUD/启停、核销可达性」语义。

## 与其他 Owner Docs 的关系

系统配置域向主链路提供配置开关与运营消费能力：

| 交接点 | 方向 | 目标文档 | 说明 |
|--------|------|---------|------|
| 运费/包邮门槛 | → 出 | `order-and-cart.md` | 结算时运费计算依据 |
| 订单超时/自动收货时长 | → 出 | `order-and-cart.md` | 系统取消、系统收货的触发阈值 |
| 支付开关 | → 出 | `order-and-cart.md` | 决定真实模式与示例模式分流 |
| 地址数量上限 | → 出 | `user-and-address.md` | 每用户地址上限 |
| 营销过期任务 | ← 入 | `marketing-and-promotions.md` | 优惠券、团购、评价窗口过期处理 |
| 优惠券 DIY 投放 | ← 入 | `marketing-and-promotions.md` | 后台投放配置（商品范围/限领/有效期）的业务语义由营销域持有 |
| 事件通知/报表 | ← 入 | `order-and-cart.md` / `marketing-and-promotions.md` | 支付、发货、退款等事件转化为通知与报表 |
| 文件素材 | → 出 | `product-catalog.md` / `user-and-address.md` | 商品图片、品牌图片、头像依赖文件存储 |

全局流程视图见 `flow-overview.md`。
