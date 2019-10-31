package com.creactiviti.piper.workflows.services;

import com.creactiviti.piper.core.job.JobRepository;
import com.creactiviti.piper.workflows.model.WorkflowVersion;
import com.creactiviti.piper.workflows.model.ReleaseWorkflow;
import com.creactiviti.piper.workflows.model.Workflow;
import com.creactiviti.piper.workflows.repos.IWorkflowRepository;
import com.creactiviti.piper.workflows.repos.IWorkflowVersionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class WorkflowService {

    @Autowired
    private IWorkflowRepository workflowRepository;
    @Autowired
    private IWorkflowVersionRepository wfVersionRepository;
    @Autowired
    private JobRepository jobRepository;
    private YAMLMapper yamlMapper;

    @PostConstruct
    public void initBean() {
        yamlMapper = new YAMLMapper(new YAMLFactory());
        yamlMapper.configure(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID, true);
        yamlMapper.configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false);
    }

    public ReleaseWorkflow getPipelineByName(String customerID, String projectID, String pipelineName, Long versionID) throws IOException {

        Assert.noNullElements(new String[]{customerID, projectID, pipelineName}, "CustomerID, ProjectID and PipelineName cannot be empty");


        Workflow wf = workflowRepository.findByCustomerIdAndProjectIdAndName(customerID, projectID, pipelineName);
        Assert.notNull(wf, String.format("Workflow not found for %s, %s, %s", customerID, projectID, pipelineName));
        ReleaseWorkflow rwf =  getWorkflowPOJO(wf, versionID);
        return rwf;
    }

    public Workflow getPipelineByID(String pipelineID) {

        Assert.notNull(pipelineID, "pipelineID cannot be empty");
        Long id = NumberUtils.parseNumber(pipelineID, Long.class);
        Assert.isTrue((id > 0), "Inavlid Pipeline ID " + pipelineID);
        return workflowRepository.findOne(id);
    }

    public Workflow saveWorkFlow(Workflow wf, WorkflowVersion wfVersion) {
        Workflow oldWf = null;
        if(wf.getId() > 0 ) {
            oldWf = workflowRepository.findById(wf.getId());
            log.debug("Workflow with ID {} exists", wf.getId());
            if(oldWf == null) {
                String errorMsg = String.format("No workflow is defined with ID '%s'.", wf.getId());
                IllegalArgumentException iae = new IllegalArgumentException(errorMsg);
                log.error(errorMsg, iae);
                throw iae;
            }
            if(!oldWf.getName().equals(wf.getName())) {
                String errorMsg = String.format("Name of the Workflow for the given ID '%s'  is '%s', this does not match with name '%s' in url param.", wf.getId(), oldWf.getName(), wf.getName());
                IllegalArgumentException iae = new IllegalArgumentException(errorMsg);
                log.error(errorMsg, iae);
                throw iae;
            }
        } else {
            oldWf = workflowRepository.findByCustomerIdAndProjectIdAndName(wf.getCustomerId(), wf.getProjectId(), wf.getName());
            log.debug("workflow with customerID '{}' project '{}' and  name '{}' exists.", wf.getCustomerId(), wf.getProjectId(), wf.getName());
        }

        if (oldWf == null) {
            return insertIntoDB(wf, wfVersion);
        } else {
            log.info("Workflow with name [{}] already exists in DB with ID [{}], updating.", oldWf.getName(), oldWf.getId());
            wf.setId(oldWf.getId());
            wf.setHeadRevision(oldWf.getHeadRevision());
            wfVersion.setWorkflowID(wf.getId());
            if(wfVersion.getVersionID() > 0 && wfVersion.getVersionID() != wf.getHeadRevision()) {
                String errorMsg = String.format("Can only modify the head version '%s' but given version is '%s', so throwing error.", wf.getHeadRevision(), wfVersion.getVersionID());
                IllegalStateException e = new IllegalStateException(errorMsg);
                log.error(errorMsg, e);
                throw e;
            }
            return updateIntoDB(wf, wfVersion);
        }
    }

    @Transactional
    private Workflow updateIntoDB(Workflow wf, WorkflowVersion wfVersion) {
        if(wf.getHeadRevision() > 0) {
            int instanceCount = jobRepository.countJobsByJobId(wf.getId() + ":" + wf.getHeadRevision());
            if(instanceCount > 0) {
                wfVersion.setVersionID(-1L);
            } else {
                wfVersion.setVersionID(wf.getHeadRevision());
            }
        }
        WorkflowVersion newWfVersion = wfVersionRepository.save(wfVersion);
        wf.setHeadRevision(newWfVersion.getVersionID());
        return workflowRepository.save(wf);
    }

    @Transactional
    private Workflow insertIntoDB(Workflow wf, WorkflowVersion wfVersion) {
        Workflow newWf = workflowRepository.save(wf);
        wfVersion.setWorkflowID(newWf.getId());
        WorkflowVersion newWfVersion = wfVersionRepository.save(wfVersion);
        newWf.setHeadRevision(newWfVersion.getVersionID());
        return workflowRepository.save(newWf);
    }

    public Workflow saveWorkflowWithPOJO(String customerID, String projectID, String workflowName, ReleaseWorkflow releaseWF) throws JsonProcessingException {
        Workflow wf = new Workflow();
        wf.setCustomerId(customerID);
        wf.setProjectId(projectID);
        wf.setName(workflowName);


        WorkflowVersion workflowVersion = new WorkflowVersion();
        workflowVersion.setLastModified(new Date());
        workflowVersion.setLastModifiedBy(null);
        workflowVersion.setWorkflow(yamlMapper.writeValueAsString(releaseWF));
        if(!StringUtils.isEmpty(releaseWF.getWorkflowId())) {
            String[] ids = releaseWF.getWorkflowId().split(":");
            wf.setId(Long.parseLong(ids[0]));
            if(ids.length == 2) {
                workflowVersion.setVersionID(Long.parseLong(ids[1]));
            }
        }
        return saveWorkFlow(wf, workflowVersion);
    }

    public Map<String, ReleaseWorkflow> getAllPipelinesByProject(String customerID, String projectID) {

        List<Workflow> workflowList = workflowRepository.findAllByCustomerIdAndProjectId(customerID, projectID);
        Assert.notEmpty(workflowList, String.format("Unable to retrieve workflows for %s and %s", customerID, projectID));
        Map<String, ReleaseWorkflow> workflowMap = new HashMap<>();
        workflowList.forEach(wf -> {
            ReleaseWorkflow rwf = getWorkflowPOJO(wf, -1L);
            workflowMap.put(wf.getName(), rwf);
        });

        return workflowMap;
    }

    private ReleaseWorkflow getWorkflowPOJO(Workflow wf, long versionId) {
        ReleaseWorkflow rwf = null;
        try {
            WorkflowVersion wfVersion = getWorkflow(wf, versionId);
            if(wfVersion == null) {
                return rwf;
            }
            rwf = yamlMapper.readValue(wfVersion.getWorkflow(), ReleaseWorkflow.class);
            long requiredVersion =  (versionId > 0L) ?  versionId : wf.getHeadRevision();
            rwf.setWorkflowId(wf.getId() + ":" + requiredVersion);
        } catch (IOException e) {
            log.error("Unable to retrieve Workflow for customer '{}', project '{}', name '{}", wf.getCustomerId(), wf.getProjectId(), wf.getName(), e);
        }
        return rwf;
    }

    private WorkflowVersion getWorkflow(Workflow wf, long versionId) {
        long requiredVersion =  (versionId > 0L) ?  versionId : wf.getHeadRevision();
        WorkflowVersion wfVersion = wfVersionRepository.findOne(requiredVersion);
        log.info("Unable to find Workflow yaml with ID %s", requiredVersion);
        return wfVersion;
    }

    /**
     * Returns the Workflow yaml using the combination of WorkflowID and VersionID.
     * So piperID will be like "<WF ID>:<WF Version ID>"
     * @param piperWorkflowID
     * @return
     */
    public String[] getPipelineByPiperID(String piperWorkflowID) {
        String[] ids = piperWorkflowID.split(":");
        Workflow wf = workflowRepository.findById(Long.valueOf(ids[0]));
        if(wf == null) {
            throw new IllegalArgumentException(String.format("Workflow not found with  ID '%s' retrieved from piperID '%s'", ids[0], piperWorkflowID));
        }
        long requiredVersion = (ids.length == 2) ? Long.valueOf(ids[1]) : wf.getHeadRevision();
        WorkflowVersion wfVersion = wfVersionRepository.findOne(requiredVersion);

        if(wfVersion == null) {
            throw new IllegalArgumentException(String.format("Workflow version not found with  ID '%s' and version ID '%s'", ids[0], requiredVersion));
        }

        return new String[]{(wf.getId() + ":" + requiredVersion), wfVersion.getWorkflow()};
    }

    public List<Workflow> getAllWorkflowsByProject(String customerID, String projectID) {
        List<Workflow> workflowList = workflowRepository.findAllByCustomerIdAndProjectId(customerID, projectID);
        Assert.notEmpty(workflowList, String.format("Unable to retrieve workflows for %s and %s", customerID, projectID));

        return workflowList;
    }
}
