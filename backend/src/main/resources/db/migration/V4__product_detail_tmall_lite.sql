ALTER TABLE skus
  ADD COLUMN image_url VARCHAR(512) NULL AFTER specs;

CREATE TABLE product_parameters (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  name VARCHAR(64) NOT NULL,
  value VARCHAR(255) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_product_parameters_product (product_id, enabled, sort_order)
);

CREATE TABLE product_service_promises (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  title VARCHAR(64) NOT NULL,
  description VARCHAR(255) NULL,
  icon VARCHAR(64) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_product_service_promises_product (product_id, enabled, sort_order)
);

CREATE TABLE product_promotion_texts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  title VARCHAR(64) NOT NULL,
  description VARCHAR(255) NULL,
  label VARCHAR(32) NULL,
  start_at DATETIME NULL,
  end_at DATETIME NULL,
  sort_order INT NOT NULL DEFAULT 0,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_product_promotion_texts_product (product_id, enabled, sort_order),
  KEY idx_product_promotion_texts_time (start_at, end_at)
);

CREATE TABLE product_reviews (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  sku_id BIGINT NULL,
  user_nickname VARCHAR(64) NOT NULL,
  rating INT NOT NULL DEFAULT 5,
  content VARCHAR(1000) NOT NULL,
  image_urls_json TEXT NULL,
  tags_json TEXT NULL,
  reviewed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(20) NOT NULL DEFAULT 'VISIBLE',
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_product_reviews_product (product_id, status, sort_order, reviewed_at),
  KEY idx_product_reviews_sku (sku_id)
);

CREATE TABLE product_recommendations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  source_product_id BIGINT NOT NULL,
  target_product_id BIGINT NOT NULL,
  scene VARCHAR(32) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_product_recommendations_source (source_product_id, scene, enabled, sort_order),
  KEY idx_product_recommendations_target (target_product_id)
);

INSERT INTO product_service_promises (product_id, title, description, icon, sort_order)
SELECT id, '满99包邮', '订单商品金额满 99 元免运费', 'truck', 10 FROM products WHERE deleted = 0;

INSERT INTO product_service_promises (product_id, title, description, icon, sort_order)
SELECT id, '自营保障', 'KKMall 单商家自营商品', 'shield', 20 FROM products WHERE deleted = 0;

INSERT INTO product_promotion_texts (product_id, title, description, label, sort_order)
SELECT id, '今日精选好物', '下单、支付、发货、订单查询都能体验', '精选', 10 FROM products WHERE deleted = 0;

INSERT INTO product_parameters (product_id, name, value, sort_order)
SELECT id, '品牌', COALESCE(brand, 'KKMall 自营'), 10 FROM products WHERE deleted = 0;

INSERT INTO product_parameters (product_id, name, value, sort_order)
SELECT id, '服务', '单商家自营', 20 FROM products WHERE deleted = 0;

INSERT INTO product_reviews (product_id, sku_id, user_nickname, rating, content, tags_json, reviewed_at, sort_order)
SELECT p.id, MIN(s.id), '匿名买家', 5, '产品符合预期，页面展示清楚，整体体验不错。', JSON_ARRAY('质量好', '发货快'), DATE_SUB(NOW(), INTERVAL 3 DAY), 10
FROM products p
LEFT JOIN skus s ON s.product_id = p.id AND s.deleted = 0
WHERE p.deleted = 0
GROUP BY p.id;

INSERT INTO product_recommendations (source_product_id, target_product_id, scene, sort_order)
SELECT p1.id, p2.id, 'STORE_RECOMMEND', 10
FROM products p1
JOIN products p2 ON p2.id <> p1.id AND p2.deleted = 0 AND p2.status = 'ON_SALE'
WHERE p1.deleted = 0
  AND p1.status = 'ON_SALE'
  AND p2.id = (
    SELECT MIN(p3.id) FROM products p3
    WHERE p3.deleted = 0 AND p3.status = 'ON_SALE' AND p3.id <> p1.id
  );

INSERT INTO product_recommendations (source_product_id, target_product_id, scene, sort_order)
SELECT p1.id, p2.id, 'RELATED_RECOMMEND', 10
FROM products p1
JOIN products p2 ON p2.id <> p1.id AND p2.deleted = 0 AND p2.status = 'ON_SALE' AND p2.category_id = p1.category_id
WHERE p1.deleted = 0
  AND p1.status = 'ON_SALE'
  AND p2.id = (
    SELECT MIN(p3.id) FROM products p3
    WHERE p3.deleted = 0 AND p3.status = 'ON_SALE' AND p3.category_id = p1.category_id AND p3.id <> p1.id
  );
