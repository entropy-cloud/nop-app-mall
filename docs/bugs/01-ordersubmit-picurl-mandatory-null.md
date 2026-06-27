# 01 Order submit 失败：OrderGoods.picUrl 非空校验因测试 fixture 用普通 URL 而非文件引用

## Problem

- 通过 GraphQL `LitemallOrder__submit` 提交订单时返回 `status=-1`，订单无法创建。
- 影响测试：`TestLitemallOrderBizModel`（8）、`TestLitemallAftersaleBizModel`（5）、`TestLitemallCommentBizModel`（6）、`TestLitemallGrouponBizModel`，合计 ~19 个 case 失败。这些测试覆盖下单/支付/售后/评论/团购主流程，等于核心交易链路全断。
- 错误表面信息：`submit failed: io.nop.api.core.beans.ApiResponse@<hash>`（断言消息不可读，见 bug 03）。

## Reproduction

- 环境：`mvn test -pl app-mall-service`，H2 内存库 + autotest 框架。
- 触发：任何调用 `LitemallOrder__submit` 的测试，或测试 fixture 直接 `saveEntity(LitemallOrderGoods)`。
- 最小复现：在 `TestLitemallOrderBizModel.setUp` 中 `cart.setPicUrl("http://test.com/cart-pic.png")`，然后跑 `testSubmitAndPay`，submit 返回 -1。
- 真实报错（需读 surefire `.txt` 报告才看得到）：`实体对象[订单商品表:<id>]的非空属性[商品/货品图片(picUrl)]为null`。

## Diagnostic Method

- 诊断难点：GraphQL 引擎把业务异常吞进 `ApiResponse` 字段，JUnit 断言消息 `"submit failed: " + response` 走 `ApiResponse.toString()`，而 `ApiMessage` 继承链未重写 `toString()`，只输出 `ApiResponse@<hash>`，错误字段（`msg/code/errors`）完全不可见（详见 bug 03）。
- 第一步排查：直接读 `app-mall-service/target/surefire-reports/*.txt` 的 `AssertionFailedError` 行，绕过 toString，拿到真因：`非空属性[picUrl]为null`。
- 排除的假设：
  - 假设 A「`submit` 没初始化 picUrl」—— 读 `LitemallOrderBizModel.java:205` 发现有 `orderGoods.getPicUrlComponent().copyFrom(item.getPicUrlComponent())`，submit 确实在拷贝，排除。
  - 假设 B「模型错误地给 picUrl 加了 `mandatory=true`」—— `git show 57a7784~1:model/app-mall.orm.xml` 证明 `OrderGoods.picUrl` 在该 commit 之前就是 `domain="image" mandatory="true"`，是既有设计，排除「误加 mandatory」。
- 决定性证据：读平台 `OrmFileComponent.copyFrom`（`nop-entropy`）—— `copyFrom` 调 `source.getFileId()`，经 `DaoResourceFileStore.decodeFileId` 解析文件链接；`domain="image"` 列存的是**文件引用** `/f/download/{fileId}`（`FileConstants.PATH_DOWNLOAD = "/f/download"`），不是普通 URL。测试 fixture 写的是 `http://test.com/cart-pic.png`，`decodeFileId` 因不以 `/f/download/` 开头返回 null → `copyFrom` 静默不赋值 → save 撞 mandatory。

## Root Cause

- **机制**：`domain="image"` / `stdDomain="file"` 的列底层是 `OrmFileComponent`，值必须是文件链接（`/f/download/{fileId}`，指向 `NopFileRecord`）。测试 fixture 误用普通 HTTP URL，组件解析失败后 `copyFrom` **静默丢弃**（无异常、无日志），最终 mandatory 非空校验才暴露。
- **为何长期未发现**：commit `57a7784`（ORM 模型扩展）之前 `mvn clean install` 一直因 codegen 引用已删除的 `LitemallUser` 而失败，build 坏掉掩盖了这批测试失败；build 修好后才暴露。

## Fix

- 把 4 个测试 fixture 里 cart / orderGoods 的 picUrl 从普通 URL 改为与已注册 `NopFileRecord` 对应的文件链接 `/f/download/{fileId}`，使 `OrmFileComponent.copyFrom` 能正确解析。
- 设计意图：fixture 的 `NopFileRecord` 已用 `fileId="cart-pic"` 等注册，picUrl 值只需用 `getFileLink(fileId)` 的产物即 `/f/download/{fileId}`，与平台文件域语义对齐。
- 不改 `submit` / 不改模型——业务代码与 mandatory 设计均正确，错在 fixture 用错了值的类型。

## Tests

- `app-mall-service/src/test/java/app/mall/service/entity/TestLitemallOrderBizModel.java` - cart picUrl 改为文件引用（level: component/integration，GraphQL + H2）
- `app-mall-service/src/test/java/app/mall/service/entity/TestLitemallAftersaleBizModel.java` - 同上
- `app-mall-service/src/test/java/app/mall/service/entity/TestLitemallCommentBizModel.java` - 2 处 cart picUrl
- `app-mall-service/src/test/java/app/mall/service/entity/TestLitemallGrouponBizModel.java` - orderGoods 直接保存的 picUrl
- 验证：`mvn test` → `Tests run: 123, Failures: 0, Errors: 0`，BUILD SUCCESS

## Affected Artifacts

- `app-mall-service/src/test/java/app/mall/service/entity/TestLitemallOrderBizModel.java:111` - cart picUrl → `/f/download/cart-pic`
- `app-mall-service/src/test/java/app/mall/service/entity/TestLitemallAftersaleBizModel.java:116` - 同上
- `app-mall-service/src/test/java/app/mall/service/entity/TestLitemallCommentBizModel.java:107,201` - cart-pic-1 / cart-pic-2
- `app-mall-service/src/test/java/app/mall/service/entity/TestLitemallGrouponBizModel.java:125` - orderGoods groupon-goods-pic

## Notes For Future Refactors

- **不变量**：任何 `domain="image"` / `stdDomain="file"` 列（picUrl/url/shareUrl 等，见 `model/app-mall.orm.xml` 共 14 处）的值必须是 `/f/download/{fileId}` 文件链接，不能是普通 URL 或空字符串。如果有人把测试 fixture 改回 `http://...` 或生产代码里给这类字段塞外链，会复现「值在但 copyFrom 静默丢弃 → mandatory 报错」。
- **易错点**：`OrmFileComponent.copyFrom` 在源 fileId 为空/不可解析时**静默返回不报错**，调试时容易误以为「没拷贝过」。涉及文件域字段复制时，必须确认源值是合法文件链接。
- **联动**：bug 03（ApiResponse.toString 吞异常）是本次诊断耗时的直接放大器；若 toString 早已暴露 msg，本 bug 会被立刻定位。

## Prevention Gap

- 无静态校验阻止给 `domain="image"` 列赋普通 URL；可考虑在测试基类或 lint 规则里增加「文件域字段值必须匹配 `/f/download/` 前缀」的断言。
