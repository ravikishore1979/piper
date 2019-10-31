package com.creactiviti.piper.workflows.repos;

import com.creactiviti.piper.core.DSL;
import com.creactiviti.piper.core.job.SimpleJob;
import com.creactiviti.piper.core.json.JsonHelper;
import com.creactiviti.piper.workflows.model.WorkflowVersion;
import com.creactiviti.piper.workflows.model.Workflow;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class WorkflowRepository implements IWorkflowRepository {

    private NamedParameterJdbcOperations jdbcOperations;

    public void setJDBC(NamedParameterJdbcOperations jdbc) {
        this.jdbcOperations = jdbc;
    }

    @Override
    public boolean existsByCustomerIdAndProjectIdAndName(String customerId, String projectId, String wfName) {
        return true;
    }

    @Override
    public Workflow findByCustomerIdAndProjectIdAndName(String customerId, String projectId, String wfName) {
//        List<Job> query = jdbcOperations.query("select * from pipelines p join pipelineVersions pv on (p.workflowid = pv.workflowID)" +
//                " where (pv.versionID = :vId and p.customerid = :cusId and p.projectid = :prId and p.workflowname = :wfName) ", Collections.singletonMap("id", aId),this::jobRowMappper);


        return null;
    }

    @Override
    public Workflow findByCustomerIdAndProjectIdAndNameAndVersion(String customerId, String projectId, String wfName, long versionId) {

        List<Workflow> query = jdbcOperations.query("select * from pipelines p join pipelineVersions pv on (p.workflowid = pv.workflowID)" +
                " where (pv.versionID = :vId ) ", Collections.singletonMap("vId", versionId), this::jobRowMappper);
        if(query.size() == 1) {
            return query.get(0);
        }
        return null;
    }

    private Workflow jobRowMappper (ResultSet aRs, int aIndex) throws SQLException {

        Workflow wf = new Workflow();
        wf.setId(aRs.getLong("workflowid"));
        wf.setCustomerId(aRs.getString("customerid"));
        wf.setProjectId(aRs.getString("projectid"));
        wf.setName(aRs.getString("workflowname"));
        wf.setCreatedBy(aRs.getString("createdBy"));
        wf.setCreatedTime(new Date(aRs.getTimestamp("createdTime").getTime()));

        WorkflowVersion pv = new WorkflowVersion();
        pv.setVersionID(aRs.getLong("versionID"));
        pv.setWorkflowID(wf.getId());
        pv.setLastModifiedBy(aRs.getString("lastModifiedBy"));
        pv.setLastModified(new Date(aRs.getTimestamp("lastModified").getTime()));

        Map<String, Object> map = new HashMap<>();
        map.put("id", aRs.getString("id"));
        map.put("status", aRs.getString("status"));
        map.put("currentTask", aRs.getInt("current_task"));
        map.put("pipelineId", aRs.getString("pipeline_id"));
        map.put("label", aRs.getString("label"));
        map.put("createTime", aRs.getTimestamp("create_time"));
        map.put("startTime", aRs.getTimestamp("start_time"));
        map.put("endTime", aRs.getTimestamp("end_time"));
        map.put("execution", getExecution(aRs.getString("id")));
        map.put("tags", aRs.getString("tags").length()>0?aRs.getString("tags").split(","):new String[0]);
        map.put("priority", aRs.getInt("priority"));
        map.put("inputs", JsonHelper.readValue(json,aRs.getString("inputs"),Map.class));
        map.put("outputs", JsonHelper.readValue(json,aRs.getString("outputs"),Map.class));
        map.put("webhooks", JsonHelper.readValue(json,aRs.getString("webhooks"),List.class));
        map.put(DSL.PARENT_TASK_EXECUTION_ID, aRs.getString("parent_task_execution_id"));
        return new SimpleJob(map);
    }


    @Override
    public Workflow findOne(Long workflowId) {
        return null;
    }

    @Override
    public Workflow save(Workflow wf) {
        return null;
    }

    @Override
    public List<Workflow> findAllByCustomerIdAndProjectId(String customerID, String projectID) {
        return null;
    }
}
