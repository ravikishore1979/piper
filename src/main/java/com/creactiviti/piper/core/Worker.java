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

import com.creactiviti.piper.core.context.MapContext;
import com.creactiviti.piper.core.error.ErrorObject;
import com.creactiviti.piper.core.event.EventPublisher;
import com.creactiviti.piper.core.event.Events;
import com.creactiviti.piper.core.event.PiperEvent;
import com.creactiviti.piper.core.job.Job;
import com.creactiviti.piper.core.job.JobRepository;
import com.creactiviti.piper.core.messenger.Messenger;
import com.creactiviti.piper.core.messenger.Queues;
import com.creactiviti.piper.core.task.*;
import com.creactiviti.piper.core.taskhandler.interrupts.Wait;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

/**
 * <p>The class responsible for executing tasks spawned by the {@link Coordinator}.</p>
 * 
 * <p>Worker threads typically execute on a different
 * process than the {@link Coordinator} process and most likely
 * on a seperate node altogether.</p>
 * 
 * <p>Communication between the two is decoupled through the 
 * {@link Messenger} interface.</p>
 * 
 * @author Arik Cohen
 * @since Jun 12, 2016
 *
 */
public class Worker {

  private TaskHandlerResolver taskHandlerResolver;
  private Messenger messenger;  
  private final ExecutorService executors = Executors.newCachedThreadPool();
  private final Map<String, Future<?>> taskExecutions = new ConcurrentHashMap<>();
  private TaskEvaluator taskEvaluator = new SpelTaskEvaluator();
  private EventPublisher eventPublisher;
  private JobRepository jobRepository;

  private Logger logger = LoggerFactory.getLogger(getClass());
  
  private static final long DEFAULT_TIME_OUT = 24 * 60 * 60 * 1000; 
  
  /**
   * Handle the execution of a {@link TaskExecution}. Implementors
   * are expected to execute the task asynchronously. 
   * 
   * @param aTask
   *          The task to execute.
   */
  public void handle (TaskExecution aTask) {
    Future<?> future = executors.submit(() -> {
      try {
        Job aJob = jobRepository.findOne(aTask.getJobId());
        long startTime = System.currentTimeMillis();
        logger.debug("Recived task: {}",aTask);
        TaskHandler<?> taskHandler = taskHandlerResolver.resolve(aTask);
        eventPublisher.publishEvent(PiperEvent.of(Events.TASK_STARTED,"taskId",aTask.getId(),"jobId",aTask.getJobId()));
        Object output = taskHandler.handle(aTask, aJob);
        SimpleTaskExecution completion = SimpleTaskExecution.createForUpdate(aTask);
        if(output!=null) {
          if(completion.getOutput() != null) {
            TaskExecution evaluated = taskEvaluator.evaluate(completion, new MapContext ("execution", new MapContext("output", output)));
            completion = SimpleTaskExecution.createForUpdate(evaluated);
          }
          else {
            completion.setOutput(output);
          }
        }
        //This is required to set if COMPLETION/WAITING is handled by DefaultTaskCompletionHandler before the above START event listener.
        completion.setStartTime(new Date(startTime));
        completion.setProgress(100);
        completion.setEndTime(new Date());
        completion.setExecutionTime(System.currentTimeMillis()-startTime);
        if(taskHandler instanceof Wait) {
          completion.setStatus(TaskStatus.WAITING);
          messenger.send(Queues.WAITING, completion);
          //TODO: Currently commenting this as changes done in completion are not updated in DB and causing flow execution issues.
//          eventPublisher.publishEvent(PiperEvent.of(Events.TASK_WAIT, "taskId", aTask.getId(), "jobId", aTask.getJobId()));
        } else {
          completion.set("taskAction", TaskActions.COMPLETE);
          completion.setStatus(TaskStatus.COMPLETED);
          messenger.send(Queues.COMPLETIONS, completion);
        }
      }
      catch (InterruptedException e) {
        // ignore
      }
      catch (Exception e) {
        Future<?> myFuture = taskExecutions.get(aTask.getId());
        if(!myFuture.isCancelled()) {
          handleException(aTask, e);
        }
      }
      finally {
        taskExecutions.remove(aTask.getId());
      }
    });
    
    taskExecutions.put(aTask.getId(), future);
    
    try {
      future.get(calculateTimeout(aTask), TimeUnit.MILLISECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      handleException(aTask, e);
    }
    catch (CancellationException e) {
      logger.debug("Cancelled task: {}", aTask.getId());
    }
    
  }

  /**
   * Handle the execution of a {@link TaskExecution} that is waiting. Implementors
   * are expected to execute the task asynchronously.
   *
   * @param aTask
   *          The task to execute.
   */
  public void handleWaitingTask (TaskExecution aTask) {
    Future<?> future = executors.submit(() -> {
      try {
        long startTime = aTask.getStartTime().getTime();
        logger.debug("Received Waiting task: {}", aTask);
        TaskHandler<?> taskHandler = taskHandlerResolver.resolve(aTask);
        TaskEventHandler<?> eventHandler = (TaskEventHandler<?>) taskHandler;
        Object output = eventHandler.handleEvent(aTask);
        SimpleTaskExecution completion = SimpleTaskExecution.createForUpdate(aTask);

        if (aTask.getStatus() == TaskStatus.WAITING) {
          eventPublisher.publishEvent(PiperEvent.of(Events.TASK_WAIT, "taskId", aTask.getId(), "jobId", aTask.getJobId()));
          messenger.send(Queues.WAITING, completion);
        } else {
          if (output != null) {
            if (completion.getOutput() != null) {
              TaskExecution evaluated = taskEvaluator.evaluate(completion, new MapContext("execution", new MapContext("output", output)));
              completion = SimpleTaskExecution.createForUpdate(evaluated);
            } else {
              completion.setOutput(output);
            }
          }

          completion.setProgress(100);
          completion.setEndTime(new Date());
          completion.setExecutionTime(System.currentTimeMillis() - startTime);

          messenger.send(Queues.COMPLETIONS, completion);
        }
      } catch (InterruptedException e) {
        // ignore
      } catch (Exception e) {
        Future<?> myFuture = taskExecutions.get(aTask.getId());
        if (!myFuture.isCancelled()) {
          handleException(aTask, e);
        }
      } finally {
        taskExecutions.remove(aTask.getId());
      }
    });

    taskExecutions.put(aTask.getId(), future);

    try {
      future.get(calculateTimeout(aTask), TimeUnit.MILLISECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      handleException(aTask, e);
    } catch (CancellationException e) {
      logger.debug("Cancelled Waiting task: {}", aTask.getId());
    }

  }

  private void handleException (TaskExecution aTask, Exception aException) {
    logger.error(aException.getMessage(),aException);
    SimpleTaskExecution task = SimpleTaskExecution.createForUpdate(aTask);
    task.setError(new ErrorObject(aException.getMessage(),ExceptionUtils.getStackFrames(aException)));
    task.setStatus(TaskStatus.FAILED);
    messenger.send(Queues.ERRORS, task);
  }
  
  private long calculateTimeout (TaskExecution aTask) {
    if(aTask.getTimeout() != null) {
      return Duration.parse("PT"+aTask.getTimeout()).toMillis();
    }
    return DEFAULT_TIME_OUT;
  }
  
  /**
   * Handle control tasks. Control tasks are used by the Coordinator
   * to control Worker instances. For example to stop an ongoing task
   * or to adjust something on a worker outside the context of a job.
   */
  public void handle (ControlTask aControlTask) {
    Assert.notNull(aControlTask,"task must not be null");
    if(ControlTask.TYPE_CANCEL.equals(aControlTask.getType())) {
      String taskId = aControlTask.getRequiredString("taskId");
      Future<?> future = taskExecutions.get(taskId);
      if(future != null) {
        logger.info("Cancelling task {}",taskId);
        future.cancel(true);
      }
    }
  }

  public void setTaskHandlerResolver(TaskHandlerResolver aTaskHandlerResolver) {
    taskHandlerResolver = aTaskHandlerResolver;
  }

  public void setMessenger(Messenger aMessenger) {
    messenger = aMessenger;
  }
  
  public void setEventPublisher(EventPublisher aEventPublisher) {
    eventPublisher = aEventPublisher;
  }

  public void setJobRepository(JobRepository aJobRepository) {jobRepository = aJobRepository;}
  
}