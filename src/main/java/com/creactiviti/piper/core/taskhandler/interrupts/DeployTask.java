package com.creactiviti.piper.core.taskhandler.interrupts;

import com.creactiviti.piper.core.task.SimplePipelineTask;
import com.creactiviti.piper.core.task.Task;
import com.creactiviti.piper.workflows.services.SaparateClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.creactiviti.piper.core.taskhandler.interrupts.DeployTask.DeployTaskDSL.*;

@Slf4j
@Component
public class DeployTask extends Wait {

    @Autowired
    private SaparateClientService saparateClientService;

    @Override
    public Object hanldeEvent(Task task) throws Exception {
        return null;
    }

    @Override
    public Object handle(Task aTask) throws Exception {
        log.info("Deploying the task [{}]", aTask);
        Map<String, Object> outputMap = new HashMap<>();
        String deployJobName = aTask.getRequiredString(JOB_NAME);
        String buildNumber = saparateClientService.triggerJenkinsDeployJob(deployJobName, aTask.getRequiredString(JENKINS_AUTH_TOKEN));
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
    }
}

