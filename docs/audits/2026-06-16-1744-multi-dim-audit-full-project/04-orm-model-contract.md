# 维度 04：ORM 模型与契约

> 审计日期：2026-06-16 | 审计对象：整个项目 | Round 1 初审 + 主 agent 复核（含 P0 核验）

## 第 1 轮（初审）发现

### [维度04-01] LitemallGoods→orderGoods 双向 cascadeDelete=true 致订单商品历史被误删 — P0
- **文件**: `model/app-mall.orm.xml:642-648`（goods 侧）、`:1035-1041`（order 侧）
- **证据片段**:
```xml
<to-many cascadeDelete="true" displayName="订单商品" name="orderGoods"
         refEntityName="app.mall.dao.entity.LitemallOrderGoods" refPropName="goods" ...>
```
- **严重程度**: P0
- **现状**: OrderGoods 被 Goods 和 Order 同时 cascadeDelete=true 持有；删任一商品连带删除所有引用它的历史订单商品
- **风险**: 运营下架/删除商品摧毁已成交订单快照明细，退款/对账/统计失真不可恢复
- **建议**: 移除 goods→orderGoods 的 cascadeDelete（保留 order→orderGoods），商品走 useLogicalDelete 软删
- **信心水平**: 确定
- **复核状态**: 已保留（主 agent 直读 orm.xml:642 确认；注意 goods 用 useLogicalDelete 故为软删级联，仍破坏 orderGoods 可见性/统计）

### [维度04-02] API 契约 MallService.findMallProducts 在 service 层完全未实现 — P1
- **文件**: `model/app-mall.api.xml:8-12`
- **证据片段**: api.xml 声明 MallService.findMallProducts；app-mall-api/service 无 MallService 实现类
- **严重程度**: P1
- **建议**: 补实现或从 api.xml 移除声明
- **复核状态**: 已保留（与维度07-2 同源）

### [维度04-03] deploy/sql 三方言 DDL 完全缺失 ORM 声明的 31 个索引与 1 个唯一键 — P0
- **文件**: `deploy/sql/{mysql,postgresql,oracle}/_create_app-mall.sql`（各 0 个 CREATE INDEX），对照 ORM 31 个 `<index>`
- **证据片段**: `grep -c "CREATE INDEX"` 三方言 DDL 均为 0；ORM `<index>` 计数 31
- **严重程度**: P0
- **现状**: 三 DDL 除 PK 外无任何索引；module-boundaries.md:95 "31 index 已同步"声明虚假
- **风险**: 按 DDL 部署则订单/商品高频查询全表扫描；文档与产物严重不一致
- **建议**: 真正同步索引到 DDL（独立 _create_index.sql 防 regen 覆盖）+ 修正 module-boundaries.md
- **信心水平**: 确定
- **复核状态**: 已保留（主 agent 三重 grep 亲自验证：ORM 31 索引，三 DDL 0 索引）

### [维度04-04] LitemallStorage.key 声明"唯一索引"但 ORM 与 DDL 均无唯一约束 — P1
- **文件**: `model/app-mall.orm.xml:1184-1185`
- **现状**: displayName="文件的唯一索引"但无 unique-key
- **建议**: 加 `<unique-keys><unique-key columns="key" name="storageKeyUnique"/>`

### [维度04-05] 多个语义唯一业务编号缺唯一约束（orderSn/aftersaleSn/coupon.code/goodsSn/system.keyName） — P1
- **文件**: `app-mall.orm.xml:973/185/434/568/1208`

### [维度04-06] 金额字段全部使用 DECIMAL(10,2) 而非整数（分） — P1
- **文件**: `app-mall.orm.xml:987-1002`（order 价格族）、`:420`（coupon.discount）等 19 处
- **风险**: 浮点累计误差；微信支付按分计价需反复转换

### [维度04-07] 三个 status 字段无字典引用（Feedback/Groupon/GrouponRules） — P1
- **文件**: `app-mall.orm.xml:511-512/789-790/835-836`

### [维度04-08] 孤儿字典 mall/gender 与 mall/user-status 完全无字段引用 — P2
- **文件**: `app-mall.orm.xml:61-70`
- **现状**: mall/user-status 取值与平台 auth/user-status 完全反向（0=可用 vs 0=停用）

### [维度04-09] LitemallResetCode 主键设计不一致：VARCHAR(50)+seq，DDL 允许 NULL — P1
- **文件**: `app-mall.orm.xml:1259-1260`、`mysql/_create_app-mall.sql:289`

### [维度04-10] i18n-en:displayName 覆盖严重不均（仅 Goods/OrderGoods/NopAuthUserEx 全覆盖） — P2

### [维度04-11] LitemallCouponUser 缺到 LitemallOrder 的关系定义（orderId 有列与索引） — P2
- **文件**: `app-mall.orm.xml:476-483`

### [维度04-12] LitemallGoods 唯一键约束的是 name 而非 goodsSn，业务语义错位 — P2
- **文件**: `app-mall.orm.xml:650-652`

## 维度复核结论

主 agent 对 2 个 P0 独立复核：
- 04-01：直读 orm.xml:642 cascadeDelete="true" 确认；补充 nuance：goods 用 useLogicalDelete 故为软删级联
- 04-03：三重 grep 亲自验证（ORM 31 索引，mysql/pg/oracle DDL 各 0 索引）；与维度07 根因分析（平台 ddl.xlib CreateTables 不调 AddIndex + 今天 regen 覆盖）印证
12 项全部保留。

## 最终保留项

| 编号 | 严重 | 文件 | 一句话摘要 |
|------|------|------|-----------|
| 04-01 | P0 | app-mall.orm.xml:642-648 | goods↔orderGoods 双向 cascadeDelete |
| 04-03 | P0 | deploy/sql/* DDL | 三方言 DDL 缺 31 索引 |
| 04-02 | P1 | app-mall.api.xml:8-12 | MallService.findMallProducts 无实现 |
| 04-04 | P1 | app-mall.orm.xml:1184 | Storage.key 无唯一约束 |
| 04-05 | P1 | app-mall.orm.xml 多处 | 5 个业务编号缺唯一约束 |
| 04-06 | P1 | app-mall.orm.xml 多处 | 金额用 DECIMAL 非整数分 |
| 04-07 | P1 | app-mall.orm.xml:511/789/835 | 3 个 status 无字典 |
| 04-09 | P1 | app-mall.orm.xml:1259 | ResetCode 主键设计不一致 |
| 04-08 | P2 | app-mall.orm.xml:61-70 | 孤儿字典 gender/user-status |
| 04-10 | P2 | app-mall.orm.xml | i18n-en 覆盖不均 |
| 04-11 | P2 | app-mall.orm.xml:476 | CouponUser 缺 order 关系 |
| 04-12 | P2 | app-mall.orm.xml:650 | Goods 唯一键错位到 name |

## 维度评级：Moderate（偏下）

实体建模整体规范、字典与设计基本一致；但 2 个 P0（双向级联+DDL 索引缺失）+ 6 个 P1（契约漂移）需优先修复才达生产级。
