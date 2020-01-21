package com.creactiviti.piper.workflows.services;

import com.creactiviti.piper.workflows.exceptions.WorkflowException;
import com.creactiviti.piper.workflows.model.JenkinsJobTask;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(name="saparate.rest.url")
public class SaparateClientService implements InitializingBean {

    @Value("${saparate.rest.url}")
    private String saparateUrl;
    @Value("${workflow.rest.url}")
    private String workflowUrl;

    @Autowired
    private ObjectMapper objectMapper;
    private RestTemplate restTemplate;

    public void createOrUpdateJenkinsDeployJob(String inputJsonStr, String authToken) {
        JsonNode inputJson = null;
        String jobName = null;
        try {
            inputJson = objectMapper.readTree(inputJsonStr);
            jobName = inputJson.findPath("jobName").textValue();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);
            HttpEntity<JsonNode> httpEntity = new HttpEntity<>(inputJson, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(this.saparateUrl.concat("/jenkins/addOrUpdateJob"), HttpMethod.POST, httpEntity, String.class);
            log.info("Jenkins Deploy job {} creation Response: {}", jobName, responseEntity.getBody());
        } catch (IOException | RestClientException e) {
            String errorMsg = String.format("Error while creating the Deploy pipeline job in Jenkins. [%s]", jobName);
            throw new WorkflowException(errorMsg, e);
        }
    }
    public String triggerJenkinsDeployJob(String jobName, String authToken, String buildPipelineName, String buildPipelineBuildNumber, String taskInstanceId) {
        JsonNode inputJson = null;
        String buildNumber = null;
        try {
            inputJson = objectMapper.readTree(String.format(JenkinsJobTask.JENKINS_TRIGGER_DEPLOY_JOB, jobName, buildPipelineName, buildPipelineBuildNumber, buildPipelineName, buildPipelineBuildNumber, authToken, workflowUrl, taskInstanceId));
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);
            HttpEntity<JsonNode> httpEntity = new HttpEntity<>(inputJson, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(this.saparateUrl.concat("/jenkins/triggerDeployJob"), HttpMethod.POST, httpEntity, String.class);
            JsonNode outputJson = objectMapper.readTree(responseEntity.getBody());
            log.info("Response: {}", responseEntity.getBody());
            buildNumber = outputJson.get("cycleNumber").asText("");
            log.info("Trigger deploy job with build Number: {}", buildNumber);
        } catch (IOException | RestClientException e) {
            String errorMsg = String.format("Exception while running Deploy pipeline job in Jenkins. [%s]", jobName);
            log.error(errorMsg, e);
            throw new WorkflowException(errorMsg, e);
        }
        return buildNumber;
    }

    public Map<String, JsonNode> getCFDetails(String authToken) {
        Map<String, JsonNode> cfMap = new HashMap<>();
        String jobName = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);
            HttpEntity httpEntity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(this.saparateUrl.concat("/cloudfoundary/getAll"), HttpMethod.GET, httpEntity, String.class);
            String responseBody = responseEntity.getBody();
            log.info("Retrieved CF details Response: {}", responseBody);
            JsonNode outputJson = objectMapper.readTree(responseBody);
            outputJson.elements().forEachRemaining(jsonNode -> {
                cfMap.put(jsonNode.findPath("id").asText(), jsonNode);
            });
            return cfMap;
        } catch (IOException | RestClientException e) {
            String errorMsg = String.format("Error while creating the Deploy pipeline job in Jenkins. [%s]", jobName);
            throw new WorkflowException(errorMsg, e);
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        this.restTemplate = new RestTemplate();
    }
}
