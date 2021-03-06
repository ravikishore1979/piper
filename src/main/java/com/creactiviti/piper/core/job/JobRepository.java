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
package com.creactiviti.piper.core.job;

import com.creactiviti.piper.core.Page;

public interface JobRepository {
  
  Page<Job> findAll (int aPageNumber);
  
  Job findOne (String aId);
  
  void create (Job aJob);

  Page<Job> findRecentJobs(int limit);

    /*
      select j.* from pipelines as pi join job as j on j.workflow_id = pi.workflowid and j.wfversion_id = pi.headrevision where pi.createdby = 'ram1@yahoo.com' order by j.create_time desc limit 10;
       */
    Page<Job> findRecentJobsByCreatedUser(String createdBy, int limit);

    Job merge (Job aJob);
  
  Job findJobByTaskId (String aTaskId);
  
  int countRunningJobs ();
  
  int countCompletedJobsToday ();
  
  int countCompletedJobsYesterday ();

  int countJobsByJobId(String s);

  Page<Job> findJobsByWorkflowID(String wfID, int limit);
}
