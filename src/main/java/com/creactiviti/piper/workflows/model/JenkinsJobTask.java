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
            "   \"jobType\":\"deploy\",\n" +
            "   \"clodfoundryId\":\"%s\",\n" +
            "   \"piperPipeline\": {\n" +
            "  \"stages\" : [ {\n" +
            "    \"name\" : \"Deploy\",\n" +
            "    \"surroundWithTryCatch\" : true,\n" +
            "    \"statementList\" : [ {\n" +
            "      \"type\" : \"gst\",\n" +
            "      \"statment\" : \" sh \\\"cp -ra \\\\\\\"${JENKINS_HOME}/workspace/${params.BUILD_JOB_NAME}/${params.BUILD_JOB_NUMBER}/.\\\\\\\"  \\\\\\\"${JENKINS_HOME}/workspace/${JOB_NAME}/\\\\\\\"\\\"\"\n" +
            "    }, {\n" +
            "      \"type\" : \"gst\",\n" +
            "      \"statment\" : \"writeFile file:'.pipeline/config.yaml', text: \\\"${params.configyaml}\\\"\"\n" +
            "    }, {\n" +
            "       \"type\":\"gst\",\n" +
            "       \"statment\":\"setupCommonPipelineEnvironment script:this\"\n" +
            "    }, {\n" +
            "      \"type\" : \"gst\",\n" +
            "      \"statment\" : \"cloudFoundryDeploy script: this\"\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"name\" : \"Webhook\",\n" +
            "    \"surroundWithTryCatch\" : false,\n" +
            "    \"statementList\" : [ {\n" +
            "      \"type\" : \"gst\",\n" +
            "      \"statment\" : \"if(buildFailed) {\\n            currentBuild.result = 'FAILURE'\\n        } else {\\n            currentBuild.result = 'SUCCESS'\\n        }\"\n" +
            "    }, {\n" +
            "      \"type\" : \"gst\",\n" +
            "      \"statment\" : \"echo \\\"Error Msg: ${errorMsg}\\\"\"\n" +
            "    }, {\n" +
            "      \"type\" : \"gst\",\n" +
            "      \"statment\" : \"echo 'Current Build Status: ' + currentBuild.result\"\n" +
            "    }, {\n" +
            "      \"type\" : \"gst\",\n" +
            "      \"statment\" : \"echo 'Build Number: ' + currentBuild.number\"\n" +
            "    }, {\n" +
            "      \"type\" : \"gst\",\n" +
            "      \"statment\" : \"echo \\\"Env-FLOW_TASK_ID: ${params.RATE_FLOW_TASK_ID}\\\"\"\n" +
            "    }, {\n" +
            "      \"type\" : \"gst\",\n" +
            "      \"statment\" : \"def inputMsg = '{ \\\"humanResponse\\\": { \\\"buildNumber\\\" : ' + currentBuild.number + ', \\\"buildStatus\\\" : \\\"' + currentBuild.result + '\\\", \\\"errorMsg\\\" : \\\"' + errorMsg + '\\\" } }'\"\n" +
            "    }, {\n" +
            "      \"type\" : \"gst\",\n" +
            "      \"statment\" : \"httpRequest acceptType: 'APPLICATION_JSON', consoleLogResponseBody: true, contentType: 'APPLICATION_JSON', customHeaders: [[maskValue: false, name: 'Authorization', value: \\\"${params.RATE_AUTH_TOKEN}\\\"]], httpMode: 'PUT', requestBody: \\\"${inputMsg}\\\", responseHandle: 'NONE', timeout: 120, url: params.RATE_URL +'/tasks/' + params.RATE_FLOW_TASK_ID + '?action=COMPLETE', validResponseCodes: '100:599'\"\n" +
            "    } ]\n" +
            "  } ]\n" +
            "}"+
            "}";

    @JsonIgnore
    public static String JENKINS_TRIGGER_DEPLOY_JOB = "{\n" +
            "\"jobName\":\"%s\",\n" +
            "\"buildPipelineJobName\":\"%s\",\n" +
            "\"buildPipelineBuildNumber\":\"%s\",\n" +
            "\"triggerInputParams\" : {\n" +
                "\"BUILD_JOB_NAME\":\"%s\",\n" +
                "\"BUILD_JOB_NUMBER\":\"%s\",\n" +
                "\"RATE_AUTH_TOKEN\":\"%s\",\n" +
                "\"RATE_URL\":\"%s\",\n" +
                "\"RATE_FLOW_TASK_ID\":\"%s\"\n" +
                "}" +
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
