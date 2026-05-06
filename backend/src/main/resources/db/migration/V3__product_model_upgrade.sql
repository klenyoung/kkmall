ALTER TABLE products
  ADD COLUMN brand VARCHAR(64) NULL AFTER title,
  ADD COLUMN subtitle VARCHAR(255) NULL AFTER brand,
  ADD COLUMN selling_points JSON NULL AFTER description,
  ADD COLUMN unit VARCHAR(32) NOT NULL DEFAULT '件' AFTER selling_points,
  ADD COLUMN detail_html TEXT NULL AFTER unit,
  ADD COLUMN attributes JSON NULL AFTER detail_html,
  ADD COLUMN main_image VARCHAR(512) NULL AFTER attributes,
  ADD COLUMN sales_count INT NOT NULL DEFAULT 0 AFTER main_image,
  ADD COLUMN sort_order INT NOT NULL DEFAULT 0 AFTER sales_count;

ALTER TABLE skus
  ADD COLUMN sku_code VARCHAR(64) NULL AFTER product_id,
  ADD COLUMN specs JSON NULL AFTER spec_value,
  ADD COLUMN market_price BIGINT NULL AFTER price,
  ADD COLUMN cost_price BIGINT NULL AFTER market_price,
  ADD COLUMN weight_grams INT NULL AFTER stock,
  ADD COLUMN barcode VARCHAR(64) NULL AFTER weight_grams,
  ADD COLUMN enabled TINYINT NOT NULL DEFAULT 1 AFTER barcode;

UPDATE products
SET
  brand = COALESCE(brand, 'KKMall 自营'),
  subtitle = COALESCE(subtitle, description),
  selling_points = COALESCE(selling_points, JSON_ARRAY('自营好物', '库存实时校验', '满 99 元包邮')),
  detail_html = COALESCE(detail_html, description),
  attributes = COALESCE(attributes, JSON_OBJECT('服务', '单商家自营', '配送', '商家手动发货')),
  main_image = COALESCE(main_image, JSON_UNQUOTE(JSON_EXTRACT(images, '$[0]'))),
  unit = COALESCE(unit, '件');

UPDATE skus
SET
  sku_code = COALESCE(sku_code, CONCAT('SKU-', id)),
  specs = COALESCE(specs, JSON_OBJECT(spec_name, spec_value)),
  market_price = COALESCE(market_price, price),
  cost_price = COALESCE(cost_price, price),
  weight_grams = COALESCE(weight_grams, 500),
  enabled = COALESCE(enabled, 1);

CREATE INDEX idx_products_sort_status ON products (status, sort_order, id);
CREATE INDEX idx_skus_enabled_product ON skus (product_id, enabled);
