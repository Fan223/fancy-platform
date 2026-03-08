CREATE TABLE sys_user (
    id          bigint primary key,
    username    varchar(64),
    age         int,
    delete_time timestamp,
    create_time timestamp,
    update_time timestamp
);
