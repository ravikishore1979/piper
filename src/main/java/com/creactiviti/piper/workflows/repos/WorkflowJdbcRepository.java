package com.creactiviti.piper.workflows.repos;

import com.creactiviti.piper.workflows.model.ReleasePipelineBuildInput;
import com.creactiviti.piper.workflows.model.WorkflowWithInput;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Slf4j
public class WorkflowJdbcRepository {

    private NamedParameterJdbcOperations jdbc;
    @Autowired
    ObjectMapper jsonMapper;

    public List<WorkflowWithInput> findAllByCustomerIdAndProjectId(String customerID, String projectID) {
        Map<String, String> params =  new HashMap<>();
        params.put("customerID", customerID);
        params.put("projectID", projectID);
        List<WorkflowWithInput> list = jdbc.query("select p.*, pv.buildinputjson from pipelines as p join pipelineversions as pv  " +
                    "on p.workflowid = pv.workflowid and p.headrevision = versionid " +
                    "where p.customerid = :customerID and p.projectid = :projectID", params, this::workflowMapper);
        return list;
    }

    private WorkflowWithInput workflowMapper(ResultSet rs, int i) throws SQLException {
        WorkflowWithInput withInput = new WorkflowWithInput();

        withInput.setId(rs.getLong("workflowid"));
        withInput.setHeadRevision(rs.getLong("headrevision"));
        withInput.setName(rs.getString("workflowname"));
        withInput.setCustomerId(rs.getString("customerid"));
        withInput.setProjectId(rs.getString("projectid"));
        withInput.setCreatedBy(rs.getString("createdby"));
        withInput.setCreatedTime(rs.getTimestamp("createdtime"));
        try {
            withInput.setBuildInput(jsonMapper.readValue(rs.getString("buildinputjson"), ReleasePipelineBuildInput.class));
        } catch (IOException e) {
            log.error("Exception while parsing buildInput for WFName {}", withInput.getName(), e);
        }

        return withInput;
    }
}
