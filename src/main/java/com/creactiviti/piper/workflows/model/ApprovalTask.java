package com.creactiviti.piper.workflows.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@ToString
@SuperBuilder
public class ApprovalTask extends HumanWorkflowTask{

    @Builder.Default
    private String type = "approvalTask";
}
