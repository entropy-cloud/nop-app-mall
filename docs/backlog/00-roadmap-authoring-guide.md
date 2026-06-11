# Roadmap Authoring Guide

## Purpose

说明 `docs/backlog/implementation-roadmap.md` 的定位、编写规范和更新规则。

## What a Roadmap Is

Roadmap 是**粗粒度的阶段划分和全局状态索引**。

它的核心用途：

1. **AI 读完 roadmap 后即可知道**哪些能力尚未实现（`todo`）、哪些已有计划（`planned`）、哪些正在实现（`in-progress`）、哪些已经完成（`done`）——**无需重新遍历所有项目文档和代码**。
2. 标注每个阶段的依赖关系、对应 owner doc、可复用的平台能力。
3. 作为选择下一个工作项的入口。

## What a Roadmap Is NOT

- **不是 execution plan。** Roadmap 不包含具体实现步骤、checkbox、closure criteria。
- **不是 design doc。** Roadmap 引用 owner doc，不重复业务规则。
- **不是 backlog。** Roadmap 是 backlog 的编排层，backlog 条目引用 roadmap 阶段。

## Status Tracking

每个阶段必须有 status：

| Status | 含义 | 对应行动 |
|--------|------|----------|
| `todo` | 尚未开始，无对应 plan | 可选为下一个计划目标 |
| `planned` | 已有 execution plan | 等待实施 |
| `done` | 已完成并通过 closure audit | 更新 owner docs 和 logs |

**状态更新时机（由 plan 生命周期驱动）：**
- Plan audit 通过后：`todo` → `planned`
- **Plan closure audit 通过后**：`planned` → `done`。**必须在 closure audit 通过后才能更新**，不能在 plan 实现完成但 audit 未通过时就改为 `done`

## Structure

Roadmap 应包含以下固定段落（按顺序）：

1. **Header** — 最后更新日期、source 文档
2. **Purpose** — 本文档定位（固定文本，引用本 guide）
3. **Status Values** — 状态定义（固定表格）
4. **Nop Platform Reuse** — 已引入/未引入的平台模块，以及各阶段可复用的能力
5. **Current Baseline** — 已有实现和核心缺口的简述
6. **Phases 表** — 全局阶段状态索引表（Phase / Status / Owner Doc / 依赖 / Platform Reuse / Plan 链接）
7. **Phase Details** — 每个阶段的简要交付范围（短列表，不含 checkbox）
8. **Dependency Graph** — Mermaid 流程图
9. **Entity Coverage** — 35 个 ORM 实体到阶段的映射
10. **Cross-Cutting** — 跨阶段关注点
11. **Rule** — 编写和更新规则

## Writing Rules

1. **保持粗粒度。** Phase Details 中的交付范围用短列表，不展开为实现步骤。具体细节由 execution plan 负责。
2. **标注平台复用。** 明确标注哪些能力由 Nop 平台直接提供，避免重复造轮子。
3. **状态必须准确。** 每次创建 plan、开始实施、完成验证后必须更新阶段 status。过时的 status 比没有 status 更有害。
4. **依赖关系在表和图中一致。** 如有冲突，以 Phases 表为准。
5. **Entity Coverage 保持完整。** 35 个 ORM 实体必须全部出现在覆盖表中，包括"平台覆盖"的实体。
6. **不重复 owner doc 内容。** Phase Details 只列交付范围，不重复业务规则。
7. **Nop Platform Reuse 段落标注依赖引入状态。** 区分"已引入"和"未引入"的模块。

## Update Triggers

以下事件必须触发 roadmap 更新。**所有状态变更由 plan 生命周期驱动**（见 `docs/plans/00-plan-authoring-and-execution-guide.md` 的 When Executing / When Closed 段落）：

| 事件 | 更新内容 | 前提条件 |
|------|----------|----------|
| Plan audit 通过 | Phase status `todo` → `planned` | Plan 已通过独立 plan audit |
| **Plan closure audit 通过** | Phase status `planned` → `done` | **必须等 closure audit 通过；audit 未通过则不更新** |
| Plan closure 发现新的平台复用机会 | 更新 Nop Platform Reuse 段落和相关 Phase | Plan 已关闭 |
| 新增或调整设计 owner doc | 检查是否影响 Phase Details | — |
| ORM 模型实体变更 | 更新 Entity Coverage | — |

## Anti-Patterns

- 把 roadmap 写成详细 implementation plan（checkbox、closure criteria、audit evidence 应放在 plan 文件中）
- 在 roadmap 中重复 owner doc 的业务规则
- 让 status 过时（完成后不更新）
- **在 closure audit 通过前就更新 roadmap 为 `done`**（过早更新制造虚假完成信号）
- 不标注平台已有能力，导致重复造轮子
