package com.creactiviti.piper.workflows.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowIO {

    @JsonProperty("name")
    private String variableName;
    private String label;
    @JsonProperty("type")
    private String variableType;
    private boolean required;
    private Object value;
}
