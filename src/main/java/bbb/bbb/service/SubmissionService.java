package bbb.bbb.service;

import bbb.bbb.dto.submission.SubmitFormRequest;
import bbb.bbb.dto.submission.SubmissionResponse;
import bbb.bbb.dto.submission.SubmissionValueResponse;
import bbb.bbb.entity.Form;
import bbb.bbb.entity.FormField;
import bbb.bbb.entity.Submission;
import bbb.bbb.entity.SubmissionValue;
import bbb.bbb.entity.enums.FormStatus;
import bbb.bbb.exception.BadRequestException;
import bbb.bbb.exception.ResourceNotFoundException;
import bbb.bbb.repository.FormFieldRepository;
import bbb.bbb.repository.FormRepository;
import bbb.bbb.repository.SubmissionRepository;
import bbb.bbb.validator.FormSubmissionValidator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmissionService {

    private final FormRepository formRepository;
    private final FormFieldRepository formFieldRepository;
    private final SubmissionRepository submissionRepository;
    private final FormSubmissionValidator submissionValidator;

    public SubmissionService(FormRepository formRepository,
                             FormFieldRepository formFieldRepository,
                             SubmissionRepository submissionRepository,
                             FormSubmissionValidator submissionValidator) {
        this.formRepository = formRepository;
        this.formFieldRepository = formFieldRepository;
        this.submissionRepository = submissionRepository;
        this.submissionValidator = submissionValidator;
    }

    @Transactional
    public SubmissionResponse submitForm(Long formId, SubmitFormRequest request) {
        Form form = loadForm(formId);
        if (form.getStatus() != FormStatus.ACTIVE) {
            throw new BadRequestException("Only ACTIVE forms can be submitted");
        }

        List<FormField> fields = formFieldRepository.findAllByFormIdOrderByDisplayOrderAscIdAsc(formId);
        Map<String, Object> submittedValues = new LinkedHashMap<>(request.values());
        Submission submission = new Submission();
        submission.setForm(form);

        List<SubmissionValue> values = new ArrayList<>();
        for (FormField field : fields) {
            Object rawValue = submittedValues.remove(String.valueOf(field.getId()));
            String normalizedValue = submissionValidator.validate(field, rawValue);
            SubmissionValue submissionValue = new SubmissionValue();
            submissionValue.setSubmission(submission);
            submissionValue.setField(field);
            submissionValue.setValue(normalizedValue);
            values.add(submissionValue);
        }

        if (!submittedValues.isEmpty()) {
            String unknownField = submittedValues.keySet().iterator().next();
            throw new BadRequestException("Unknown field id in submission values: " + unknownField);
        }

        submission.setValues(values);
        Submission saved = submissionRepository.save(submission);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<SubmissionResponse> getSubmissions(Long formId, Pageable pageable) {
        Page<Submission> page = formId == null
                ? submissionRepository.findAllByOrderByCreatedAtDesc(pageable)
                : submissionRepository.findAllByFormIdOrderByCreatedAtDesc(formId, pageable);
        return page.map(this::toResponse);
    }

    private Form loadForm(Long id) {
        return formRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Form not found: " + id));
    }

    private SubmissionResponse toResponse(Submission submission) {
        List<SubmissionValueResponse> values = submission.getValues().stream()
                .sorted(Comparator.comparing(value -> value.getField().getDisplayOrder()))
                .map(value -> new SubmissionValueResponse(
                        value.getField().getId(),
                        value.getField().getLabel(),
                        value.getField().getType(),
                        value.getValue()))
                .toList();

        return new SubmissionResponse(
                submission.getId(),
                submission.getForm().getId(),
                submission.getForm().getTitle(),
                submission.getCreatedAt(),
                values);
    }
}
