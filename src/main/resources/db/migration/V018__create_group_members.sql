create table group_members
(
    member_id bigint      not null,
    group_id  bigint      not null,
    role      varchar(30) not null,
    active    boolean default true,
    selected  boolean default true,
    joined_at timestamp   not null,
    left_at   timestamp null,
    primary key (member_id, group_id),
    constraint fk_group_member_member foreign key (member_id) references users (id),
    constraint fk_group_member_group foreign key (group_id) references groups (id)
);