package bbb.bbb.service;

import bbb.bbb.dto.form.FormFieldResponse;
import bbb.bbb.dto.form.FormFieldUpsertRequest;
import bbb.bbb.entity.Form;
import bbb.bbb.entity.FormField;
import bbb.bbb.entity.enums.FieldType;
import bbb.bbb.exception.BadRequestException;
import bbb.bbb.exception.FieldValidationException;
import bbb.bbb.exception.ResourceNotFoundException;
import bbb.bbb.repository.FormFieldRepository;
import bbb.bbb.repository.FormRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FormFieldService {

    private final FormRepository formRepository;
    private final FormFieldRepository formFieldRepository;
    private final ObjectMapper objectMapper;

    public FormFieldService(FormRepository formRepository, FormFieldRepository formFieldRepository, ObjectMapper objectMapper) {
        this.formRepository = formRepository;
        this.formFieldRepository = formFieldRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public FormFieldResponse addField(Long formId, FormFieldUpsertRequest request) {
        Form form = loadForm(formId);
        // check for duplicate displayOrder within the same form
        if (request.displayOrder() != null) {
            formFieldRepository.findByFormIdAndDisplayOrder(formId, request.displayOrder())
                    .ifPresent(existing -> {
                        throw new FieldValidationException("displayOrder", "displayOrder must be unique within the form");
                    });
        }
        FormField field = new FormField();
        applyRequest(field, form, request);
        return toResponse(formFieldRepository.save(field));
    }

    @Transactional
    public FormFieldResponse updateField(Long formId, Long fieldId, FormFieldUpsertRequest request) {
        FormField field = loadField(formId, fieldId);
        // ensure new displayOrder doesn't conflict with other fields
        if (request.displayOrder() != null) {
            formFieldRepository.findByFormIdAndDisplayOrder(formId, request.displayOrder())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(fieldId)) {
                            throw new FieldValidationException("displayOrder", "displayOrder must be unique within the form");
                        }
                    });
        }
        applyRequest(field, field.getForm(), request);
        return toResponse(formFieldRepository.save(field));
    }

    @Transactional
    public void deleteField(Long formId, Long fieldId) {
        FormField field = loadField(formId, fieldId);
        formFieldRepository.delete(field);
    }

    private void applyRequest(FormField field, Form form, FormFieldUpsertRequest request) {
        validateFieldConfiguration(request.type(), request.optionsJson());
        field.setForm(form);
        field.setLabel(request.label().trim());
        field.setType(request.type());
        field.setDisplayOrder(request.displayOrder());
        field.setRequired(request.required());
        field.setOptionsJson(normalizeOptionsJson(request.type(), request.optionsJson()));
    }

    private void validateFieldConfiguration(FieldType type, String optionsJson) {
        if (type == FieldType.SELECT) {
            if (optionsJson == null || optionsJson.isBlank()) {
                throw new BadRequestException("optionsJson is required for SELECT fields");
            }
            try {
                List<String> options = objectMapper.readValue(optionsJson, new TypeReference<List<String>>() {
                });
                if (options.isEmpty()) {
                    throw new BadRequestException("optionsJson must contain at least one option for SELECT fields");
                }
            } catch (BadRequestException exception) {
                throw exception;
            } catch (JsonProcessingException exception) {
                throw new BadRequestException("optionsJson must be a valid JSON array of strings");
            }
        }
    }

    private String normalizeOptionsJson(FieldType type, String optionsJson) {
        if (type != FieldType.SELECT || optionsJson == null || optionsJson.isBlank()) {
            return null;
        }
        return optionsJson.trim();
    }

    private Form loadForm(Long id) {
        return formRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Form not found: " + id));
    }

    private FormField loadField(Long formId, Long fieldId) {
        return formFieldRepository.findByIdAndFormId(fieldId, formId)
                .orElseThrow(() -> new ResourceNotFoundException("Form field not found: " + fieldId));
    }

    private FormFieldResponse toResponse(FormField field) {
        return new FormFieldResponse(
                field.getId(),
                field.getForm() == null ? null : field.getForm().getId(),
                field.getLabel(),
                field.getType(),
                field.getDisplayOrder(),
                field.getRequired(),
                field.getOptionsJson(),
                field.getCreatedAt(),
                field.getUpdatedAt());
    }
}
