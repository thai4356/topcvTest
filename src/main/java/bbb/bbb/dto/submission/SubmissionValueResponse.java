package bbb.bbb.dto.submission;

import bbb.bbb.entity.enums.FieldType;

public record SubmissionValueResponse(
        Long fieldId,
        String fieldLabel,
        FieldType fieldType,
        String value) {
}
