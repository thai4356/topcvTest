package bbb.bbb.dto.form;

import bbb.bbb.entity.enums.FieldType;
import java.time.Instant;

public record FormFieldResponse(
        Long id,
        Long formId,
        String label,
        FieldType type,
        Integer displayOrder,
        Boolean required,
        String optionsJson,
        Instant createdAt,
        Instant updatedAt) {
}
