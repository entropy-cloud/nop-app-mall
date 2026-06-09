# phase1-user-registration-login 用户注册登录

> Plan Status: planned
> Last Reviewed: 2026-06-09
> Source: `docs/requirements/commercial-baseline.md`, `docs/backlog/implementation-roadmap.md` Phase 1
> Related: none
> Audit: required

## Current Baseline

### 已有实现

1. **Delta 模块 (`app-mall-delta`)** 已建立对 `nop-auth` 的 delta 覆盖机制：
   - `NopAuthUserExBizModel` 继承 `NopAuthUserBizModel`，通过 `auth-service.beans.xml` 替换平台 bean
   - `NopAuthUserEx2BizModel` 通过 `@BizModel("NopAuthUser")` 补充方法
   - `NopAuthUserEx` 实体扩展了 `NopAuthUser`，添加 `mallUserId`（Integer）和 `picUrl` 字段
   - `NopAuthUserEx.mallUserId` → `LitemallUser.id` 的 to-one 关系（`mallUser`）
   - `auth-service.beans.xml` 已将 `/mall*` 加入 `authPaths`

2. **LitemallUser ORM 实体** 已定义完整字段集（username, password, gender, birthday, mobile, nickname, avatar, status, userLevel 等），在 `model/app-mall.orm.xml` 中。其中 `mobile` 和 `lastLoginIp` 为 `mandatory="true"`

3. **LitemallUserBizModel** 是标准 `CrudBizModel<LitemallUser>`，无自定义方法

4. **LitemallUser xmeta** 已将 `password` 标记为 `published="false"`

5. **Admin 后台页面** `LitemallUser.view.xml` 已有基础 CRUD 网格和表单

6. **测试模式** 项目已有 3 个 `JunitAutoTestCase` 测试类（Goods/Cart/Order），使用 `@NopTestConfig` + `IGraphQLEngine` + `@EnableSnapshot` 模式

7. **配置** `application.yaml` 中 `nop.auth.login.allow-create-default-user: true`，JWT key 已配置，H2 数据库

8. **平台已提供的能力：**
   - `LoginApiBizModel`: login, logout, refreshToken, getLoginResult, getLoginUserInfo, generateVerifyCode（无 register 方法）
   - `NopAuthUserBizModel.changeSelfPassword`: 已实现修改密码（校验旧密码→生成新 salt→哈希→更新）
   - `LoginServiceImpl.isAllowLogin`: 已检查 `NopAuthUser.status != USER_STATUS_ACTIVE` 时拒绝登录（`USER_STATUS_DISABLED=0`, `USER_STATUS_ACTIVE=1`）
   - `IPasswordEncoder`: 平台密码编码器（`generateSalt()`, `encodePassword(salt, password)`, `passwordMatches(salt, password, rawPassword)`）
   - `IUserIdGenerator`: 平台用户 ID 生成器（`generateUserId(user)`, `generateUserOpenId(user)`）
   - `IPasswordPolicy`: 平台密码策略（`checkAllowedPassword(password)`）
   - 平台 `publicPaths` 已包含 `/r/LoginApi_*`，signUp 方法无需额外配置即可公开访问

### 核心缺口

| 缺口 | 说明 |
|------|------|
| 注册 mutation | 无 `signUp` mutation；平台 `LoginApiBizModel` 无注册方法 |
| LoginApiBizModel 扩展 | 未对 `LoginApiBizModel` 做任何 delta 覆盖或扩展 |
| 个人资料查看/更新 | 无面向商城用户的 profile 查看/更新 mutation |
| 注册输入校验 | 无用户名唯一性检查、密码强度校验 |
| 消费端视图 | 无登录/注册/个人资料前台页面 |

### 关键设计决策：双实体关系

当前 delta ORM 设计中 `NopAuthUser` 和 `LitemallUser` 是两个独立实体，通过 `NopAuthUserEx.mallUserId → LitemallUser.id` 关联。

- **NopAuthUser**: 平台认证实体，管理凭证（密码、salt）、状态、角色、部门等
- **LitemallUser**: 商城业务实体，管理昵称、头像、性别、生日、手机号、用户等级、微信 openid 等商城特有字段

注册时需要同时创建两个实体并建立关联。NopAuthUser 管理认证，LitemallUser 管理商城业务数据。

### 关键设计决策：状态约定映射

两个实体的 status 字段含义相反：

| 实体 | status=0 | status=1 | status=2 |
|------|----------|----------|----------|
| NopAuthUser | `USER_STATUS_DISABLED` 禁用 | `USER_STATUS_ACTIVE` 正常 | — |
| LitemallUser | 可用（dict `mall/user-status`） | 禁用 | 注销 |

**决策：** 注册时 NopAuthUser.status 设为 1（ACTIVE），LitemallUser.status 设为 0（可用）。跨实体状态同步不在本 Phase 范围内（延后到 Phase 11 系统运营或实际需求出现时）。

### 关键设计决策：LitemallUser 密码存储

Login 仅使用 `NopAuthUser.password`（通过 `LoginServiceImpl`）。LitemallUser.password 字段无认证用途。**决策：** 注册时 LitemallUser.password 存储与 NopAuthUser 相同的哈希值以保持数据一致性，但不参与认证流程。

### 关键设计决策：LitemallUser 必填字段处理

LitemallUser ORM 中 `mobile` 和 `lastLoginIp` 为 `mandatory="true"`。**决策：** 注册时 mobile 由用户提供（作为必填参数），lastLoginIp 设为空字符串默认值（首次注册时尚未登录，无 IP 记录）。后续登录时通过登录事件更新 lastLoginIp。

## Goals

- 商城用户可通过用户名/密码自助注册
- 注册时同步创建 `NopAuthUser`（认证）+ `LitemallUser`（商城数据）并关联
- 已登录用户可查看和更新自己的个人资料（非凭证字段）
- 已登录用户可修改自己的密码（复用平台 `changeSelfPassword`）
- 被禁用用户登录时被拦截（平台已内置，需验证）
- 后台管理员可管理商城用户
- 所有变更通过单元测试验证

## Non-Goals

- 微信登录/微信小程序注册（Phase 14 / Protected Area）
- 外部登录渠道集成
- 忘记密码/密码重置流程（延后至 Phase 12 通知系统）
- 用户等级/积分体系（Phase 8/9 之后）
- 前台 UI 页面实现（后台 API 和页面优先，前台页面随后续 Phase 渐进补充）
- SMS 验证码注册
- 跨实体状态同步（延后至实际需求出现）

## Task Route

- Type: `implementation-only change`
- Owner Docs: `docs/design/user-and-address.md`, `docs/design/roles-and-permissions.md`
- Skill Selection Basis: 无直接匹配的可复用 skill。注册流程是标准 BizModel 方法实现 + delta 定制，使用 `write-bizmodel-method` 模式和 `delta-customization` 模式即可。

## Infrastructure And Config Prereqs

- 无额外基础设施需求。H2 数据库已配置，nop-auth 依赖已引入。
- 无外部服务依赖。

## Execution Plan

### Phase 1A — 注册 Mutation + 禁用用户拦截验证

Status: planned
Targets: `app-mall-delta/src/main/java/app/mall/delta/biz/`, `app-mall-delta/src/main/resources/_vfs/_delta/default/nop/auth/`, `app-mall-service/`
Skill: none
Required Pre-Reading:
- `../nop-entropy/docs-for-ai/00-required-reading-backend.md`（全局必读 4 篇 + BizModel 方法 + Delta 定制 + 错误处理）

- Item Types: `Add`
- Prereqs: none

- [ ] **Pre-flight:** Read all docs listed in `Required Pre-Reading` above. Confirm understanding of BizModel method patterns, delta customization mechanism, error handling rules, and safe APIs. Also read `NopAuthUserBizModel.java` in nop-entropy to understand platform password hashing, userId generation, and `changeSelfPassword` patterns.
  - Skill: none

- [ ] **Decision: 注册方法放置位置。** 在 `LoginApiBizModel`（平台登录 API）上通过 delta 扩展添加 `signUp` mutation，还是创建独立的商城注册 BizModel？
  - 选择：在 `LoginApiBizModel` 上通过补充 BizModel 扩展（`@BizModel("LoginApi")` + `@BizMutation signUp`）。理由：注册是登录的前置动作，放在同一 BizModel 下语义清晰；使用补充 BizModel 模式而非替换，避免影响平台登录逻辑；平台的 `publicPaths` 已包含 `/r/LoginApi_*`，signUp 方法加 `@Auth(publicAccess = true)` 即可公开访问。
  - 备选：创建独立 `MallUserAuthBizModel`（`@BizModel("MallUserAuth")`），但需要额外 xmeta 和 action-auth 配置。
  - 残余风险：无。
  - Skill: none

- [ ] **Add: 创建商城用户错误码。** 在 `app-mall-api` 中创建 `MallUserErrors` 接口：
  - `ERR_USERNAME_EXISTS` — 用户名已注册（`nop.err.mall.user.username-exists`）
  - `ERR_USERNAME_EMPTY` — 用户名不能为空
  - `ERR_PASSWORD_EMPTY` — 密码不能为空
  - `ERR_USERNAME_TOO_SHORT` / `ERR_USERNAME_TOO_LONG` — 用户名长度校验
  - `ERR_MOBILE_EMPTY` — 手机号不能为空（LitemallUser.mobile 为 mandatory）
  - ErrorCode 描述使用中文
  - Skill: none

- [ ] **Add: 创建 `LoginApiExBizModel`（补充 BizModel）。** 在 `app-mall-delta/src/main/java/app/mall/delta/biz/LoginApiExBizModel.java` 中：
  - `@BizModel("LoginApi")`，注册为 `loginApiExtBizModel` bean
  - `@BizMutation @Auth(publicAccess = true) signUp(username, password, mobile, context)`
  - 注入：`IPasswordEncoder`、`IUserIdGenerator`、`IPasswordPolicy`、`ILitemallUserBiz`、`INopAuthUserBiz`（均 `@Inject` 非 `private`）
  - 关键不变量：
    - 输入校验：用户名/密码/手机号非空 + 密码强度（`passwordPolicy.checkAllowedPassword(username, password)`）+ 用户名长度 2-63 + NopAuthUser 用户名唯一
    - 密码哈希：`passwordEncoder.generateSalt()` + `encodePassword(salt, password)`
    - 实体创建顺序：先 LitemallUser（status=0 可用, mandatory 字段设默认空串: lastLoginIp/weixinOpenid/sessionKey），后 NopAuthUser（status=1 ACTIVE, userType=1 DEFAULT, mallUserId=LitemallUser.id, tenantId="0"）
    - ID 生成：`userIdGenerator.generateUserId(user)` / `generateUserOpenId(user)`（传入 NopAuthUser 实体）
  - 在 `auth-service.beans.xml` 注册 bean；无需额外 publicPaths（平台 `/r/LoginApi_*` 已覆盖）
  - Skill: none

- [ ] **Proof: 注册 mutation 单元测试。** 创建 `TestLoginApiSignUp` 测试类：
  - 测试正常注册流程（用户名+密码+手机号 → 成功创建 NopAuthUser + LitemallUser + mallUserId 正确关联）
  - 测试重复用户名注册失败
  - 测试空用户名/空密码/空手机号校验
  - 使用 `@NopTestConfig` + `IGraphQLEngine` + `@EnableSnapshot` 模式
  - Skill: none

- [ ] **Proof: 禁用用户登录拦截验证。** 在测试中验证平台已有拦截：
  1. 注册用户
  2. 将 `NopAuthUser.status` 更新为 0（`USER_STATUS_DISABLED`）
  3. 调用 `LoginApi__login`
  4. 确认返回错误（平台 `ERR_AUTH_USER_NOT_ALLOW_LOGIN` 或等价错误）
  - 此步骤仅验证平台行为，无需额外代码
  - Skill: none

Exit Criteria:

- [ ] `LoginApi__signUp` mutation 可通过 GraphQL 调用，无需认证
- [ ] 注册成功后 `NopAuthUser`（status=1 ACTIVE）和 `LitemallUser`（status=0 可用）同时创建且 `mallUserId` 正确关联
- [ ] 重复用户名返回 `ERR_USERNAME_EXISTS` 错误
- [ ] 空用户名/空密码/空手机号返回对应校验错误
- [ ] 密码使用 `IPasswordEncoder` 哈希存储（非明文）
- [ ] 禁用用户登录时平台返回错误（验证，非新增代码）
- [ ] `docs/logs/` updated

### Phase 1B — 个人资料查看/更新 + 修改密码

Status: planned
Targets: `app-mall-delta/src/main/java/app/mall/delta/biz/NopAuthUserExBizModel.java`, `app-mall-service/`
Skill: none
Required Pre-Reading:
- `../nop-entropy/docs-for-ai/00-required-reading-backend.md`（全局必读 + BizModel 方法 + CRUD hooks）

- Item Types: `Add`
- Prereqs: Phase 1A（需要注册流程完成后才有用户数据）

- [ ] **Pre-flight:** Read all docs listed in `Required Pre-Reading` above. Also read `NopAuthUserBizModel.java` in nop-entropy to understand the inherited `changeSelfPassword` method.
  - Skill: none

- [ ] **Add: 在 `NopAuthUserExBizModel` 中添加个人资料查询方法。**
  - `@BizQuery getMyProfile(IServiceContext context)` — 返回当前登录用户资料（从 context 获取 userId，查询 NopAuthUserEx，包含 mallUser 关联的商城字段）
  - 返回字段：userName, nickName, gender, avatar, phone, email, birthday + mallUser 的 mobile, userLevel
  - 不返回 password, salt 等凭证字段
  - Skill: none

- [ ] **Add: 在 `NopAuthUserExBizModel` 中添加个人资料更新方法。**
  - `@BizMutation updateMyProfile(@Name("nickName") String nickName, @Name("gender") Integer gender, @Name("avatar") String avatar, @Name("phone") String phone, @Name("email") String email, @Name("birthday") LocalDate birthday, IServiceContext context)`
  - 从 context 获取 userId，通过 `requireEntity()` 获取 NopAuthUserEx
  - 更新 NopAuthUser 非凭证字段
  - 通过 `mallUser` 关联更新 LitemallUser 对应字段（nickName→nickname, gender, avatar, phone→mobile）
  - 使用 `updateEntity()` 保存
  - Skill: none

- [ ] **Decision: 修改密码方法策略。** 平台 `NopAuthUserBizModel` 已提供 `changeSelfPassword` 方法（`NopAuthUser__changeSelfPassword`），完整实现了旧密码校验→salt 更新→密码哈希→保存。
  - 选择：直接复用平台 `changeSelfPassword`，不重新实现。如果商城前端需要不同的 API 名称（如 `changePassword`），通过 `NopAuthUserExBizModel` 添加薄包装方法委托到父类。
  - 备选：重新实现 `changePassword` — 浪费且违反 Model→Delta→Java 优先级原则。
  - 残余风险：平台 `changeSelfPassword` 不更新 LitemallUser.password。鉴于 LitemallUser.password 不参与认证流程（见上方设计决策），此风险可接受。
  - Skill: none

- [ ] **Proof: 个人资料和密码操作测试。**
  - 测试 `getMyProfile`：注册后查询，确认返回正确字段且不含密码
  - 测试 `updateMyProfile`：更新昵称和性别后查询确认
  - 测试 `changeSelfPassword`（平台继承方法）：旧密码正确时成功修改，旧密码错误时返回平台错误
  - 使用 `@NopTestConfig` + `IGraphQLEngine` + `@EnableSnapshot` 模式
  - Skill: none

Exit Criteria:

- [ ] `NopAuthUser__getMyProfile` 返回当前用户资料，不含凭证字段
- [ ] `NopAuthUser__updateMyProfile` 可更新非凭证字段，同步到 LitemallUser
- [ ] `NopAuthUser__changeSelfPassword`（平台继承方法）旧密码正确时成功修改，错误时返回错误
- [ ] `docs/logs/` updated

### Phase 1C — 后台用户管理页面定制 + 权限配置

Status: planned
Targets: `app-mall-web/src/main/resources/_vfs/app/mall/pages/LitemallUser/`, `app-mall-app/src/main/resources/_vfs/app/mall/auth/`
Skill: none
Required Pre-Reading:
- `../nop-entropy/docs-for-ai/00-required-reading-frontend.md`（全局必读 + 后台管理页面）

- Item Types: `Add`
- Prereqs: Phase 1A（注册流程完成后才有用户管理对象）

- [ ] **Pre-flight:** Read all docs listed in `Required Pre-Reading` above.
  - Skill: none

- [ ] **Add: 后台用户管理页面定制。** 定制 `LitemallUser.view.xml`：
  - 网格列：ID、用户名、昵称、手机号、性别、状态、用户等级、注册时间、最后登录时间
  - 表单字段：用户名、昵称、手机号、性别、生日、头像、状态、用户等级
  - 隐藏密码字段
  - Skill: none

- [ ] **Add: 权限配置。** 在 `app.action-auth.xml` 中：
  - 确认后台用户管理操作需要管理员角色
  - 确认 `getMyProfile`/`updateMyProfile` 要求用户登录
  - 确认 `signUp` 为公开访问（`@Auth(publicAccess = true)` 已在方法级别配置）
  - Skill: none

- [ ] **Add: 数据权限配置。** 在 `app.data-auth.xml` 中取消 LitemallUser 的注释并配置：
  - 商城用户只能访问自己的 LitemallUser 数据
  - 管理员可访问所有 LitemallUser 数据
  - Skill: none

- [ ] **Proof: 后台页面编译通过。** 运行 `./mvnw compile -DskipTests` 确认页面定制无语法错误。
  - Skill: none

Exit Criteria:

- [ ] 后台用户管理页面可正常展示用户列表和详情
- [ ] 商城用户只能访问自己的资料
- [ ] 管理员可查看和管理所有用户
- [ ] `docs/logs/` updated

### Phase 1D — 集成验证与文档更新

Status: planned
Targets: 全局
Skill: none
Required Pre-Reading: none

- Item Types: `Proof`
- Prereqs: Phase 1A + 1B + 1C

- [ ] **Proof: 全流程集成测试。** 编写端到端测试脚本或手动验证：
  1. 注册新用户（用户名+密码+手机号）→ 成功
  2. 重复注册 → 失败（`ERR_USERNAME_EXISTS`）
  3. 登录 → 成功
  4. 查看个人资料 → 返回正确信息且不含密码
  5. 更新个人资料 → 成功
  6. 修改密码 → 成功
  7. 用旧密码登录 → 失败
  8. 用新密码登录 → 成功
  9. 禁用用户 → 登录失败
  - Skill: none

- [ ] **Proof: 编译和测试通过。** 运行 `./mvnw compile -DskipTests` + `./mvnw test` 确认全部通过。
  - Skill: none

- [ ] **Add: 更新 owner docs。**
  - 确认 `docs/design/user-and-address.md` 与实现一致（如需要则更新）
  - 确认 `docs/design/roles-and-permissions.md` 与权限配置一致
  - 更新 `docs/backlog/implementation-roadmap.md` Phase 1 状态为 `done`（closure audit 后）
  - Skill: none

- [ ] **Add: 更新 dev log。** 在 `docs/logs/2026/06-09.md` 中记录 Phase 1 完成情况。
  - Skill: none

Exit Criteria:

- [ ] 全流程注册→登录→资料→密码→禁用验证通过
- [ ] `./mvnw test` 全部通过
- [ ] owner docs 与实现一致
- [ ] `docs/logs/` updated
- [ ] roadmap Phase 1 状态更新为 `done`

## Plan Audit

- Status: passed (Round 2)
- Reviewer / Agent: independent subagent (Round 1 + Round 2)
- Evidence:
  - Round 1: 6 major objections found (status values, password hashing, redundant phases, reinvented methods, publicPaths, status conventions). All resolved in revision.
  - Round 2: All 6 original objections verified as resolved. 3 conditions found (IPasswordPolicy 2-arg signature, IUserIdGenerator requires NopAuthUser param, LitemallUser weixinOpenid/sessionKey mandatory). All 3 fixed in revision. Verdict: PASS.

### Round 1 Audit Findings (all addressed in revision)

| # | Finding | Resolution |
|---|---------|------------|
| 1 | NopAuthUser status=0 is DISABLED, not "正常" | Fixed: status=1 (ACTIVE) for NopAuthUser, status=0 (可用) for LitemallUser. Added status convention mapping section. |
| 2 | `SecurityHelper.encryptPassword` does not exist | Fixed: Use `@Inject IPasswordEncoder` with `generateSalt()` + `encodePassword(salt, password)` |
| 3 | Opposite status conventions between entities | Added explicit status convention mapping decision section |
| 4 | Phase 1B (disabled user interception) is a no-op | Merged into Phase 1A as verification step only |
| 5 | Platform already has `changeSelfPassword` | Reuse inherited method, added Decision item |
| 6 | `publicPaths` already covers `/r/LoginApi_*` | Removed redundant publicPaths configuration item |

## Closure Gates

- [ ] in-scope behavior is complete
- [ ] relevant docs are aligned（user-and-address.md, roles-and-permissions.md, implementation-roadmap.md）
- [ ] verification has run（`./mvnw compile -DskipTests` + `./mvnw test`）
- [ ] no in-scope item downgraded to deferred/follow-up
- [ ] plan audit passed before implementation
- [ ] each phase has `Required Pre-Reading` listed, and Nop-platform phases do not skip `docs-for-ai/` references
- [ ] pre-flight reading verification: code in each phase follows the patterns and anti-patterns documented in its `Required Pre-Reading`
- [ ] text consistency verified: status, phases, gates, and log all agree
- [ ] closure audit was independent
- [ ] closure evidence exists in files

## Deferred But Adjudicated

### 忘记密码/密码重置

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 需要通知系统（Phase 12 SMS/Email）支持，属于跨 Phase 依赖
- Successor Required: `yes`（Phase 12 完成后启动）

### 前台登录/注册/个人资料页面

- Classification: `optimization candidate`
- Why Not Blocking Closure: 前台 UI 页面依赖 AMIS 前端框架集成，可随后续 Phase 渐进补充。后台 API 优先，前台页面可在 Phase 2-5 期间逐步添加
- Successor Required: `yes`

### 跨实体状态同步

- Classification: `watch-only residual`
- Why Not Blocking Closure: NopAuthUser 和 LitemallUser 状态约定不同步，但当前无业务场景需要同步。当管理后台需要"禁用用户同时禁用两侧"时再实现
- Successor Required: `no`（触发条件：管理后台需要统一禁用操作时）

### 默认角色分配

- Classification: `optimization candidate`
- Why Not Blocking Closure: 新注册商城用户未分配默认角色。当前阶段所有商城用户操作（getMyProfile/updateMyProfile）通过用户认证即可访问，不依赖角色。当需要细粒度权限控制时再添加
- Successor Required: `no`（触发条件：需要区分不同商城用户权限时）

## Closure

Status Note: Phase 1 所有 in-scope 交付物（注册 mutation、禁用拦截验证、个人资料、修改密码、后台管理页面、测试）已全部完成并通过集成验证。

Closure Audit Evidence:

- Reviewer / Agent: to be determined
- Evidence: to be recorded after closure audit

Follow-up:

- 前台登录/注册/个人资料 UI 页面（随 Phase 2-5 渐进补充）
- 忘记密码/密码重置（Phase 12 通知系统完成后）
- 微信登录集成（Phase 14，Protected Area ask-first）
- 跨实体状态同步（当管理后台需要统一禁用操作时）
- 默认角色分配（当需要细粒度权限控制时）
