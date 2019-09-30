package com.creactiviti.piper.workflows.services;

import com.creactiviti.piper.workflows.model.Workflow;
import com.creactiviti.piper.workflows.repos.IWorkflowRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WorkflowService {

    @Autowired
    private IWorkflowRepository workflowRepository;

    public Workflow getPipelineByName(String pipelineName) {
        if (StringUtils.isEmpty(pipelineName)) {
            log.error("name parameter is required to retrieve workflow.");
            throw new IllegalArgumentException("Input parameter PipelineName is empty");
        }
        return workflowRepository.findByName(pipelineName);
    }

    public void saveWorkFlow(Workflow wf) {
        log.debug("name exists debug: {}", workflowRepository.existsByName(wf.getName()));
        log.info("name exists INFO: {}", workflowRepository.existsByName(wf.getName()));
        Workflow oldWf = workflowRepository.findByName(wf.getName());
        if(oldWf != null) {
            log.info("Workflow with name [{}] already exists in DB with ID [{}]", oldWf.getName(), oldWf.getId());
            wf.setId(oldWf.getId());
        }
        workflowRepository.save(wf);
    }
}
