package bbb.bbb.dto.submission;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record SubmitFormRequest(
        @NotNull(message = "values is required") Map<String, Object> values) {
}
