package com.creactiviti.piper.workflows.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class WorkflowException extends RuntimeException {
    public WorkflowException(String msg, Throwable e) {
        super(msg, e);
    }
}
