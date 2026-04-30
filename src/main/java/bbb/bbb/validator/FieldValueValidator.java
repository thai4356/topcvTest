package bbb.bbb.validator;

import bbb.bbb.entity.FormField;
import bbb.bbb.entity.enums.FieldType;

public interface FieldValueValidator {

    FieldType supports();

    String validate(FormField field, Object value);
}
