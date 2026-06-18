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
