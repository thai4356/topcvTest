package bbb.bbb.validator;

import bbb.bbb.entity.FormField;
import bbb.bbb.entity.enums.FieldType;
import bbb.bbb.exception.FieldValidationException;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class DateFieldValueValidator implements FieldValueValidator {

    @Override
    public FieldType supports() {
        return FieldType.DATE;
    }

    @Override
    public String validate(FormField field, Object value) {
        try {
            LocalDate date = LocalDate.parse(String.valueOf(value).trim());
            if (date.isBefore(LocalDate.now())) {
                throw new FieldValidationException(field.getLabel(), "must not be in the past");
            }
            return date.toString();
        } catch (FieldValidationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new FieldValidationException(field.getLabel(), "must be a valid ISO date (yyyy-MM-dd)");
        }
    }
}
