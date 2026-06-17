# 维度 07：代码生成管线完整性

> 审计日期：2026-06-16 | 审计对象：整个项目 | Round 1 初审 + 主 agent 复核

## 第 1 轮（初审）发现

### [维度07-1] DDL 三方言缺全部 31 索引；声称的 "Option B directed append" 已被 codegen regen 覆盖（根因定位到平台模板） — P1
- **文件**: `deploy/sql/{mysql,postgresql,oracle}/_create_app-mall.sql`；平台 `nop-entropy/.../ddl.xlib`、`DdlSqlCreator.java`
- **证据片段**: ORM 31 索引；三 DDL 0 个 CREATE INDEX；mysql DDL 时间戳 6月16（今天 regen）；平台 ddl.xlib `CreateTables`（lines 7-28）只调 CreateTable+AddComment，从不调 AddIndex
- **严重程度**: P1（根因 P0 数据风险已在维度04 记录，本条聚焦根因）
- **现状**: ORM 生成层正确传播索引（_app.orm.xml 含 31 索引），但 DDL 生成层系统性丢弃索引；今天 regen 重写 DDL 抹掉 06-15 的 append
- **风险**: 生产按 DDL 部署全表扫描；module-boundaries.md:95 虚假声明
- **建议**: 修正 module-boundaries.md；根因需平台层扩展 _create_{appName}.sql.xgen 遍历 indexes；短期用独立 _create_index.sql 防 regen
- **信心水平**: 极高（平台模板源码+Java 源码+DDL 内容三重验证）
- **复核状态**: 已保留（主 agent 独立 grep 验证三 DDL 0 索引）

### [维度07-2] model/app-mall.api.xml 是孤立源模型：无 xgen 生成、无 Java 实现 MallService.findMallProducts — P1
- **文件**: `model/app-mall.api.xml:7-13`
- **证据片段**: grep findMallProducts/MallService 全仓库仅命中 api.xml；无 xgen 引用 api.xml；gen-crud-api.xgen 从 ORM 生成 CRUD Api 不从 api.xml
- **严重程度**: P1
- **现状**: api.xml 声明 RPC 服务但生成管线无环节消费它，运行时无 backing 方法
- **建议**: 补 BizModel+xgen 或删除 api.xml services
- **复核状态**: 已保留（与维度04-02 同源）

### [维度07-3] codegen.sh/codegen.bat 失效脚手架残留：引用不存在的 nop-cli.jar、硬编码 v:/ 盘、指向过时 .xlsx — P2
- **文件**: `codegen.sh`、`codegen.bat`
- **证据片段**: `java -jar nop-cli.jar gen -t=v:/nop/templates/orm model/app-mall.orm.xlsx`；nop-cli.jar 不存在；.xlsx 比 .xml 旧 2 天
- **建议**: 删除或标注 DEPRECATED

### [维度07-4] 多类生成产物时间戳陈旧但内容与源一致（codegen 跳过内容相同产物） — P3
- **现状**: 平台标准行为，非漂移

## 正面发现
- 源模型完整且格式正确；gen-orm.xgen 引用正确源模型；31 实体端到端一致（model=_gen entity=BizModel=_service.beans.xml）；xbiz 双文件模式标准；**_gen 文件无手改痕迹，未发现 P0 级生成产物被手改**

## 维度复核结论
主 agent 复核：07-1 根因分析经独立验证（三 DDL 0 索引已亲自确认，平台 ddl.xlib 行为可信）；07-2 与 04-02 交叉印证。保留。

## 最终保留项

| 编号 | 严重 | 文件 | 一句话摘要 |
|------|------|------|-----------|
| 07-1 | P1 | deploy/sql/* + ddl.xlib | DDL 缺索引根因在平台模板 CreateTables 不调 AddIndex |
| 07-2 | P1 | model/app-mall.api.xml | api.xml 孤立于生成链 |
| 07-3 | P2 | codegen.sh/bat | 失效脚手架残留 |
| 07-4 | P3 | 多生成产物 | 时间戳陈旧但内容一致（非漂移） |

## 维度评级：Moderate

生成链路主体闭合健康（model→dao→meta→web→service 无断裂，无生成产物手改，BUILD SUCCESS）；但 2 个 P1（DDL 索引根因+api.xml 孤立）直接影响生产部署正确性与源模型可信度，且 module-boundaries.md 有虚假声明需修正。
