# 维度 03：模块边界与依赖

> 审计日期：2026-06-16 | 审计对象：整个项目 | Round 1 初审 + 主 agent 复核

## 完整依赖图摘要
```
codegen → [nop-ooxml-xlsx, nop-orm, nop-auth-dao, nop-xlang-debugger]（纯平台）
api     → [nop-api-core]
dao     → [nop-api-core, nop-orm] + codegen(test)
service → dao, meta, wx + delta(test), codegen(test) + 平台
web     → service + codegen(test) + 平台
app     → delta, service, web + 平台
wx      → api + 平台 + wechatpay-java + quarkus-resteasy
delta   → nop-auth-dao, nop-auth-service + dao
meta    → (main 无依赖；codegen/dao test)
```
**循环依赖：无。** 7/9 模块完全合规。

## 发现列表

### [维度03-1] web 层 view.xml 直接引用 dao 层 Java 类，违反规则 4 — P1
- **文件**: `app-mall-web/.../LitemallAftersale/LitemallAftersale.view.xml:6`
- **证据**: `<c:import class="app.mall.dao.AppMallDaoConstants" />`；AppMallDaoConstants 在 dao 模块
- **现状**: web pom 未声明 dao（合规），但通过 service→dao 传递链，dao 类在 web 类路径可被 XLang 直接 import；仅此 1 文件
- **风险**: web 绑定 dao 常量，绕过 service 抽象；dao 重命名时 web 运行时才解析失败
- **建议**: 常量提升至 api 层，或 service 暴露

### [维度03-2] service 将 wx 声明为 compile scope，但仅测试代码使用 — P2
- **文件**: `app-mall-service/pom.xml:35-39`
- **证据**: service main grep `app.mall.wx|WxPay` 0 匹配；wx 仅 3 测试文件引用；无 `<scope>test</scope>`
- **建议**: wx 改 test scope，app 显式声明 wx（运行时 IoC bean 提供者）

### [维度03-3] module-boundaries.md 声称集成测试在 app-mall-app/src/test/，实际不存在 — P2
- **文件**: `module-boundaries.md:84`
- **现状**: app-mall-app 无 src/test；28 个集成测试在 app-mall-service

### [维度03-4] service 声明 meta 为 compile scope，但 meta 无 Java 代码（纯资源模块） — P3
- **现状**: 规则 3 service 允许列表未列 meta，但 meta 仅资源；属架构灰区

### [维度03-5] MockPayServiceImpl 为未注册死代码 — P3
- **文件**: `app-mall-service/.../pay/MockPayServiceImpl.java`
- **现状**: 无 @Named/@BizModel/beans.xml 注册/调用；system-baseline.md:69 称"不涉及 MockPayServiceImpl"

## 维度复核结论
主 agent 复核：依赖图无循环确认；03-1 web→dao import 经 grep 单点命中确认；03-2 service main 无 wx 引用确认。5 项保留。

## 最终保留项

| 编号 | 严重 | 文件 | 一句话摘要 |
|------|------|------|-----------|
| 03-1 | P1 | LitemallAftersale.view.xml:6 | web 直接 import dao 常量类 |
| 03-2 | P2 | app-mall-service/pom.xml:35 | wx scope 错误（应 test） |
| 03-3 | P2 | module-boundaries.md:84 | 集成测试位置文档虚假 |
| 03-4 | P3 | app-mall-service/pom.xml:22 | service→meta 灰区 |
| 03-5 | P3 | MockPayServiceImpl.java | 死代码 |

## 维度评级：Moderate

架构骨架健康：依赖图无循环，pom 层面无模块违反显式禁止列表，7/9 模块完全合规。存在 1 处代码层跨层违规（web→dao import）+ scope 错误 + 文档漂移，需修复以维持边界文档可信度。
