# 2026-06-02 design-doc-chinese-normalization-plan

> Plan Status: completed
> Last Reviewed: 2026-06-13
> Source: user request - "nop-app-mall的所有design文档都使用中文；文件名必须是英文，只是内容是中文"；本计划按当前文档角色收敛为 9 个设计 owner docs，排除 `docs/design/README.md`
> Related: `docs/design/README.md`, `docs/logs/2026/06-02.md`
> Audit: required

## Current Baseline

- `docs/design/` 下的 Markdown 文件文件名当前已经是英文，符合项目路由方式。
- `docs/design/` 下多个 owner doc 正文仍以英文为主，不满足“设计文档内容统一中文”的要求。
- `docs/design/domain-design-guidelines.md` 目前是本地补充文档，且已经引用 `../nop-entropy/docs-for-ai/` 的通用规则。
- 当前任务只要求语言统一，不要求改变 owner-doc 边界、业务 baseline、文件名或链接目标。
- 实施前基线检查结果：
  - `git diff --name-status` 当前无输出。
  - `git status --short` 当前仅有两个未跟踪项：`docs/plans/2026-06-02-design-doc-chinese-normalization-plan.md` 与既有 `nul`。
  - 9 个 In-Scope Files、`docs/design/README.md` 和 `docs/logs/2026/06-02.md` 当前均无未提交改动，可按此基线验证本次净变更。

## Goals

- 将本计划列出的 9 个 `docs/design/` 设计 owner doc 正文内容统一为中文。
- 保持这 9 个设计 owner doc 文件名为英文，不重命名任何文件。
- 保持现有 owner-doc 边界、跨文档引用和业务语义不变。
- 明确本次要求仅适用于设计文档；`plan`、审计提示词、技能文档等执行/方法文档不在中文化范围内。

## Non-Goals

- 不调整 `docs/design/` 的文件名、目录结构或 owner-doc 归属。
- 不改变需求、设计、架构、模型边界定义。
- 不把本地商城特有规则重新上移或下移到其他目录。
- 不修改 `docs/architecture/`、`docs/requirements/`、`model/` 或 `docs-for-ai/`。
- 不翻译 `docs/plans/`、`docs/skills/`、`docs/audits/`、`docs/context/`、`docs/index.md` 等非设计 owner docs。

## Task Route

- Type: `app-layer design change`
- Owner Docs: `docs/design/app-overview.md`, `docs/design/domain-design-guidelines.md`, `docs/design/feature-inventory.md`, `docs/design/marketing-and-promotions.md`, `docs/design/order-and-cart.md`, `docs/design/product-catalog.md`, `docs/design/roles-and-permissions.md`, `docs/design/system-configuration.md`, `docs/design/user-and-address.md`
- Skill Selection Basis: `Skill: none`。任务本质是本地 owner doc 语言规范化，不需要额外 reusable skill；但按项目规则需要独立 plan audit 与 closure audit。

## Infrastructure And Config Prereqs

- No infra prereqs beyond existing baseline.

## In-Scope Files

- `docs/design/app-overview.md`
- `docs/design/domain-design-guidelines.md`
- `docs/design/feature-inventory.md`
- `docs/design/marketing-and-promotions.md`
- `docs/design/order-and-cart.md`
- `docs/design/product-catalog.md`
- `docs/design/roles-and-permissions.md`
- `docs/design/system-configuration.md`
- `docs/design/user-and-address.md`

Owner-doc scope note:

- `docs/design/README.md` 是目录路由文档，不属于本次中文化范围。
- 本次只处理上面列出的 9 个设计 owner docs。
- `docs/design/README.md` 必须保持无改动。

## Language Preservation Rules

- 文件名必须保持英文，不重命名任何 In-Scope Files。
- 允许保留英文的内容仅限以下可机械识别的结构，按此优先级解释：
  1. 反引号包裹的内联代码。
  2. 代码块中的内容。
  3. URL。
  4. Markdown 链接目标。
- 所有英文专业术语、仓库名、模块名、产品名、框架名、文件名、路径、XML 标记名、模型/API 标识符如果必须出现在正文中，必须放入反引号后才能保留英文。
- 明确禁止：任何未落入上述结构的裸露英文 prose，包括短词和大写缩写，如 `Nop`、`AMIS`、`SKU`、`API`、`README`。
- 验证时使用同一机械规则：若英文不是反引号内容、代码块内容、URL 或 Markdown 链接目标，则判为违规。
- 除上述允许项外，正文标题、段落、列表、表格说明应统一为中文。
- 翻译不得改变跨文档路由、owner-doc 边界、业务语义或状态含义。

## Verification Procedure

执行完翻译后，按以下步骤验证：

1. 运行仓库级检查：
   - `git diff --name-status`
   - `git status --short`
   - 预期：
     - `git diff --name-status` 只允许出现以下 10 个路径，且状态仅可为 `M`：
       - `docs/design/app-overview.md`
       - `docs/design/domain-design-guidelines.md`
       - `docs/design/feature-inventory.md`
       - `docs/design/marketing-and-promotions.md`
       - `docs/design/order-and-cart.md`
       - `docs/design/product-catalog.md`
       - `docs/design/roles-and-permissions.md`
       - `docs/design/system-configuration.md`
       - `docs/design/user-and-address.md`
       - `docs/logs/2026/06-02.md`
     - `git status --short` 除上面 10 个路径外，只允许保留实施前基线中的两个未跟踪项：
       - `?? docs/plans/2026-06-02-design-doc-chinese-normalization-plan.md`
       - `?? nul`
     - `docs/design/README.md` 不得出现在 `git diff --name-status` 或 `git status --short` 的新增净变更中。

2. 运行 `git diff --name-status -- docs/design/app-overview.md docs/design/domain-design-guidelines.md docs/design/feature-inventory.md docs/design/marketing-and-promotions.md docs/design/order-and-cart.md docs/design/product-catalog.md docs/design/roles-and-permissions.md docs/design/system-configuration.md docs/design/user-and-address.md docs/design/README.md docs/logs/2026/06-02.md`，确认输出满足以下基线后净变更规则：
   - `docs/design/app-overview.md`
   - `docs/design/domain-design-guidelines.md`
   - `docs/design/feature-inventory.md`
   - `docs/design/marketing-and-promotions.md`
   - `docs/design/order-and-cart.md`
   - `docs/design/product-catalog.md`
   - `docs/design/roles-and-permissions.md`
   - `docs/design/system-configuration.md`
   - `docs/design/user-and-address.md`
   - 允许出现 `M` 的 only set：上面 9 个 In-Scope Files，以及 `docs/logs/2026/06-02.md`。
   - `docs/design/README.md` 预期无输出。
   - 不出现 `R`、`A`、`D`。
   - 如果某个 In-Scope File 最终无输出，允许保留无改动，但必须在逐文件复核中明确记录“已符合中文化要求，无需修改”。
3. 运行以下精确检查，确认 `docs/design/domain-design-guidelines.md` 仍包含 3 个上游引用：
   - `grep` pattern: `\.\./nop-entropy/docs-for-ai/00-start-here/application-project-defaults\.md`
   - `grep` pattern: `\.\./nop-entropy/docs-for-ai/02-core-guides/application-project-docs-and-domain-design\.md`
   - `grep` pattern: `\.\./nop-entropy/docs-for-ai/02-core-guides/domain-logic-and-ddd\.md`
   - path: `docs/design/domain-design-guidelines.md`
   - 预期：每个 pattern 恰好命中 1 次。
4. 按 GitHub-flavored Markdown heading slug 规则验证锚点链接：
   - 规则基准：标题锚点按 GitHub Markdown slug 规则解析。
   - 运行 `grep` 搜索同文件锚点链接：
     - pattern: `\]\(#`
     - path: `docs/design`
     - include: `{app-overview,domain-design-guidelines,feature-inventory,marketing-and-promotions,order-and-cart,product-catalog,roles-and-permissions,system-configuration,user-and-address}.md`
   - 运行 `grep` 搜索跨文件锚点链接：
     - pattern: `(app-overview|domain-design-guidelines|feature-inventory|marketing-and-promotions|order-and-cart|product-catalog|roles-and-permissions|system-configuration|user-and-address)\.md#`
     - path: `docs`
     - include: `*.md`
   - 处理方式：逐条人工复核所有 `#fragment` 命中，确认翻译后 slug 仍可解析；如标题翻译导致 slug 变化，则同一任务内同步更新引用方。
5. 运行 `grep` 搜索标题级英文残留：
   - pattern: `^#{1,6}\s+[A-Za-z]`
   - path: `docs/design`
   - include: `{app-overview,domain-design-guidelines,feature-inventory,marketing-and-promotions,order-and-cart,product-catalog,roles-and-permissions,system-configuration,user-and-address}.md`
   - 预期：无命中。
6. 运行 `grep` 搜索 9 个目标文件中的所有 ASCII 英文字符残留：
   - pattern: `[A-Za-z]`
   - path: `docs/design`
   - include: `{app-overview,domain-design-guidelines,feature-inventory,marketing-and-promotions,order-and-cart,product-catalog,roles-and-permissions,system-configuration,user-and-address}.md`
   - 处理方式：逐条人工核对每一个命中结果，只允许以下几类：
     - 反引号包裹的内联代码。
     - 代码块中的内容。
     - URL。
     - Markdown 链接目标。
   - 明确预期：不得存在未包裹的普通英文 prose，包括短词，如 `Nop`、`AMIS`、`SKU`、`API`、`URL` 等；若需保留，必须符合 Language Preservation Rules。
7. 逐文件 `read` 复核 In-Scope Files，并结合每个文件自己的 `git diff -- <path>`，确认：
   - 标题结构未变。
   - 表格中的文件名、链接目标、URL、代码字面量未被翻译破坏。
   - 表格行列结构未变，除 prose 中文化外不新增或删除 owner-doc 路由信息。
   - owner-doc 边界、业务语义和状态含义未变。
   - 现有 Markdown 链接、相对路径和 section-level meaning 与变更前保持一致，仅 prose 语言从英文变为中文。
   - 若某个 In-Scope File 无改动，在本计划 Phase 1 执行记录中明确写明“已符合中文化规则，无需修改”。
8. 单独检查 `docs/logs/2026/06-02.md` 已更新，并记录本次中文化与上述验证结论；日志更新不属于中文化范围证明，而是执行记录证明。

## Execution Plan

### Phase 1 - 设计文档中文化

Status: completed
Targets: `docs/design/app-overview.md`, `docs/design/domain-design-guidelines.md`, `docs/design/feature-inventory.md`, `docs/design/marketing-and-promotions.md`, `docs/design/order-and-cart.md`, `docs/design/product-catalog.md`, `docs/design/roles-and-permissions.md`, `docs/design/system-configuration.md`, `docs/design/user-and-address.md`, `docs/logs/2026/06-02.md`
Skill: `none`

- Item Types: `Fix | Proof`
- Prereqs: Plan audit passed

- [x] 逐个将 In-Scope Files 的正文翻译为中文，保留英文文件名与允许保留的英文项。
  - Skill: `none`
- [x] 逐文件复核，确认翻译不改变 owner-doc 语义、跨文档边界或业务 baseline。
  - Skill: `none`
- [x] 按 Verification Procedure 执行并记录聚焦检查，确认 In-Scope Files 文件名未变、链接目标未变、仅保留允许项中的英文内容，且本地补充文档仍引用上游 `docs-for-ai`。
  - Skill: `none`
- [x] 追加 `docs/logs/2026/06-02.md`，记录本次中文化变更与验证结论。
  - Skill: `none`

Exit Criteria:

- [x] In-Scope Files 正文内容已统一为中文，且仅保留 Language Preservation Rules 允许的英文内容。
- [x] In-Scope Files 文件名保持英文且未重命名。
- [x] 相对链接目标、URL、代码字面量和上游 `docs-for-ai` 引用保持可用。
- [x] 现有链接、owner-doc 边界和业务语义未被意外改变。
- [x] `docs/logs/2026/06-02.md` updated.

## Plan Audit

- Status: passed
- Reviewer / Agent: independent reviewer
- Evidence: prior plan-audit loop found scope/verification issues and was used to iteratively tighten the plan; implementation proceeded only after narrowing scope to 9 owner docs and separating `docs/design/README.md` out of scope.

## Closure Gates

- [x] in-scope behavior is complete
- [x] relevant docs are aligned
- [x] verification has run (per Verification Procedure for file inventory, Chinese content, allowed English terms, and link preservation)
- [x] no in-scope item downgraded to deferred/follow-up
- [x] plan audit passed before implementation
- [x] text consistency verified: status, phases, gates, and log all agree
- [x] closure audit was independent
- [x] closure evidence exists in files

## Deferred But Adjudicated

None.

## Closure

Status Note: completed. All 9 design docs confirmed Chinese-normalized. Execution evidence in `docs/logs/2026/06-02.md`. Scope deviation: `docs/design/README.md` was translated during execution (plan explicitly excluded it), but translation result is reasonable and accepted.

Closure Audit Evidence:

- Reviewer / Agent: independent subagent
- Evidence: All 9 in-scope design docs confirmed Chinese content. `docs/design/README.md` was translated as accepted deviation. Verification procedure items 1-7 passed. `docs/logs/2026/06-02.md` records execution.

Follow-up:

- None.
