create table if not exists category (
    category_id serial primary key,
    title varchar(100) not null check (length(trim(title)) > 0) unique
);

create table if not exists product (
    product_id bigserial primary key,
    category_id int not null references category (category_id) on delete restrict on update cascade,
    title varchar(100) not null check (length(trim(title)) > 0),
    description text check (length(trim(description)) > 0),
    length integer not null,
    width integer not null,
    height integer not null
);

create table if not exists supplier (
    supplier_id serial primary key,
    name varchar(100) not null check (length(trim(name)) > 0),
    phone varchar(12)  not null check (phone ~ '^\+7\d{10}$|^8\d{10}$'),
    email varchar(100) check (email ~ '^[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}$'),
    address varchar
);

create table if not exists warehouse (
    warehouse_id serial primary key,
    title varchar(100),
    address varchar not null check (length(trim(address)) > 0),
    capacity integer,
    width bigint not null,
    length bigint not null,
    height bigint not null
);

create table if not exists supply (
    supply_id bigserial primary key,
    supplier_id int not null references supplier (supplier_id) on delete restrict,
    status varchar(20) not null check (
    status in ('CREATED','PENDING','CONFIRMED','SHIPPED','DELIVERED','COMPLETED','CANCELLED')),
    total_price bigint not null default 0 check (total_price >= 0),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists warehouse_product (
    warehouse_product_id bigserial primary key,
    warehouse_id int not null references warehouse (warehouse_id) on delete restrict,
    product_id bigint not null references product (product_id) on delete restrict,
    quantity int not null check (quantity >= 0),
    total_price bigint not null check (total_price >= 0),

    constraint uq_warehouse_product unique (warehouse_id, product_id)
);

create table if not exists warehouse_supply (
    warehouse_supply_id bigserial primary key,
    warehouse_id int not null references warehouse (warehouse_id) on delete restrict,
    supply_id bigint not null references supply (supply_id) on delete cascade,

    constraint uq_warehouse_supply unique (warehouse_id, supply_id)
);

create table if not exists supply_product (
    supply_product_id bigserial primary key,
    supply_id bigint not null references supply (supply_id) on delete cascade,
    product_id bigint not null references product (product_id) on delete restrict,
    quantity int not null check (quantity > 0),
    unit_price bigint not null check (unit_price > 0),

    constraint uq_supply_product unique (supply_id, product_id)
);