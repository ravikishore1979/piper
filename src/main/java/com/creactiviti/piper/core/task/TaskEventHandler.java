package com.creactiviti.piper.core.task;

public interface TaskEventHandler<O> {
    O handleEvent(Task task) throws Exception;
}
