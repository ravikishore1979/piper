package com.creactiviti.piper.workflows.model;

import com.creactiviti.piper.validation.SafeText;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import static com.creactiviti.piper.validation.ValidateRegex.REGEX_DESC;

@Getter
@Setter
@NoArgsConstructor
@ToString
@SuperBuilder
public abstract class HumanWorkflowTask extends WorkflowTask {
    @JsonProperty("waitUntil")
    @SafeText
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
    @SafeText(regex = REGEX_DESC)
    protected String message;
}
