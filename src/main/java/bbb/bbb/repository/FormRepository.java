package bbb.bbb.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import bbb.bbb.entity.Form;
import bbb.bbb.entity.enums.FormStatus;

public interface FormRepository extends JpaRepository<Form, Long> {

    Page<Form> findAllByOrderByDisplayOrderAscIdAsc(Pageable pageable);

    List<Form> findAllByStatusOrderByDisplayOrderAscIdAsc(FormStatus status);

    java.util.Optional<Form> findByDisplayOrder(Integer displayOrder);
}
