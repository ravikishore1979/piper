package com.creactiviti.piper.workflows.model;

import com.creactiviti.piper.validation.SafeText;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Email;

import java.util.Arrays;
import java.util.List;

import static com.creactiviti.piper.validation.ValidateRegex.*;

@Getter
@Setter
@ToString
public class ReleasePipelineUI {
    public static final String BUILD_JOB_NAME = "buildJobName";
    public static final String BUILD_NUMBER = "buildNumber";
    public static final String JENKINS_AUTH_TOKEN = "authToken";

    @SafeText(regex = REGEX_DESC)
    private String workflowId;
    @SafeText(regex = REGEX_DESC)
    private String label;
    @Email
    private String createdBy;
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
