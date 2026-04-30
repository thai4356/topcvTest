package bbb.bbb.validator;

import bbb.bbb.entity.FormField;
import bbb.bbb.entity.enums.FieldType;
import bbb.bbb.exception.BadRequestException;
import bbb.bbb.exception.FieldValidationException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class FormSubmissionValidator {

    private final Map<FieldType, FieldValueValidator> validators = new EnumMap<>(FieldType.class);

    public FormSubmissionValidator(List<FieldValueValidator> validators) {
        for (FieldValueValidator validator : validators) {
            this.validators.put(validator.supports(), validator);
        }
    }

    public String validate(FormField field, Object rawValue) {
        if (isMissing(rawValue)) {
            if (Boolean.TRUE.equals(field.getRequired())) {
                throw new FieldValidationException(field.getLabel(), "is required");
            }
            return "";
        }

        FieldValueValidator validator = validators.get(field.getType());
        if (validator == null) {
            throw new BadRequestException("Unsupported field type: " + field.getType());
        }

        return validator.validate(field, rawValue);
    }

    private boolean isMissing(Object rawValue) {
        if (rawValue == null) {
            return true;
        }
        if (rawValue instanceof String text) {
            return text.trim().isEmpty();
        }
        return false;
    }
}
