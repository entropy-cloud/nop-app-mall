# 2026-06-15-1500 Phase 14 微信支付集成计划

> Plan Status: completed
> Last Reviewed: 2026-06-15
> Source: `docs/backlog/implementation-roadmap.md` Phase 14（`planned`，Protected Area ask-first），用户 2026-06-15 批准推进
> Related: `docs/plans/2026-06-13-next-phase-notification-report-wxpay-plan.md`（completed，Phase 3A 骨架，skipped per user decision；本计划替代其 Phase 3A）
> Audit: required

## Ask-First Gate（已通过）

Phase 14（微信支付集成）在 `docs/context/ai-autonomy-policy.md` 中定义为 Protected Area `ask-first`。用户 2026-06-15 明确批准：

> "自己拟制一个示例性的 ID，仅在代码层面实现微信支付。要完成微信支付最合适的最新实现方式的调研，并做技术实现。"

含义：
- 使用**示例性配置值**（非真实商户凭证），代码完整但无法处理真实支付
- 必须**调研最新最合适的实现方式**（已完成，见 Phase 1 Decision）
- **在代码层面完成技术实现**（完整代码，不要求真实沙箱联调）

本计划满足 ask-first 证据要求：owner doc（`docs/architecture/system-baseline.md`）+ tests。

## Why One Plan

Phase 14 是 roadmap 唯一未完成阶段，涉及 `app-mall-api`（接口合约扩展）、`app-mall-wx`（真实实现 + SDK）、`app-mall-service`（订单流程集成 + IoC 绑定清理）、`app-mall-app`（配置）四个模块。所有改动共享同一行为合约（`PayService` 接口 + 微信支付端到端流程）和同一 closure gate，合并为一个计划。

## Current Baseline

> 经独立 subagent 逐文件 live-repo 核验（ses_135a500d5ffef17kULtza7xOW0，2026-06-15）。

### `PayService` 接口（仅 refund）

`app-mall-api/.../pay/PayService.java`（10 行）：

```java
@BizModel("PayService")
public interface PayService {
    @BizMutation("refund")
    PayRefundResponseBean refund(PayRefundRequestBean req);
}
```

**只有 `refund`。** 没有 `createPayment`/`queryPayment`/`prepay`。

### 已有 Bean（部分未使用）

| Bean | 字段 | 状态 |
|------|------|------|
| `PayPrepayRequestBean` | `outTradeNo`, `totalFee` | **定义但零引用** |
| `PayPrepayResponseBean` | `payId` | **定义但零引用** |
| `PayRefundRequestBean` | `outTradeNo`, `outRefundNo`, `totalFee`, `refundFee` | 被 aftersale/groupon 退款使用 |
| `PayRefundResponseBean` | `success`（boolean，默认 true） | 仅布尔，无错误码/退款单号 |

### `PayService` 实现与 IoC 绑定（**经审计修正：无歧义**）

| 实现 | 注册方式 | 逻辑 | IoC 活跃性 |
|------|----------|------|-----------|
| `MockPayServiceImpl`（app-mall-service） | 仅 `@Named` 注解，**不在任何 beans.xml 中** | 静态 `forceRefundFailure` 标志，始终返回 success | **未注册**（Nop IoC 基于文件发现，`@Named` 单独不触发注册；`app-service.beans.xml` 仅含 `MallNotificationService`） |
| `WxPayServiceImpl`（app-mall-wx） | `app-wx.beans.xml`（id=`wxPayService`） | **逐字节等同于 Mock**（无 SDK、无 HTTP、无签名） | **唯一注册的 `PayService` bean**——`@Inject PayService payService` 按类型解析到此类 |

> **审计修正（B1）：** Nop IoC 是基于文件发现的（`ioc-and-config.md`：bean 发现通过 VFS 遍历，非 Java classpath scanning）。`MockPayServiceImpl` 虽有 `@Named`，但不在任何 `*.beans.xml` 中，因此**不是 IoC 注册 bean**。`@Inject` 无 `@Named` 限定时按类型（byType）解析，字段名 `payService` 不参与。因此**不存在 IoC 歧义**——所有注入点（`LitemallAftersaleBizModel:47`、`LitemallGrouponBizModel:58`、测试 `TestLitemallAftersaleBizModel:45`）当前已解析到 `WxPayServiceImpl`（即 stub）。测试中 `MockPayServiceImpl.setForceRefundFailure(true)` 是无效死代码（不影响活跃 bean），`WxPayServiceImpl.setForceRefundFailure(true)` 才是实际生效的测试钩子。

**本计划的真实工作：** `WxPayServiceImpl` 已是活跃 bean，需将其从 stub 升级为真实实现（保持 IoC 绑定不变）。`MockPayServiceImpl` 的 `@Named` 是无效注解，应清理；测试中的死代码 `MockPayServiceImpl.setForceRefundFailure` 调用应移除。

### 订单 `pay()` 方法（不调用 PayService）

`LitemallOrderBizModel.pay()`（L298-318）直接翻转订单状态 `CREATED → PAY`，**不调用任何支付提供方**。这是"模拟支付"路径，零金额订单和开发环境使用。真实微信支付需要独立的 prepay → 回调 → 状态更新流程。

### `app-mall-wx` 模块（极简）

```
app-mall-wx/
├── pom.xml                          # 仅依赖 app-mall-api + nop-core，无微信 SDK
└── src/main/
    ├── java/app/mall/wx/WxPayServiceImpl.java   # 23 行 stub
    └── resources/_vfs/app/mall/beans/app-wx.beans.xml   # 注册 wxPayService
```

模块通过 `app-mall-service/pom.xml`（compile scope）→ `app-mall-app` 传递到运行时 classpath。在根 `pom.xml` L28 注册为顶级模块。

### 微信支付配置（完全缺失）

`application.yaml`（88 行）无任何 `wxpay`/`wechat`/`mch` 配置。集成需新增配置段。

### 调研结论（独立 subagent ses_135a4c958ffe3Ap2wHcabA28sp，2026-06-15）

**推荐：官方 `wechatpay-java` SDK 0.2.17 + `RSAPublicKeyConfig`（公钥模式）。**

| 方案 | 结论 | 理由 |
|------|------|------|
| **官方 `wechatpay-java` 0.2.17**（com.github.wechatpay-apiv3） | **采用** | 腾讯一方维护，V3-only 聚焦，支持 2024+ 公钥模式（消除证书轮换复杂度），契约与微信支付文档 1:1 |
| WxJava `weixin-java-pay` 4.7.0 | 不采用 | 社区维护，拖入 MP/MiniProgram 传递依赖，公钥模式采纳滞后 |
| 直接 V3 HTTP | 不采用 | 需自行实现签名+证书轮换+AES-GCM 解密，安全风险高，无正当理由 |

关键 V3 概念：mchId、AppID、apiV3Key（32 字节对称密钥）、商户私钥 `apiclient_key.pem`、商户证书序列号、微信支付公钥（2024+，替代轮换的平台证书）、回调签名验证 + AES-256-GCM 解密。

## Goals

1. **引入官方 `wechatpay-java` SDK**，在 `app-mall-wx` 模块完成真实微信支付技术实现
2. **扩展 `PayService` 接口**：新增 `createPayment`（统一下单）+ `queryPayment`（查询支付状态），保留 `refund`（真实化）
3. **实现 Native 扫码支付端到端流程**：prepay（返回 code_url）→ 前端渲染二维码 → 用户扫码支付 → 微信回调通知 → 订单状态更新
4. **实现支付回调端点**：接收并验证微信回调（签名验证 + AES-GCM 解密），更新订单状态
5. **确保单一活跃 `PayService` bean 与配置驱动的"示例模式/真实模式"切换**（`WxPayServiceImpl` 已是唯一注册 bean，无需解歧义，仅需清理无效 `@Named` 与测试死代码）
6. **示例性配置**：`application.yaml` 使用 `${ENV:default}` 占位符，默认为示例值（`enabled=false`，真实调用被跳过，行为退化为日志 + 模拟响应）
7. **测试覆盖**：示例模式下的 `createPayment`/`queryPayment`/`refund`/回调端点通过测试验证

## Non-Goals

- **真实沙箱联调**：用户明确"仅在代码层面实现"，不要求真实微信沙箱验证
- **JSAPI / H5 / App 支付场景**：本计划仅实现 Native（扫码）。JSAPI 需 openid 获取流程，H5 需外部浏览器 UA 判定，架构预留扩展点但不在范围
- **微信登录 / 公众号 / 小程序集成**：不在 Phase 14 范围
- **真实商户凭证**：使用示例占位符，不提交真实密钥
- **退款回调（refund notify）**：仅实现退款请求，退款异步通知延后（触发条件：业务需要退款状态对账时）
- **账单下载 / 商家转账 / 合单支付**：不在范围
- **前端二维码渲染页面**：后端返回 `code_url`，前端渲染二维码的页面属前端开发阶段，不在本计划（但扩展页面可后续补充）
- **删除 `MockPayServiceImpl`**：保留类用于测试直接实例化，但清理其无效 `@Named`（当前已非 IoC 活跃 bean）

## Task Route

- Type: `architecture change`（支付渠道替换 + API 合约扩展）+ `implementation-only change`
- Owner Docs: `docs/architecture/system-baseline.md`（支付集成技术结构）、`docs/design/order-and-cart.md`（订单支付流程）
- Skill Selection Basis:
  - `nop-backend-dev`（BizModel 方法、PayService 接口扩展、IoC）
  - `nop-testing`（IGraphQLEngine / I*Biz 接口测试）
  - 调研结论作为 Decision 输入

## Infrastructure And Config Prereqs

- `nop-entropy` parent POM 可用
- `app-mall-wx` 模块可引入新依赖（`wechatpay-java` + 可能的 `quarkus-resteasy-reactive-jackson`，见 Phase 1 Decision）
- 验证命令（`docs/context/project-context.md`）：`./mvnw.cmd compile -DskipTests`、`./mvnw.cmd test`
- 无真实外部服务依赖（示例模式下不发起真实 HTTP 调用）

## Execution Plan

### Phase 1 — SDK 引入、配置与接口合约扩展
Targets: `app-mall-wx/pom.xml`、`app-mall-api/.../pay/`（接口 + Bean）、`app-mall-app/src/main/resources/application.yaml`
Required Skill: `nop-backend-dev`

- Item Types: `Decision | Add`
- Prereqs: 无
- Protected Area: `PayService` 接口与 Bean 属 `app-mall-api` 公共合约，本计划作为已审计 plan + ask-first 已通过

- [ ] **Skill loading gate:** 加载 `nop-backend-dev` skill，读取其路由表中所有必读文档。列出已读文档路径。每写完一个方法/类用 selfcheck 校验。
  - Docs read: <列出路径>
- [ ] **Decision — SDK 选型：** 采用官方 `com.github.wechatpay-apiv3:wechatpay-java:0.2.17`，配置模式 `RSAPublicKeyConfig`（公钥模式，2024+ 推荐路径，消除平台证书轮换复杂度）。Alternatives：WxJava（overscoped，拖入 MP 依赖）、直接 HTTP（安全风险高）。理由见 Current Baseline 调研结论
- [ ] **Decision — 支付场景范围：** 仅实现 Native（扫码支付）。理由：(1) Web 商城桌面端最自然场景；(2) 不依赖 openid 获取流程（JSAPI 需要）；(3) 返回 `code_url` 供前端渲染二维码，流程最简。JSAPI/H5 架构预留（通过 `PayPrepayRequestBean` 可选字段 + WxPayServiceImpl 内部分支），但不在本计划实现
- [ ] **Decision — PayService 行为切换与 Mock 清理策略：** `WxPayServiceImpl` 已是唯一注册的活跃 bean（无歧义）。策略：
  - 选项 A（配置驱动单 bean）：`WxPayServiceImpl` 内置 `enabled` 标志（读取配置 `wxpay.enabled`）。`enabled=false`（默认/示例）时跳过真实 HTTP 调用，行为退化为日志 + 模拟响应；`enabled=true` 时发起真实微信调用。清理 `MockPayServiceImpl` 的无效 `@Named`（保留类用于测试直接实例化），`WxPayServiceImpl` 保持现有 IoC 绑定不变
  - 选项 B（profile 切换）：dev profile 注册 Mock，prod profile 注册 WxPay（需新增 beans.xml + profile 条件）
  - 选项 C（移除 Mock）：删除 MockPayServiceImpl，WxPay 示例模式承担全部
  - **推荐选项 A：** 单 bean 无歧义（已验证），配置控制行为，Mock 保留供测试直接使用。测试通过 `WxPayServiceImpl.setForceRefundFailure` 或 `enabled=false` 控制行为
  - Alternatives：选项 B 增加配置复杂度且无必要（当前无歧义）；选项 C 破坏现有测试引用
  - 残留风险：`enabled=false` 时退款等路径退化为模拟，生产部署需确保 `enabled=true`
- [ ] **Add: 引入 SDK 依赖。** `app-mall-wx/pom.xml` 新增：
  ```xml
  <dependency>
      <groupId>com.github.wechatpay-apiv3</groupId>
      <artifactId>wechatpay-java</artifactId>
      <version>0.2.17</version>
  </dependency>
  ```
- [ ] **Decision — 回调端点技术方案（JAX-RS 无先例，需显式决策）：** 当前项目**零 JAX-RS 端点**（全项目 grep `jakarta.ws.rs`/`@Path` 零匹配），`app-mall-app/pom.xml` 未显式依赖 `quarkus-resteasy`。回调端点是首个原始 HTTP 端点。
  - 选项 A（引入 `quarkus-resteasy-reactive-jackson`）：在 `app-mall-wx/pom.xml`（或 `app-mall-app/pom.xml`）新增 `io.quarkus:quarkus-resteasy-reactive-jackson` 依赖，用标准 JAX-RS `@Path/@POST` 实现回调。Quarkus 原生支持
  - 选项 B（Nop 平台 HTTP 机制）：若 Nop 有非 GraphQL 的 HTTP 端点机制（如 `@RpcController` 或 servlet filter），则复用。需查证 `nop-entropy/docs-for-ai/` 是否有相关 runbook
  - 选项 C（GraphQL mutation 接收原始 body）：不可行——GraphQL 经过 JSON 解析，无法获取原始 body 字符串，破坏微信签名验证
  - **推荐选项 A：** JAX-RS 是 Quarkus 标准方式，依赖轻量，文档充分。引入 `quarkus-resteasy-reactive-jackson` 即可
  - Alternatives：选项 B 需额外调研且无先例；选项 C 技术上不可行
  - 残留风险：首个 JAX-RS 端点，需验证与 Nop 的 HTTP 栈共存（端口、路由前缀不冲突）。`/wxpay/notify` 路径不与 Nop 的 `/r/`（GraphQL）或 `/p/`（页面）前缀冲突
- [ ] **Add: 引入 REST 依赖（若选项 A）。** `app-mall-wx/pom.xml` 新增 `io.quarkus:quarkus-resteasy-reactive-jackson`（版本由 Quarkus BOM 管理）
- [ ] **Add: 扩展 `PayService` 接口。** 新增两个方法：
  ```java
  @BizMutation("createPayment")
  PayPrepayResponseBean createPayment(PayPrepayRequestBean req);

  @BizQuery("queryPayment")
  PayStatusResponseBean queryPayment(@Name("outTradeNo") String outTradeNo);
  ```
- [ ] **Add: 扩展 `PayPrepayRequestBean`。** 新增字段：
  - `description`（String，订单描述，微信支付必填）
  - `notifyUrl`（String，`@Optional`，回调地址，缺省从配置读取）
- [ ] **Add: 扩展 `PayPrepayResponseBean`。** 新增字段：
  - `codeUrl`（String，Native 扫码支付二维码链接）
  - `tradeType`（String，支付类型，如 "NATIVE"）
- [ ] **Add: 新增 `PayStatusResponseBean`。** 字段：
  - `success`（boolean）
  - `tradeState`（String，微信支付状态：SUCCESS/REFUND/NOTPAY/CLOSED/REVOKED/USERPAYING/PAYERROR）
  - `transactionId`（String，微信支付订单号）
  - `outTradeNo`（String，商户订单号）
  - `errorMessage`（String，`@Optional`）
- [ ] **Add: 扩展 `PayRefundResponseBean`。** 新增字段：
  - `refundId`（String，微信退款单号，`@Optional`）
  - `errorCode`（String，`@Optional`）
  - `errorMessage`（String，`@Optional`）
  - 保留 `success` 字段（向后兼容）
- [ ] **Add: 微信支付配置。** `application.yaml` 新增 `wxpay` 配置段（示例值 + ENV 占位符）：
  ```yaml
  wxpay:
    enabled: ${WXPAY_ENABLED:false}
    app-id: ${WXPAY_APP_ID:wx_sample_appid_0000000000}
    mch-id: ${WXPAY_MCH_ID:1900000000}
    api-v3-key: ${WXPAY_API_V3_KEY:00000000000000000000000000000000}
    private-key-path: ${WXPAY_PRIVATE_KEY_PATH:./certs/apiclient_key_sample.pem}
    public-key-path: ${WXPAY_PUBLIC_KEY_PATH:./certs/wechatpay_public_key_sample.pem}
    public-key-id: ${WXPAY_PUBLIC_KEY_ID:PUB_KEY_ID_SAMPLE}
    merchant-serial-number: ${WXPAY_MERCHANT_SERIAL:0000000000000000000000000000000000}
    notify-url: ${WXPAY_NOTIFY_URL:http://localhost:8080/wxpay/notify}
  ```
  - `enabled: false` 为默认，确保开发/测试环境不发起真实调用
- [ ] **Add: 示例证书占位。** 在 `app-mall-wx/src/main/resources/certs/` 放置示例 PEM 文件占位（仅占位文本，非真实密钥），避免运行时文件找不到
- [ ] **Proof: 编译验证。** `./mvnw.cmd compile -DskipTests` 通过

Exit Criteria:

- [ ] SDK 依赖引入且编译通过
- [ ] `PayService` 接口含 `createPayment` + `queryPayment` + `refund`
- [ ] 所有 Bean 字段扩展完成（`PayPrepayRequestBean`/`PayPrepayResponseBean`/`PayStatusResponseBean`/`PayRefundResponseBean`）
- [ ] `application.yaml` 含 `wxpay` 配置段，默认 `enabled: false`
- [ ] 回调端点技术方案 Decision 落地（引入 `quarkus-resteasy-reactive-jackson` 或记录替代方案）
- [ ] SDK 版本 0.2.17 经 Maven Central 可用性确认（编译验证隐含）
- [ ] `./mvnw.cmd compile -DskipTests` 通过
- [ ] `docs/logs/` updated

### Phase 2 — WxPayServiceImpl 完整实现

Status: completed
Targets: `app-mall-wx/src/main/java/app/mall/wx/`
Required Skill: `nop-backend-dev`

- Item Types: `Add-heavy`
- Prereqs: Phase 1（接口 + 依赖 + 配置就绪）

- [ ] **Skill loading gate:** 加载 `nop-backend-dev` skill，读取其路由表中所有必读文档。列出已读文档路径。每写完一个方法用 selfcheck 校验。
  - Docs read: <列出路径>
- [ ] **Add: 微信支付配置类。** 新建 `WxPayConfig` 类（`@Configuration` 或 Nop 配置注入），读取 `wxpay.*` 配置项，暴露为 IoC bean。包含 `isEnabled()`、各配置 getter
- [ ] **Add: 初始化 SDK `Config`。** 在 `WxPayServiceImpl` 构造时（`@PostConstruct`），若 `enabled=true`，用 `RSAPublicKeyConfig.Builder` 构建 SDK `Config` 并初始化 `NativePayService`、`RefundService`、`NotificationParser`。若 `enabled=false`，跳过初始化（SDK 服务保持 null，方法内检查）
- [ ] **Add: 实现 `createPayment`（Native 统一下单）。**
  - `enabled=false`：记录 WARN 日志，返回模拟 `PayPrepayResponseBean`（`codeUrl="weixin://wxpay/bizpayurl?pr=sample"`, `payId="sample_prepay_" + outTradeNo`, `tradeType="NATIVE"`）
  - `enabled=true`：构建 `PrepayRequest`（appid, mchid, description, outTradeNo, notifyUrl, amount），调用 `NativePayService.prepay()`，返回 `code_url` 映射到 `PayPrepayResponseBean.codeUrl`
  - 异常处理：SDK 异常（`ServiceException`/`HttpException`/`ValidationException`）捕获，抛出 `NopException` + ErrorCode（`ERR_WXPAY_CREATE_PAYMENT_FAILED`），携带 errorCode/errorMessage
- [ ] **Add: 实现 `queryPayment`（查询支付状态）。**
  - `enabled=false`：返回模拟 `PayStatusResponseBean`（`success=true`, `tradeState="NOTPAY"`, `outTradeNo=入参`）
  - `enabled=true`：调用 SDK 查询订单（按 outTradeNo），映射 `Transaction.tradeState` 到 `PayStatusResponseBean`
  - `tradeState="SUCCESS"` 时 `success=true`
- [ ] **Add: 实现 `refund`（真实化，替代现有 stub）。**
  - `enabled=false`：保留现有 `forceRefundFailure` 行为（模拟），`refundId="sample_refund_" + outRefundNo`
  - `enabled=true`：构建 `RefundService.CreateRequest`，调用 `RefundService.create()`，映射结果到 `PayRefundResponseBean`（`refundId`、`success`、错误信息）
- [ ] **Add: ErrorCode 定义。** 在 `app-mall-wx` 中定义（app-mall-wx 依赖 app-mall-api，可复用 `NopException`）：
  - `ERR_WXPAY_DISABLED` — 微信支付未启用（`enabled=false` 时调用真实路径）
  - `ERR_WXPAY_CREATE_PAYMENT_FAILED` — 统一下单失败
  - `ERR_WXPAY_QUERY_FAILED` — 查询支付状态失败
  - `ERR_WXPAY_REFUND_FAILED` — 退款失败
  - `ERR_WXPAY_CONFIG_INVALID` — 配置缺失或无效
  - `ERR_WXPAY_NOTIFY_VERIFY_FAILED` — 回调签名验证失败
- [ ] **Fix: 清理无效的 `MockPayServiceImpl` `@Named` 注解。** `MockPayServiceImpl` 的 `@Named` 在 Nop 文件发现 IoC 中不生效（不在任何 beans.xml）。移除该无效注解，避免误导。`MockPayServiceImpl` 保留为可测试类（通过直接实例化或静态方法调用使用，不通过 IoC 注入）。**`WxPayServiceImpl` 的 IoC 绑定不变**（已是唯一注册的 `PayService` bean，无需 `@Named` 调整）
- [ ] **Fix: 清理测试死代码。** `TestLitemallAftersaleBizModel:261-262,276-277` 中 `MockPayServiceImpl.setForceRefundFailure(...)` 调用是死代码（Mock 非 IoC bean，不影响活跃的 `WxPayServiceImpl`）。移除这些调用，保留 `WxPayServiceImpl.setForceRefundFailure(...)`。移除测试中 `import app.mall.pay.MockPayServiceImpl`（如不再使用）
- [ ] **Proof: 编译验证。** `./mvnw.cmd compile -DskipTests` 通过

Exit Criteria:

- [ ] `WxPayServiceImpl` 完整实现 `createPayment`/`queryPayment`/`refund`，示例模式（`enabled=false`）与真实模式（`enabled=true`）双路径
- [ ] SDK `Config`/`NativePayService`/`RefundService`/`NotificationParser` 正确初始化
- [ ] `WxPayServiceImpl` 保持为唯一注册的 `PayService` bean（IoC 绑定不变）
- [ ] `MockPayServiceImpl` 无效 `@Named` 清理；测试死代码移除
- [ ] ErrorCode 定义完成，异常均 `extends NopException`
- [ ] `./mvnw.cmd compile -DskipTests` 通过
- [ ] 现有退款测试仍通过（清理死代码后）
- [ ] `docs/logs/` updated

### Phase 3 — 支付回调端点与订单流程集成

Status: completed
Targets: `app-mall-wx/`（回调端点）、`app-mall-service/.../LitemallOrderBizModel.java`（订单 prepay 集成）
Required Skill: `nop-backend-dev`

- Item Types: `Add | Fix`
- Prereqs: Phase 2（WxPayServiceImpl 可用）

- [ ] **Skill loading gate:** 加载 `nop-backend-dev` skill，读取其路由表中所有必读文档。列出已读文档路径。每写完一个方法用 selfcheck 校验。
  - Docs read: <列出路径>
- [ ] **Add: 支付回调端点。** 新建 `WxPayNotifyResource`（JAX-RS 或 Nop HTTP 端点），路径 `/wxpay/notify`：
  - 接收原始 body（**必须原始字符串，不可重新序列化**）+ 微信回调 headers（`Wechatpay-Serial`、`Wechatpay-Nonce`、`Wechatpay-Signature`、`Wechatpay-Timestamp`、`Wechatpay-Signature-Type`）
  - `enabled=false`：记录日志并返回 200（示例模式不验证签名）
  - `enabled=true`：构建 `RequestParam`，调用 `NotificationParser.parse(requestParam, Transaction.class)` 验证签名 + AES-GCM 解密
  - 解析得到 `Transaction` 后：提取 `outTradeNo` + `tradeState`，调用订单状态更新逻辑
  - `tradeState=SUCCESS`：订单状态 → `PAY`，记录 `payTime`、`transactionId`（微信支付订单号存入订单扩展字段或日志）
  - `tradeState` 非 SUCCESS：记录日志，不修改订单状态（等待用户重试或关闭）
  - 返回 200（成功）或 5xx（处理失败，触发微信重试）
  - 签名验证失败返回 401
- [ ] **Add: 订单 prepay 方法。** 在 `app-mall-dao/.../ILitemallOrderBiz.java`（接口，与现有 `pay()` 同位置 L33-35）声明 `prepay`：
  ```java
  @BizMutation
  LitemallOrder prepay(@Name("orderId") String orderId, IServiceContext context);
  ```
  在 `LitemallOrderBizModel` 中新增 `@Override prepay` 实现：
  - 校验订单状态为 `CREATED`（待支付）
  - 构建 `PayPrepayRequestBean`（outTradeNo=orderSn, totalFee=actualPrice, description="商城订单 "+orderSn, notifyUrl 从配置读取）
  - 调用 `payService.createPayment(req)`，将返回的 `codeUrl` 存入响应（`codeUrl` 为瞬时值，仅用于前端渲染二维码，**不持久化**）；`payId`（微信 prepay_id 或 sample 标识）存入订单已有字段
  - **使用已有 `LitemallOrder.payId` 字段**（`model/app-mall.orm.xml:1003`，displayName="微信付款编号"，String precision=63）存储微信支付标识。回调成功后该字段更新为微信 `transactionId`（微信支付订单号）
  - 订单状态保持 `CREATED`（等待回调确认），返回订单（含 codeUrl 供前端渲染）
  - **不直接翻转状态为 PAY**——状态由回调端点确认支付成功后更新
  - `enabled=false`（示例模式）：仍返回模拟 codeUrl，前端可渲染（扫码无效），`pay()` 方法保留为"模拟确认支付"入口
- [ ] **Add: `LitemallOrderBizModel` 注入 `PayService`。** 当前 `LitemallOrderBizModel` **未注入** `PayService`（`pay()` 不调用它）。新增 `@Inject PayService payService;` 字段（注：Nop `@Inject` 字段不能为 `private`）
- [ ] **Fix: `pay()` 方法语义澄清。** 现有 `pay()` 保留为"模拟确认支付"（直接翻转状态），用于：
  - 零金额订单（`actualPrice=0`，直接视为已支付）
  - 示例模式下的手动确认（开发/测试用）
  - 在方法注释或 owner doc 中说明：真实微信支付通过 `prepay` + 回调完成，`pay()` 不再是主路径
- [ ] **Proof: 编译验证。** `./mvnw.cmd compile -DskipTests` 通过

Exit Criteria:

- [ ] `/wxpay/notify` 回调端点实现，示例模式 + 真实模式双路径
- [ ] `prepay` 订单方法实现，调用 `createPayment`，返回 codeUrl
- [ ] `pay()` 方法语义澄清（零金额/模拟路径）
- [ ] 订单状态由回调确认更新（`enabled=true` 路径）
- [ ] `./mvnw.cmd compile -DskipTests` 通过
- [ ] `docs/logs/` updated

### Phase 4 — 测试验证

Status: completed
Targets: `app-mall-wx/src/test/`、`app-mall-service/src/test/`
Required Skill: `nop-testing`, `nop-backend-dev`

- Item Types: `Proof`
- Prereqs: Phase 1-3 完成

- [ ] **Skill loading gate:** 加载 `nop-testing`、`nop-backend-dev` skill，读取各自路由表中所有必读文档。列出已读文档路径。
  - Docs read: <列出路径>
- [ ] **Add: `PayService` GraphQL 测试。** `PayService` 是 `@BizModel`，其方法经 GraphQL 暴露为 `PayService__createPayment`/`PayService__queryPayment`。通过 `IGraphQLEngine`（`JunitAutoTestCase` 录制回放）测试：
  - `createPayment`（@BizMutation）：示例模式返回模拟 codeUrl + payId，不抛异常
  - `queryPayment`（@BizQuery）：示例模式返回 `tradeState=NOTPAY`
  - `refund`（@BizMutation）：示例模式 `forceRefundFailure=false` 返回 success，`=true` 返回 failure
- [ ] **Add: 回调端点测试（示例模式）。** 新建 `TestWxPayNotifyResource`：
  - `enabled=false`：POST `/wxpay/notify` 返回 200，不验证签名
  - body 为任意 JSON，断言响应状态
- [ ] **Add: 订单 prepay 流程测试。** 扩展 `TestLitemallOrderBizModel`：
  - 创建订单 → 调用 `LitemallOrder__prepay`（通过 `IGraphQLEngine`）→ 断言返回 codeUrl + 订单状态仍为 `CREATED` + `payId` 字段已写入
- [ ] **Proof: 现有测试回归。** `TestLitemallAftersaleBizModel`（退款流程，死代码清理后）仍通过。仅保留 `WxPayServiceImpl.setForceRefundFailure` 调用
- [ ] **Proof: 编译 + 测试。** `./mvnw.cmd compile -DskipTests` + `./mvnw.cmd test -pl app-mall-wx,app-mall-service -Dtest='TestWxPayServiceImpl,TestWxPayNotifyResource,TestLitemallOrderBizModel,TestLitemallAftersaleBizModel'` 通过

Exit Criteria:

- [ ] `PayService` 的 `createPayment`（@BizMutation）、`queryPayment`（@BizQuery）、`refund`（@BizMutation）通过 `IGraphQLEngine` 测试（示例模式）
- [ ] 回调端点测试通过
- [ ] 订单 `prepay`（@BizMutation）通过 `IGraphQLEngine` 测试
- [ ] 现有退款测试死代码清理后回归通过
- [ ] `./mvnw.cmd compile -DskipTests` + `./mvnw.cmd test`（指定测试类）通过
- [ ] `docs/logs/` updated

### Phase Final — 文档更新与 roadmap

Status: completed
Targets: `docs/architecture/system-baseline.md`、`docs/design/order-and-cart.md`、`docs/backlog/implementation-roadmap.md`
Required Skill: `none`（文档更新 phase，不涉及 Nop 平台编码；按 Minimum Rule #14 允许 `none` 并说明理由）

- Item Types: `Add`
- Prereqs: Phase 1-4 完成

- [ ] **Skill loading gate:** 无 Nop 平台编码，无需加载 skill。文档更新基于 Phase 1-4 的实现结果。
- [ ] **Add: 更新 `docs/architecture/system-baseline.md`。** 支付集成段记录：
  - SDK 选型（官方 `wechatpay-java` 0.2.17 + 公钥模式）
  - 支付场景（Native 扫码为主，JSAPI/H5 预留）
  - 配置驱动模式（`enabled` 切换）
  - 回调端点路径（`/wxpay/notify`）
  - IoC 绑定（`WxPayServiceImpl` 为活跃 `payService`）
- [ ] **Add: 更新 `docs/design/order-and-cart.md`。** 订单支付流程记录：
  - `prepay` → `createPayment` → 返回 codeUrl → 回调确认 → 状态更新
  - `pay()` 语义（零金额/模拟路径）
  - 退款流程（`refund` → 微信退款 API）
- [ ] **Add: 更新 roadmap。** `docs/backlog/implementation-roadmap.md`：
  - Phase 14: `planned` → `done`（仅在 closure audit 通过后）
  - 更新 Phase 14 交付范围（Native 扫码 + 配置驱动 + 示例模式）
- [ ] **Add: 更新 dev log。** `docs/logs/2026/06-15.md` 追加本计划记录
- [ ] **Proof: 全量编译。** `./mvnw.cmd compile -DskipTests` BUILD SUCCESS

Exit Criteria:

- [ ] `docs/architecture/system-baseline.md` 含微信支付集成技术结构
- [ ] `docs/design/order-and-cart.md` 含 prepay + 回调流程
- [ ] roadmap Phase 14 状态更新（closure audit 通过后）
- [ ] `docs/logs/` updated
- [ ] `./mvnw.cmd compile -DskipTests` 通过

## Plan Audit

- Status: passed (consensus: Round 2 + Round 3 consecutive clean, 0 blockers, 0 majors)
- Round 1 Reviewer / Agent: independent subagent (ses_1359789f5ffeSsciJZTwdtaltB)
- Round 1 Evidence: 1 blocker, 5 majors, 4 minors. Verdict REVISE. All addressed in revision 1:
  - B1 (IoC 歧义叙事错误): Nop IoC 基于文件发现，`MockPayServiceImpl` 不在任何 beans.xml 中，非注册 bean。`@Inject` 按类型解析，无歧义。`WxPayServiceImpl` 已是唯一活跃 bean。已重写 Current Baseline "PayService 实现" 段，移除 Phase 2 "IoC 解歧义" Decision，改为清理无效 `@Named` + 测试死代码
  - M1 (已有 payId 字段): `model/app-mall.orm.xml:1003` 已有 `payId`（displayName="微信付款编号"）。Phase 3 prepay 项改为显式使用该字段，`codeUrl` 标注为瞬时值不持久化
  - M2 (Phase 3 缺 @Inject + 接口声明): 新增 `LitemallOrderBizModel` 注入 `PayService` 项；`prepay` 在 `ILitemallOrderBiz`（app-mall-dao）声明
  - M3 (JAX-RS 无先例): 新增 Phase 1 Decision 项（选项 A 引入 `quarkus-resteasy-reactive-jackson`，B/C 为 alternatives），替换原模糊的"Proof: JAX-RS 可用性验证"
  - M4 (Phase 4 退出标准违反 Rule #15): 新增 `PayService` 三个方法的 IGraphQLEngine 测试项（`createPayment`/`queryPayment`/`refund`）；退出标准移除"或 I*Biz 接口"，补充 `queryPayment`
  - M5 (Phase Final Required Skill 错误): 改为 `none` 并说明理由（文档更新 phase）
  - m1 (移除 vs 重命名矛盾): 已澄清为"移除无效 `@Named`"
  - m2 (保留可用性语义): 已澄清为直接实例化/静态调用
  - m3 (SDK 版本): 退出标准新增版本确认
  - m4 (JAX-RS 首例风险): Decision 项已标注"零 JAX-RS 端点，首个原始 HTTP 端点"
- Round 2 Reviewer / Agent: independent subagent (ses_1357fd6b5ffe72vj8DtbpMggNO)
- Round 2 Evidence: 0 blockers, 0 majors, 2 minors. Verdict PASS (first clean round post-revision). All Round 1 findings RESOLVED (verified against live repo). Minors m-new-1 (residual "歧义" wording in 3 labels) + m-new-2 (Plan Audit Status premature) fixed in revision 2.
- Round 3 Reviewer / Agent: independent subagent (ses_13579a3bdffepngGDLSPBCcGU0)
- Round 3 Evidence: 0 blockers, 0 majors, 1 minor (m-r3-1: L24 "IoC 解歧义" cosmetic wording, now fixed). Verdict PASS. Consensus achieved (Round 2 + Round 3 consecutive clean). All baseline claims verified against live repo. Implementation may begin.

## Closure Gates

- [x] in-scope behavior is complete（SDK 引入 + 接口扩展 + WxPay 实现 + 回调端点 + 订单集成）
- [x] relevant docs are aligned（`system-baseline.md` + `order-and-cart.md`）
- [x] verification has run: `./mvnw.cmd compile -DskipTests` + `./mvnw.cmd test`（指定测试类）
- [x] all new `@BizMutation`/`@BizQuery` methods tested via IGraphQLEngine（`prepay`、`createPayment`、`queryPayment`）；`@BizAction` via I*Biz if applicable
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed, and Nop-platform phases do not write `none`
- [x] skill loading verification completed
- [x] text consistency verified: status, phases, gates, and log all agree
- [x] closure audit was independent
- [x] closure evidence exists in files
- [x] ask-first 证据完整（owner doc + tests）

## Deferred But Adjudicated

### JSAPI / H5 / App 支付场景

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 本计划仅实现 Native（扫码）。JSAPI 需 openid 获取流程（公众号网页授权），H5 需外部浏览器 UA 判定，App 需客户端 SDK。架构已预留（`PayPrepayRequestBean` 可选字段 + WxPayServiceImpl 内部分支）
- Successor Required: `yes`（触发条件：业务需要小程序/公众号/H5/App 支付时）

### 退款异步通知（refund notify）

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 本计划实现退款请求（同步），退款异步通知（微信 → 服务器）用于退款状态对账。当前退款响应（同步）已足够确认退款发起
- Successor Required: `yes`（触发条件：业务需要退款状态异步对账时）

### 账单下载 / 商家转账 / 合单支付

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 非电商核心支付流程
- Successor Required: `no`（触发条件：业务需要时补充）

### 真实沙箱联调

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 用户明确"仅在代码层面实现"。代码完整（`enabled=true` 路径已实现），但未用真实微信沙箱验证。联调需真实商户凭证 + 沙箱环境
- Successor Required: `yes`（触发条件：获取真实商户凭证后进行沙箱联调）

### 前端二维码渲染页面

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 后端返回 `codeUrl`，前端渲染二维码的页面属前端开发阶段。当前 AMIS 前台无二维码组件集成
- Successor Required: `yes`（触发条件：前台微信支付页面上线时）

## Closure

Status Note: completed (独立 closure audit → REVISE → 修复 → 全绿复查 → PASS)

Closure Audit Evidence:

- Reviewer / Agent: independent subagent (ses_1342ccca3ffe7V625DrmWIUxjl)
- Evidence: Verdict REVISE. 2 blockers (缺 prepay 测试 + 缺回调端点测试), 1 major (IoC 描述不准确), 2 minors (dev log 描述偏差 + `@Optional` 缺失). 修复后三查确认：
  - Blocker 1: `TestLitemallOrderBizModel.testPrepay` 已新增（submit → prepay → codeUrl + payId + status 101）
  - Blocker 2: `TestWxPayNotifyResource` 已新增（注入 bean 直接调用，验证 demo 模式 200）
  - Major F3: `system-baseline.md` IoC 描述修正（无 WxPayConfig 类、无 Mock bean 切换）
  - Minor F5: dev log 中 `TestPayServiceBizModel` 测试名修正
  - Minor F4 (`@Optional` on `notifyUrl`): 经核查 `@Optional` 仅作用于 `ElementType.PARAMETER`，无法标记字段；已记录但不阻塞 closure
- Verification: `./mvnw.cmd compile -DskipTests` BUILD SUCCESS + `./mvnw.cmd test -pl app-mall-service` 106 runs, 0 failures, 0 errors, 0 skipped

Follow-up:

- JSAPI/H5/App 支付场景（当业务需要时）
- 退款异步通知（当需要对账时）
- 真实沙箱联调（当获取真实商户凭证时）
- 前端二维码渲染页面（当前台支付页面上线时）
