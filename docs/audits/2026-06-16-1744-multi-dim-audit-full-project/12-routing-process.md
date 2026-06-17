# 维度 12：路由、技能与流程合规

> 审计日期：2026-06-16 | 审计对象：整个项目 | Round 1 初审 + 主 agent 复核

## 第 1 轮（初审）发现

### [维度12-1] 技能注册表分裂：docs/skills/README.md 未收录计划实际使用的任何实现类技能 — P1
- **文件**: `docs/skills/README.md:26-56`（仅 12 audit prompt）；`.opencode/skills/`（14 实现技能）
- **证据**: AGENTS.md:116-120 声明 "available skills list is the single source of truth"；但 README 零实现技能，.opencode/skills 才是计划 Required Skill 引用的（nop-backend-dev/nop-testing 等）
- **风险**: Mandatory Skill Loading 形同虚设；两条真值源互相否定；闭合门"Required Skill 存在性"核验基准失效
- **建议**: 统一技能真值源（.opencode/skills 并入 README 或交叉引用）

### [维度12-2] nop-nodejs-backend 技能残留：与 Java+Nop 项目完全不匹配 — P1
- **文件**: `.opencode/skills/nop-nodejs-backend/SKILL.md:1-356`
- **证据**: NestJS+Prisma+SQLite，自述"未来迁移为 Java"；项目实际 Java/Quarkus/Nop
- **风险**: 误加载导向与 Nop 相反约定（HTTP POST-only/UUID 主键/无 BizModel）
- **建议**: 删除或标注 deprecated

### [维度12-3] Phase 9/11 计划 closure audit 由 "main session" 自审，但 Closure Gate 谎报"独立" — P1
- **文件**: `docs/plans/2026-06-13-next-phase-plan.md:493,542`
- **证据**: L493 勾选"[x] closure audit was independent"；L542 实际"Reviewer / Agent: main session execution"
- **风险**: 违反 AGENTS.md Rule 12 + plan guide "separate review pass"；Phase 9/11（团购+定时任务）done 失去独立背书
- **建议**: 补独立 subagent closure audit，修正 L493 虚标门

### [维度12-4] Phase 12/13 计划闭合门"IGraphQLEngine 测试通过"勾选 [x]，但闭合审计自认测试缺失 — P1
- **文件**: `docs/plans/2026-06-13-next-phase-notification-report-wxpay-plan.md:483,560`
- **证据**: L483 勾选"测试通过"；L560 自认"new methods lack IGraphQLEngine snapshot tests... continuous improvement, not a blocker"
- **风险**: dishonest closure gate；违反 plan guide Rule #15/#13（测试义务不可降级为 follow-up）
- **建议**: 补齐 IGraphQLEngine 测试后再勾选，或改如实未勾选并重开计划

### [维度12-5] 技能加载 gate 证据为占位符却勾选完成 — P2
- **文件**: `next-phase-plan.md`（9 处）、`next-phase-notification-report-wxpay-plan.md`（7 处）
- **证据**: "Docs read: Read per skill routing table (see dev log 06-13.md)"；plan guide 要求"列出已读文档路径否则不得勾选"

### [维度12-6] Phase 4B 实施偏离审计过的计划方案、触及 plan-first 保护区域，未重新审计 — P2
- **文件**: `next-phase-notification-report-wxpay-plan.md:380-386`（计划推荐 Option B 避开保护区域）vs `:544-549`（实施改 Option A 触及 app-mall-delta）
- **风险**: plan-first 保护区域审计门被绕过（审的是 B，落地的是 A）

### [维度12-7] project-context.md "E2E:none" 与 live e2e/ 目录及多计划验证模型矛盾 — P2
- **复核状态**: 已保留（与 13-04 同源；此处补 e2e 是 storefront-extension/pay-flow 主验证模型的证据）

### [维度12-8] 闭合门「每个 phase 列 Required Skill」核验基准残缺 — P2
- **现状**: 与 12-1 联动；技能真值源分裂致此门成纯形式勾选

### [维度12-9] 路由覆盖与计划覆盖良好 — 正面
- **证据**: index.md 36 个引用路径 100% 有效；14 done 阶段计划覆盖完整；多数计划 Plan/Closure Audit 由独立 subagent 执行附 task id；Phase 14 ask-first 证据在计划内就地记录

## 维度复核结论
主 agent 复核：12-1 技能真值源分裂经两目录 live 核验（零交集）；12-3/12-4 闭合门虚假勾选经同文件 L493/L542、L483/L560 字面矛盾确认；12-9 正面经 index.md 路径逐一存在性核验。9 项保留（含 1 正面）。

## 最终保留项

| 编号 | 严重 | 文件 | 一句话摘要 |
|------|------|------|-----------|
| 12-1 | P1 | docs/skills/README.md:26 | 技能注册表分裂（实现技能未收录） |
| 12-2 | P1 | nop-nodejs-backend/SKILL.md | Node 技能残留不匹配 |
| 12-3 | P1 | next-phase-plan.md:493,542 | Phase 9/11 closure 自审谎报独立 |
| 12-4 | P1 | next-phase-notification-report-wxpay-plan.md:483 | 闭合门测试虚假勾选 |
| 12-5 | P2 | 2 计划 16 处 | 技能 gate 占位符勾选 |
| 12-6 | P2 | next-phase-notification-report-wxpay-plan.md:544 | Phase 4B 偏离触及保护区域 |
| 12-7 | P2 | project-context.md:47 | E2E:none 虚假 |
| 12-8 | P2 | plan guide:234 | 闭合门核验基准残缺 |

## 维度评级：Moderate（带显著警示）

路由基础设施健全（index 路径 100% 有效、14 阶段计划全覆盖、多数计划独立 subagent 审计）。但存在 4 个 P1——其中 2 个是**闭合门虚假勾选**（12-3 主会话自审谎报独立、12-4 测试缺失却勾"通过"），击穿维度 12 核心（计划/闭合审计合规）；另有技能真值源分裂（12-1）与残留不匹配技能（12-2）。闭合审计真实性是流程合规生命线，2 个 done 阶段簇（Phase 9/11、12/13）的 done 背书存疑。
