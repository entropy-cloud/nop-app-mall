# 开放式对抗性审查报告：nop-app-mall 全项目

> **日期：** 2026-06-13
> **审计风格：** 开放式对抗性审查（基于 `nop-entropy/ai-dev/skills/open-ended-adversarial-review-prompt.md`）
> **范围：** nop-app-mall 全项目（service / dao / delta / web / api / wx / app / model / config）
> **切入点：** 无预设维度，从代码中的异常信号出发

## 去重确认

已浏览 `docs/audits/2026-06-12-multi-dimensional-audit-full-project.md`（前次多维审计）和 `nop-entropy/ai-dev/audits/` 下的 nop-entropy 模块审计标题。本报告只报告新发现或已有问题的新线索，不机械复述。

---

## 发现

### [AR-1] model/app-mall.api.xml 从 nop-wf 模板复制，所有元数据错误

- **文件**: `model/app-mall.api.xml:1-14`
- **证据片段**:
  ```xml
  <api ext:appName="nop-wf" ext:serviceModuleName="nop-wf-core"
       ext:servicePackageName="io.nop.wf.core.service"
       ...
       ext:mavenArtifactId="nop-wf" ...>
      <service name="MallService" displayName="工作流服务" i18n-en:displayName="Mall Service">
  ```
- **严重程度**: P0
- **现状**: 整个 `app-mall.api.xml` 文件从 `nop-wf`（工作流）模块模板复制而来，从未修正。`appName`、`serviceModuleName`、`servicePackageName`、`mavenGroupId`、`mavenArtifactId` 全部指向 nop-wf。中文名 `displayName="工作流服务"`（工作流服务）与英文名 `Mall Service` 直接矛盾。唯一定义的服务 `findMallProducts` 使用 `mutation="true"` 和 `tagSet="sync"` 组合，在 Nop API 模型中不太常见。
- **风险**: 如果有工具或管线读取此文件（如 API 文档生成、客户端代码生成、服务注册），将产生完全错误的元数据。即使当前无管线依赖此文件，它作为项目正式模型文件存在，会误导任何未来集成。
- **建议**: 重写整个文件，修正所有元数据为 app-mall 项目的实际值，或如果 API 模型目前不被使用，在文件顶部加注释标记为 placeholder。
- **信心水平**: 确定
- **发现来源视角**: 代码生成受害者

### [AR-2] `readCount` 定义为 VARCHAR(255) 而非整数 — ORM 模型类型错误

- **文件**: `model/app-mall.orm.xml:1102`
- **证据片段**:
  ```xml
  <column code="READ_COUNT" displayName="专题阅读量" name="readCount" precision="255"
          stdDataType="string" stdSqlType="VARCHAR"/>
  ```
- **严重程度**: P1
- **现状**: `LitemallTopic.readCount`（专题阅读量）定义为字符串类型 `VARCHAR(255)`，这是语义上的整数字段。对比同实体中 `sortOrder`（排序）正确使用了 `stdDataType="int"`。
- **风险**: 所有对 `readCount` 的排序、比较、聚合运算在数据库层面都会走字符串语义（字典序而非数值序）。如果未来有人写 `readCount > 100` 的查询，会得到错误结果。代码层面对该字段的 `+1` 操作如果涉及类型转换也可能出错。
- **建议**: 改为 `stdDataType="int" stdSqlType="INTEGER"`。
- **信心水平**: 确定
- **发现来源视角**: 模型攻击者

### [AR-3] `LitemallGrouponRules.discount` 使用 `precision="63" scale="0"` — 疑似笔误

- **文件**: `model/app-mall.orm.xml:731`
- **证据片段**:
  ```xml
  <column code="DISCOUNT" displayName="优惠金额" ... precision="63" scale="0"
          stdDataType="decimal" stdSqlType="DECIMAL"/>
  ```
- **严重程度**: P1
- **现状**: 团购规则的优惠金额字段使用 63 位精度 0 位小数，这意味着该字段在数据库中为 `DECIMAL(63,0)` — 一个 63 位整数。对比同项目的 `LitemallCoupon.discount` 使用 `precision="10" scale="2"`（合理的货币精度）。测试代码中使用 `BigDecimal("20.00")` 创建折扣值，scale=0 会导致 `20.00` 被截断为 `20`。
- **风险**: 
  1. 小数金额折扣（如 9.50 元）会被静默截断为整数
  2. `DECIMAL(63,0)` 在 MySQL 中占用约 29 字节存储，不合理
  3. 与 `LitemallCoupon.discount` 的类型不一致，增加维护困惑
- **建议**: 改为 `precision="10" scale="2"`，与 Coupon 保持一致。
- **信心水平**: 确定
- **发现来源视角**: 模型攻击者

### [AR-4] ORM 模型所有实体缺少外键索引 — 生产性能隐患

- **文件**: `model/app-mall.orm.xml`（全文）
- **严重程度**: P1
- **现状**: 整个 ORM 模型仅在 `LitemallGoods.name` 上定义了一个唯一索引（`goodsNameKey`）。所有 FK 列（`userId`、`orderId`、`goodsId`、`categoryId`、`brandId`、`couponId`、`pid` 等）均无索引定义。涉及约 30+ 个外键列。
- **风险**: 当数据量增长到一定规模（万级以上），所有按用户查订单、按商品查足迹、按类目查商品、按订单查售后等查询都会退化为全表扫描。这是电商系统最常见的性能瓶颈来源。
- **建议**: 为所有外键列添加索引。至少覆盖以下高频查询路径：
  - `LitemallOrder.userId`、`LitemallOrder.orderStatus`
  - `LitemallCart.userId`、`LitemallCart.goodsId`
  - `LitemallFootprint.userId`、`LitemallFootprint.goodsId`
  - `LitemallOrderGoods.orderId`、`LitemallOrderGoods.goodsId`
  - `LitemallGrouponRules.goodsId`
  - `LitemallCategory.pid`、`LitemallRegion.pid`
- **信心水平**: 确定
- **发现来源视角**: 10x 规模运维者

### [AR-5] `IllegalStateException` 违反 Nop 异常规范

- **文件**: `app-mall-dao/src/main/java/app/mall/dao/entity/LitemallCart.java:19`
- **证据片段**:
  ```java
  throw new IllegalStateException("cart.number is zero");
  ```
- **严重程度**: P1
- **现状**: `LitemallCart.validateForCheckout()` 抛出 `IllegalStateException` 而非 `NopException`。根据 AGENTS.md 明确规则：*"所有业务异常必须继承 `NopException`"*。`IllegalStateException` 不继承 `NopException`，无法被 Nop 的错误处理管线正确捕获和国际化。
- **风险**: 
  1. 异常消息为英文硬编码字符串，无法走 Nop 的 i18n 管线
  2. Nop 的全局异常处理器对 `NopException` 和非 `NopException` 的处理路径不同，可能返回不同的 HTTP 状态码和错误格式
  3. 违反了项目编码规范
- **建议**: 在 `AppMallErrors` 中定义错误码 `ERR_CART_NUMBER_ZERO`，使用 `throw new NopException(ERR_CART_NUMBER_ZERO)`。
- **信心水平**: 确定
- **发现来源视角**: 异常路径侦探

### [AR-6] 三个 `to-one` 关系的 `displayName` 错误标注为"订单"

- **文件**: `model/app-mall.orm.xml:265,490,747`
- **证据片段**:
  ```xml
  <!-- LitemallCart -> goods -->
  <to-one displayName="订单" name="goods" refEntityName="...LitemallGoods" ...>
  <!-- LitemallFootprint -> goods -->
  <to-one displayName="订单" name="goods" refEntityName="...LitemallGoods" ...>
  <!-- LitemallGrouponRules -> goods -->
  <to-one displayName="订单" name="goods" refEntityName="...LitemallGoods" ...>
  ```
- **严重程度**: P2
- **现状**: 三个实体（Cart、Footprint、GrouponRules）到 `LitemallGoods` 的关系均错误标注为 `displayName="订单"`。这些关系指向的是"商品"（Goods），不是"订单"（Order）。英文名 `i18n-en:displayName="Goods"` 是正确的。
- **风险**: 
  1. AMIS 前端自动生成的关联字段标签会显示"订单"而非"商品"
  2. 自动生成的 API 文档和 xmeta 会包含错误的中文标签
  3. 在管理后台的关联选择器中，用户看到的标签与实际实体不匹配
- **建议**: 将三处的 `displayName` 改为 `"商品"`。
- **信心水平**: 确定

### [AR-7] `MallLogManager` 中重复 `setType` 调用

- **文件**: `app-mall-dao/src/main/java/app/mall/dao/manager/MallLogManager.java:90-91`
- **证据片段**:
  ```java
  log.setType(type);
  log.setType(type);
  ```
- **严重程度**: P2
- **现状**: `logCommon` 方法中 `log.setType(type)` 被连续调用两次（复制粘贴错误）。
- **风险**: 功能上无害（幂等操作），但表明此代码缺乏 review。如果有未来开发者将第二次调用改为设置另一个字段（如 `subType`）但忘记了，这个位置就是 bug 的潜伏地。
- **建议**: 删除重复行。
- **信心水平**: 确定

### [AR-8] `getUserStatistics()` 通过 DAO 直接访问 `NopAuthUser`，未使用 I*Biz 接口且未记录理由

- **文件**: `app-mall-service/src/main/java/app/mall/service/entity/LitemallOrderBizModel.java:536`
- **证据片段**:
  ```java
  List<NopAuthUser> users = daoProvider().daoFor(NopAuthUser.class).findAllByQuery(query);
  ```
- **严重程度**: P2
- **现状**: `LitemallOrderBizModel.getUserStatistics()` 通过 `daoProvider().daoFor(NopAuthUser.class)` 直接访问 nop-auth 模块的实体。这违反了 AGENTS.md 规则 *"within BizModel, always inject I*Biz interfaces for other entities"*。且与同文件中其他注入（如 `@Inject INopAuthUserBiz userBiz` in delta）的模式不一致。此处也无注释说明为何绕过 I*Biz。
- **风险**: 
  1. 跨模块边界违规：Order BizModel 绕过 auth 模块的服务层直接操作其数据
  2. 绕过了 auth 模块可能的数据权限过滤、逻辑删除过滤等 BizModel 层逻辑
  3. 如果 auth 模块重构其数据模型，此处会静默失败
- **建议**: 注入 `INopAuthUserBiz` 并通过其 `findList` 方法查询。如果 I*Biz 不支持所需的过滤器，则添加注释说明理由。
- **信心水平**: 很可能
- **发现来源视角**: IoC 侦探

### [AR-9] `LitemallTopicBizModel.frontDetail()` 返回 null 而非抛异常 — 不一致

- **文件**: `app-mall-service/src/main/java/app/mall/service/entity/LitemallTopicBizModel.java:54-58`
- **证据片段**:
  ```java
  LitemallTopic topic = get(id, false, context);
  if (topic == null || Boolean.TRUE.equals(topic.getDeleted())) {
      return null;
  }
  ```
- **严重程度**: P2
- **现状**: `frontDetail` 在专题不存在时返回 `null`，而所有其他实体的类似方法（`LitemallGoodsBizModel.frontDetail`、`LitemallAftersaleBizModel.userDetail` 等）均抛出 `NopException`。GraphQL 层期望非 null 返回值时可能导致 NPE。
- **风险**: 
  1. 客户端（前端/移动端）收到 null 后需要特殊处理，不一致增加调用方负担
  2. 如果 GraphQL schema 标记返回类型为非 null（`LitemallTopic!`），运行时会抛出不明确的 NPE
  3. 违反了"fail fast"原则
- **建议**: 抛出 `NopException(ERR_TOPIC_NOT_FOUND)` 与其他实体保持一致。
- **信心水平**: 确定

### [AR-10] `dispatchRegistrationCoupons` 修改全局 Context 的 userId — 跨请求污染风险

- **文件**: `app-mall-delta/src/main/java/app/mall/delta/biz/LoginApiExBizModel.java:230`
- **证据片段**:
  ```java
  ContextProvider.getOrCreateContext().setUserId(user.getUserId());
  ```
- **严重程度**: P1
- **现状**: `dispatchRegistrationCoupons` 在注册流程中修改了线程上下文的 `userId`。此方法在 `signUp()` 内部调用。如果 `signUp()` 的调用链中有后续代码依赖原始 context（如日志、审计），userId 已被修改。更重要的是，如果 `claimCoupon` 内部依赖 `context.getUserId()` 来确定领取者（它确实如此，见 `LitemallCouponUserBizModel.java:83`），那么此处的 context 修改是功能必需的。但方式脆弱——直接修改全局上下文而非传递参数。
- **风险**: 
  1. 如果 Nop 框架在 `signUp()` 返回后复用此线程处理另一个请求，线程上下文可能仍残留已注册用户的 ID
  2. 异常路径中 context 可能未被正确恢复
  3. 如果 `claimCoupon` 的签名被修改为需要显式 `userId` 参数，此处会被遗忘
- **建议**: 将 `claimCoupon` 改为接受显式 `userId` 参数（或新增一个 `claimCouponForUser(couponId, userId, context)` 方法），避免修改全局 context。
- **信心水平**: 很可能
- **发现来源视角**: 事务边界追踪者

### [AR-11] 密码重置验证码存储在 JVM 内存中 — 集群不安全

- **文件**: `app-mall-delta/src/main/java/app/mall/delta/biz/LoginApiExBizModel.java:87`
- **证据片段**:
  ```java
  static final ConcurrentHashMap<String, ResetCodeEntry> resetCodeStore = new ConcurrentHashMap<>();
  ```
- **严重程度**: P1
- **现状**: 密码重置验证码存储在静态 `ConcurrentHashMap` 中。在多实例部署中，用户从实例 A 发送验证码，从实例 B 重置密码会失败（验证码不存在）。应用重启后所有未使用的验证码丢失。
- **风险**: 
  1. 多实例部署（最常见的生产部署方式）下功能完全不可用
  2. 无 TTL 清理机制，长时间运行后 map 会持续增长（虽然当前有 5 分钟过期检查，但只在验证时检查，不主动清理）
  3. 如果应用被重启攻击，所有用户将无法重置密码
- **建议**: 使用数据库表（如 `mall_reset_code`）或分布式缓存（Redis）存储验证码。如果使用数据库表，可以利用 Nop 的 ORM 机制自动管理。
- **信心水平**: 确定
- **发现来源视角**: 10x 规模运维者

### [AR-12] JWT enc-key 硬编码在 application.yaml 中

- **文件**: `app-mall-app/src/main/resources/application.yaml:22`
- **证据片段**:
  ```yaml
  enc-key: 57adcda2601e429f8422d37bfa07166e
  ```
- **严重程度**: P1
- **现状**: JWT 加密密钥以明文形式硬编码在 YAML 配置文件中。此文件已被提交到 Git 仓库。任何有代码访问权限的人都能获取 JWT 密钥，进而伪造任意用户的认证令牌。
- **风险**: 
  1. 如果此密钥被用于生产环境，攻击者可以伪造管理员令牌
  2. 密钥已进入 Git 历史，即使后续修改也无法从历史中移除（需要密钥轮换 + git history 重写）
  3. 无 production profile 覆盖此值
- **建议**: 
  1. 将密钥外部化为环境变量：`enc-key: ${JWT_ENC_KEY}`
  2. 创建 `application-prod.yaml` 覆盖生产配置
  3. 在 CI 中添加密钥扫描规则
- **信心水平**: 确定
- **发现来源视角**: 新人开发者

### [AR-13] `submit()` 中团购优惠从 `orderPrice` 而非 `goodsPriceTotal` 减去 — 可能导致负价格

- **文件**: `app-mall-service/src/main/java/app/mall/service/entity/LitemallOrderBizModel.java:212-224`
- **证据片段**:
  ```java
  BigDecimal orderPrice = goodsPriceTotal;
  if (order.getFreightPrice() != null) {
      orderPrice = orderPrice.add(order.getFreightPrice());
  }
  orderPrice = orderPrice.subtract(couponPrice);
  ...
  BigDecimal actualPrice = orderPrice;
  actualPrice = actualPrice.subtract(grouponPrice);
  ```
- **严重程度**: P2
- **现状**: 订单价格计算中，优惠券折扣从 `goodsPriceTotal + freightPrice` 中减去，然后团购优惠再从已经减去优惠券的 `orderPrice` 中减去。如果团购折扣金额大于剩余的 orderPrice，`actualPrice` 可能为负。虽然代码在 245 行检查 `actualPrice <= 0` 并自动标记为已支付，但负数价格会被存入数据库。
- **风险**: 
  1. 负数订单价格存入数据库，影响财务报表和统计
  2. 如果后续对接真实支付网关，负数金额可能导致支付 API 调用失败或行为未定义
  3. 没有对优惠券折扣 + 团购折扣之和是否超过商品总价进行校验
- **建议**: 在价格计算后添加 `if (actualPrice.compareTo(BigDecimal.ZERO) < 0) throw NopException(...)` 防止负价格。
- **信心水平**: 很可能

### [AR-14] `selectCouponForOrder` 是 `@BizQuery`（无事务）但被 `submit()` 用于金额计算 — TOCTOU 风险

- **文件**: `app-mall-service/src/main/java/app/mall/service/entity/LitemallCouponUserBizModel.java:118`
- **证据片段**:
  ```java
  @Override
  @BizQuery
  public BigDecimal selectCouponForOrder(...)
  ```
- **严重程度**: P2
- **现状**: `selectCouponForOrder` 被标注为 `@BizQuery`（只读、无事务），在 `LitemallOrderBizModel.submit()` 的 `@BizMutation` 事务中被调用来计算优惠金额。虽然 `submit()` 本身有事务，`selectCouponForOrder` 内部的读取操作在该事务内执行，所以不存在经典的 TOCTOU 问题。但 `selectCouponForOrder` 同时也是公开的 GraphQL 查询端点——客户端可以在非事务上下文中独立调用它。该方法的语义是"为订单选择优惠券"，作为公开查询端点暴露可能引起误解。
- **风险**: 低风险，但此方法更适合作为内部辅助方法而非公开端点。如果客户端利用此端点探测优惠券信息而不实际下单，可能产生大量无效查询。
- **建议**: 考虑将此方法降级为非公开的内部方法（移除 `@BizQuery`，改为包内可见的辅助方法），或在方法名中更明确表达其"预校验"语义（如 `previewCouponDiscount`）。
- **信心水平**: 很可能

### [AR-15] `grouponDetail()` 中 `findList` 结果被丢弃 — 死代码或遗漏

- **文件**: `app-mall-service/src/main/java/app/mall/service/entity/LitemallGrouponBizModel.java:178-186`
- **证据片段**:
  ```java
  QueryBean query = new QueryBean();
  query.addFilter(FilterBeans.or(
          FilterBeans.eq(LitemallGroupon.PROP_NAME_id, id),
          FilterBeans.eq(LitemallGroupon.PROP_NAME_grouponId, id)
  ));
  query.addFilter(FilterBeans.eq(LitemallGroupon.PROP_NAME_deleted, false));
  findList(query, null, context);
  
  return groupon;
  ```
- **严重程度**: P2
- **现状**: `findList` 查询了所有相关团购记录（包括团长和团员），但返回值被完全丢弃。方法最终只返回 `requireEntity` 获取的原始团购记录。
- **风险**: 
  1. 如果这是死代码，则浪费了一次数据库查询
  2. 如果设计意图是将相关团购列表一起返回（如前端需要展示"还差 N 人成团"），那么功能缺失
  3. 无法从前端代码确认意图，因为 Groupon view 是空壳
- **建议**: 要么删除此查询（如果是死代码），要么将结果附加到返回值中（如通过 GraphQL selection 展开关联关系）。
- **信心水平**: 很可能

### [AR-16] `LitemallUser` 视图引用不存在的 BizModel — 运行时必崩

- **文件**: `app-mall-web/src/main/resources/_vfs/app/mall/pages/LitemallUser/_gen/_LitemallUser.view.xml`
- **严重程度**: P1
- **现状**: 生成的 `_LitemallUser.view.xml` 声明 `bizObjName="LitemallUser"` 并引用 `@query:LitemallUser__findPage` 等方法，但：
  - `LitemallUser` 没有对应的 ORM 实体（`app-mall.orm.xml` 中不存在）
  - 没有 `LitemallUserBizModel`（service 模块中不存在）
  - 没有 `ILitemallUserBiz` 接口
  - 只有 `LitemallUserOutputBean` 和一个空壳 view.xml 存在
  
  系统通过 `nop-auth-delta.orm.xml` 将 `NopAuthUser` 扩展了商场字段，但 `LitemallUser` 的 BizModel 从未被创建。这是 Phase 1 实体消除的残留问题——`LitemallUser` 被消除（合并到 `NopAuthUser`），但前端视图和 OutputBean 未被同步清理。
- **风险**: 用户管理页面在管理后台导航时将报 `BizModel not found` 错误。
- **建议**: 要么创建 `LitemallUserBizModel` 作为 `NopAuthUser` 的视图别名，要么删除 `LitemallUser` 页面并使用 `NopAuthUser` 管理页面替代。
- **信心水平**: 确定
- **发现来源视角**: GraphQL 契约考古学家

### [AR-17] `LitemallComment.adminReply` 页面缺少 `id` 参数传递

- **文件**: `app-mall-web/src/main/resources/_vfs/app/mall/pages/LitemallComment/LitemallComment.view.xml`
- **严重程度**: P2
- **现状**: `admin-reply` 页面的提交 API `@mutation:LitemallComment__adminReply` 的 form 只包含 `adminContent` 字段，不包含 `id`。`LitemallCommentBizModel.adminReply()` 方法需要 `@Name("id") String id` 参数。`id` 只在 `initApi` 的 URL 中使用（`?id=$id`），未通过 form data 传递给 mutation。
- **风险**: 管理员回复评论时，后端会收到 `id=null`，导致 `requireEntity` 失败或回复到错误的评论。
- **建议**: 在 `admin-reply-form` 中添加 `id` 为隐藏字段，或在 mutation URL 中传递 `?id=$id`。
- **信心水平**: 很可能

### [AR-18] `signUp` 中 `gender` 硬编码为 1（男性）

- **文件**: `app-mall-delta/src/main/java/app/mall/delta/biz/LoginApiExBizModel.java:132`
- **证据片段**:
  ```java
  "gender", 1,
  ```
- **严重程度**: P3
- **现状**: 注册时性别被硬编码为 1（男性），无用户输入或默认值 0（未知）。
- **风险**: 所有通过此接口注册的用户都被标记为男性，影响用户画像准确性。
- **建议**: 改为 `"gender", 0`（未知）或允许用户传入 gender 参数。
- **信心水平**: 确定

### [AR-19] `nop-auth-delta.orm.xml` 中 `CLIENT_ID` 列被标记为 `tagSet="del"` — 删除原因未记录

- **文件**: `model/nop-auth-delta.orm.xml:36-38`
- **严重程度**: P2
- **现状**: Delta 模型将 `NopAuthUser` 的 `clientId` 列标记为删除（`tagSet="del"`）。如果 nop-auth 的任何逻辑依赖 `clientId`（如设备追踪、会话管理、多端登录控制），此删除会导致运行时异常。删除原因未在设计文档中记录。
- **风险**: 
  1. nop-auth 升级后如果新增 `clientId` 相关逻辑，会静默失败
  2. 与 nop-auth 上游的兼容性未经验证
- **建议**: 在 `docs/design/user-and-address.md` 中记录删除 `clientId` 的原因和影响分析。
- **信心水平**: 很可能
- **发现来源视角**: Delta 地雷探测者

### [AR-20] 版本字符串不一致：`1.0-SNAPSHOT` vs `1.0.0-SNAPSHOT`

- **文件**: `model/app-mall.orm.xml:3` vs `model/nop-auth-delta.orm.xml:4`
- **证据片段**:
  ```xml
  <!-- app-mall.orm.xml -->
  ext:mavenVersion="1.0-SNAPSHOT"
  <!-- nop-auth-delta.orm.xml -->
  ext:mavenVersion="1.0.0-SNAPSHOT"
  ```
- **严重程度**: P3
- **现状**: 两个核心模型文件的 Maven 版本字符串不一致。
- **风险**: 如果有构建管线依赖这些版本字符串，可能导致元数据不一致。实际构建由根 POM 的版本控制，所以对编译无直接影响。
- **建议**: 统一为 `1.0-SNAPSHOT`（与根 pom.xml 一致）。
- **信心水平**: 确定

### [AR-21] `app-mall-api` 模块不继承父 POM — 版本管理孤岛

- **文件**: `app-mall-api/pom.xml`
- **严重程度**: P2
- **现状**: `app-mall-api/pom.xml` 是唯一没有 `<parent>` 引用 `app-mall` 的模块。它独立声明了 `java.version=11` 和硬编码的 `nop-entropy.version=2.0.0-SNAPSHOT`。其他所有模块都继承父 POM 并使用统一的 Java 版本和依赖管理。
- **风险**: 
  1. Java 版本不一致（api 模块声明 Java 11，父 POM 可能使用 Java 17）
  2. nop-entropy 版本更新时需手动同步
  3. 依赖版本可能与父 POM 的 dependencyManagement 冲突
- **建议**: 添加 `<parent>` 引用 `app-mall`，移除独立的 properties 声明。
- **信心水平**: 确定
- **发现来源视角**: 新人开发者

### [AR-22] `getOrderStatistics` 和 `getGoodsSalesRanking` 全表加载后在内存中聚合 — 性能隐患

- **文件**: `app-mall-service/src/main/java/app/mall/service/entity/LitemallOrderBizModel.java:436-462,489-520`
- **证据片段**:
  ```java
  List<LitemallOrder> orders = findList(query, null, context);  // 加载所有订单
  ...
  for (LitemallOrder order : orders) {  // 内存中遍历
  ```
- **严重程度**: P2
- **现状**: `getOrderStatistics()` 和 `getGoodsSalesRanking()` 将所有符合条件的订单加载到内存中，然后遍历计算统计数据。没有使用 SQL 聚合（COUNT/SUM/GROUP BY）。`getGoodsSalesRanking` 进一步为每个订单加载其 `orderGoods` 关联，形成 N+1 加载模式。
- **风险**: 
  1. 当订单数达到万级以上时，单次查询可能消耗大量堆内存
  2. `getUserStatistics()` 同样将所有用户加载到内存
  3. 数据量增长后 admin dashboard 页面将变得不可用
- **建议**: 使用 SQL 聚合查询或 `@SqlLibMapper` 实现数据库端的统计计算。
- **信心水平**: 很可能
- **发现来源视角**: 10x 规模运维者

### [AR-23] Demo/测试代码残留于生产 delta 模块

- **文件**: `app-mall-delta/src/main/java/app/mall/delta/biz/NopAuthUserEx2BizModel.java` 和 `app-mall-delta/src/main/resources/_vfs/_delta/default/nop/auth/pages/NopAuthUser/NopAuthUser.xbiz`
- **严重程度**: P3
- **现状**: Delta 模块中存在两个扩展 BizModel（`NopAuthUserExBizModel` 和 `NopAuthUserEx2BizModel`）同时注册到 `NopAuthUser`。`Ex2BizModel` 的方法 `extAction2` 描述为"定义在NopAuthUserEx2BizModel中的扩展方法"。xbiz 文件中存在 `extAction3`，描述为"定义在biz文件中的测试函数3"。
- **风险**: 
  1. 测试代码在生产环境中注册为可用 GraphQL 端点
  2. 两个 BizModel 同时注册到同一实体增加维护复杂度
  3. 暴露不必要的 API 攻击面
- **建议**: 移除 `NopAuthUserEx2BizModel` 和 xbiz 中的测试函数，或将它们移到 test scope。
- **信心水平**: 确定
- **发现来源视角**: 死代码清道夫

### [AR-24] `LitemallGoods.name` 全局唯一约束可能不符合电商业务意图

- **文件**: `model/app-mall.orm.xml`（`goodsNameKey` 唯一索引）
- **严重程度**: P2
- **现状**: `LitemallGoods.name` 有全局唯一约束。在电商场景中，不同品牌、不同类目、不同规格的商品可能有相同的显示名称（如"经典款 T 恤"可同时属于多个品牌）。
- **风险**: 
  1. 无法录入同名不同品牌的商品
  2. 数据迁移时可能因名称冲突导致失败
  3. 此约束在业务需求文档中未找到明确依据
- **建议**: 如果业务确实要求名称唯一，在设计文档中记录原因。否则考虑移除唯一约束，或改为 `name + brandId` 联合唯一。
- **信心水平**: 有趣的猜测

### [AR-25] 无 production Quarkus profile — 部署时安全/调试设置无法切换

- **文件**: `app-mall-app/src/main/resources/application.yaml`
- **严重程度**: P1
- **现状**: 配置文件只有 default 和 `%dev` 两个 profile，缺少 `%prod` profile。以下设置在 default 中均为"开发友好"状态：
  - `init-database-schema: true` — 生产环境可能意外重建 schema
  - `support-debug: true` — 暴露调试端点
  - `schema-introspection.enabled: true` — 暴露完整 GraphQL schema
  - `allow-create-default-user: true` — 可能创建默认管理员
  - H2 作为默认数据库
- **风险**: 如果应用在未正确配置的情况下部署到生产环境，所有以上"开发友好"设置都会生效，造成安全风险和数据丢失风险。
- **建议**: 创建 `application-prod.yaml`，覆盖以上所有设置为生产安全值。
- **信心水平**: 确定
- **发现来源视角**: 未来破坏者

---

## 总评

本项目最值得关注的 3 个方向：

1. **ORM 模型质量**：`app-mall.api.xml` 完全错误（AR-1），`readCount` 类型错误（AR-2），`discount` 精度错误（AR-3），3 个关系 displayName 错误（AR-6），缺少所有外键索引（AR-4），唯一约束可能过严（AR-24）。ORM 模型是 Nop 平台的代码生成源头，这里的错误会传播到所有下游产物。建议优先修正 `app-mall.api.xml` 和类型/精度错误，然后补全索引。

2. **生产部署就绪度**：JWT 密钥硬编码（AR-12）、无 production profile（AR-25）、验证码存储在 JVM 内存（AR-11）这三个问题组合在一起，意味着项目当前状态无法安全地部署到多实例生产环境。这不是单一模块的问题，而是横跨 config、delta、安全三个领域的连锁缺陷。

3. **模型-代码一致性**：`LitemallUser` 页面引用不存在的 BizModel（AR-16）、`LitemallComment` adminReply 缺少 id 参数（AR-17）、`LitemallGrouponRules.discount` 精度与代码中使用的 BigDecimal 不匹配（AR-3）——这些是模型定义与实际代码之间的断裂。随着项目继续开发，如果不同步修正，断裂会持续扩大。

## 审查盲区自评

- **前端 AMIS 页面的交互正确性**：未启动应用做视觉/交互验证，部分 AMIS 配置问题（如 AR-17 的 id 传递）需要实际运行确认。
- **BizModel 方法级别的业务逻辑正确性**：未逐行验证每个方法的业务规则（如优惠券使用条件、团购规则判定等），仅检查了模式层面的异常。
- **并发和事务边界**：`submit()` 中的库存扣减使用了原子 SQL，但其他并发敏感操作（如优惠券领取的 `claimCoupon` 并发竞争、团购参与人数并发更新）未做深入分析。
- **Nop 平台生成管线的正确性**：未执行 `codegen.sh` 验证生成产物是否与当前代码一致。
- **数据库 DDL 与 ORM 模型的完整对齐**：仅抽查了部分 DDL，未逐表逐列验证。

## 按严重程度分布表

| 严重程度 | 数量 | 主要类别 |
|---------|------|---------|
| P0      | 1    | ORM 模型元数据完全错误（api.xml） |
| P1      | 5    | 安全配置（JWT/profile）、类型错误、内存存储、Context 污染、前端页面崩溃 |
| P2      | 12   | ORM 模型质量、性能隐患、代码一致性、缺少索引、边界违规 |
| P3      | 3    | 死代码残留、硬编码默认值、版本不一致 |
