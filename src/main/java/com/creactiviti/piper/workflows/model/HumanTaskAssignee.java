package com.creactiviti.piper.workflows.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "humantaskassignee")
@Access(value= AccessType.FIELD)
public class HumanTaskAssignee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long humanTaskId;
    @Column(name = "taskinstanceid")
    private String taskInstanceId;
    @Column(name = "assigneeid")
    private String assigneeId;
    @Column(name = "assigneetype")
    private String assigneeType;
    @Column(name = "assigneename")
    private String assigneeName;
    @Column(name = "businesslogicid")
    private String businessLogicID;
    @Column(name = "assigndate")
    private Date assignDate;
    @Column(name = "releaseworkflow")
    private String releaseWorkflow;
    @Column(name = "releasecyclename")
    private String releaseCycleName;

    @JsonInclude
    @Transient
    private WorkflowTask humanTask;

}
