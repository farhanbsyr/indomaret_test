package com.test.indomaret.repository;

import com.test.indomaret.entity.Province;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Long> {

    Optional<Province> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    Optional<Province> findByCodeIgnoreCaseAndIsActiveTrueAndIsDeletedFalse(String code);

    Optional<Province> findByNameIgnoreCaseAndIsActiveTrueAndIsDeletedFalse(String name);

    Page<Province> findAllByIsActiveTrueAndIsDeletedFalse(Pageable pageable);

    boolean existsByCodeIgnoreCaseAndIsDeletedFalse(String code);

    boolean existsByNameIgnoreCaseAndIsDeletedFalse(String name);
}
