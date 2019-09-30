package com.creactiviti.piper.workflows.controllers;

import com.creactiviti.piper.workflows.model.Workflow;
import com.creactiviti.piper.workflows.services.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/piper")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    @GetMapping(value = "/workflow", produces = "application/json")
    public ResponseEntity<Workflow> getWorkflowByName(@RequestParam(name = "wfname") String workflowName) {
        return ResponseEntity.ok(workflowService.getPipelineByName(workflowName));
    }

    @PostMapping(value = "/workflow", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
}
