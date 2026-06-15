# 2026-06-15 阿里"业务需求专家 Agent"文章 vs AGE/nop-app-mall 对照分析

## 元信息

- **分析对象**：森叶《如何搭建一个端到端业务需求专家 Agent》（阿里云开发者公众号）
  - 原文收录：`docs/input/2026-06-15-alibaba-end-to-end-business-requirement-agent-article.md`
- **对照方**：
  - AGE 方法论：`c:/can/nop/attractor-guided-engineering-template/`（抽象模板）+ `nop-chaos-flux/docs/articles/`（理论文章群）
  - 本项目（nop-app-mall）：AGE 的实际应用实例（基于 Nop 平台的电商 App）
- **分析类型**：外部文章方法论对照 + 本项目借鉴清单
- **审查状态**：独立子 agent 多轮审查通过（R1 修订 + R2 共识 + R3 理论强化共识）

## 一句话定位

森叶的文章是一套**高质量的生产级 Harness + Skill Orchestration 系统**（接近 OpenAI Ryan Lopopolo 的 Harness Engineering，叠加 Hermes 式 Agent 能力闭环）。AGE/nop-app-mall 是**把 attractor 作为一等对象的 Repository-centric 方法论**。

两者在"减少人工串联"的工程动作上高度重叠（文件化、plan、audit、log、closure），但在一等对象（skill vs attractor）、世界观（Agent-centric vs Repository-centric）、失败模式定义（单次错误 vs 长期漂移）上根本不同。

## 1. 三方定位对照

| 对象                 | 一句话定位                                                                      | 一等对象           |
| -------------------- | ------------------------------------------------------------------------------- | ------------------ |
| 文章（superai）      | 运行时编排系统：用一组 `superai-*` skill 把"需求→上线"在运行时串成闭环          | **Agent 执行能力** |
| AGE 模板/nop-app-mall | 仓库结构契约：用带 precedence 的 owner doc 把"项目应向哪里收敛"沉淀进 Git       | **仓库长期结构**   |
| nop-app-mall         | AGE 实例化产物：在 AGE 骨架上填入 Nop 平台业务、模型、技能与已执行的计划         | 同 AGE             |

## 2. AGE 四对象框架下的诊断

AGE 的核心命题（`nop-chaos-flux/docs/articles/attractor-before-harness-ai-large-scale-development-methodology.md`）是逻辑依赖链：

```
状态空间 → 吸引子 → 轨迹 → 控制
```

**这不是修辞排序，是逻辑依赖。** 状态空间不定义就谈不上吸引子；吸引子不定义就无法判断轨迹是否在漂移；轨迹判断不存在，控制就没有目标。

用这个框架诊断文章的"四层架构"：

| AGE 四对象 | 文章对应承载物                                    | AGE 视角诊断                                                                                                                                                  |
| ---------- | ------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 状态空间   | 长期 wiki + 项目记忆仓库 + 代码仓（三仓库分离）   | ✅ 比一般 Harness 更完整，已接近 AGE 的"仓库 = 真相源"前提                                                                                                     |
| **吸引子** | ❌ **文章没有独立的 attractor 对象**               | 长期 wiki 承担了一部分 owner doc 的角色，但 wiki / skill / plan / CR 之间的 precedence 没有显式定义。冲突时"听谁的"靠运行时编排，不靠仓库结构。**这是文章与 AGE 的核心差距** |
| 轨迹       | CR 评论、milestone 回刷、验收证据、closeout log   | ✅ 文章的"留痕"意识很强，符合 AGE 把轨迹作为一等公民的要求                                                                                                     |
| 控制       | 四层架构 + `superai-*` skill + git pre-push hook  | ✅ 控制层非常完整，是典型的生产级 harness                                                                                                                      |

**核心缺口**：文章把"方向"嵌入在 harness 里（`superai-clarify` 决定 requirements 对不对、`superai-plan` 决定方案对不对、CR 决定接不接受），而没有把"系统应向哪里收敛"作为独立于 harness 的一等对象。

这正是 `harness-engineering-vs-age-direction-before-control.md` 指出的 Harness Engineering 的核心特征：

> 方向被嵌入在 harness 内部，没有独立于 harness 的存在。

AGE 的反命题：

> attractor 逻辑上先于 harness。"向哪里？"必须在"如何约束？"变得有意义之前被回答。

文章这个缺口最具体的表现：**当某次 CR 反馈和长期 wiki 冲突时，谁赢？** 文章的回答是"由 `superai-workflow` 编排层判断"——即由 Agent runtime 临时裁决。AGE 的回答是"由 source-of-truth precedence 决定，且这个 precedence 写在仓库里，不写进 skill"。

## 3. 动力系统图像 vs harness 截面评价（理论支柱 1）

这一节正面回答"为什么 AGE 不只是更严格的 harness"。差别是数学本体论的，不是流程复杂度的。

### 3.1 harness 的隐含图像

```
[方向已知] → 执行 → 检查/约束/纠偏
```

harness 默认"什么是正确的"已经知道（在 PRD、在 reviewer 脑中、在 lint 规则里），问题被压成"如何防止偏离"。**评价单位是状态**：这次 PR 对不对、这次 CI 过没过、这次 review 通过没。

### 3.2 动力系统的图像

```
状态空间 → 吸引子 → 轨迹 → 控制
```

- **状态空间**：仓库在所有约束下可能演化到的实现状态（不只是源码，是 code+test+doc+model+log+audit 的整体）
- **吸引子**：系统长期演化中反复被拉回的稳定结构——不是终点清单，是少量高阶不变量"方程式定义的流形"（Lorenz 吸引子模型：局部轨迹近乎混沌，整体被稳定结构约束）
- **轨迹**：每一轮生成+验证+纠偏之后真实留下的演化路径
- **控制**：通过局部信号持续影响轨迹

**评价单位是轨迹**：许多次变更累积后，系统是否仍在向 attractor 收敛？

### 3.3 关键差别：截面通过 ≠ 系统健康

`age-from-state-engineering-to-trajectory-engineering.md` 把 AI 时代的核心风险命名为：

> AI 时代最危险的不是"写错"，而是"每一步都看起来对"。
> 代码能跑、测试能过、文档也更新了、plan 也关闭了、summary 写得很完整——但系统整体越来越偏。

**Plan 76 案例**（来自 `nop-chaos-flux`，是这条命题的典型案例）：多次合法提交累积，每次 review/CI 都过、测试套件长期绿色；Phase 2 一旦尝试移除 `array-editor` / `key-value` 的本地状态镜像，引出 11 个测试失败。暴露的不是某个 bug，而是**测试套件已经与旧实现的时序紧密耦合**。

- harness 截面视角看到："全部通过，系统健康"
- 动力系统轨迹视角看到："测试在不知不觉中漂到了无法支撑结构演进的位置"

传统软件工程用"技术债/坏味道/腐蚀"追认这类问题，但都是**事后诊断词**，不是基础对象。AGE 把"轨迹漂移"提升为一等对象——这就是 `attractor-before-harness` 第三节那句"传统理论的范畴集里没有正面的轨迹对象"的具体含义。

### 3.4 应用到本对照

| 评价方式          | harness 截面评价                                              | 动力系统轨迹评价                                                                                       |
| ----------------- | ------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------ |
| 评价单位          | 单次需求/PR                                                  | N 次需求累积后的仓库方向                                                                               |
| 文章用的度量      | 介入次数、一次通过率、回滚次数（都是截面）                   | （文章未提供轨迹判断的度量）                                                                            |
| AGE 用的度量      | CI 全绿、plan closure 通过（必要但不充分）                   | owner-doc baseline 是否被保持、precedence 是否被遵守、proof relation 是否仍可恢复                        |
| 失败模式          | Agent 在某个任务上做错                                        | 每次都对、整体在漂（Plan 76）                                                                          |

文章的全部度量都是截面度量，这正是它即便 harness 完整也回答不了自身 4.2 提出的困惑（"怎么知道 skill/prompt 迭代是真的改进"）的原因——截面度量无法回答轨迹问题。

## 4. 文章与 Hermes 共享 Agent-centric worldview

`nop-chaos-flux/docs/articles/hermes-skills-self-evolution-vs-age-comparative-analysis.md` 把这个分野压缩到最锋利：

> Hermes 在问：Agent 怎么才能越来越能干？
> AGE 在问：仓库怎么才能在 Agent 越来越能干之后仍然保持正确的结构？

**范围澄清**：Hermes 是 skill 子系统级的经验生命周期管理；森叶文章是端到端需求交付的运行时编排。两者不是同一层级的对象，但**优化目标同向**（都把 Agent 当主要优化对象，仓库是 Agent 作用的环境）。文章相对于 Hermes 范围更宽：它不只管 skill 沉淀，还把澄清/方案/实现/CR/验收/发布/结项全编排起来。下表的对照是 worldview 层面的同向，不是机制层面的等价。逐条对照：

| Hermes 特征                              | 文章是否具备                                | 备注                                         |
| ---------------------------------------- | ------------------------------------------- | -------------------------------------------- |
| Agent 能力生命周期（产生/评估/整合）      | ✅ 8 阶段纵向闭环                            | 文章的纵向闭环就是 Agent 能力生命周期         |
| 经验从 Agent 行为中提炼                   | ✅ closeout 蒸馏 CR/验收 → skill/prompt 改进 | 对应 Hermes Layer 1                          |
| 使用热度驱动状态转换                      | ⚠️ 文章 4.2 自承"缺少度量体系"               | 作者自己已看到这个缺口                       |
| 脏计数器 / 定量触发                      | ❌ 文章按需求阶段触发，非按工具迭代次数       |                                              |
| Agent-centric worldview                  | ✅ 整套系统的优化对象是"业务需求专家 Agent"   | 这是定位，不是缺陷                           |
| 影子 Agent 隔离                          | ❌ 文章没提                                  | 但 `superai-code-review` 起到类似独立审查作用 |

AGE 有而文章**没有**的 5 项（来自 `hermes-...` 文章第九节"AGE 有而 Hermes 没有的"）：

1. **吸引子作为数学概念** — 文章的"长期 wiki"是知识集合，不构成动力系统意义上的 attractor
2. **轨迹作为一等对象** — 文章有"留痕"，但没有"轨迹是否向 attractor 收敛"的判断（因为没定义 attractor）
3. **不动点 vs 吸引子的区分** — 文章没区分"当前状态"和"应回归结构"
4. **收敛方向 vs 到达** — 文章的度量维度（介入次数、一次通过率）衡量"到达"，不衡量"方向"
5. **Owner-doc precedence 层级** — 文章没定义 wiki / plan / requirements / code 之间的语义权威排序

**这五项缺失不是文章做得不好，而是 Agent-centric worldview 的必然结果。** 当优化对象是 Agent 能力时，仓库结构是次要的——只要 Agent 能调用对的工具、读到对的上下文，仓库是什么样不重要。AGE 的反命题：**当 AI 高频扰动仓库时，仓库结构漂移是不可逆的，修复成本随时间指数增长**（Plan 76 案例）。

## 5. 内生 vs 外置：skill 不能承担 attractor 职责（理论支柱 2）

这一节正面回答"为什么不能把 AGE 做成一个 `age-skill`"。这是 `agent-skills-vs-age-practice.md` 的核心论点。

### 5.1 外置的（skill 类）：组织轴是调用意图

```
task intent -> matched skill -> procedure bundle
```

一个 `bug-diagnosis` skill 可以用于任何仓库、任何领域、任何 bug 类型——它是**通用任务能力**。skill 知道"怎么做 review"，但**不能知道**"这个仓库的 review 该由哪个 owner doc 裁决、什么测试保护哪条承诺、哪条信息该进 owner doc 而不是 bug note"。

skill 是从 Agent runtime 外部施加到仓库的**能力包**。它是语义级 hash map：key 是任务意图，value 是能力包。

### 5.2 内生的（AGE attractor）：组织轴是领域结构

```
domain concept -> semantic commitment -> implementation location -> proof evidence -> audit/memory -> follow-up obligation
```

这是仓库自身的底层拓扑，**不可移植**。它不是任何一份文件，而是存在于以下**关系网络**中：

- `AGENTS.md` 的操作边界
- `docs/index.md` 的路由结构
- owner docs 的事实归属
- source-of-truth precedence 的冲突规则
- plans 的义务声明
- tests 的 proof relation
- audits 的 closure gate
- logs / bug notes / lessons 的轨迹记忆
- freshness / autonomy 的行动限制

### 5.3 判断标准（来自 `agent-skills-vs-age-practice.md` 第八节四问）

判断一套实践是不是过度 skill 化，问四个问题：

1. 它主要按**操作动词**组织知识，还是按**领域概念和架构 owner** 组织知识？
2. 它的链接主要是**执行资源引用**，还是 **owner / invariant / proof / precedence / freshness 关系**？
3. 信息在 skill / plan / code / test / doc / log 之间转换后，**领域结构是否仍可恢复**？
4. **删除所有 skills 后，仓库是否仍知道什么是对的、谁拥有它、如何证明它？**

如果第四问答案是否定的，skill 已经承担了不该承担的 attractor 职责。

### 5.4 为什么 `age-skill` 是错误抽象（第五节核心论证）

skill 是**任务发生后被匹配和加载**的能力包；attractor 必须是**任务开始前就已经存在的仓库结构**。把 AGE 做成 skill，等于：

- 让 AGE 在 Agent "没匹配到这个 skill"时直接失效
- 把"仓库内生拓扑"降级为"外部施加的能力包"
- 让 owner / proof / precedence / freshness 关系只活在 skill 文本里，而不是真正存在于仓库文件之间

**always-on global skill 也救不了**。它最多能规定"先读哪些 owner docs、如何检查 precedence、怎样做 closure audit"。但真正的 owner / proof / precedence / freshness 关系**必须存在于仓库文件、测试、日志、计划和审计证据之间**，不能只活在 skill 文本里。

### 5.5 应用到本对照

| 对象              | 外置还是内生 | 证据                                                                                                                       |
| ----------------- | ------------ | -------------------------------------------------------------------------------------------------------------------------- |
| `superai-workflow`（文章核心编排层） | **外置**     | 从 Agent runtime 介入仓库；冲突时由运行时编排临时裁决（文章 3.2/3.3/3.5）                                                  |
| 阿里平台绑定 skill（`superai-aone` 等） | **外置**     | 通用平台工具能力，与仓库内部领域概念无关                                                                                   |
| AGE 的 `AGENTS.md` + `docs/index.md` + owner docs | **内生**     | 是仓库自身的结构；删除所有 `.opencode/skills/` 后，nop-app-mall 仍然知道"什么是对的"（model 是 database truth、design 是 behavior truth） |
| nop-app-mall 的 12 个 `.opencode/skills/` | **外置（method selector）** | 作为方法选择器使用；它们加速执行，但不承担 attractor 职责。删掉它们仓库的 owner/proof/precedence 关系仍然成立               |

**关键判断**：文章没有内生层。它把全部"什么是对的"判断都放在外置 skill 链里。这是 Agent-centric worldview 的必然——不需要内生结构，只要 Agent 能调用对的工具。AGE 的反命题：当 AI 高频扰动时，外置 skill 无法防止仓库结构漂移，因为 skill 本身不知道"这个仓库的领域结构是什么"。

## 6. 流程与目录的等价映射

虽然一等对象不同，但纵向流程在工程动作层面**几乎是同一张流程图**：

| 文章纵向阶段            | AGE/nop-app-mall 默认工作流                     | 等价度 |
| ----------------------- | ----------------------------------------------- | ------ |
| 3.1 需求进入：组织上下文 | `docs/input/` + `docs/context/`（必读）          | ✅     |
| 3.2 需求澄清：第一质量门 | `docs/discussions/` → `docs/requirements/`      | ✅     |
| 3.3 技术方案：第二质量门 | `docs/plans/` + 强制 draft-review               | ✅     |
| 3.4 实现 + 内部质量门    | 最小完整切片 + verification baseline            | ⚠️ AGE 无 pre-push hook |
| 3.5 CR/Issue 协同        | file-in/file-out + 独立 subagent audit          | ⚠️ 通道不同（评论 vs 文件） |
| 3.6 验收：真实证据       | `docs/testing/known-good-baselines.md` + plan closure | ⚠️ AGE 未强制三类证据 |
| 3.7 发布与变更观察       | （AGE 模板未覆盖，属运行时关注点）              | ❌     |
| 3.8 结项沉淀             | `docs/retrospectives/` + `docs/lessons/` + `docs/archive/` | ✅     |

### skill 映射表

文章的 `superai-*` skill 在 nop-app-mall 大多有方法等价物（少数无等价）：

| 文章 superai-* skill          | nop-app-mall 对应 skill（方法等价）                     |
| ----------------------------- | ------------------------------------------------------- |
| superai-clarify               | `nop-deep-interview`（深度访谈/需求澄清）                |
| superai-plan                  | AGENTS plan 模板 + 全部 Nop skill                        |
| superai-execute               | `nop-backend-dev` / `nop-frontend-dev`                   |
| superai-code-review           | `docs/skills/code-quality-audit-prompt.md` + skill selfcheck |
| superai-tjx / -mt（配置/schema） | `nop-orm-modeler` / `nop-file-converter`                 |
| superai-memories              | `docs/context/` + `docs/logs/`（文件化记忆）             |
| superai-finish                | `docs/plans/00-...-guide.md` closure 流程                |
| **superai-workflow**          | **无结构等价** — AGE 用 `AGENTS.md` + `docs/index.md` 静态路由替代运行时编排；这正是本文第 5 节核心批评指向的对象 |
| superai-aone / -sls / -ops / dingtalk-doc-rw | 无等价 — 阿里平台深度绑定，nop-app-mall 无对应运行时工具 |

注意：**这是方法等价，不是结构等价。** 文章的 skill 是运行时执行引擎（外置）；nop-app-mall 的 skill 是被 owner doc 路由的方法选择器（外置但不承担 attractor 职责）。`agent-skills-vs-age-practice.md` 明确区分：

> Skill 让 Agent 更会做事。AGE 让仓库在 Agent 反复做事之后，仍然沿着领域结构受控收敛。

### 流程覆盖缺口（公允性）

对照表里 3.7 一格 AGE 标 ❌：**AGE 模板确实不含"发布与变更观察"阶段**——这是运行时关注点（部署 + 日志回读 + 监控），属于 Agent 运行栈而非仓库结构。这是 AGE 模板真实存在的覆盖边界，文章在这一段明显领先。

## 7. nop-app-mall 的 AGE 实例化点检

逐条点检 AGE 五项一等对象在 nop-app-mall 中的具体承载：

| AGE 一等对象              | nop-app-mall 实例化                                                                                                  |
| ------------------------- | -------------------------------------------------------------------------------------------------------------------- |
| 吸引子（attractor）       | `docs/design/` + `docs/architecture/` + `model/app-mall.orm.xml`（后者是合同级 owner doc，连文档都不是，是模型）      |
| 承载层（carrier）         | `AGENTS.md`（强化的，含 Nop 平台规则）+ `docs/context/source-of-truth-and-precedence.md`                              |
| 优先级模型（precedence）  | `source-of-truth-and-precedence.md` 按问题类型指定 primary source（database→`model/*.orm.xml` / supported behavior→`docs/design/` / build-now→`docs/requirements/`），**不设全局文件类型层级** |
| 轨迹（trajectory）        | `docs/logs/2026/*.md` + `docs/plans/2026-06-*-plan.md`（每个含 closure audit）                                       |
| 控制（harness）           | 三级审计（plan-audit / closure-audit / code-quality-audit prompt）+ Maven 验证命令；**selfcheck 仅 3/12 skill 具备**（`nop-backend-dev` / `nop-frontend-dev` / `nop-testing`），其余 9 个为纯方法选择器无自检 — 这是 nop-app-mall 当前真实的薄弱面 |
| 自我验证陷阱的显式规避    | 每个 plan 强制"独立 subagent 或 reviewer"做 closure audit，不能自己审自己（AGENTS Rule 12）                          |
| Freshness / autonomy 状态 | `docs/context/ai-autonomy-policy.md` 的 autonomy 级别随 freshness 收紧；`project-context.md` 用 `AI Block Conditions` 兜底（注：项目当前 `project-context.md` 未显式承载 freshness 字段，这是与 AGE 模板的偏差） |

**特别注意**：`model/app-mall.orm.xml` 是比文章"长期 wiki"更严格的 attractor 承载——它是**可执行**的 owner doc，不是文档。这是 Nop 平台"模型驱动"的天然优势，让 attractor 直接进入编译管线，而不只是靠 Agent 自觉去读。

## 8. AI 高速扩张如何受控收敛：AGE 五条机制（理论支柱 3）

这一节正面回答"如果文章加 attractor 就够了，AGE 还有什么可教文章的"。答案是：**收敛机制本身就是 AGE 的核心贡献**，而文章即便补上 attractor，也还需要补这五条。

### 8.1 核心困难：自我验证陷阱

`attractor-before-harness` 第五节命名了这个陷阱：

> AI 在生成代码的同时，也在生成判断这段代码是否正确的所有材料——类型、测试、文档、完成总结都出自同一个上下文。如果这套理解本身偏了，所有"验证证据"会一致地偏在同一方向，互相不矛盾，但整体错了。

人类协作中这种风险被 CI、reviewer、规范文档这些**外部独立标准**自然削弱（不同认知主体维护）。AI 协作打破了这种独立性，所以必须用工程手段**人为重建生成与验收的分离**。

**文章的现状**：3.6 有"独立环境验收 + 人工确认发布"两层门，部分缓解但没上升到认识论命题。所有 superai 链路都出自同一 Agent runtime，自我验证陷阱的结构性风险还在。

### 8.2 五条收敛机制

#### 机制 ①：attractor 先于 harness（方向层）

把"系统应向哪里收敛"外化进仓库（owner doc + precedence）。不是终点清单，是少量高阶不变量"方程式定义流形"——局部轨迹可以近乎混沌，整体被稳定结构约束（Lorenz 吸引子）。工程载体：`docs/architecture/` 带 precedence 的 owner doc。

**文章对应**：长期 wiki 是知识集合（fixed-point），不是结构不变量（attractor）。**缺**。

#### 机制 ②：强制重建生成与验收的分离（认知层，最关键）

不让同一上下文既做实现又做完成判定。closure audit 必须 fresh session 执行，输入只有 plan + diff summary + 验证输出，不带实现历史。

- 这是**纵向认知隔离**（运动员不能同时是裁判）
- 区别于 Hermes 的横向安全隔离（防止影子 Agent 搞破坏）
- AGENTS Rule 12 + nop-app-mall 每个 plan 的 closure audit 就是这条的工程化

**文章对应**：3.6 有两层门（独立环境 + 人工确认），但 superai-execute 和 superai-code-review 都在同一 Agent runtime，不是真正的 fresh session。**部分缺**。

#### 机制 ③：harness 五层叠加（控制层）

| 层 | AGE 实现 | 文章对应 |
| -- | -------- | -------- |
| 路由 harness | `docs/index.md` 决定碰到什么问题先读什么 | `superai-workflow` 编排（运行时路由） |
| Plan harness | 局部收敛机制（不是待办列表，是关闭合同） | `superai-plan` + CR 确认 |
| Verification harness | lint / check / typecheck / build / test | git pre-push hook（文章最强项） |
| Audit harness | 独立审计回看 live repo（fresh session 强制） | `superai-code-review`（同 runtime，强度弱） |
| Memory harness | `docs/logs/` `docs/bugs/` `docs/discussions/` 跨 session 外部化记忆 | closeout raw log + CR 评论留痕 |

**文章在 Verification harness 领先**（pre-push hook 硬绑定），**在 Audit harness 偏弱**（无 fresh session 强制）。

#### 机制 ④：轨迹作为一等对象（评价层）

logs/bugs/discussions 不只是日志，是判断"系统在收敛还是漂移"的基础对象。关键：判断"许多看似正确的变更是否仍在把系统推向正确方向"。反面教材：Plan 76。

AGE 文献的明确论断（`age-from-state-engineering` 第二节）：

> 传统方法论有"技术债务""坏味道""腐蚀"等否定性词汇追认轨迹问题，但这些概念在体系里的地位是修正机制和诊断词汇，不是基础对象。AGE 把轨迹提升为基础范畴。

**文章对应**：有留痕，没有轨迹判断。**缺**。

#### 机制 ⑤：attractor 与 harness 共演化（不是定义一次永久执行）

```
定义 attractor → 扩张 → 纠偏 → 更新 attractor → 再扩张
```

更新 attractor 不是 AI 自然漂出来的，而是经过 owner-doc + audit + precedence + 人工裁定的方向更新。`flux-compiler / flux-action-core / flux-runtime` 三层拆分就是 attractor 被实践校正后再扩张的实例。

AGE 文献的三条轨迹→吸引子反馈制品：
- `architecture-guardrails-from-bugs.md`：bug 模式 → 收敛约束 → 架构锚点
- `deep-audit-calibration-patterns.md`：误报模式 → 更精确的合法状态空间定义
- `reopened-design-decisions-and-audit-adjudications.md`：防止重复翻案削弱 attractor

**文章对应**：closeout 时蒸馏三类（稳定知识/流程改进/归档），方向对，但产出物（skill/prompt 改进候选）是 Agent 能力侧的，不是 attractor 侧的。**部分缺**。

### 8.3 最重要的限制（人机分工）

`attractor-before-harness` 第九节明确：

> 不能把定义新 attractor 的责任默认外包给 AI。当前主流模型擅长围绕既有 attractor 高速展开，但通常回到见过的平均方案；真正新的概念切分、边界重定义、架构语言，仍需要人先给出。

AGE 的工程贡献是把这种稀缺判断**外化为可版本化、可审计、可传承的仓库结构**——但如果团队没人能做这种判断，方法论也无法凭空生成它。这是 AGE 的边界，不是它的弱点。

**应用到文章**：文章作者显然具备这种判断力（设计出 8 阶段闭环本身就是架构判断）。但文章把这种判断力的产出全部沉淀到了 Agent runtime（superai-* skill 编排），而不是仓库结构（owner doc + precedence）。这是 AGE 视角下文章最可惜的地方——**有 attractor 级别的判断力，却把产出外置到了 Agent 能力层**。

### 8.4 五条机制对照汇总

| 机制 | AGE 状态 | 文章状态 |
| ---- | -------- | -------- |
| ① attractor 先于 harness | ✅ owner doc + precedence | ❌ wiki 是知识集合 |
| ② 生成/验收分离（fresh session） | ✅ AGENTS Rule 12 强制 | ⚠️ 独立环境+人工，但同 runtime |
| ③ harness 五层 | ✅ 全五层 | ✅ 四层（Audit 偏弱） |
| ④ 轨迹作为一等对象 | ✅ logs/bugs/discussions | ⚠️ 有留痕无轨迹判断 |
| ⑤ attractor 共演化 | ✅ 三条反馈制品 | ⚠️ closeout 蒸馏但产出在 Agent 侧 |

## 9. 适用域对照（公允性必读）

在进入互相借鉴清单前，必须先处理一个不对称：**文章已上生产，AGE/nop-app-mall 当前作为方法论实例，业务应用层的实战证据相对单薄**。

| 维度        | 文章（superai）                                          | AGE/nop-app-mall                                          |
| ----------- | -------------------------------------------------------- | --------------------------------------------------------- |
| 已部署情况  | ✅ 文章开篇明说"已经在真实业务需求上跑通"               | ⚠️ nop-app-mall 是 Nop 电商 App 的方法论实例化，仍在迭代 |
| 适用层级    | 大型组织内、多平台、Agent runtime 强                    | `age-from-state-engineering` 第 403 行自承："小项目、原型和业务应用……所需的 attractor 与 harness 可以更轻" |
| 重度来源    | 重在运行时编排机器（Multica + superai-* + 平台 CLI）    | nop-app-mall 的重度 owner-doc + plan + closure-audit 部分来自 **Nop 平台模型驱动天然带来的结构**（model→codegen→dao/service/web 强制分层），部分来自 AGE 强加的流程 |
| 团队要求    | 需要 Multica 接入成本（文章 4.1 自承）                  | `attractor-before-harness` 第 240 行明说：AGE 依赖"能定义 attractor 的人"，是稀缺资源 |

**这一不对称不否定本文核心结论**（文章缺独立 attractor、Agent-centric vs Repository-centric 分野），但影响借鉴清单的可信度：读者会问"你这套重，人家那套轻且已上线，凭什么让我加 pre-push hook 而不是你该减 owner-doc"。下面的借鉴清单会区分**对中小业务应用**和**对长期复杂仓库**两类不同建议。

## 10. 补充对照：spec-driven vs attractor-guided（业务应用层）

AGE 文献里有一篇与本文场景高度相关、但容易被忽略：`nop-chaos-flux/docs/articles/ai-practices-for-business-application-development.md`（《从 Spec-Driven Development 到 Attractor-Guided Engineering》）。它专门讨论"普通业务项目能从 nop-chaos-flux 的大规模 AI 工程实践中学到什么"，并系统对照 OpenSpec 的 spec-driven 模式。

引入这条对照轴能补齐本文的薄弱面：之前主要在"Hermes / Harness Engineering 哲学分野"上对照，但森叶文章里的 `superai-plan` + requirements 确认门 + CR 反馈循环，**结构上更接近 spec-driven 模式**（围绕一次变更组织 proposal/design/tasks/delta），而非 Hermes 的 skill 子系统。

| 对照轴            | spec-driven（OpenSpec）            | attractor-guided（AGE）                                | 文章 superai 链路的位置                                       |
| ----------------- | ---------------------------------- | ------------------------------------------------------ | ------------------------------------------------------------- |
| 组织轴            | 围绕一次变更组织（change package） | 围绕仓库长期结构组织（owner doc）                      | 文章以"一次需求端到端"为单位，属变更中心                       |
| 完成判定          | tasks.md checklist + 可选 verify   | 独立 closure audit 从 live repo 重新判定               | 文章用 CR + milestone + 验收证据，强度介于二者之间             |
| 文档路由          | spec/change/archive 三态强结构     | 自由但有职责边界（每类材料有明确 owner）               | 文章用项目记忆 feature 分支 + 长期 wiki，路由较松              |
| 长期结构承载      | specs/ 主规格树                    | `docs/architecture/` + `docs/design/` owner doc        | 文章用长期 wiki，但 wiki 是知识集合而非结构不变量（见第 2、5 节） |

`ai-practices-for-business-application-development.md` 的关键论断："spec-driven 可以作为 AGE 的一个局部 harness……但它不应该替代架构文档，也不应该替代 plan closure、logs、bugs、testing 和 audit 这些各自独立的仓库记忆"。这条论断**同样适用于文章的 superai 链路**——它是一组高质量局部 harness，但不替代 attractor。

## 11. 互相借鉴的精确清单

### 11.1 nop-app-mall 应从文章借鉴（harness 层，不触碰 attractor）

| # | 借鉴项                                | 操作建议                                                                                             | 适用域                |
| - | ------------------------------------- | ---------------------------------------------------------------------------------------------------- | --------------------- |
| 1 | pre-push 硬质量门                     | 把 `mvnw test` / `mvnw compile` 钉进 git pre-push hook；文章 3.4 强调"硬规则不能只靠提示词约束"        | 所有域                |
| 2 | 验收三类证据显式化                    | plan 模板的 closure gate 加"正例/反例/回归"三栏；对应文章 3.6                                         | 所有域                |
| 3 | 结项度量指标                          | 启用 `docs/audits/` 承载（介入次数、一次通过率、回滚次数）；但要警惕 AGE 反对的方向：度量 ≠ 方向判断 | 长期复杂仓库更必要    |
| 4 | feature 分支与 owner doc 显式分离      | 强化约定：process materials（plan/log/discussion）在 feature 分支，attractor（design/architecture/model）只在主干 | 长期复杂仓库更必要    |

### 11.2 文章应从 AGE 借鉴（结构性补强）

| # | 借鉴项                          | 为什么文章需要                                                                                                                                                                            |
| - | ------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1 | 独立的 attractor 对象            | 文章的"长期 wiki"目前是知识集合。要变成 attractor，需要：(a) 明确 precedence 层级；(b) 区分"当前事实"和"应回归结构"；(c) 让 wiki 不被运行时编排临时裁决                                     |
| 2 | 仓库内 Source-of-truth precedence | 文章的"Aone ↔ Multica 双事实源同步"是**跨平台工具间**问题（Aone=阿里工作台，Multica=Agent 运行平台），AGE 的 precedence 解决的是**仓库内**文档冲突，两者不同构。AGE 借鉴点应限制为"仓库内冲突用 precedence 显式裁决，不靠运行时编排临时判断"——跨平台同步文章场景特有，AGE 不直接覆盖 |
| 3 | 轨迹判断（不只是日志）           | 文章的 closeout raw log 是素材；AGE 的轨迹是一等对象，要判断"这 N 次需求做完之后，仓库是不是更接近长期结构"。文章目前缺这一层判断                                                          |
| 4 | 自我验证陷阱的命名               | 文章 3.6 已经有"独立环境验收 + 人工确认发布"两层门，但没有上升到认识论命题。AGE 给它起了名字："同一上下文不能既生成又判断完成"                                                                |
| 5 | 不动点 vs 吸引子的区分           | 文章的"长期 wiki"如果只是"当前快照集合"，那它是 fixed-point 不是 attractor。要变成 attractor，wiki 必须描述"应回归结构"而非"当前事实"                                                         |
| 6 | 把架构判断力沉淀到仓库而非 runtime | 文章作者已展现出 attractor 级判断力（8 阶段闭环本身是架构判断）。可惜产出全部沉淀到 superai-* skill（Agent 能力侧），没有沉淀到 owner doc + precedence（仓库结构侧）。这是 AGE 视角下文章最根本的"可惜" |

### 11.3 不可借鉴的部分（公允性）

文章有些东西 AGE 学不会，因为它们绑在阿里栈上：`superai-aone` 的钉钉群绑定通知、`superai-sls` / `superai-ops` 的运行时日志/监控回读、Multica 沙箱。AGE 模板的"发布观察"阶段缺位（见第 6 节流程覆盖缺口），是真实的覆盖边界，不是 AGE 不想做，而是这些是 Agent 运行栈能力，不属于仓库结构契约。

## 12. 哲学层总结与最终结论

这个对照揭示 AI 工程的两种根本不同的优化目标：

```
Agent-centric（文章、Hermes）：优化 Agent 能力，失败模式 = 单次错误，度量 = 介入次数/一次通过率/skill 复用度
Repository-centric（AGE、nop-app-mall）：优化仓库结构稳定性，失败模式 = 每次都对整体在漂（Plan 76 案例），度量 = owner-doc baseline 是否被保持/precedence 是否被遵守/proof relation 是否成立
```

`age-from-state-engineering-to-trajectory-engineering.md` 把分野讲得最清楚：AGE 要问的不是"这次 AI 有没有把任务做完"，而是"在 AI 连续高频扰动之后，整个仓库是否仍然沿着正确的长期结构收敛"。

**文章完整回答了第一个问题；nop-app-mall 用 Nop 电商业务回答第二个问题。两套系统都不可或缺，但要清楚自己优化的到底是 Agent 还是仓库。** 混淆这两者，就会出现文章 4.2 自身已感觉到的困惑："怎么知道 skill/prompt 迭代是真的改进？"——答案不在 Agent runtime，而在仓库的 attractor 是否被保持。

**最终判断**：nop-app-mall 应把文章的运行时编排能力（pre-push hook、验收三类证据、结项度量）作为 **harness 层**补强，但绝不能把 attractor 让位给 skill——否则就退化为 AGE 反复警告的 `age-skill` 错误抽象（第 5 节论证）。反过来，文章若想突破"每次需求都从零开始"的瓶颈，需要的不只是"补一个 attractor"，而是要把架构判断力的产出**从 runtime 沉淀到仓库结构**（第 11.2 节第 6 项）——这等于改变"Agent 优化对象"这一根本定位。

## 审查记录

- v1（合并初稿）：2026-06-15，融合原 v1（工程对照）+ v2（AGE 理论深化）
- 审查 R1：独立子 agent 审查，判定"需修订"。发现 1 个事实错误（selfcheck 覆盖率 12/12 → 实际 3/12，结论方向反了）、2 处项目结构误述（precedence 全局线性、freshness 字段位置）、1 处节号误引（第十→第九）、1 处被批评对象遗漏（superai-workflow 没进映射表）、1 处最相关文献遗漏（`ai-practices-for-business-application-development.md`）、1 处公允性问题、1 处牵强类比。共识点 11 条已确认核心论点站得住。
- v2（修订稿）：2026-06-15，按 R1 意见修订。修复 R1 全部 🔴 阻塞性问题；处理 Y1–Y8 全部 🟡 改进建议；新增"适用域对照""spec-driven vs attractor-guided"两节；精简哲学总结；skill 映射表补 superai-workflow 与阿里平台绑定项；precedence 描述改为按问题类型；selfcheck 改为 3/12 真实值并明确为薄弱面。
- 审查 R2：独立子 agent 复审，判定"**达成共识**"。R1 全部 🔴 已修复，Y1–Y8 全部 🟡 到位，G1/G4 已处理，G2/G3 部分吸收，章节编号连续无错乱，未引入新准确性问题。残留 3 项可容忍小问题不再要求修订。
- v3（理论强化稿）：2026-06-15，针对"AGE 三大理论支柱未正面讲透"的反馈重写。新增三大理论支柱作为独立章节：
  - **第 3 节《动力系统图像 vs harness 截面评价》（理论支柱 1）**：正面讲清楚"CI 全绿 ≠ 系统健康"的数学本体论差别，用 Plan 76 作为最佳样本，区分截面评价 vs 轨迹评价。
  - **第 5 节《内生 vs 外置：skill 不能承担 attractor 职责》（理论支柱 2）**：展开 `agent-skills-vs-age-practice.md` 第八节四条判断标准、第五节 age-skill 错误抽象论证，应用到 superai-workflow（外置）vs AGENTS.md+owner doc（内生）的对照。
  - **第 8 节《AI 高速扩张如何受控收敛：AGE 五条机制》（理论支柱 3）**：展开自我验证陷阱 + 五条收敛机制（attractor 先于 / 生成验收分离 / harness 五层 / 轨迹一等对象 / 共演化）+ 人机分工限制，逐条对照文章实现状态。
  - 原章节顺延为 4/6/7/9/10/11/12；第 11.2 借鉴清单加第 6 项"把架构判断力沉淀到仓库而非 runtime"（呼应第 8.3 节人机分工）。
- 审查 R3：独立子 agent 审查三个新增理论支柱章节，判定"**达成共识**"。逐项核对 §3/§5/§8 论断在 AGE 文献中的原始出处：动力系统四对象、自我验证陷阱、四条判断标准、age-skill 错误抽象、纵向认知隔离 vs 横向安全隔离——全部逐字或合理转述可核。章节编号 1–12 连续，三个理论支柱之间无矛盾，公允性反而优于 v2（多处承认文章强项）。仅发现 1 处证据精度问题：§3.3 把 Plan 76 写为"100 次合法提交"是捏造数字（源文用变量 N），已修正为"多次合法提交累积"；同时把"最佳样本"改为"典型案例"（轻微拔高修正）。19 项共识点确认理论支柱准确。
- 最终状态：审查通过（v3.1 定稿）