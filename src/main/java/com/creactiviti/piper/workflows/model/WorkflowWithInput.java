package com.creactiviti.piper.workflows.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkflowWithInput extends Workflow{

    private ReleasePipelineBuildInput buildInput;
}
