package com.creactiviti.piper.workflows.repos;

import com.creactiviti.piper.workflows.model.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface IWorkflowRepository extends JpaRepository<Workflow, Long> {

    Workflow findByCustomerIdAndProjectIdAndName(String customerID, String projectID, String name);

    boolean existsByCustomerIdAndProjectIdAndName(String customerId, String projectId, String name);

    List<Workflow> findAllByCustomerIdAndProjectId(String customerID, String projectID);
}
