package com.creactiviti.piper.core.taskhandler.interrupts;

import com.creactiviti.piper.core.task.Task;
import com.creactiviti.piper.core.task.TaskStatus;
import com.creactiviti.piper.workflows.model.HumanTaskAssignee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApprovalTask extends HumanTask {

    @Override
    protected TaskStatus processBusinessLogic(HumanTaskAssignee humanTaskAssignee, Task task, Object taskOutput) {
        //TODO: Execute the business logic. like all users in the group or few users in the group should approve.
        return TaskStatus.COMPLETED;
    }
}
