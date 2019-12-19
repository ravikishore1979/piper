package com.creactiviti.piper.workflows.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "humantaskaction")
@Access(value= AccessType.FIELD)
public class HumanTaskAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long taskStatusId;
    @Column(name = "humantaskid")
    private long humanTaskId;
    @Column(name = "taskinstanceid")
    private String taskInstanceId;
    @Column(name = "actionname")
    private String actionName;
    @Column(name = "actiondoneby")
    private String actionDoneBy;
    @Column(name = "actiondate")
    private Date actionDate;
    @Column(name = "errormsg")
    private String errorMsg;
}
