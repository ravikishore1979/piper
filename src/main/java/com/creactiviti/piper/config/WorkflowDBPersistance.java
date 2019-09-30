package com.creactiviti.piper.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan({"com.creactiviti.piper.workflows"})
@EnableJpaRepositories(basePackages = {"com.creactiviti.piper.workflows.model", "com.creactiviti.piper.workflows.repos"})
public class WorkflowDBPersistance {
}
