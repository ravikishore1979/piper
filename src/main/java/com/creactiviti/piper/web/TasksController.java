package com.creactiviti.piper.web;


import com.creactiviti.piper.core.Coordinator;
import com.creactiviti.piper.core.job.Job;
import com.creactiviti.piper.core.task.TaskActions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("tasks")
public class TasksController {

    @Autowired
    private Coordinator coordinator;

    @PutMapping("/{taskId}")
    public Job performTaskAction(@PathVariable(name = "taskId") String taskId, @RequestParam(name = "action") TaskActions action,
                            @RequestBody Map<String, Object> taskOutput) {

        switch (action) {
            case FINISHED: {
                IllegalArgumentException ex = new IllegalArgumentException(String.format("[FINISHED] task action is not yet implemented. task ID [%s]", taskId));
                log.error(ex.getMessage(), ex);
                throw ex;
            }
            case COMPLETE: {
                return coordinator.completeTaskAndResumeJob(taskId, taskOutput);
            }
            default: {
                throw new IllegalArgumentException(String.format("Invalid action [%s] for task [%s]", action, taskId));
            }
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public void handleIllegalArgumentException (IllegalArgumentException ex, HttpServletRequest request, HttpServletResponse aResponse) throws IOException {
        log.error("Exception occurred while processing the REST API {}", request.getRequestURI(), ex);
        aResponse.sendError(HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(Exception.class)
    public void handleUncaughtExceptions (Exception ex, HttpServletRequest request, HttpServletResponse aResponse) throws IOException {
        log.error("Error found for REST API {}", request.getRequestURI(), ex);
        aResponse.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
