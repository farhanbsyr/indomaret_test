package com.test.indomaret.repository;

import com.test.indomaret.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    @Query("SELECT s FROM Store s " +
           "JOIN FETCH s.branch b " +
           "JOIN FETCH b.province p " +
           "WHERE s.id = :id AND s.isActive = true AND s.isDeleted = false")
    Optional<Store> findActiveByIdWithHierarchy(@Param("id") Long id);

    Optional<Store> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    Optional<Store> findByCodeIgnoreCaseAndIsActiveTrueAndIsDeletedFalse(String code);

    boolean existsByCodeIgnoreCaseAndIsDeletedFalse(String code);

    boolean existsByCodeIgnoreCaseAndIdNotAndIsDeletedFalse(String code, Long id);

    @Query(
        value = "SELECT DISTINCT s FROM Store s " +
                "JOIN FETCH s.branch b " +
                "JOIN FETCH b.province p " +
                "WHERE s.isActive = true AND s.isDeleted = false " +
                "AND b.isActive = true AND b.isDeleted = false " +
                "AND p.isActive = true AND p.isDeleted = false " +
                "AND (" +
                "   (:provinceName IS NOT NULL AND :provinceName != '' AND LOWER(p.name) LIKE LOWER(CONCAT('%', :provinceName, '%'))) " +
                "   OR (:includeWhitelist = true AND s.id IN (" +
                "       SELECT ws.store.id FROM WhitelistStore ws WHERE ws.isActive = true AND ws.isDeleted = false" +
                "   )) " +
                "   OR ((:provinceName IS NULL OR :provinceName = '') AND :includeWhitelist = false)" +
                ")",
        countQuery = "SELECT count(DISTINCT s) FROM Store s " +
                     "JOIN s.branch b " +
                     "JOIN b.province p " +
                     "WHERE s.isActive = true AND s.isDeleted = false " +
                     "AND b.isActive = true AND b.isDeleted = false " +
                     "AND p.isActive = true AND p.isDeleted = false " +
                     "AND (" +
                     "   (:provinceName IS NOT NULL AND :provinceName != '' AND LOWER(p.name) LIKE LOWER(CONCAT('%', :provinceName, '%'))) " +
                     "   OR (:includeWhitelist = true AND s.id IN (" +
                     "       SELECT ws.store.id FROM WhitelistStore ws WHERE ws.isActive = true AND ws.isDeleted = false" +
                     "   )) " +
                     "   OR ((:provinceName IS NULL OR :provinceName = '') AND :includeWhitelist = false)" +
                     ")"
    )
    Page<Store> searchStoresByProvince(
        @Param("provinceName") String provinceName,
        @Param("includeWhitelist") boolean includeWhitelist,
        Pageable pageable
    );

    @Query(
        value = "SELECT s FROM Store s " +
                "JOIN FETCH s.branch b " +
                "JOIN FETCH b.province p " +
                "WHERE s.isActive = true AND s.isDeleted = false " +
                "AND b.isActive = true AND b.isDeleted = false " +
                "AND p.isActive = true AND p.isDeleted = false",
        countQuery = "SELECT count(s) FROM Store s " +
                     "WHERE s.isActive = true AND s.isDeleted = false"
    )
    Page<Store> findAllActiveWithHierarchy(Pageable pageable);
}
