package com.creactiviti.piper.workflows.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "pipelineVersions")
@Access(value= AccessType.FIELD)
public class WorkflowVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long versionID;
    private long workflowID;
    private Date lastModified;
    private String lastModifiedBy;
    @Column(name="script")
    private String workflow;
}
