# 开发任务提示词模板

## 使用说明

在给 AI 分配开发任务时，使用以下模板可以确保 AI：
1. 先理解文档和现有模式
2. 按正确的模式编写代码
3. 对照规则表验证决策

---

## 推荐提示词模板

```
完成 @docs\litemall-requirements.md 的团购部分实现，按以下步骤严格执行：

## 第一步：读取文档
仔细阅读 c:/can/nop/nop-entropy/docs-for-ai/ 下的核心文档：
- INDEX.md（必读：代码放置判断规则）
- 03-development-guide/ddd-in-nop.md（必读：Entity vs BizModel 职责划分）
- 03-development-guide/bizmodel-guide.md（必读：BizModel 编写规范）
- 03-development-guide/service-layer.md（必读：CrudBizModel 使用）
- 03-development-guide/crud-development.md（必读：CRUD 扩展点）

## 第二步：观察现有代码
检查项目中已有的实现模式，特别是：
- app-mall-service/src/main/java/app/mall/service/entity/ 下的 BizModel
- app-mall-dao/src/main/java/app/mall/dao/entity/ 下的 Entity
- app-mall-delta/ 下的扩展模式

## 第三步：对照规则表验证
每个决策前必须对照 INDEX.md 中的规则：

| 逻辑类型 | 放置位置 | 示例 |
|---------|---------|------|
| 纯函数/状态查询 | Entity | canBeCancelled(), isExpired() |
| 简单修改操作 | BizModel | setStatus(), updateEntity() |
| 跨聚合操作 | BizModel | 提交订单涉及订单+库存+优惠券 |
| 复杂流程 | Processor | 订单提交流程 |

## 第四步：严格执行规范
1. **禁止直接调用 dao() 方法**：使用 requireEntity(), save(), update() 等 CrudBizModel 方法
2. **禁止在 Entity 中使用 enum**：使用 String + Constants（从 orm.xml dict 生成）
3. **禁止在 Entity 中写修改逻辑**：Entity 只放只读帮助函数
4. **禁止手动开启事务**：@BizMutation 自动开启事务
5. **禁止返回 Map 类型**：定义 @DataBean DTO

## 第五步：执行并验证
实现后检查：
- lsp_diagnostics 无错误
- 代码符合现有模式
- 所有业务规则已实现

整个过程不要询问用户，遇到问题根据文档规则自动修正。如果文档中没有明确规则，按照现有代码模式实现。
```

---

## 核心规则速查表

### 代码放置判断规则

| 逻辑类型 | 放置位置 | 原因 | 示例 |
|---------|---------|------|------|
| 纯函数，读取字段/关联 | **Entity** | 稳定的领域事实 | calculateTotal() |
| 状态查询 (canXxx, isXxx) | **Entity** | 稳定的领域事实 | canBeCancelled() |
| 简单修改操作 | **BizModel** | 可定制的业务行为 | updateStatus() |
| 跨聚合操作 | **BizModel** | 需要协调多个实体 | submitOrder() |
| 调用外部服务 | **BizModel** | 易变的集成逻辑 | processPayment() |
| 复用性高的业务规则 | **Processor** | 多处复用 | InventoryProcessor |
| 复杂流程/多步骤 | **Processor** | 降低 BizModel 复杂度 | OrderSubmitProcessor |

### 常量定义规则

| 类型 | 位置 | 方式 |
|------|------|---------|
| 数据库字段枚举 | orm.xml dict | codegen 自动生成 Constants |
| 业务规则常量 | 手动定义常量类 | 如免运费金额、超时时间 |

### 数据访问规则

| 场景 | ✅ 推荐方法 | ❌ 禁止方法 |
|------|------------|------------|
| 获取实体 | requireEntity(id, action, context) | dao().getEntityById(id) |
| 查询列表 | doFindList(query, ...) | dao().findListByQuery(query) |
| 保存数据 | save(data, context) | dao().saveEntity(entity) |
| 更新数据 | update(data, context) 或 updateEntity(entity, context) | dao().updateEntity(entity) |
| 删除数据 | delete(id, context) | dao().deleteEntity(entity) |

### 事务管理规则

| 注解 | 事务行为 |
|------|---------|
| @BizQuery | 无事务（只读操作） |
| @BizMutation | **自动开启事务**，无需 @Transactional |

---

## 常见错误（必须避免）

### 1. 不观察现有代码就直接开始写
**错误**：凭想象写代码，与项目风格不一致
**正确**：先检查项目中其他模块的实现方式，按同样模式编写

### 2. 把只读方法放在 BizModel 上
**错误**：在 BizModel 中写 isExpired(), canBeCancelled()
**正确**：这些方法应该放在 Entity 上

```java
// ✅ Entity 上
public class LitemallOrder extends _LitemallOrder {
    public boolean canBeCancelled() {
        return ORDER_STATUS_101 == this.orderStatus;
    }
}

// ✅ BizModel 中使用
@BizMutation
public LitemallOrder cancel(@Name("orderId") String orderId, IServiceContext context) {
    LitemallOrder order = requireEntity(orderId, "update", context);
    if (!order.canBeCancelled()) {
        throw new NopException(ERR_ORDER_CANNOT_CANCEL);
    }
    order.setOrderStatus(ORDER_STATUS_102);
    return update(order, context);
}
```

### 3. 手动定义数据库字段常量
**错误**：在代码中手动定义 `public static final int STATUS_PENDING = 101;`
**正确**：在 orm.xml 的 dict 中定义，用 codegen 生成

### 4. 直接调用 dao() 方法
**错误**：`dao().getEntityById(id)` 绕过数据权限检查
**正确**：`requireEntity(id, "update", context)` 自动执行数据权限检查

### 5. 实体字段使用 enum
**错误**：`private OrderStatus status;` 使用 Java enum
**正确**：`private Short status;` 使用基础类型 + Constants

```java
// ❌ 错误
public enum OrderStatus { PENDING(101), PAID(201) }

// ✅ 正确：codegen 生成的 Constants
public interface _AppMallDaoConstants {
    short ORDER_STATUS_101 = 101;  // 待付款
    short ORDER_STATUS_201 = 201;  // 已付款
}
```

### 6. 在 BizMutation 中手动开启事务
**错误**：`@BizMutation @Transactional` 双重注解
**正确**：只用 `@BizMutation`（已自动事务）

---

## 正确的开发流程

```
1. 读 INDEX.md 快速了解结构
2. 找到相关详细文档（ddd-in-nop.md, bizmodel-guide.md 等）
3. 检查项目中现有代码的模式
4. 按现有模式编写新代码
5. 对照规则表验证
6. 运行 lsp_diagnostics 检查
```

---

## 项目结构参考

```
app-mall/
├── app-mall-dao/           # 数据访问层
│   └── src/main/java/app/mall/
│       ├── biz/            # Biz 接口 (ILitemallOrderBiz)
│       ├── dao/entity/     # 实体类 (LitemallOrder)
│       └── dao/mapper/     # Mapper 接口 (自定义SQL)
│
├── app-mall-service/       # 业务逻辑层
│   └── src/main/java/app/mall/service/
│       └── entity/         # BizModel (LitemallOrderBizModel)
│
├── app-mall-delta/         # 扩展模块（可定制）
│   └── src/main/java/app/mall/delta/
│       └── biz/            # 扩展 BizModel
│
├── app-mall-meta/          # 元数据
│   └── src/main/resources/_vfs/app/mall/model/
│       └── LitemallOrder/  # xmeta, xbiz 文件
│
└── model/
    └── app-mall.orm.xlsx   # ORM 模型定义（数据结构源）
```
