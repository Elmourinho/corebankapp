-- create public schema
create schema if not exists public;

-- create tables
create table if not exists public.accounts (
    id bigserial primary key,
    customer_id bigint not null,
    country varchar(255)
);

create table if not exists public.balances (
    id bigserial primary key,
    account_id bigint not null,
    currency varchar(10) not null,
    amount decimal not null,
    constraint fk_account
    foreign key(account_id)
    references accounts(id)
);

create table if not exists public.transactions (
    id bigserial primary key,
    account_id bigint not null,
    amount decimal not null,
    currency varchar(5) not null,
    direction varchar(3) not null,
    description text,
    constraint fk_account
    foreign key(account_id)
    references accounts(id)
);
