package com.creactiviti.piper.core.pipeline;

import com.creactiviti.piper.workflows.services.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.nio.charset.Charset;
import java.util.List;

public class DbPipelineRepository extends YamlPipelineRepository {

    @Autowired
    private WorkflowService workflowService;

    @Override
    public Pipeline findOne(String s) {

        String[] pipelineDetails = workflowService.getPipelineByPiperID(s);
        String pipelineYaml = pipelineDetails[1];
        String pipelineId = pipelineDetails[0];
        Assert.notNull(pipelineYaml, String.format("Pipeline with name '%s' not found.", s));

        Resource wfResource = new ByteArrayResource(pipelineYaml.getBytes(Charset.forName("UTF-8")));
        IdentifiableResource resource = new IdentifiableResource(pipelineId, wfResource);
        return parsePipeline(resource);
    }

    @Override
    public List<Pipeline> findAll() {
        return null;
    }
}
