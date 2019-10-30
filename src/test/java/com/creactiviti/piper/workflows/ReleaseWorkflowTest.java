package com.creactiviti.piper.workflows;

import com.creactiviti.piper.workflows.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class ReleaseWorkflowTest {

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
        ReleaseWorkflow workflow = getReleaseWorkflow();

        log.info("\n Yaml: \n" + yamlObjectMapper.writeValueAsString(workflow));
    }

    @Test
    void loadYamlString() throws IOException {
        ReleaseWorkflow workflow = yamlObjectMapper.readValue(yamlString, ReleaseWorkflow.class);
        assertNotNull(workflow);
        log.info("release Workflow Object: {}", workflow.toString());
    }

    @Test
    void getWorkflowJson() throws JsonProcessingException {
        ReleaseWorkflow workflow = getReleaseWorkflow();
        log.info("\n JSON: \n {}", jsonObjectMapper.writeValueAsString(workflow));
    }

    private ReleaseWorkflow getReleaseWorkflow() {
        ReleaseWorkflow workflow = new ReleaseWorkflow();
        workflow.setLabel("jsonToYaml");
        WorkflowIO input = WorkflowIO.builder()
                .variableName("name1")
                .label("Release Workflow Label:")
                .variableType("String")
                .required(true)
                .build();
        WorkflowIO output = WorkflowIO.builder()
                .variableName("magicNumer")
                .value("${randomNumber}")
                .build();

        List<WorkflowTask> tasks = new ArrayList<>();
        tasks.add(RandomInt.builder()
                .name("randomNumber")
                .label("RandomInt Stage 1")
                .startInclusive(0)
                .endInclusive(5000)
                .build());
        tasks.add(PrintTask.builder()
                .name("Stage2")
                .label("Stage 2")
                .text("Before Approval test test print ${name1}")
                .build());
        tasks.add(ApprovalTask.builder()
                .name("Approval1")
                .label("Approval1")
                .waitForMessage("humanResponse")
                .build());
        tasks.add(PrintTask.builder()
                .name("Stage4")
                .label("Stage 4")
                .text("After Approval test print ${name1} with taskOutput Details ${approval1.msg} ==== ${approval1.subObj.k2}")
                .build());

        workflow.setInputs(Arrays.asList(input));
        workflow.setOutputs(Arrays.asList(output));
        workflow.setTasks(tasks);
        return workflow;
    }

}
