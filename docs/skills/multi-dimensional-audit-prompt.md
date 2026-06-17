# 多维深度审计提示词手册（nop-app-mall 应用层）

> **定位**：本文件是面向 AI 的多维度深度审计提示词集合，专门针对 `nop-app-mall` 应用层产品（基于 Nop 平台的电商 mall）。每个维度提供一个可复用的"维度正文"，由主 agent 与"共享提示词前缀"拼接后派发给专用子 agent 执行。
>
> **与 `../nop-entropy/ai-dev/skills/deep-audit-prompts.md` 的关系**：方法论（共享前缀、迭代深挖、独立复核、内容质量门禁、归档结构）移植自该手册，但维度、必读文档、误报校准、执行步骤全部针对本应用层项目重写，不再审计 nop-entropy 框架代码本身。
>
> **前提**：执行审计前必须先阅读 `AGENTS.md`、`docs/index.md`、`docs/context/` 下全部文件，以及与本维度相关的 owner 文档，以当前 live code 与 live doc 为准。
>
> **输出格式**：每个发现按统一格式输出（见附录 A），最终汇总报告见附录 B。

---

## 审核总览

本手册覆盖 **13 个审计维度**，分为 5 大类：

| 类别 | 编号 | 维度名称 | 审核目标 |
|------|------|---------|---------|
| **A. 需求与设计对齐** | 01 | 需求正确性与 owner-doc 对齐 | input→requirements→design 链路是否完整、无遗漏、无矛盾 |
| | 02 | 设计与架构一致性 | docs/design 业务语义与 docs/architecture 技术结构、model 真值是否一致 |
| **B. Nop 平台规范与模型** | 03 | 模块边界与依赖 | 9 个模块（codegen/api/dao/service/web/app/wx/delta/meta）是否遵守 `module-boundaries.md` |
| | 04 | ORM 模型与契约 | `model/*.orm.xml`、`model/*.api.xml` 作为源真值的质量（实体/关系/域/字典/索引/displayName） |
| | 05 | BizModel 与服务层规范 | CrudBizModel 继承、跨实体访问、@SqlLibMapper 合理性、错误处理两档策略 |
| | 06 | Delta 定制合规性 | `app-mall-delta` 对 nop-auth 的 Delta 覆盖是否合规、升级兼容 |
| | 07 | 代码生成管线完整性 | model→codegen→dao→meta→service→web 链路闭合、生成产物未被手改 |
| **C. 前端与用户面** | 08 | AMIS 页面层 | `app-mall-web` 的 `.view.xml` 三层模型（grid/form/page）、bounded-merge、业务动作按钮 |
| **D. 电商域逻辑** | 09 | 电商域逻辑正确性 | 订单/库存/支付退款/价格/优惠券/售后/购物车的状态机与一致性 |
| **E. 验证、风险与流程** | 10 | 验证充分性 | 构建/测试/手动证据、known-good baseline 是否真实更新 |
| | 11 | 回归与跨特性风险 | 阶段依赖、跨特性影响、并发/事务/数据完整性 |
| | 12 | 路由、技能与流程合规 | AGENTS.md 路由、强制技能加载、自主性政策、计划/闭合审计 |
| | 13 | 文档-代码一致性与维护 | 日志/owner 文档是否更新、design 文案与 ORM 字典是否对齐、文档漂移 |

---

## 调度架构

### 总体流程

```
主 agent（读取本文件，选择审计对象 + 要执行的维度）
  │
  ├── 准备阶段：读取 owner 文档，生成命令基线
  │
  ├── 阶段一 — 迭代深挖（每个维度独立）
  │   ├── Task → 维度 01 第 1 轮（初审）
  │   ├── Task → 维度 01 第 2 轮（追加深挖）
  │   ├── ...（最多 10 轮，直到无新发现）
  │   ├── Task → 维度 02 第 1 轮（初审）
  │   ├── ...
  │   └── [各维度独立执行]
  │
  ├── 阶段二 — 独立复核（每个维度独立）
  │   ├── Task → 维度 01 维度复核（读完整文件，逐条保留/降级/驳回）
  │   ├── Task → 维度 01 高风险子项复核（逐条）
  │   ├── Task → 维度 01 低风险批量复核
  │   ├── Task → 维度 02 维度复核
  │   ├── ...
  │   └── 汇总已复核通过的结果 → summary.md
  │
  └── 阶段三 — 跨维度汇总（所有维度完成后）
      └── 主 agent 生成深度审计汇总报告（附录 B）
```

### 调度方法

1. 主 agent 读取本文件，选择**审计对象**（整个项目 / 某模块 / 某阶段 / 某 plan / 某变更集）和要执行的**维度**。
2. **准备阶段**：
   a. 主 agent 读取审计对象相关的 owner 文档（`docs/design/`、`docs/architecture/`、`model/*.orm.xml`、`model/*.api.xml`、相关 requirement / plan）。
   b. 按"自动化工具基线"节运行命令生成基线，并把输出随 prompt 一起提供给子 agent。
   c. **禁止阅读 `docs/audits/`、`docs/plans/`（除审计对象本身外）、`docs/bugs/`、`docs/lessons/` 等历史记录**。只审计当前 live code / live doc；未修复的问题审计时自然会再次发现。
3. **阶段一 — 迭代深挖**：
   a. 主 agent 用"共享提示词前缀 + 该维度正文"拼接出完整 prompt，派发**第 1 轮（初审）子 agent**。
   b. 初审完成后，发现保存到 `docs/audits/{YYYY}-{MM}-{DD}-{HHMM}-multi-dim-audit-{对象}-{简短标识}/{维度编号}-{名}.md`。
   c. 如果第 1 轮有发现，主 agent 派发**第 2 轮（追加深挖）子 agent**，prompt 中包含：共享前缀 + 维度正文（相同）+ 已保存发现全文 + 深挖追加指令（见下）。
   d. 第 2 轮完成后，新发现追加到同一个维度文件（标注 `## 深挖第 2 轮追加`）。
   e. 重复 c-d，直到某轮无新发现或达到 10 轮上限。
   f. 如果第 1 轮就无发现，直接进入阶段二维度复核（复核 agent 确认零发现并列出检查范围）。
4. **阶段二 — 复核**：
   a. 深挖结束后，主 agent 派发一个**独立的维度复核子 agent**，输入为该维度完整文件，要求重新读 live code / live doc，输出"保留 / 降级 / 驳回"逐条判断。
   b. 维度复核完成后，主 agent 必须对高风险或不确定发现项，再派发**独立的子项复核子 agent**逐项验证；低风险项可按文件或按模式批量复核。
5. 只有在"深挖完成 + 维度复核 + 必要的子项复核"都完成后，该维度结果才允许进入最终汇总。
6. 所有维度完成后，主 agent 汇总**已复核通过**的结果，生成「深度审计汇总报告」（附录 B）。

### 深挖追加提示词模板

从第 2 轮起，主 agent 在维度正文后追加以下内容：

```text
---

## 深挖追加指令（第 N 轮）

以下是本维度前 N-1 轮已保存的全部发现：

[粘贴前 N-1 轮的完整发现文本]

你的任务：

1. 读取上述已有发现，理解本维度已经覆盖了哪些文件、模式和路径。
2. 基于已有发现暴露的文件、模式和关联路径，**继续深挖**尚未覆盖的盲区：
   - 已有发现涉及的文件是否有同类型问题未被检出？
   - 已有发现的模式是否在其他文件/模块中也存在？
   - 是否有与已有发现相关、但尚未检查的代码路径或文档？
   - 该维度执行步骤中是否有尚未充分覆盖的步骤？
3. **只输出新发现的条目**。不要重复已有发现中已经报告的内容。
4. 如果经过仔细检查后确实没有新的问题，输出："未发现新的问题。深挖结束。"
5. 如果仍能找到零散细节，但只是低价值、机械重复、缺少明确风险或可行动建议，不要凑数；输出："未发现新的高价值问题。深挖结束。"
6. 每个新发现仍需遵守统一格式（文件路径 + 行号 + 3-10 行证据 + 严重程度 + 现状 + 风险 + 建议 + 误报排除）。

【发现完整性要求 — 不可违反】：
- 每个新发现必须按完整格式输出（### 标题 + 文件 + 行号 + 3-10 行证据片段 + 严重程度 + 现状 + 风险 + 建议 + 误报排除）。
- 禁止压缩为关键词、一句话摘要或表格行。
- 禁止只输出标题列表然后说"详情见上方"。
- 发现较多时宁可分多个子 agent 输出，也不要压缩每个发现的内容。
```

### 保存与追加规则

1. **第 1 轮结果**：将子 agent 输出的完整发现正文写入文件。如果输出被压缩（发现条目少于 9 行），主 agent 必须要求重新输出。
2. **追加深挖结果**：在文件末尾追加 `## 深挖第 N 轮追加` 标题，然后写入新发现的完整正文。**不修改、不压缩、不覆盖**前 N-1 轮已保存的内容。
3. **复核结论追加**：在深挖全部结束后，追加 `## 维度复核结论` 和 `## 子项复核结论`。复核结论引用已有发现编号（如 `[维度04-01]`），不重写发现内容。

### 并行策略

- 同批次的**第 1 轮（初审）**可以并行派发。
- 同一维度的**深挖追加轮次**必须串行（每轮依赖前一轮结果）。
- 同一维度的**维度复核**必须在该维度深挖阶段全部结束后进行。
- 同一维度的**子项复核**必须在该维度复核完成后进行。
- **不同维度之间**的深挖、复核可以并行（互不依赖）。

> 同批次内各维度的第 1 轮初审可并行，但每个维度自身的深挖追加轮次是串行的。实际调度可按批次并行派发各维度第 1 轮，然后对有发现的维度串行深挖，最后再并行派发各维度复核。

### 结果输出与归档

所有已通过独立复核的结果保存到 `docs/audits/` 下的专用子目录（符合 `AGENTS.md` 与 `docs/audits/00-audit-execution-guide.md` 对 multi-dimensional audit 的归档约定）。

**目录结构**：

```
docs/audits/{year}-{month}-{day}-{HHMM}-multi-dim-audit-{对象}-{简短标识}/
├── 01-requirement-design-alignment.md
├── 02-design-architecture-consistency.md
├── 03-module-boundary.md
├── 04-orm-model-contract.md
├── 05-bizmodel-service.md
├── 06-delta-customization.md
├── 07-codegen-pipeline.md
├── 08-amis-frontend.md
├── 09-ecommerce-domain.md
├── 10-verification.md
├── 11-regression-risk.md
├── 12-routing-process.md
├── 13-doc-code-consistency.md
└── summary.md
```

**命名规则**：
- 子目录：`{YYYY}-{MM}-{DD}-{HHMM}-multi-dim-audit-{对象}-{简短标识}`（如 `2026-06-16-1430-multi-dim-audit-full-project` 或 `2026-06-16-0900-multi-dim-audit-order-flow`）。`{HHMM}` 为 24 小时制时分，用于区分同一天多次执行。
- 每个维度一个 md 文件：`{维度编号}-{英文简短名}.md`。
- `summary.md` 保存主 agent 汇总报告（附录 B）。

**维度文件内部结构**：
- 第 1 轮（初审）发现（每个发现是完整的 `###` 级条目，不是关键词列表）
- `## 深挖第 2 轮追加`（如有，每个追加发现同样是完整条目）
- `## 深挖第 N 轮追加`（如有）
- `## 维度复核结论`（引用发现编号，不重写内容）
- `## 子项复核结论`（如有逐条复核）
- `## 最终保留项`（表格：编号、严重程度、文件路径、一句话摘要）

**内容质量门禁**：主 agent 保存维度文件前必须检查：
- 每个发现条目包含完整格式（文件、行号、证据片段、严重程度、现状、风险、建议、误报排除、信心水平）
- 深挖轮次是完整发现而非关键词列表
- 复核结论引用已有发现编号而非独立重写
- 不通过则要求子 agent 重新输出后再保存

### 最低内容量参考

经验参考（非硬性上下限）：一个有 5 个发现的维度文件，按完整格式输出通常在 130-220 行。如果 5 个发现只写出 45 行，几乎可以确定发生了压缩。

### 批次策略

| 批次 | 维度 | 可否并行 |
|------|------|---------|
| 第一批 | 01, 04, 09 | 互相独立，可并行（需求/模型/域逻辑基线） |
| 第二批 | 03, 05, 06, 07 | 互相独立，可并行（平台规范与生成管线） |
| 第三批 | 02, 08, 13 | 互相独立，可并行（一致性维度） |
| 第四批 | 10, 11 | 互相独立，可并行（验证与风险） |
| 第五批 | 12 | 可与第四批并行（流程合规） |

### 自动化工具基线

主 agent 在派发维度前，应优先运行以下命令并将输出提供给子 agent（实际命令以 `docs/context/project-context.md` 为准，如该文件命令为占位符须先修正）：

1. `./mvnw compile -DskipTests` — 编译基线（维度 05/07/10）
2. `./mvnw test` — 测试报告基线（维度 10）
3. 统计各模块 Java 文件行数：`find app-mall-*/src/main/java -name "*.java"` 配合行数统计（维度 03/05）
4. 生成产物清单：`glob model/*.orm.xml model/*.api.xml` 与 `_gen` / `_` 前缀文件（维度 04/07）
5. 前端页面清单：`glob app-mall-web/**/_vfs/**/*.view.xml`（维度 08）
6. 错误码清单：`grep -r "nop.err.mall" model/ app-mall-service/`（维度 05）

如子 agent 需要但主 agent 未提供基线，子 agent 应标注"缺少工具基线"，不能把手工估算当确定事实。

### 子 Agent 提示词装配结构

下文每个维度给出的内容是**维度正文**，不是可单独复制执行的完整 prompt。主 agent 派发前必须先拼接共享前缀：

```
1. 共享提示词前缀（固定，所有维度相同）
2. 维度正文（本维度目标 + 必读文档 + 执行步骤 + 额外输出要求）
```

---

## 共享提示词前缀

主 agent 派发任一维度前，应先附加以下共享前缀：

```text
你正在审计 nop-app-mall 项目。这是一个基于 Nop 平台（nop-entropy 2.0.0-SNAPSHOT）的商业级电商 mall 应用，Java 17 + Quarkus + AMIS，Maven 多模块工程。

项目核心特征：
- 模型优先开发（model → codegen → dao → meta → service → web → app）
- 9 个模块：app-mall-codegen / app-mall-api / app-mall-dao / app-mall-service / app-mall-web / app-mall-app / app-mall-wx / app-mall-delta / app-mall-meta
- 源真值在 model/*.orm.xml 与 model/*.api.xml；其他为生成产物或手写代码
- app-mall-delta 通过 Nop Delta 机制覆盖 nop-auth 行为（用户/角色/权限）
- 前端是 AMIS JSON（.view.xml），位于 app-mall-web/_vfs/
- 产品基线源自 litemall 需求，分 14 个阶段交付（见 docs/backlog/implementation-roadmap.md）

执行前先阅读：
1. AGENTS.md（agent 工作流、Nop 平台规则、验证基线、规划规则）
2. docs/index.md（文档路由）
3. docs/context/project-context.md（验证命令、AI 阻断条件）
4. docs/context/codebase-map.md（代码入口与变更路由）
5. docs/context/source-of-truth-and-precedence.md（源真值优先级，事实冲突时阅读）
6. docs/architecture/module-boundaries.md（9 模块边界）
7. docs/backlog/implementation-roadmap.md（阶段状态，确认"已完成"vs"未开始"）
8. ../nop-entropy/docs-for-ai/INDEX.md（Nop 平台规范路由，仅当审计涉及平台规范时）
9. 本维度列出的 owner 文档

注意：当前审计基线按 **live code + live doc 为准** 执行。不以历史日志、已关闭计划或口头约定为准。

通用审计口径：
1. 以当前代码与文档为准，不以历史日志、已关闭计划或口头约定为准。
2. 不重复报告已收敛的问题。
3. 不把"看起来不优雅"当问题，必须有结构性原因或可量化风险。
4. 对 Nop 平台的模型驱动开发保持克制。代码生成产物（`_` 前缀文件、`_gen/` 目录、`_app.orm.xml`、`_service.beans.xml`）不应作为审计发现的对象，除非追溯到模型或模板层面的错误。审计重点是手写代码、模型定义与 owner 文档。
5. 对框架"约定大于配置"模式保持克制。下列是平台标准模式，不是问题（详见下方"常见误报校准"）。
6. 每个发现必须可定位：文件路径 + 行号范围 + 3-10 行证据片段。
7. 区分生成代码与手写代码。生成代码的问题应追溯至模型或模板，而不是标记生成产物本身。
8. 初审输出只是线索，不是最终事实；最终结论必须经过独立复核。
9. 如果某条发现看起来像历史上的高频误报模式，必须明确写出"为什么这次不是同类误报"。
10. 区分"已完成阶段"与"未开始阶段"。未开始阶段（todo）的空壳实现（如 stub BizModel、空 view.xml）是预期状态，不报告；已完成阶段（done）的空壳才是问题。

严重程度判级（针对应用层）：
- P0: 当前已构成错误行为、资金/数据完整性风险、安全违约、或违反 AI 阻断条件（见 project-context.md 的支付/数据删除/XML 模型/数据库 schema 保护区域）。
- P1: 高概率回归、核心契约漂移、跨模块边界错误、设计文档与 ORM 真值矛盾、已完成阶段的功能缺口、会误导后续开发的文档问题。
- P2: 真实维护成本或局部缺陷，但可排期处理。
- P3: 低优先级但真实存在的问题。

常见误报校准（以下【不是】问题，不应作为审计发现）：
- BizModel 方法返回 ORM 实体对象（平台标准模式，xmeta + GraphQL selection 控制字段可见性）
- @Inject 注入 protected 字段而非 private（NopIoC 限制，非设计缺陷）
- `_` 前缀生成文件内容（除非追溯到模型/模板层面的错误）
- I*Biz 接口放在 dao 模块（代码生成的 DAO 层 CRUD 契约，绑定 DAO 实体）
- jakarta.inject / jakarta.annotation 出现在任一层（Java 标准规范，不算框架依赖）
- CrudBizModel 的标准继承模式（setEntityName + extends CrudBizModel<T>）
- xbiz 文件中的方法签名与 BizModel Java 方法不完全一致（GraphQL 引擎通过反射调用，xbiz 是声明性元数据）
- 多模块 pom.xml 中必要的传递依赖声明（Maven 最佳实践要求显式声明直接使用的依赖）
- 未显式声明平台核心包（nop-api-core、nop-commons、nop-core、nop-xlang、nop-markdown）的依赖——通过传递链稳定获得
- app-mall-delta 覆盖 nop-auth（这是设计意图，不是边界违规；见 docs/design/roles-and-permissions.md）
- 未开始阶段（roadmap 中标记为 todo）的空壳实现：WxPayServiceImpl stub、空 view.xml、stub BizModel（仅在对应阶段进入 done 后才审计）
- Litemall* 实体名前缀（litemall 遗产，实体消除是独立 roadmap 项，命名本身不是问题）
- deploy/sql/ 三方言 DDL 与 ORM 模型的非索引维度漂移（module-boundaries.md 已记录为已知非阻塞，仅 ORM 模型是源真值）
- 内部服务接口返回 core 层模型（内部高性能合理选择）

自动化工具有效性规则：
1. 已被 ./mvnw compile / 编译器类型检查覆盖且当前通过的问题不得重复报告。
2. 主 agent 提供的命令基线输出应作为起点，不是让子 agent 重新手工扫描。
3. checkstyle 报告的机械问题只在主 agent 未提供基线时才手工报告（本项目 Lint/static check 当前为 none）。
4. 测试失败已记录在案的不重复报告，但"缺失测试"（无测试文件）是有效发现。

【发现条目完整性 — 强制规则，不可违反】：
1. 每个发现必须按统一格式（附录 A）完整输出，包含文件路径、行号范围、3-10 行证据代码片段、严重程度、现状、风险、建议、信心水平、误报排除。
2. 禁止将发现压缩为一行标题或表格单元格。
3. 禁止用摘要替代完整条目。
4. 证据代码片段不可省略。如因上下文限制无法贴出代码，必须标注"[证据片段待补充]"并列出需查看的具体行号范围。
5. 每个发现条目最少 9 行（文件、行号、证据、严重程度、现状、风险、建议、信心水平、误报排除），少于 9 行视为不合格。
6. 复核结论表是补充索引，不能替代发现正文。

如本维度需要命令输出（编译、测试、文件清单），优先由主 agent 先生成基线，再把结果连同本 prompt 一起提供给你；不要假设你一定能直接运行命令。
```

维护规则：
- 共享方法论、严重程度判级、命令基线策略只维护在"共享提示词前缀"中。
- Nop 平台与应用层特定的误报校准直接写在共享前缀的"常见误报校准"节中。
- 各维度正文只保留该维度特有的目标、owner 文档、执行步骤、特例说明和额外输出要求。
- 如某维度需额外约束，只写新增部分，不要重复抄写共享前缀内容。

---

## 发现条目完整性守则

> **本节是所有子 agent 的硬性约束**。违反本节规则的维度文件视为不合格，主 agent 必须要求子 agent 重新输出。

### 反模式：发现被过度压缩

**错误**（深挖轮次只记关键词）：

```markdown
## 深挖轮次
- 第 1 轮: ORM 关系缺失、displayName 未本地化。
- 第 2 轮: 错误码缺 .param()。
```

**正确**：每轮每个发现必须是完整条目（见附录 A 的完整格式）。

### 维度文件质量检查清单

主 agent 接收子 agent 输出后、保存文件前必须检查：

1. **每个发现条目 ≥ 9 行**：文件路径、行号范围、证据片段、严重程度、现状、风险、建议、信心水平、误报排除缺一不可。
2. **证据片段存在**：每个发现 3-10 行代码片段，或明确标注 `[证据片段待补充]`。
3. **深挖轮次不是关键词列表**：每一轮每个发现都是完整 `###` 级条目。
4. **复核结论引用发现编号**：不独立重写，引用上方已有发现编号。
5. **最终保留项有文件路径和严重程度**：不是纯文字列表。
6. **零发现维度也需说明检查范围**：列出读过的关键文件和检查过什么，不能只写"零发现"。

---

## A. 需求与设计对齐

### 维度 01：需求正确性与 owner-doc 对齐

**子 Agent 提示词**：

```text
以下为"维度 01"的维度正文。派发时必须与上文"共享提示词前缀"拼接。

审核维度 01：需求正确性与 owner-doc 对齐

目标：检查 docs/input/ → docs/requirements/ → docs/design/ 链路是否完整、无遗漏、无矛盾，落地实现是否被授权的需求/roadmap 项覆盖。

必读文档：
- docs/input/litemall-requirements.md（原始 PM 输入，注意文件很大）
- docs/requirements/commercial-baseline.md（综合需求）
- docs/requirements/README.md
- docs/design/app-overview.md、docs/design/feature-inventory.md
- docs/backlog/implementation-roadmap.md（阶段状态）
- docs/context/source-of-truth-and-precedence.md

执行步骤：
1. 抽样核对 input 原始需求中的关键业务规则是否被 requirements/commercial-baseline.md 覆盖（是否有静默丢弃的需求）。
2. 检查每个已完成阶段的需求是否都有对应实现（需求→代码可追溯）。
3. 检查是否存在"代码已实现但无需求/roadmap 授权"的功能（scope creep）。
4. 检查是否存在"需求已写但 owner 文档未承接"的断层。
5. 检查 unresolved question 是否被伪装成已定论（docs/discussions/ 中是否有未关闭问题被当成需求实现）。
6. 检查 source-of-truth 优先级是否被违反（如设计文档与 ORM 矛盾时未以 ORM 为准）。
7. 检查已完成阶段的验收标准是否真的满足，而非标记 done 但功能缺口。

注：未开始阶段（todo）的需求未实现是预期状态，不报告。

输出格式：同标准发现格式。
```

---

### 维度 02：设计与架构一致性

**子 Agent 提示词**：

```text
以下为"维度 02"的维度正文。派发时必须与上文"共享提示词前缀"拼接。

审核维度 02：设计与架构一致性

目标：检查 docs/design/（业务语义）与 docs/architecture/（技术结构）是否相互一致，并与 model/*.orm.xml / model/*.api.xml 真值对齐。

必读文档：
- docs/design/ 全部文件（app-overview、feature-inventory、domain-design-guidelines、roles-and-permissions、order-and-cart、product-catalog、user-and-address、marketing-and-promotions 等）
- docs/architecture/system-baseline.md、module-boundaries.md、project-vision.md
- model/app-mall.orm.xml、model/app-mall.api.xml
- docs/design/domain-design-guidelines.md（设计规范基线）

执行步骤：
1. 检查设计文档描述的实体/字段/状态是否与 ORM 模型一致（如订单状态码、字典定义）。
2. 检查设计文档描述的状态机/工作流是否与代码实现的状态转移一致。
3. 检查 docs/design 的角色/权限描述与 docs/architecture、app-mall-delta 实现是否一致。
4. 检查 docs/architecture 的模块边界描述与实际 pom.xml 依赖是否一致。
5. 检查设计文档之间是否相互矛盾（跨文档引用同一概念但定义不同）。
6. 检查设计文档声明"已消除/已迁移"的实体是否真的从 model 中移除。
7. 检查设计文档引用的文件路径/行号是否仍有效。
8. 检查设计文案与 ORM 字典（displayName / dict label）是否文字一致。

注：以 ORM 模型为字段/字典真值；设计文档声明"字典由 model 维护"时，文字差异以 ORM 为准报告。

输出格式：同标准发现格式。
```

---

## B. Nop 平台规范与模型

### 维度 03：模块边界与依赖

**子 Agent 提示词**：

```text
以下为"维度 03"的维度正文。派发时必须与上文"共享提示词前缀"拼接。

审核维度 03：模块边界与依赖

目标：检查 9 个模块是否遵守 docs/architecture/module-boundaries.md 的依赖约束。

必读文档：
- docs/architecture/module-boundaries.md
- docs/architecture/system-baseline.md
- 各模块 pom.xml

标准分层依赖规则（来自 module-boundaries.md）：
1. app-mall-api：只依赖 nop-entropy API 库；禁止依赖 service/dao/web
2. app-mall-dao：只依赖 nop-entropy ORM 库 + app-mall-api；禁止依赖 service/web/app
3. app-mall-service：依赖 api + dao + nop-entropy service 库；禁止依赖 web/app
4. app-mall-web：依赖 api + service + nop-entropy web 库；禁止直接依赖 dao（通过 service 访问数据）
5. app-mall-wx：依赖 api + WeChat SDK；禁止依赖 web/dao
6. app-mall-delta：依赖 nop-auth 模块；禁止依赖 web/service 业务逻辑
7. app-mall-meta：依赖 nop-entropy meta 库；禁止依赖 web/service
8. app-mall-codegen：依赖 nop-entropy codegen 库；禁止依赖运行时模块
9. app-mall-app：聚合所有模块；无禁止依赖
10. 不允许循环依赖

执行步骤：
1. 读取各模块 pom.xml，提取所有 io.nop.* 与 app-mall-* 依赖，构建依赖图。
2. 对照上述规则逐条检查违规或可疑依赖。
3. 对每个违规指出：哪个 pom.xml、违规依赖声明、违反哪条规则、是否有合理例外。
4. 检查是否有跨层数据访问（如 web 直接 new DAO、service 反向调用 web）。
5. 检查是否有本应在 api 层的共享 DTO/接口被放进 service 或 dao。
6. 检查 module-boundaries.md 是否与实际结构漂移（如文档说测试在某处但实际在别处）。
7. 检查跨模块工具类（如日志工具）的归属是否合理且被文档记录。

输出：违规清单（按严重程度排序）+ 完整依赖图 + 合规模块清单 + 总结。
```

---

### 维度 04：ORM 模型与契约

**子 Agent 提示词**：

```text
以下为"维度 04"的维度正文。派发时必须与上文"共享提示词前缀"拼接。

审核维度 04：ORM 模型与契约

目标：检查 model/*.orm.xml 与 model/*.api.xml 作为源真值的质量：实体建模、关系定义、域使用、字典、索引、displayName 本地化、主键设计。

必读文档：
- model/app-mall.orm.xml（核心 ORM 源真值）
- model/app-mall.api.xml（API 契约源真值）
- model/nop-auth-delta.orm.xml（delta 源真值）
- docs/design/domain-design-guidelines.md
- ../nop-entropy/docs-for-ai/02-core-guides/model-first-development.md
- .opencode/skills/nop-database-design/SKILL.md（数据库设计规范，如存在）

执行步骤：
1. 读取 model/*.orm.xml（源模型，非生成的 _app.orm.xml）。
2. 检查每个实体：
   - 主键设计（sid? varchar(32)? 自增?）是否规范一致
   - 字段类型是否使用合适 domain
   - 关系定义（to-one/to-many）是否与业务关系匹配
   - displayName 是否已本地化（有对应 i18n key）
   - 审计字段（createTime/updateTime/createdBy/updatedBy）配置
   - 字段命名 snake_case 数据库命名
3. 检查字典（dict）定义是否与实际枚举/设计文档一致（如 order-status 状态码）。
4. 检查 tagSet/status 字段是否有对应字典定义。
5. 检查索引是否覆盖常见查询场景；与 deploy/sql/ 三方言 DDL 的 index 是否同步（module-boundaries.md 记录的漂移监控点）。
6. 检查关系级联行为（cascade delete 是否有误删风险，如订单删除连带商品）。
7. 检查 model/*.api.xml 定义的接口是否与 service 实现的方法一致。
8. 检查是否有定义了完整表结构但从未被使用的实体（死实体）。
9. 检查已完成阶段声称消除的实体是否仍残留在模型中。

输出格式：同标准发现格式。
```

---

### 维度 05：BizModel 与服务层规范

**子 Agent 提示词**：

```text
以下为"维度 05"的维度正文。派发时必须与上文"共享提示词前缀"拼接。

审核维度 05：BizModel 与服务层规范

目标：检查 app-mall-service 的 BizModel 是否遵循 Nop 平台规范：CrudBizModel 继承、跨实体访问、@SqlLibMapper 合理性、错误处理两档策略。

必读文档：
- ../nop-entropy/docs-for-ai/02-core-guides/service-layer.md
- ../nop-entropy/docs-for-ai/02-core-guides/error-handling.md
- ../nop-entropy/docs-for-ai/00-start-here/ai-defaults.md（跨实体访问规则、反模式表）
- docs/design/app-overview.md
- app-mall-service/src/main/java/ 下 BizModel 类
- model/*.api.xml（I*Biz 契约）

平台规范基线：
- 标准实体服务 extends CrudBizModel<T> + 构造函数 setEntityName()
- 跨实体访问：注入 I*Biz 接口；仅在 I*Biz 无法满足时用 IDaoProvider/IOrmTemplate/@SqlLibMapper，并注释说明原因
- 公共 API（GraphQL/跨模块）：必须用 ErrorCode + NopException + .param()，错误码格式 nop.err.mall.{domain}.{specific}
- 内部实现：可用模块异常类 + 英文字符串
- @BizMutation 自动包裹事务，不重复加 @Transactional（除非需显式传播控制）

执行步骤：
1. 收集所有 @BizModel 类。
2. 检查每个 BizModel：
   - 标准 CRUD 服务是否 extends CrudBizModel<T> + setEntityName()
   - 查询方法 @BizQuery + QueryBean + doFindList/doFindPage
   - 修改方法 @BizMutation + requireEntity + save/update/delete
   - 参数用 @Name（少量）或 @RequestBean（多参数），多字段返回用 @DataBean
   - 是否用 Map<String,Object> 代替类型安全结构（反模式）
3. 检查跨实体访问：所有 IDaoProvider/IOrmTemplate/@SqlLibMapper 注入是否有注释说明原因（AGENTS.md 规则）。
4. 搜索所有 throw 语句，检查：
   - 是否有 throw new RuntimeException / IllegalArgumentException（反模式）
   - 公共 API 是否用 ErrorCode；.param() 是否传递上下文
   - 异常链是否保留（catch 后 re-throw 传 cause）
   - 是否有吞掉异常（catch 后不处理）
   - 是否有硬编码中文错误消息（公共 API 应英文/ErrorCode，i18n 处理翻译）
5. 检查 @BizLoader 使用是否正确。
6. 检查事务边界：txn().afterCommit() 是否在事务上下文；是否有长事务（事务内远程调用）。
7. 检查是否有 BizModel 方法应属于另一聚合根。
8. 检查错误码命名是否一致（nop.err.mall.{domain}.{specific}）。

注：BizModel 返回实体对象是平台标准模式，不算问题。

输出格式：同标准发现格式。
```

---

### 维度 06：Delta 定制合规性

**子 Agent 提示词**：

```text
以下为"维度 06"的维度正文。派发时必须与上文"共享提示词前缀"拼接。

审核维度 06：Delta 定制合规性

目标：检查 app-mall-delta 对 nop-auth 的 Delta 覆盖是否合规、升级兼容。

必读文档：
- ../nop-entropy/docs-for-ai/02-core-guides/delta-customization.md
- docs/design/roles-and-permissions.md
- app-mall-delta/src/main/resources/_vfs/_delta/
- model/nop-auth-delta.orm.xml

执行步骤：
1. 查找 app-mall-delta 中所有 Delta 文件（_vfs/_delta/）。
2. 检查每个 Delta 文件：
   - 是否用 x:extends="super"（必须）
   - 是否用正确 x:override（replace/merge/remove）
   - 文件路径是否对应原始 nop-auth 路径
   - 是否在不必要处用 Delta（应直接改源模型的手写场景）
3. 检查 Delta 覆盖的 signUp / profile 等接口是否符合 design 文档（roles-and-permissions.md）。
4. 检查 Delta 新增字段是否需要 tag="not-gen" 标记。
5. 检查 Delta 是否与原始 nop-auth 产生冲突或循环继承。
6. 检查 nop-auth-delta.orm.xml 是否与 app-mall-delta 生成产物一致。
7. 评估 Delta 升级兼容性：nop-auth 升级时这些 Delta 是否会断裂。

注：app-mall-delta 覆盖 nop-auth 是设计意图本身，不是边界违规；审计重点是覆盖的合规性与正确性。

输出格式：同标准发现格式。
```

---

### 维度 07：代码生成管线完整性

**子 Agent 提示词**：

```text
以下为"维度 07"的维度正文。派发时必须与上文"共享提示词前缀"拼接。

审核维度 07：代码生成管线完整性

目标：检查 model→codegen→dao→meta→service→web 生成链路是否闭合、生成产物未被手改、源模型与产物一致。

必读文档：
- docs/architecture/module-boundaries.md（Code Generation 节 + Deploy DDL vs ORM Model Sync Status 节）
- ../nop-entropy/docs-for-ai/01-repo-map/domain-module-pattern.md
- ../nop-entropy/docs-for-ai/02-core-guides/model-first-development.md
- app-mall-codegen/ 的生成脚本
- codegen.sh（如存在）

执行步骤：
1. 确认源模型 model/*.orm.xml / model/*.api.xml 存在且格式正确。
2. 检查 app-mall-codegen 的生成脚本（postcompile/gen-orm.xgen 等）是否引用正确源模型。
3. 检查 dao 生成产物（_app.orm.xml、Entity.java、I*Biz.java）是否与源模型一致。
4. 检查 meta 生成脚本（precompile/gen-meta.xgen、postcompile/gen-i18n.xgen）。
5. 检查 web 生成脚本（precompile/gen-page.xgen）是否正确生成 view/page。
6. 检查生成产物是否有手写修改痕迹（_ 前缀文件、_gen/ 目录）。
7. 检查 service 的 xbiz 文件是否与 BizModel 方法对应（注意 xbiz 签名容差，见误报校准）。
8. 检查源模型与生成产物的时间戳一致性（产物更旧说明未重新生成）。
9. 检查 deploy/sql/ 三方言 DDL 与 ORM 模型 index 维度的同步状态（module-boundaries.md 已记录为已知漂移点，仅报告新增漂移）。

注：生成产物内容本身不审计（除非追溯到模型/模板错误）。_ 前缀文件被手改是 P0/P1 级问题。

输出格式：同标准发现格式。
```

---

## C. 前端与用户面

### 维度 08：AMIS 页面层

**子 Agent 提示词**：

```text
以下为"维度 08"的维度正文。派发时必须与上文"共享提示词前缀"拼接。

审核维度 08：AMIS 页面层

目标：检查 app-mall-web 的 .view.xml 三层模型（grid/form/page）、bounded-merge、业务动作按钮、字段可见性。

必读文档：
- ../nop-entropy/docs-for-ai/02-core-guides/（页面定制相关指南）
- docs/design/app-overview.md、feature-inventory.md
- model/*.api.xml（页面绑定的 API 契约）
- app-mall-web/src/main/resources/_vfs/app/mall/pages/ 下所有 .view.xml

执行步骤：
1. 收集所有 .view.xml 文件，分类：完整定制页 / 仅继承默认值的空壳页。
2. 对已完成阶段（done）的实体，检查其 view.xml 是否：
   - 提供必要的状态筛选/tab 分组（如订单按状态分组）
   - 提供必要的业务操作按钮（如发货、退款、上下架）
   - 展示关键字段（如价格构成、库存）
   - 与 xmeta 字段权限一致（只读字段不进表单）
3. 检查 .view.xml 的 x:prototype / bounded-merge 使用是否符合三层模型。
4. 检查页面绑定的 API（通过 model/*.api.xml）是否真实存在。
5. 检查 Delta 覆盖的页面是否用正确 x:extends/x:override。
6. 检查是否有页面引用了不存在的实体或字段。
7. 检查 admin 面与 mall 面是否正确分离（无共享 UI 组件越界）。

注：未开始阶段（todo）的空壳 view.xml 是预期状态，不报告。已完成阶段的空壳页才是 P1/P2 发现。

输出格式：同标准发现格式。
```

---

## D. 电商域逻辑

### 维度 09：电商域逻辑正确性

**子 Agent 提示词**：

```text
以下为"维度 09"的维度正文。派发时必须与上文"共享提示词前缀"拼接。

审核维度 09：电商域逻辑正确性

目标：检查核心电商域的状态机、一致性、并发与数据完整性：订单/库存/支付退款/价格/优惠券/售后/购物车。

必读文档：
- docs/design/order-and-cart.md、product-catalog.md、marketing-and-promotions.md、user-and-address.md
- model/app-mall.orm.xml（订单状态字典、库存字段、优惠券字段）
- app-mall-service/src/main/java/ 下订单/库存/支付/优惠券/售后相关 BizModel
- app-mall-wx/（支付集成，仅已完成部分）

核心检查点（电商域高频缺陷）：
1. 库存：扣减/回滚是否原子；超卖防护；并发竞态；reduceStock/addStock 的 SQL 是否事务安全
2. 订单状态机：状态转移是否合法（不能从已完成跳回待支付）；取消/超时/退款的状态流转
3. 支付退款：refund() 返回值是否被检查；退款失败是否阻止状态推进；支付回调幂等性
4. 价格：单价×数量=总价是否一致；优惠券/团购价格计算；金额精度（是否用整数分而非浮点）
5. 优惠券：使用/核销/过期；领取数量限制；并发领取竞态；CouponUser 状态流转
6. 售后：申请/审核/退款的状态机；退款金额校验；售后与订单状态联动
7. 购物车：选中商品/数量变更/默认地址/数量上限
8. 数据一致性：订单↔订单商品↔商品快照（下单时的价格/规格是否快照保存）

执行步骤：
1. 按上述 8 个核心检查点逐项审查对应 BizModel 方法。
2. 对每个状态转移，画出状态机并验证代码是否覆盖所有合法转移、拒绝非法转移。
3. 检查涉及金额、库存的字段类型是否为整数（分/件），非浮点。
4. 检查并发场景：是否有乐观锁/悲观锁；是否有竞态（如同时下单扣库存）。
5. 检查外部调用（支付）失败时的回滚/补偿/重试。
6. 检查是否有硬编码业务规则（应进设计文档/字典）。

注：支付路径受 AI 阻断条件约束（project-context.md）。app-mall-wx 的 stub（todo 阶段）不报告，但已完成阶段的 mock 支付逻辑要审计。

输出格式：同标准发现格式。域逻辑缺陷默认至少 P1。
```

---

## E. 验证、风险与流程

### 维度 10：验证充分性

**子 Agent 提示词**：

```text
以下为"维度 10"的维度正文。派发时必须与上文"共享提示词前缀"拼接。

审核维度 10：验证充分性

目标：检查构建/测试/手动证据是否真实覆盖已完成阶段，known-good baseline 是否更新。

必读文档：
- docs/context/project-context.md（验证命令）
- docs/testing/known-good-baselines.md
- docs/testing/index.md
- 各模块 src/test/java/

执行步骤：
1. 列出每个已完成阶段（done）对应的核心 BizModel，检查是否有测试覆盖。
2. 评估测试质量（不是数量）：
   - 是否覆盖核心业务逻辑的边界条件（非 happy path）
   - 是否覆盖错误路径（非法输入、并发、资源不可用）
   - AutoTest 快照是否与当前代码一致
   - 域逻辑（维度 09）的核心方法是否有测试
3. 检查 docs/testing/known-good-baselines.md 是否真实更新（非占位符）。
4. 检查是否存在"标记 done 但零测试"的阶段。
5. 检查测试是否依赖不该依赖的实现细节。
6. 检查测试是否用了正确的 NopAutoTest 基类。
7. 检查是否有"看起来很多测试但保护力弱"的位置（assertNotNull 遍历、getter/setter 测试）。
8. 检查 docs/testing/ 的手动/探索性测试记录是否与已完成阶段对应。

注：AutoTest 快照比对是平台标准模式，不审计快照文件本身。本项目 Lint/static check 与 E2E 当前为 none，这是已知状态不报告，但"已完成阶段无任何测试"是有效发现。

输出格式：同标准发现格式。
```

---

### 维度 11：回归与跨特性风险

**子 Agent 提示词**：

```text
以下为"维度 11"的维度正文。派发时必须与上文"共享提示词前缀"拼接。

审核维度 11：回归与跨特性风险

目标：检查阶段依赖、跨特性影响、并发/事务/数据完整性、潜在的回归注入点。

必读文档：
- docs/backlog/implementation-roadmap.md（阶段依赖图）
- docs/design/（跨特性业务规则）
- model/app-mall.orm.xml（跨实体关系）
- app-mall-service/ 涉及多实体的方法

执行步骤：
1. 检查阶段依赖：已完成阶段之间的依赖是否正确，是否存在"前置未真正完成就开启后续"。
2. 检查跨实体修改：一个 BizModel 方法修改多个实体时，事务边界是否覆盖全部（一致性）。
3. 检查 roadmap 状态漂移：标记 done 但实际有缺口的阶段（实体消除未完成等）。
4. 检查未开始阶段对已完成阶段的影响（如未来团购/微信支付集成是否会回归已写代码）。
5. 检查共享数据（如商品表被订单/购物车/收藏/足迹共用）的修改是否考虑全部消费方。
6. 检查 ORM 模型变更（如实体消除）是否会破坏引用它的代码/页面/模板。
7. 检查资源泄漏（未关闭连接/流）。
8. 检查是否有"修复一个问题引入另一个"的历史模式（参考 docs/bugs/ 如有）。

输出格式：同标准发现格式。
```

---

### 维度 12：路由、技能与流程合规

**子 Agent 提示词**：

```text
以下为"维度 12"的维度正文。派发时必须与上文"共享提示词前缀"拼接。

审核维度 12：路由、技能与流程合规

目标：检查 AGENTS.md 路由、强制技能加载、自主性政策、计划/闭合审计是否被遵守。

必读文档：
- AGENTS.md
- docs/index.md
- docs/context/ai-autonomy-policy.md
- docs/skills/README.md
- docs/plans/00-plan-authoring-and-execution-guide.md
- docs/audits/00-audit-execution-guide.md
- docs/backlog/implementation-roadmap.md

执行步骤：
1. 检查已完成阶段是否都有对应 plan，且 plan 是否通过 plan audit + closure audit。
2. 检查是否有"标记 completed 但 closure audit 缺失"的计划。
3. 检查 AI 自主性政策：保护区域（支付/数据删除/Auth/XML 模型/DB schema）的变更是否有 ask-first 或 human approval 证据。
4. 检查技能集合是否与项目匹配（如 nop-nodejs-backend 是否为遗留；Java 项目是否缺必要技能）。
5. 检查 docs/index.md 路由是否覆盖实际工作场景，引用路径是否有效。
6. 检查 backlog/roadmap 状态索引是否与实际代码实现一致（状态漂移）。
7. 检查是否有变更未更新日志（docs/logs/）或未更新对应 owner 文档。
8. 检查源真值优先级是否被违反（如以 chat 约定代替文档真值）。

输出格式：同标准发现格式。
```

---

### 维度 13：文档-代码一致性与维护

**子 Agent 提示词**：

```text
以下为"维度 13"的维度正文。派发时必须与上文"共享提示词前缀"拼接。

审核维度 13：文档-代码一致性与维护

目标：检查文档-代码一致性、文档维护是否到位、文档真值是否漂移。

必读文档：
- docs/context/conventions.md
- docs/references/maintenance-checklist.md
- docs/references/document-naming-and-timeliness.md
- docs/logs/（最新日志）
- docs/design/、docs/architecture/ 全部文件
- model/*.orm.xml、model/*.api.xml

执行步骤：
1. 检查设计文档引用的文件路径/行号/实体名/字段名是否仍与 live code 一致。
2. 检查设计文案与 ORM 字典（dict label / displayName）是否文字一致（如订单状态标签）。
3. 检查 module-boundaries.md 描述的测试位置/模块职责是否与实际漂移。
4. 检查 codebase-map.md 的 Last Verified 是否过旧、路径是否仍有效。
5. 检查已完成变更是否更新了 docs/logs/{year}/{month}-{day}.md。
6. 检查变更是否更新了对应 owner 文档（design/architecture）。
7. 检查 docs/bugs/、docs/lessons/、docs/discussions/ 是否有应提升为正式记录但缺失的内容。
8. 检查文档命名是否遵循 dated vs stable 规则（document-naming-and-timeliness.md）。
9. 检查是否有英文/中文混用违反中文化计划目标（design 中文、architecture 英文是设计意图，不算违规）。
10. 检查是否有"文档说 X 但代码做 Y"的对齐缺口。

输出格式：同标准发现格式。
```

---

## 附录 A：发现条目统一格式

每个发现条目必须遵循以下格式：

```markdown
### [维度{NN}-{序号}] 简短标题

- **文件**: `相对路径/文件名.java:行号范围`
- **证据片段**:
  ```java
  // 3-10 行代码片段
  ```
- **严重程度**: P0/P1/P2/P3
- **现状**: 一句话描述当前问题是什么
- **风险**: 不修复会怎样
- **建议**: 修复方向
- **信心水平**: 确定 / 很可能 / 有趣的猜测
- **误报排除**: 为什么这不是同类误报
- **复核状态**: 未复核 / 已保留 / 已降级 / 已驳回
```

对于纯文档发现（无代码证据），证据片段替换为文档原文引用，并标注 `[文档证据]`。

---

## 附录 B：汇总报告格式

```markdown
# 多维深度审计汇总报告

## 基本信息

- **审计对象**: [整个项目 / 某模块 / 某阶段 / 某 plan / 某变更集]
- **审计日期**: YYYY-MM-DD
- **执行维度**: [列出实际执行的维度编号和名称]
- **目标范围**: [描述审计的代码/文档范围]

## 执行统计

| 维度 | 深挖轮次 | 初审发现数 | 追加发现数 | 保留 | 降级 | 驳回 |
|------|---------|-----------|-----------|------|------|------|
| 01   | 3       | 5         | 3         | 6    | 1    | 1    |
| ...  | ...     | ...       | ...       | ...  | ...  | ...  |

## 按严重程度分布

| 严重程度 | 数量 | 主要类别 |
|---------|------|---------|
| P0      | N    | ...     |
| P1      | N    | ...     |
| P2      | N    | ...     |
| P3      | N    | ...     |

## 关键发现摘要

### P0 发现
[列出所有 P0 发现的编号、文件、一句话摘要]

### P1 发现
[列出所有 P1 发现的编号、文件、一句话摘要]

## 各维度评级

| 维度 | 评级 | 关键残留风险 |
|------|------|-------------|
| 01   | Good/Moderate/Poor | ... |

## 总评

[1-3 段对审计对象整体质量的评估]

## 优先修复建议

| # | 优先级 | 行动 | 关联发现 |
|---|--------|------|---------|
| 1 | P0 | ... | [维度NN-NN] |

## 本次审计盲区自评

[说明本次审计可能遗漏的方面、未运行命令、未深入的方法]
```

---

## 附录 C：审计对象特定调优指南

不同审计对象应有不同侧重：

### 整个项目审计（如阶段里程碑）

优先维度：01, 04, 09, 10, 12（需求/模型/域逻辑/验证/流程）

### 单模块审计（如 app-mall-service）

优先维度：03, 05, 09, 10, 13（边界/服务规范/域逻辑/测试/一致性）

### 单阶段闭合审计

优先维度：01, 09, 10, 11, 13（需求/域逻辑/验证/回归/文档）

### 变更集审计（某次改动）

优先维度：03, 04, 05, 11, 13（边界/模型/服务/回归/文档）

### 跨模块集成审计（如支付集成）

优先维度：03, 05, 09, 11, 12（边界/服务/域逻辑/风险/流程）

### 前端专项审计

优先维度：08, 04, 02, 13（页面/契约/设计一致性/文档）
