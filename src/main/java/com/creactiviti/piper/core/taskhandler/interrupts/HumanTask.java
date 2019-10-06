package com.creactiviti.piper.core.taskhandler.interrupts;

import com.creactiviti.piper.core.task.Task;
import org.springframework.stereotype.Component;

@Component
public class HumanTask extends Wait {
    @Override
    public Object handle(Task aTask) throws Exception {
        return null;
    }
}
