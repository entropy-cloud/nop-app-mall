# 用户与地址业务设计

## 目的

说明 `nop-app-mall` 中商城用户身份、后台用户角色、个人资料行为、地址管理和地区数据使用方式。

## 边界

- 本文档负责用户侧和管理员侧账号语义、资料规则以及地址行为。
- 持久化模型结构、字段集和字典定义以 `model/*.orm.xml` 和 `model/nop-auth-delta.orm.xml` 为准。
- 平台认证实现细节属于 `docs/architecture/`。

## 领域概览

应用区分两类业务用户：

- 商城用户：浏览、购买，并维护自己的资料和地址
- 后台用户：负责店铺运营和业务数据管理

地址领域用于支持结算时的配送信息和地区选择。

## 用户实体架构

### 单实体模型：NopAuthUser（通过 Delta 扩展）

本应用不维护独立的商城用户实体。所有用户（商城用户和后台管理员）统一使用平台 `NopAuthUser` 实体，通过 Delta 扩展添加商城特有字段。

**决策依据：**

- 原 litemall 项目的 `LitemallUser` 与平台 `NopAuthUser` 字段高度重复（userName/username, nickName/nickname, avatar, gender, birthday, status, password 共 9 个字段）
- `LitemallRole`、`LitemallPermission`、`LitemallUserRole` 已由平台 `NopAuthRole`、`NopAuthResource`、`NopAuthUserRole` 覆盖
- 单实体模型消除了双实体注册、状态同步、密码一致性等复杂问题

### Delta 扩展字段

在 `model/nop-auth-delta.orm.xml` 的 `NopAuthUserEx` 实体中添加以下商城特有字段（propId >= 102）：

| 字段 | 类型 | 用途 | 字典 |
|------|------|------|------|
| `lastLoginTime` | datetime | 最近一次登录时间 | — |
| `lastLoginIp` | varchar(63) | 最近一次登录 IP | — |
| `userLevel` | int | 用户等级 | mall/user-level |
| `sessionKey` | varchar(100) | 微信会话 KEY（预留字段，微信登录当前不在产品范围内） | — |

**不需要添加的字段（已由 NopAuthUser 覆盖）：**

| LitemallUser 原字段 | NopAuthUser 对应字段 | 说明 |
|---------------------|---------------------|------|
| username | userName | 用户名 |
| password | password | 密码（由平台 `IPasswordEncoder` 管理） |
| nickname | nickName | 昵称 |
| avatar | avatar | 头像 |
| gender | gender | 性别（dict: auth/gender） |
| birthday | birthday | 生日 |
| mobile | phone | 手机号 |
| status | status | 状态（使用平台语义：0=禁用, 1=正常） |
| weixinOpenid | openId | 微信标识（平台 `openId` 字段） |

### 用户类型区分

通过 `NopAuthUser.userType` 区分用户类别：

| userType | 含义 | 说明 |
|----------|------|------|
| 0 | 系统内置 | 平台默认管理员 |
| 1 | 普通用户 | 商城注册用户 |
| 2 | 管理员 | 后台管理用户 |

### 已删除字段

- `clientId` 列已删除：商城应用不使用 nop-auth 原生的 clientId 设备追踪功能，该字段仅用于 nop-auth 内部的多端登录控制场景。标记为 `tagSet="del"` 在 ORM 中已逻辑删除。

### 被消除的实体

以下 litemall 原项目实体不再使用，由平台实体覆盖：

| 原实体 | 替代方案 | 说明 |
|--------|----------|------|
| `LitemallUser` | `NopAuthUserEx`（Delta 扩展） | 字段通过 Delta 列和平台字段覆盖 |
| `LitemallRole` | `NopAuthRole` | 平台角色 |
| `LitemallPermission` | `NopAuthResource` | 平台权限资源 |
| `LitemallUserRole` | `NopAuthUserRole` | 平台用户-角色映射 |
| `LitemallAdmin` | `NopAuthUser`（userType=2） | 管理员用户 |

## 会员等级体系

> 持久化字段、等级字典与等级规则表以 `model/app-mall.orm.xml`、`model/nop-auth-delta.orm.xml` 为准。本节描述等级规则、升级/降级机制、权益配置和会员价（vipPrice）的业务语义。

### 等级模型

- 用户当前等级记录在 `NopAuthUserEx.userLevel`（Delta 扩展字段，字典 `mall/user-level`：0=普通用户，1=VIP，2=高级VIP）。
- 等级规则由独立实体 `LitemallMemberLevel` 维护（每个等级一行规则），包含升级阈值、保级（降级）阈值、权益配置与排序。等级值（`level`）与 `mall/user-level` 字典值对齐。

### 升级条件指标

- **指标抉择：累计消费金额**（用户已支付/已收货订单的 `actualPrice` 累加），而非成长值或累计订单数。
- 抉择理由：累计消费直接反映用户商业价值，且可由订单历史聚合得到，**无需在 `NopAuthUserEx` 新增 `growthValue` 字段**（成长值需要 P27 积分/签到等多源贡献，而 P27 为本体系的下游且尚未实现业务逻辑，采用成长值会引入空字段与未接线依赖）。累计订单数对客单价差异不敏感。
- 升级判定：`evaluateUserLevel` 取用户累计消费，命中「累计消费 ≥ 等级 `upgradeThreshold`」的最高等级，更新 `userLevel`。等级阈值以 `LitemallMemberLevel.upgradeThreshold` 为准（后台可配）。

### 降级机制（必实现）

- **机制抉择：周期内累计未达保级阈值则降一级**（备选 A）。
- 评估周期：自然滚动周期（最近 N 天，N 由系统配置 `mall_member_eval_period_days` 控制，默认 365）。
- 保级阈值取当前等级的 `LitemallMemberLevel.downgradeThreshold`：若周期内累计消费 < 该阈值，则 `userLevel` 下调一级（普通用户为最低，不再下调）。降级按当前等级逐级进行，不跨级直降。
- 周期重置/触发：通过后台手动触发的 `@BizMutation`（`downgradeExpiredLevels`）批量评估。nop-job-local 已装配（`scheduler.yaml` 6+ 个 scheduled job），可由定时任务调用同一 BizMutation；当前 `downgradeExpiredLevels` 保留手工入口。
- 降级不可跳过：roadmap Phase 26 交付范围明确包含「降级机制（周期内未达标降级）」。

### 会员价（vipPrice）

- 会员价为 **SKU 单价级**优惠，作用于订单 `goodsPrice` 汇总（每行 `min(retailPrice, vipPrice) × number`），**不是订单级减项**。
- 会员价适用对象：`userLevel >= 1`（VIP / 高级VIP）的用户；`userLevel = 0`（普通用户）按零售价购买。`LitemallGoodsProduct.vipPrice` 为可空字段，空或为 0 表示该 SKU 无会员价（会员仍按零售价）。
- 与满减（P15 `promotionPrice`）的层位区分与计算顺序见 `order-and-cart.md`「价格计算顺序约定」：先按会员价汇总 `goodsPrice` → 再以 `goodsPrice` 判定满减门槛。
- 订单快照：订单提交时将生效会员价快照到 `LitemallOrderGoods.vipPrice`（生效的会员单价；非会员或无会员价时为空），`LitemallOrderGoods.price` 记录实际成交单价（`min(retail, vip)`）。快照支撑退款额计算与「会员价」展示标签。

### 权益配置

- 权益项以 `LitemallMemberLevel.benefits`（JSON）配置；自动发放经 LitemallSystem key/value 开关控制。
  - 专属价：已由 `vipPrice` 价格机制落地（见上）。
  - 专属券：`LitemallCoupon.minMemberLevel` 控制等级准入（≥该等级方可领取/被发放，0=全员可领）；等级提升自动发券由 `mall_benefit_level_coupon_{level}` 配置 couponId，受 `mall_benefit_level_up_enabled` 开关控制。
  - 生日礼包：nop-job 定时任务 `dispatch-birthday-coupons` 扫描当日生日用户（基类 `NopAuthUser.birthday`），发放 `mall_benefit_birthday_coupon` 配置的券，受 `mall_benefit_birthday_enabled` 开关控制；同年幂等。

### 自动权益发放（D3 自动升级触发）

- 订单确认收货（`confirm` / `confirmExpiredOrders`）触发 `evaluateUserLevel`：基于累计消费（含本单）重新评估等级，命中升级阈值则自动升级并触发等级提升权益发放。
- hysteresis（防等级抖动）：仅自动升级，降级仍由 `downgradeExpiredLevels` 周期任务负责。
- 等级提升权益发放：`ensureUserLevel` 检测 upgrade（target > current）→ `dispatchLevelUpBenefit` 发放配置的专属券；发放计入 `limit`（D4），超限静默跳过；发放成功触发站内信通知。

### 个人中心等级展示

- 提供等级 + 进度查询：返回当前等级、累计消费、下一级等级、距下一级还差多少消费。
- 进度计算依据「升级条件指标」的累计消费与各等级 `upgradeThreshold`。

### 与其他域的交接

| 交接点 | 方向 | 目标文档 | 说明 |
|--------|------|---------|------|
| 会员价价格影响 | → 出 | `order-and-cart.md` | vipPrice 作用于 goodsPrice 汇总层，影响满减门槛命中（计算顺序见该文档） |
| 累计消费来源 | ← 入 | `order-and-cart.md` | 累计消费由已支付/已收货订单的 actualPrice 聚合 |
| 积分/签到贡献 | ← 入 | `marketing-and-promotions.md`、`wallet-and-assets.md`（P27/P28） | 本基线升级指标为累计消费；P27 积分体系若改用成长值指标，须与本体系协调指标源（successor） |

## 商城用户管理

### 支持的用户能力

- 注册与登录
- 查看当前个人资料
- 更新个人资料信息
- 修改密码
- 管理收货地址
- 查看个人订单及订单状态

### 业务规则

- 只有已认证用户才能查看或修改自己的资料和地址。
- 被禁用用户（`status=0`）不得继续正常使用商城。
- 资料更新过程不得暴露或返回敏感凭证数据。
- 微信等附加登录渠道在启用时，必须保持与原有商城用户身份和账号安全语义一致。

## 注册与认证

### 业务基线

- 支持用户名/密码登录。
- 支持商城用户自助注册（创建 `NopAuthUser` + `userType=1`）。
- 管理员可通过后台创建账号（`userType=2`）。
- 注册使用平台 `NopAuthUserBizModel.defaultPrepareSave()` 管道处理密码哈希、salt、userId 生成。

### 附加登录渠道

- 微信小程序登录和自动注册不在当前产品范围内（见 `docs/backlog/implementation-roadmap.md` Phase 1 明确「不在范围内：微信登录、外部登录渠道」）。产品定位为 H5/Web 商城，未交付微信小程序前端；Phase 14 仅覆盖微信支付（Native 扫码），不含微信登录。
- `NopAuthUser.openId` 与扩展字段 `sessionKey` 作为预留保留，未来启用微信登录时可直接复用，无需再改模型。

### 密码重置与验证码

- 找回密码通过手机号验证码：`LoginApiExBizModel.sendResetCode` 发码、`resetPassword` 验码并改密（`app-mall-delta`，对 nop-auth 的 Delta 扩展）。
- 验证码记录持久化于 `litemall_reset_code`（`LitemallResetCode`，逻辑删除）。发送频率与有效期由 `LoginApiExBizModel` 常量控制。
- **清理路径（惰性 + 定期，互补）：**
  - 惰性清理：发送新码时删除该手机号旧码（`sendResetCode`），验证成功/过期时删除已用码（`resetPassword`）。
  - 定期清理（successor of deferred「验证码过期记录的定期清理」）：覆盖惰性路径未触及的「已发送但从未验证、也未重发」的累积记录。定时任务 `cleanup-expired-reset-codes`（每日，入口 `MallJobInvoker.cleanupExpiredResetCodes()` → `LitemallResetCodeBizModel.cleanupExpiredResetCodes`）逻辑删除超过保留期的记录（`addTime < now - mall_reset_code_retention_days`，缺省 **7 天**）。单轮限 500 条，超出顺延下一轮；与惰性清理一致采用逻辑删除（`deleteEntity`，`deleted=true`）。
  - 抉择：逻辑删除（与既有惰性清理一致，避免引入第二种删除语义）；保留期缺省 7 天（验证码为短生命周期临时数据）；ORM 管道批量查询逐条删除（参照 `cancelExpiredOrders`/`expirePoints` 先例）。

## 地址管理

### 业务规则

- 每个用户可以维护多条收货地址，数量上限由系统配置控制。
- 其中一条地址作为默认收货地址。
- 修改默认地址后，系统中必须仍然且只能存在一条默认地址。
- 用户只能管理自己名下的地址。
- 地址数据必须足以支持配送，包括收件人、联系方式、地区和详细地址。
- `LitemallAddress` 的 `user` 关联指向 `NopAuthUser`（而非已消除的 `LitemallUser`）。

### 支持行为

- 新增地址。
- 编辑地址。
- 删除地址。
- 列表展示地址，并清晰标识默认地址。
- 设置或切换默认地址。

## 地区数据

### 业务角色

- 地区数据用于支持级联地址选择。
- 地区数据属于参考数据，而不是用户自行维护的业务内容。
- 地区层级必须足够稳定，以支持地址录入和配送展示。

## 后台用户管理

### 业务角色

- 超级管理员拥有完整店铺管理权限。
- 管理员负责商品管理、订单管理等运营范围。

### 支持行为

- 管理后台账号和角色分配。
- 按运营职责应用对应的权限边界。
- 记录重要管理员操作，满足审计和排障需要。

## 用户标签与黑名单

> 持久化字段、索引与唯一键以 `model/app-mall.orm.xml` 为准。本节描述用户运营工作台（P20）的标签/黑名单建模决策与业务语义。

### 建模决策

- **抉择：标签与黑名单各自独立建表**，不复用 `NopAuthUser.status` 或 `NopAuthUserEx` 的 JSON/布尔扩展列。
  - `LitemallUserTag`（表 `litemall_user_tag`，列 id/userId/tag/name/addTime/updateTime/deleted，索引 `(userId,tag)`）：多对多自由打标，一个用户可持有多条标签记录。`tag` 为运营维护的标签码（自由文本，去重），`name` 为展示名。
  - `LitemallUserBlacklist`（表 `litemall_user_blacklist`，列 id/userId/reason(511)/operatorId/addTime/updateTime/deleted，唯一键 `userId`）：每用户至多一条封禁记录，记录原因与操作员便于审计。封禁/解禁时随表增删。
- **被否备选 A（在 `NopAuthUserEx` Delta 加 `tags` JSON + `blacklisted` bool）：** JSON 标签无法高效分群查询；黑名单审计需要操作员/原因/时间独立记录，布尔标记承载不了。
- **被否备选 B（黑名单直接复用 `NopAuthUser.status=disabled` 不建表）：** status 是平台通用态，无法承载商城黑名单的封禁原因与操作审计；运营工作台需要可追溯的封禁记录。
- 残留风险：双新表增加 regen 量（model-first 流程已接受）。

### 业务语义

- 封禁/解禁：杠杆平台 `NopAuthUser.status`（0=禁用、1=正常）并同步写 `LitemallUserBlacklist`，二者保持一致。封禁用户登录由平台 status 机制拒绝，下单由订单 `submit` 增加 `ERR_USER_BANNED` 守卫拒绝。
- 标签：运营对用户打标/去标，并按标签分群查询（`findUsersByTag`）。本基线为简单标签集合。
- **算法化分群（P20 successor）**：`LitemallOrder__getSegmentMembers(segmentType, segmentValue, page, pageSize)` 在手工标签分群基础上新增算法化维度，按 `segmentType=rfm`（8 段之一）/ `segmentType=lifecycle`（新客/活跃/沉睡/流失）圈选用户成员列表（`PageBean<SegmentMemberBean>`：userId / rfmSegment / lifecycleStage / orderCount / totalAmount / lastPayTime）。all-time / 当前快照口径与 `getUserPortrait` 一致，分类逻辑与 P19 同源（详见 `system-configuration.md`「Per-User 画像口径」）。前端 `LitemallUserTag/segment.page.yaml` 提供三 Tab 并存：手工标签 / 按 RFM 段 / 按生命周期。无消费用户不计入算法化分群命中段。
- **用户详情算法画像（P20 successor）**：用户详情页 `mall/user-ops/user-detail.page.yaml` 在「基本信息」面板下新增「算法画像（RFM + 生命周期）」面板，调用 `@query:LitemallOrder__getUserPortrait(userId)`，展示 RFM 段 + 生命周期阶段 + R/F/M 原始值 + 首末单时间；无消费用户展示「该用户暂无支付订单，仅有手工标签画像」。与既有「订单与消费 / 积分与优惠券 / 浏览与反馈」面板并存。算法画像口径为 all-time / 当前快照（与 P19 报表 period 口径不同），分类逻辑同源；详见 `system-configuration.md`「Per-User 画像口径」。
- 权益手工发放：运营工作台提供用户级直达入口（手工调级 `setUserLevel`、手工发券 `dispatchCoupon`、手工加积分复用既有 `adjustPoints`），覆盖 P26/P32 自动化发放 deferred 的手动路径。

### 与其他 Owner Docs 的关系

- 封禁状态对下单的影响见 `order-and-cart.md`。
- 会员等级调级语义见本文档「会员等级体系」；优惠券/积分发放语义见 `marketing-and-promotions.md`、`wallet-and-assets.md`。

## 与其他 Owner Docs 的关系

用户与地址域向主链路提供前置条件：

| 交接点 | 方向 | 目标文档 | 说明 |
|--------|------|---------|------|
| 认证前置 | → 出 | `order-and-cart.md` | 购物车、结算、订单、售后均要求已认证用户 |
| 收货地址归属 | → 出 | `order-and-cart.md` | 结算要求地址归属于当前用户 |
| 地址数量上限 | ← 入 | `system-configuration.md` | 每用户地址数量上限由系统配置控制 |
| 权限语义 | ← 入 | `roles-and-permissions.md` | 后台用户权限边界进一步约束 |
| ORM Delta 扩展 | — | `model/nop-auth-delta.orm.xml` | 商城特有字段通过 Delta 扩展 NopAuthUser |

全局流程视图见 `flow-overview.md`。
