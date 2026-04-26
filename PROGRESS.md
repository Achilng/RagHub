# RagHub 开发进度

> 本文档记录 RagHub 项目的开发进度,供任何接手此项目的 AI / 开发者快速了解当前状态。
>
> **铁律:每完成一个步骤,必须更新本文档(勾选任务 + 补充"最近一次更新"段落)**。
>
> 项目蓝图见 `PLAN.md`,本文档只反映**实际执行情况**。

---

## 项目概况

- **项目名**:RagHub
- **性质**:学习项目,目标是完整体验 Spring Boot + Spring AI 的企业级开发流程
- **最终形态**:RAG-as-a-Service 平台(文档上传 → 解析 → 分块 → Embedding → 向量入库 → REST 检索 API)
- **当前阶段**:阶段 1-4 已完成,准备进入阶段 5(工程化)
- **路径**:`D:\java\project\RagHub`

---

## 技术栈(已落地)

| 层 | 选型 | 版本 |
|---|---|---|
| JDK | Java 17 | `C:\Program Files\Java\jdk-17` |
| 构建工具 | Maven | 3.9.14(本机安装,不走 mvnw) |
| 框架 | Spring Boot | 3.5.13 |
| AI 抽象层 | Spring AI | 1.1.2 |
| Spring AI Alibaba | extensions-bom | 1.1.2.2 |
| Embedding 模型 | 通义 DashScope `text-embedding-v3` | - |
| LLM(暂留) | DeepSeek | - |
| 数据库 | MySQL 8.x | localhost:3306 |
| ORM | Spring Data JPA | - |
| Schema 管理 | Flyway | - |
| 文档解析 | Spring AI Tika Document Reader | - |
| 安全框架 | Spring Security + JWT(jjwt 0.12.6） | - |
| 向量库 | Redis Stack（RedisVectorStore） | localhost:6390 |
| 前端框架 | Vue 3 + TypeScript + Vite | frontend/ |
| 前端路由 | Vue Router 4 | - |
| HTTP 客户端 | Axios | - |

---

## 环境依赖(接手必须先确认)

### 本机环境
- [x] JDK 17 已安装并配置
- [x] Maven 3.9.14 已安装(`C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.14\bin\mvn.cmd`)
- [x] MySQL 8.x 本机运行中

### IDEA Run Configuration 环境变量(必须设置)
这些变量**不进 git**,每个新开发者自己配:
- `DASHSCOPE_API_KEY` — 通义 DashScope API key
- `DEEPSEEK_API_KEY` — DeepSeek API key(因为引入了 DeepSeek starter,不配会启动失败)
- `RAGHUB_DB_PASSWORD` — MySQL `raghub_app` 用户的密码
- `RAGHUB_JWT_SECRET` — JWT 签名密钥(至少 32 个字符的随机字符串)

### 数据库
- 数据库名:`raghub`(utf8mb4)
- 应用账号:`raghub_app@localhost`,仅对 `raghub.*` 有业务权限
- Schema 由 Flyway 自动管理(`src/main/resources/db/migration/`)

---

## 任务进度

### 阶段 1:最小 RAG 闭环

- [x] **#0** 撰写 `PLAN.md` 开发实施计划
- [x] **#1** 修改 `pom.xml`:引入 Spring AI 1.1.2 + Spring AI Alibaba 1.1.2.2 + JPA + MySQL + Flyway + DashScope + Tika + DeepSeek
- [x] **#2** 创建 MySQL `raghub` 数据库与专用账号 `raghub_app`
- [x] **#3** 在 IDEA Run Config 中配置 `DASHSCOPE_API_KEY` / `DEEPSEEK_API_KEY` / `RAGHUB_DB_PASSWORD` 环境变量
- [x] **#4** 编写 `application.yaml`:datasource / JPA(ddl-auto=validate, open-in-view=false)/ flyway / dashscope / deepseek
- [x] **#5** 编写 Flyway `V1__init_schema.sql`:`documents` / `document_chunks` 两张表
- [x] **#5.1** 首次启动验证:Flyway 成功执行 V1,三张表(documents / document_chunks / flyway_schema_history)已建立
- [x] **#6** 编写 JPA 实体类 `Document` / `DocumentChunk` / `DocumentStatus`
- [x] **#7** 编写 Repository 接口(`DocumentRepository` / `DocumentChunkRepository`),并通过集成测试验证
- [x] **#8** 编写 `VectorStoreConfig`:注册 `SimpleVectorStore` Bean,启动时从 MySQL 重建索引
- [x] **#9** 编写 `DocumentService`:上传 → Tika 解析 → 分块 → DashScope embedding → MySQL 持久化 → VectorStore 写入
- [x] **#10** 编写 `RetrievalService`:query → VectorStore 相似度检索 → 返回 Top-K
- [x] **#11** 编写 `DocumentController`:`POST /api/documents`、`POST /api/search` + `SearchRequest` DTO
- [x] **#12** 端到端验证:上传 txt 文件 → 发起 query → 拿到 Top-K 相关分块 ✓

### 阶段 2:REST API 完整化 + 文档管理 + Redis 向量库

- [x] **#13** 统一返回结构 `Result<T>`(`common/Result.java`)
- [x] **#14** 全局异常处理 `@RestControllerAdvice`(`GlobalExceptionHandler` + `ResourceNotFoundException`)
- [x] **#15** `DocumentResponse` DTO,Controller 不再直接返回 Entity
- [x] **#16** 文档列表接口 `GET /api/documents`(返回 `Result<List<DocumentResponse>>`)
- [x] **#17** 文档删除接口 `DELETE /api/documents/{id}`(同步清理向量库 + MySQL 分块 + 文档记录)
- [x] **#18** Flyway `V2__add_vector_id.sql`:给 `document_chunks` 表增加 `vector_id` 列,用于关联向量库条目
- [x] **#19** 上传 / 检索接口统一包装为 `Result<T>` 返回
- [x] **#20** 引入 Redis Stack(Docker 部署 `redis/redis-stack`,端口映射 6390:6379,含 RediSearch 模块)
- [x] **#21** 加入 `spring-ai-starter-vector-store-redis` 依赖 + `application.yaml` 配置 Redis 连接与向量索引
- [x] **#22** 改造 `VectorStoreConfig`:用 `@Profile("simple")` / `@Profile("redis")` 切换向量库实现;新增 `application-simple.yaml` 排除 Redis 自动配置;Redis profile 下新增 `rebuildRedisIndex` 启动重建逻辑
- [x] **#23** 验证通过:切换 `spring.profiles.active` 即可在 SimpleVectorStore 和 Redis 间切换,业务代码零修改 ✓
- [x] **#24** 编写 `RagDemoController`,演示 DeepSeek + 检索结果做 RAG 生成

### 阶段 3:用户系统 + 安全 + 登录前端

**后端任务**:
- [x] **#25** Flyway `V3__create_users_table.sql`:创建 `users` 表
- [x] **#26** 用户实体 `User` + `UserRepository`
- [x] **#27** 用户注册接口 `POST /api/auth/register`
- [x] **#28** 用户登录接口 `POST /api/auth/login`
- [x] **#29** 引入 Spring Security + JWT(jjwt 0.12.6)
- [x] **#30** 编写 `JwtAuthenticationFilter`、`SecurityConfig`
- [x] **#31** 密码使用 BCrypt 存储
- [x] **#32** 配置 CORS(允许 `localhost:5173` 跨域)
- [x] **#33** Flyway `V4__add_user_id_to_documents.sql`:给 `documents` 表加 `user_id` 非空约束 + 外键 + 索引
- [x] **#34** 所有文档 / 检索 / RAG 接口做权限校验:用户只能操作自己的文档;向量搜索用 filterExpression 按 userId 过滤

**前端任务**:
- [x] **#35** 初始化前端项目(`frontend/` 目录,Vue 3 + TypeScript + Vite)
- [x] **#36** 搭建项目结构:路由(Vue Router)、HTTP 客户端(Axios + 拦截器)
- [x] **#37** Axios 请求拦截器:自动携带 `Authorization: Bearer <token>` 头;401 自动清 Token 跳登录
- [x] **#38** 注册页面(含确认密码校验)
- [x] **#39** 登录页面 + JWT Token 本地存储
- [x] **#40** 路由守卫:未登录自动跳转登录页

### 阶段 4:文档管理 + RAG 前端

- [x] **#41** 整体布局:顶栏导航(文档管理 / RAG 问答)+ 用户信息 / 退出登录
- [x] **#42** 文档管理 API 封装(`api/document.ts`)
- [x] **#43** 文档管理页面:文件上传 + 文档列表(状态/大小/分块数)+ 删除
- [x] **#44** RAG 问答 API 封装(`api/rag.ts`)
- [x] **#45** RAG 问答页面:输入问题 + 回答展示 + 引用来源展示 + topK 可选

### 阶段 5:工程化
见 `PLAN.md`,尚未开始。

---

## 已创建的文件清单

```
D:\java\project\RagHub\
├── PLAN.md                                              开发实施计划
├── PROGRESS.md                                          本文档(开发进度)
├── CLAUDE.md                                            项目协作规则
├── pom.xml                                              Maven 依赖配置
├── .mvn/wrapper/maven-wrapper.properties                (已不再使用)
├── frontend/                                            前端项目(Vue 3 + TypeScript + Vite)
│   ├── package.json                                     前端依赖(vue, vue-router, axios)
│   ├── vite.config.ts                                   Vite 配置(含 /api 代理到 8080)
│   ├── tsconfig.json                                    TypeScript 配置
│   └── src/
│       ├── main.ts                                      入口文件(挂载 Router)
│       ├── App.vue                                      根组件(<router-view />)
│       ├── style.css                                    全局样式
│       ├── api/
│       │   ├── http.ts                                  Axios 实例 + 请求/响应拦截器
│       │   ├── auth.ts                                  注册 / 登录 API
│       │   ├── document.ts                              文档上传 / 列表 / 删除 API
│       │   └── rag.ts                                   RAG 问答 API
│       ├── router/
│       │   └── index.ts                                 路由配置 + 路由守卫
│       └── views/
│           ├── Login.vue                                登录页
│           ├── Register.vue                             注册页
│           ├── Home.vue                                 主布局(导航栏 + router-view)
│           ├── Documents.vue                            文档管理页
│           └── RagChat.vue                              RAG 问答页
└── src/
    ├── main/java/com/hanae/raghub/
    │   ├── RagHubApplication.java                       启动类(脚手架生成)
    │   ├── common/
    │   │   └── Result.java                              统一返回结构
    │   ├── entity/
    │   │   ├── Document.java                            文档实体(含 userId)
    │   │   ├── DocumentChunk.java                       分块实体
    │   │   ├── DocumentStatus.java                      文档状态枚举
    │   │   └── User.java                                用户实体
    │   ├── repository/
    │   │   ├── DocumentRepository.java                  文档 Repository(含 findByUserId)
    │   │   ├── DocumentChunkRepository.java             分块 Repository
    │   │   └── UserRepository.java                      用户 Repository
    │   ├── config/
    │   │   ├── VectorStoreConfig.java                   向量库配置(Profile 切换 Simple/Redis + 启动重建)
    │   │   ├── RedisVectorStoreConfig.java              Redis VectorStore 手动配置(含 metadata 字段声明)
    │   │   └── SecurityConfig.java                      Spring Security + JWT + CORS 配置
    │   ├── security/
    │   │   ├── JwtService.java                          JWT 签发 / 验签 / 解析
    │   │   └── JwtAuthenticationFilter.java             从请求头提取 Token 并设置 SecurityContext
    │   ├── service/
    │   │   ├── DocumentService.java                     文档上传全链路 + 列表 + 删除(按 userId 隔离)
    │   │   ├── RetrievalService.java                    向量相似度检索服务(按 userId 过滤)
    │   │   └── AuthService.java                         注册 / 登录业务逻辑
    │   ├── dto/
    │   │   ├── SearchRequest.java                       检索请求 DTO
    │   │   ├── DocumentResponse.java                    文档响应 DTO
    │   │   ├── RegisterRequest.java                     注册请求 DTO
    │   │   ├── LoginRequest.java                        登录请求 DTO
    │   │   ├── AuthResponse.java                        认证响应 DTO(token + username)
    │   │   ├── RagRequest.java                          RAG 请求 DTO
    │   │   └── RagResponse.java                         RAG 响应 DTO
    │   ├── exception/
    │   │   ├── GlobalExceptionHandler.java              全局异常处理(@RestControllerAdvice)
    │   │   └── ResourceNotFoundException.java           资源未找到异常
    │   └── controller/
    │       ├── DocumentController.java                  REST 接口(上传/列表/删除/检索,含 @AuthenticationPrincipal)
    │       ├── RagDemoController.java                   RAG 演示接口(DeepSeek + 检索,含用户过滤)
    │       └── AuthController.java                      注册 / 登录接口
    ├── main/resources/
        ├── application.yaml                             应用配置(默认 profile: redis)
        ├── application-simple.yaml                      simple profile 配置(排除 Redis 自动配置)
        └── db/migration/
            ├── V1__init_schema.sql                      Flyway V1(建表)
            ├── V2__add_vector_id.sql                    Flyway V2(document_chunks 增加 vector_id)
            ├── V3__create_users_table.sql               Flyway V3(创建 users 表)
            └── V4__add_user_id_to_documents.sql         Flyway V4(documents 加 user_id + 外键 + 索引)
```

---

## 关键决策与踩坑记录

### 决策

1. **Spring AI 版本降级到 1.1.2**(原计划 1.1.4):为了匹配 Spring AI Alibaba 1.1.2.2 的兼容性。
2. **使用 `spring-ai-alibaba-extensions-bom` 而非 `spring-ai-alibaba-bom`**:主 BOM 不管理 dashscope starter,extensions BOM 才管理。
3. **`ddl-auto: validate` + Flyway**:生产标准做法。表结构由 Flyway SQL 严格控制,JPA 只校验实体与表是否对齐。严禁使用 `update` / `create-drop`。
4. **`open-in-view: false`**:生产标准。关闭 OSIV,避免数据库连接持有到 view 渲染结束。
5. **两层数据架构**:MySQL 是事实源(存原始向量 JSON),向量库是可重建索引。换模型 / 换向量库时 drop 索引重建即可。
6. **JPA 关系映射用"单向 + 仅存外键 ID"**:`DocumentChunk` 里只有 `Long documentId`,不持有 `Document` 对象引用。避免懒加载陷阱与 N+1 查询。
7. **枚举用 `EnumType.STRING`**:数据库存枚举名字符串,不存序号,避免新增枚举值破坏老数据。
8. **Redis 向量库用 Docker 部署 `redis/redis-stack`**:端口映射 6390:6379(6379 被 Hyper-V 保留端口占用)。
9. **向量库通过 Spring Profile 切换**:`simple` profile 用 SimpleVectorStore + 排除 Redis 自动配置;`redis` profile 用 RedisVectorStore 自动配置。业务代码面向 `VectorStore` 接口编程,切换零改动。
10. **JWT Filter 不标 `@Component`**:在 `SecurityConfig` 里直接 `new` 传给 Security 链,避免 Spring Boot 同时注册为 Servlet Filter 和 Security Filter 导致请求过两遍。
11. **Security 异常返回 JSON**:自定义 `AuthenticationEntryPoint`(401) 和 `AccessDeniedHandler`(403),保证所有响应格式统一为 `Result<T>`。
12. **Document 与 User 关系用 `Long userId` 而非 `@ManyToOne`**:与 DocumentChunk 的做法一致,简单直接,避免懒加载和 N+1 问题。
13. **Controller 用 `@AuthenticationPrincipal User user` 获取当前用户**:Spring Security 提供的注解,自动从 SecurityContext 提取 principal,避免手写 `SecurityContextHolder.getContext().getAuthentication().getPrincipal()` 样板代码。
14. **Redis VectorStore 手动配置替代 YAML 自动配置**:Spring AI 1.1.x 的 `metadata-fields` YAML 属性无效(文档只展示 Java Bean 配置方式),必须通过 `RedisVectorStore.builder()` 手动声明 `MetadataField.numeric("userId")` 等字段,搜索时才能用 `filterExpression` 过滤。
15. **前端采用"按功能纵切"开发方式**:后端做完一个功能立刻做对应前端,而非先做完所有后端再做前端。更接近真实全栈开发节奏。
16. **Vite proxy 代理**:前端 `/api` 请求通过 Vite 的 `server.proxy` 转发到后端 `localhost:8080`,开发阶段不需要 CORS(但后端仍保留 CORS 配置供生产部署用)。

### 踩坑

1. **Maven Wrapper 在中国网络环境下载 Maven 发行版失败**
   - 现象:IDEA Maven 同步"无响应",手动 `./mvnw` 卡住或 SSL 错误
   - 原因:`mvnw` 用 curl 从 Maven Central 下载 Maven 二进制,国内网络不通;即便改为阿里云镜像,Windows curl 也报 `CRYPT_E_REVOCATION_OFFLINE`
   - 解决:直接本机安装 Maven 3.9.14,绕过 mvnw

2. **`dependencies.dependency.version missing` 错误**
   - 现象:加了 `spring-ai-alibaba-starter-dashscope` 后 Maven 报缺 version
   - 原因:错用了 `spring-ai-alibaba-bom`,该 BOM 只管理主工程 artifacts(graph / nacos / agent-framework 等),不管理 dashscope
   - 解决:改用 `spring-ai-alibaba-extensions-bom`(管理 dashscope / rag / memory / mcp 等扩展)
   - 教训:`version missing` 未必是你忘写版本,可能是你选的 BOM 根本不管这个 artifact。用 curl 下载 BOM pom 文件 grep artifactId 可以验证

3. **DeepSeek 启动失败:`DeepSeek API key must be set`**
   - 现象:启动抛 `IllegalArgumentException`
   - 原因:引入了 `spring-ai-starter-model-deepseek`,Spring Boot auto-configuration 强制要求必须配 key
   - 解决:在 yaml 加 `spring.ai.deepseek.api-key: ${DEEPSEEK_API_KEY}` + IDEA 环境变量
   - 另一种解法(备用):`spring.autoconfigure.exclude: org.springframework.ai.model.deepseek.autoconfigure.DeepSeekChatAutoConfiguration`

4. **Redis 向量库自动配置与手动 Bean 冲突**
   - 现象:启动报 `The bean 'vectorStore' could not be registered... overriding is disabled`
   - 原因:`spring-ai-starter-vector-store-redis` 的自动配置会无条件注册 `vectorStore` Bean,与手动定义的 `SimpleVectorStore` 冲突
   - 解决:在 `application-simple.yaml` 中 `spring.autoconfigure.exclude` 排除 `RedisVectorStoreAutoConfiguration`
   - 教训:添加带自动配置的 starter 时,需要考虑 Bean 冲突问题,按需排除

5. **Docker 绑定 6379 端口失败**
   - 现象:`Ports are not available: listen tcp 0.0.0.0:6379: bind: An attempt was made to access a socket in a way forbidden`
   - 原因:Windows Hyper-V 动态保留端口范围包含 6379(通过 `netsh interface ipv4 show excludedportrange protocol=tcp` 确认)
   - 解决:Docker 端口映射改为 6390:6379,应用配置 `spring.data.redis.port: 6390`

6. **多个 ChatModel Bean 导致 `ChatClient.Builder` 注入失败**
   - 现象:`required a single bean, but 2 were found: dashScopeChatModel, deepSeekChatModel`
   - 原因:DashScope 和 DeepSeek starter 各自注册了一个 `ChatModel`,`ChatClient.Builder` 自动配置不知道选哪个
   - 解决:在 `RagDemoController` 中用 `@Qualifier("deepSeekChatModel")` 明确指定
   - 教训:同时引入多个 AI 模型 starter 时,需要用 `@Qualifier` 消歧义

7. **Redis VectorStore metadata-fields YAML 配置无效导致 filterExpression 搜不到结果**
   - 现象:向量搜索加 `filterExpression("userId == 1")` 后始终返回 0 条结果,去掉过滤则正常
   - 原因:Spring AI 1.1.x 的 Redis VectorStore 自动配置不支持通过 YAML 声明 metadata 字段,文档明确只展示 Java Bean 配置方式
   - 解决:排除 `RedisVectorStoreAutoConfiguration`,在 `RedisVectorStoreConfig` 中手动创建 `RedisVectorStore` Bean + `JedisPooled` Bean,通过 `MetadataField.numeric("userId")` 声明可过滤字段
   - 教训:Spring AI 的自动配置能力有限,涉及高级特性(metadata 过滤)时需要手动配置

### Flyway 使用铁律

- **所有 schema 变更必须通过 Flyway 文件**,本机也不例外
- **已成功执行的 V 文件永远不能修改内容**(checksum 校验会拒绝启动)
- 开发期 SQL 写错了:只要 V1 没成功跑过(或可以 drop 整个库),就可以直接改
- 生产环境要改 schema:只能写 V2、V3... 追加
- `flyway_schema_history` 表存在业务库内,drop 库会一起删(这是特性,保证 history 与库强绑定)

---

## 最近一次更新

**日期**:2026-04-26
**完成**:阶段 3 全部完成(#25-#40),阶段 4 全部完成(#41-#45)。具体包括:
- 后端:Spring Security + JWT 认证、用户注册/登录、文档 userId 归属隔离、向量搜索 userId 过滤、Redis VectorStore 手动配置
- 前端:Vue 3 + TypeScript + Vite 项目搭建、Vue Router 路由守卫、Axios 拦截器(自动携带 Token / 401 跳转)、注册/登录页面、文档管理页面(上传/列表/删除)、RAG 问答页面(提问/回答/引用来源)
- 全流程已验证:注册 → 登录 → 上传文档 → RAG 问答 → 看到生成的回答和引用来源 ✓
**下一步**:阶段 5 — 工程化(Swagger UI、日志、测试、README)
**负责人**:用户 + Claude
