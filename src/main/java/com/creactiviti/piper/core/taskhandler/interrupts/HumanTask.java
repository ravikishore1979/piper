package com.creactiviti.piper.core.taskhandler.interrupts;

import com.creactiviti.piper.core.task.Task;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Map;

@Component
public class HumanTask extends Wait {
    @Override
    public Object handle(Task aTask) throws Exception {
        return null;
    }

    @Override
    public Object hanldeEvent(Task task) throws Exception {
        Map<String, Object> taskCompleteInput = task.get("taskCompleteInput");
        Object taskOutput = taskCompleteInput.get("humanResponse");
        Assert.notNull(taskOutput, "Task Input does not have an object with key [humanResponse]");
        return taskOutput;
    }
}
