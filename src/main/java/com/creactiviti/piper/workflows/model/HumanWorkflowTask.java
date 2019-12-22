package com.creactiviti.piper.workflows.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@ToString
@SuperBuilder
public abstract class HumanWorkflowTask extends WorkflowTask {
    @JsonProperty("waitUntil")
    protected String waitForMessage;
    @Builder.Default
    protected TaskCategory taskCategory = TaskCategory.TASK;
    protected String categoryFor;
    /**
     *  ID of the User/group from user module;
     */
    protected String assignID;
    /**
     * Name of the user/group
     */
    protected String assignedTo;
    @Builder.Default
    protected AssignType assignType=AssignType.USER;
    protected String message;
}
