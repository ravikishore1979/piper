package com.creactiviti.piper.workflows.controllers;

import com.creactiviti.piper.core.Page;
import com.creactiviti.piper.core.job.Job;
import com.creactiviti.piper.core.job.JobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin(origins = {"https://p2001885952trial-trial-dev-ui.cfapps.us10.hana.ondemand.com", "https://na1.saparate.com"})
@RequestMapping("/release/instances")
public class InstanceController {

    @Autowired
    private JobRepository jobRepository;

    @GetMapping("/testInstance")
    public ResponseEntity<String> getTestInstance() {
        return ResponseEntity.ok("Testing duplicate /piper controller");
    }

    @GetMapping(value = "/{customerID}/{projectID}/{wfID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<Job>> getInstancesByID(@PathVariable String customerID, @PathVariable String projectID,
                                                      @PathVariable Long wfID, @RequestParam(value="limit", defaultValue="10") Integer recordCount) {
        //TODO: Validate login and corresponding customer and project details

        Page<Job> jobList = jobRepository.findJobsByWorkflowID(String.valueOf(wfID), recordCount);
        return ResponseEntity.ok(jobList);
    }
}
