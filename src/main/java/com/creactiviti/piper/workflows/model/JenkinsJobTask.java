package com.creactiviti.piper.workflows.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Getter
@Setter
@ToString
@NoArgsConstructor
@SuperBuilder
@Slf4j
public class JenkinsJobTask extends WorkflowTask {

    @JsonIgnore
    private static String JENKINS_JOB_CREATE_TEMPLATE;

    @JsonIgnore
    public static String JENKINS_TRIGGER_DEPLOY_JOB;
    static {
        try {
            File createJobFile = new ClassPathResource("jenkinsJobCreateTemplate.json").getFile();
            File triggerFile = new ClassPathResource("jenkinsTriggerDeployJob.json").getFile();
            JENKINS_JOB_CREATE_TEMPLATE = new String(Files.readAllBytes(createJobFile.toPath()));
            JENKINS_TRIGGER_DEPLOY_JOB = new String(Files.readAllBytes(triggerFile.toPath()));
        } catch (IOException e) {
            log.error("Error reading the template JSON [jenkinsJobCreateTemplate.json] or [jenkinsTriggerDeployJob.json]", e);
        }
    }

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
