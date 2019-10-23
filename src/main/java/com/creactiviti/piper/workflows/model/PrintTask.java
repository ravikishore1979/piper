package com.creactiviti.piper.workflows.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@ToString
@SuperBuilder
public class PrintTask extends WorkflowTask {
    private String text;
    @Builder.Default
    private String type = "print";
}
