# KKMall 商品详情页精简天猫版设计文档

## 1. 设计概述

本需求采用“`catalog` 轻量详情读模型 + 前端组件化详情页 + 后台基础维护”的方案。第一期不引入完整评价系统、推荐系统、营销系统或履约系统，而是在商品目录上下文内补齐商品详情页展示所需的数据、接口和管理能力。

核心原则：

1. 商品详情页前台只调用一个核心详情接口，避免前端拼接多个关键业务接口。
2. 交易主链路继续复用现有购物车、结算、订单、支付能力。
3. 评价、推荐、促销和服务承诺先做轻量展示与维护，不做复杂业务闭环。
4. 所有新增数据必须支持空值或空列表，保证旧商品仍可打开。

## 2. 架构与边界

### 2.1 后端边界

- `catalog`：承载商品详情增强，包括详情读模型、SKU 图片、商品参数、图文详情、促销文案、服务承诺、轻量评价和推荐配置。
- `cart`：不扩展领域模型，加入购物车继续由现有接口做最终库存和 SKU 校验。
- `order`：不改订单模型，立即购买第一期复用购物车到结算链路。
- `fulfillment`：不接入真实配送时效，详情页仅展示运营维护的配送/服务说明。
- `account/payment`：不受影响，未登录购买沿用现有登录拦截和 JWT 鉴权。

### 2.2 前端边界

商品详情页由页面级容器组合多个商品组件：

- `ProductGallery`：主图、缩略图、占位和图片错误降级。
- `ProductSummary`：品牌、标题、副标题、销量、价格、优惠、卖点。
- `SkuSelector`：SKU 文本/图片、选中态、售罄态、禁用态。
- `ProductPurchasePanel`：数量、库存提示、加入购物车、立即购买。
- `ProductDetailTabs`：评价、参数、图文详情、购买须知。
- `ProductRecommendations`：本店推荐和看了又看。
- `MobileBuyBar`：移动端底部固定购买栏。

`ProductDetailPage.vue` 只负责路由参数、接口调用、页面状态、组件组合和购买动作编排。

## 3. 数据模型

### 3.1 复用现有商品字段

继续复用 `products` 已有字段：`brand`、`subtitle`、`selling_points`、`detail_html`、`attributes`、`main_image`、`sales_count`、`sort_order`。

继续复用 `skus` 已有字段：`sku_code`、`specs`、`price`、`market_price`、`stock`、`enabled`。

### 3.2 新增字段与表

新增 Flyway：`V4__product_detail_tmall_lite.sql`。

建议变更：

- `skus.image_url`：SKU 规格图片。
- `product_parameters`：结构化商品参数，字段包含 `product_id`、`name`、`value`、`sort_order`、`enabled`。
- `product_service_promises`：服务承诺，字段包含 `product_id`、`title`、`description`、`icon`、`sort_order`、`enabled`。
- `product_promotion_texts`：轻量促销文案，字段包含 `product_id`、`title`、`description`、`label`、`start_at`、`end_at`、`sort_order`、`enabled`。
- `product_reviews`：轻量评价展示，字段包含 `product_id`、`sku_id`、`user_nickname`、`rating`、`content`、`image_urls_json`、`tags_json`、`reviewed_at`、`status`、`sort_order`。
- `product_recommendations`：推荐配置，字段包含 `source_product_id`、`target_product_id`、`scene`、`sort_order`、`enabled`。

JSON 列表字段第一期使用 `TEXT` 存储，由应用层序列化，降低 MySQL 版本差异风险。

## 4. API 设计

### 4.1 前台详情接口

扩展现有接口：

```text
GET /api/v1/products/{id}
```

返回在现有字段基础上新增：

- `imageUrls`：商品图集。
- `priceRange`：最低价、最高价、币种。
- `tags`：商品标签。
- `skus[].imageUrl`：SKU 图片。
- `skus[].originPrice`：划线价，兼容现有 `marketPrice`。
- `skus[].sellable`：是否可购买。
- `promotions`：轻量促销文案。
- `servicePromises`：服务承诺。
- `parameters`：参数列表。
- `reviewSummary`：评价数量、好评率、平均分、标签。
- `reviews`：代表性评价列表。
- `storeRecommendations`：本店推荐。
- `relatedRecommendations`：看了又看。

旧字段 `images`、`mainImage`、`coverImage`、`skus[].marketPrice` 保留，保证旧前端类型兼容。

### 4.2 后台维护接口

新增或扩展：

```text
GET /api/v1/admin/products/{productId}/detail-config
PUT /api/v1/admin/products/{productId}/detail-config
POST /api/v1/admin/products/{productId}/reviews
PUT /api/v1/admin/products/{productId}/reviews/{reviewId}
DELETE /api/v1/admin/products/{productId}/reviews/{reviewId}
PUT /api/v1/admin/products/{productId}/recommendations
```

`detail-config` 一次性维护商品参数、服务承诺、促销文案、SKU 图片、图文详情和基础展示字段。评价与推荐可以独立维护，避免商品编辑主表单过重。

## 5. 前端交互设计

### 5.1 桌面端

首屏采用左右结构：

- 左侧：商品图廊，主图 1:1，缩略图列表。
- 右侧：商品信息和购买面板，按标题、价格优惠、服务承诺、SKU、数量、购买按钮排列。

下方采用 Tab 或分区结构：

- 用户评价。
- 参数信息。
- 图文详情。
- 本店推荐。
- 看了又看。

### 5.2 移动端

移动端采用纵向结构：

- 商品图在顶部。
- 商品信息、价格、服务、SKU、数量依次展示。
- 底部固定购买栏展示加入购物车和立即购买。
- 长内容按分区展示，避免复杂吸顶嵌套。

### 5.3 页面状态

详情页需要支持：

- `loading`：加载中骨架或 Spin。
- `error`：接口失败展示重试。
- `notFound`：商品不存在或下架展示结果页。
- `addingCart`：加购按钮 loading。
- `selectedSku`：当前 SKU 派生价格、库存、图片和按钮状态。
- `quantityMax`：当前 SKU 库存上限。
- 图片加载失败降级。
- 评价、参数、详情、推荐为空时的空态。

## 6. 关键规则

1. 商品不存在或非上架状态时，前台详情接口返回现有统一异常。
2. SKU `enabled=false` 或库存为 0 时，`sellable=false`。
3. 推荐商品必须过滤当前商品，只返回上架商品。
4. 促销文案按 `enabled` 和时间窗口过滤。
5. 加入购物车和立即购买不能只依赖前端判断，后端购物车仍做最终校验。
6. `detailHtml` 第一阶段仅渲染受控后台内容；实现时需要选择 HTML 净化或限制输入方式。
7. 立即购买第一期复用购物车结算链路，不新增订单模型。

## 7. 兼容与迁移

1. 新增表和字段允许空值，旧商品详情可正常打开。
2. 保留现有详情 API 字段，避免破坏首页、购物车和现有详情页调用。
3. Flyway 只新增 `V4`，不修改历史 `V1/V2/V3`。
4. 种子数据可为现有商品补充少量评价、服务承诺、促销文案和推荐关系，便于验收。

## 8. 测试策略

后端验证：

- Flyway 空库和已有库迁移通过。
- 商品详情接口返回完整扩展结构。
- 空数据降级返回空数组或空对象，不抛异常。
- SKU 可售状态、推荐过滤、促销时间窗口正确。
- 后台保存详情配置后，前台详情接口可读取最新数据。
- 购物车仍拒绝无效 SKU 和库存不足 SKU。

前端验证：

- 桌面端详情页首屏和下方模块展示正确。
- 移动端底部购买栏可用，不遮挡内容。
- SKU 切换联动价格、库存、图片和数量上限。
- 加购成功留在当前页面并提示成功。
- 立即购买进入结算链路。
- 图片、评价、推荐、参数、图文详情为空时展示正常。

## 9. 设计澄清记录

1. 采用 `catalog` 内轻量详情模型，不引入独立评价、推荐、营销和履约子系统。
2. 前台详情页以单接口为核心，后台维护按详情配置分组。
3. 立即购买第一期复用购物车到结算链路，不改订单模型。
4. 评价第一期为轻量展示数据，不接订单评价闭环。
5. 推荐第一期使用后台配置或同类目/热销降级，不做个性化算法。
6. 移动端底部购买栏纳入验收。
