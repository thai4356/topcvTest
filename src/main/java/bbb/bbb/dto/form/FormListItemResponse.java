package bbb.bbb.dto.form;

import bbb.bbb.entity.enums.FormStatus;
import java.time.Instant;

public record FormListItemResponse(
        Long id,
        String title,
        String description,
        Integer displayOrder,
        FormStatus status,
        Instant createdAt,
        Instant updatedAt,
        int fieldCount) {
}
