# 领域设计指南

## 目的

定义 `nop-app-mall` 的项目级领域设计补充规则。

通用的 Nop 应用项目规则由 `../nop-entropy/docs-for-ai/02-core-guides/application-project-docs-and-domain-design.md` 负责。

本文件只用于记录商城项目特有的领域归属和本地解释，不重复通用 Nop 平台规则。

## 上游规则

修改商城设计文档前，应先应用这些共享的 Nop 应用规则：

- `../nop-entropy/docs-for-ai/00-start-here/application-project-defaults.md`
- `../nop-entropy/docs-for-ai/02-core-guides/application-project-docs-and-domain-design.md`
- `../nop-entropy/docs-for-ai/02-core-guides/domain-logic-and-ddd.md`

本地规则：

- 如果本文件与共享的 Nop 应用规则冲突，应优先修正本文件，除非冲突来自明确的商城项目特有业务约束。
- 商城特有业务约束必须可以追溯到 `docs/requirements/`、`docs/design/`、`docs/architecture/` 或 `model/*.orm.xml` / `model/*.api.xml`。
- 术语中英文对应或具体含义发生冲突时，以 `domain-glossary.md` 为准。

## 商城领域区域

在 `nop-app-mall` 中，以下领域区域构成稳定的 owner-doc 映射：

| 领域区域 | Owner Doc | 负责内容 |
| -------- | --------- | -------- |
| 商品目录 | `product-catalog.md` | 分类、品牌、商品、SKU、规格、属性、目录搜索、前台展示 |
| 交易领域 | `order-and-cart.md` | 购物车、结算、订单生命周期、支付状态、发货状态、取消、退款、售后 |
| 身份与地址 | `user-and-address.md` | 商城用户、后台用户、个人资料、认证基线、地址、地区 |
| 营销与互动 | `marketing-and-promotions.md` | 优惠券、团购、收藏、评论、专题、广告、反馈、足迹、搜索历史、关键词 |
| 运营配置 | `system-configuration.md` | 业务配置、存储、公告、运营任务、管理员日志、统计 |
| 角色与权限 | `roles-and-permissions.md` | 跨领域的业务角色含义、可见性与受保护操作 |

## 跨领域归属

- 结算由 `order-and-cart.md` 负责；其中可以引用商品可售性、用户地址、优惠券资格和支付能力，但不复制这些领域的完整规则。
- 支付、退款和售后业务状态由 `order-and-cart.md` 负责；集成机制属于 `docs/architecture/` 或实现文档。
- 优惠券和团购资格由 `marketing-and-promotions.md` 负责；最终订单价格影响和订单状态结果由 `order-and-cart.md` 负责。
- 用户身份与地址归属由 `user-and-address.md` 负责；订单配送与收货结果由 `order-and-cart.md` 负责。
- 角色含义和受保护操作的可见性由 `roles-and-permissions.md` 负责；各领域文档仅在业务流程说明需要时提及角色参与。
- 存储、公告、定时运营效果、管理员日志和统计语义由 `system-configuration.md` 负责；技术调度、存储适配器、通知投递和报表实现属于 `docs/architecture/`。

## 商城特有编写规则

- 保持前台用户动作、后台管理员动作和系统自动动作之间的区别清晰。
- 即使实现按小步闭环推进，也要保持商业行为定义是正式的，而不是临时演示语义。
- 支付和退款状态应描述为业务结果，不应超出 owner docs 和代码路径去虚构外部微信支付行为。
- 持久化字段集、状态码和字典值仍以 `model/*.orm.xml` 和 `model/*.api.xml` 为准。
- 实施顺序写入 `docs/backlog/` 或 `docs/plans/`，不要写进领域设计文档。

## 更新规则

只有在商城领域归属映射或本地解释发生变化时，才更新本文件。

如果只是某个单独功能的支持行为变化，应更新对应的设计 owner doc，而不是修改本文件。
