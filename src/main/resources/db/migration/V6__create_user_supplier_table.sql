CREATE TABLE user_supplier (
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    supplier_id INTEGER NOT NULL REFERENCES supplier(supplier_id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, supplier_id)
);
