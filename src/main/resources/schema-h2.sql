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
  parent_task_execution_id varchar(256),
  instantiated_by varchar(50),
  cyclename varchar(50)
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
    customerid varchar(50) not null,
    projectid varchar(50) not null,
    workflowname varchar(50) not null,
    createdby varchar(50),
    createdtime datetime,
    headrevision bigint,
    unique index name_ix (customerId, projectId, workflowname)
) AUTO_INCREMENT = 10000;

create table if not exists pipelineversions (
    versionid bigint primary key AUTO_INCREMENT,
    workflowid bigint,
    buildinputjson text,
    lastmodified datetime,
    lastmodifiedby varchar(50),
    script text,
    stage_summary varchar(100),
    unique index name_ix (workflowid, versionid),
    constraint pipeline_fk foreign key (workflowid) references pipelines(workflowid)
) AUTO_INCREMENT = 10000;

create table if not exists humantaskassignee (
    id bigint primary key AUTO_INCREMENT,
    taskinstanceid varchar(256) not null,
    assigneeid varchar(50) not null,
    assigneetype varchar(15) not null,
    assigneename varchar(50),
    assigndate datetime,
    businesslogicid varchar(10),
    releaseworkflow varchar(256),
    releasecyclename varchar(50),
    index assignee_ix(assigneeid),
    index taskinstance_fk(taskinstanceid),
    foreign key(taskinstanceid) references task_execution(id)
) AUTO_INCREMENT = 10000;

create table if not exists humantaskaction (
    id bigint primary key AUTO_INCREMENT,
    humantaskid bigint,
    taskinstanceid varchar(256),
    actionname varchar(10),
    actiondoneby varchar(50),
    actiondate datetime,
    errormsg varchar(1000),
    index taskinstance_fk(taskinstanceid),
    index assignee_fk(humantaskid),
    foreign key(taskinstanceid) references task_execution(id),
    foreign key(humantaskid) references humantaskassignee(id)
) AUTO_INCREMENT = 10000;
