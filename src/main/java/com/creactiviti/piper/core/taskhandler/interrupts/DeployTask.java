package com.creactiviti.piper.core.taskhandler.interrupts;

import com.creactiviti.piper.core.task.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class DeployTask extends Wait {
    @Override
    public Object hanldeEvent(Task task) throws Exception {
        return null;
    }

    @Override
    public Object handle(Task aTask) throws Exception {
        log.info("atask");
        return null;
    }
}
