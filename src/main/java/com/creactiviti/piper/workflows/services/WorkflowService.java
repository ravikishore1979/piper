package com.creactiviti.piper.workflows.services;

import com.creactiviti.piper.core.job.JobRepository;
import com.creactiviti.piper.workflows.exceptions.WorkflowException;
import com.creactiviti.piper.workflows.model.*;
import com.creactiviti.piper.workflows.repos.IWorkflowRepository;
import com.creactiviti.piper.workflows.repos.IWorkflowVersionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;

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
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SaparateClientService saparateClientService;

    @PostConstruct
    public void initBean() {
        yamlMapper = new YAMLMapper(new YAMLFactory());
        yamlMapper.configure(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID, true);
        yamlMapper.configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false);
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

    public Workflow saveWorkflowWithPOJO(String customerID, String projectID, String workflowName, ReleasePipelineUI releaseWF, String authToken) throws JsonProcessingException {
        Workflow wf = new Workflow();
        wf.setCustomerId(customerID);
        wf.setProjectId(projectID);
        wf.setName(workflowName);

        releaseWF.getTasks().forEach(task -> {
            if(task instanceof JenkinsJobTask) {
                JenkinsJobTask jenkinsJobTask = (JenkinsJobTask) task;
                jenkinsJobTask.setBuildJobName(String.format("${%s}", ReleasePipelineUI.BUILD_JOB_NAME));
                jenkinsJobTask.setBuildJobNumber(String.format("${%s}", ReleasePipelineUI.BUILD_NUMBER));
                jenkinsJobTask.setJenkinsAuthToken(String.format("${%s}", ReleasePipelineUI.JENKINS_AUTH_TOKEN));
            }
        });

        ReleaseWorkflowYaml rwfYaml = releaseWF.getReleaseWorkflowYaml();
        WorkflowVersion workflowVersion = new WorkflowVersion();
        workflowVersion.setBuildInputJson(objectMapper.writeValueAsString(releaseWF.getReleasePipelineBuildInput()));
        workflowVersion.setLastModified(new Date());
        workflowVersion.setLastModifiedBy(null);
        workflowVersion.setWorkflow(yamlMapper.writeValueAsString(rwfYaml));
        if(!StringUtils.isEmpty(releaseWF.getWorkflowId())) {
            String[] ids = releaseWF.getWorkflowId().split(":");
            wf.setId(Long.parseLong(ids[0]));
            if(ids.length == 2) {
                workflowVersion.setVersionID(Long.parseLong(ids[1]));
            }
        }
        Workflow savedFlow = saveWorkFlow(wf, workflowVersion);

        rwfYaml.getTasks().forEach(task -> {
            if(task instanceof JenkinsJobTask) {
                JenkinsJobTask jenkinsJobTask = (JenkinsJobTask) task;
                String jenkinsDeployJobName = workflowName.concat("_" + jenkinsJobTask.getName())
                        .concat("_"+savedFlow.getId())
                        .concat("_"+savedFlow.getHeadRevision());
                jenkinsJobTask.setJenkinsJobName(jenkinsDeployJobName);
                try {
                    String jenkinsDeployPipelineScript = jenkinsJobTask.getJenkinsJobCreateTemplateStr();
                    saparateClientService.createOrUpdateJenkinsDeployJob(jenkinsDeployPipelineScript, authToken);
                } catch (WorkflowException e) {
                    log.error("Unable to create Jenkins Deploy job for {}", jenkinsDeployJobName, e);
                }
            }
        });
        workflowVersion.setWorkflow(yamlMapper.writeValueAsString(rwfYaml));
        workflowVersion.setVersionID(savedFlow.getHeadRevision());
        wfVersionRepository.save(workflowVersion);
        return savedFlow;
    }

    private ReleasePipelineUI generateReleasePipelineUI(Workflow wf, long versionId) {
        ReleasePipelineUI rpui = null;
        try {
            WorkflowVersion wfVersion = getWorkflow(wf, versionId);
            if(wfVersion == null) {
                return rpui;
            }
            ReleaseWorkflowYaml rwfYaml = yamlMapper.readValue(wfVersion.getWorkflow(), ReleaseWorkflowYaml.class);
            ReleasePipelineBuildInput rpbi = objectMapper.readValue(wfVersion.getBuildInputJson(), ReleasePipelineBuildInput.class);
            long requiredVersion =  (versionId > 0L) ?  versionId : wf.getHeadRevision();
            rwfYaml.setWorkflowId(wf.getId() + ":" + requiredVersion);
            rpui = new ReleasePipelineUI();
            rpui.setWorkflowId(rwfYaml.getWorkflowId());
            rpui.setLabel(rwfYaml.getLabel());
            rpui.setReleasePipelineBuildInput(rpbi);
            rpui.setTasks(rwfYaml.getTasks());
        } catch (IOException e) {
            log.error("Unable to retrieve Workflow for customer '{}', project '{}', name '{}", wf.getCustomerId(), wf.getProjectId(), wf.getName(), e);
        }
        return rpui;
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

    /**
     * Returns the Release Pipeline UI Object that is required to render the Release Pipeline UI for editing.
     * @param customerID
     * @param projectID
     * @param pipelineName
     * @param versionID
     * @return
     */
    public ReleasePipelineUI getPipelineUIByName(String customerID, String projectID, String pipelineName, Long versionID) {
        Workflow wf = getWorkflow(customerID, projectID, pipelineName);
        Assert.notNull(wf, String.format("Workflow not found for %s, %s, %s", customerID, projectID, pipelineName));
        ReleasePipelineUI rwf =  generateReleasePipelineUI(wf, versionID);
        return rwf;
    }

    public Workflow getWorkflow(String customerID, String projectID, String pipelineName) {
        Workflow wf = workflowRepository.findByCustomerIdAndProjectIdAndName(customerID, projectID, pipelineName);
        return wf;
    }
}

