package com.kpi.web.systems.lab3.repository;

import com.kpi.web.systems.lab3.entity.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PageRepository extends JpaRepository<Page, Long> {

    Optional<Page> findByCode(String code);

    List<Page> findByParentPageCode(String code);
}
