# mobile-m1 移动端项目脚手架 & 基础设施

> Plan Status: completed
> Last Reviewed: 2026-06-30
> Source: `docs/backlog/mobile-frontend-roadmap.md` Mobile Phase 1；`docs/analysis/2026-06-21-mobile-mall-functional-design-analysis.md`（半游客模式 + 登录后编排设计模式）
> Related: 后续 `2026-06-30-1343-2-mobile-m2-home-category-plan.md`（M2）、`2026-06-30-1343-3-mobile-m3-product-detail-cart-plan.md`（M3）均依赖本计划
> Audit: required

## Current Baseline

**后端 API（已 done，移动端直接消费，无后端改动）：**
- 认证：`LoginApi` 暴露 `login` / `logout` / `signUp`（注册） / `resetPassword` / `sendResetCode`（手机号验证码）GraphQL mutations/queries（`docs/plans/2026-06-09-phase1-user-registration-login-plan.md` 已交付并 IGraphQLEngine 测试通过）。
- 用户实体：`NopAuthUser`（含 Delta 扩展字段 `lastLoginIp`/`userLevel`/`sessionKey`）。
- Nop 后端由 Quarkus 提供 GraphQL 端点（`/api` GraphQL 入口，`dev` profile）。

**移动端现状（零实现，本计划基线必须 inventory 的矛盾点）：**
- **无任何移动端前端代码**：`nop-app-mall` 仓库内无移动端工程；`app-mall-web` 为 AMIS（`.view.xml`）技术栈，**与移动端 nop-chaos-flux（React 19）不共用渲染栈，不可复用其页面**。仓库根存在 `app-mall-mobile/` 占位目录（仅含 `README.md`，git 未跟踪，无任何代码/配置）——D1 备选 (a) 所述「在 mall 仓库内新建 `app-mall-mobile/`」实际指复用此占位目录，非全新创建；D1 已否决该备选。
- **nop-chaos-flux 框架已就绪**（外部 monorepo `~/app/nop-chaos-flux-wt/nop-chaos-flux-master/`）：
  - workspace 包：`@nop-chaos/flux-react`（SchemaRenderer 工厂 `createSchemaRenderer`）、`flux-core`、`flux-renderers-basic/-layout/-form/-data/-content`、`flux-renderers-mobile`（pull-refresh / infinite-scroll / swipe-cell / countdown / notice-bar）、`flux-i18n`、`theme-tokens`、`ui`。
  - 参考工程：`apps/playground/`（Vite + React 19 + pnpm workspace，`package.json` 列出全部 `workspace:*` 依赖与 `dev/build/typecheck/test/lint` 脚本，是 app 接线范本）。
  - `flux-guide/`：`README.md`（架构 + 文件索引）、`01-quickstart.md`（15 段骨架）、`02-reference.md`（表达式/API/事件/Action Algebra）、`mobile/README.md`（事件驱动、请求下沉、M0 44×44px 触摸基线、page→pull-refresh→infinite-scroll→list 组合）。

**Gap：** 无可启动的移动端 app；无路由/Tab/页面栈；无 auth/拦截/续期；无全局状态；半游客模式缺失。

## Goals

- 一个可 `pnpm dev` 启动、可 `pnpm build` 产物的 nop-chaos-flux 移动端 app，渲染器与 env（fetcher/notify/confirm/navigate）接线完成。
- 路由框架 + 底部 Tab 导航 + 页面栈（返回）就绪。
- 登录 / 注册 / 忘记密码三页面落地，消费既有 `LoginApi`，token 持久化 + 请求自动注入 + 自动续期。
- 半游客模式：未登录可浏览，加购/下单/访问个人中心等关键动作拦截跳登录。
- 全局状态（Zustand）：用户信息 + 购物车角标，登录后编排刷新。

## Non-Goals

- M2+ 页面（首页/分类/商品详情/购物车/结算/订单等）—— 后续计划。
- 任何后端 / ORM / BizModel 改动 —— 纯消费既有 GraphQL API。
- 微信小程序 / 原生 App 打包 —— 本项目权威设计为 H5（见 `docs/design/user-and-address.md:170`「产品定位为 H5/Web 商城，未交付微信小程序前端」、`mobile-frontend-roadmap.md` L5/L117）。
- 三方登录（微信/Apple）—— 后端 `LoginApi` 未提供，属后续增强。
- 国际化 / 多主题 —— 后续。

## Task Route

- Type: `implementation-only change`（业务设计已在 `docs/design/user-and-address.md` 落地；本计划为移动端脚手架 + auth 消费层，无 net-new 业务语义）
- Owner Docs: `docs/design/user-and-address.md`（用户/认证语义）、`docs/backlog/mobile-frontend-roadmap.md`（M1 范围 + nop-chaos-flux 复用强制）、`docs/design/flow-overview.md`（半游客边界）
- Skill Selection Basis: 见各 phase `Required Skill`。总体：本计划为 nop-chaos-flux（React 19 + JSON Schema）移动端开发，**非 Nop 平台 AMIS 前端**。

## Infrastructure And Config Prereqs

- 后端 GraphQL 端点：本地需运行 Quarkus（`./mvnw clean package -DskipTests && java -Dquarkus.profile=dev -jar app-mall-app/target/app-mall-app-1.0-SNAPSHOT-runner.jar`）。移动端 dev 代理把 `/api`（GraphQL）转发到该后端。
- Vite dev server 端口（如 5173）与后端端口隔离；CORS 由 dev proxy 规避（生产部署由反代统一）。
- 无外部密钥；无数据迁移；无回滚脚本需求。

## Execution Plan

### Phase 1 - 工程脚手架 & 渲染器/env 接线

Status: completed
Targets: `apps/mall-mobile/`（nop-chaos-flux monorepo 内新 app，见 Decision D1）、`pnpm-workspace.yaml`（已含 `apps/*`，无需改）、Vite/TS 配置、`src/main.tsx`、`src/schema-renderer.ts`、`src/env.ts`、`vite.config.ts`
Required Skill: `none`（nop-chaos-flux React 移动端脚手架，非 AMIS；available skills 中 `nop-frontend-dev` 触发词为 view.xml/AMIS/grid/form，与本 phase 技术栈不匹配，其余 skills 均为后端/模型/测试导向。方法源：`flux-guide/README.md`、`flux-guide/01-quickstart.md`、`apps/playground/` 范本）

- Item Types: `Decision | Add`
- Prereqs: 无

- [x] **Skill loading gate:** 通读 `flux-guide/README.md`（架构 + 文件索引）、`flux-guide/01-quickstart.md` §1-§4/§8/§9（page/api/form/action/data-source 骨架）、`apps/playground/package.json` + `apps/playground/src/main.tsx`（接线范本）。列出已读路径于本 item 下。
  - Docs read: `flux-guide/README.md`、`flux-guide/01-quickstart.md`（§1-§4/§8/§9/§16）、`flux-guide/02-reference.md`（表达式/API/事件/数据流）、`flux-guide/mobile/README.md`、`flux-guide/design-patterns/form.md`、`apps/playground/package.json`、`apps/playground/vite.config.ts`、`apps/playground/tsconfig.json`、`apps/playground/vitest.config.ts`、`apps/playground/src/main.tsx`、`apps/playground/src/App.tsx`、`apps/playground/src/use-route.ts`、`apps/playground/src/route-model.ts`、`apps/playground/src/pages/data-verify-page.tsx`（fetcher/env 范本）、`packages/flux-core/src/types/renderer-api.ts`（RendererEnv/ApiFetcher 契约）、`packages/flux-react/src/index.tsx` + `defaults.ts`（createSchemaRenderer/createDefaultRegistry）、`packages/flux-renderers-mobile/src/index.ts`（mobileRendererDefinitions）、`tsconfig.base.json`、`vite.workspace-alias.ts`、`vitest.shared.ts`、`turbo.json`、`pnpm-workspace.yaml`
- [x] **Decision D1（移动端 app 落点）：** 扶择——在 nop-chaos-flux monorepo 内新建 `apps/mall-mobile/`，以 `workspace:*` 消费 flux 包。备选（a）在 `nop-app-mall` 仓库内新建 `app-mall-mobile/` 目录——否决理由：flux 包均为 `private:true` workspace 包（如 `flux-renderers-mobile/package.json`），跨仓库消费需先发布或 pnpm link，摩擦大且 playground 范本即位于 monorepo 内。备选（b）fork flux 包到 mall 仓库——否决理由：破坏单一事实源、升级困难。残留风险：交付物跨仓库（mall 计划文档驱动 flux 仓库代码）——与既有 `nop-entropy` 兄弟仓库依赖模式一致，可接受；记录于 owner doc。
- [x] **Add:** 创建 `apps/mall-mobile/`（`package.json` 仿 `apps/playground/package.json`，仅保留必需 `workspace:*` 依赖：`flux-react`/`flux-core`/`flux-renderers-basic`/`-layout`/`-form`/`-data`/`-content`/`-mobile`/`flux-i18n`/`theme-tokens`/`ui` + `react`/`react-dom`/`lucide-react`；脚本 `dev`/`build`/`typecheck`/`test`/`lint`）。
- [x] **Add:** `createSchemaRenderer([...所有需要的 renderer 定义])` 注册（含 `mobileRendererDefinitions`）；`src/main.tsx` 挂载 `<SchemaRenderer schema env>`。
- [x] **Add:** `src/env.ts` 实现 flux env 契约（`flux-guide/README.md` §如何用）：`fetcher`（注入 auth token、命中 GraphQL 端点）、`notify`、`confirm`、`navigate`（接路由）。
- [x] **Add:** `vite.config.ts`（React 插件 + dev proxy `/api`→后端 + 别名）。
- [x] **Proof:** `pnpm --filter @nop-chaos/mall-mobile typecheck` 与 `pnpm --filter @nop-chaos/mall-mobile build` 通过；`pnpm dev` 渲染一个 `{type:page,body:"mall-mobile ready"}` 占位首页。

Exit Criteria:

- [x] app 可 dev 启动并渲染占位页；`typecheck` + `build` 通过
- [x] env fetcher 能命中后端 GraphQL 端点（手动发一个既有 query 如 `LoginApi` introspection 验证 200）
- [x] 无新增 `@BizMutation`/`@BizQuery`（纯消费），故无 IGraphQLEngine 后端测试要求
- [x] No owner-doc update required（D1 决策在 Phase 3 收口时统一同步；本 phase 不改 mall 仓库文件）
- [x] `docs/logs/` 更新

### Phase 2 - 路由框架 + 底部 Tab + 全局状态

Status: completed
Targets: `src/route-model.ts`、`src/layouts/`（Tab 壳）、`src/store/`（Zustand）、`src/pages/`（占位骨架页）
Required Skill: `none`（理由同 Phase 1；方法源：`flux-guide/02-reference.md` 事件/数据流、`apps/playground/src/route-model.ts` + `use-route.ts` 范本）

- Item Types: `Add | Decision`
- Prereqs: Phase 1

- [x] **Skill loading gate:** 通读 `flux-guide/02-reference.md`（表达式/事件/数据流）、`apps/playground/src/route-model.ts` 与 `apps/playground/src/use-route.ts`（路由范本）。列出已读路径。
  - Docs read: `flux-guide/02-reference.md`（§1 模板表达式 / §2 API 配置 / §3 事件与 Action Algebra / §4 数据域 ScopeRef / §7 Tabs 状态管理 / §9 与 AMIS 差异→状态管理 Zustand + ScopeRef）、`apps/playground/src/route-model.ts`（RouteSpec union + parseRoute/buildRoute hash 序列化）、`apps/playground/src/use-route.ts`（hashchange 监听 + applyRoute）
- [x] **Decision D2（路由方案）：** 抉择——复用 playground 既有路由模型（route-model + use-route）+ flux page 的 `navigate` env 钩子。备选（引入 react-router）——否决理由：flux 事件驱动 navigate 已由 env 提供，引入第二套路由产生双源。残留风险：无。
- [x] **Add:** 路由表 + 底部 Tab 壳（首页/分类/购物车/我的，购物车 Tab 带角标 slot）；页面栈（详情页 push、返回 pop）。
- [x] **Add:** Zustand store：`user`（userInfo + token + 登录态）、`cartBadge`（计数）；selector + 持久化（token/userInfo 持久到 localStorage）。
- [x] **Add:** 4 个 Tab 落地页占位骨架（M2/M3/M6 填充实体内容）。
- [x] **Proof:** Tab 切换 + push/pop 页面栈可用；`typecheck` + `build` 通过；store 读写 + 持久化单测（vitest）。

Exit Criteria:

- [x] 4 Tab 导航 + 页面栈 push/pop 行为正确
- [x] Zustand store token/userInfo/cartBadge 读写 + 持久化（localStorage）有 vitest 覆盖
- [x] No owner-doc update required
- [x] `docs/logs/` 更新

### Phase 3 - 认证（登录/注册/忘记密码）+ Token 拦截续期 + 半游客模式

Status: completed
Targets: `src/pages/auth/`（login/register/forgot-password）、`src/env.ts`（fetcher 拦截增强）、`src/store/`、`src/guards/`（半游客拦截）
Required Skill: `none`（理由同 Phase 1；方法源：`flux-guide/01-quickstart.md` §4/§8 form+action、`flux-guide/design-patterns/form.md`、`docs/design/user-and-address.md` 认证语义）

- Item Types: `Add | Decision`
- Prereqs: Phase 2

- [x] **Skill loading gate:** 通读 `flux-guide/design-patterns/form.md`（表单提交+校验）、`flux-guide/01-quickstart.md` §4/§8/§16（form/action/confirm）、`flux-guide/mobile/README.md`（M0 44×44px 触摸基线，auth 表单/按钮须满足）；复阅 `docs/design/user-and-address.md`（认证/密码重置语义）+ `docs/plans/2026-06-09-phase1-user-registration-login-plan.md`（LoginApi GraphQL 契约：login/signUp/resetPassword/sendResetCode 入参出参）。列出已读路径。
  - Docs read: `flux-guide/design-patterns/form.md`（基础表单 + submitAction/onSubmitSuccess/onSubmitError）、`flux-guide/01-quickstart.md`（§4 表单+提交 / §8 Action Algebra 动作链 / §16 Confirm 确认框）、`flux-guide/mobile/README.md`（触摸目标 44×44px 基线）、`docs/design/user-and-address.md`（H5 定位 + 认证/密码重置语义）、`docs/plans/2026-06-09-phase1-user-registration-login-plan.md`（completed，LoginApi 契约）。**实测契约核对（live repo）**：`app-mall-delta/.../biz/LoginApiExBizModel.java`（signUp/sendResetCode/resetPassword `@BizMutation @Auth(publicAccess=true)`）+ 平台 `LoginApiBizModel`（login/logout/refreshToken）+ `app-mall-service/.../biz/TestLoginApiSignUp.java` + `TestPasswordReset.java` + `e2e/tests/auth.ts`（REST RPC 范本 `/r/LoginApi__login` body 裸 args）。契约：login 入参 `{principalId, principalSecret, loginType:1}`→`{accessToken,refreshToken,expiresIn,userInfo}`；signUp `{username,password,mobile}`→NopAuthUser（不自动登录，客户端须再调 login）；sendResetCode `{mobile}`→void；resetPassword `{mobile,code,newPassword}`→void；refreshToken `{refreshToken}`→LoginResult。
- [x] **Decision D3（token 续期策略）：** 抉择——access token 失效时由 fetcher 拦截 401，自动以 refresh 凭证（Nop 登录返回）静默续期并重放原请求；续期失败回退登录页。备选（定时主动刷新）——否决理由：Nop 无显式 refresh_token 双令牌契约，定时刷新无依据；被动 401 续期对既有契约侵入最小。残留风险：并发请求同时 401 的重放去重——fetcher 内加单飞（in-flight refresh promise 复用）。
- [x] **Add:** 登录页（用户名/手机号 + 密码）→ 调 `LoginApi.login`，成功写 token+userInfo 到 store、触发登录后编排（刷新购物车角标占位）。
- [x] **Add:** 注册页 → `LoginApi.signUp`（用户名/密码/手机号，对齐既有契约）；忘记密码页 → `sendResetCode` + `resetPassword`（手机号+验证码+新密码）。
- [x] **Add:** fetcher 增强：请求注入 `Authorization`；401 → 续期单飞 → 重放 / 失败回退登录。
- [x] **Add:** 半游客拦截 guard：定义需登录动作集（加购/结算/个人中心/订单/收藏等），未登录触发时跳登录页并在登录成功后回到原意图（returnTo）。
- [x] **Proof:** vitest 覆盖——登录成功写 store、401 续期单飞重放、半游客拦截 returnTo；手动 e2e 烟测（登录/注册/忘记密码三流程通）；`typecheck` + `build` 通过。

Exit Criteria:

- [x] 登录/注册/忘记密码三流程消费既有 `LoginApi` 跑通（成功 + 主要失败态：错密码/重复注册/错验证码）
- [x] token 自动注入 + 401 续期单飞重放有 vitest 覆盖
- [x] 半游客拦截 + returnTo 有 vitest 覆盖
- [x] 无新增 `@BizMutation`/`@BizQuery`（纯消费 LoginApi）
- [x] owner doc 更新：`docs/architecture/` 增「移动端工程落点 + auth 消费层」技术结构说明（D1/D3）；`docs/design/` 无业务语义变更无需改
- [x] `docs/logs/` 更新

## Plan Audit

- Status: passed
- Auditor / Agent: 独立 plan-audit pass（fresh session，非计划起草者）
- Evidence: 对照 live repo 逐项核验基线声明——(1) `LoginApiExBizModel.java`（`app-mall-delta/.../biz/`）实测含 `@BizModel("LoginApi")` + `signUp`/`sendResetCode`/`resetPassword` 三个 `@BizMutation @Auth(publicAccess=true)`，login/logout 来自平台，契约属实；(2) 仓库内确认无移动端工程（`app-mall-web` 为 AMIS），`app-mall-mobile/` 仅为未跟踪 README 占位（已补入基线）；(3) `docs/design/user-and-address.md` H5 定位、`docs/backlog/mobile-frontend-roadmap.md`、`docs/plans/2026-06-09-phase1-user-registration-login-plan.md`(completed) 均存在且与引用一致。格式：必需节齐全、字段名正确、Phase 结构合规。范围：3 phase 边界清晰、Non-Goals 明确、无 scope creep。`Required Skill: none` 附非 Nop/非 AMIS 技术栈理由，符合规则 #14。BizModel 测试规则 #15 正确标 N/A（纯消费）。无 Blocker/Major。Minor 残留：各 phase `Item Types` 摘要未含 `Proof`（每项已内联标注类型，不违反逐项规则），留待 closure/deep audit。

## Closure Gates

- [x] in-scope behavior is complete
- [x] relevant docs are aligned（`docs/architecture/system-baseline.md` Mobile Mall H5 章节：M1 delivered + D1 工程落点 + D3 auth 消费层）
- [x] verification has run（`pnpm --filter @nop-chaos/mall-mobile typecheck` + `build` + `test`(58 passed) + `lint` 全 EXIT=0；本计划为视觉/行为驱动，前端 vitest 替代后端 IGraphQLEngine）
- [x] 无新增 `@BizMutation`/`@BizQuery`（纯消费既有后端 API），故 IGraphQLEngine 后端测试项不适用
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed（均为 `none` 并附非 AMIS 技术栈理由，符合规则 #14「with justification」）
- [x] skill loading verification: 各 phase 通读 flux-guide 路由文档，路径列于 skill loading gate
- [x] text consistency verified: status, phases, gates, and log all agree
- [x] closure audit was performed by a different agent/session than implementation
- [x] closure evidence exists in files

## Deferred But Adjudicated

### 三方登录（微信/Apple/支付宝）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 后端 `LoginApi` 未提供三方登录契约；本项目 H5 定位，三方登录属后续增强。
- Successor Required: `yes`（触发条件：后端提供三方登录 API 时）

### 生产部署与反代/CORS

- Classification: `watch-only residual`
- Why Not Blocking Closure: 本计划仅保证 dev proxy 可用；生产部署（反代统一域名/CORS）属发布工程范围。
- Successor Required: `yes`（触发条件：移动端进入生产部署时）

## Closure

Status Note: M1 全部三 phase 交付并独立闭合审计 PASS（无 Blocker/Major）。交付物跨仓库落地于 nop-chaos-flux monorepo `apps/mall-mobile/`（`@nop-chaos/mall-mobile`），纯消费既有 `LoginApi`，无后端改动。验证全绿：typecheck/build/lint EXIT=0，vitest 58 passed。残留 successor：三方登录（后端契约缺失）、生产反代/CORS（仅 dev proxy 已验证）。

Closure Audit Evidence:

- Reviewer / Agent: 独立 closure audit subagent（fresh session，非实现者；task `ses_0e8ab8c5cffeOE65G2PkkyAAWi`）
- Verdict: PASS（无 Blocker / Major）
- Evidence:
  - 逐项核验 12 条交付物清单（Phase 1 脚手架/renderer/env/main、Phase 2 路由/Tab 壳/Zustand store/4 占位页、Phase 3 auth 三页/fetcher 401 续期单飞重放/半游客 guard）对照 live repo 全部 OK，附 file:line 证据。
  - 验证命令（`pnpm --filter @nop-chaos/mall-mobile ...`）：typecheck EXIT=0、test EXIT=0（7 files / 58 passed）、build EXIT=0（dist index 167KB + react-vendor 190KB）、lint EXIT=0。
  - 文档一致性：Phase items 全 `[x]`、Phase Status 全 `completed`、roadmap M1=`done`、`docs/architecture/system-baseline.md` Mobile 章节 updated、log 详细。
  - 唯一 Minor：闭合文书本身（Plan Status/Closure Gates/Closure section）待本次 finalize——已由本闭合动作完成。

Follow-up:

- 三方登录（见 Deferred，触发条件：后端提供三方登录 API）
- 生产部署反代/CORS（见 Deferred，触发条件：移动端进入生产部署）
