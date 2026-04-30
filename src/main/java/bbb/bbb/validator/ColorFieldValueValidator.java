package bbb.bbb.validator;

import bbb.bbb.entity.FormField;
import bbb.bbb.entity.enums.FieldType;
import bbb.bbb.exception.FieldValidationException;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class ColorFieldValueValidator implements FieldValueValidator {

    private static final Pattern HEX_COLOR = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    @Override
    public FieldType supports() {
        return FieldType.COLOR;
    }

    @Override
    public String validate(FormField field, Object value) {
        String color = String.valueOf(value).trim();
        if (!HEX_COLOR.matcher(color).matches()) {
            throw new FieldValidationException(field.getLabel(), "must match HEX format (#RRGGBB)");
        }
        return color.toUpperCase();
    }
}
