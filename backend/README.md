# KKMall Backend

Spring Boot 2.7 + JDBC + MySQL backend for the KKMall MVP.

Java 11 is used because the current local environment is Java 11.

## Implemented APIs

- `POST /api/v1/auth/mock-code`
- `POST /api/v1/auth/login`
- `POST /api/v1/admin/auth/login`
- `GET /api/v1/categories`
- `GET /api/v1/products`
- `GET /api/v1/products/{id}`
- `GET /api/v1/cart`
- `POST /api/v1/cart/items`
- `PATCH /api/v1/cart/items/{id}`
- `DELETE /api/v1/cart/items/{id}`
- `GET /api/v1/addresses`
- `POST /api/v1/addresses`
- `POST /api/v1/orders`
- `GET /api/v1/orders`
- `GET /api/v1/orders/{id}`
- `POST /api/v1/payments/mock`
- `GET /api/v1/admin/products`
- `POST /api/v1/admin/products`
- `PUT /api/v1/admin/products/{id}`
- `PATCH /api/v1/admin/products/{id}/status`
- `GET /api/v1/admin/orders`
- `GET /api/v1/admin/orders/{id}`
- `POST /api/v1/admin/orders/{id}/shipment`

## Database initialization

Run the SQL files against MySQL:

```bash
mysql -h <mysql-host> -u root -p < src/main/resources/db/schema.sql
mysql -h <mysql-host> -u root -p < src/main/resources/db/seed.sql
```

## Local run

Copy `.env.example` to `.env` or set environment variables manually.

Required:

```bash
MYSQL_URL=jdbc:mysql://<mysql-host>:3306/kkmall?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
MYSQL_USER=root
MYSQL_PASSWORD=<password>
KKMALL_AUTH_SECRET=<long-random-secret>
```

Build:

```bash
mvn "-Dhttps.protocols=TLSv1.2" -DskipTests package
```

Run:

```bash
java -jar target/kkmall-backend-0.1.0.jar
```

## Auth

User login:

- Any valid 11-digit phone number.
- Mock code: `123456`.

Admin login:

- Seeded admin phone is configured in `seed.sql`.
- Mock code: `123456`.

## Notes

RabbitMQ and MinIO are reserved for later phases. The current MVP backend does not require them because it does not implement async messaging or image upload yet.
