package bbb.bbb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import bbb.bbb.entity.FormField;

public interface FormFieldRepository extends JpaRepository<FormField, Long> {

    List<FormField> findAllByFormIdOrderByDisplayOrderAscIdAsc(Long formId);

    Optional<FormField> findByIdAndFormId(Long id, Long formId);

    Optional<FormField> findByFormIdAndDisplayOrder(Long formId, Integer displayOrder);
}
