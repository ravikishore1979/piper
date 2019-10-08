package com.creactiviti.piper.core.task;

public interface TaskEventHandler<O> {
    O hanldeEvent(Task task) throws Exception;
}
