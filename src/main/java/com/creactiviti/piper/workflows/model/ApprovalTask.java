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
}
