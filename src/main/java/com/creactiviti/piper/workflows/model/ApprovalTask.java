package com.creactiviti.piper.workflows.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@ToString
@SuperBuilder
public class ApprovalTask extends WorkflowTask{

    @JsonProperty("waitUntil")
    private String waitForMessage;
    @Builder.Default
    private String type = "humanTask";
    @Builder.Default
    private TaskCategory taskCategory = TaskCategory.TASK;
    private String categoryFor;
    /**
     *  ID of the User/group from user module;
     */
    private String assignID;
    /**
     * Name of the user/group
     */
    private String assignedTo;
    @Builder.Default
    private AssignType assignType=AssignType.USER;
    private String message;
}
