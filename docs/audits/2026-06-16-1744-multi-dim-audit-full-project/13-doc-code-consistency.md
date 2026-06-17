# 维度 13：文档-代码一致性与维护

> 审计日期：2026-06-16 | 审计对象：整个项目 | Round 1 初审 + 主 agent 复核

## 第 1 轮（初审）发现（基线确认项 + 新发现）

### [维度13-01] module-boundaries.md "No E2E test framework configured" 虚假 — P1（新发现）
- **文件**: `docs/architecture/module-boundaries.md:84-85`
- **证据**: e2e/package.json 声明 playwright 1.60；e2e/tests 含 3 spec；日志 06-16:13 "38 passed"
- **建议**: Test Ownership 段新增 e2e/ 条目

### [维度13-02] module-boundaries.md 集成测试位置"app-mall-app/src/test/"不存在 — P1
- **文件**: `module-boundaries.md:84`
- **现状**: app-mall-app 无 src/test；业务/集成测试在 app-mall-service/src/test（29 类）

### [维度13-03] module-boundaries.md "31 个 ORM 索引已同步到 DDL" 完全虚假 — P0
- **文件**: `module-boundaries.md:91-96`
- **证据**: ORM 31 索引；三 DDL 各 531/1215 行但 0 CREATE INDEX；06-15 日志也声称追加但 live 不含
- **严重程度**: P0（文档误导致生产性能事故）
- **复核状态**: 已保留（主 agent 亲自验证）

### [维度13-04] project-context.md "E2E / integration tests: none" 虚假 — P1
- **文件**: `project-context.md:47`
- **现状**: e2e 套件成熟（38 用例），文档却标 none

### [维度13-05] codebase-map.md 完全缺失 e2e/ 入口 — P1（新发现）
- **文件**: `codebase-map.md:11-23`

### [维度13-06] codebase-map.md 全表 "Last Verified: 2026-06-02" 严重过期 — P1
- **现状**: 14 天未更新；期间 storefront 25 页、e2e、微信支付全部落地

### [维度13-07] codebase-map.md Entry Points 表缺失 app-mall-wx/delta/meta 模块 — P1（新发现）
- **现状**: Entry Points 只列 6 模块，漏 wx/delta/meta；Change Routes 却引用 app-mall-wx，两表不自洽

### [维度13-08] roadmap Current Baseline "4 个测试类"严重低估 — P1
- **文件**: `implementation-roadmap.md:70`
- **现状**: 实际 31 Java 测试类 + e2e 38 用例

### [维度13-09] roadmap "核心缺口"与 Phase Status 全 done 直接矛盾 — P1

### [维度13-10] roadmap "extAction1 查询"引用已删除的 delta action — P1（新发现）
- **文件**: `implementation-roadmap.md:68`
- **现状**: live NopAuthUser.xbiz 为空（06-15 AR-23 已移除全部 extAction）

### [维度13-11] roadmap Entity Coverage 表缺 LitemallResetCode + "35 实体"计数过期 — P1（新发现）
- **现状**: live 32 活跃实体；表内缺 ResetCode

### [维度13-12] delta 含未文档化 picUrl 字段（displayName="测试图片"） — P1
- **文件**: `model/nop-auth-delta.orm.xml:39-40` vs `user-and-address.md:38-43`

### [维度13-13] order-and-cart.md 状态叙述用"已申请退款"但表/字典用"退款中"(202) — P2（新发现）

### [维度13-14] project-context.md Unit tests 行未反映 e2e 与规模 — P2

## 维度复核结论
主 agent 复核：13-03（P0）亲自验证三 DDL 0 索引；13-01/02/04/06/08/09/12 七项基线已确认项全部复核实锤；13-05/07/10/11/13 五项新发现经 live 核验保留。14 项全部保留。

## 最终保留项

| 编号 | 严重 | 文件 | 一句话摘要 |
|------|------|------|-----------|
| 13-03 | P0 | module-boundaries.md:91-96 | "31 索引已同步 DDL"虚假 |
| 13-01 | P1 | module-boundaries.md:84-85 | "No E2E framework"虚假 |
| 13-02 | P1 | module-boundaries.md:84 | 集成测试位置不存在 |
| 13-04 | P1 | project-context.md:47 | "E2E:none"虚假 |
| 13-05 | P1 | codebase-map.md:11-23 | 缺 e2e/ 入口 |
| 13-06 | P1 | codebase-map.md 全表 | Last Verified 过期 14 天 |
| 13-07 | P1 | codebase-map.md | 缺 wx/delta/meta 模块 |
| 13-08 | P1 | roadmap:70 | "4 测试类"低估 |
| 13-09 | P1 | roadmap:18-33,73 | 核心缺口 vs 全 done 矛盾 |
| 13-10 | P1 | roadmap:68 | extAction1 僵尸引用 |
| 13-11 | P1 | roadmap:459 | 缺 ResetCode+计数过期 |
| 13-12 | P1 | nop-auth-delta.orm.xml:39 | picUrl 测试字段污染 |
| 13-13 | P2 | order-and-cart.md:79,89 | 术语漂移 |
| 13-14 | P2 | project-context.md:46 | Unit 行未反映规模 |

## 维度评级：Poor

P0 级资损风险（13-03 文档误导致生产无索引慢查询）；多重 P1 集中在最高频被读的 context 文档（project-context/codebase-map/module-boundaries）；roadmap 核心状态索引自相矛盾；维护纪律未落地（06-15/16 日志详尽但 owner doc 系统性未更新）。design 层一致性尚可（仅 1 个 P2）。
