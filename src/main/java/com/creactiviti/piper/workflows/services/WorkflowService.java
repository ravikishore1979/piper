package com.creactiviti.piper.workflows.services;

import com.creactiviti.piper.workflows.model.ReleaseWorkflow;
import com.creactiviti.piper.workflows.model.Workflow;
import com.creactiviti.piper.workflows.repos.IWorkflowRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.NumberUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class WorkflowService {

    @Autowired
    private IWorkflowRepository workflowRepository;

    private YAMLMapper yamlMapper;

    @PostConstruct
    public void initBean() {
        yamlMapper = new YAMLMapper(new YAMLFactory());
        yamlMapper.configure(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID, true);
        yamlMapper.configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false);
    }

    public ReleaseWorkflow getPipelineByName(String customerID, String projectID, String pipelineName) throws IOException {

        Assert.noNullElements(new String[]{customerID, projectID, pipelineName}, "CustomerID, ProjectID and PipelineName cannot be empty");
        Workflow wf = workflowRepository.findByCustomerIdAndProjectIdAndName(customerID, projectID, pipelineName);
        Assert.notNull(wf, String.format("Workflow not found for %s, %s, %s", customerID, projectID, pipelineName));
        ReleaseWorkflow rwf = yamlMapper.readValue(wf.getWorkflow(), ReleaseWorkflow.class);
        rwf.setWorkflowId(wf.getId());
        return rwf;
    }

    public Workflow getPipelineByID(String pipelineID) {

        Assert.notNull(pipelineID, "pipelineID cannot be empty");
        Long id = NumberUtils.parseNumber(pipelineID, Long.class);
        Assert.isTrue((id > 0), "Inavlid Pipeline ID " + pipelineID);
        return workflowRepository.findOne(id);
    }

    public Workflow saveWorkFlow(Workflow wf) {
        log.debug("name exists debug: {}", workflowRepository.existsByCustomerIdAndProjectIdAndName(wf.getCustomerId(), wf.getProjectId(), wf.getName()));
        Workflow oldWf = workflowRepository.findByCustomerIdAndProjectIdAndName(wf.getCustomerId(), wf.getProjectId(), wf.getName());
        if(oldWf != null) {
            log.info("Workflow with name [{}] already exists in DB with ID [{}], updating.", oldWf.getName(), oldWf.getId());
            wf.setId(oldWf.getId());
        }
        return workflowRepository.save(wf);
    }

    public Workflow saveWorkflowWithPOJO(String customerID, String projectID, String workflowName, ReleaseWorkflow workflow) throws JsonProcessingException {
        Workflow wf = new Workflow();
        wf.setCustomerId(customerID);
        wf.setProjectId(projectID);
        wf.setName(workflowName);
        wf.setWorkflow(yamlMapper.writeValueAsString(workflow));
        return saveWorkFlow(wf);
    }

    public Map<String, ReleaseWorkflow> getAllPipelinesByProject(String customerID, String projectID) {

        List<Workflow> workflowList = workflowRepository.findAllByCustomerIdAndProjectId(customerID, projectID);
        Assert.notEmpty(workflowList, String.format("Unable to retrieve workflows for %s and %s", customerID, projectID));
        Map<String, ReleaseWorkflow> workflowMap = new HashMap<>();
        workflowList.forEach(wf -> {
            ReleaseWorkflow rwf = null;
            try {
                rwf = yamlMapper.readValue(wf.getWorkflow(), ReleaseWorkflow.class);
                rwf.setWorkflowId(wf.getId());
            } catch (IOException e) {
                log.error("Unable to parse workflow string from DB for name {}", wf.getName(), e);
            }
            workflowMap.put(wf.getName(), rwf);
        });

        return workflowMap;
    }
}
