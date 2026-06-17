# 维度 05：BizModel 与服务层规范

> 审计日期：2026-06-16 | 审计对象：整个项目 | Round 1 初审 + 主 agent 复核

## 第 1 轮（初审）发现

### [维度05-1] LitemallAftersaleBizModel 三个 public 方法未同步到 ILitemallAftersaleBiz 接口 — P1
- **文件**: `LitemallAftersaleBizModel.java:62,78,96`；`ILitemallAftersaleBiz.java:15-31`
- **证据**: batchApprove/batchReject/refund 仅实现类有，接口无 @Override 声明
- **风险**: 违反"public 方法必须同步 I*Biz"规则；跨模块代理调用会 unsupported-method
- **建议**: 接口补声明 + 加 @Override

### [维度05-2] 事务内调用外部支付服务（prepay/refund），事务边界错误，潜在资损 — P1
- **文件**: `LitemallOrderBizModel.java:308-335`（prepay:326）、`LitemallAftersaleBizModel.java:96-134`（refund:111）、`LitemallGrouponBizModel.java:212-260`（refundGrouponOrder:231）
- **证据**: 三处 @BizMutation（自动事务）内直接调 PayService 远程服务；全仓库 grep afterCommit 0 命中
- **风险**: 外部退款成功后 DB 更新失败则事务回滚→钱已退但状态未变/库存未补；长事务持锁
- **建议**: 外部支付调用移到 afterCommit 或事务边界外，补对账/补偿
- **复核状态**: 已保留（与 09-13 同源；事务边界是系统性缺口）

### [维度05-3] LitemallGrouponBizModel.refundGrouponOrder 吞异常静默跳过 — P1
- **文件**: `LitemallGrouponBizModel.java:225-239`
- **证据**: `catch(Exception e){ LOG.error(...); return; }` 静默吞掉；expireGroupons 调用方无感知
- **风险**: 违反 error-handling.md 禁止吞异常；批量退款个别失败被忽略，无告警/重试台账
- **建议**: 退款失败抛 NopException 中断批量并记录明细

### [维度05-4] LitemallOrderBizModel.prepay 返回 Map<String,Object>（返回值反模式） — P2
- **文件**: `LitemallOrderBizModel.java:308,331-334`
- **建议**: 定义 @DataBean PrepayResultBean 替换

### [维度05-5] Category.getCategoryTree/Region.getRegionTree 返回 List<Map<String,Object>> — P2
- **文件**: `LitemallCategoryBizModel.java:36,48,55-71`；`LitemallRegionBizModel.java:26-60`
- **现状**: Region 还用 @SuppressWarnings unchecked 强转 children

### [维度05-6] LitemallCategoryBizModel 用 dao().findAllByQuery 绕过 doFindList 预处理 — P2
- **文件**: `LitemallCategoryBizModel.java:39,78`

### [维度05-7] @SqlLibMapper 注入多处缺少"使用原因"注释 — P3
- **文件**: `LitemallGrouponBizModel.java:54-55`、`LitemallGoodsBizModel.java:35-36`、`LitemallOrderBizModel.java:101-102`

### [维度05-8] 错误码命名子域不一致（coupon vs coupon-user） — P3
- **文件**: `AppMallErrors.java:192-194`（ERR_COUPON_NOT_USABLE 落 coupon-user 子域）

## 正面发现
31 BizModel 全部 extends CrudBizModel<T>+setEntityName()；零 @Transactional 重复事务；零 throw new RuntimeException；零 @Inject private；跨实体访问一律注入 I*Biz；错误码集中化；@SqlLibMapper 仅用于原子库存 SQL 与统计聚合。

## 维度复核结论
主 agent 复核：05-2 事务边界系统性缺口与 09-13 印证；afterCommit 0 命中独立 grep 确认。8 项保留。

## 最终保留项

| 编号 | 严重 | 文件 | 一句话摘要 |
|------|------|------|-----------|
| 05-1 | P1 | LitemallAftersaleBizModel.java:62,78,96 | 3 方法未同步接口 |
| 05-2 | P1 | OrderBizModel:326 等 | 事务内调外部支付 |
| 05-3 | P1 | LitemallGrouponBizModel.java:225 | 吞异常静默跳过 |
| 05-4 | P2 | OrderBizModel.java:308 | prepay 返回 Map 反模式 |
| 05-5 | P2 | Category/RegionBizModel | 树接口返回 Map |
| 05-6 | P2 | CategoryBizModel.java:39 | 绕过 doFindList |
| 05-7 | P3 | 多处 | @SqlLibMapper 缺注释 |
| 05-8 | P3 | AppMallErrors.java:192 | 错误码子域不一致 |

## 维度评级：Moderate（中等偏好）

核心结构（CrudBizModel 继承、I*Biz 跨实体、ErrorCode 两档策略）执行扎实；但事务边界与外部副作用编排是系统性缺口（三处外部支付裸跑事务内，全仓无 afterCommit），需生产前优先修复 P1。
