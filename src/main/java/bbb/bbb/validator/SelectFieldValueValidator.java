package bbb.bbb.validator;

import bbb.bbb.entity.FormField;
import bbb.bbb.entity.enums.FieldType;
import bbb.bbb.exception.FieldValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SelectFieldValueValidator implements FieldValueValidator {

    private final ObjectMapper objectMapper;

    public SelectFieldValueValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public FieldType supports() {
        return FieldType.SELECT;
    }

    @Override
    public String validate(FormField field, Object value) {
        String selectedValue = String.valueOf(value).trim();
        try {
            String optionsJson = field.getOptionsJson();
            if (optionsJson == null || optionsJson.isBlank()) {
                throw new FieldValidationException(field.getLabel(), "has no selectable options configured");
            }
            List<String> options = objectMapper.readValue(optionsJson, new TypeReference<List<String>>() {
            });
            if (!options.contains(selectedValue)) {
                throw new FieldValidationException(field.getLabel(), "must be one of the configured options");
            }
            return selectedValue;
        } catch (FieldValidationException exception) {
            throw exception;
        } catch (JsonProcessingException exception) {
            throw new FieldValidationException(field.getLabel(), "has invalid options configuration");
        }
    }
}
