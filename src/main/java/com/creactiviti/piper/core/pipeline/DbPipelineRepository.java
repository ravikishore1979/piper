package com.creactiviti.piper.core.pipeline;

import com.creactiviti.piper.core.pipeline.Pipeline;
import com.creactiviti.piper.core.pipeline.YamlPipelineRepository;
import com.creactiviti.piper.workflows.model.Workflow;
import com.creactiviti.piper.workflows.services.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.nio.charset.Charset;
import java.util.List;

public class DbPipelineRepository extends YamlPipelineRepository {

    @Autowired
    private WorkflowService workflowService;

    @Override
    public Pipeline findOne(String s) {

        Workflow wf = workflowService.getPipelineByName(s);
        if(wf == null) {
            throw new IllegalArgumentException(String.format("Pipeline with name '%s' not found.", s));
        }

        Resource wfResource = new ByteArrayResource(wf.getWorkflow().getBytes(Charset.forName("UTF-8")));
        IdentifiableResource resource = new IdentifiableResource(s, wfResource);
        return parsePipeline(resource);
    }

    @Override
    public List<Pipeline> findAll() {
        return null;
    }
}
