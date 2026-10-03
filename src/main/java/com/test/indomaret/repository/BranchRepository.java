package com.test.indomaret.repository;

import com.test.indomaret.entity.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    @Query("SELECT b FROM Branch b JOIN FETCH b.province p " +
           "WHERE b.id = :id AND b.isActive = true AND b.isDeleted = false")
    Optional<Branch> findActiveByIdWithProvince(@Param("id") Long id);

    Optional<Branch> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    Optional<Branch> findByCodeIgnoreCaseAndIsActiveTrueAndIsDeletedFalse(String code);

    @Query(value = "SELECT b FROM Branch b JOIN FETCH b.province p " +
                   "WHERE b.isActive = true AND b.isDeleted = false",
           countQuery = "SELECT count(b) FROM Branch b " +
                        "WHERE b.isActive = true AND b.isDeleted = false")
    Page<Branch> findAllActiveWithProvince(Pageable pageable);

    @Query(value = "SELECT b FROM Branch b JOIN FETCH b.province p " +
                   "WHERE b.province.id = :provinceId AND b.isActive = true AND b.isDeleted = false",
           countQuery = "SELECT count(b) FROM Branch b " +
                        "WHERE b.province.id = :provinceId AND b.isActive = true AND b.isDeleted = false")
    Page<Branch> findAllByProvinceIdActive(@Param("provinceId") Long provinceId, Pageable pageable);

    boolean existsByCodeIgnoreCaseAndIsDeletedFalse(String code);

    boolean existsByCodeIgnoreCaseAndIdNotAndIsDeletedFalse(String code, Long id);
}
