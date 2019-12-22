package com.creactiviti.piper.core.taskhandler.interrupts;

import com.creactiviti.piper.core.DSL;
import com.creactiviti.piper.core.job.Job;
import com.creactiviti.piper.core.task.Task;
import org.springframework.util.Assert;

import java.util.Map;

public class AsyncRest extends Wait {
    /**
     * What to be done after getting a callback after task Execution.
     * @param task
     * @return
     * @throws Exception
     */
    @Override
    public Object handleEvent(Task task) throws Exception {
        Map<String, Object> taskCompleteInput = task.get(DSL.TASK_ACTION_INPUT, Map.class);
        Object taskOutput = taskCompleteInput.get("humanResponse");
        Assert.notNull(taskOutput, "Task Input does not have an object with key [humanResponse]");
        return taskOutput;

    }

    @Override
    public Object handle(Task aTask, Job aJob) throws Exception {
        //Implementation for calling a Asynchronous REST API.
        String url = aTask.get("restapi");

        return null;
    }
}
