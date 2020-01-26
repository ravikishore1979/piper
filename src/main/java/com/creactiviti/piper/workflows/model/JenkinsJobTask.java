package com.creactiviti.piper.workflows.model;

import com.creactiviti.piper.validation.SafeText;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.creactiviti.piper.validation.ValidateRegex.REGEX_DESC;
import static com.creactiviti.piper.validation.ValidateRegex.REGEX_NUMERIC;

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

    @SafeText(regex = REGEX_DESC)
    private String jenkinsJobName;
    @SafeText(regex = REGEX_NUMERIC)
    private String jenkinsBuildNumber;

    /**
     * This is runtime data.
     * Value will be set when this task started and a jenkins build is triggered.
     */
    @SafeText
    private String jenkinsBuildStatus;
    @SafeText(regex = REGEX_NUMERIC)
    private String cfCredentialsID;
    @SafeText
    private String waitForMessage;
    @Builder.Default
    private String type = "deployTask";
    @SafeText(regex = REGEX_DESC)
    private String buildJobName;
    @SafeText(regex = REGEX_NUMERIC)
    private String buildJobNumber;
    @SafeText
    private String jenkinsAuthToken;

    @JsonIgnore
    public String getJenkinsJobCreateTemplateStr() {
        return String.format(JENKINS_JOB_CREATE_TEMPLATE, this.jenkinsJobName, this.cfCredentialsID);
    }

}
