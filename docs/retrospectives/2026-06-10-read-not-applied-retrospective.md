# 2026-06-10 已读文档未转化为执行纪律

## 事件

实现 `LitemallAddressBizModel.saveAddress()` 时，参数使用 `Object data` + 强转 `Map<String, Object>`。

`service-layer.md` 第56-57行明确写了：

> - 1–5 个参数使用 `@Name`
> - 超过 5 个参数使用 `@RequestBean` + `@DataBean` DTO

Agent 在 pre-flight 阶段读了此文档并标记 checkbox `[x]`，但编码时未对照规则校验。闭包审计才发现。

**注意：** 标准 CRUD 继承 CrudBizModel 的 `save(Map, context)` 用 Map 是正确的平台模式。问题仅在自定义业务方法用 `Object`/`Map` 代替 DTO。

## 根因

**"已读"不等于"已校验"。** 文档阅读和代码编写之间存在执行间隙：

1. Pre-flight checkbox 只证明文档被打开过，不证明规则被应用
2. AGENTS.md 虽然写了 "After writing each method, self-check against the anti-patterns table"，但没有提供具体的逐条校验清单
3. Agent 倾向于批量写完全部方法后再做整体检查，而非逐方法自检
4. 反模式表是散文描述，不是可勾选的 checklist

## 改进

创建了 `docs/skills/bizmodel-method-selfcheck-prompt.md`：

- 19 条逐条 checklist，覆盖参数类型、接口声明、实体操作、异常处理、事务注入、平台工具类
- 强制触发点：每写完**一个**方法立即校验，不是批量写完后统一校验
- 与已有流程的关系明确：pre-flight 阅读在前，本 skill 是编码时的实时防线，closure audit 是后置验证

## 影响范围

- 本次事件：`saveAddress` 参数类型错误，已修复
- 潜在影响：任何 BizModel 方法都可能因相同原因遗漏反模式校验
- 后续：Plan 模板中应在 execution item 里明确引用 `Skill: bizmodel-method-selfcheck-prompt`
