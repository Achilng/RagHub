# RagHub

基于 Spring Boot + Spring AI 构建的 RAG-as-a-Service 平台。用户上传文档后，系统自动完成解析、分块、Embedding 生成及向量入库，对外提供标准化 RESTful API 和 Web 界面进行检索增强问答。

## 技术栈

**后端**：Java 17 / Spring Boot 3.5 / Spring AI 1.1 / Spring Security + JWT / Spring Data JPA / Flyway / MySQL 8 / Redis Stack

**前端**：Vue 3 + TypeScript / Vite / Vue Router / Axios

**AI 模型**：通义 DashScope（Embedding）/ DeepSeek（LLM 生成）

## 本地启动

### 前置要求

- JDK 17+
- Maven 3.9+
- MySQL 8.x
- Redis Stack 或 Redis 8+（需支持向量搜索）
- Node.js 18+

### 1. 数据库准备

```sql
CREATE DATABASE raghub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'raghub_app'@'localhost' IDENTIFIED BY '你的密码';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON raghub.* TO 'raghub_app'@'localhost';
FLUSH PRIVILEGES;
```

### 2. 环境变量

在 IDEA Run Configuration 或系统环境中配置：

| 变量名 | 说明 |
|---|---|
| `DASHSCOPE_API_KEY` | 阿里云百炼 DashScope API Key |
| `DEEPSEEK_API_KEY` | DeepSeek API Key |
| `RAGHUB_DB_PASSWORD` | MySQL raghub_app 用户密码 |
| `RAGHUB_JWT_SECRET` | JWT 签名密钥（至少 32 个字符） |

### 3. 启动后端

```bash
mvn spring-boot:run
```

后端启动在 `http://localhost:8080`，Flyway 自动执行数据库迁移。

API 文档：`http://localhost:8080/swagger-ui/index.html`

### 4. 启���前端

```bash
cd frontend
npm install
npm run dev
```

前端启动在 `http://localhost:5173`，API 请求通过 Vite 代理转发到后端。

## 使用流程

1. 打开 `http://localhost:5173`，注册账号并登录
2. 在「文档管理」页面上传文本文件（支持 txt / pdf / docx 等）
3. 等待文档状态变为「已完成」
4. 在「RAG 问答」页面输入问题，系统基于上传的文档内容生成回答

## API 概览

| 方法 | 路径 | 说明 | 认证 |
|---|---|---|---|
| POST | `/api/auth/register` | 注册 | 否 |
| POST | `/api/auth/login` | 登录 | 否 |
| GET | `/api/documents` | 文档列表 | 是 |
| POST | `/api/documents` | 上���文档 | 是 |
| DELETE | `/api/documents/{id}` | 删除文档 | 是 |
| POST | `/api/search` | 向量检索 | 是 |
| POST | `/api/rag` | RAG 问答 | 是 |

认证方式：请求头 `Authorization: Bearer <token>`

## 项目结构

```
RagHub/
├── frontend/                  Vue 3 前端
│   └── src/
│       ├── api/               API 封装（Axios）
│       ├── router/            路由 + 守卫
│       └── views/             页面组件
└── src/main/
    ├── java/.../raghub/
    │   ├── config/            配置类（Security、VectorStore、OpenAPI��
    │   ├── controller/        REST 控制器
    │   ├── service/           业务逻辑
    │   ├── repository/        数据访问层
    │   ├── entity/            JPA 实体
    │   ├── dto/               请求/响应对象
    │   ├── security/          JWT 相关
    │   └── exception/         异常处理
    └── resources/
        ├── application.yaml   应用配置
        └── db/migration/      Flyway 迁移脚本
```
