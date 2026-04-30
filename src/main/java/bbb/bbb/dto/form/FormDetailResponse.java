package bbb.bbb.dto.form;

import bbb.bbb.entity.enums.FormStatus;
import java.time.Instant;
import java.util.List;

public record FormDetailResponse(
        Long id,
        String title,
        String description,
        Integer displayOrder,
        FormStatus status,
        Instant createdAt,
        Instant updatedAt,
        int fieldCount,
        List<FormFieldResponse> fields) {
}
