# KKMall 企业级电商平台

KKMall 是一个单商家自营电商 MVP，当前已升级为企业级前后端分离项目：

- 后端：Spring Boot、Spring Security、MyBatis-Plus、Flyway、战术 DDD 分层。
- 前端：Vue3、Vite、TypeScript、Pinia、Vue Router、Ant Design Vue。
- 部署：Docker Compose 编排 frontend、backend、MySQL、RabbitMQ、MinIO。

## 运行

后端本地运行：

```bash
cd backend
mvn test
mvn spring-boot:run
```

前端本地运行：

```bash
cd frontend
npm install
npm run dev
```

Compose 运行：

```bash
docker compose up -d --build
```

## 登录

- 用户端：任意 11 位手机号，验证码 `123456`
- 后台：手机号 `13900000000`，验证码 `123456`

## 文档

- PRD：`docs/prd.md`
- DDD 改造说明：`docs/enterprise-ddd-implementation.md`
- 部署 Runbook：`docs/deploy-runbook.md`
