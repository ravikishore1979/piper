package com.creactiviti.piper.workflows.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Entity
@Getter
@Setter
@Table(name = "pipelines")
@Access(value= AccessType.FIELD)
public class Workflow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="workflowid")
    private Long id;
    @Column(name="customerid")
    private String customerId;
    @Column(name="projectid")
    private String projectId;
    @Column(name="workflowname")
    private String name;
    @Column(name="script")
    private String workflow;
}

