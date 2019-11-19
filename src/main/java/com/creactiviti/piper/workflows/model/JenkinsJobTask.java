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
            "   \"clodfoundryId\":\"%s\",\n" +
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

    @JsonIgnore
    public static String JENKINS_TRIGGER_DEPLOY_JOB = "{\n" +
            "\"jobName\":\"%s\",\n" +
            "\"buildPipelineJobName\":\"%s\",\n" +
            "\"buildPipelineBuildNumber\":\"%s\"\n" +
            "}";

    private String jenkinsJobName;
    private String jenkinsBuildNumber;

    /**
     * This is runtime data.
     * Value will be set when this task started and a jenkins build is triggered.
     */
    private String jenkinsBuildStatus;
    private String cfCredentialsID;
    private String waitForMessage;
    @Builder.Default
    private String type = "deployTask";
    private String buildJobName;
    private String buildJobNumber;
    private String jenkinsAuthToken;

    @JsonIgnore
    public String getJenkinsJobCreateTemplateStr() {
        return String.format(JENKINS_JOB_CREATE_TEMPLATE, this.jenkinsJobName, this.cfCredentialsID);
    }

}
