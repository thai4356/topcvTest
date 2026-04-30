package bbb.bbb.validator;

import bbb.bbb.entity.FormField;
import bbb.bbb.entity.enums.FieldType;
import bbb.bbb.exception.FieldValidationException;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class NumberFieldValueValidator implements FieldValueValidator {

    @Override
    public FieldType supports() {
        return FieldType.NUMBER;
    }

    @Override
    public String validate(FormField field, Object value) {
        try {
            BigDecimal number = new BigDecimal(String.valueOf(value).trim());
            if (number.compareTo(BigDecimal.ZERO) < 0 || number.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new FieldValidationException(field.getLabel(), "must be between 0 and 100");
            }
            return number.stripTrailingZeros().toPlainString();
        } catch (NumberFormatException exception) {
            throw new FieldValidationException(field.getLabel(), "must be a valid number");
        }
    }
}
