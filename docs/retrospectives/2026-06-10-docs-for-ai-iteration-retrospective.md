# docs-for-ai 与项目 Guide 迭代回顾

> Date: 2026-06-10
> Trigger: 执行 order-full-lifecycle-plan 时暴露一系列问题，回溯发现近一周的文档优化都在解决同一类根因

## 1. 背景

2026-06-02 ~ 2026-06-10 期间，在构建 nop-app-mall 商城应用和优化 nop-entropy 平台的 `docs-for-ai/` 文档体系过程中，出现了大量"读了文档但写代码时违反规则"的问题。每次发现问题后都针对性地修补了文档，但同类问题持续以不同形式出现。

本文档梳理这段期间所有文档修改，提取共性根因，评估已有修复是否充分，指出仍存在的结构性缺口。

## 2. 变更清单

### 2.1 nop-entropy docs-for-ai（25 个文件，+1253/-95 行）

| 日期 | 提交 | 修改内容 | 触发原因 |
|------|------|---------|---------|
| 06-02 | c8d0baeb2 | 补充外部应用项目开发规则 | 项目文档初始化 |
| 06-06 | 572c3f4dc | INDEX 路由更新 | 计划状态管理 |
| 06-07 | db1e21a4b | 强化 XDef/XDSL 规则，要求读 XDSL 必须先读 xdef | AI 跳过 XDef 直接写 XDSL |
| 06-07 | 9b36d9ce2 | 增强 XDef/XDSL 合并算法和 Delta 调试文档 | Delta 调试困难 |
| 06-08 | 75a215811 | BizModel ORM 访问规范和 ErrorCode 消息语言规则 | AI 绕过 I*Biz 直接用 dao() |
| 06-08 | 6288e1d0a | IoHelper.safeClose 资源关闭规范 | AI 手写 try-catch close |
| 06-09 | 031805063 | INDEX 重构为四阶段路由 + 模型设计/权限入口 | AI 不知道该读哪些文档 |
| 06-09 | de385e776 | newEntity() 规范，禁止直接 new 实体 | AI 直接 new XxxEntity() |
| 06-09 | e2e514489 | 跨实体创建通过 I*Biz.newEntity() | AI 跨实体时直接 new |
| 06-09 | 732c90677 | BizModel public 方法必须同步到 I*Biz 接口 | AI 写了方法不补接口 |
| 06-09 | ced9a52ee | 常见坑升级为写后自检清单 | AI 读了规则但不逐条校验 |
| 06-09 | 71c93cfc1 | 自检清单补充禁止修改 _ 前缀文件 | AI 编辑了 _gen 文件 |
| 06-09 | a784c24bd | Entity 可用 BeanContainer 获取 I*Biz 做只读查询 | 实体层跨模块访问规范 |
| 06-09 | 3072a9b19 | 实体应通过 requireBiz 获取 I*Biz | 修正上一条的访问方式 |
| 06-09 | 942417072 | findList/findPage 三参数方法说明 | AI 用了错误参数签名 |
| 06-09 | 3ee9bc000 | doFindList/doFindPage 签名修正，IBiz 接口定位 | AI 调用参数不对 |
| 06-09 | 3911a08be | 精简 ICrudBiz 注释、ai-defaults 反模式表 | ICrudBiz 注释冗余 |
| 06-10 | 513ffcab9 | 拆分测试为独立开发阶段入口，新增必读索引 | AI 不知道 BizModel 测试要用 IGraphQLEngine |
| 06-10 | 19af9faad | **接口注入与转型规则、二次引用追踪规则** | AI 转型注入的接口 + 不追踪文档内部引用 |

### 2.2 nop-app-mall 项目 docs（61 个文件，+4755/-341 行）

| 日期 | 范围 | 修改内容 | 触发原因 |
|------|------|---------|---------|
| 06-02 | 全局 | 应用 AGE 模板建立完整文档结构 | 项目初始化 |
| 06-02 | 全局 | ORM 模型从 xlsx 迁移到 xml | 建立源文件 truth |
| 06-02 | 全局 | 强化 log 强制记录规则 | AI 不写 dev log |
| 06-02 | 设计 | 对齐商业基线与文档治理 | 设计文档与基线不一致 |
| 06-03 | 设计 | 统一中文表述、补充领域词汇表 | 中英混用导致歧义 |
| 06-03 | 设计 | 深化营销/订单/系统配置设计文档 | 设计文档不够深入无法指导实现 |
| 06-06 | 全局 | Nop 平台编码规范和计划模板强制 Reference Docs | AI 不读 Reference Docs |
| 06-08 | 全局 | 补充编码规范 | 实现时发现规范缺失 |
| 06-09 | 计划 | 多轮强化：审计规则、必读阅读门控、路线图生命周期 | AI 写计划不规范 |
| 06-10 | 计划 | **测试验证规则和预读索引强化** | AI 不用 IGraphQLEngine 测试 |

## 3. 共性根因分析

所有修改可归为 **三条根因**：

### 根因 A：读文档但不追踪内部引用

`ai-defaults.md` 说"使用前先阅读 ICrudBiz"，但 AI 读完 service-layer.md 后没有继续去读 ICrudBiz。导致不知道接口提供了哪些方法，于是：
- 转型注入的接口到实现类（不知道 updateEntity 在 ICrudBiz 上）
- 绕过 CrudBizModel 的安全 API 直接用 dao()（不知道 findList 在 CrudBizModel 上）
- 不在 I*Biz 接口上补方法（不知道 ICrudBiz 继承链）

**修复**：在 `ai-defaults.md` "阅读即理解"中增加第 3 条"追踪文档内部的二次引用"。

### 根因 B：实现优先，接口/契约事后补

AI 的实际执行顺序是：写 BizModel 实现 → 写完再补 I*Biz 接口 → 写完再补测试。正确顺序应是：接口声明 → 实现 → 测试。这导致：
- 方法不在接口上（findCheckedCart）
- 需要跨模块调用时转型而非补接口
- 测试用实体级纯逻辑而非 IGraphQLEngine

**修复**：plan guide 增加"接口优先是强制顺序"规则和"转型注入接口是禁止操作"规则。

### 根因 C：自检清单当阅读材料，不当执行步骤

`write-bizmodel-method.md` 有写后自检清单，但 AI 扫过清单后不逐条对照代码检查。所有反模式（转型、dao()、private @Inject、不补接口）都在清单中有对应条目。

**修复**：06-09 已将常见坑升级为自检清单并强调"逐条校验"。06-10 在 plan guide 的 When Executing #9 中强化了"不能只因为签名存在就标记完成"。但这是一个执行纪律问题，单靠文档规则可能无法完全解决。

## 4. 仍存在的结构性缺口

| 缺口 | 说明 | 状态 |
|------|------|------|
| 自检清单的强制执行机制 | 规则存在但 AI 可以跳过，没有硬性拦截 | open — 可能需要工具化 |
| 批量写代码时的中间检查点 | AI 一次写多个方法，不逐方法自检 | open — plan guide 可加"每个方法写完立即自检" |
| order-full-lifecycle-plan 中的 API 测试 | 4 个 Phase 的 IGraphQLEngine 测试仍为 unchecked | open — 需要补充实现 |
| dao() / 转型问题的代码修复 | CartBizModel、OrderBizModel、AftersaleBizModel 中仍有 dao() 和转型调用 | open — 需要修正 |

## 5. 可提取的通用教训

1. **二次引用等效于 Required Pre-Reading** — 文档内部"先读 X"的指令不是背景描述，是强制门控。
2. **接口注入 → 接口调用** — 任何通过接口注入的依赖，转型都是禁止操作。接口上没有需要的方法 → 补接口，不是绕过接口。
3. **最高抽象层级优先** — CrudBizModel 方法 > dao() > 直接 SQL。先查基类/接口提供了什么，再决定是否需要降级。
4. **BizModel 方法测试必须走 IGraphQLEngine** — @BizMutation/@BizQuery 是 GraphQL API，测试入口是 IGraphQLEngine 不是实体类。
5. **开发顺序是接口声明 → 实现 → 测试** — 不是实现 → 补接口 → 跳过 API 测试。
