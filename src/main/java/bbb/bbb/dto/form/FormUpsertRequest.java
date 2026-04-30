package bbb.bbb.dto.form;

import bbb.bbb.entity.enums.FormStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record FormUpsertRequest(
        @NotBlank(message = "title is required") String title,
        String description,
        @NotNull(message = "displayOrder is required") Integer displayOrder,
        @NotNull(message = "status is required") FormStatus status,
        @Valid List<FormFieldUpsertRequest> fields) {
}
