/*
 * Copyright 2016-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.creactiviti.piper.web;

import com.creactiviti.piper.core.Coordinator;
import com.creactiviti.piper.core.Page;
import com.creactiviti.piper.core.annotations.ConditionalOnCoordinator;
import com.creactiviti.piper.core.job.Job;
import com.creactiviti.piper.core.job.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@ConditionalOnCoordinator
@CrossOrigin(origins = {"https://p2001885952trial-trial-dev-ui.cfapps.us10.hana.ondemand.com", "https://na1.saparate.com", ""})
@RequestMapping("/jobs")
public class JobsController {

  @Autowired private JobRepository jobRepository;
  @Autowired private Coordinator coordinator;
  
  @GetMapping(value="/")
  public Page<Job> list (@RequestParam(value="p",defaultValue="1") Integer aPageNumber) {
    return jobRepository.findAll(aPageNumber);
  }

  @GetMapping(value="/latest")
  public Page<Job> getRecentJobs (@RequestParam(value="limit", defaultValue="10") Integer recordCount) {
    return jobRepository.findRecentJobs(recordCount);
  }
  
  @PostMapping("/")
  public Job create (@RequestBody Map<String, Object> aJobRequest) {
    return coordinator.create(aJobRequest);
  }
  
  @GetMapping(value="/{id}")
  public Job get (@PathVariable("id")String aJobId) {
    Job job = jobRepository.findOne (aJobId);
    Assert.notNull(job,"Unknown job: " + aJobId);
    return job;
  }
  
  @ExceptionHandler(IllegalArgumentException.class)
  public void handleIllegalArgumentException (HttpServletResponse aResponse) throws IOException {
    aResponse.sendError(HttpStatus.BAD_REQUEST.value());
  }
  
  @PutMapping(value="/{id}/restart")
  public Job restart (@PathVariable("id")String aJobId) {
    return coordinator.resume(aJobId);
  }
  
  @PutMapping(value="/{id}/stop")
  public Job step (@PathVariable("id")String aJobId) {
    return coordinator.stop(aJobId);
  }
    
}
