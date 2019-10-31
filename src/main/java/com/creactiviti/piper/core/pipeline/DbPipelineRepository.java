package com.creactiviti.piper.core.pipeline;

import com.creactiviti.piper.core.MapObject;
import com.creactiviti.piper.workflows.model.Workflow;
import com.creactiviti.piper.workflows.services.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.nio.charset.Charset;
import java.util.List;

@Slf4j
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

    @Override
    public boolean validateInputForRun(MapObject jobParams) {
        String pipelineId = jobParams.getRequiredString("pipelineId");
        String[] ids = pipelineId.split(":");
        if(ids.length >= 2) {
            Workflow wf = workflowService.getPipelineByID(ids[0]);
            if(wf.getHeadRevision() != Long.parseLong(ids[1])) {
                String errorMsg = String.format("Given revision '%s' is not same as the head revision '%s' for Workflow ID '%s'", ids[1], wf.getHeadRevision(), wf.getId());
                IllegalArgumentException iae = new IllegalArgumentException(errorMsg);
                log.error(errorMsg, iae);
                throw iae;
            }
        }
        return true;
    }
}
