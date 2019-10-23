package com.creactiviti.piper.workflows.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@ToString
@SuperBuilder
public class RandomInt extends WorkflowTask {
    private int startInclusive;
    private int endInclusive;
    @Builder.Default
    private String type = "randomInt";
}
