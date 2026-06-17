# 维度 06：Delta 定制合规性

> 审计日期：2026-06-16 | 审计对象：整个项目 | Round 1 初审 + 主 agent 复核

## 第 1 轮（初审）发现

### [维度06-1] app.orm.xml Delta 用 `raw:` 前缀而非 `x:extends="super"`，偏离 delta-customization.md 强制规则 — P2
- **文件**: `app-mall-delta/.../orm/app.orm.xml:2-3`
- **现状**: `x:extends="raw:/nop/auth/orm/app.orm.xml,default/_app.orm.xml"`；delta-customization.md:9 要求 super
- **建议**: 确认 codegen-bridge 场景 raw: 是否合法例外，或改 super

### [维度06-2] 生产 Delta 残留 `x:dump="true"` 调试标记 — P2
- **文件**: `app-mall-delta/.../orm/app.orm.xml:3`
- **建议**: 删除

### [维度06-3] Delta ORM 含未文档化 picUrl 测试字段，违反 design propId 范围 — P1
- **文件**: `app-mall-delta/.../orm/default/_app.orm.xml:48-49`、`model/nop-auth-delta.orm.xml:39-40`
- **证据**: `<column code="PIC_URL" displayName="测试图片" propId="101" .../>`；design 声明 propId>=102 且字段表只列 4 个
- **风险**: GraphQL 表面污染；升级 propId 冲突；displayName"测试图片"暴露调试残留
- **建议**: 删除 picUrl 或补入 user-and-address.md 并改业务化 displayName
- **复核状态**: 已保留（与 13-12 同源）

### [维度06-4] app-mall-delta 跨模块依赖 app-mall-dao + signUp 耦合商城优惠券，违反 module-boundaries.md — P1
- **文件**: `app-mall-delta/pom.xml:25-29`、`LoginApiExBizModel.java:3-7,82-89,233-252`
- **证据**: delta 依赖 app-mall-dao（边界允许列表外）；signUp() 在认证路径内调 couponUserBiz.claimCouponForUser
- **风险**: 认证路径脆弱（缺优惠券 bean 则 LoginApiExBizModel 不可构造→signUp 503）；module-boundaries.md 声明 delta 允许依赖仅 nop-auth
- **建议**: dispatchRegistrationCoupons 解耦为领域事件（UserRegisteredEvent）由 service 监听

### [维度06-5] 17 个 xmeta Delta 全量复制原始元数据，升级静默失配风险 — P2
- **文件**: `NopAuthRole.xmeta` 等 17 套（`x:extends="super,default/_*.xmeta"`）
- **风险**: nop-auth 升级调整字段被本地复制版覆盖
- **建议**: 升级流程文档化（nop-auth 版本变更须重跑 Delta codegen）

### [维度06-6] module-meta.json Delta 用本地 `_module-meta.json` 而非 super，全量替换 — P2
- **文件**: `model/module-meta.json:2`、`model/_module-meta.json:1-6`

### [维度06-7] `_dao.beans.xml` Delta 空 no-op — P3
### [维度06-8] `NopAuthUser.xbiz` Delta 空 no-op — P3
### [维度06-9] delta-local `INopAuthUserBiz` 与原接口同名平行 — P3
### [维度06-10] `NopAuthDaoConstants`/`_NopAuthDaoConstants` 空占位 — P3

## 正面发现
NopAuthUser.view.xml 教科书级 Delta（bounded-merge+remove 敏感字段）；bean 替换标准；实体 Delta 模式合规；signUp 密码处理经继承管道 defaultPrepareSave 正确哈希；公开端点鉴权正确（signUp publicAccess，getMyProfile 默认认证）；方法名无冲突。

## 维度复核结论
主 agent 复核：06-3 picUrl 经 design 字段表对照确认；06-4 delta→dao 依赖 + signUp 耦合优惠券经 pom+代码确认。10 项保留。

## 最终保留项

| 编号 | 严重 | 文件 | 一句话摘要 |
|------|------|------|-----------|
| 06-3 | P1 | nop-auth-delta.orm.xml:39 | picUrl 测试字段污染 |
| 06-4 | P1 | LoginApiExBizModel.java:82 | delta 耦合商城优惠券违反边界 |
| 06-1 | P2 | orm/app.orm.xml:2 | raw: 而非 super |
| 06-2 | P2 | orm/app.orm.xml:3 | x:dump 调试残留 |
| 06-5 | P2 | 17 个 xmeta | 全量复制升级失配风险 |
| 06-6 | P2 | module-meta.json | 全量替换 |
| 06-7 | P3 | _dao.beans.xml | 空 no-op |
| 06-8 | P3 | NopAuthUser.xbiz | 空 no-op |
| 06-9 | P3 | INopAuthUserBiz | 同名平行接口 |
| 06-10 | P3 | NopAuthDaoConstants | 空占位 |

## 维度评级：Moderate

无 P0（Delta 未引入认证/权限失效）；核心 Delta 机制（x:extends/bounded-merge/bean replace/实体扩展）全部正确使用。2 项 P1（picUrl 未文档化字段+跨模块耦合）反映 Delta 边界管控有漏洞；4 项 P2 升级兼容 watch 项需文档化。
