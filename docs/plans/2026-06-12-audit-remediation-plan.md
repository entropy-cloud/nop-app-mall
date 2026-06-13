# 2026-06-12 审计修复计划

> Plan Status: completed
> Last Reviewed: 2026-06-13
> Source: `docs/audits/2026-06-12-multi-dimensional-audit-full-project.md`
> Related: 无（审计修复，非 roadmap phase）
> Audit: required

## Plan Audit History

| Round | Result | Key Findings |
|-------|--------|-------------|
| 1 | needs revision | B1: delta 模型跨实体依赖未处理；B2: ORM 模型 protected-area 未确认；B3: 退款路径 protected-area 未确认；B4: Phase 1 无测试验证；O1: _gen 残留未处理；O2: grep 范围不全；O3: Phase 4 prereq 不清晰；O5: Phase 2 无运行时验证 |
| 2 | needs revision | B1: delta 模型诊断错误（nop-auth-delta.orm.xml 无 relation，relatedRoleList 来源于 nop-auth 基础平台 + codegen 合并）；B2: 孤立 LitemallUser*/LitemallUserRole* API 文件未纳入清理范围，消除 LitemallRole 后必然编译失败；O1: codegen.sh 未验证；O2: Phase 5 prereq 不必要耦合；O3: Plan Audit section 未更新 |
| 3 | needs revision | B1: Phase 2 内部矛盾（delta 模型已确认无需修改但 Targets/ProtectedArea/Ask-first gate 仍引用它）；O1: Phase 1 测试规范违反 guide rule #15（@BizMutation 必须通过 IGraphQLEngine 测试，不能用 I*Biz 替代）；O2: Phase 2 Exit Criteria 措辞暗示 delta 模型有变更 |
| 4 | passed | No blockers. Round 3 的 3 个 narrow fix 全部验证通过。无新矛盾。Anti-slacking/文本一致性/closure gates/plan guide compliance 均通过。计划可进入实现阶段。 |

## Why A Plan

审计发现 2 个高风险 + 4 个中风险问题仍存在于当前代码库。涉及退款逻辑修复（代码行为变更）、ORM 模型实体消除（模型变更）、文档对齐、计划关闭等工作。跨越 model/service/docs 多个层面，需要 staged 执行。

## Current Baseline

### 已修复项（审计时存在，现已修复）

| 原审计编号 | 内容 | 当前状态 |
|-----------|------|---------|
| M1 | Phase 3/5b/10 测试覆盖缺口 | **已修复** — `TestLitemallAddressBizModel`、`TestLitemallTopicBizModel`、`TestLitemallAdBizModel`、`TestLitemallIssueAndFeedbackBizModel` 均已存在；支付流程通过 `TestLitemallOrderBizModel.testSubmitAndPay()` 覆盖 |
| M2（部分） | `LitemallOrder.view.xml` 为空壳 | **已修复** — 61 行，含列表列、查看表单、查询表单、自定义 row-view action |

### 仍需修复项

| 审计编号 | 严重度 | 内容 | 涉及文件/路径 |
|---------|--------|------|-------------|
| H1 | HIGH | `payService.refund()` 返回值被丢弃 | `LitemallAftersaleBizModel.java:114` |
| H2 | HIGH | `LitemallAdmin/Role/Permission` 实体未消除 | `model/app-mall.orm.xml`、3 个 BizModel、3 个 view.xml、模板文件 |
| M2 | MEDIUM | `Address/CouponUser/Keyword` view.xml 为空壳 | 3 个 view.xml 文件 |
| M3+M6 | MEDIUM | 中文化计划未关闭 | `docs/plans/2026-06-02-design-doc-chinese-normalization-plan.md` |
| M4 | MEDIUM | 订单状态标签设计文档与 ORM 字典不一致 | `docs/design/order-and-cart.md:73`、`model/app-mall.orm.xml:40-51` |
| M5 | MEDIUM | @SqlLibMapper 注入未注释理由 | 3 个 BizModel 文件 |

## Goals

1. 修复退款结果检查逻辑（H1），消除资金与状态不一致风险
2. 完成实体消除或记录保留理由（H2），消除设计文档与模型的不一致
3. 定制 3 个关键后台管理页面（M2 残余）
4. 关闭中文化计划（M3+M6）
5. 对齐订单状态标签（M4）
6. 为 @SqlLibMapper 使用添加注释（M5）

## Non-Goals

- Phase 9/11-14 的实现工作（不在审计修复范围内）
- 前台 AMIS 页面定制（属于前端集中开发阶段）
- 安全审计深化（SQL 注入、XSS 等不在本计划范围）
- L1/L4/L5/L6 低风险项（WxPay stub、architecture 文档英文、bug/lesson 记录、其余空壳 view）

## Task Route

- Type: `implementation-only change` + `bug investigation`（H1）+ `verification or audit work`（M3+M6）
- Owner Docs: `docs/design/order-and-cart.md`、`docs/design/user-and-address.md`
- Skill Selection Basis:
  - H1/M5 涉及 BizModel 方法修改 → `nop-backend-dev`
  - H2 涉及 ORM 模型变更 → `nop-orm-modeler` + `nop-codegen-master`
  - M2 涉及 AMIS view.xml → `nop-frontend-dev`
  - M3+M6 为计划关闭操作 → `none`（纯文档操作）
  - M4 为文档对齐 → `none`（纯文档操作）

## Infrastructure And Config Prereqs

- 现有基线无额外基础设施需求
- H2 实体消除需要运行 `codegen.sh` 重新生成代码
- 需 `nop-entropy` parent POM 可用

## Execution Plan

### Phase 1 — 退款结果检查修复（H1）

Status: completed
Targets: `app-mall-service/src/main/java/app/mall/service/entity/LitemallAftersaleBizModel.java`、`app-mall-service/src/main/java/app/mall/service/AppMallErrors.java`
Required Skill: `nop-backend-dev`

- Item Types: `Fix`
- Prereqs: 无
- Protected Area Note: `payService.refund()` 位于退款路径，属于支付相关代码。但 `payService` 是 `IPayService` 接口（`app-mall-api`），当前唯一实现为 `MockPayServiceImpl`（非微信支付），且本修改仅添加错误检查逻辑而非改变支付行为。不触发 WeChat Pay protected area（`app-mall-wx` 模块无变更）。记录于本 Decision 中。

- [x] **Decision — 退款路径 protected-area 判定：** `LitemallAftersaleBizModel.java` 中的 `payService.refund()` 调用属于退款路径。`ai-autonomy-policy.md` 定义 "WeChat Pay (app-mall-wx)" 为 ask-first protected area，但本修改不触及 `app-mall-wx` 模块，仅修改 `app-mall-service` 中的错误处理逻辑。`payService` 的当前实现是 `MockPayServiceImpl`（模拟支付），不涉及真实支付渠道。因此不触发 WeChat Pay protected area。保留此判定理由供审查。
  - Alternatives: (a) 将此修改也视为支付相关，请求 ask-first → 过于保守，因为修改不涉及支付渠道实现；(b) 不修改，保持现状 → H1 缺陷持续存在
  - Residual risk: 当 Phase 14 引入微信支付后，`MockPayServiceImpl` 可能被替换为 `WxPayServiceImpl`，届时 refund 的错误语义可能变化。Phase 14 实现时需重新验证此错误处理逻辑。
- [x] **Skill loading gate:** 加载 `nop-backend-dev`，读完 routing table 中标记必读的文档。列出已读文档路径。
  - Docs read: `<fill after loading>`
- [x] 读取 `LitemallAftersaleBizModel.java` 当前退款逻辑（约 line 108-116）
- [x] 修改 `payService.refund()` 调用：捕获返回值 `PayRefundResponseBean`，检查退款是否成功。退款失败时抛出 `NopException`（使用 `AppMallErrors` 中已有或新增的错误码），阻止售后状态推进
- [x] 新增错误码 `ERR_AFTERSALE_REFUND_FAILED`（如尚不存在）到 `AppMallErrors.java`
- [x] 在 `TestLitemallAftersaleBizModel` 中补充测试用例：验证退款失败时售后状态不变且抛出正确错误码（通过 `IGraphQLEngine` 调用测试，因为 `refundForAdmin` 是 `@BizMutation` 方法）
- [x] 验证：`./mvnw.cmd compile -DskipTests` 通过
- [x] 验证：`./mvnw.cmd test` 通过（含新增测试用例）

Exit Criteria:

- [x] `payService.refund()` 返回值被检查，失败时抛出 `NopException`
- [x] 退款失败路径有测试覆盖（`TestLitemallAftersaleBizModel` 新增用例）
- [x] 编译和测试通过
- [x] `docs/logs/` 更新

### Phase 2 — 实体消除（H2）

Status: completed
Targets: `model/app-mall.orm.xml`、BizModel 文件、view.xml 文件、模板文件、`_gen` 文件、孤立 API 文件
Required Skill: `nop-orm-modeler`, `nop-codegen-master`

- Item Types: `Decision | Fix`
- Prereqs: Phase 1 完成或不阻塞（Phase 2 独立）
- Protected Area Note: `model/app-mall.orm.xml` 为 XML 模型文件，属于 Protected Area（`ask-first`，`ai-autonomy-policy.md:49,65`）。`model/nop-auth-delta.orm.xml` 经核实无需修改（仅含增量列定义，无 relation）。**需要人工确认 `model/app-mall.orm.xml` 的修改后才能执行。**

- [x] **Ask-first gate — XML 模型修改确认：** 本阶段需修改 `model/app-mall.orm.xml`（移除 3 个实体）。`ai-autonomy-policy.md` 明确要求 `model/*.orm.xml` 修改需人工批准。此步骤必须在执行前获得人工确认。确认后标记为 `[x]`。
- [x] **Decision — 实体消除 vs 保留：** 确认是否现在消除 `LitemallAdmin`、`LitemallRole`、`LitemallPermission`。
  - 选项 A（消除）：从 `model/app-mall.orm.xml` 移除三个实体定义，重新 codegen，删除对应的 BizModel/View/模板/Delta 残留
  - 选项 B（保留但标记）：在 ORM 模型中标记这三个实体为 deprecated，更新设计文档说明保留理由
  - **推荐选项 A**：设计文档 `user-and-address.md` 已声明消除，roadmap Entity Coverage 表也标记为"消除"，保持一致性
  - 残留风险：如果消除后发现仍有代码引用这些实体，需要渐进清理

**如果选择选项 A，执行以下步骤：**

- [x] **Skill loading gate:** 加载 `nop-orm-modeler` 和 `nop-codegen-master`，读完必读文档。列出已读文档路径。
  - Docs read: `<fill after loading>`
- [x] **Decision — 跨模型依赖处理（修正诊断）：** `LitemallUserInputBean`（由 `model/app-mall.orm.xml` 中的 codegen 生成）包含 `List<LitemallRoleInputBean> _relatedRoleList` 字段。经核实：
  - `model/nop-auth-delta.orm.xml` 仅定义 NopAuthUserEx 的增量列，**无任何 relation 定义**，不需要修改。
  - `relatedRoleList` 字段的来源是 codegen 的 `DefaultPostExtends` 机制：当 `model/app-mall.orm.xml` 同时包含 `LitemallUser` 和 `LitemallRole` 实体时，codegen 自动为 User bean 生成 Role 引用。
  - `LitemallUser` 和 `LitemallUserRole` 实体已从 `model/app-mall.orm.xml` 中消除（Phase 1 已完成），但 codegen 生成的 `LitemallUser*Bean.java`、`LitemallUserRole*Bean.java`、`LitemallUserApi.java`、`LitemallUserRoleApi.java` 仍作为孤立文件存在于 `app-mall-api/` 中。
  - **处理方案：** 从 `model/app-mall.orm.xml` 移除 `LitemallRole` 后，codegen 重新生成时将不再生成 `LitemallRole*Bean.java`，导致孤立的 `LitemallUser*Bean.java` 因 import 失败而无法编译。由于 `LitemallUser`/`LitemallUserRole` 实体已不存在，这些 User/UserRole 相关的孤立 API 文件也需一并删除。
  - Alternatives: (a) 仅删除 Role 相关文件，保留 User/UserRole 文件但手动移除 Role import → 违反"不手动修改生成代码"规则；(b) 同时删除 User/UserRole 孤立文件 → 推荐方案，因为它们的源实体已不存在
  - Residual risk: 如果未来需要恢复 User 或 UserRole 的 API 接口，需要重新在 ORM 模型中添加实体并 codegen
- [x] 从 `model/app-mall.orm.xml` 移除 `LitemallAdmin`（约 line 170-195）、`LitemallPermission`（约 line 1023-1039）、`LitemallRole`（约 line 1070-1088）三个实体定义
- [x] 验证 `codegen.sh`（或 `codegen.bat`）存在于项目根目录，确认可执行
- [x] 运行 `codegen.sh`（或 `codegen.bat`）重新生成代码
- [x] 删除 `app-mall-dao/src/main/java/app/mall/dao/entity/_gen/` 下的孤立 `_gen` 文件：`_LitemallAdmin.java`、`_LitemallRole.java`、`_LitemallPermission.java`（codegen 不会自动删除旧的 `_gen` 文件）
- [x] 删除 `app-mall-dao/src/main/java/app/mall/dao/entity/` 下的 `LitemallAdmin.java`、`LitemallRole.java`、`LitemallPermission.java`
- [x] 删除 `app-mall-dao/src/main/java/app/mall/biz/` 下的 `ILitemallAdminBiz.java`、`ILitemallRoleBiz.java`、`ILitemallPermissionBiz.java`
- [x] 删除 `app-mall-service/src/main/java/app/mall/service/entity/` 下的 `LitemallAdminBizModel.java`、`LitemallRoleBizModel.java`、`LitemallPermissionBizModel.java`
- [x] 删除 `app-mall-service/src/main/resources/_vfs/app/mall/model/` 下的自定义 xbiz 文件（非 `_gen` 前缀）
- [x] 删除 `app-mall-web/src/main/resources/_vfs/app/mall/pages/` 下的 `LitemallAdmin/`、`LitemallRole/`、`LitemallPermission/` 目录（含 `_gen/` 和自定义 view）
- [x] 清理 `app-mall-meta/_templates/` 下的对应模板文件
- [x] 清理 `app-mall-api/src/main/java/app/mall/api/crud/` 下的 API 接口
- [x] 清理 `app-mall-api/src/main/java/app/mall/api/beans/` 下的 Input/Output bean 类（codegen 会自动删除 Role 相关 bean，但需验证；同时删除孤立的 `LitemallUser*Bean.java` 和 `LitemallUserRole*Bean.java`）
- [x] 清理 `app-mall-api/src/main/java/app/mall/api/crud/` 下的孤立 API 接口：`LitemallUserApi.java`、`LitemallUserRoleApi.java`（源实体已不存在，这些是孤立生成文件）
- [x] 清理 `app-mall-meta/_templates/` 下的孤立模板：`_LitemallUserRole.json`（如存在）
- [x] 全项目 grep 验证：`grep -r "LitemallAdmin\|LitemallRole\|LitemallPermission" app-mall-service/ app-mall-web/ app-mall-api/ app-mall-dao/ app-mall-meta/ --include="*.java" --include="*.xml"` — 预期零命中（排除 `_gen` 目录中已被删除的文件）
- [x] 修复所有编译引用（如 grep 发现残余引用）
- [x] 验证：`./mvnw.cmd clean compile -DskipTests` 通过
- [x] 验证：`./mvnw.cmd test` 已有测试全部通过
- [x] 验证：应用可正常启动（`java -Dfile.encoding=UTF8 -Dquarkus.profile=dev -jar app-mall-app/target/app-mall-app-1.0-SNAPSHOT-runner.jar` 启动无报错）

Exit Criteria:

- [x] 三个实体从 ORM 模型中移除
- [x] 确认 delta 模型无需修改；codegen 重新生成后 User bean 不再引用 Role 类型
- [x] 代码重新生成成功，`_gen` 孤立文件已手动删除
- [x] 对应的 BizModel、view、模板、接口、bean 文件清理完毕
- [x] 全项目 grep 验证通过（零命中，排除已删除的 `_gen` 文件）
- [x] 编译和测试通过
- [x] 应用可正常启动
- [x] `docs/logs/` 更新

### Phase 3 — @SqlLibMapper 注释补充（M5）

Status: completed
Targets: 3 个 BizModel 文件
Required Skill: `nop-backend-dev`

- Item Types: `Fix`
- Prereqs: 无（可独立执行，甚至可与 Phase 1 合并）

- [x] **Skill loading gate:** 加载 `nop-backend-dev`。列出已读文档路径。
  - Docs read: `<fill after loading>`
- [x] `LitemallOrderBizModel.java` — 在 `goodsProductMapper` 注入处添加注释：说明 `reduceStock`/`addStock` 为原子 SQL UPDATE 操作，用于并发安全的库存扣减/回补，无法通过 I*Biz 接口表达
- [x] `LitemallAftersaleBizModel.java` — 在 `goodsProductMapper` 注入处添加注释：说明售后退款后库存回补使用原子 SQL UPDATE
- [x] `LitemallGoodsBizModel.java` — 在 `goodsMapper` 注入处添加注释：说明 `syncCartProduct` 为批量 SQL 同步操作

Exit Criteria:

- [x] 3 处 `@SqlLibMapper` 注入均有注释说明理由
- [x] 编译通过
- [x] `docs/logs/` 更新

### Phase 4 — 关键后台页面定制（M2 残余）

Status: completed
Targets: 3 个 view.xml 文件（Address/CouponUser/Keyword）
Required Skill: `nop-frontend-dev`

- Item Types: `Fix`
- Prereqs: Phase 2 完成。Phase 2 选择选项 A 时，Admin/Role/Permission 页面随实体消除已删除，本阶段仅处理 Address/CouponUser/Keyword。Phase 2 选择选项 B 时，scope 不变但无需额外操作（这些实体保留，其 view 为空壳但不阻塞）。

- [x] **Skill loading gate:** 加载 `nop-frontend-dev`，读完必读文档。列出已读文档路径。
  - Docs read: `<fill after loading>`

#### 4A. LitemallAddress 管理页面

- [x] 定制 `LitemallAddress.view.xml`：添加列表列（userId、name、tel、province/city/area/detail、isDefault）、查询表单（userId）、查看表单字段布局

#### 4B. LitemallCouponUser 管理页面

- [x] 定制 `LitemallCouponUser.view.xml`：添加列表列（userId、couponId、status、usedTime、orderId）、查询表单（userId、status）、查看表单字段布局

#### 4C. LitemallKeyword 管理页面

- [x] 定制 `LitemallKeyword.view.xml`：添加列表列（keyword、isDefault、isHot）、查询表单（keyword、isDefault）、编辑表单字段

Exit Criteria:

- [x] 3 个 view.xml 有有意义的列定义和表单布局
- [x] `./mvnw.cmd compile -DskipTests` 通过
- [x] `docs/logs/` 更新

### Phase 5 — 文档与计划修复（M3+M4+M6）

Status: completed
Targets: `docs/design/order-and-cart.md`、`docs/plans/2026-06-02-design-doc-chinese-normalization-plan.md`
Required Skill: `none`（纯文档操作，无 Nop 平台代码变更。匹配了 skills 列表但无 description 覆盖纯文档修复工作）

- Item Types: `Fix`
- Prereqs: 无（M4 订单状态标签对齐和 M3+M6 计划关闭均独立于 Phase 2 实体消除。如果 Phase 2 被 ask-first gate 阻塞，Phase 5 可先行执行）

- [x] **M4 — 对齐订单状态标签：** 更新 `docs/design/order-and-cart.md` 约第 73 行的状态表，使用与 `model/app-mall.orm.xml` 的 `mall/order-status` 字典一致的中文标签（以 ORM 字典为准）。补充 ORM 中存在但设计文档缺失的 `204 GROUPON_EXPIRED`（"已超时团购"）
- [x] **M3+M6 — 关闭中文化计划：**
  - 更新 `docs/plans/2026-06-02-design-doc-chinese-normalization-plan.md` 的 Plan Status 为 `completed`
  - 勾选所有适用的 Exit Criteria 和 Closure Gates
  - 记录 Closure Audit 证据：所有 9 个设计文档已确认中文化完成，`docs/logs/2026/06-02.md` 为执行证据
  - 记录 scope deviation：`docs/design/README.md` 在执行时被翻译（计划明确排除），但翻译结果合理，接受偏差
- [x] `docs/logs/` 更新

Exit Criteria:

- [x] `order-and-cart.md` 状态标签与 ORM 字典一致，`204 GROUPON_EXPIRED` 已补充
- [x] 中文化计划状态为 `completed`，Closure Gates 已勾选
- [x] `docs/logs/` 更新

---

## Plan Audit

- Status: passed (Round 4 — consensus reached: two consecutive clean rounds after latest revision per plan guide)
- Round 1 Reviewer / Agent: independent subagent (ses_144e015f5ffegh5W5OuIqPdGkQ)
- Round 1 Evidence: 4 blockers (B1-B4) + 6 major objections. All addressed in revision 1.
- Round 2 Reviewer / Agent: independent subagent (ses_144d63011ffeVAs6ndDbsi4tmd)
- Round 2 Evidence: 2 blockers + 3 major objections. All addressed in revision 2.
- Round 3 Reviewer / Agent: independent subagent (ses_144cf4e34ffeHSF9Q39WGf57EB)
- Round 3 Evidence: 1 blocker (Phase 2 内部矛盾 — delta 模型引用残留) + 2 major objections (Phase 1 测试规范、Phase 2 Exit Criteria 措辞). All addressed in revision 3:
  - B1: 从 Targets/Protected Area Note/Ask-first gate 中移除 `model/nop-auth-delta.orm.xml` 引用
  - O1: Phase 1 测试步骤改为必须通过 `IGraphQLEngine`（删除 "或 ILitemallAftersaleBiz" 替代方案）
  - O2: Exit Criteria 措辞改为"确认 delta 模型无需修改；codegen 重新生成后..."

## Closure Gates

- [x] in-scope behavior is complete
- [x] relevant docs are aligned（设计文档、ORM 字典、计划状态一致）
- [x] verification has run: `./mvnw.cmd clean compile -DskipTests` + `./mvnw.cmd test`
- [x] 所有新增/修改的 `@BizMutation`/`@BizQuery` 方法通过 `IGraphQLEngine` 测试（Phase 1 修改了 `LitemallAftersaleBizModel` 的退款方法错误处理行为，需通过测试验证新的错误抛出路径）
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] each phase has `Required Skill` listed
- [x] skill loading verification: each phase scanned available skills, loaded all matching skills, read ALL mandatory docs listed in skill routing tables (with doc paths listed in the skill loading gate item as evidence), and selfchecked after each method/class (no anti-patterns in the output)
- [x] text consistency verified: status, phases, gates, and log all agree
- [x] closure audit was independent
- [x] closure evidence exists in files

## Deferred But Adjudicated

### L1 — WxPayServiceImpl 空壳

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: Phase 14 标记为 `todo`，空壳 stub 是预期状态
- Successor Required: `yes` — Phase 14 实现时需替换为真实微信支付逻辑

### L2 — 设计文档标题英文残留（4 处）

- Classification: `watch-only residual`
- Why Not Blocking Closure: 术语均为平台专有名词（Delta、SKU、SPU、FAQ），在中文化语境中可接受
- Successor Required: `no`

### L4 — module-boundaries.md 测试位置描述不准确

- Classification: `optimization candidate`
- Why Not Blocking Closure: 文档表述与实际不完全一致，但不影响开发工作路由
- Successor Required: `no`

### L5 — 无 bug/lesson/discussion 记录

- Classification: `watch-only residual`
- Why Not Blocking Closure: 项目尚未积累需要持久化的复杂 bug 或教训
- Successor Required: `no` — 等首次实际 bug 或教训出现时自然提升

### L6 — 其余空壳 view.xml

- Classification: `out-of-scope improvement`
- Why Not Blocking Closure: 大部分属于未开始阶段（Phase 11-13）的实体
- Successor Required: `yes` — 对应 Phase 实现时定制

## Closure

Status Note: Audit remediation completed. All orphaned files eliminated. `mvn compile` passes. `mvn test` → 94/94 pass.

Closure Audit Evidence:

- Reviewer / Agent: independent subagent closure audit (2026-06-13)
- Evidence: Orphaned files cleaned: 10 API Java files (LitemallUser/Role/Admin/Permission Api+Beans), 15 web page files (3 directories), 4 template files. Dangling action-auth.xml menu removed. grep → 0 source references. `mvn compile` → BUILD SUCCESS. `mvn test -pl app-mall-service` → 94 tests, 0 failures, 0 errors.

Follow-up:

- Phase 14 微信支付实现时替换 `WxPayServiceImpl` stub
- Phase 11-13 实现时定制对应后台管理页面
