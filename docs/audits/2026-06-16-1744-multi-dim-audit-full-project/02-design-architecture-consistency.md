# 维度 02：设计与架构一致性

> 审计日期：2026-06-16 | 审计对象：整个项目 | Round 1 初审 + 主 agent 复核

## 第 1 轮（初审）发现

### [维度02-01] userType 取值定义与平台字典严重不一致 — P1
- **文件**: `docs/design/user-and-address.md:61-67,83,108-109`；平台 `auth/user-type.dict.yaml`
- **证据**: 设计声称 userType=0/1/2；平台字典只有 1=普通用户/100=外部用户；0 和 2 在 dict 中不存在；delta 未覆盖此字典
- **风险**: 按"用 userType=2 创建管理员"会写入不在字典的值；管理员识别逻辑与平台不一致
- **建议**: delta 扩展 user-type.dict 或修正 design 对齐平台取值（1/100），管理员识别改基于 NopAuthRole

### [维度02-02] mall/user-status 与 mall/gender 孤儿字典，且 mall/user-status 与平台完全反向 — P2
- **文件**: `model/app-mall.orm.xml:61-70`
- **现状**: 全仓库无 column 引用；mall/user-status（0=可用）与平台 auth/user-status（0=停用）语义完全反向

### [维度02-03] 设计声称 LitemallAddress 有 user 关联指向 NopAuthUser，但 ORM 无 relations 节点 — P2
- **文件**: `user-and-address.md:126` vs `app-mall.orm.xml:141-178`

### [维度02-04] 设计声明售后须关联"订单商品"，但 ORM 缺 orderGoodsId，refund 误退整单 — P2
- **文件**: `order-and-cart.md:218-222`；`LitemallAftersaleBizModel.java:122-129`
- **现状**: LitemallAftersale 只有 orderId；refund 遍历整单 orderGoods addStock（与 09-12 同源）

### [维度02-05] 设计通知类别只列 3 类，实际 NotifyType 4 类 + 6 个发送方法 — P2
- **文件**: `system-configuration.md:58-63`；`NotifyType.java`、`MallNotificationService.java`
- **现状**: 实现多 REFUND/CAPTCHA 两类、sendRefundNotification/sendCaptchaCode 等方法 design 未列

### [维度02-06] system-baseline.md 描述 uber-jar/runner.jar，默认构建产 fast-jar，文档内部 build vs run 命令矛盾 — P2
- **文件**: `system-baseline.md:9-10,52`；`app-mall-app/target/quarkus-artifact.properties`
- **现状**: Quarkus 3.35.1 默认 fast-jar（quarkus-app/quarkus-run.jar），非 *-runner.jar；按文档 `./mvnw clean package` 后 `java -jar .../runner.jar` 会失败

### [维度02-07] 设计声明"包邮门槛"配置，代码完全未实现，freight 永远 flat — P2
- **文件**: `system-configuration.md:22`；`order-and-cart.md:62-65`；`LitemallOrderBizModel.java:167-173`
- **现状**: freight 永远等于 mall_freight_price 配置值，无满 X 免运费判断

### [维度02-08] 运行时配置 key 命名不一致：代码读 mall_*，种子用 litemall_*，缺失时静默退化为零 — P2
- **文件**: `LitemallOrderBizModel.java:170`、`LitemallAddressBizModel.java:69` vs `deploy/sql/mysql/litemall_data.sql:291`
- **现状**: 用种子数据初始化时 getConfig("mall_*") 返回 null，freight=0 资损/address 无限制

### [维度02-09] 设计订单状态机迁移规则未列 201→204（团购超时） — P3
### [维度02-10] LitemallKeyword 无 enabled 字段，设计却把"启用状态"列为可见性因素 — P3
### [维度02-11] LitemallComment ORM 支持 type=1 专题评论，design 评论小节未描述 — P3

## 维度复核结论
主 agent 复核：11 项全部保留。02-01 经平台字典 live 核验；02-08 配置 key 不一致经 grep 确认代码只引用 mall_* 而种子全 litemall_*；其余与多维度交叉印证。

## 最终保留项

| 编号 | 严重 | 文件 | 一句话摘要 |
|------|------|------|-----------|
| 02-01 | P1 | user-and-address.md:61-67 | userType 0/1/2 与平台字典 1/100 不一致 |
| 02-02 | P2 | app-mall.orm.xml:61-70 | 孤儿字典+user-status 反向 |
| 02-03 | P2 | user-and-address.md:126 | Address ORM 无 user 关系 |
| 02-04 | P2 | order-and-cart.md:218 | Aftersale 缺 orderGoodsId 误退整单 |
| 02-05 | P2 | system-configuration.md:58 | 通知类别 3 vs 实现 4 |
| 02-06 | P2 | system-baseline.md:9-10 | uber-jar vs fast-jar 矛盾 |
| 02-07 | P2 | system-configuration.md:22 | 包邮门槛未实现 |
| 02-08 | P2 | OrderBizModel:170 等 | 配置 key mall_* vs litemall_* |
| 02-09 | P3 | order-and-cart.md:88-92 | 状态机缺 201→204 |
| 02-10 | P3 | marketing-and-promotions.md:120 | Keyword 无 enabled 字段 |
| 02-11 | P3 | marketing-and-promotions.md:139 | Comment type=1 未描述 |

## 维度评级：Moderate

1 个 P1（userType 契约漂移）+ 7 个 P2（多为 design 声明能力在 ORM/代码找不到对应）。核心业务实体字段/字典/index 与 ORM 基本对齐；问题集中在 design 与平台字典对齐、design 声明能力未实现、system-baseline 部署描述不一致。
