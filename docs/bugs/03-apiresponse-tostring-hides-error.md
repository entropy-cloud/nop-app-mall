# 03 ApiResponse/ApiRequest 默认 toString 只输出对象 hash，业务错误信息对调试完全不可见

## Problem

- GraphQL mutation（及任何返回 `ApiResponse` 的接口）失败时，测试断言消息 `"failed: " + response` 只打印 `ApiResponse@<hash>`，看不到 `msg/code/errors/data`。
- 影响范围：**全局**。所有 `ApiResponse`/`ApiRequest` 的日志、断言失败、调试器观察都受影响。本次直接导致 mission-driver 的 build agent 在「定位 submit 失败真因」上卡死 23 分钟无法推进。
- 严重性：高（调试可见性）。本身不破坏运行时正确性，但让任何 ApiResponse 相关的故障诊断成本极高，会放大一切底层 bug 的排查难度（bug 01 就是被它放大的典型案例）。

## Reproduction

- 环境：任意调用 GraphQL/ApiResponse 的代码。
- 触发步骤：构造一个失败的 `ApiResponse`（`status=-1`，`msg`/`code` 非空），执行 `"x" + response` 或 `response.toString()`。
- 最小复现：
  ```java
  ApiResponse<Object> fail = new ApiResponse<>();
  fail.setStatus(-1);
  fail.setMsg("实体对象...的非空属性[picUrl]为null");
  System.out.println("failed: " + fail);
  ```
  改前输出：`failed: io.nop.api.core.beans.ApiResponse@4538fda7`（msg 完全不见）。

## Diagnostic Method

- 诊断难度：低（机制清晰），但「影响确认」需查全局。
- 第一步排查：确认 `ApiResponse` 继承自 `ApiMessage`，整个继承链（`ApiResponse` → `ApiMessage` → `Object`）**均未重写 `toString()`**，因此 `"x" + response` 走 `Object.toString()` 默认实现 `getClass().getName() + "@" + Integer.toHexString(hashCode())`，只输出类名+hash，不读任何字段。
- 验证影响面：grep 两仓库测试，确认无断言依赖旧的 `ApiResponse@hash`/`ApiRequest@hash` 输出（`nop-api-core` 与 `nop-app-mall` 均无），故重写 toString 安全。
- 决定性证据：bug 01 的事故现场——build agent 反复 grep surefire `system-out`/`system-err`（空）、临时加 `System.out.println`（被 surefire 重定向吞掉）、反复编译跑单测，都因为「错误信息物理上在 ApiResponse 对象里但 toString 不输出」而看不到，最终只能靠直接读 surefire `.txt` 报告的 `AssertionFailedError` 行绕过 toString 才拿到真因。

## Root Cause

- **机制**：`ApiMessage`（`ApiResponse`/`ApiRequest` 的基类）未重写 `toString()`，`Object.toString()` 默认只返回 `类名@hash`。字段（`status/code/msg/errors/data/headers`）物理存在于对象中，但 `toString()` 不读它们。Java 的 `"str" + obj` 隐式调用 `obj.toString()`，于是拼接错误消息时真实错误内容对调用方完全不可见。
- **为何是平台级问题**：`ApiResponse` 是所有 GraphQL/接口的统一返回包装，被日志、断言、异常消息广泛拼接，缺 toString 让整个调试链路「失明」。

## Fix

- 在 nop-entropy（`nop-kernel/nop-api-core`）重写 toString，遵循现有 `ErrorBean.toString()` 的 `StringBuilder` + `BeanName[field=value]` 约定：
  - `ApiMessage`：新增 `protected appendHeaders(StringBuilder)`（仅 `hasHeaders()` 时追加 `,headers={...}`）和 `protected static truncate(Object,int)`（data 等大字段截断到 200 字符，标 `...(原长度)`），供子类复用。
  - `ApiResponse.toString()`：`ApiResponse[status=,httpStatus=?,code=,msg=,errors=,data=(截断),headers=]`，空字段跳过。
  - `ApiRequest.toString()`：`ApiRequest[data=(截断),selection=,properties=,headers=]`。
- 设计意图：让核心字段在断言失败/日志/调试器中直接可见；`data` 截断防日志爆炸，注释提示「需完整内容用 `JsonTool.stringify`」。header 输出放基类，Request/Response 共用。

## Tests

- 无新增自动化测试（toString 输出格式属实现细节，不值得固化断言；格式可能随字段演进）。
- 手动验证（已执行，代码已移除）：写临时 `ToStringSmoke` 主类构造 success/failure/headers/big-data 四种 ApiResponse + 空/带数据 ApiRequest，确认输出含 `status/code/msg/headers` 且大 data 截断为 `...(500)`。
- 回归验证：`mvn test -pl nop-kernel/nop-api-core` → `Tests run: 38, Failures: 0`；`mvn install` 后 nop-app-mall 全量 `mvn test` → `Tests run: 123, Failures: 0`。无回归。

## Affected Artifacts

- `nop-entropy/nop-kernel/nop-api-core/src/main/java/io/nop/api/core/beans/ApiMessage.java` - 新增 `appendHeaders` / `truncate`
- `nop-entropy/nop-kernel/nop-api-core/src/main/java/io/nop/api/core/beans/ApiResponse.java` - 重写 `toString()`
- `nop-entropy/nop-kernel/nop-api-core/src/main/java/io/nop/api/core/beans/ApiRequest.java` - 重写 `toString()`
- （改动在 nop-entropy 工作树，日志记于 `nop-entropy/ai-dev/logs/2026/06-27.md`）

## Notes For Future Refactors

- **不变量**：`ApiResponse`/`ApiRequest` 的 `toString()` 必须暴露 `status/code/msg/errors/data/headers` 等核心字段，禁止回退到默认 `Object.toString()`。如果有人重构时误删 toString 或新增同类 message bean 忘记重写，会复现「错误被吞进 hash」。
- **测试断言规范**：即便有了可读 toString，测试断言失败消息仍建议显式 `JsonTool.stringify(response)` 获取完整 data（toString 会截断大 data）；不要依赖 `+ response` 能看到所有嵌套内容。
- **联动**：本 bug 是 bug 01 诊断耗时 23 分钟的直接根因。toString 修复后，同类 ApiResponse 失败可一眼定位，不再需要「绕到 surefire .txt 报告」。

## Prevention Gap

- 平台未强制 `@DataBean` 类重写 toString；可考虑在 codegen 模板里为 `@DataBean`/继承 `ApiMessage` 的类默认生成可读 toString，从源头杜绝此类问题。
