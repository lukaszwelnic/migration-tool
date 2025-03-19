DROP TABLE IF EXISTS order_products CASCADE;

CREATE TABLE order_products (
  order_id INT NOT NULL,
  product_id INT NOT NULL,
  PRIMARY KEY (order_id, product_id),
  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);