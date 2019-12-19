package com.creactiviti.piper.workflows.repos;

import com.creactiviti.piper.workflows.model.HumanTaskAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IHumanTaskActionRepository extends JpaRepository<HumanTaskAction, Long> {
}
