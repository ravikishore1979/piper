create table if not exists job (
  id varchar(256) not null primary key,
  status varchar(256) not null,
  current_task int not null,
  pipeline_id varchar(256) not null,
  label varchar(256),
  start_time datetime,
  create_time timestamp not null default now(),
  end_time datetime,
  tags text not null,
  priority int not null,
  inputs text not null,
  webhooks text not null,
  outputs text not null,
  parent_task_execution_id varchar(256)
);

create table if not exists task_execution  (
  id varchar(256) not null primary key,
  parent_id varchar(256),
  status varchar(256) not null,
  progress int not null,
  job_id varchar(256) not null,
  create_time timestamp not null default now(),
  start_time datetime,
  end_time datetime,
  serialized_execution text not null,
  priority int not null,
  task_number int not null
);

create table if not exists  context  (
  id varchar(256) not null primary key,
  stack_id varchar(256) not null,
  create_time timestamp not null default now(),
  serialized_context text not null
);

create table if not exists  counter (
  id varchar(256) not null primary key,
  create_time timestamp not null default now(),
  value bigint not null
);

create table if not exists pipelines (
    workflowid bigint primary key AUTO_INCREMENT,
    workflowname varchar(50) not null,
    script text,
    index name_ix (workflowname)
);
