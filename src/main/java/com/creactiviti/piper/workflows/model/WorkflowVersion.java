package com.creactiviti.piper.workflows.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "pipelineversions")
@Access(value= AccessType.FIELD)
public class WorkflowVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "versionid")
    private long versionID;
    @Column(name = "workflowid")
    private long workflowID;
    @Column(name = "lastmodified")
    private Date lastModified;
    @Column(name = "lastmodifiedby")
    private String lastModifiedBy;
    @Column(name="script")
    private String workflow;
    @Column(name="buildinputjson")
    private String buildInputJson;
}
