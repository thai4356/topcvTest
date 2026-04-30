package bbb.bbb.dto.form;

import bbb.bbb.entity.enums.FieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FormFieldUpsertRequest(
        @NotBlank(message = "label is required") String label,
        @NotNull(message = "type is required") FieldType type,
        @NotNull(message = "displayOrder is required") Integer displayOrder,
        @NotNull(message = "required is required") Boolean required,
        String optionsJson) {
}
