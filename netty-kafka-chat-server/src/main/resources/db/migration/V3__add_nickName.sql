ALTER TABLE IF EXISTS public.client
    RENAME name TO login;

ALTER TABLE IF EXISTS public.client
    ADD COLUMN nick_name character varying(30);

ALTER TABLE IF EXISTS public.client
    ADD COLUMN email character varying(50);

ALTER TABLE IF EXISTS public.client
    ADD COLUMN token character varying(255);