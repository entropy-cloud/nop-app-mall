# 功能清单

## 目的

将产品能力区域映射到稳定的 owner doc。

本文件不是 roadmap、backlog 或实施状态矩阵。它的用途是把读者路由到某项能力的 owner doc，而不是记录该能力当前是否正在实施。

## 能力映射

| 能力区域 | Owner Doc | 说明 |
| -------- | --------- | ---- |
| 用户与后台账号管理 | `docs/design/user-and-address.md` | 注册、登录、个人资料、后台账号、地址、地区数据 |
| 角色与权限 | `docs/design/roles-and-permissions.md` | 业务角色含义、可见性、受保护操作 |
| 商品目录 | `docs/design/product-catalog.md` | 分类、品牌、商品、SKU、规格、属性、目录搜索 |
| 购物车与结算 | `docs/design/order-and-cart.md` | 购物车行、结算前置条件、价格构成、订单提交 |
| 订单生命周期 | `docs/design/order-and-cart.md` | 支付、发货、收货、取消、退款、售后状态含义 |
| 营销与促销 | `docs/design/marketing-and-promotions.md` | 优惠券、团购、互动能力、营销内容、反馈界面 |
| 系统配置与运营 | `docs/design/system-configuration.md` | 业务配置、存储、公告、运营任务、管理员日志、统计 |

## 规则

- 只有在确实有助于把读者路由到稳定 owner doc 时，才增加一行。
- 不要在这里加入按功能粒度的实施状态、roadmap 阶段标签、计划链接或 backlog 状态。
- 实施顺序应放在 `docs/backlog/` 或计划文件中。
- 持久化模型细节仍以 `model/*.orm.xml` 为准。
