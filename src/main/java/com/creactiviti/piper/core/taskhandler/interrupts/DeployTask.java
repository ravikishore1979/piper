package com.creactiviti.piper.core.taskhandler.interrupts;

import com.creactiviti.piper.core.DSL;
import com.creactiviti.piper.core.job.Job;
import com.creactiviti.piper.core.task.SimplePipelineTask;
import com.creactiviti.piper.core.task.SimpleTaskExecution;
import com.creactiviti.piper.core.task.Task;
import com.creactiviti.piper.core.task.TaskStatus;
import com.creactiviti.piper.workflows.services.SaparateClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

import static com.creactiviti.piper.core.taskhandler.interrupts.DeployTask.DeployTaskDSL.*;

@Slf4j
@Component
public class DeployTask extends Wait {

    @Autowired
    private SaparateClientService saparateClientService;

    @Override
    public Object handleEvent(Task task) throws Exception {
        Map<String, Object> taskCompleteInput = task.get(DSL.TASK_ACTION_INPUT, Map.class);
        Object taskOutput = taskCompleteInput.get("humanResponse");
        Assert.notNull(taskOutput, "Task Input does not have an object with key [humanResponse]");
        Map<String, Object> jenkinsResponse = (Map<String, Object>) taskOutput;
        String jobStatus = (String) jenkinsResponse.get("buildStatus");
        int buildNumber = (Integer) jenkinsResponse.get("buildNumber");
        SimpleTaskExecution taskObj = (SimpleTaskExecution) task;
        taskObj.set("jenkinsBuildNumber", buildNumber);
        taskObj.set("jenkinsBuildStatus", jobStatus);
        if("FAILURE".equalsIgnoreCase(jobStatus)) {
            String errorMsg = (String) jenkinsResponse.get("errorMsg");
            throw new Exception(errorMsg);
        }
        taskObj.setStatus(TaskStatus.COMPLETED);
        return taskOutput;
    }

    @Override
    public Object handle(Task aTask, Job aJob) throws Exception {
        log.info("Deploying the task [{}]", aTask);
        Map<String, Object> outputMap = new HashMap<>();
        String deployJobName = aTask.getRequiredString(JOB_NAME);
        String buildNumber = saparateClientService.triggerJenkinsDeployJob(deployJobName, aTask.getRequiredString(JENKINS_AUTH_TOKEN), aTask.getRequiredString(BUILD_PIPELINE_NAME), aTask.getRequiredString(BUILD_PIPELINE_BUILD_NUMBER), aTask.getRequiredString("id"));
        if(aTask instanceof SimplePipelineTask) {
            SimplePipelineTask pipelineTask = (SimplePipelineTask) aTask;
            pipelineTask.put(BUILD_NUMBER, buildNumber);
        }
        outputMap.put(BUILD_NUMBER, buildNumber);
        outputMap.put(JOB_NAME, deployJobName);

        return outputMap;
    }

    interface DeployTaskDSL {
        String JOB_NAME = "jenkinsJobName";
        String BUILD_NUMBER = "jenkinsBuildNumber";
        String BUILD_STATUS = "jenkinsBuildStatus";
        String JENKINS_AUTH_TOKEN = "jenkinsAuthToken";
        String BUILD_PIPELINE_NAME = "buildJobName";
        String BUILD_PIPELINE_BUILD_NUMBER = "buildJobNumber";
    }
}

