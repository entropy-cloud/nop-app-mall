# app-mall

#### 介绍
用于演示Nop平台基本开发流程的示例工程。功能设计与数据结构设计来自于开源项目[litemall](https://github.com/linlinjava/litemall)。

#### 软件架构
软件架构说明
1. model/app-mall.orm.xlsx Excel格式的数据模型
2. app-mall-api 对外暴露的服务接口
3. app-mall-codegen maven打包时根据app-mall.orm.xlsx来自动生成后台工程代码
4. app-mall-dao 后台数据库访问代码以及实体代码
5. app-mall-service  后台服务的实现代码
6. app-mall-web 前端页面对应的JSON和JS代码
7. deploy 根据数据模型自动生成的数据库建表语句
8. nop-cli.jar 代码生成工具


#### 安装教程

环境准备： JDK 11+、Maven 3.6+、Git

```shell
git clone https://gitee.com/canonical-entropy/app-mall.git
cd app-mall
mvn clean install -DskipTests -Dquarkus.package.type=uber-jar
```

* 以调试模式启动
```shell
java -Dfile.encoding=UTF8 -Dquarkus.profile=dev -jar app-mall-app/target/app-mall-app-1.0-SNAPSHOT-runner.jar 
```

服务链接http://localhost:8080，用户名nop，密码123



#### 使用说明

1.  xxxx
2.  xxxx
3.  xxxx

