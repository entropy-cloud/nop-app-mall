# 测试覆盖与质量审计提示词（nop-app-mall 应用层）

> **定位**：本提示词用于专项深审单元测试、集成测试与 e2e 测试的覆盖范围与有效性，发现真实保护力缺口并产出可排序、可执行的加固建议（hardening backlog）。
>
> **与多维审计维度 10「验证充分性」的关系**：维度 10 在多维审计中做快速过一遍；本提示词是专项深审，聚焦测试本身的质量、覆盖深度、断言有效性与失败追踪，产出更细的问题清单与加固 backlog。两者可串联使用：维度 10 暴露线索后，用本提示词深挖。
>
> **前提**：以 live test code + live doc 为准。执行前先阅读 `AGENTS.md`、`docs/index.md`、`docs/context/project-context.md`、`docs/testing/`，以及审计范围相关的 owner 文档（`docs/design/`）与 BizModel 源码。

## 何时使用

- 需要确认核心业务逻辑是否被测试真实保护（而非"看起来测试很多"）
- 需要评估测试质量（断言有效性、边界覆盖、失败路径覆盖）
- 失败测试长期存在，需诊断是否被正确追踪与记录
- 计划测试加固，需要一份可排序的缺口清单
- 重大域逻辑变更后（订单/库存/支付/优惠券/售后/拼团），回归充分性评估
- 阶段闭合或里程碑前，验证"已完成"阶段的测试可信度

## 何时不用

- 只改文案/样式的低风险变更
- 只需快速冒烟（用维度 10 即可）
- 生成代码的测试（`_gen/`、`_` 前缀产物）

## 本项目测试栈基线（执行前先确认实际形态，勿盲目假设）

- **后端单元/集成测试**：`app-mall-service/src/test/java/`。两种合规形态并存，审计前先确认被审范围用的是哪种：
  - 形态 A（本项目主流）：`extends JunitBaseTestCase` + `@NopTestConfig(localDb=true, initDatabaseSchema=true)`，通过 `IGraphQLEngine.executeRpc()` 调 GraphQL mutation/query，断言 `ApiResponse.status` 与返回数据；测试数据在 `@BeforeEach` 用 `daoProvider` 构造。这是真实集成测试（真实 local DB + 真实引擎）。
  - 形态 B（录制回放）：`extends JunitAutoTestCase` + `request.json5`/`response.json5` 快照比对（若存在）。
- **e2e**：`e2e/`，Playwright + TypeScript，经 HTTP `/r/{Entity}__{action}` 与 `/graphql` 端点验证。
- **验证命令**：以 `docs/context/project-context.md` 为准（后端 `./mvnw test -pl app-mall-service -am`；e2e 见 `e2e/package.json`）。若命令为占位符，先修正再审计。
- **测试基线记录**：`docs/testing/known-good-baselines.md`（passed 与 Known Failures 必须真实分开）。
- **测试策略文档**：`docs/testing/index.md`（若仍为空模板，这本身就是一个发现）。

## 审计维度

1. **核心逻辑覆盖广度**：已完成阶段（done）核心 BizModel 的每个 `@BizMutation`/`@BizQuery` 方法是否有对应测试；订单/库存/支付/优惠券/售后/拼团状态机的每个合法与非法转移是否有覆盖。
2. **覆盖深度**：是否只覆盖 happy path；边界值、非法输入、并发竞态（库存超卖、优惠券重复领取）、资源不可用、幂等性（支付回调重放）、状态机非法转移是否覆盖。
3. **断言有效性**：是否存在"弱断言"——只 `assertNotNull`、只检查 `status=0` 不校验返回数据、只遍历不验业务规则、getter/setter 测试；断言能否真正抓住回归。
4. **失败测试追踪**：长期失败的测试是否在 `known-good-baselines.md` 的 Known Failures 中如实记录并附原因与证据；是否有"失败但未记录"或"虚标 passed"；是否做过 clean baseline（git stash）验证。
5. **集成与 e2e 覆盖**：端到端业务流程是否贯通（不只是页面渲染 smoke）；关键用户旅程（下单→支付→发货→确认→评价、售后、优惠券领用抵扣、拼团、退款取消）是否有 e2e 串联。
6. **测试数据与隔离**：`@BeforeEach` 数据是否充分支持被测场景；测试间是否有隐式耦合（依赖其他测试创建的数据）；共享静态数据是否导致顺序依赖。
7. **测试反模式**：测试绕过公共接口直接调内部私有方法、硬编码 `Thread.sleep`/时间、断言依赖实现细节而非契约、Mock 了不该 Mock 的真实集成。
8. **测试可维护性**：是否依赖过时的录制快照、是否有重复样板可抽取、高价值方法是否缺少说明测试意图的注释。

## 必读输入

- `docs/testing/`（index、known-good-baselines、00-testing-note-guide）
- `docs/context/project-context.md`（验证命令、AI 阻断条件、保护区域）
- `docs/backlog/implementation-roadmap.md`（区分 done / todo）
- `docs/design/`（核心域：order-and-cart、marketing-and-promotions、product-catalog、user-and-address）
- 审计范围内的 BizModel 源码（对照测试是否覆盖每个公开方法）
- 对应测试类与 `src/test/resources/`（录制回放文件若存在）

## 执行步骤

1. **建基线**：先跑 `./mvnw test -pl app-mall-service -am` 拿到真实通过/失败数；记录每个失败用例及其报错。
2. **画覆盖矩阵**：列出审计范围内每个 BizModel 的公开方法（`@BizMutation`/`@BizQuery`），标记每个方法是否有测试、几条用例、是否覆盖合法+非法转移。
3. **逐维度检查**：按上述 8 维度逐项审查，重点维度 1/2/3（广度/深度/断言）与维度 4（失败追踪）。
4. **抽样深读**：至少深读 3 个核心测试类（订单/支付/售后），判断断言能否真正抓住回归（删掉一行实现代码，测试会不会红）。
5. **交叉验证**：把"无测试"方法与 roadmap 阶段对照——已完成阶段的无测试是发现，未开始阶段（todo）的无测试是预期状态。
6. **核对基线真实性**：检查 `known-good-baselines.md` 是否最近更新、Known Failures 是否与实跑结果一致、是否有"虚标 passed"。
7. **产出加固建议**：把缺口转成可排序、可执行的测试用例建议（见输出格式）。

## 审计规则

- 发现优先，按严重程度排序
- 区分三类输出：**确认缺陷**（测试错误/断言错误）、**覆盖缺口**（应有但无）、**加固建议**（可提升保护力）
- 引用 `文件路径:行号` 定位
- 对"无测试"必须指出该方法的业务风险（资金/数据完整性/状态一致性），不只是"缺测试"
- 区分生成代码测试与手写测试；只审计手写部分
- 零发现时须明确说明检查范围、抽样文件与已检查维度

## 严重程度判级（测试特化）

- **P0**：核心资金/数据完整性逻辑（订单状态机推进、库存扣减、支付回调、退款、优惠券核销）零测试或断言无效（虚假绿）；AI 阻断保护区域逻辑无回归网。
- **P1**：关键业务路径某状态转移无测试；失败测试长期未修且未在 Known Failures 记录；断言只验 status 不验数据导致保护力形同虚设；已完成阶段核心方法零覆盖。
- **P2**：边界/并发/幂等/非法转移场景缺失；弱断言（assertNotNull 遍历）；测试间隐式耦合；e2e 只 smoke 不串联业务。
- **P3**：测试可读性/重复样板/可维护性；缺少测试意图注释。

## 误报校准（以下不是问题）

- **本项目标准测试模式**：`JunitBaseTestCase` + `@NopTestConfig(localDb=true)` + `IGraphQLEngine.executeRpc()` 调 GraphQL + `@BeforeEach` 用 `daoProvider` 构造数据——这是真实集成测试，不是反模式。
- 录制回放快照文件本身不审计（除非追溯到手写代码与快照契约矛盾）。
- `_gen/`、`_` 前缀生成测试不审计。
- 未开始阶段（roadmap = todo）的空 stub 无测试是预期状态，不报告；仅已完成阶段（done）的无测试才是发现。
- 本项目当前 Lint/static check 与 e2e 若标记为 none 是已知状态，不作为发现重复报告；但"已完成阶段零单元测试"仍是有效发现。
- 测试方法名 `testXxx` 命名、测试类继承 `JunitBaseTestCase` 或 `JunitAutoTestCase`——平台两种基类都合规。
- 测试在 `@BeforeEach` 设置固定 userId（如 "1"）——local DB 隔离下的标准做法。
- AutoTest 快照比对是平台标准模式，快照存在本身不算问题。

## 加固建议（hardening backlog）输出格式

把所有缺口整理为可排序清单：

```markdown
### 加固建议（按优先级）

| # | 优先级 | 目标方法/流程 | 缺口类型 | 建议用例 | 关联风险 |
|---|--------|--------------|---------|---------|---------|
| 1 | P0 | LitemallOrder__refund | 无测试 | 退款>实付拒、退款失败状态不推进、退款幂等 | 资金/状态一致性 |
| 2 | P1 | e2e: 发货→确认→评价串联 | e2e 缺串联 | 补 ship/confirm/comment e2e | 履约链路回归无网 |
```

缺口类型枚举：`无测试` / `弱断言` / `缺边界` / `缺并发` / `缺幂等` / `缺非法转移` / `失败未追踪` / `e2e 缺串联` / `虚标 passed`

## 发现条目格式

```markdown
### [T-{序号}] 简短标题
- **位置**: `文件路径:行号` 或 `某方法无测试`
- **证据**: 3-10 行代码/测试片段，或"无对应测试文件"
- **严重程度**: P0/P1/P2/P3
- **现状**: 当前测试（或无测试）的情况
- **风险**: 不修会漏掉什么回归（业务风险，不只是"少测试"）
- **建议**: 应补什么用例 / 如何修断言
- **信心水平**: 确定 / 很可能 / 猜测
```

## 本项目高频缺口（实战已观察到，优先排查是否仍存在或已收敛）

> 审计时优先检查以下模式。若已收敛，在报告中注明"已修复"；若仍存在，按判级上报。

- **失败测试追踪真空**：delta 模块 auth 测试（`TestPasswordReset`/`TestNopAuthUserProfile`/`TestLoginApiSignUp`）曾因 userType 字典冲突（`非法的字典项:0`）长期失败——确认是否仍在 Known Failures、是否有修复 plan、clean baseline 是否复现。
- **e2e 仅 happy-path 前半段**：下单→支付(201) 之后无发货/确认/评价串联；售后/优惠券领用抵扣/拼团/退款取消无 e2e。
- **页面 smoke ≠ 业务验证**：`storefront-pages` 只验页面能加载，不验业务行为。
- **核心方法无专门测试类**：曾观察到 `LitemallGrouponRulesBizModel`、`LitemallOrderGoodsBizModel` 缺测试类——复查是否已补。
- **无测试策略文档**：`docs/testing/index.md` 若仍为空模板，记为 P2（应定义覆盖目标与分层策略：单元/集成/e2e 各自职责）。
