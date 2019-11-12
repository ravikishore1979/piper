package com.creactiviti.piper.workflows.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@ConditionalOnProperty(name="saparate.rest.url")
public class SaparateClientService {

    @Value("${saparate.rest.url}")
    private String saparateUrl;
    private RestTemplate restTemplate;

    public void createOrUpdateJenkinsDeployJob() {

    }

}
