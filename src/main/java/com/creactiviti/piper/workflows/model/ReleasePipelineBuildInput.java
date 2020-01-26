package com.creactiviti.piper.workflows.model;

import com.creactiviti.piper.validation.SafeText;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static com.creactiviti.piper.validation.ValidateRegex.REGEX_DESC;
import static com.creactiviti.piper.validation.ValidateRegex.REGEX_NUMERIC;

@Getter
@Setter
@ToString
public class ReleasePipelineBuildInput {

    @SafeText(regex = REGEX_DESC)
    private String buildPipelineJobName;
    @SafeText(regex = REGEX_NUMERIC)
    private String buildPipelineBuildID;
}
