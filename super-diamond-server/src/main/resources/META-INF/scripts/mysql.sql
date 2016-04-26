create database edu_diamond;

create table `conf_user` (
  `id` int(11) not null,
  `user_code` varchar(32) default null,
  `user_name` varchar(32) not null,
  `password` varchar(32) not null,
  `delete_flag` int(1) default '0',
  `create_time` datetime default null,
  `update_time` datetime default null,
  primary key (`id`)
) engine=innodb default charset=utf8;

create table `conf_project` (
  `id` int(11) not null,
  `proj_code` varchar(32) default null,
  `proj_name` varchar(32) default null,
  `owner_id` int(11) default null ,
  `development_version` int(11) default 0  null,
  `production_version` int(11) default 0  null,
  `test_version` int(11) default 0  null,
  `delete_flag` int(1) default '0',
  `create_time` datetime default null,
  `update_time` datetime default null,
  primary key (`id`)
) engine=innodb default charset=utf8;

create table `conf_project_user` (
  `proj_id` int(11) not null,
  `user_id` int(11) not null default '0',
  primary key (`proj_id`,`user_id`)
) engine=innodb default charset=utf8;

create table `conf_project_module` (
  `module_id` int(11) not null,
  `proj_id` int(11) not null,
  `module_name` varchar(32) default null,
  primary key (`module_id`)
) engine=innodb default charset=utf8;

create table `conf_project_user_role` (
  `proj_id` int(11) not null,
  `user_id` int(11) not null,
  `role_code` varchar(32) not null,
  primary key (`proj_id`,`user_id`,`role_code`)
) engine=innodb default charset=utf8;

create table `conf_project_config` (
  `config_id` int(11) not null,
  `config_key` varchar(64) not null,
  `config_value` varchar(256) not null,
  `config_desc` varchar(256) default null,
  `project_id` int(11) not null,
  `module_id` int(11) not null,
  `delete_flag` int(1) default '0',
  `opt_user` varchar(32) default null,
  `opt_time` datetime default null,
  `production_value` varchar(256) not null,
  `production_user` varchar(32) default null,
  `production_time` datetime default null,
  `test_value` varchar(256) not null,
  `test_user` varchar(32) default null,
  `test_time` datetime default null,
  `build_value` varchar(256) not null,
  `build_user` varchar(32) default null,
  `build_time` datetime default null,
  primary key (`config_id`)
) engine=innodb default charset=utf8;
