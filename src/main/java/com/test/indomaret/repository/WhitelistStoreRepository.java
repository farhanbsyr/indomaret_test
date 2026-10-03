package com.test.indomaret.repository;

import com.test.indomaret.entity.WhitelistStore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WhitelistStoreRepository extends JpaRepository<WhitelistStore, Long> {

    @Query("SELECT ws FROM WhitelistStore ws " +
           "JOIN FETCH ws.store s " +
           "JOIN FETCH s.branch b " +
           "JOIN FETCH b.province p " +
           "WHERE ws.id = :id AND ws.isActive = true AND ws.isDeleted = false")
    Optional<WhitelistStore> findActiveByIdWithStore(@Param("id") Long id);

    Optional<WhitelistStore> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @Query("SELECT ws FROM WhitelistStore ws " +
           "WHERE ws.store.id = :storeId AND ws.isActive = true AND ws.isDeleted = false")
    Optional<WhitelistStore> findActiveByStoreId(@Param("storeId") Long storeId);

    Optional<WhitelistStore> findByStoreId(Long storeId);

    @Query(
        value = "SELECT ws FROM WhitelistStore ws " +
                "JOIN FETCH ws.store s " +
                "JOIN FETCH s.branch b " +
                "JOIN FETCH b.province p " +
                "WHERE ws.isActive = true AND ws.isDeleted = false " +
                "AND s.isActive = true AND s.isDeleted = false",
        countQuery = "SELECT count(ws) FROM WhitelistStore ws " +
                     "WHERE ws.isActive = true AND ws.isDeleted = false"
    )
    Page<WhitelistStore> findAllActiveWithStore(Pageable pageable);

    @Query("SELECT ws.store.id FROM WhitelistStore ws WHERE ws.isActive = true AND ws.isDeleted = false")
    List<Long> findActiveWhitelistStoreIds();

    boolean existsByStoreIdAndIsDeletedFalse(Long storeId);

    boolean existsByStoreIdAndIdNotAndIsDeletedFalse(Long storeId, Long id);
}
