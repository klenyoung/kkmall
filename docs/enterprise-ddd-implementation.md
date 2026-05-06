# KKMall 企业级 DDD 改造说明

## 后端结构

- `account`：用户、角色、手机号登录、管理员登录。
- `catalog`：分类、商品、SKU、上下架。
- `cart`：购物车与购物项。
- `order`：订单聚合、订单明细、地址快照、订单状态。
- `payment`：模拟支付与库存扣减。
- `fulfillment`：发货和物流单号。
- `common`：统一响应、异常、Money 值对象、MyBatis-Plus 配置。
- `security`：Spring Security、JWT、RBAC。

## 部署

```bash
docker compose up -d --build
```

服务端口：

- 前端：`http://localhost`
- 后端：`http://localhost:8080`
- MySQL：`3306`
- RabbitMQ：`15672`
- MinIO Console：`9001`
