package bbb.bbb.controller;

import bbb.bbb.dto.submission.SubmissionResponse;
import bbb.bbb.service.SubmissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @GetMapping
    public Page<SubmissionResponse> getSubmissions(@RequestParam(required = false) Long formId, Pageable pageable) {
        return submissionService.getSubmissions(formId, pageable);
    }
}
