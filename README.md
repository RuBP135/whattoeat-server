# WhatToEat Server

WhatToEat2 的后端服务，为 Android 与 iOS 客户端提供菜品及点评相关的数据接口。

## 主要功能

- 提供包含用户点评信息的菜品列表
- 接收并管理用户上传的菜品
- 处理菜品评分与评论的提交和查询

> 项目目前处于初始开发阶段，业务接口和数据持久化功能仍在完善中。

## 技术栈

- Java 21
- Spring Boot 4.1
- Spring Web MVC
- Jakarta Bean Validation
- Maven

## 本地运行

### 环境要求

- JDK 21+
- 无需单独安装 Maven，项目已包含 Maven Wrapper

### 启动服务

Linux、macOS 或 WSL：

```bash
./mvnw spring-boot:run
```

Windows：

```bat
mvnw.cmd spring-boot:run
```

服务默认运行在 `http://localhost:8080`。

## 构建与测试

```bash
# 运行测试
./mvnw test

# 构建可执行 JAR
./mvnw clean package

# 运行构建产物
java -jar target/whattoeat-server-0.0.1-SNAPSHOT.jar
```

## 项目结构

```text
whattoeat-server/
├── src/main/java/          # 应用与业务代码
├── src/main/resources/     # 配置及资源文件
├── src/test/java/          # 自动化测试
├── pom.xml                 # Maven 项目配置
└── mvnw                    # Maven Wrapper
```
