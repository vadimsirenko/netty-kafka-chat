create table room
(
    id   uuid not null primary key,
    name varchar(50)
);
create table client
(
    id   uuid not null primary key,
    name varchar(50)
);
create table message
(
    id   uuid not null primary key,
    sender_id uuid not null,
    recipient_id uuid null,
    room_id uuid null,
    message_text varchar(1024),
    ts bigint
);