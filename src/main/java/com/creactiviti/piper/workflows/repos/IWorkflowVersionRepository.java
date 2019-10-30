package com.creactiviti.piper.workflows.repos;

import com.creactiviti.piper.workflows.model.WorkflowVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IWorkflowVersionRepository extends JpaRepository<WorkflowVersion, Long> {
}
