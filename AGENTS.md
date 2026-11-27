# WhatToEat2 后端开发指南

## 1. 技术栈与环境
- **核心栈**：Java 21 + Spring Boot 4.x + Maven Wrapper
- **测试与运行**：
    - 测试依赖本地/IDE 注入的环境变量，**不要直接在命令行运行全量 `./mvnw test`**。
    - 需要验证编译与打包时：`./mvnw clean compile` 或与修改相关的单测。

## 2. 项目架构与分层
项目统一采用 **按特性划分（Feature-first）** 的包结构：`com.rubp.whattoeat.server`
```text
server/
├── core/                  # 全局基础能力（全局异常处理、公用过滤器、通用响应封装）
└── feature/<module>/      # 各业务功能模块
    ├── controller/        # 接口层，只负责接收请求、校验参数、返回 DTO
    ├── service/           # 业务逻辑与用例
    ├── data/              # 持久化相关（Repository 与 Entry/Entity）
    ├── model/             # 外部交互 DTO（Request / Response）
    └── exception/         # 模块特有异常与异常处理
```

## 3. 需求未定先对齐

字段、功能未明确时先确认，不要自己猜测和引入一堆写法

## 4.依赖引入要求

优先使用 Spring Boot 已管理或现有的组件，引入新依赖需说明理由。

## 5. 命名规范

- **整体一致**：变量名、方法名、类名等应与仓库既有代码风格一致，不要混搭。
- **Repository 层**：
  - 简短命名 + @Query：业务排序方式基本是固定的，大多数情况可用简短命名加 @Query（如 findAll、findById、findNameById），无需用 Spring Data JPA 派生命名拼出冗长方法；但像 findById、deleteById、findByUsername、existsByEmailHmac 这类简短的单字段查询且没有特殊要求时，仍然可以保留直接使用。
  - **省略主实体名**：依托 `*Repository` 接口上下文，操作主表的方法不重复主实体名（比如应该用 `findAll`、`findById`、`updateNameById`，而不是 `findStalls`、`deleteCategoryById`）。
  - **主表主键命名**：直接按主表主键操作的方法统一体现 `ById`（如 `findById`、`updateNameById`、`removeById`），主键参数统一简写为 `id`。
  - **关联表命名**：操作关联表（如 `*_link`）的方法统一用 `Links` 指代关联记录（如 `insertLinks`、`deleteLinksByStallId`、`deleteLinksByCategoryId`）；多表外键时保留明确前缀（如 `stallId`、`categoryId`）。
- **Controller / Service 层**：保留业务资源名词以明确用例语义（如 `listStalls`、`getStall`、`createCategory` / `renameCategory` / `deleteCategory`）。

## 6.指令规范

不可执行rm -rf等危险指令，执行rm等指令前必须强调

## 7.文件规范

单个文件不宜太长，尽量在 350 行以下

## 8.计划新功能

提出修改和新增新功能的时候，若有表述不清晰的部分，必须进行追问，不可自己捏造细节。需等所有细节都确认清楚，且被接受了，才可开始动工。

## 9.修改代码

修改或者增加代码的时候，若要修改其他文件的代码，必须进行强调，而且非必要不进行修改

## 10.IDEA mcp

- 若配置了 IDEA mcp，应当优先使用 mcp，以获得更准确的结果

## 11. Commit 规范

- **提交格式**：统一使用 `类别前缀(修改范围): 描述`，例如 `feat(JwtTokenService): 增加创建并签名的createToken方法`。描述应简洁、准确地说明本次变更。
- **提交粒度**：每次提交应围绕一个明确的功能、修复或重构，避免混入无关改动。较大的变更应按职责合理拆分，但不得为缩小提交规模而破坏功能完整性或代码的可编译性。

## 12. 管理员操作审计

- 管理员增、删、改等写操作成功时需在 `@Transactional` 事务中调用 `AdminAuditService.record(...)` 记录审计（具体参数见方法注释）。
- 新增审计事件时，同步在 `i18n/messages*.properties` 补充各语言摘要模板；涉及新资源时补充 `AuditResourceType`。
