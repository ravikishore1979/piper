package com.creactiviti.piper.workflows.controllers;

import com.creactiviti.piper.core.Coordinator;
import com.creactiviti.piper.core.job.Job;
import com.creactiviti.piper.workflows.model.ReleasePipelineUI;
import com.creactiviti.piper.workflows.model.Workflow;
import com.creactiviti.piper.workflows.model.WorkflowVersion;
import com.creactiviti.piper.workflows.model.WorkflowWithInput;
import com.creactiviti.piper.workflows.services.WorkflowService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/workflows")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;
    @Autowired
    private Coordinator coordinator;

    @GetMapping(value = "/{customerID}/{projectID}/{wfName}", produces = "application/json")
    public ResponseEntity<ReleasePipelineUI> getWorkflowByName(@PathVariable(name = "customerID") String customerID,
                                                               @PathVariable(name = "projectID") String projectID,
                                                               @PathVariable(name = "wfName") String workflowName,
                                                               @RequestParam(name = "wfversion", defaultValue = "0") Long versionId) {
        Assert.noNullElements(new String[]{customerID, projectID, workflowName}, "CustomerID, ProjectID and PipelineName cannot be empty");
        log.info("Retrieving WF details by name {}", workflowName);
        ReleasePipelineUI pipelineByName = workflowService.getPipelineUIByName(customerID, projectID, workflowName, versionId);

        Assert.notNull(pipelineByName, String.format("Error while getting Workflow by %s, %s and %s", customerID, projectID, workflowName));
        return ResponseEntity.ok(pipelineByName);
    }

    @GetMapping(value = "/{customerID}/{projectID}", produces = "application/json")
    public ResponseEntity<List<WorkflowWithInput>> getAllWorkflowsByProject(@PathVariable(name = "customerID") String customerID,
                                                                            @PathVariable(name = "projectID") String projectID, final Principal principal) {
        return ResponseEntity.ok(workflowService.getAllWorkflowsByProject(customerID, projectID));
    }

    @PostMapping(value = "/workflowWithFile", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> saveWorkflow(@RequestParam(name="wfname") String workflowName,
                                                 @RequestParam("wffile") MultipartFile wfFile) throws IOException {
        log.info("Received workflow [{}] with fileName [{}]", workflowName, wfFile.getOriginalFilename());

        //TODO: Put validations on file Type, File size also try validating content.
        try {
            String wfFileContent = new String(wfFile.getBytes());
            Workflow wf = new Workflow();
            wf.setName(workflowName);

            WorkflowVersion workflowVersion = new WorkflowVersion();
            workflowVersion.setLastModified(new Date());
            workflowVersion.setWorkflow(wfFileContent);
            workflowService.saveWorkFlow(wf, workflowVersion);
        } catch (IOException e) {
            log.error("Exception while reading the file [{}], updating it.", workflowName, e);
            throw e;
        }
        return ResponseEntity.ok("SUccessfully saved.");
    }

    @PostMapping(value = "/{customerID}/{projectID}/{wfName}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Workflow> saveWorkflowJson(@PathVariable(name = "customerID") String customerID,
                                    @PathVariable(name = "projectID") String projectID,
                                    @PathVariable(name = "wfName") String workflowName,
                                    @RequestBody ReleasePipelineUI workflow,
                                    @RequestHeader(name = "Authorization") String authToken) throws IOException {
        Assert.notNull("Authorization token cannot be empty", authToken);
        Assert.noNullElements(new String[]{customerID, projectID, workflowName}, "CustomerID, ProjectID and PipelineName cannot be empty");
        log.info("Received workflow [{}] using Auth token [{}]", workflowName, authToken);
        Workflow wf = null;
        try {
            wf = workflowService.saveWorkflowWithPOJO(customerID, projectID, workflowName, workflow, authToken);
            workflow.setWorkflowId(wf.getId() + ":" + wf.getHeadRevision());
        } catch (JsonProcessingException e) {
            log.error("Exception while parsing Workflow POJO {} [{}]", workflowName, workflow.toString(), e);
            throw e;
        } catch (Throwable e) {
            log.error("Exception while saving.", e);
            throw e;
        }
        return ResponseEntity.ok(wf);
    }

    @PostMapping(value = "/trigger/{customerID}/{projectID}/{wfName}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Job> triggerWorkflow(@PathVariable(name = "customerID") String customerID,
                                               @PathVariable(name = "projectID") String projectID,
                                               @PathVariable(name = "wfName") String wfName,
                                               @RequestBody Map<String, Object> jobInput,
                                               @RequestHeader(name = "Authorization") String authToken) {
        Assert.notNull("Authorization token cannot be empty", authToken);
        Assert.noNullElements(new String[]{customerID, projectID, wfName}, "CustomerID, ProjectID and PipelineName cannot be empty");
        log.info("Triggering Workflow {}", wfName);
        ReleasePipelineUI rpi = workflowService.getPipelineUIByName(customerID, projectID, wfName, -1L);
        Assert.notNull(rpi, String.format("Unable to find Workflow with wfName [%s]", wfName));
        jobInput.put("pipelineId", rpi.getWorkflowId());
        Map inputs = (Map) jobInput.get("inputs");
        inputs.put(ReleasePipelineUI.JENKINS_AUTH_TOKEN, authToken);
        inputs.put(ReleasePipelineUI.BUILD_JOB_NAME, rpi.getReleasePipelineBuildInput().getBuildPipelineJobName());
        Job releaseCycle = coordinator.create(jobInput);
        return ResponseEntity.ok(releaseCycle);
    }
}
