package com.creactiviti.piper.workflows.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@NoArgsConstructor
@SuperBuilder
public class JenkinsJobTask extends WorkflowTask {

    @JsonIgnore
    private static String JENKINS_JOB_CREATE_TEMPLATE = "{\n" +
            "   \"jobName\":\"%s\",\n" +
            "   \"piperPipeline\":{\n" +
            "      \"stages\":[\n" +
            "         {\n" +
            "            \"name\":\"Deploy\",\n" +
            "            \"statementList\":[\n" +
            "                {\n" +
            "                  \"type\":\"gst\",\n" +
            "                  \"statment\":\"writeFile file:'.pipeline/config.yaml', text: \\\"${params.configyaml}\\\"\"\n" +
            "               },\n" +
            "                {\n" +
            "                  \"type\":\"gst\",\n" +
            "                  \"statment\":\"setupCommonPipelineEnvironment script:this\"\n" +
            "               },\n" +
            "               {\n" +
            "                  \"type\":\"gst\",\n" +
            "                  \"statment\":\"cloudFoundryDeploy script: this\"\n" +
            "               }\n" +
            "            ]\n" +
            "         }\n" +
            "      ]\n" +
            "   }\n" +
            "}";


    private String jenkinsJobName;
    private String jenkinsJobBuildID;

    /**
     * This is runtime data.
     * Value will be set when this task started and a jenkins build is triggered.
     */
    private String jenkinsJobStatus;
    private String cfCredentialsID;
    private String waitForMessage;
    @Builder.Default
    private String type = "deployTask";
    private String buildJobName;
    private String buildJobNumber;


    @JsonIgnore
    public String getJenkinsJobCreateTemplateStr(String jobName) {
        return String.format(JENKINS_JOB_CREATE_TEMPLATE, jobName);
    }

}
