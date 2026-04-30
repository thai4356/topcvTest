package bbb.bbb.dto.submission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record SubmitFormRequest(
        @NotBlank(message = "submittedBy is required") String submittedBy,
        @NotNull(message = "values is required") Map<String, Object> values) {
}
