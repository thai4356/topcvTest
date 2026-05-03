package bbb.bbb.controller;

import bbb.bbb.dto.form.FormDetailResponse;
import bbb.bbb.dto.form.FormFieldResponse;
import bbb.bbb.dto.form.FormFieldUpsertRequest;
import bbb.bbb.dto.form.FormListItemResponse;
import bbb.bbb.dto.form.FormUpsertRequest;
import bbb.bbb.dto.submission.SubmitFormRequest;
import bbb.bbb.dto.submission.SubmissionResponse;
import bbb.bbb.service.FormFieldService;
import bbb.bbb.service.FormService;
import bbb.bbb.service.SubmissionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forms")
public class FormController {

    private final FormService formService;
    private final FormFieldService formFieldService;
    private final SubmissionService submissionService;

    public FormController(FormService formService, FormFieldService formFieldService, SubmissionService submissionService) {
        this.formService = formService;
        this.formFieldService = formFieldService;
        this.submissionService = submissionService;
    }

    @GetMapping
    public Page<FormListItemResponse> getForms(Pageable pageable) {
        return formService.getForms(pageable);
    }

    @GetMapping("/active")
    public List<FormDetailResponse> getActiveForms() {
        return formService.getActiveForms();
    }

    @PostMapping
    public ResponseEntity<FormDetailResponse> createForm(@Valid @RequestBody FormUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(formService.createForm(request));
    }

    @GetMapping("/{id}")
    public FormDetailResponse getForm(@PathVariable Long id) {
        return formService.getForm(id);
    }

    @PutMapping("/{id}")
    public FormDetailResponse updateForm(@PathVariable Long id, @Valid @RequestBody FormUpsertRequest request) {
        return formService.updateForm(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteForm(@PathVariable Long id) {
        formService.deleteForm(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{formId}/fields")
    public ResponseEntity<FormFieldResponse> addField(@PathVariable Long formId, @Valid @RequestBody FormFieldUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(formFieldService.addField(formId, request));
    }

    @PutMapping("/{formId}/fields/{fieldId}")
    public FormFieldResponse updateField(@PathVariable Long formId,
                                         @PathVariable Long fieldId,
                                         @Valid @RequestBody FormFieldUpsertRequest request) {
        return formFieldService.updateField(formId, fieldId, request);
    }

    @DeleteMapping("/{formId}/fields/{fieldId}")
    public ResponseEntity<Map<String, String>> deleteField(@PathVariable Long formId, @PathVariable Long fieldId) {
        formFieldService.deleteField(formId, fieldId);
        return ResponseEntity.ok(Map.of("message", "deleted"));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<SubmissionResponse> submitForm(@PathVariable Long id, @Valid @RequestBody SubmitFormRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(submissionService.submitForm(id, request));
    }

    @GetMapping("/{id}/submissions")
    public Page<SubmissionResponse> getFormSubmissions(@PathVariable Long id, Pageable pageable) {
        return submissionService.getSubmissions(id, pageable);
    }
}
