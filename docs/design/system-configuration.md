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

### 业务规则

- 支持的配置项由应用基线预先定义。
- 配置变更应尽快对新进入评估的业务流程生效。
- 运营配置只能由具备相应职责的管理角色编辑。

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
- 后台统计看板页面：`app-mall-web/.../mall/stat/stat-dashboard.page.yaml`
- 后台菜单入口：`stat-manage`（`app-mall.action-auth.xml` 已开放）
- 复杂报表导出（PDF/Excel）需 nop-report 引擎，当前不在范围内

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
