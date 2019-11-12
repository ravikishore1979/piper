package com.creactiviti.piper.workflows.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReleasePipelineBuildInput {

    private String buildPipelineJobName;
    private String buildPipelineBuildID;
}
