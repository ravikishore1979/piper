package com.creactiviti.piper.core.taskhandler.interrupts;

import com.creactiviti.piper.core.task.Task;
import com.creactiviti.piper.core.task.TaskStatus;
import com.creactiviti.piper.workflows.model.HumanTaskAssignee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ManualTask extends HumanTask {

    @Override
    protected TaskStatus processBusinessLogic(HumanTaskAssignee humanTaskAssignee, Task task, Object taskOutput) {
        return TaskStatus.COMPLETED;
    }
}
