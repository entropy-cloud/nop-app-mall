# 设计文档一致性审计（文档内在 + 文档-代码）

- 日期：2026-06-17
- 范围：`docs/design/` 全量（含本次新增 `flow-overview.md`、各域跨域引用表、`order-and-cart.md` 迁移规则段改写）与代码真相源（`model/app-mall.orm.xml` 字典、`_AppMallDaoConstants`、`LitemallOrderBizModel`、`LitemallAftersaleBizModel`、`LitemallGrouponBizModel`、`LitemallCommentBizModel`）
- 方法：状态码字典 ↔ 生成常量 ↔ 设计状态表逐值比对；BizModel 中每个 `setOrderStatus`/`setStatus` 写入点与资格守卫逐条比对设计迁移规则与资格规则

## 结论摘要

| 类别 | 数量 |
|------|------|
| 文档内在不一致 | 2（均为本次新增 flow-overview.md 引入，已定位修复点） |
| 文档-代码不一致 | 3（1 重要 / 2 次要） |
| 命名混淆 | 1（既有） |
| 已核对一致项 | 6 类（状态码全对齐） |

---

## 一、文档内在不一致

### F1【本次引入】flow-overview.md 订单状态机 mermaid 与设计抉择矛盾

- 位置：`docs/design/flow-overview.md` L2 订单主状态机
- 问题：mermaid 画出 `已支付 --> 退款中 --> 已退款`，与 `order-and-cart.md` 迁移规则段刚改写的设计抉择直接冲突——该抉择明确"未发货订单售后退款成功直接进入已退款，不经过退款中；退款中（202）保留为未来路径，当前售后路径不使用"。
- 旁证：`order-and-cart.md` 的 text-art 状态叙述同样跳过 202；代码无任何 `setOrderStatus(ORDER_STATUS_REFUND=202)` 写入点（见 F-clean-1）。
- 性质：本次新增 flow-overview.md 时未与同文件既有抉择对齐的回归。
- 建议：将 mermaid 改为 `已支付 --> 已退款: 售后退款成功(未发货)`，移除 退款中 作为可达中间态。

### F2【本次引入】flow-overview.md L3 售后资格规则过宽

- 位置：`docs/design/flow-overview.md` L3 资格前置
- 问题：`[规则] 评价与售后资格都以"订单完成收货"为边界`。其中"评价"部分正确，但"售后"部分错误——售后资格不限于已收货（见 F-doc-code-1）。
- 性质：从 `order-and-cart.md` 既有错误表述复制而来，本次放大到总览文档。
- 建议：拆分为两条——评价以收货完成为边界；售后以"已支付且未进入退款/删除流程"为边界。

---

## 二、文档-代码不一致

### F-doc-code-1【重要】售后申请资格：文档过度收窄

- 文档侧：`order-and-cart.md` 售后范围与售后流转规则——"对已收货订单的售后"、"只有已完成收货的订单才能进入可申请售后状态"。
- 代码侧：`LitemallAftersaleBizModel.java:194-196` 允许 `ORDER_STATUS_PAY(201) || CONFIRM(401) || AUTO_CONFIRM(402)`，即**未发货已支付订单也可申请售后**。
- 旁证：`app-mall.orm.xml` aftersale-type 字典含 `GOODS_MISS 未收货退款=0`，印证未发货退款是支持场景，非代码漏洞。
- 判定：文档错。代码 + 字典是真相：售后覆盖"未发货退款"和"已收货退款/退货退款"两类。
- 连带影响：F2（已污染 flow-overview.md L3 规则）。
- 建议：修订 `order-and-cart.md` 售后范围/流转规则，明确售后资格为"已支付(201)或已完成收货(401/402)，且当前售后状态为可申请(INIT)"。

### F-doc-code-2【次要】团购活动状态：文档多列"开团成功"持久态

- 文档侧：`marketing-and-promotions.md` 团购活动状态表述为"开团未支付、开团中、开团失败或开团成功"。
- 代码/模型侧：`app-mall.orm.xml` `LitemallGroupon.status` 注释与值仅 `0 开团未支付 / 1 开团中 / 2 开团失败`；`LitemallGrouponBizModel` 仅 `setStatus(1)`/`setStatus(2)`，无成功态写入。
- 判定：成功不持久化为状态值，而是由"有效参团人数 ≥ 规则要求人数"计算得出。文档把计算概念误列为持久状态。
- 建议：文档明确"开团成功"是计算结果而非 status 枚举值，避免读者到 ORM 找第 4 个枚举。

### F-doc-code-3【次要】团购超时(204)订单不可删除

- 文档侧：`order-and-cart.md` 状态叙述与 flow-overview.md mermaid 均把 204 团购超时当作终态（→ [*]），且"终态订单支持通过软删除语义从用户可见列表中移除"。
- 代码侧：`LitemallOrderBizModel.java:478-482` 删除白名单为 `CANCEL(102) || AUTO_CANCEL(103) || CONFIRM(401) || AUTO_CONFIRM(402) || REFUND_CONFIRM(203)`，**不含 GROUPON_EXPIRED(204)**。
- 判定：文档与代码对"204 是否用户可删"不一致。要么代码漏放 204（疑似 bug），要么文档对"终态可删"应加例外。需产品确认后定向修一侧。

---

## 三、命名混淆

### F-name-1【既有】order-and-cart.md 迁移叙述用"已申请退款"作订单状态名

- 位置：`order-and-cart.md` 迁移规则与 text-art 叙述——"已支付订单可以进入已申请退款、已退款或已发货"。
- 问题：订单状态表（101~402）无"已申请退款"，对应的是 `202 退款中`。叙述里的"已申请退款"实际语义是售后 `REQUEST`（此时 orderStatus 保持 201，仅 aftersaleStatus 变为 REQUEST）。
- 性质：把售后子状态名混入了订单主状态迁移叙述，读者会误以为存在一个"已申请退款"订单状态码。
- 建议：迁移叙述统一用订单状态码语义（如"已退款 203"），把"用户申请退款"动作明确归到售后状态机描述。

---

## 四、已核对一致项（确认干净，记录以备复核）

| 项 | 比对结果 |
|----|---------|
| 订单状态码 101~402 | ORM 字典 ↔ `_AppMallDaoConstants`（101/102/103/201/202/203/204/301/401/402）↔ `order-and-cart.md` 状态表：**三方逐值一致** |
| 售后状态码 0~5 | ORM 字典（INIT/REQUEST/APPROVED/REFUND/REJECT/CANCELLED）↔ 常量 ↔ `order-and-cart.md` 售后表：**一致** |
| 评价资格 | 代码 `LitemallCommentBizModel:52-54` 要求 CONFIRM(401)‖AUTO_CONFIRM(402)，与文档"只有已完成收货的订单商品才能评价"**一致** |
| 团购超时→204 | 代码 `LitemallGrouponBizModel:253` `setOrderStatus(GROUPON_EXPIRED)`，与设计抉择"团购超时 201→204"**一致** |
| 未发货售后退款→203 | 代码 `LitemallAftersaleBizModel:143` `setOrderStatus(REFUND_CONFIRM)`，跳过 202，与设计抉择**一致** |
| 优惠券状态 0/1/2 | ORM `coupon-status`（正常可用/过期/下架）与文档描述**一致** |

---

## 五、定向修复建议（按优先级）

1. **F1 + F2**：本次引入的 flow-overview.md 两处，属明确回归，应立即修。
2. **F-doc-code-1**：售后资格文档过度收窄，应修文档（代码 + 字典为真相）。
3. **F-name-1**：迁移叙述命名混淆，与 #2 一并修 `order-and-cart.md`。
4. **F-doc-code-2**：团购"开团成功"措辞，文档侧澄清。
5. **F-doc-code-3**：团购超时(204)可删性，需产品确认是代码漏放还是文档应加例外——此项不要单方面改，先留作待决议。

## 六、方法学说明

本次审计的价值集中在 F-doc-code-1（售后资格）：仅看设计文档会以为售后只服务已收货订单，但代码实际支持未发货退款，这类"文档比代码更窄"的偏差最容易在后续开发中误导新逻辑（例如误判某订单"不可售后"而拦截合法的未发货退款）。状态码全对齐（第四节）说明数据字典层是可信的，偏差主要来自业务规则的自然语言表述与代码守卫的精度差。
