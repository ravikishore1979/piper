package com.creactiviti.piper.config;

import com.creactiviti.piper.workflows.repos.WorkflowJdbcRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
@ConditionalOnProperty(name="piper.persistence.provider",havingValue="jdbc")
@ComponentScan({"com.creactiviti.piper.workflows"})
@EnableJpaRepositories(basePackages = {"com.creactiviti.piper.workflows.model", "com.creactiviti.piper.workflows.repos"})
public class WorkflowDBPersistance {

    @Bean
    WorkflowJdbcRepository workflowJdbcRepository (NamedParameterJdbcTemplate aJdbcTemplate, ObjectMapper aObjectMapper) {
        WorkflowJdbcRepository jdbcJobRepository = new WorkflowJdbcRepository();
        jdbcJobRepository.setJdbc(aJdbcTemplate);
        return jdbcJobRepository;
    }
}
