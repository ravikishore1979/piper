package com.creactiviti.piper.workflows.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ReleaseWorkflow {
    private String label;
    private List<WorkflowIO> inputs;
    private List<WorkflowIO> outputs;
    private List<WorkflowTask> tasks;
}
