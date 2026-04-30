package bbb.bbb.repository;

import bbb.bbb.entity.SubmissionValue;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionValueRepository extends JpaRepository<SubmissionValue, Long> {

    List<SubmissionValue> findAllBySubmissionId(Long submissionId);
}
