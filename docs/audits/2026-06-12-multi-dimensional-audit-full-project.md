# 多维审计报告：nop-app-mall 全项目审计

> **日期：** 2026-06-12
> **审计员：** 主代理（subagent 辅助采集）
> **审计风格：** multi-dimensional audit（基于 `docs/skills/multi-dimensional-audit-prompt.md`）
> **范围：** nop-app-mall 全项目（已完成阶段 Phase 1-8/10 + 未开始阶段 Phase 9/11-14）

---

## 总体结论

**passes multi-dimensional audit with residual risks**

无阻断性阻塞项，但存在 2 个高风险发现、6 个中风险发现和若干低风险残留。项目已实现 11/14 个阶段，代码质量和文档体系整体健康，主要风险集中在实体消除未完成、退款路径缺陷、测试覆盖缺口和计划关闭遗漏。

---

## 发现汇总（按严重度排序）

### HIGH — 高风险发现

#### H1. 退款结果被丢弃 — 数据完整性缺陷

- **文件：** `app-mall-service/src/main/java/app/mall/service/entity/LitemallAftersaleBizModel.java:114`
- **问题：** `payService.refund()` 返回值 `PayRefundResponseBean` 被完全忽略。无论退款成功或失败，售后状态都会推进到 `REFUND`（line 116）。
- **影响：** 退款失败时售后单仍标记为已退款，造成资金与状态不一致。
- **建议：** 检查 `refund()` 返回值，失败时抛出 `NopException` 阻止状态推进。

#### H2. 实体消除未完成 — Phase 1 关闭缺口

- **设计文档声明：** `docs/design/user-and-address.md:69-79` 声明 `LitemallAdmin`、`LitemallRole`、`LitemallPermission` 已消除。
- **实际状态：**
  - `model/app-mall.orm.xml` 仍包含 `LitemallAdmin`（line 170-195）、`LitemallPermission`（line 1023-1039）、`LitemallRole`（line 1070-1088）三个实体定义。
  - `app-mall-service/src/main/java/app/mall/service/entity/` 下仍存在 `LitemallAdminBizModel.java`、`LitemallRoleBizModel.java`、`LitemallPermissionBizModel.java`（空 stub）。
  - `app-mall-web` 下仍存在 `LitemallAdmin`、`LitemallRole`、`LitemallPermission` 的 view.xml。
  - `app-mall-meta/_templates/` 下仍存在对应模板文件。
- **路线图状态：** Phase 1 标记为 `done`，但实体消除只完成了一半（`LitemallUser` 和 `LitemallUserRole` 已消除，其余 3 个未消除）。
- **建议：** 在下一次 model 变更时完成消除，或明确记录保留理由并更新设计文档。

---

### MEDIUM — 中风险发现

#### M1. 测试覆盖缺口

| 已完成阶段 | 缺失测试 | 影响实体 |
|-----------|---------|---------|
| Phase 3（地址管理） | 无 `TestLitemallAddressBizModel.java` | `LitemallAddressBizModel`（155 行 CRUD + 默认地址 + 数量限制） |
| Phase 5b（支付集成） | 无支付流程测试 | `MockPayServiceImpl` |
| Phase 10（内容营销） | 无任何测试 | `LitemallTopicBizModel`、`LitemallAdBizModel`、`LitemallIssueBizModel`、`LitemallFeedbackBizModel` |
| Phase 2（部分） | 无 Category/Brand 测试 | `LitemallCategoryBizModel`（有删除保护逻辑） |

**已有测试：** 10 个 BizModel 测试类（Goods、Cart、Order、Aftersale、SearchHistory、Collect、Footprint、Comment、Coupon、CouponUser）+ 2 个 Delta 测试（SignUp、Profile）。

**建议：** 至少为 `LitemallAddressBizModel`（Phase 3 核心逻辑）和 `LitemallTopicBizModel`（Phase 10 有业务逻辑）补充测试。

#### M2. 订单管理后台页面为空壳

- **文件：** `app-mall-web/src/main/resources/_vfs/app/mall/pages/LitemallOrder/LitemallOrder.view.xml`（19 行）
- **问题：** Phase 5 订单核心流程标记为 `done`，但订单管理后台页面仅继承默认值，缺少：
  - 状态筛选（按订单状态 tab 分组）
  - 发货操作（填写物流信息）
  - 退款操作按钮
  - 价格构成展示
- **同样为空壳的关键页面：** `LitemallAddress`、`LitemallCouponUser`、`LitemallKeyword`
- **建议：** 在前端集中开发阶段优先定制订单管理页面。

#### M3. 中文化计划未正式关闭

- **文件：** `docs/plans/2026-06-02-design-doc-chinese-normalization-plan.md`
- **状态：** 标记为 `in progress`，所有退出标准 checkbox 未勾选，closure audit 未执行。
- **实际：** 所有 9 个设计文档已完成中文化（`docs/logs/2026/06-02.md` 确认）。
- **偏差：** 计划明确排除 `docs/design/README.md`，但执行时翻译了该文件，且未更新计划。
- **建议：** 补充关闭检查并关闭计划，或记录偏差说明。

#### M4. 订单状态标签设计文档与 ORM 字典不一致

- **设计文档：** `docs/design/order-and-cart.md:73` 使用"待支付"、"用户取消"、"已申请退款"等标签。
- **ORM 字典：** `model/app-mall.orm.xml` 的 `mall/order-status` 字典使用"未付款"(101)、"已取消"(102)、"订单取消，退款中"(202)。
- **额外状态：** ORM 字典有"已超时团购"(204)，设计文档未提及。
- **建议：** 对齐标签文字，以 ORM 字典为准（设计文档自身声明"持久化状态码字典由 model/app-mall.orm.xml 维护"）。

#### M5. @SqlLibMapper 直接注入未记录理由

- **位置（3 处）：**
  - `LitemallOrderBizModel.java:60` — 注入 `LitemallGoodsProductMapper`（reduceStock/addStock）
  - `LitemallAftersaleBizModel.java:59` — 注入 `LitemallGoodsProductMapper`（addStock）
  - `LitemallGoodsBizModel.java:34` — 注入 `LitemallGoodsMapper`（syncCartProduct）
- **问题：** Nop 平台规范要求"仅在 I*Biz 无法满足需求时使用 @SqlLibMapper，并注释说明原因"。这三处使用合理（原子 SQL 操作、批量同步），但缺少注释。
- **建议：** 每处添加一行注释说明为何绕过 I*Biz。

#### M6. 计划关闭审计缺失

除中文化计划外，检查所有已完成计划：
- `2026-06-12-next-execution-slice-plan.md` — completed，closure audit 已执行。
- `2026-06-12-phase-7-10-interactive-coupon-content-plan.md` — completed，closure audit 已执行。
- `2026-06-11-order-core-flow-plan.md` — completed，closure audit 已执行。
- `2026-06-09-phase1-user-registration-login-plan.md` — completed，closure audit 已执行。
- `2026-06-02-design-doc-chinese-normalization-plan.md` — **in progress**，closure audit **缺失**。

---

### LOW — 低风险残留

#### L1. WxPayServiceImpl 为空壳

- **文件：** `app-mall-wx/src/main/java/app/mall/wx/WxPayServiceImpl.java`
- **状态：** 仅包含 stub `refund()` 方法，无实际微信 SDK 集成。Phase 14 标记为 `todo`，这是预期状态。
- **无风险：** 无 SDK 依赖、无密钥泄露。

#### L2. 4 个设计文档标题含英文残留

- `user-and-address.md:34` — `### Delta 扩展字段`
- `product-catalog.md:65` — `### Goods（SPU）规则`
- `product-catalog.md:73` — `### SKU 规则`
- `marketing-and-promotions.md:198` — `### FAQ 业务规则`

这些是平台/产品术语嵌在中文标题中，语义可接受，但中文化计划的验证 grep 会标记它们。

#### L3. 架构文档为英文、设计文档为中文

- `docs/architecture/` 全英文，`docs/design/` 全中文。
- 中文化计划明确排除 architecture 目录，属设计意图。
- 跨文档引用时存在语言切换，但不影响理解。

#### L4. module-boundaries.md 提到 app-mall-app/src/test/ 为空

- `docs/architecture/module-boundaries.md:84` 提到"Integration tests in `app-mall-app/src/test/`"，但该目录实际为空。测试集中在 `app-mall-service/src/test/`。
- **建议：** 更新该文件反映实际测试位置。

#### L5. 无实际 bug/lessons/discussion 记录

- `docs/bugs/` 仅有写作指南模板，无实际 bug 记录。
- `docs/lessons/` 仅有 README，无实际 lesson 文件。
- `docs/discussions/` 仅有指南，无实际讨论记录。
- 开发过程经历了多次 bug 修复和设计迭代，但未提升为可复用的教训记录。

#### L6. 13 个 view.xml 为空壳

以下实体的自定义 view 仅继承默认值，无业务定制：

`LitemallOrder`、`LitemallAddress`、`LitemallCouponUser`、`LitemallFootprint`、`LitemallKeyword`、`LitemallSearchHistory`、`LitemallUser`、`LitemallAdmin`、`LitemallRole`、`LitemallPermission`、`LitemallNotice`、`LitemallNoticeAdmin`、`LitemallLog`、`LitemallStorage`、`LitemallSystem`

这些大多属于未开始阶段（Phase 11-13）或待消除实体，部分属于已完成阶段（Phase 3/5/8），风险可控。

---

## 各维度评估

### 1. 需求正确性（Requirement Correctness）

**评级：Good**

- `docs/requirements/commercial-baseline.md` 覆盖了全部 14 个阶段的业务需求。
- 原始输入 `docs/input/litemall-requirements.md`（1876+ 行）与综合需求之间无明显的范围遗漏。
- 已完成阶段的需求与实现代码一致。

**残留风险：** 团购（Phase 9）和微信支付（Phase 14）的需求描述在设计文档中已就绪，但未细化到实现级别。

### 2. Owner-Doc 对齐（Owner-Doc Alignment）

**评级：Moderate**

- 9 个设计文档覆盖全部业务领域，与路由表一致。
- **H2 问题：** 设计文档声明 3 个实体已消除，但 ORM 模型仍保留它们。
- **M4 问题：** 订单状态标签在设计和 ORM 之间有文字差异。
- 其余设计文档与代码实现高度对齐。

### 3. 架构与边界影响（Architecture and Boundary Impact）

**评级：Good**

- 模块边界清晰，`module-boundaries.md` 准确描述了 9 个模块的职责和依赖约束。
- 无跨模块违规（app-mall-web 不直接访问 app-mall-dao）。
- Delta 机制正确使用（`app-mall-delta` 仅依赖 nop-auth 模块）。
- 代码生成规范遵循良好（无手动修改 `_gen` 文件的迹象）。

**残留风险：** `MallLogManager` 是跨模块日志工具类，未在 `module-boundaries.md` 中记录。

### 4. 验证充分性（Verification Adequacy）

**评级：Moderate**

- 编译检查 `./mvnw compile -DskipTests` 在所有已完成计划中通过。
- 有 13 个测试文件覆盖核心业务逻辑。
- **M1 问题：** Phase 3（地址）、Phase 5b（支付）、Phase 10（内容营销）无测试覆盖。
- 无 E2E 测试框架。
- `docs/testing/known-good-baselines.md` 未更新。

### 5. 回归风险（Regression Risk）

**评级：Low**

- 已完成阶段之间的依赖关系正确（Phase 1→2→3→4→5→5b→5c 等）。
- **H1 问题：** 退款路径的缺陷可能导致未来微信支付集成时回归。
- 未开始阶段（Phase 9/11-14）不会对已完成阶段产生回归风险。
- ORM 模型变更通过代码生成流程保护，手动修改风险低。

### 6. 路由和技能选择正确性（Routing and Skill-Selection Correctness）

**评级：Good**

- `docs/index.md` 路由表完整，覆盖所有常见工作场景。
- 技能集合（13 个技能）覆盖了后端开发、前端开发、测试、ORM 建模、调试等主要工作类型。
- `AGENTS.md` 的强制技能加载规则清晰。
- `docs/backlog/README.md` 的选择规则与自主性政策一致。

**残留风险：** 技能 `nop-nodejs-backend` 似乎与当前 Java 项目不匹配，可能是遗留。

### 7. Backlog 与自主性政策漂移（Backlog and Autonomy Policy Drift）

**评级：Good**

- `docs/backlog/implementation-roadmap.md` 状态索引与实际代码实现一致（除 H2 实体消除缺口外）。
- 自主性政策正确定义了 5 个保护区域（微信支付、数据删除、Auth/权限、XML 模型、数据库 schema）。
- 所有已完成计划的自主性标签与实际执行路径一致。

**残留风险：** Phase 1 标记为 `done` 但实体消除未完成，属于 backlog 状态漂移。

---

## 审计维度补充：项目特定维度

### 8. 安全与保护区域（Security and Protected Areas）

**评级：Good**

- 无密钥或凭证泄露（`app-mall-wx` 无实际 SDK 集成，无 appId/secret）。
- 支付路径受保护区域约束（`ask-first`）。
- Delta 扩展的注册接口 `signUp` 正确使用 `@BizMutation` + 公开访问注解。
- 错误处理全部使用 `NopException` + `ErrorCode`，无 `RuntimeException` 泄露。

### 9. 代码规范遵循（Code Convention Compliance）

**评级：Good**

- 全部 37 个错误码遵循 `nop.err.mall.<domain>.<specific>` 命名。
- 全部 BizModel 类正确继承 `CrudBizModel<T>`。
- 跨实体访问全部通过 `I*Biz` 接口注入（除 M5 的 3 处 `@SqlLibMapper` 合理使用）。
- 代码注释保持最小化，无冗余注释。

### 10. 文档完整性（Documentation Completeness）

**评级：Good**

- 4 层文档体系（context → requirements → design → architecture）完整。
- 审计方法论文档完备（`docs/audits/00-audit-execution-guide.md` + 8 个审计提示词）。
- 日志、计划、技能体系均按规范维护。
- **缺口：** `docs/bugs/`、`docs/lessons/`、`docs/discussions/` 无实际记录（L5）。

---

## 行动建议（按优先级）

| # | 优先级 | 行动 | 关联发现 |
|---|--------|------|---------|
| 1 | P0 | 修复退款结果检查逻辑 | H1 |
| 2 | P1 | 完成实体消除或记录保留理由，更新 ORM 模型 | H2 |
| 3 | P1 | 为 `LitemallAddressBizModel` 补充测试 | M1 |
| 4 | P1 | 关闭中文化计划（补充 closure audit） | M3 |
| 5 | P2 | 对齐订单状态标签（设计文档 vs ORM 字典） | M4 |
| 6 | P2 | 为 Phase 10 有逻辑的 BizModel 补充测试 | M1 |
| 7 | P2 | 定制订单管理后台页面 | M2 |
| 8 | P3 | 为 3 处 @SqlLibMapper 添加注释 | M5 |
| 9 | P3 | 更新 `module-boundaries.md` 测试位置描述 | L4 |
| 10 | P3 | 考虑将开发过程中的 bug/教训提升为正式记录 | L5 |

---

## 审计范围限制

- 本审计基于静态代码和文档分析，未运行测试或启动应用。
- 未深入检查每个 BizModel 方法的业务逻辑正确性（仅检查了模式和规范）。
- 前端 AMIS 页面的视觉和交互正确性未审计。
- `nop-entropy` 平台核心代码不在审计范围内。
- 安全审计（SQL 注入、XSS、CSRF 等）未深入执行，仅检查了保护区域合规性。

---

## 项目健康度指标

| 指标 | 值 | 评级 |
|------|-----|------|
| 已完成阶段 | 11/14（79%） | ★★★★ |
| 实体数 | 35（ORM） | — |
| BizModel 类 | 36 | — |
| 测试文件 | 16 | ★★★ |
| 设计文档覆盖 | 9/9 领域 | ★★★★★ |
| 计划完成率 | 9/10 | ★★★★ |
| 代码规范违反 | 0 严重 + 3 轻微 | ★★★★★ |
| 安全问题 | 0 泄露 + 1 逻辑缺陷 | ★★★★ |
| 文档-代码对齐 | 2 不一致 | ★★★★ |
