package bbb.bbb.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bbb.bbb.dto.form.FormDetailResponse;
import bbb.bbb.dto.form.FormFieldResponse;
import bbb.bbb.dto.form.FormFieldUpsertRequest;
import bbb.bbb.dto.form.FormListItemResponse;
import bbb.bbb.dto.form.FormUpsertRequest;
import bbb.bbb.entity.Form;
import bbb.bbb.entity.FormField;
import bbb.bbb.entity.enums.FormStatus;
import bbb.bbb.exception.FieldValidationException;
import bbb.bbb.exception.ResourceNotFoundException;
import bbb.bbb.repository.FormFieldRepository;
import bbb.bbb.repository.FormRepository;

@Service
public class FormService {

    private final FormRepository formRepository;
    private final FormFieldRepository formFieldRepository;

    public FormService(FormRepository formRepository, FormFieldRepository formFieldRepository) {
        this.formRepository = formRepository;
        this.formFieldRepository = formFieldRepository;
    }

    @Transactional(readOnly = true)
    public Page<FormListItemResponse> getForms(Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Order.asc("displayOrder"), Sort.Order.asc("id")));
        return formRepository.findAll(sortedPageable)
                .map(form -> new FormListItemResponse(
                        form.getId(),
                        form.getTitle(),
                        form.getDescription(),
                        form.getDisplayOrder(),
                        form.getStatus(),
                        form.getCreatedAt(),
                        form.getUpdatedAt(),
                        form.getFields() == null ? 0 : form.getFields().size()));
    }

    @Transactional(readOnly = true)
    public List<FormDetailResponse> getActiveForms() {
        List<Form> forms = formRepository.findAllByStatusOrderByDisplayOrderAscIdAsc(FormStatus.ACTIVE);
        if (forms.isEmpty()) {
            return List.of();
        }

        List<Long> formIds = forms.stream().map(Form::getId).toList();
        List<FormField> allFields = formFieldRepository.findAllByFormIdInOrderByFormIdAscDisplayOrderAscIdAsc(formIds);
        Map<Long, List<FormField>> fieldsByFormId = new LinkedHashMap<>();
        for (FormField field : allFields) {
            Long formId = field.getForm() == null ? null : field.getForm().getId();
            if (formId == null) {
                continue;
            }
            fieldsByFormId.computeIfAbsent(formId, ignored -> new ArrayList<>()).add(field);
        }

        return forms.stream()
                .map(form -> toDetailResponse(form, fieldsByFormId.getOrDefault(form.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public FormDetailResponse getForm(Long id) {
        Form form = loadForm(id);
        List<FormField> fields = formFieldRepository.findAllByFormIdOrderByDisplayOrderAscIdAsc(id);
        return toDetailResponse(form, fields);
    }

    @Transactional
    public FormDetailResponse createForm(FormUpsertRequest request) {
        Form form = new Form();
        applyRequest(form, request);
        // ensure displayOrder is unique across forms
        if (request.displayOrder() != null) {
            try {
                var existingOpt = formRepository.findByDisplayOrder(request.displayOrder());
                if (existingOpt.isPresent()) {
                    throw new FieldValidationException("displayOrder", "display order has been taken please choose other");
                }
            } catch (Exception ex) {
                throw new FieldValidationException("displayOrder", "display order has been taken please choose other");
            }
        }
        Form saved = formRepository.save(form);
        List<FormField> fields = formFieldRepository.findAllByFormIdOrderByDisplayOrderAscIdAsc(saved.getId());
        return toDetailResponse(saved, fields);
    }

    @Transactional
    public FormDetailResponse updateForm(Long id, FormUpsertRequest request) {
        Form form = loadForm(id);
        applyRequest(form, request);
        // ensure displayOrder is unique across forms (allow keeping same id)
        if (request.displayOrder() != null) {
            try {
                var existingOpt = formRepository.findByDisplayOrder(request.displayOrder());
                if (existingOpt.isPresent() && !existingOpt.get().getId().equals(id)) {
                    throw new FieldValidationException("displayOrder", "display order has been taken please choose other");
                }
            } catch (Exception ex) {
                throw new FieldValidationException("displayOrder", "display order has been taken please choose other");
            }
        }
        Form saved = formRepository.save(form);
        List<FormField> fields = formFieldRepository.findAllByFormIdOrderByDisplayOrderAscIdAsc(id);
        return toDetailResponse(saved, fields);
    }

    @Transactional
    public void deleteForm(Long id) {
        Form form = loadForm(id);
        formRepository.delete(form);
    }

    private void applyRequest(Form form, FormUpsertRequest request) {
        form.setTitle(request.title().trim());
        form.setDescription(normalizeText(request.description()));
        form.setDisplayOrder(request.displayOrder());
        form.setStatus(request.status());
        if (request.fields() != null) {
            // validate unique displayOrder among provided fields
            Set<Integer> seen = new HashSet<>();
            int idx = 0;
            for (FormFieldUpsertRequest fieldReq : request.fields()) {
                Integer ord = fieldReq.displayOrder();
                if (ord == null) {
                    throw new FieldValidationException("fields[" + idx + "].displayOrder", "displayOrder is required");
                }
                if (!seen.add(ord)) {
                    throw new FieldValidationException("fields[" + idx + "].displayOrder", "displayOrder must be unique");
                }
                idx++;
            }

            form.getFields().clear();
            for (FormFieldUpsertRequest fieldReq : request.fields()) {
                FormField field = new FormField();
                field.setForm(form);
                field.setLabel(fieldReq.label().trim());
                field.setType(fieldReq.type());
                field.setDisplayOrder(fieldReq.displayOrder());
                field.setRequired(fieldReq.required());
                field.setOptionsJson(normalizeText(fieldReq.optionsJson()));
                form.getFields().add(field);
            }
        }
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Form loadForm(Long id) {
        return formRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Form not found: " + id));
    }

    private FormDetailResponse toDetailResponse(Form form, List<FormField> fields) {
        List<FormFieldResponse> fieldResponses = fields.stream().map(this::toFieldResponse).toList();
        return new FormDetailResponse(
                form.getId(),
                form.getTitle(),
                form.getDescription(),
                form.getDisplayOrder(),
                form.getStatus(),
                form.getCreatedAt(),
                form.getUpdatedAt(),
                fieldResponses.size(),
                fieldResponses);
    }

    private FormFieldResponse toFieldResponse(FormField field) {
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
