package com.creactiviti.piper.workflows.services;

import com.creactiviti.piper.workflows.exceptions.WorkflowException;
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

@Slf4j
@Service
@ConditionalOnProperty(name="saparate.rest.url")
public class SaparateClientService implements InitializingBean {

    @Value("${saparate.rest.url}")
    private String saparateUrl;

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

    public String triggerJenkinsDeployJob(String jobName, String authToken) {
        JsonNode inputJson = null;
        String buildNumber = null;
        try {
            inputJson = objectMapper.readTree(String.format("{\"jobName\":\"%s\"}", jobName));
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);
            HttpEntity<JsonNode> httpEntity = new HttpEntity<>(inputJson, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(this.saparateUrl.concat("/jenkins/triggerDeployJob"), HttpMethod.POST, httpEntity, String.class);
            log.info("Response: {}", responseEntity.getBody());
        } catch (IOException | RestClientException e) {
            String errorMsg = String.format("Exception while running Deploy pipeline job in Jenkins. [%s]", jobName);
            log.error(errorMsg, e);
            throw new WorkflowException(errorMsg, e);
        }
        return buildNumber;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        this.restTemplate = new RestTemplate();
    }
}
