# 设计文档索引

## 目的

`docs/design/` 用于存放稳定的应用层 owner docs。

本目录适合承载：

- 产品功能基线
- 页面与流程行为
- 角色与权限
- 应用层壳和交互行为

跨功能的技术结构请写入 `docs/architecture/`。

## 范围边界

- `docs/requirements/` 负责某个切片或产品基线“应该做什么”
- `docs/design/` 负责该切片落定后的稳定应用层基线
- `docs/architecture/` 负责技术设计与跨功能结构
- `model/*.orm.xml` 负责持久化实体结构、字段集、字典和 ORM 真相

当某个功能同时依赖业务设计与技术设计时，应将两类内容分别写在对应 owner doc 中，并通过引用建立关联。

设计文档可以保留面向业务的实体名称、状态含义和状态迁移规则。

设计文档不应重复表目录、逐字段 schema 定义、字典清单，或本应属于 `model/*.orm.xml`、`docs/architecture/` 的平台实现章节。

## 起始文档

- `app-overview.md`
- `feature-inventory.md`
- `domain-design-guidelines.md`
- `domain-glossary.md`
- `roles-and-permissions.md`

## 详细业务设计文档

| 文档 | 领域 |
|-----|------|
| `product-catalog.md` | 商品、分类、品牌、SKU、规格、搜索 |
| `order-and-cart.md` | 购物车、订单生命周期、状态机、结算、支付、发货、退款、售后 |
| `user-and-address.md` | 用户资料、认证、地址管理、地区 |
| `marketing-and-promotions.md` | 优惠券、团购、搜索历史、收藏、专题、广告 |
| `system-configuration.md` | 系统配置、文件存储、通知、公告、定时任务、管理员日志、统计 |

## 编写规则

- 保持 `docs/design/` 聚焦于业务语义、角色、流程和支持行为。
- 通用 Nop 应用 owner-doc 与领域设计规则以上游 `../nop-entropy/docs-for-ai/02-core-guides/application-project-docs-and-domain-design.md` 为准。
- `domain-design-guidelines.md` 只负责商城项目自己的领域归属映射和本地解释。
- 术语冲突或中英文对应不一致时，以 `domain-glossary.md` 为准。
- 当文档需要持久化模型细节时，应引用 `model/app-mall.orm.xml`，不要重复抄写 schema。
- 当文档需要实现策略时，应引用 `docs/architecture/`，不要在这里混入平台实现细节。
- 不要把设计文档当作 roadmap 或实施状态跟踪器；实施顺序应写入 `docs/backlog/` 或计划文件。

## 参考项目

本目录的设计参考了以下常见 Java 电商开源项目：

| 项目 | Star 数 | 相关性 |
|------|---------|--------|
| [macrozheng/mall](https://github.com/macrozheng/mall) (83k+) | 企业级 Spring Boot + MyBatis 电商系统 | 架构模式、促销模型、RBAC |
| [linlinjava/litemall](https://github.com/linlinjava/litemall) (20k+) | Spring Boot + Vue + 微信小程序商城 | 直接需求来源、订单状态机、团购模型 |
| [YunaiV/yudao-cloud](https://github.com/YunaiV/yudao-cloud) (19k+) | Spring Cloud Alibaba 多租户系统 | 多租户模式、权限模型 |
