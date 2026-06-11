---
name: nop-backend-dev
description: Nop平台后端服务开发（BizModel / IBiz / Processor / ErrorCode）。涵盖实体服务创建、自定义动作、跨实体调用、错误处理、事务边界。触发词：后端开发、BizModel、IBiz、写方法、加接口、错误码、跨实体。
---

# Nop 后端服务开发

## 什么时候用我

| 场景 | 触发关键词 |
|------|-----------|
| 创建/修改 BizModel 方法 | "写方法"、"加接口"、"BizModel"、"加动作" |
| 定义 IBiz 接口 | "接口声明"、"IBiz"、"加方法声明" |
| 跨实体调用 | "调用其他实体"、"注入IBiz"、"跨模块" |
| 错误处理 | "错误码"、"ErrorCode"、"NopException"、"抛异常" |
| 事务/并发 | "事务边界"、"乐观锁"、"并发控制" |
| Delta 定制后端 | "覆盖平台BizModel"、"Delta定制" |

---

## 必读文档路径

以下路径相对于 `{DOCS-FOR-AI}` 目录。`{DOCS-FOR-AI}` 的实际位置由当前项目 `AGENTS.md` 的 "Nop Platform Documentation" 部分指定（例如 `../nop-entropy/docs-for-ai/`）。

### 全局必读（写任何后端代码前全部读完）

| 文档 | 为什么必读 | 不读会怎样 |
|------|-----------|-----------|
| `05-examples/README.md` + `05-examples/ibiz-and-bizmodel.java` | Entity/IBiz/BizModel/DTO/ErrorCode 精简代码骨架 | 不知道各类文件怎么写 |
| `02-core-guides/service-layer.md` | IBiz接口契约、注解规则、开发顺序、跨实体访问、safe API、反模式表 | 接口方法漏注解→代理无法路由；@Inject写private；用dao()而非requireEntity |
| `02-core-guides/error-handling.md` | NopException + ErrorCode 规则 | throws RuntimeException；错误消息用中文；丢失异常链 |
| `04-reference/safe-api-reference.md` | requireEntity/doFindList/saveEntity/newEntity 速查 | 绕过CrudBizModel管道直接操作dao() |

### 按场景选读

| 场景 | 文档 |
|------|------|
| 新建实体+代码生成 | `03-runbooks/create-new-entity.md`, `02-core-guides/model-first-development.md` |
| 写BizModel方法（含强制顺序） | `03-runbooks/write-bizmodel-method.md` |
| 扩展CRUD钩子 | `03-runbooks/extend-crud-with-hooks.md` |
| 多步编排流程 | `03-runbooks/implement-complex-business-flow.md` |
| 何时拆Processor | `03-runbooks/choose-entity-bizmodel-processor.md` |
| 自定义QueryBean查询 | `03-runbooks/custom-query-with-querybean.md` |
| 跨模块IBiz接口 | `03-runbooks/add-cross-module-biz-interface.md` |
| Request/Response DTO | `03-runbooks/create-request-response-dto.md` |
| Delta定制原理 | `02-core-guides/delta-customization.md` |
| 认证与权限 | `02-core-guides/auth-and-permissions.md` |
| 事务与并发 | `02-core-guides/concurrency-and-transactions.md`, `03-runbooks/transaction-boundaries.md` |
| 错误码runbook | `03-runbooks/error-codes-and-nop-exception.md` |
| 功能实现总流程 | `03-runbooks/feature-implementation-checklist.md` |

---

## 开发流程

### 强制实现顺序

1. **先 IBiz 接口** → 在 `I*Biz` 接口上声明方法（含注解 + @Name）
2. **再 BizModel 实现** → 实现类中 `@Override` 实现
3. **自检** → 每写完/修改一个方法后，立即用 `{DOCS-FOR-AI}/04-reference/bizmodel-method-selfcheck.md` 逐项校验
4. **测试** → 用 IGraphQLEngine 测试（见 nop-testing skill）

### 自检纪律（强制）

**每增加或修改一个 public 方法后，必须立即执行 `{DOCS-FOR-AI}/04-reference/bizmodel-method-selfcheck.md` 的 19 项检查。**

- 不能批量写完所有方法后统一自检——那样和闭包审计没有区别
- 任何一条不通过必须立即修复后才能继续写下一个方法
- 自检是实时执行纪律，不是产出物

### 项目文件位置

| 文件类型 | 位置 |
|---------|------|
| IBiz 接口 | `app-mall-dao/src/main/java/app/mall/biz/I{Entity}Biz.java` |
| BizModel 实现 | `app-mall-service/src/main/java/app/mall/service/entity/{Entity}BizModel.java` |
| 错误码 | `app-mall-service/src/main/java/app/mall/service/AppMallErrors.java` |
| 常量 | `app-mall-service/src/main/java/app/mall/service/AppMallConstants.java` |
| DTO | `app-mall-dao/src/main/java/app/mall/dao/dto/` 或 `app-mall-service/` |
| 外部RPC接口 | `app-mall-api/src/main/java/app/mall/` |

---

## 代码模式速查

### 1. BizModel 最小结构

```java
@BizModel("LitemallOrder")
public class LitemallOrderBizModel extends CrudBizModel<LitemallOrder>
    implements ILitemallOrderBiz {
    public LitemallOrderBizModel() {
        setEntityName(LitemallOrder.class.getName());
    }
}
```

### 2. IBiz 接口声明

```java
public interface ILitemallOrderBiz extends ICrudBiz<LitemallOrder> {
    @BizMutation
    LitemallOrder createOrder(@Name("addressId") String addressId,
                              @Name("message") String message,
                              IServiceContext context);

    @BizQuery
    List<LitemallOrder> myOrders(@Name("orderStatus") Short orderStatus,
                                 IServiceContext context);
}
```

### 3. 外部 RPC Service 接口（api 模块）

`*-api/` 模块中的 Service 接口用于外部系统 RPC 调用。直接使用业务 DTO 类型，不包装 `ApiRequest`/`ApiResponse`：

```java
@BizModel("PayService")
public interface PayService {
    @BizMutation("refund")
    PayRefundResponseBean refund(PayRefundRequestBean req);
}
```

异步调用等高级特性见 `{DOCS-FOR-AI}/04-reference/async-service-guide.md`，一般业务开发不需要使用。

### 4. 参数规则

- ≤5 个参数 → `@Name`
- \>5 个参数 → `@RequestBean` + `@DataBean` DTO
- 不要用 `Object` 或 raw `Map` 代替 DTO

### 5. 跨实体访问

```java
@Inject
ILitemallGoodsProductBiz goodsProductBiz;

// 业务代码：走权限管道
LitemallGoodsProduct product = goodsProductBiz.requireEntity(productId, null, context);

// 已持有实体的关联 → 直接用关系 getter
LitemallGoods goods = product.getGoods();
```

### 6. 实体操作

```java
// 创建：必须用 newEntity()
LitemallOrder order = newEntity();

// 程序化保存
saveEntity(order, null, context);

// 前端Map保存
save(data, context);

// 获取（不存在抛错）
LitemallOrder order = requireEntity(orderId, null, context);

// 获取（可返回null）
LitemallOrder order = get(orderId, false, context);

// 查询列表
List<LitemallOrder> list = findList(query, null, context);

// 分页查询
PageBean<LitemallOrder> page = findPage(query, null, context);
```

### 7. 错误处理

所有业务异常必须使用 `ErrorCode` + `NopException`：

```java
public interface AppMallErrors {
    String ARG_ORDER_ID = "orderId";
    ErrorCode ERR_ORDER_NOT_FOUND = ErrorCode.define(
        "nop.err.mall.order.not-found",
        "Order not found: {orderId}",
        ARG_ORDER_ID
    );
}

throw new NopException(AppMallErrors.ERR_ORDER_NOT_FOUND)
    .param(AppMallErrors.ARG_ORDER_ID, orderId);
```

规则：
- 所有错误码定义在 `*Errors.java` 接口中，不要散落在业务代码里
- `ErrorCode.define()` 描述用中文，框架通过 i18n 翻译
- 用 `.param(...)` 附加上下文参数（实体 ID、当前状态等）
- 包装底层异常：`new NopException(ERR_XXX, e).param(...)`

### 8. 事务后回调

```java
txn().afterCommit(null, () -> {
    sendNotification(order);
});
```

### 9. QueryBean 查询构造

```java
QueryBean query = new QueryBean();
query.addFilter(FilterBeans.eq("userId", userId));
query.addFilter(FilterBeans.in("status", statusList));
query.setLimit(20);
```

---

## 反模式表（快速参考，完整自检见 selfcheck 文档）

> 以下仅列出最常见反模式。**每写完/修改一个方法后，必须用 `{DOCS-FOR-AI}/04-reference/bizmodel-method-selfcheck.md` 完整 19 项校验，不能只看此表。**

| 不要这样写 | 应该这样写 |
|-----------|-----------|
| `dao().getEntityById(id)` | `requireEntity(id, null, context)` |
| `dao().findAllByQuery(query)` | `findList(query, null, context)` |
| `dao().saveEntity(entity)` | `saveEntity(entity, null, context)` |
| `new LitemallOrder()` | `newEntity()` |
| `@BizMutation @Transactional` | 只用 `@BizMutation` |
| `@Inject private` | `@Inject` 不能 private |
| `extends RuntimeException` | `extends NopException` |
| `throw new RuntimeException("msg")` 或 `new NopException("msg")` | 必须用 `ErrorCode.define()` + `new NopException(ERR_XXX).param(...)` |
| 直接注入其他BizModel实现类 | 注入 `I*Biz` 接口 |
| IBiz接口方法缺少注解 | 必须有 `@BizQuery`/`@BizMutation`/`@BizAction` |
| BizModel新增public方法未同步IBiz | 必须先在接口声明 |
| 自定义方法与ICrudBiz标准方法重名 | 用不同的名字 |
| 已有ORM关系时用IBiz.get()获取关联 | 用关系getter |
| `daoProvider().daoFor(Xxx.class)` 在业务BizModel中 | 注入 `I*Biz` |
| 局部DTO放入 `*-api/` 模块 | 放 `*-dao/.../dto/` 或 `*-service/` |
| 创建 `*Service`/`*Controller` 类 | Nop用BizModel/IBiz |
| BizModel返回值无脑用DTO代替Entity | 实体能表达的优先用实体 |
| 创建无xmeta的伪BizModel | GraphQL无法识别 |

---

## 平台工具类

| 用途 | 正确用法 | 不要用 |
|------|---------|--------|
| 时间 | `CoreMetrics.currentTimeMillis()` | `System.currentTimeMillis()` |
| JSON | `JsonTool.parse/serialize()` | 第三方JSON库 |
| 字符串 | `StringHelper.isEmpty/isBlank/...` | Apache Commons |
| 资源关闭 | `IoHelper.safeClose(obj)` | 手写try-catch close |

---

## 参考文件

- `{DOCS-FOR-AI}/05-examples/ibiz-and-bizmodel.java` — IBiz + BizModel 完整示例
- `{DOCS-FOR-AI}/05-examples/dto-and-errors.java` — DTO + ErrorCode 示例
- `{DOCS-FOR-AI}/05-examples/entity-class.java` — 实体类 + 领域方法示例
- `{DOCS-FOR-AI}/05-examples/delta-customization.java` — Delta定制示例
- `{DOCS-FOR-AI}/04-reference/bizmodel-method-selfcheck.md` — 19项方法自检清单
