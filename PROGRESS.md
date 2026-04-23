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
- **当前阶段**:阶段 2 进行中(REST API 完整化已完成,下一步引入 Redis 向量库)
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
- [ ] **#24**(可选)编写 `RagDemoController`,演示 DeepSeek + 检索结果做 RAG 生成

### 阶段 3/4
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
└── src/
    ├── main/java/com/hanae/raghub/
    │   ├── RagHubApplication.java                       启动类(脚手架生成)
    │   ├── common/
    │   │   └── Result.java                              统一返回结构
    │   ├── entity/
    │   │   ├── Document.java                            文档实体
    │   │   ├── DocumentChunk.java                       分块实体
    │   │   └── DocumentStatus.java                      文档状态枚举
    │   ├── repository/
    │   │   ├── DocumentRepository.java                  文档 Repository
    │   │   └── DocumentChunkRepository.java             分块 Repository
    │   ├── config/
    │   │   └── VectorStoreConfig.java                   向量库配置(Profile 切换 Simple/Redis + 启动重建)
    │   ├── service/
    │   │   ├── DocumentService.java                     文档上传全链路 + 列表 + 删除
    │   │   └── RetrievalService.java                    向量相似度检索服务
    │   ├── dto/
    │   │   ├── SearchRequest.java                       检索请求 DTO
    │   │   └── DocumentResponse.java                    文档响应 DTO
    │   ├── exception/
    │   │   ├── GlobalExceptionHandler.java              全局异常处理(@RestControllerAdvice)
    │   │   └── ResourceNotFoundException.java           资源未找到异常
    │   └── controller/
    │       └── DocumentController.java                  REST 接口(上传/列表/删除/检索)
    ├── main/resources/
        ├── application.yaml                             应用配置(默认 profile: redis)
        ├── application-simple.yaml                      simple profile 配置(排除 Redis 自动配置)
        └── db/migration/
            ├── V1__init_schema.sql                      Flyway V1(建表)
            └── V2__add_vector_id.sql                    Flyway V2(document_chunks 增加 vector_id)
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

### Flyway 使用铁律

- **所有 schema 变更必须通过 Flyway 文件**,本机也不例外
- **已成功执行的 V 文件永远不能修改内容**(checksum 校验会拒绝启动)
- 开发期 SQL 写错了:只要 V1 没成功跑过(或可以 drop 整个库),就可以直接改
- 生产环境要改 schema:只能写 V2、V3... 追加
- `flyway_schema_history` 表存在业务库内,drop 库会一起删(这是特性,保证 history 与库强绑定)

---

## 最近一次更新

**日期**:2026-04-23
**完成**:#20 ~ #23 全部完成。Redis Stack 部署 + Spring AI Redis 向量库接入 + Profile 切换验证通过
**下一步**:#24(可选)编写 `RagDemoController` 演示 RAG 生成;或直接进入阶段 3(用户系统 + 安全)
**负责人**:用户 + Claude
