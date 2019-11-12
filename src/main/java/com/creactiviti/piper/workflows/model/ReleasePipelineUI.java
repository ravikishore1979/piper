package com.creactiviti.piper.workflows.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@ToString
public class ReleasePipelineUI {
    public static final String BUILD_JOB_NAME = "buildJobName";
    public static final String BUILD_NUMBER = "buildNumber";


    private String workflowId;
    private String label;
    private ReleasePipelineBuildInput releasePipelineBuildInput;
    private List<WorkflowTask> tasks;

    @JsonIgnore
    public ReleaseWorkflowYaml getReleaseWorkflowYaml() {
        ReleaseWorkflowYaml rwfYaml = new ReleaseWorkflowYaml();
        WorkflowIO input = new WorkflowIO();
        input.setVariableName(BUILD_JOB_NAME);
        input.setVariableType("String");
        input.setLabel("Jenkins Build Name");
        WorkflowIO input2 = new WorkflowIO();
        input2.setVariableName(BUILD_NUMBER);
        input2.setVariableType("String");
        input2.setLabel("Jenkins Build Number");
        rwfYaml.setWorkflowId(this.getWorkflowId());
        rwfYaml.setLabel(this.label);
        rwfYaml.setTasks(this.tasks);
        rwfYaml.setInputs(Arrays.asList(input, input2));
        return rwfYaml;
    }

}
