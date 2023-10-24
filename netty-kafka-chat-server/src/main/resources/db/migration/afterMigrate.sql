TRUNCATE TABLE client;
INSERT INTO client(
    id, login, nick_name, email, token)
VALUES
    ('a5461723-b1de-4351-a2f0-94bc2e1a2f92', 'vadim', 'Вадим', 'vadim@mail.ru', '1234567'),
    ('f9b26b13-6a8e-41e0-9332-9b14eaa6d60a', 'sergey', 'Сергей', 'sergey@mail.ru', '1234567'),
    ('92aeaf7c-39e3-49a7-b69a-18de73efb479', 'oleg', 'Олег', 'oleg@mail.ru', '1234567');

TRUNCATE TABLE room;
INSERT INTO room(id, name)
VALUES
    ('2bd09cbf-ef16-469f-82ab-f51ae9913aa0', 'Первый чат'),
    ('643c0714-a948-42c3-946a-a4818896a219', 'Второй чат'),
    ('54a72323-1be5-460e-a4c4-43f2cb227a1f', 'Третий чат');

TRUNCATE TABLE message;
INSERT INTO public.message(
    id, sender_id, sender, room_id, message_text, ts)
VALUES
    ('fd2d7cc1-5ac9-40e9-b4f7-fc518eedd095','a5461723-b1de-4351-a2f0-94bc2e1a2f92','Вадим', '2bd09cbf-ef16-469f-82ab-f51ae9913aa0', 'Our use case is simple, the server will simply transform the request body and query parameters, if any, to uppercase. A word of caution here on reflecting request data in the response – we are doing this only for demonstration purposes, to understand how we can use Netty to implement an HTTP server.', 4324324),
    ('057a7522-df71-4406-9559-844e7ce7cf4c','a5461723-b1de-4351-a2f0-94bc2e1a2f92','Вадим', '2bd09cbf-ef16-469f-82ab-f51ae9913aa0', 'As we can see, when our channel receives an HttpRequest, it first checks if the request expects a 100 Continue status. In that case, we immediately write back with an empty response with a status of CONTINUE',3254356),
    ('3eac2bf1-e223-4bbf-a51e-2c3d3ef71cbc','92aeaf7c-39e3-49a7-b69a-18de73efb479','Вадим', '2bd09cbf-ef16-469f-82ab-f51ae9913aa0', 'Can anyone provide an example of a simple HTTP server implemented using Netty, that supports persistent HTTP connections. In other words, it won''t close the connection until the client closes it, and can receive additional HTTP requests over the same connection?',54366534),
    ('9c2ccdad-5aca-474b-8151-f8368a65b844','f9b26b13-6a8e-41e0-9332-9b14eaa6d60a','Сергей', '2bd09cbf-ef16-469f-82ab-f51ae9913aa0', 'In other words, it won''t close the connection until the client closes it, and can receive additional HTTP requests over the same connection?',3543535),
    ('65eda846-3dfe-48b1-8c2b-8304e047c544','a5461723-b1de-4351-a2f0-94bc2e1a2f92','Вадим', '2bd09cbf-ef16-469f-82ab-f51ae9913aa0', 'Netty — это фреймвёрк, позволяющий разрабатывать высокопроизводительные сетевые приложения. Подробнее о нём можно прочитать на сайте проекта. Для создания сокет-серверов Netty предоставляет весьма удобный функционал, но для создание REST-серверов данный функционал, на мой взгляд, является не очень удобным.',54376765),
    ('0ea840e0-8c9f-4a4e-93ef-5cb75a28027e','f9b26b13-6a8e-41e0-9332-9b14eaa6d60a','Сергей', '2bd09cbf-ef16-469f-82ab-f51ae9913aa0', 'Вот, в общем-то, и всё. Используя указанные выше методы, можно обрабатывать http-запросы. Правда, обрабатывать всё придётся в одном месте, а именно в методе channelRead. Даже если разнести логику обработки запросов по разным методам и классам, всё равно придётся сопоставлять URL с этими методами где-то в одном месте.',76457543);