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
package com.creactiviti.piper.core;

import java.util.*;

import com.creactiviti.piper.core.task.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.creactiviti.piper.core.context.ContextRepository;
import com.creactiviti.piper.core.context.MapContext;
import com.creactiviti.piper.core.error.ErrorHandler;
import com.creactiviti.piper.core.error.ErrorObject;
import com.creactiviti.piper.core.error.Errorable;
import com.creactiviti.piper.core.error.Prioritizable;
import com.creactiviti.piper.core.event.EventPublisher;
import com.creactiviti.piper.core.event.Events;
import com.creactiviti.piper.core.event.PiperEvent;
import com.creactiviti.piper.core.job.Job;
import com.creactiviti.piper.core.job.JobRepository;
import com.creactiviti.piper.core.job.JobStatus;
import com.creactiviti.piper.core.job.SimpleJob;
import com.creactiviti.piper.core.messenger.Messenger;
import com.creactiviti.piper.core.messenger.Queues;
import com.creactiviti.piper.core.pipeline.Pipeline;
import com.creactiviti.piper.core.pipeline.PipelineRepository;
import com.creactiviti.piper.core.uuid.UUIDGenerator;

/**
 * The central class responsible for coordinating 
 * and executing jobs.
 * 
 * @author Arik Cohen
 * @since Jun 12, 2016
 */
public class Coordinator {

  private PipelineRepository pipelineRepository;
  private JobRepository jobRepository;
  private TaskExecutionRepository jobTaskRepository;
  private EventPublisher eventPublisher;
  private ContextRepository contextRepository;
  private TaskDispatcher taskDispatcher;
  private ErrorHandler errorHandler;
  private TaskCompletionHandler taskCompletionHandler;
  private JobExecutor jobExecutor;
  private Messenger messenger;

  private static final String INSTANTIATED_BY = "instantiatedBy";
  private static final String JOB_CYCLE_NAME = "cycleName";
  private static final String PIPELINE_ID = "pipelineId";
  private static final String TAGS = "tags";
  private static final String INPUTS = "inputs";
  private static final String WEBHOOKS = "webhooks";

  private final Logger log = LoggerFactory.getLogger(getClass());

  /**
   * Starts a job instance.
   * 
   * @param aJobParams
   *          The Key-Value map representing the job
   *          parameters
   * @return Job
   *           The instance of the Job
   */
  public Job create (Map<String,Object> aJobParams) {
    Assert.notNull(aJobParams,"request can't be null");
    MapObject jobParams = MapObject.of(aJobParams);
    String pipelineId = jobParams.getRequiredString(PIPELINE_ID);
    long[] ids = Arrays.stream(pipelineId.split(":")).map(String::trim).mapToLong(Long::valueOf).toArray();
    pipelineRepository.validateInputForRun(jobParams);
    Pipeline pipeline = pipelineRepository.findOne(pipelineId);
    Assert.notNull(pipeline,String.format("Unkown pipeline: %s", pipelineId));
    Assert.isNull(pipeline.getError(), pipeline.getError()!=null?String.format("%s: %s",pipelineId,pipeline.getError().getMessage()):null);

    validate (jobParams, pipeline);
    
    MapObject inputs = MapObject.of(jobParams.getMap(INPUTS,Collections.EMPTY_MAP));
    List<Accessor> webhooks = jobParams.getList(WEBHOOKS, MapObject.class, Collections.EMPTY_LIST);
    List<String> tags = (List<String>) aJobParams.get(TAGS);

    SimpleJob job = new SimpleJob();
    job.setId(UUIDGenerator.generate());
    job.setLabel(jobParams.getString(DSL.LABEL,pipeline.getLabel()));
    job.setPriority(jobParams.getInteger(DSL.PRIORITY, Prioritizable.DEFAULT_PRIORITY));
    job.setPipelineId(pipeline.getId());
    job.setStatus(JobStatus.CREATED);
    job.setCreateTime(new Date());
    job.setParentTaskExecutionId((String)aJobParams.get(DSL.PARENT_TASK_EXECUTION_ID));
    job.setTags(tags!=null?tags.toArray(new String[tags.size()]):new String[0]);
    job.setWebhooks(webhooks!=null?webhooks:Collections.EMPTY_LIST);
    job.setInputs(inputs);
    job.setInstantiatedBy(jobParams.getRequiredString(INSTANTIATED_BY));
    job.setJobCycleName(jobParams.getRequiredString(JOB_CYCLE_NAME));
    job.setWorkflowId(ids[0]);
    job.setWorkflowVersionId(ids[1]);
    log.debug("Job {} started",job.getId());
    jobRepository.create(job);
    
    MapContext context = new MapContext(jobParams.getMap(INPUTS,Collections.EMPTY_MAP));
    contextRepository.push(job.getId(),context);
    
    eventPublisher.publishEvent(PiperEvent.of(Events.JOB_STATUS,"jobId",job.getId(),"status",job.getStatus()));
    
    messenger.send(Queues.JOBS, job);

    return job;
  }

  public void start (Job aJob) {
    SimpleJob job = new SimpleJob(aJob);
    job.setStartTime(new Date());
    job.setStatus(JobStatus.STARTED);
    job.setCurrentTask(0);
    jobRepository.merge(job);
    jobExecutor.execute (job);
    eventPublisher.publishEvent(PiperEvent.of(Events.JOB_STATUS,"jobId",aJob.getId(),"status",job.getStatus()));
  }
  
  private void validate (MapObject aCreateJobParams, Pipeline aPipeline) {
    // validate inputs
    Map<String, Object> inputs = aCreateJobParams.getMap(DSL.INPUTS,Collections.EMPTY_MAP);
    List<Accessor> input = aPipeline.getInputs();
    for(Accessor in : input) {
      if(in.getBoolean(DSL.REQUIRED, false)) {
        Assert.isTrue(inputs.containsKey(in.get(DSL.NAME)), "Missing required param: " + in.get("name"));
      }
    }
    // validate webhooks
    List<Accessor> webhooks = aCreateJobParams.getList(WEBHOOKS, MapObject.class, Collections.EMPTY_LIST);
    for(Accessor webhook : webhooks) {
      Assert.notNull(webhook.getString(DSL.TYPE), "must define 'type' on webhook");
      Assert.notNull(webhook.getString(DSL.URL), "must define 'url' on webhook");
    }
  }

  /**
   * Stop a running job.
   * 
   * @param aJobId
   *          The id of the job to stop
   *          
   * @return The stopped {@link Job}
   */
  public Job stop (String aJobId) {
    Job job = jobRepository.findOne(aJobId);
    Assert.notNull(job,"Unknown job: " + aJobId);
    Assert.isTrue(job.getStatus()==JobStatus.STARTED,"Job " + aJobId + " can not be stopped as it is " + job.getStatus());
    SimpleJob mjob = new SimpleJob(job);
    mjob.setStatus(JobStatus.STOPPED);
    jobRepository.merge(mjob);
    eventPublisher.publishEvent(PiperEvent.of(Events.JOB_STATUS,"jobId",job.getId(),"status",job.getStatus()));
    if(mjob.getExecution().size() > 0) {
      SimpleTaskExecution currentTask = SimpleTaskExecution.createForUpdate(job.getExecution().get(job.getExecution().size()-1));
      currentTask.setStatus(TaskStatus.CANCELLED);
      currentTask.setEndTime(new Date());
      jobTaskRepository.merge(currentTask);
      taskDispatcher.dispatch(new CancelTask(currentTask.getId()));
    }
    return mjob;
  }

  /**
   * Resume a stopped or failed job.
   * 
   * @param aJobId
   *          The id of the job to resume.
   * @return The resumed job
   */
  public Job resume (String aJobId) {
    log.debug("Resuming job {}", aJobId);
    Job job = jobRepository.findOne (aJobId);
    Assert.notNull(job,String.format("Unknown job %s",aJobId));
    Assert.isTrue(job.getParentTaskExecutionId() == null,"Can't resume a subflow");
    Assert.isTrue(isRestartable(job), "can't restart job " + aJobId + " as it is " + job.getStatus());
    SimpleJob mjob = new SimpleJob (job);
    mjob.setStatus(JobStatus.STARTED);
    jobRepository.merge(mjob);
    jobExecutor.execute(mjob);
    return mjob;
  }

  /**
   * Complete the current task and resume the execution of corresponding job.
   *
   * @param taskId
   *          The id of the Task to complete.
   * @param taskOutput
   *          A Map that should be used as the output of the Task.
   * @param action
   * @return The resumed job
   */
  public Job handleTaskActionAndResumeJob(String taskId, Map<String, Object> taskOutput, TaskActions action) {

    log.debug("Completing Task {}", taskId);
    TaskExecution taskExecution = jobTaskRepository.findOne(taskId);
    Assert.notNull(taskExecution, String.format("Unknown Task executionID %s", taskId));
    Job job = jobRepository.findOne (taskExecution.getJobId());
    Assert.notNull(job,String.format("Unknown job %s", taskExecution.getJobId()));
    Assert.isTrue(job.getParentTaskExecutionId() == null,"Can't resume a subflow");
    Assert.isTrue(JobStatus.WAITING.equals(job.getStatus()), "can't restart job " + job.getId() + " as it is " + job.getStatus());
    SimpleJob mjob = new SimpleJob (job);
    mjob.setStatus(JobStatus.STARTED);
    jobRepository.merge(mjob);
    eventPublisher.publishEvent(PiperEvent.of(Events.JOB_STATUS,"jobId",job.getId(),"status",job.getStatus()));

    //TODO: Explore a way to avoid below type casting.
    SimpleTaskExecution stask = (SimpleTaskExecution) taskExecution;
    stask.set(DSL.TASK_ACTION_INPUT, taskOutput);
    stask.set(DSL.TASK_ACTION, action);
    //TODO: This is temporary, acted by should be read from user context object when user context is implemented.
    stask.set(DSL.ACTED_BY, taskOutput.get(DSL.ACTED_BY));
    messenger.send(Queues.RUN_WAITING_TASKS, stask);
    return mjob;
  }

  private boolean isRestartable (Job aJob) {
    return aJob.getStatus() == JobStatus.STOPPED || aJob.getStatus() == JobStatus.FAILED;
  }

  /**
   * Complete a task of a given job.
   * 
   * @param aTask
   *          The task to complete.
   */
  public void complete (TaskExecution aTask) {
    try {
      taskCompletionHandler.handle(aTask);
    }
    catch (Exception e) {
      SimpleTaskExecution exec = SimpleTaskExecution.createForUpdate(aTask);
      exec.setError(new ErrorObject(e.getMessage(), ExceptionUtils.getStackFrames(e)));
      handleError(exec);
    }
  }

  /**
   * Change the status of the job to waiting for the current job and complex tasks like For, Switch.
   *
   * @param aTask
   *          The task to waiting.
   */
  public void wait(TaskExecution aTask) {
    try {
      taskCompletionHandler.handleWaitingState(aTask);
    }
    catch (Exception e) {
      SimpleTaskExecution exec = SimpleTaskExecution.createForUpdate(aTask);
      exec.setError(new ErrorObject(e.getMessage(), ExceptionUtils.getStackFrames(e)));
      handleError(exec);
    }
  }

  /**
   * Handle an application error.
   * 
   * @param aErrorable
   *          The erring message.
   */
  public void handleError (Errorable aErrorable) {
    errorHandler.handle(aErrorable);
  }

  public void setContextRepository(ContextRepository aContextRepository) {
    contextRepository = aContextRepository;
  }

  public void setEventPublisher(EventPublisher aEventPublisher) {
    eventPublisher = aEventPublisher;
  }
  
  public void setJobRepository(JobRepository aJobRepository) {
    jobRepository = aJobRepository;
  }

  public void setTaskDispatcher(TaskDispatcher aTaskDispatcher) {
    taskDispatcher = aTaskDispatcher;
  }

  public void setPipelineRepository(PipelineRepository aPipelineRepository) {
    pipelineRepository = aPipelineRepository;
  }

  public void setJobTaskRepository(TaskExecutionRepository aJobTaskRepository) {
    jobTaskRepository = aJobTaskRepository;
  }

  public void setErrorHandler(ErrorHandler aErrorHandler) {
    errorHandler = aErrorHandler;
  }
  
  public void setTaskCompletionHandler(TaskCompletionHandler aTaskCompletionHandler) {
    taskCompletionHandler = aTaskCompletionHandler;
  }
  
  public void setJobExecutor(JobExecutor aJobExecutor) {
    jobExecutor = aJobExecutor;
  }
  
  public void setMessenger(Messenger aMessenger) {
    messenger = aMessenger;
  }
  
}
