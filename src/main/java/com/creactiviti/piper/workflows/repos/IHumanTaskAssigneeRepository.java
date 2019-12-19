package com.creactiviti.piper.workflows.repos;

import com.creactiviti.piper.workflows.model.HumanTaskAssignee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IHumanTaskAssigneeRepository extends JpaRepository<HumanTaskAssignee, Long> {

    HumanTaskAssignee findByAssigneeIdAndTaskInstanceId(String taskInstanceId, String assigneeId);
}
