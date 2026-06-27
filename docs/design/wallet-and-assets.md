# 钱包与资产业务设计

## 目的

说明 `nop-app-mall` 中商城用户的钱包余额、充值交易和资产流水的业务设计。

## 边界

- 本文档负责用户钱包账户、余额变动、充值流程和资产记录的语义。
- 持久化模型结构、字段集和字典定义以 `model/*.orm.xml` 为准。
- 支付集成实现属于 `docs/architecture/`。

## 领域概览

钱包与资产领域包含以下概念：

- Wallet：每个商城用户的钱包账户，记录当前可用余额。
- Recharge：用户通过支付渠道向钱包充值的交易记录。
- Wallet Flow：每笔余额变动的流水记录，包含来源和去向。
- Points：用户的积分账户和流水，积分作为一种用户资产在 `marketing-and-promotions.md` 定义的规则中产生和消耗。

## 钱包账户

### 业务规则

- 每个商城用户有且只有一个钱包账户。
- 钱包账户记录当前可用余额、累计充值总额和累计消费总额。
- 钱包账户在用户注册时自动创建。
- 钱包余额不可为负值。

### 支持行为

- 用户可查看钱包余额和收支汇总。
- 用户可查看钱包流水，支持按时间和类型筛选。

## 充值

### 业务意图

- 充值允许用户通过外部支付渠道向钱包余额存入资金。

### 充值流程

- 用户选择充值金额（支持固定套餐或自定义金额），选择支付渠道后发起支付。
- 支付成功后，钱包余额增加对应金额。
- 充值套餐支持赠送金额配置（如充 100 送 10）。

### 业务规则

- 充值订单走外部支付渠道（微信/支付宝），支付确认后余额到账。
- 充值赠送的金额作为系统赠送余额入账，与用户充值金额在语义上不做严格区分。
- 充值记录对用户可见，包含充值金额、赠送金额、支付渠道和充值时间。

### 管理员动作

- 管理员可查看用户的充值记录。
- 管理员可配置充值套餐（固定金额与赠送金额的组合）。

## 余额支付

- 余额作为支付通道之一在收银台展示（详见 `order-and-cart.md`）。
- 余额支付需要有支付确认环节（支付密码或短信验证码）。
- 余额支付成功后，钱包余额扣减对应金额，并产生余额扣减流水。

## 积分账户

### 业务意图

- 积分账户记录用户的积分余额和变动历史，支撑积分获取和消耗的核算。
- 每个商城用户有且只有一个积分账户，记录当前可用积分（`balance`）、累计获得（`totalEarned`）、累计消耗（`totalSpent`）。
- 账户在用户首次获得积分时自动创建（首次 `earnPoints` 调用），无需注册时预创建。
- 持久化字段、字典、唯一键（`uk_litemall_points_account_user` on `userId`）以 `model/app-mall.orm.xml` 为准。

### 并发安全与原子操作

- 积分账户的获取（earn）与消耗（spend）均为**单事务内原子操作**：以 `version` 乐观锁执行 `update account set balance=balance±N where id=? and version=?`，version 不匹配即抛出 ErrorCode 失败。
- `spend` 前校验 `balance - N >= 0`（余额不可为负），余额不足抛 `nop.err.mall.points.insufficient`。
- 每次变动同时写入一条流水记录（含 `balanceAfter` 变动后余额快照），使账户表与流水表可在任意时刻对账。
- 高并发单账户需重试；本基线非秒杀场景，乐观锁失败即返回错误由调用方重试。

### 积分流水与 changeType × sourceType 分类法

- 每笔积分的增加或减少均产生一条流水记录（`LitemallPointsFlow`）。
- 流水字段：变动时间（`addTime`）、变动类型（`changeType`，字典 `mall/points-change-type`）、变动数量（`changeAmount`）、变动后余额（`balanceAfter`）、来源类型（`sourceType`，VARCHAR(50)）、来源业务 ID（`sourceId`）、备注（`remark`）。
- **字典复用约定（不改模型）：** `mall/points-change-type` 字典仅有三值 `EARN`(0, 获取)/`SPEND`(10, 消耗)/`EXPIRE`(20, 过期)。`changeType` 严格映射这三值——获取类全用 `EARN`、消耗类全用 `SPEND`、过期用 `EXPIRE`。**所有细分来源/去向落入 `sourceType` 字段**：
  - 购物赠送 = `EARN` + `sourceType=order-confirm-earn`
  - 取消/整单退款返还抵扣积分 = `EARN` + `sourceType=refund-return`
  - 结算积分抵扣 = `SPEND` + `sourceType=order-deduct`
  - 后台手工调账（加） = `EARN` + `sourceType=admin-adjust`
  - 后台手工调账（扣） = `SPEND` + `sourceType=admin-adjust`
  - 签到（P28） = `EARN` + `sourceType=check-in`
  - 评价（P33） = `EARN` + `sourceType=comment-reward`
- `sourceId` 记录来源业务 ID（orderId / commentId / checkInRecordId 等），支撑幂等查重（如同一 orderId 不重复赠送）。

### 业务规则

- 积分余额不可为负值。
- 用户可查看积分余额和流水记录，支持按时间范围和类型筛选。
- **积分有效期：** 业务规则要求积分有有效期，过期积分自动扣减并产生 `EXPIRE` 流水。当前**为计划中能力**——模型尚无有效期字段/过期批次实体，且批量过期需 nop-job-local 定时编排。账户/流水/抵扣/获取基座不依赖有效期，自动过期作为 successor（触发条件：积分有效期建模需求或 PointsAccount 模型修改时）。

### 获取规则（积分来源）

积分获取规则配置详见 `marketing-and-promotions.md`「积分体系」章节；本文件仅持有账户/流水语义。主要触发源：

- **购物赠送**（主触发源，已落地）：订单确认收货（confirm）时按 `mall_points_earn_per_yuan` × `actualPrice` 赠送；幂等（同 orderId 不重复赠送，按 `sourceType=order-confirm-earn` + `sourceId=orderId` 查重）。
- 签到（P28，已落地）：每日签到按规则档位发积分，调用 earn API（`sourceType=check-in`, `sourceId=CheckInRecord.id`），业务设计见 `marketing-and-promotions.md`「签到 / Daily Check-In」。评价（P33）、分享（不在基线）作为后续接入源复用同一 earn API。

### 抵扣与返还语义

- 抵扣在结算页的勾选行为、积分兑换比例与抵扣上限由 `order-and-cart.md` 价格语义负责引用结果。
- 取消/整单退款的积分返还语义由 `order-and-cart.md` 退款流程负责引用结果（与券恢复对称：仅订单级返还，item 级部分退款不返还）。

## 与其他 Owner Docs 的关系

- 积分获取规则和积分商城兑换由 `marketing-and-promotions.md` 负责。
- 积分在结算页的抵扣行为由 `order-and-cart.md` 负责。
- 用户个人资料页面由 `user-and-address.md` 负责，本文件补充钱包余额和积分展示的具体资产语义。
