CREATE TABLE IF NOT EXISTS public.users
(
    id        uuid         NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    login     varchar(255) NOT NULL UNIQUE,
    name      varchar(255) NOT NULL,
    password  varchar(255) NOT NULL,
    birthdate timestamptz  NOT NULL
);

CREATE TABLE IF NOT EXISTS public.accounts
(
    id       uuid       NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id  uuid       NOT NULL REFERENCES public.users (id) ON DELETE CASCADE,
    currency varchar(3) NOT NULL CHECK (currency IN ('RUB', 'USD', 'CNY')),
    value    int        NOT NULL,
    exists   boolean    NOT NULL
);