package com.creactiviti.piper.workflows.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@ToString
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ApprovalTask.class, name = "approvalTask"),
        @JsonSubTypes.Type(value = RandomInt.class, name = "randomInt"),
        @JsonSubTypes.Type(value = PrintTask.class, name = "print"),
        @JsonSubTypes.Type(value = JenkinsJobTask.class, name = "deployTask")
})
public class WorkflowTask {
    private String name;
    private String label;
    private String stageName;
}
