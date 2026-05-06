# 用户信息管理设计说明

## 架构与模块边界

本期沿用模块化单体结构：

- `account` 模块负责用户资料查询、编辑和头像绑定。
- `order` 模块继续承载收货地址，因为订单创建会读取地址并生成地址快照。
- `catalog` 现有 `ImageUploadApplicationService` 作为图片存储基础能力，扩展上传目录参数以支持头像路径。
- `security` 继续通过 `SecurityUtils.currentUserId()` 获取当前登录用户。
- 前端在用户侧新增账户页面和 API 类型，复用 `ShopLayout`、`mallApi`、`auth` store。

边界原则：

- 用户资料 API 不接受 `userId` 参数，只操作当前登录用户。
- 地址 API 不接受 `userId` 参数，只操作当前登录用户的地址。
- 头像上传只返回文件信息；保存头像 URL 通过用户资料更新或头像 API 完成。

## 领域模型或数据模型变化

### `users` 表

新增字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `avatar_url` | `VARCHAR(512) NULL` | 用户头像 URL |
| `gender` | `VARCHAR(16) NOT NULL DEFAULT 'UNKNOWN'` | 性别枚举 |
| `birthday` | `DATE NULL` | 生日 |

对应修改：

- `UserPo` 增加 `avatarUrl`、`gender`、`birthday`。
- 登录返回的 `user` 增加 `nickname`、`avatarUrl`、`gender`、`birthday`。
- 新用户默认 `gender=UNKNOWN`，头像为空。

### `addresses` 表

本期不新增字段，继续使用：

- `receiver_name`
- `receiver_phone`
- `region`
- `detail`
- `is_default`

通过应用服务保证：

- 每个用户最多 20 条未删除地址。
- 同一用户最多一个默认地址。
- 删除默认地址后自动补默认。

## API 变化与请求/响应结构

### 查询当前用户资料

`GET /api/v1/account/profile`

响应：

```json
{
  "id": 1,
  "phone": "13800138000",
  "nickname": "会员8000",
  "avatarUrl": "/api/v1/files/avatars/2026-05-06/xxx.jpg",
  "gender": "UNKNOWN",
  "birthday": "1990-01-01",
  "role": "CUSTOMER",
  "createdAt": "2026-05-06T12:00:00"
}
```

### 更新当前用户资料

`PUT /api/v1/account/profile`

请求：

```json
{
  "nickname": "KK 用户",
  "avatarUrl": "/api/v1/files/avatars/2026-05-06/xxx.jpg",
  "gender": "FEMALE",
  "birthday": "1990-01-01"
}
```

响应返回更新后的资料对象。

### 上传头像

`POST /api/v1/account/avatar`

请求为 `multipart/form-data`，字段名 `file`。

响应：

```json
{
  "url": "/api/v1/files/avatars/2026-05-06/xxx.jpg",
  "publicUrl": "/api/v1/files/avatars/2026-05-06/xxx.jpg",
  "objectName": "avatars/2026-05-06/xxx.jpg"
}
```

限制：

- `image/*`
- 最大 2MB

### 地址接口扩展

沿用 `/api/v1/addresses`：

- `GET /api/v1/addresses`：列表，默认地址置顶。
- `POST /api/v1/addresses`：新增地址。
- `PUT /api/v1/addresses/{id}`：编辑地址。
- `DELETE /api/v1/addresses/{id}`：删除地址。
- `PATCH /api/v1/addresses/{id}/default`：设为默认地址。

地址请求结构：

```json
{
  "receiverName": "张三",
  "receiverPhone": "13800138000",
  "region": "上海市 浦东新区",
  "detail": "世纪大道 100 号",
  "isDefault": true
}
```

## 前端页面、组件与交互设计

### 路由

新增：

- `/account`：用户账户页面，需要登录。

### 顶部入口

`ShopLayout` 登录态菜单增加：

- 头像和昵称展示。
- “我的账户”跳转 `/account`。
- “我的订单”跳转 `/orders`。
- “退出登录”沿用现有逻辑。

### 我的账户页面

页面使用 Ant Design Vue：

- 顶部资料摘要：头像、昵称、手机号。
- 资料表单：昵称、性别、生日、头像上传。
- 地址管理：地址列表、默认标签、新增/编辑弹窗、删除确认、设为默认按钮。

交互：

- 头像上传成功后立即预览，并写入表单 `avatarUrl`。
- 保存资料成功后刷新 `auth` store 中的用户摘要。
- 地址操作成功后重新加载地址列表。

### 结算页

加载地址后：

1. 优先选择 `isDefault` 地址。
2. 没有默认地址时选择第一条地址。
3. 没有地址时继续展示新增地址表单。

## 状态流和核心业务流程

### 资料保存

```text
用户进入 /account
  -> 前端请求 GET /account/profile
  -> 填充资料表单
  -> 用户上传头像或编辑资料
  -> 前端请求 PUT /account/profile
  -> 后端校验并更新 users
  -> 前端刷新页面资料和顶部用户摘要
```

### 地址默认唯一

```text
用户新增/编辑/设默认地址
  -> 如果请求 isDefault=true 或设默认
  -> 后端先把当前用户其他地址 is_default 更新为 0
  -> 保存目标地址 is_default=1
  -> 返回地址列表时按 is_default desc, updated_at desc, id desc 排序
```

### 删除默认地址

```text
用户删除默认地址
  -> 后端软删除或逻辑删除该地址
  -> 查询当前用户剩余地址
  -> 如果仍有地址，选择最近更新的一条设为默认
```

## 异常处理与边界情况

- 未登录：返回 `AUTH_REQUIRED`，前端跳转登录。
- 资料不存在：返回 `USER_NOT_FOUND`。
- 昵称为空或过长：返回 `PROFILE_NICKNAME_INVALID`。
- 性别枚举非法：返回 `PROFILE_GENDER_INVALID`。
- 生日晚于当前日期：返回 `PROFILE_BIRTHDAY_INVALID`。
- 上传非图片：返回 `UPLOAD_IMAGE_ONLY`。
- 上传超过 2MB：返回 `UPLOAD_FILE_TOO_LARGE`。
- 地址不存在或不属于当前用户：返回 `ADDRESS_NOT_FOUND`。
- 地址数量超过 20：返回 `ADDRESS_LIMIT_EXCEEDED`。
- 地址字段为空或手机号格式不合法：返回对应业务错误。

## 兼容性、迁移与发布说明

- 新增 Flyway 迁移 `V5__user_profile_management.sql`，对已有 `users` 表补列。
- 现有用户默认 `gender='UNKNOWN'`，`avatar_url` 和 `birthday` 为空。
- 地址表不变，已有地址继续可用。
- 商品后台上传接口保持 `/api/v1/admin/uploads/images` 不变。
- 头像上传扩展同一个上传服务，不改变现有商品图片 URL 读取逻辑。

## 测试策略

后端：

- 单元或集成测试覆盖资料查询、资料更新、头像大小校验。
- 地址服务测试覆盖新增自动默认、最多 20 条、编辑、删除默认后补默认、跨用户隔离。

前端：

- `npm run build` 校验类型和打包。
- 手工验证登录后 `/account` 页面资料保存、头像上传、地址操作。
- 手工验证 `/checkout` 默认地址选择。

## 设计澄清记录

- 2026-05-06：采用渐进式增强方案：在现有 `account`、`order`、上传服务上补齐用户中心 MVP，不引入独立会员中心大模块。
- 2026-05-06：头像使用现有 MinIO/本地降级存储能力，新增 `avatars/` 对象路径；商品图片上传保持兼容。
- 2026-05-06：地址继续保存在 `order` 模块，避免本期迁移订单地址快照边界。
