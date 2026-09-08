# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Fresh Market（fresh-market）：生鲜市场后端，Spring Boot 3.5.16 多模块 Maven 工程（2026-09-08 从 2.7.18 迁移，jakarta 命名空间 + JDK 17 基线）。代码注释、日志、commit message 均为中文，新增代码请保持一致的中文注释风格。

已实现业务：员工登录、商品、分类、地址簿、购物车、订单（含超时取消）、秒杀（含并发扣库存）、数据报表、管理端 AI 问答（function calling 查经营数据），以及通用基础设施（工具类、配置、拦截器、切面等）。

## 常用命令

```bash
# 全量构建（根目录执行，聚合 fresh-common / fresh-pojo / fresh-server）
mvn clean package

# 启动应用（需先 install，再进入 fresh-server 目录运行；
# 注意：不要在根目录 mvn -pl fresh-server -am spring-boot:run，会在父 pom 上执行并报找不到主类）
mvn install -DskipTests
cd fresh-server && mvn spring-boot:run

# 运行全部测试
mvn test
```

- **JDK 注意**：Boot 3.5 要求 JDK 17+（父 pom 默认按 release 17 编译，fresh-server 不再单独覆盖 compiler 插件），命令行 Maven 默认用 JDK 8（JAVA_HOME=D:\Java\jdk1.8）编不过，构建前需指定：`JAVA_HOME="C:\Program Files\Java\latest\jdk-17" mvn ...`。
- 也可直接在 IDE 中运行启动类 `com.fresh.FreshApplication`（fresh-server 模块）。
- 服务端口 8080，接口文档（knife4j 4.x，基于 springdoc/OpenAPI3 注解）：http://localhost:8080/doc.html ，分"管理端接口"和"用户端接口"两个分组。
- 测试：集成测试位于 `fresh-server/src/test/java`（连本机 MySQL/Redis/RabbitMQ，如 `SeckillOrderCancelTest`、`SeckillConcurrencyTest`），IDEA 中可直接点运行键，命令行跑法同上（需 JDK 17）。

## 运行环境依赖

启动前需保证本地中间件可用（配置见 `fresh-server/src/main/resources/application-dev.yml`）：

- MySQL：localhost:3306，库 `fresh_market`
- Redis：localhost:6379（Redisson 客户端在 `RedisConfiguration` 中读取 `fresh.redis.*` 配置，不再硬编码）
- MySQL/Redis 的真实密码与 AI key 一样放在仓库外 `~/.fresh-market/application-secret.yml`，dev yml 只留占位符（`<your-mysql-password>` 等）；缺该文件的机器启动会连不上数据库/Redis
- RabbitMQ：localhost:5672，vhost `/fresh`，用户 fresh_market/123（当前无消费者，不装延迟消息插件也可启动）

## 模块结构

| 模块 | 职责 |
|---|---|
| fresh-common | 工具类（JwtUtil、AliOssUtil、HttpClientUtil）、常量、枚举、Result/PageResult 统一返回、BaseContext、通用异常类、配置属性类 |
| fresh-pojo | 纯数据对象：dto（前端请求）、entity（数据库表）、vo（前端响应），Lombok + swagger 注解 |
| fresh-server | 可运行的主应用：controller / service / mapper（待开发）、config / interceptor / aspect / task / websocket（已就绪） |

依赖方向：fresh-server → fresh-common / fresh-pojo。改 pojo/common 后需重新构建才能被 server 引用到。

## 核心架构（已就绪的通用部分）

### 双端接口体系

- 约定 `controller/admin/**` 为管理端、`controller/user/**` 为用户端（C 端），新建 Controller 时按此分包。
- 两套 JWT 拦截器（`JwtTokenAdminInterceptor` / `JwtTokenUserInterceptor`），注册于 `WebMvcConfiguration`：
  - 管理端拦截 `/admin/**`，token 请求头名 `token`
  - 用户端拦截 `/user/**`，token 请求头名 `authentication`
  - `WebMvcConfiguration` 中放行的登录路径为占位 TODO，开发登录接口后需改成实际路径。
- 拦截器解析 JWT 后将当前用户 id 存入 `BaseContext`（ThreadLocal），供后续层使用。

### 请求处理流

Controller（`@RestController` + OpenAPI3 注解：`@Tag`/`@Operation`/`@Schema`）→ Service 接口 + `service/impl` 实现 → Mapper（MyBatis，注解与 `resources/mapper/*.xml` 混用）。

- 统一返回 `Result<T>` / `PageResult`；业务异常继承 `BaseException`，由 `handler/GlobalExceptionHandler` 统一处理。
- 分页用 PageHelper：Service 中先 `PageHelper.startPage(page, pageSize)`，再用 `new Page<>(list)` 包装。
- `WebMvcConfiguration` 中自定义 Jackson 消息转换器（`JacksonObjectMapper`）处理 LocalDateTime 序列化。

### 公共字段自动填充（AOP）

Mapper 的 insert/update 方法标注 `@AutoFill(OperationType.INSERT/UPDATE)`，`AutoFillAspect` 切面在 SQL 执行前通过反射调用实体 setter，填充 createTime/updateTime/createUser/updateUser，操作人取自 `BaseContext.getCurrentId()`。新增需要自动填充的 Mapper 方法时照此模式标注（实体需有对应的四个字段）。

### 其他

- WebSocket：`websocket/WebSocketServer`（`@ServerEndpoint("/ws/{sid}")`），静态 ConcurrentHashMap 管理会话，群发用 `sendToAllClient`；`task/WebSocketTask` 有定时推送示例（已注释）。
- 定时任务：`@EnableScheduling` 已开启，`task/MyTask` 为示例。
- RabbitMQ：`RabbitMqConfig` 已配置 Jackson 消息转换器，直接声明 `@RabbitListener` 即可。
- 阿里云 OSS：`AliOssUtil` 由 `OssConfiguration` 创建，注入即用；密钥占位符 `<your-aliyun-access-key-id>` 需替换后上传才能用。

## 配置

- `application.yml` 为主配置（激活 dev profile），`application-dev.yml` 存放环境相关的数据源/Redis/OSS 配置。
- JWT 密钥与 token 名在 `fresh.jwt.*`；阿里云 OSS 密钥在 dev yml 中。

## Git 约定

- commit message 为中文、描述功能点，如 "商品分类管理功能开发"。
