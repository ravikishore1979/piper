package com.creactiviti.piper.workflows.controllers;

import com.creactiviti.piper.workflows.exceptions.WorkflowException;
import com.creactiviti.piper.workflows.model.ReleaseWorkflow;
import com.creactiviti.piper.workflows.model.Workflow;
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
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/piper")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    @GetMapping(value = "/workflows/{customerID}/{projectID}/{wfName}", produces = "application/json")
    public ResponseEntity<ReleaseWorkflow> getWorkflowByName(@PathVariable(name = "customerID") String customerID,
                                                      @PathVariable(name = "projectID") String projectID,
                                                      @PathVariable(name = "wfName") String workflowName) {
        ReleaseWorkflow pipelineByName = null;
        try {
            pipelineByName = workflowService.getPipelineByName(customerID, projectID, workflowName);
        } catch (IOException e) {
            String errorMsg = String.format("Error while getting Workflow by %s, %s and %s", customerID, projectID, workflowName);
            log.error(errorMsg, e);
            throw new WorkflowException(errorMsg, e);
        }
        Assert.notNull(pipelineByName, "Unable to get Workflow object DB.");
        return ResponseEntity.ok(pipelineByName);
    }

    @GetMapping(value = "/workflows/{customerID}/{projectID}", produces = "application/json")
    public ResponseEntity<Map<String, ReleaseWorkflow>> getAllWorkflowsByProject(@PathVariable(name = "customerID") String customerID,
                                                                                 @PathVariable(name = "projectID") String projectID) {
        Map<String, ReleaseWorkflow> pipelinesByName = null;
        pipelinesByName = workflowService.getAllPipelinesByProject(customerID, projectID);
        Assert.notEmpty(pipelinesByName, "Unable to get Workflow object DB.");
        return ResponseEntity.ok(pipelinesByName);
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
            wf.setWorkflow(wfFileContent);
            workflowService.saveWorkFlow(wf);
        } catch (IOException e) {
            log.error("Exception while reading the file [{}], updating it.", workflowName, e);
            throw e;
        }
        return ResponseEntity.ok("SUccessfully saved.");
    }

    @PostMapping(value = "/workflows/{customerID}/{projectID}/{wfName}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> saveWorkflowJson(@PathVariable(name = "customerID") String customerID,
                                                   @PathVariable(name = "projectID") String projectID,
                                                   @PathVariable(name = "wfName") String workflowName,
                                                   @RequestBody ReleaseWorkflow workflow) throws IOException {
        log.info("Received workflow [{}]", workflowName);

        try {
            workflowService.saveWorkflowWithPOJO(customerID, projectID, workflowName, workflow);
        } catch (JsonProcessingException e) {
            log.error("Exception while parsing Workflow POJO {} [{}]", workflowName, workflow.toString(), e);
            throw e;
        }
        return ResponseEntity.ok("SUccessfully saved.");
    }
}
