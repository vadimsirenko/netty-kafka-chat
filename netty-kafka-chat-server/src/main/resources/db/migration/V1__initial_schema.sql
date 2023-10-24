create table room
(
    id uuid not null primary key,
    name varchar(50)
);
create table client
(
    id uuid not null primary key,
    login varchar(50),
    nick_name varchar(30),
    email varchar(50),
    token varchar(255)
);
create table message
(
    id uuid not null primary key,
    sender_id uuid not null,
    sender character varying(30),
    room_id uuid null,
    message_text varchar(1024),
    ts bigint
);