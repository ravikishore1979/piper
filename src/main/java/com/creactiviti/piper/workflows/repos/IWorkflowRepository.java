package com.creactiviti.piper.workflows.repos;

import com.creactiviti.piper.workflows.model.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;


public interface IWorkflowRepository extends JpaRepository<Workflow, Long> {

    Workflow findByName(String name);

    boolean existsByName(String name);
}
