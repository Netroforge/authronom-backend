-- Inspired by https://stackoverflow.com/a/6508293/285571
------------------------------------------------------------------------------------------------------------------------
-- Primary
-- Primary DB for dev
create database dev_authronom_backend encoding 'utf8';
\connect dev_authronom_backend;
create schema dev_authronom_backend;

-- Primary DB for test
create database test_authronom_backend encoding 'utf8';
\connect test_authronom_backend;
create schema test_authronom_backend;
