package com.creactiviti.piper.core.taskhandler.interrupts;

import com.creactiviti.piper.core.DSL;
import com.creactiviti.piper.core.job.Job;
import com.creactiviti.piper.core.task.SimpleTaskExecution;
import com.creactiviti.piper.core.task.Task;
import com.creactiviti.piper.core.task.TaskActions;
import com.creactiviti.piper.core.task.TaskStatus;
import com.creactiviti.piper.workflows.model.HumanTaskAction;
import com.creactiviti.piper.workflows.model.HumanTaskAssignee;
import com.creactiviti.piper.workflows.repos.IHumanTaskActionRepository;
import com.creactiviti.piper.workflows.repos.IHumanTaskAssigneeRepository;
import com.creactiviti.piper.workflows.repos.WorkflowJdbcRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public abstract class HumanTask extends Wait {
    @Autowired
    private IHumanTaskActionRepository iHumanTaskActionRepository;
    @Autowired
    private IHumanTaskAssigneeRepository iHumanTaskAssigneeRepository;
    //Use below repository for performing updates (create, update, delete)
    // as using above repositories failing to create transactions thus save is successful but not committed to DB.
    //TODO: Need to analyse why Trasacation is not when above repos are used for save.
    @Autowired
    private WorkflowJdbcRepository workflowJdbcRepository;

    @Override
    public Object handle(Task aTask, Job aJob) throws Exception {
        HumanTaskAssignee taskAssignee = buildHumanTaskAssignee(aTask, aJob);
        workflowJdbcRepository.insertHumanTaskAssignee(taskAssignee);
        log.info("Inserted HumanTask ID  {}", taskAssignee.getHumanTaskId());
        SimpleTaskExecution eTask = (SimpleTaskExecution)aTask;
        eTask.set(DSL.HUMAN_TASK_ID, taskAssignee.getHumanTaskId());
        return null;
    }

    @Override
    public Object handleEvent(Task task) throws Exception {
        Optional<HumanTaskAssignee> htAssignee = iHumanTaskAssigneeRepository.findById(task.getLong(DSL.HUMAN_TASK_ID));
        Assert.isTrue(htAssignee.isPresent(), String.format("humanTaskAssignee object not found for assignee ID [%s]", task.getString(DSL.HUMAN_TASK_ID)));
        HumanTaskAssignee humanTaskAssignee = htAssignee.get();
        Map<String, Object> taskCompleteInput = task.get(DSL.TASK_ACTION_INPUT, Map.class);
        Object taskOutput = taskCompleteInput.get("humanResponse");
        SimpleTaskExecution eTask = (SimpleTaskExecution)task;
        Assert.notNull(taskOutput, "Task Input does not have an object with key [humanResponse]");
        if (isValidAction(humanTaskAssignee,  TaskActions.valueOf(task.getString(DSL.TASK_ACTION)), task.getString(DSL.ACTED_BY))) {
            TaskStatus status = processBusinessLogic(humanTaskAssignee, task, taskOutput);
            eTask.setStatus(status);
            HumanTaskAction taskAction = buildHumanTaskAction(humanTaskAssignee, task);
            workflowJdbcRepository.insertHumanTaskAction(taskAction);
        }
        return taskOutput;
    }

    protected abstract TaskStatus processBusinessLogic(HumanTaskAssignee humanTaskAssignee, Task task, Object taskOutput);

    protected boolean isValidAction(HumanTaskAssignee humanTaskAssignee, TaskActions taskActions, String actedBy) {
        Assert.notNull(actedBy, String.format("actedBy should not be null. for taskID [%s]", humanTaskAssignee.getTaskInstanceId()));
        Assert.notNull(humanTaskAssignee.getAssigneeName(), String.format("assigneeName  should not be null. for taskID [%s]", humanTaskAssignee.getTaskInstanceId()));
        if(!actedBy.equals(humanTaskAssignee.getAssigneeName())) {
            log.error("Task should executed by [{}] but executed by [{}]", humanTaskAssignee.getAssigneeName(), actedBy);
            return false;
        }
        return true;
    }

    private HumanTaskAssignee buildHumanTaskAssignee(Task aTask, Job aJob) {

        Assert.notNull(aJob, String.format("Job object is null in Task object [%s]", aTask.getString(DSL.ID)));

        HumanTaskAssignee humanTaskAssignee = HumanTaskAssignee.builder()
                .assigneeId(aTask.getString(DSL.ASSIGNEE_ID))
                .assigneeType(aTask.getString(DSL.ASSIGNEE_TYPE))
                .assigneeName(aTask.getString(DSL.ASSIGNEE_TO))
                .taskInstanceId(aTask.getString(DSL.ID))
                .assignDate(new Date())
                .businessLogicID("ALL")
                .releaseWorkflow(aJob.getLabel())
                .releaseCycleName(aJob.getJobCycleName()).build();
        return humanTaskAssignee;
    }

    private HumanTaskAction buildHumanTaskAction(HumanTaskAssignee assignee, Task atask) {
        HumanTaskAction taskAction = HumanTaskAction.builder()
                .actionDate(new Date())
                .actionDoneBy(atask.getString(DSL.ACTED_BY))
                .actionName(atask.getString(DSL.TASK_ACTION))
                .humanTaskId(assignee.getHumanTaskId())
                .taskInstanceId(atask.get(DSL.ID)).build();
        return taskAction;
    }
}
