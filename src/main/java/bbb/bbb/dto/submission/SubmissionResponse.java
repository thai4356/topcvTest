package bbb.bbb.dto.submission;

import java.time.Instant;
import java.util.List;

public record SubmissionResponse(
        Long id,
        Long formId,
        String formTitle,
        String submittedBy,
        Instant createdAt,
        List<SubmissionValueResponse> values) {
}
