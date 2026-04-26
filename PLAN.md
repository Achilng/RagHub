# RagHub 开发实施计划书

> 本文档是 RagHub 项目的开发蓝图，用于在开发过程中保持方向一致、防止偏移。
> **任何对本计划的重大调整，都必须先更新本文档，再动手写代码。**

---

## 一、项目定位

- **性质**：学习项目，目的是完整体验 Spring Boot + Spring AI 的标准企业开发流程
- **最终形态**：RAG-as-a-Service 平台。用户上传文档后，系统自动完成解析、分块、Embedding、向量入库，对外提供标准 RESTful 检索接口，供第三方大模型应用即插即用调用

## 二、功能范围（MVP 边界）

| 功能 | 是否包含 | 阶段 |
|---|---|---|
| 用户注册 / 登录 | 是 | 阶段 3 |
| JWT 认证授权 | 是 | 阶段 3 |
| 文档上传（txt / md / pdf 等） | 是 | 阶段 1-2 |
| 文档解析 + 分块 | 是 | 阶段 1 |
| Embedding 生成 | 是 | 阶段 1 |
| 向量入库 | 是 | 阶段 1 |
| 相似度检索 RESTful API | 是 | 阶段 1-2 |
| 文档管理 CRUD（列表 / 删除） | 是 | 阶段 2 |
| DeepSeek 联动的 RAG 演示接口 | 是 | 阶段 2（已完成） |
| 前端 — 注册 / 登录页面 | 是 | 阶段 3 |
| 前端 — 文档管理界面 | 是 | 阶段 4 |
| 前端 — 检索 + RAG 问答界面 | 是 | 阶段 4 |
| Swagger / OpenAPI 文档 | 是 | 阶段 5 |
| 多租户 / 文档权限隔离 | 是 | 阶段 3 |
| 异步任务 / 大文件处理 | 不在 MVP | 后续 |
| 监控 / 链路追踪 | 不在 MVP | 后续 |

---

## 三、技术栈（已确认）

| 层 | 选型 | 备注 |
|---|---|---|
| 语言 / JDK | Java 17 | 已确定 |
| 框架 | Spring Boot 3.5.13 | 已确定 |
| AI 抽象层 | Spring AI 1.1.4 | 已确定 |
| Embedding 模型 | 通义 DashScope `text-embedding-v3`（或 v4） | 通过 Spring AI Alibaba 接入 |
| LLM（可选） | DeepSeek（已存在，仅做演示） | 暂时保留 |
| 关系型数据库 | MySQL 8.x | 用户、文档、分块元数据 + 向量原始数据 |
| ORM | Spring Data JPA | 学习 Spring 标准分层 |
| 向量库（阶段 1） | Spring AI `SimpleVectorStore`（内存） | 启动时从 MySQL 重建 |
| 向量库（阶段 2 升级） | Redis Stack / Redis 8 向量搜索 | 用户已熟悉 Redis |
| Web 层 | Spring MVC（`spring-boot-starter-web`） | 标准 REST |
| 文档解析 | Spring AI Tika Document Reader | 支持 txt/pdf/docx 等 |
| 安全 | Spring Security + JWT | 阶段 3 引入 |
| 前端框架 | Vue 3 + TypeScript | 阶段 3 引入 |
| 前端构建 | Vite | - |
| 前端路由 | Vue Router | - |
| HTTP 客户端 | Axios | - |
| API 文档 | springdoc-openapi（Swagger UI） | 阶段 5 引入 |
| 配置管理 | application.yaml + 环境变量 | 敏感信息绝不入库 |

> 注：Spring AI Alibaba 的具体 artifact 名称将在阶段 1 添加依赖时再次核对官方文档，避免版本错配。

---

## 四、整体架构

### 4.1 两层数据架构

```
┌──────────────────────────────────────────────┐
│  事实源 (Source of Truth)                    │
│  MySQL：用户 / 文档 / 分块原文 / 原始向量    │
└──────────────────┬───────────────────────────┘
                   │ 启动加载 / 新文档同步
                   ↓
┌──────────────────────────────────────────────┐
│  检索索引 (Index Layer)                      │
│  阶段 1：SimpleVectorStore（JVM 内存）       │
│  阶段 2：Redis 向量库（持久化）              │
└──────────────────────────────────────────────┘
```

**核心原则**：
- MySQL 是唯一事实源
- 向量库可以随时丢弃和重建（无论是因为换 embedding 模型、迁移到新向量库，还是单纯重启）
- 所有业务代码只面向 Spring AI 的 `VectorStore` 抽象编程，阶段 1 → 阶段 2 切换时只换 Bean 实现，业务代码不动

### 4.2 包结构（建议草案）

```
com.hanae.raghub
├── RagHubApplication.java           启动类
├── config/                          各类 @Configuration（VectorStore、Security 等）
├── controller/                      REST 控制器，只做参数校验和返回
├── service/                         业务逻辑
│   ├── DocumentService              文档上传/解析/分块
│   ├── EmbeddingService             调用 embedding 模型
│   ├── RetrievalService             相似度检索
│   └── UserService                  用户相关（阶段 3）
├── repository/                      Spring Data JPA 接口
├── entity/                          JPA 实体类
├── dto/                             请求/响应对象
├── exception/                       自定义异常 + 全局处理
└── common/                          工具类、常量
```

### 4.3 RAG 主链路时序

```
[上传阶段]
Client ──upload──▶ Controller ──▶ DocumentService
                                       │
                                       ├─ Tika 解析为纯文本
                                       ├─ TokenTextSplitter 分块
                                       ├─ EmbeddingService 批量生成向量
                                       ├─ MySQL 持久化 chunk + vector
                                       └─ VectorStore.add() 写入索引

[检索阶段]
Client ──query──▶ Controller ──▶ RetrievalService
                                       │
                                       ├─ EmbeddingService 把 query 变向量
                                       ├─ VectorStore.similaritySearch(topK)
                                       └─ 联表查 MySQL 拿元数据 → 返回 JSON
```

---

## 五、数据模型（初稿，可在阶段 1 调整）

```sql
-- 用户表（阶段 3 启用）
users
├── id            BIGINT PK AUTO_INCREMENT
├── username      VARCHAR(64) UNIQUE NOT NULL
├── password_hash VARCHAR(128) NOT NULL
├── created_at    DATETIME
└── updated_at    DATETIME

-- 文档表
documents
├── id           BIGINT PK AUTO_INCREMENT
├── user_id      BIGINT (阶段 3 起 NOT NULL，阶段 1-2 可为 NULL)
├── filename     VARCHAR(255) NOT NULL
├── file_size    BIGINT
├── mime_type    VARCHAR(64)
├── status       VARCHAR(16)  -- PENDING / PROCESSING / READY / FAILED
├── chunk_count  INT
├── created_at   DATETIME
└── updated_at   DATETIME

-- 文档分块表
document_chunks
├── id            BIGINT PK AUTO_INCREMENT
├── document_id   BIGINT FK NOT NULL
├── chunk_index   INT NOT NULL
├── content       TEXT NOT NULL
├── embedding     LONGTEXT  -- 存 JSON 序列化的浮点数组
├── token_count   INT
└── created_at    DATETIME
```

> 向量为什么存进 MySQL：保证 MySQL 是事实源，向量库可随时重建。生产环境通常会用专门的二进制压缩存储，学习阶段 JSON 字符串足够。

---

## 六、分阶段开发路线

### 阶段 1：跑通最小 RAG 闭环（核心目标）

**目标**：单文件上传 → 解析 → 分块 → 向量化 → 入库 → 检索接口能返回相关结果。**不做用户系统、不做安全**。

**任务清单**：
1. 补依赖
   - `spring-boot-starter-web`
   - `spring-boot-starter-data-jpa`
   - `mysql-connector-j`
   - Spring AI Alibaba DashScope starter
   - Spring AI Tika Document Reader
2. 创建 MySQL 数据库 `raghub`，建表
3. 配置 `application.yaml`：数据源、DashScope key（用环境变量占位）
4. 写实体类 `Document`、`DocumentChunk`
5. 写 Repository 接口
6. 写 `EmbeddingService`：封装通义 embedding 调用
7. 写 `DocumentService`：上传 → 解析 → 分块 → 向量化 → 入库
8. 写 `RetrievalService`：query → 向量化 → 相似度检索 → 返回结果
9. 写 `DocumentController`：`POST /api/documents`、`POST /api/search`
10. 写 `VectorStoreConfig`：注册 `SimpleVectorStore` Bean，启动时从 MySQL 加载
11. 用 curl / Postman 跑通端到端

**完成标志**：能上传一个 txt 文件，发起一次 query 拿到 Top-K 相关分块，全程无报错。

---

### 阶段 2：REST API 完整化 + 文档管理

**目标**：补齐文档管理接口，规范错误返回，准备接入 Redis 向量库。

**任务清单**：
1. 文档列表 / 删除接口（删除时同步清理向量库和 MySQL）
2. 全局异常处理 `@ControllerAdvice`
3. 统一返回结构 `Result<T>`
4. 引入 Redis Stack / Redis 8（docker 部署）
5. 加入 `spring-ai-starter-vector-store-redis`
6. 写 Redis 版 `VectorStoreConfig`，通过 profile 切换
7. 验证：仅修改配置即可在 SimpleVectorStore 和 Redis 之间切换，业务代码零修改
8. （可选）写 `RagDemoController`，演示用 DeepSeek + 检索结果做 RAG 生成

**完成标志**：可通过 profile 在两种向量库间切换；文档管理接口齐全。

---

### 阶段 3：用户系统 + 安全 + 登录前端

**目标**：引入用户体系，实现登录、JWT 鉴权、文档归属隔离；搭建前端项目，完成注册登录页面。采用"按功能纵切"的全栈开发方式——后端做完一个功能，立刻做对应的前端页面。

**后端任务**：
1. Flyway 迁移：创建 `users` 表
2. 用户实体 `User` + `UserRepository`
3. 用户注册接口 `POST /api/auth/register`
4. 用户登录接口 `POST /api/auth/login`
5. 引入 Spring Security + JWT（jjwt）
6. 编写 `JwtAuthenticationFilter`、`SecurityConfig`
7. 密码使用 BCrypt 存储
8. 文档表加 `user_id` 非空约束（Flyway 迁移）
9. 所有文档 / 检索接口做权限校验：用户只能操作自己的文档
10. 配置 CORS（允许前端开发服务器跨域访问）

**前端任务**：
11. 初始化前端项目（`frontend/` 目录，Vue 3 + TypeScript + Vite）
12. 搭建项目结构：路由（Vue Router）、HTTP 客户端（Axios）
13. Axios 请求拦截器：自动携带 `Authorization: Bearer <token>` 头
14. 注册页面
15. 登录页面 + JWT Token 本地存储
16. 路由守卫：未登录自动跳转登录页

**完成标志**：能在浏览器注册账号、登录、拿到 Token；未登录访问受保护页面被跳转到登录页；后端接口未携带有效 Token 返回 401。

---

### 阶段 4：文档管理 + RAG 前端

**目标**：完成文档上传、管理、检索、RAG 问答的完整前端界面。此阶段结束后，用户可以通过浏览器完成所有操作。

**任务清单**：
1. 整体布局：侧边栏导航 + 顶栏用户信息 / 退出登录
2. 文档上传页面（支持文件选择 + 上传状态展示）
3. 文档列表页面（展示状态、分块数、支持删除）
4. 检索页面（输入查询 → 展示 Top-K 结果）
5. RAG 问答页面（对话式界面，展示回答 + 引用来源）

**完成标志**：用户能在浏览器完成完整流程——注册 → 登录 → 上传文档 → 等待处理完成 → 检索 → RAG 问答 → 看到生成的回答和引用来源。

---

### 阶段 5：工程化

**目标**：让项目具备"可演示、可维护"的工程素质。

**任务清单**：
1. 引入 springdoc-openapi，自动生成 Swagger UI
2. 关键路径加日志（结构化）
3. 写若干单元测试（Service 层）和集成测试（Controller 层）
4. README：项目介绍、本地启动步骤、接口示例
5. （可选）GitHub Actions 跑测试

**完成标志**：陌生开发者按 README 能一键跑起整个项目。

---

## 七、开发规范

### 7.1 安全规范（最高优先级）
- **任何 API key、数据库密码、JWT secret 都禁止硬编码或写入 yaml 提交到 git**
- 敏感配置一律通过环境变量 `${DASHSCOPE_API_KEY}` 注入
- 本地开发用 `application-local.yaml` + `.gitignore` 隔离
- 提交前自查：`git diff` 一次，确认无敏感信息

### 7.2 代码规范
- 严格分层：Controller 不写业务、Service 不写 SQL、Repository 不写业务
- DTO 与 Entity 严格分离，Controller 不直接返回 Entity
- 命名：类用大驼峰，变量/方法用小驼峰，常量用 SCREAMING_SNAKE_CASE
- 包名小写，按"业务领域"组织而非"技术分层叠加业务"
- 异常通过自定义业务异常 + 全局异常处理器返回，不在 Controller 写 try-catch

### 7.3 协作规范
- 所有非 trivial 的代码改动，都必须先和用户对齐方案 → 得到批准 → 再写代码
- 任何对本计划的偏离，必须先更新 `PLAN.md`
- 每完成一个阶段，做一次小结和回顾

---

## 八、需要用户准备的本地环境

| 项目 | 说明 | 阶段 |
|---|---|---|
| DashScope API Key | 阿里云百炼控制台开通，免费额度足够学习 | 阶段 1 必需 |
| MySQL 8.x | 本机或 docker 均可，需要 root 密码或专用账号 | 阶段 1 必需 |
| Redis Stack 或 Redis 8+ | 必须支持向量搜索，普通 Redis 5/6/7 不行 | 阶段 2 必需 |
| Node.js 18+ | 前端构建需要 | 阶段 3 必需 |
| JDK 17 | 已具备 | 已具备 |
| Maven | 项目自带 mvnw，无需单独安装 | 已具备 |

---

## 九、计划修订原则

1. 本文档是**活文档**，会随着开发进展持续更新
2. 任何阶段开始前，先核对本文档对应章节，必要时先更新文档再写代码
3. 任何"新增需求 / 调整方向 / 替换技术栈"的决策，必须先在本文档留痕
4. 用户对开发节奏拥有最终决定权；助手只能在每一步前提出建议、等待批准

---

## 十、当前状态

- [x] 项目骨架已生成（Spring Boot + Spring AI 启动类）
- [x] 技术选型已确认
- [x] 实施计划已成文
- [x] 阶段 1 已完成（最小 RAG 闭环）
- [x] 阶段 2 已完成（REST API 完整化 + Redis 向量库 + RAG 演示）
- [x] 阶段 3 已完成（用户系统 + 安全 + 登录前端）
- [x] 阶段 4 已完成（文档管理 + RAG 前端）
- [x] 阶段 5 已完成（Swagger UI + README）
