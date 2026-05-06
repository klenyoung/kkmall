# KKMall MVP 部署与联调 Runbook

## 1. 目标

将当前 MVP 从静态本地演示推进到真实后端 + MySQL 联调。

当前已具备：

- 前端静态 MVP：`frontend/`
- 后端 Spring Boot MVP：`backend/`
- MySQL 建表脚本：`backend/src/main/resources/db/schema.sql`
- MySQL 种子数据：`backend/src/main/resources/db/seed.sql`

## 2. 安全约定

- 不把服务器、MySQL、RabbitMQ、MinIO 密码写入仓库。
- 本地和服务器运行时通过环境变量或服务器侧 `.env` 注入。
- `.env` 已被 `.gitignore` 忽略。

## 3. MySQL 初始化

在能访问 MySQL 的机器上执行：

```bash
mysql -h <mysql-host> -u root -p < backend/src/main/resources/db/schema.sql
mysql -h <mysql-host> -u root -p < backend/src/main/resources/db/seed.sql
```

执行后应具备：

- 数据库：`kkmall`
- 管理员用户：见 `seed.sql`
- 初始分类、商品、SKU 数据

## 4. 后端构建

本地构建：

```bash
cd backend
mvn "-Dhttps.protocols=TLSv1.2" -DskipTests package
```

产物：

```text
backend/target/kkmall-backend-0.1.0.jar
```

## 5. 后端运行环境变量

```bash
export SERVER_PORT=8080
export MYSQL_URL='jdbc:mysql://<mysql-host>:3306/kkmall?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
export MYSQL_USER='root'
export MYSQL_PASSWORD='<mysql-password>'
export KKMALL_AUTH_SECRET='<long-random-secret>'
export KKMALL_MOCK_CODE='123456'
```

启动：

```bash
java -jar backend/target/kkmall-backend-0.1.0.jar
```

健康检查：

```bash
curl http://<backend-host>:8080/api/v1/categories
```

## 6. 前端联调下一步

当前前端仍使用 `localStorage` 模拟数据。下一步需要：

1. 新增前端 API Client。
2. 将登录、商品、购物车、地址、订单、支付、后台发货逐步切到 HTTP API。
3. 保留 `localStorage` 模式作为离线演示模式。

建议配置：

```js
window.KKMALL_API_BASE = "http://<backend-host>:8080";
```

## 7. RabbitMQ 与 MinIO

当前 MVP 后端未使用 RabbitMQ 和 MinIO。

后续接入建议：

- RabbitMQ：订单支付成功、发货成功等事件异步通知。
- MinIO：商品图片上传和存储。

这两项进入下一阶段前，需要补详细设计和接口边界。

## 8. 当前连通性检查结果

在当前开发环境中，以下端口已检查为可达：

| 服务 | 主机 | 端口 | 结果 |
| --- | --- | --- | --- |
| SSH | `192.168.115.31` | `22` | 可达 |
| MySQL | `192.168.115.31` | `3306` | 可达 |
| RabbitMQ | `192.168.115.31` | `5672` | 可达 |
| MinIO | `192.168.115.31` | `9000` | 可达 |

## 9. systemd 部署模板

仓库提供了：

- `deploy/kkmall-backend.service`
- `deploy/deploy-backend.sh`

推荐流程：

1. 将 `backend/target/kkmall-backend-0.1.0.jar`、`deploy/kkmall-backend.service`、`deploy/deploy-backend.sh` 上传到服务器同一目录。
2. 在服务器执行 `bash deploy-backend.sh`。
3. 编辑 `/opt/kkmall/backend/.env`，填入真实 MySQL 密码和 `KKMALL_AUTH_SECRET`。
4. 重启服务：

```bash
sudo systemctl restart kkmall-backend
```

查看日志：

```bash
journalctl -u kkmall-backend -f
```
