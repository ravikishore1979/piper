package com.creactiviti.piper.workflows;

import com.creactiviti.piper.workflows.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class ReleaseWorkflowYamlTest {

    private static YAMLMapper yamlObjectMapper;
    private static ObjectMapper jsonObjectMapper;

    private static String yamlString = "label: \"jsonToYaml\"\n"+
            "inputs:\n"+
            "- label: \"Release Workflow Label:\"\n"+
            "  required: true\n"+
            "  name: \"name1\"\n"+
            "  type: \"String\"\n"+
            "outputs:\n"+
            "- required: false\n"+
            "  value: \"${randomNumber}\"\n"+
            "  name: \"magicNumer\"\n"+
            "tasks:\n"+
            "- !<randomInt>\n"+
            "  name: \"Stage1\"\n"+
            "  label: \"RandomInt Stage 1\"\n"+
            "  startInclusive: 0\n"+
            "  endInclusive: 5000\n"+
            "  type: \"randomInt\"\n"+
            "- !<print>\n"+
            "  name: \"Stage2\"\n"+
            "  label: \"Stage 2\"\n"+
            "  text: \"Before Approval test test print ${name1}\"\n"+
            "  type: \"print\"\n"+
            "- !<humanTask>\n"+
            "  name: \"Approval1\"\n"+
            "  label: \"Approval1\"\n"+
            "  type: \"humanTask\"\n"+
            "  waitUntil: \"humanResponse\"\n"+
            "- !<print>\n"+
            "  name: \"Stage4\"\n"+
            "  label: \"Stage 4\"\n"+
            "  text: \"After Approval test print ${name1} with taskOutput Details ${approval1.msg}\\\n"+
            "    \\ ==== ${approval1.subObj.k2}\"\n"+
            "  type: \"print\"";

    @BeforeAll
    static void init() {
        yamlObjectMapper = new YAMLMapper(new YAMLFactory());
//        yamlObjectMapper.configure(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID, false);
//        yamlObjectMapper.configure(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE, true);
        yamlObjectMapper.configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false);

        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    @Test
    void getWorkflowYaml() throws JsonProcessingException {
        ReleasePipelineUI workflow = getReleaseWorkflow();
        ReleaseWorkflowYaml yamlObj = workflow.getReleaseWorkflowYaml();

        log.info("\n Yaml: \n" + yamlObjectMapper.writeValueAsString(yamlObj));
    }

    @Test
    void loadYamlString() throws IOException {
        ReleaseWorkflowYaml workflow = yamlObjectMapper.readValue(yamlString, ReleaseWorkflowYaml.class);
        assertNotNull(workflow);
        log.info("release Workflow Object: {}", workflow.toString());
    }

    @Test
    void getWorkflowJson() throws JsonProcessingException {
        ReleasePipelineUI workflow = getReleaseWorkflow();
        log.info("\n JSON: \n {}", jsonObjectMapper.writeValueAsString(workflow));
    }

    private ReleasePipelineUI getReleaseWorkflow() {
        ReleasePipelineUI workflow = new ReleasePipelineUI();
        workflow.setLabel("jsonToYaml");

        ReleasePipelineBuildInput rpbi = new ReleasePipelineBuildInput();
        rpbi.setBuildPipelineBuildID("Latest");
        rpbi.setBuildPipelineJobName("buildPipeLine1");
        workflow.setReleasePipelineBuildInput(rpbi);

        List<WorkflowTask> tasks = new ArrayList<>();
        tasks.add(RandomInt.builder()
                .name("randomNumber")
                .label("RandomInt Stage 1")
                .startInclusive(0)
                .endInclusive(5000)
                .build());
        tasks.add(ApprovalTask.builder()
                .name("precondition1")
                .label("Pre Condition 1")
                .taskCategory(TaskCategory.PRE_CONDITION)
                .categoryFor("Approval1")
                .assignedTo("user1@releaseowl.com")
                .assignID("2334344")
                .assignType(AssignType.USER)
                .waitForMessage("preConditionMsg")
                .build());
        tasks.add(JenkinsJobTask.builder()
                .name("QADeploy")
                .label("Deploy to QA")
                .waitForMessage("deployResponse")
                .cfCredentialsID("334343e342sdfsdfsf2343")
                .build());
        tasks.add(ApprovalTask.builder()
                .name("postCondition1")
                .label("Post Condition 1")
                .taskCategory(TaskCategory.POST_CONDITION)
                .categoryFor("Approval2")
                .assignedTo("user2@releaseowl.com")
                .assignID("343453")
                .assignType(AssignType.USER)
                .waitForMessage("postConditionMsg")
                .build());
        tasks.add(PrintTask.builder()
                .name("Stage4")
                .label("Stage 4")
                .text("After Approval test print ${name1} with taskOutput Details ${approval1.msg} ==== ${approval1.subObj.k2}")
                .build());

        workflow.setTasks(tasks);
        return workflow;
    }

}
