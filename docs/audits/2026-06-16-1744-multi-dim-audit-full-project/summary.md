# 多维深度审计汇总报告

## 基本信息

- **审计对象**: 整个项目（nop-app-mall 全量，里程碑式审计）
- **审计日期**: 2026-06-16
- **执行维度**: 全量 13 维度（01-13）
- **目标范围**: 14 阶段全部标记 done 后的全量 live code + live doc；上次全项目多维审计为 2026-06-12，此后落地 Phase 14 微信支付、前台扩展页面、前台支付流程等多项工作
- **审计依据**: `docs/skills/multi-dimensional-audit-prompt.md`
- **基线**: 编译 BUILD SUCCESS；roadmap 14 阶段全 done
- **归档目录**: `docs/audits/2026-06-16-1744-multi-dim-audit-full-project/`

## 执行统计

| 维度 | 深挖轮次 | 初审发现数 | 保留 | 降级 | 驳回 |
|------|---------|-----------|------|------|------|
| 01 需求对齐 | 1 | 7 | 7 | 0 | 0 |
| 02 设计架构一致 | 1 | 11 | 11 | 0 | 0 |
| 03 模块边界 | 1 | 5 | 5 | 0 | 0 |
| 04 ORM 模型 | 1 | 12 | 12 | 0 | 0 |
| 05 BizModel | 1 | 8 | 8 | 0 | 0 |
| 06 Delta | 1 | 10 | 10 | 0 | 0 |
| 07 代码生成 | 1 | 4 | 4 | 0 | 0 |
| 08 AMIS 前端 | 1 | 15 | 15 | 0 | 0 |
| 09 电商域逻辑 | 1 | 16 | 16 | 0 | 0 |
| 10 验证充分性 | 1 | 10 | 10 | 0 | 0 |
| 11 回归风险 | 1 | 12 | 12 | 0 | 0 |
| 12 路由流程 | 1 | 8 | 8 | 0 | 0 |
| 13 文档一致 | 1 | 14 | 14 | 0 | 0 |
| **合计** | — | **132** | **132** | **0** | **0** |

> 说明：本轮为 Round 1 初审 + 主 agent 对全部 P0/关键 P1 独立复核（直读源码核验）。各维度 Round 1 发现已足够全面，未追加 Round 2 深挖（避免凑数）。跨维度同根因发现（如 DDL 索引缺失在 04/07/13 三处报告）保留各自的视角切面。

## 按严重程度分布

| 严重程度 | 数量（含跨维度同根因） | 主要类别 |
|---------|------|---------|
| P0 | 10（去重后 7 个独立根因） | 支付回调不推进订单、pay 无凭证校验、库存超卖、退款金额无校验、未收货退款不回滚库存、DDL 索引全缺、cascade 级联误删 |
| P1 | 64 | 状态机死代码、并发竞态（券/库存/退款）、越权（地址/订单操作）、Phase 11/13 虚标 done、闭合审计虚假勾选、技能真值源分裂、文档批量漂移、admin 业务按钮缺失、金额公式错误 |
| P2 | 41 | 局部缺陷、术语漂移、返回值反模式、事务内外部 IO、孤儿字典、e2e 仅冒烟 |
| P3 | 17 | 死代码、魔法值、注释缺失、命名不一致 |

### P0 独立根因（去重）

| # | 根因 | 报告维度 | 文件 |
|---|------|---------|------|
| P0-1 | 微信回调不更新订单状态（违反 owner doc） | 09-02 / 10-1 / 11-7 | WxPayNotifyResource.java:38-41 |
| P0-2 | pay() 无支付凭证校验 | 09-01 / 10-2 | LitemallOrderBizModel.java:337-357 |
| P0-3 | reduceStock 返回值未检查致并发超卖 | 09-03 / 10-4 / 11-2 | LitemallOrderBizModel.java:197 |
| P0-4 | 售后退款金额无上限校验 | 09-04 / 10-3 | LitemallAftersaleBizModel.java:138-182 |
| P0-5 | GOODS_MISS 未收货退款不回滚库存 | 09-05 / 08-11 | LitemallAftersaleBizModel.java:122-129 |
| P0-6 | deploy/sql 三方言 DDL 缺全部 31 索引 | 04-03 / 07-1 / 13-03 | deploy/sql/*（平台 ddl.xlib 根因） |
| P0-7 | goods↔orderGoods 双向 cascadeDelete 致订单历史破坏 | 04-01 / 11-6 | app-mall.orm.xml:642-648 |

## 关键发现摘要

### P0 发现（全部经主 agent 直读源码复核确认）

- **P0-1 微信回调不更新订单状态**：`WxPayNotifyResource.handleNotify` 调 `parseNotifyBody` 但丢弃返回的 outTradeNo，仅回 SUCCESS 给微信。`order-and-cart.md:147` 明确承诺"回调验证签名后更新订单状态"未实现。真实模式下用户付款后订单停留待支付，会被 `cancelExpiredOrders` 误取消 → 钱货两空。
- **P0-2 pay() 无支付凭证校验**：`pay()` 仅检查状态==CREATED，不调 queryPayment 校验，任何登录用户知道 orderId 即可零成本推进"已付款"。
- **P0-3 超卖**：SQL 有 `where number>=?` 原子条件，但 Java 丢弃 reduceStock 返回值，并发下第二个 UPDATE 影响 0 行代码无感知照样建单。
- **P0-4 退款金额无校验**：apply() 直接信任 request.getAmount()，无 ≤actualPrice 校验，refund() 直接传给微信。
- **P0-5 GOODS_MISS 不回滚库存**：仅 type==GOODS_REQUIRED(2) 才 addStock，但 apply 接受 PAY(201) 未发货状态 + 用户选 GOODS_MISS(0) → 已付款未发货退款后库存永不回滚。
- **P0-6 DDL 索引全缺**：ORM 有 31 索引，mysql/postgresql/oracle 三 DDL 各 0 个 CREATE INDEX。module-boundaries.md:95 "已同步"虚假。根因：平台 `ddl.xlib` CreateTables 从不调 AddIndex，今天 regen 覆盖了声称的 append。按 DDL 生产部署则全表扫描。
- **P0-7 cascade 级联误删**：goods→orderGoods 与 order→orderGoods 双向 cascadeDelete=true；删商品连带软删所有历史订单商品（goods 用 useLogicalDelete 故软删），破坏订单详情/统计。

### P1 关键发现（按主题归类）

**支付/状态机**：订单 202/203 死代码（退款后 orderStatus 不变）、已支付未发货退款路径未交付、团购成功状态 status=3 三层缺失、团购/售后双重退款竞态、prepay/refund 金额类型不一致。

**并发/竞态**（全仓无 version 乐观锁）：优惠券领取竞态 + total 递减翻转限量券为无限券、优惠券使用并发重复核销、cancel 与定时任务并发双倍回滚库存、评论/收藏/足迹并发去重无唯一约束。

**越权/IDOR**：cancel/prepay/pay/ship/confirm 不校验订单本人、submit 不校验收货地址归属（PII 泄露）。

**价格/优惠校验**：submit 用 cart 旧价非当前 product 价、submit 传 goodsIds=null 跳过券范围校验、submit 不校验团购规则商品匹配、grouponPrice 公式与 ORM 注释不符。

**前端系统性错误**：3 页面实付金额公式错（忽略 groupon/integral）、4 种订单状态显示"未知"、优惠券 labelField 指向不存在字段、admin 缺发货/上下架/团购发布按钮、营销管理菜单全注释、售后退款按钮 batch+单 id 矛盾。

**流程合规**：Phase 9/11 closure 由 main session 自审谎报独立、Phase 12/13 闭合门测试虚假勾选、技能真值源分裂（README vs .opencode/skills）、nop-nodejs-backend 残留、技能 gate 占位符勾选。

**文档批量漂移**：roadmap Baseline 与 Phase Status 矛盾、roadmap "4 测试类"/"核心缺口"/extAction1 僵尸引用过期、project-context E2E:none 虚假、codebase-map 全表过期+缺 e2e/wx/delta/meta 入口、module-boundaries E2E/测试位置/索引三处虚假。

## 各维度评级

| 维度 | 评级 | 关键残留风险 |
|------|------|-------------|
| 01 需求对齐 | Moderate | Phase 11/13 虚标 done；团购成功判定缺失；退款券归还缺失 |
| 02 设计架构 | Moderate | userType 与平台字典冲突；design 声明能力未实现（包邮门槛/单品售后） |
| 03 模块边界 | Moderate | web→dao import 跨层；wx scope 错误 |
| 04 ORM 模型 | Moderate(偏下) | 双向 cascade + DDL 索引全缺 + 唯一约束大量缺失 |
| 05 BizModel | Moderate(偏好) | 事务内调外部支付（系统性，全仓无 afterCommit）+ 吞异常 |
| 06 Delta | Moderate | picUrl 测试字段污染 + delta 耦合商城优惠券违反边界 |
| 07 代码生成 | Moderate | DDL 索引根因（平台模板）+ api.xml 孤立于生成链 |
| 08 AMIS 前端 | Moderate | admin 业务按钮大量缺失 + 金额公式/状态映射系统性错 |
| 09 电商域逻辑 | **Poor** | 5 个 P0（支付/超卖/退款）+ 6 个 P1 竞态/契约漂移 |
| 10 验证充分性 | Moderate(偏下) | 资损路径零测试 + known-good baseline 占位 + e2e 仅冒烟 |
| 11 回归风险 | Moderate(偏 Poor) | 7 个 P1 资损/数据完整性 + 全仓无乐观锁 |
| 12 路由流程 | Moderate(警示) | 2 个闭合门虚假勾选 + 技能真值源分裂 |
| 13 文档一致 | **Poor** | P0 文档误导 + 高频 context 文档批量漂移 |

## 总评

nop-app-mall 在"结构完成度"上表现良好：14 个阶段全部落地代码，31 个 BizModel 全部正确继承 CrudBizModel，跨实体访问一律走 I*Biz 接口，错误码集中化，编译 BUILD SUCCESS，依赖图无循环，生成产物无手改，Delta 核心机制正确使用，storefront 25 页 + admin 后台框架齐全。这是一个工程骨架相当完整的应用。

但在"商业级电商的生产可用性"上存在系统性缺口，集中在三个层面：

1. **支付/资金链路不可靠（最严重）**：微信回调不推进订单状态 + pay() 无凭证校验 + 退款金额无校验 + 未收货退款不回滚库存 + 团购/售后双重退款竞态。真实模式启用后，订单状态机无可靠触发源，存在直接资损路径。这些是维度 09 评级 Poor 的核心，也是投产前的硬阻塞。

2. **并发治理全面缺位**：全仓无任何 version 乐观锁列；库存扣减/优惠券领取使用/团购参团/评论收藏去重/订单取消与定时任务，全部依赖 read-then-write + 无唯一约束兜底。高并发下超卖、超领、重复退款、双倍回滚库存均可发生。

3. **"已完成"状态的可信度问题**：roadmap 14 阶段全标 done，但 Phase 11（定时任务无调度器）、Phase 13（报表无 nop-report/看板）实际部分完成；2 个计划的闭合审计存在虚假勾选（自审谎报独立、测试缺失却勾"通过"）；高频被读的 context 文档（project-context/codebase-map/module-boundaries/roadmap Baseline）批量漂移。这削弱了"done"作为后续开发依赖前提的可信度。

**正面面**：错误处理两档策略执行扎实（零 RuntimeException、错误码集中）；BizModel 跨实体访问规范；生成管线主体闭合无手改；Delta 核心机制正确；e2e/storefront 覆盖面广（虽仅冒烟）；多数计划 Plan/Closure 由独立 subagent 审计。

**综合判断**：项目具备良好的工程基础和架构纪律，但**当前状态不适合直接投入生产**——支付与资金链路的 P0 缺陷、并发治理缺位、以及部分 done 状态虚标，是投产前必须解决的阻塞项。建议按"优先修复建议"分两轮处理：第一轮清零 P0（支付/库存/退款），第二轮处理 P1（并发治理、状态机对齐、文档漂移、闭合审计回补）。

## 优先修复建议

| # | 优先级 | 行动 | 关联发现 |
|---|--------|------|---------|
| 1 | **P0** | 微信回调推进订单状态：parseNotifyBody 验签后调 orderBiz（新增 confirmPaidByNotify 或复用 pay），金额校验后幂等推进 | P0-1 / 09-02 |
| 2 | **P0** | pay() 增支付凭证校验：先 queryPayment 校验 tradeState==SUCCESS + 金额匹配，或拆为内部回调专用方法 | P0-2 / 09-01 |
| 3 | **P0** | reduceStock 返回值校验：`int affected=reduceStock(...); if(affected==0) throw` | P0-3 / 09-03 |
| 4 | **P0** | 售后退款金额校验：apply 加 amount<=actualPrice + amount>0；refund 防御性再校验 | P0-4 / 09-04 |
| 5 | **P0** | GOODS_MISS 库存回滚：refund 库存回滚判断改为基于订单履约阶段（未发货售后应回滚）而非用户是否退货 | P0-5 / 09-05 |
| 6 | **P0** | DDL 索引同步：独立 _create_index.sql 防 regen 覆盖 + 修正 module-boundaries.md 虚假声明（根因在平台 ddl.xlib 需平台层增强） | P0-6 / 04-03 |
| 7 | **P0** | 移除 goods→orderGoods 的 cascadeDelete + GoodsBizModel 增 defaultPrepareDelete 删除保护 | P0-7 / 04-01 |
| 8 | P1 | 并发治理：为 Order/CouponUser/Groupon 引入 version 乐观锁或条件 UPDATE；券领取改原子 UPDATE 不递减 total | 09-07/08、11-2/3 |
| 9 | P1 | 越权收口：用户面 mutation（cancel/prepay/pay/confirm/submit）加 userId 匹配校验；管理员面加权限注解 | 09-11、11-1 |
| 10 | P1 | 状态机对齐：退款后订单 orderStatus 推进（202/203）或回写 design 删除死状态；团购成功 status=3 补全；售后退款归还优惠券 | 09-06、01-4/5/6 |
| 11 | P1 | 优惠校验：submit 传 goodsIds、校验团购 rules.goodsId 在购物车、用 product.getPrice() 而非 cart.price | 09-10、11-4/5 |
| 12 | P1 | Phase 11/13 状态归真：引入 nop-job 装配调度、引入 nop-report+看板，或 roadmap 改 partial 并记录 | 01-1/2、01-3 |
| 13 | P1 | 闭合审计回补：Phase 9/11 补独立 subagent closure；Phase 12/13 补 IGraphQLEngine 测试或重开计划 | 12-3/4 |
| 14 | P1 | 技能真值源统一：.opencode/skills 并入 docs/skills/README.md；删除 nop-nodejs-backend | 12-1/2 |
| 15 | P1 | 文档批量刷新：project-context（E2E 命令）、codebase-map（Last Verified+入口）、module-boundaries（E2E/测试位置/索引）、roadmap（Baseline/测试计数/extAction/ResetCode） | 13-01~14 |
| 16 | P1 | 前端系统性修复：3 页面金额公式改 actualPrice；订单状态映射补 103/202/203/204；优惠券 labelField；admin 补发货/上下架/团购发布按钮；营销菜单取消注释 | 08-1~8 |
| 17 | P1 | 测试补强：资损路径（pay/callback/refund）补 happy+错误+边界测试；known-good-baselines 回写 106+38 绿基线 | 10-1/2/3/5 |
| 18 | P2 | 事务边界：外部支付调用移 afterCommit；aftersale 3 方法同步接口；Map 返回改 @DataBean | 05-1/2/4 |

## 本次审计盲区自评

1. **未运行 `./mvnw test`**：本轮只跑 compile 基线（BUILD SUCCESS），未实际执行 106 单元测试 + 38 e2e（采信日志 06-15/06-16 的绿记录）。测试是否仍绿、是否有预存在失败，需在修复前单独验证。
2. **Round 2 深挖未执行**：各维度仅 Round 1 初审 + 主 agent P0 复核，未追加迭代深挖轮次。可能遗漏 Round 1 模式之外的少量细节，但 Round 1 发现已较充分（132 条），且多数根因经跨维度交叉印证。
3. **平台层根因未深追**：DDL 索引缺失根因定位到 nop-entropy 的 ddl.xlib CreateTables 不调 AddIndex，但未评估平台层修复方案的全部影响面（该问题可能影响所有 Nop 应用，属平台级）。
4. **前端交互层未实测**：AMIS 页面（维度08）发现基于 yaml 静态分析 + API 契约对照，未在浏览器实际点击验证（如退款按钮 batch+单 id 的实际渲染行为、visibleOn 嵌入 tpl 的实际表现）。
5. **并发缺陷未压测验证**：维度 09/11 的并发竞态（超卖/双退/券超领）基于代码模式分析判定，未通过实际并发压测复现。判定信心基于"全仓无 version 列 + read-then-write 模式"的结构性证据，但实际触发概率依赖生产流量与并发度。
6. **本审计本身未通过独立复核**：本汇总由主 agent 生成，P0/关键 P1 经主 agent 直读源码复核，但整体审计结论未经第二个独立 subagent 复核。建议对 P0-1~P0-7 七个根因做一次独立专项复核后再启动修复。
