use Bible;

drop table Collections if exists;
create table Collections (
    collection_id integer not null  primary key,
    name text not null
);

drop table Books if exists;
create table Books (
    book_id integer not null primary key,
    collection_id integer not null,
    name text not null
);

drop table Verses if exists;
create table Verses (
    verse_uid integer not null  primary key,
    book_id integer not null,
    chapter_number integer not null,
    verse_number integer not null,
    text text not null
);

create unique index Verses_xpk on Verses (book_id, chapter_number, verse_number);

///////////////////////////////////////////////////////////////////////////////////
//              Reference Data
///////////////////////////////////////////////////////////////////////////////////

drop table Verses if exists;
create table Verses (
    verse_uid uuid not null identity(1,1) primary key,
    collection text not null,
    book text not null,
    chapter integer not null,
    verse integer not null,
    `text` text not null
);

create unique index Verses_xpk on Verses (book, chapter, verse);

insert into Verses (collection, book, chapter, verse, text)
values ('bible', 'john', 14, 6, 'Jesus said to him, “I am the way, and the truth, and the life. No one comes to the Father except through me.”')