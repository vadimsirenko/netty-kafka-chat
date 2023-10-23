ALTER TABLE IF EXISTS public.message
    ADD COLUMN sender character varying(30);

ALTER TABLE IF EXISTS public.message
    ADD COLUMN recipient character varying(30);