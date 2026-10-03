package com.test.indomaret.repository;

import com.test.indomaret.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    Page<AuditLog> findByEntityNameOrderByTimestampDesc(String entityName, Pageable pageable);

    Page<AuditLog> findByPerformedByOrderByTimestampDesc(String performedBy, Pageable pageable);
}
