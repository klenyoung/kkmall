USE kkmall;

INSERT IGNORE INTO users (id, phone, nickname, role) VALUES
  (1, '13900000000', '管理员', 'ADMIN');

INSERT IGNORE INTO categories (id, name, sort_order, enabled) VALUES
  (1, '服饰', 1, 1),
  (2, '数码', 2, 1),
  (3, '家居', 3, 1);

INSERT IGNORE INTO products (id, category_id, title, description, images, status) VALUES
  (1, 1, '舒适纯棉基础款 T 恤', '柔软棉感，适合日常通勤与周末出行。', JSON_ARRAY('T'), 'ON_SALE'),
  (2, 1, '轻量防泼水通勤双肩包', '多隔层收纳，轻量耐磨。', JSON_ARRAY('B'), 'ON_SALE'),
  (3, 2, '蓝牙降噪耳机 Pro', '舒适佩戴，长续航。', JSON_ARRAY('E'), 'ON_SALE'),
  (4, 2, '桌面无线快充支架', '立式观看，随放随充。', JSON_ARRAY('C'), 'ON_SALE'),
  (5, 3, '北欧风陶瓷马克杯', '温润釉面，容量适中。', JSON_ARRAY('M'), 'ON_SALE'),
  (6, 3, '可折叠收纳整理箱', '稳固叠放，透明可视。', JSON_ARRAY('S'), 'ON_SALE');

INSERT IGNORE INTO skus (id, product_id, spec_name, spec_value, price, stock) VALUES
  (101, 1, '颜色', '白色', 9900, 20),
  (102, 1, '颜色', '黑色', 9900, 12),
  (201, 2, '颜色', '曜石黑', 15900, 9),
  (202, 2, '颜色', '雾灰', 15900, 6),
  (301, 3, '颜色', '云白', 29900, 8),
  (302, 3, '颜色', '夜黑', 29900, 5),
  (401, 4, '颜色', '银灰', 8900, 14),
  (501, 5, '容量', '350ml', 6900, 25),
  (601, 6, '尺寸', '中号', 7900, 18);
