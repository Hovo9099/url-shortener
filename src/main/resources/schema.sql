create table url
(
    id serial primary key not null ,
    original_url text,
    short_url text not null unique
);