package com.creactiviti.piper.core.event;

import com.creactiviti.piper.core.task.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskWaitEventListener implements EventListener {

    private TaskExecutionRepository taskExecutionRepository;

    public TaskWaitEventListener(TaskExecutionRepository taskExecutionRepository) {
        this.taskExecutionRepository = taskExecutionRepository;
    }

    @Override
    public void onApplicationEvent (PiperEvent aEvent) {
        if(Events.TASK_WAIT.equals(aEvent.getType())) {
            String taskId = aEvent.getString("taskId");
            TaskExecution task = taskExecutionRepository.findOne(taskId);
            if(task == null) {
                log.error("Unkown task: {}", taskId);
            } else {
                SimpleTaskExecution mtask = SimpleTaskExecution.createForUpdate(task);
                if(mtask.getStatus() != TaskStatus.WAITING) {
                    mtask.setStartTime(aEvent.getCreateTime());
                    mtask.setStatus(TaskStatus.WAITING);
                    taskExecutionRepository.merge(mtask);
                }
                if(mtask.getParentId()!=null) {
                    PiperEvent pevent = PiperEvent.of(Events.TASK_WAIT,"taskId", mtask.getParentId());
                    onApplicationEvent(pevent);
                }
            }
        }
    }
}
