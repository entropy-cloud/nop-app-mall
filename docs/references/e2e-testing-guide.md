# E2E Testing Guide

## 框架

基于 Playwright，测试代码在 `e2e/` 目录。

```bash
cd e2e
npm install
npm run install:browsers   # 首次需要安装 Chromium
npm test                   # 运行全部 E2E 测试
```

## 架构

Playwright 的 `webServer` 配置（`e2e/playwright.config.ts`）会自动启动 Quarkus 应用并等待端口就绪后再执行测试。

关键设计：**E2E 测试使用内存数据库**，通过命令行参数覆盖 `application.yaml` 中的文件型 H2 配置：

```
-Dnop.datasource.jdbc-url=jdbc:h2:mem:e2e
```

这样 E2E 测试启动的应用实例不会与开发环境正在运行的应用实例产生 H2 文件锁冲突。

> 配置覆盖机制详见 nop-entropy `docs-for-ai/02-core-guides/ioc-and-config.md` → "通过命令行参数覆盖配置"。

## 测试文件

| 文件 | 验证内容 |
|------|---------|
| `e2e/tests/app-startup.spec.ts` | 根页面加载、GraphQL 端点响应 |
| `e2e/tests/storefront-pages.spec.ts` | 商城前台各页面 JSON 是否正常返回 |

## 常见问题

### 测试启动后一直无输出

如果 Playwright 启动后长时间无输出，最常见的原因是 Java 应用启动失败但 Playwright 在等待端口就绪（最长 180 秒）。排查方式：

1. 检查是否已有应用实例占用了文件型 H2 数据库锁（`db/test.mv.db`）
2. 手动运行启动命令查看报错
3. 确认 `app-mall-app/target/quarkus-app/quarkus-run.jar` 已构建

### 手动验证启动命令

```bash
java -Dfile.encoding=UTF8 -Dquarkus.profile=dev \
     -Dquarkus.http.port=8086 \
     -Dnop.datasource.jdbc-url=jdbc:h2:mem:e2e \
     -Dnop.orm.init-database-schema=true \
     -jar app-mall-app/target/quarkus-app/quarkus-run.jar
```

如果这条命令能正常启动（日志出现 `started in` 和 `Listening on`），说明 Playwright webServer 配置无误。

### 跳过自动启动 webServer

如果已经手动启动了应用，可以跳过 webServer 自动启动：

```bash
SKIP_WEBSERVER=1 BASE_URL=http://localhost:8086 npm test
```
