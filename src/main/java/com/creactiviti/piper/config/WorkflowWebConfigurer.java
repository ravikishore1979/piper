package com.creactiviti.piper.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Slf4j
@Configuration
public class WorkflowWebConfigurer extends WebMvcConfigurerAdapter {

    @Value("${rate.cors.origins}")
    private String[] origins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("ORIGINS: {}", origins);
        registry.addMapping("/**").allowedOrigins(origins);
    }
}
