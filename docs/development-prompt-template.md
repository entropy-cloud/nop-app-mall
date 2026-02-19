# 开发任务提示词模板

> **详细规范请查阅**: `c:/can/nop/nop-entropy/docs-for-ai/` 目录

## 推荐提示词模板

```
完成 @docs\litemall-requirements.md 的 [功能名称] 实现，按以下步骤严格执行：

## 第一步：读取核心文档
阅读 c:/can/nop/nop-entropy/docs-for-ai/INDEX.md，重点关注：
- 🚀 AI 驱动的完整开发流程
- BizModel 方法必须规则
- 代码放置判断规则

## 第二步：观察现有代码
检查项目中的实现模式：
- app-mall-service/src/main/java/app/mall/service/entity/ 下的 BizModel
- app-mall-dao/src/main/java/app/mall/dao/entity/ 下的 Entity
- app-mall-dao/src/main/java/app/mall/biz/ 下的 IXXBiz 接口

## 第三步：执行并验证
- lsp_diagnostics 无错误
- 代码符合现有模式

遇到问题根据 docs-for-ai 文档规则自动修正，不要询问用户。
```

---

## 核心规范速查（完整版在 docs-for-ai）

### BizModel 方法必须规则

| 规则 | 说明 |
|------|------|
| 非 private 方法必须有注解 | `@BizQuery` / `@BizMutation` / `@BizAction` |
| 最后一个参数必须是 `IServiceContext` | 所有对外暴露的业务方法 |
| 所有业务参数必须有 `@Name` 注解 | 除 `IServiceContext`、`FieldSelectionBean` 外 |

### 代码放置规则

| 逻辑类型 | 放置位置 |
|---------|---------|
| 状态查询 (canXxx, isXxx) | Entity |
| 修改操作、跨聚合操作 | BizModel |
| 复杂流程 (>50行) | Processor |

### 数据访问

| ✅ 推荐 | ❌ 禁止 |
|--------|--------|
| `requireEntity()`, `save()`, `update()` | `dao().getEntityById()` |

---

## 项目结构

```
app-mall/
├── app-mall-dao/src/main/java/app/mall/
│   ├── biz/            # IXXBiz 接口
│   └── dao/entity/     # Entity 实体类
├── app-mall-service/src/main/java/app/mall/service/
│   └── entity/         # BizModel
├── app-mall-delta/     # Delta 扩展
└── model/
    └── app-mall.orm.xml   # ORM 模型
```
