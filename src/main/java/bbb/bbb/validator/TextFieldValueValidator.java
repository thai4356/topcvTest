package bbb.bbb.validator;

import bbb.bbb.entity.FormField;
import bbb.bbb.entity.enums.FieldType;
import bbb.bbb.exception.FieldValidationException;
import org.springframework.stereotype.Component;

@Component
public class TextFieldValueValidator implements FieldValueValidator {

    @Override
    public FieldType supports() {
        return FieldType.TEXT;
    }

    @Override
    public String validate(FormField field, Object value) {
        String text = String.valueOf(value);
        if (text.length() > 200) {
            throw new FieldValidationException(field.getLabel(), "must not exceed 200 characters");
        }
        return text;
    }
}
