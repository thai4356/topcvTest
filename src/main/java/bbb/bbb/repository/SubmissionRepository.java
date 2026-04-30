package bbb.bbb.repository;

import bbb.bbb.entity.Submission;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    Page<Submission> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Submission> findAllByFormIdOrderByCreatedAtDesc(Long formId, Pageable pageable);

    Optional<Submission> findByIdAndFormId(Long id, Long formId);
}
