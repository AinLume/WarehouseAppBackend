CREATE TABLE user_product (
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES product(product_id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, product_id)
);
