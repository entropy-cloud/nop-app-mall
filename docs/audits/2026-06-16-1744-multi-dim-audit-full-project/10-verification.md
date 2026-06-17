# 维度 10：验证充分性

> 审计日期：2026-06-16 | 审计对象：整个项目 | Round 1 初审 + 主 agent 复核

## 第 1 轮（初审）发现

### [维度10-1] 微信支付回调路径完全无订单状态更新测试覆盖 — P0
- **文件**: `WxPayNotifyResource.java:28-53`、`TestWxPayNotifyResource.java:28-65`
- **证据**: parseNotifyBody 返回 outTradeNo 被丢弃；测试仅触发 enabled=false 分支断言 status==200，未断言订单状态推进
- **严重程度**: P0
- **复核状态**: 已保留（与 09-02 同源；资损路径零测试）

### [维度10-2] LitemallOrder__pay() 直接推进状态无凭证校验，测试未覆盖 — P0
- **文件**: `LitemallOrderBizModel.java:337-357`、`TestLitemallOrderBizModel.java:111-142`
- **证据**: pay() 全文 0 处 queryPayment 调用；测试只断言状态变 201，无负面断言
- **复核状态**: 已保留（与 09-01 同源）

### [维度10-3] 售后退款金额无上限校验，测试未覆盖越界 — P1
- **文件**: `LitemallAftersaleBizModel.java:170,96-134`、`TestLitemallAftersaleBizModel.java:143-276`
- **证据**: apply 直接透传 amount 无校验；测试所有 amount 都 ≤actualPrice，无越界用例

### [维度10-4] 防超卖 SQL 已就位但 submit 未检查 reduceStock 返回值，无并发测试 — P1
- **文件**: `LitemallOrderBizModel.java:197`、`LitemallGoodsProduct.sql-lib.xml:16-25`
- **证据**: reduceStock 返回 int 被丢弃；全测试目录 grep 并发关键字（Thread/concurrent/CountDownLatch 等）0 命中
- **复核状态**: 已保留（与 09-03 同源）

### [维度10-5] known-good-baselines.md 是占位符，与日志"全绿 106/38 passed"矛盾 — P1
- **文件**: `docs/testing/known-good-baselines.md:11-14` vs `logs/2026/06-15.md:147`、`06-16.md:13`
- **现状**: 表格无任何行；实际两次完整绿（106+38）未回写
- **风险**: 未来 AI 无法判断 failure 是新引入还是预存在；违反 AGENTS.md 第 8 条

### [维度10-6] project-context.md "E2E:none" 与 e2e/ 目录矛盾 — P1
- **文件**: `project-context.md:47`
- **复核状态**: 已保留（与 13-04 同源）

### [维度10-7] e2e 测试仅冒烟级（渲染/RPC 200），无业务交互与权限边界 — P2
- **文件**: `storefront-pages.spec.ts:46-72`、`auth.ts:22-32`
- **现状**: 38 passed = 11 findPage 200 + 24 getPage type=page + 2 startup + 1 GraphQL 可达；0 加购/0 下单/0 支付/0 退款

### [维度10-8] TestIBizNewEntity 保护力近乎为零（newEntity not null） — P2
- **文件**: `TestIBizNewEntity.java:29-45`
- **现状**: 3 方法只断言 not null，等同于测试 `new X() != null`

### [维度10-9] 核心 P0 资损路径（pay/aftersale refund/wxpay notify）错误路径测试几乎为零 — P2
- **证据**: grep 全测试目录 Order/Aftersale/Pay/WxPay 四测试类共 0 处 -1 断言、0 处 assertThrows

### [维度10-10] 多个测试类 setUp/createFileRecord 大段代码重复 — P3

## 维度复核结论
主 agent 复核：10-1/10-2 资损路径零测试与 09-01/09-02 印证；10-4 并发测试 0 命中独立 grep 确认；10-5 baselines 占位符直读确认。10 项保留。

## 最终保留项

| 编号 | 严重 | 文件 | 一句话摘要 |
|------|------|------|-----------|
| 10-1 | P0 | TestWxPayNotifyResource.java | 回调路径零状态更新测试 |
| 10-2 | P0 | TestLitemallOrderBizModel.java | pay 无凭证校验零测试 |
| 10-3 | P1 | TestLitemallAftersaleBizModel.java | 退款金额越界零测试 |
| 10-4 | P1 | LitemallOrderBizModel.java:197 | 超卖零并发测试 |
| 10-5 | P1 | known-good-baselines.md | 占位符与日志矛盾 |
| 10-6 | P1 | project-context.md:47 | E2E:none 虚假 |
| 10-7 | P2 | storefront-pages.spec.ts | e2e 仅冒烟 |
| 10-8 | P2 | TestIBizNewEntity.java | 保护力为零 |
| 10-9 | P2 | Order/Aftersale/Pay 测试 | 资损路径错误测试缺失 |
| 10-10 | P3 | 多测试类 | fixture 代码重复 |

## 维度评级：Moderate（偏下）

30 测试文件/106 用例/38 e2e 的"数量"基本到位，基类选择正确。但 2 个 P0（资损路径零测试）+ 3 个 P1（金额越界/超卖/baseline 占位）+ e2e 假象覆盖；核心资损路径（支付/订单/售后/回调）错误路径与边界测试几乎完全缺失，known-good baseline 未维护，project-context 验证命令矛盾。
