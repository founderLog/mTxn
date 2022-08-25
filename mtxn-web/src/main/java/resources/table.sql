-- auto-generated definition
create table com_order
(
    id      int auto_increment
        primary key,
    name    varchar(200) null,
    orderNo  varchar(100) null,
    createTime    timestamp default current_timestamp(),
    address varchar(500) null
);

-- auto-generated definition
create table com_stock
(
    id      int auto_increment
        primary key,
    name    varchar(200) null,
    amount  bigint null,
    lastModifyTime    timestamp default current_timestamp()
);