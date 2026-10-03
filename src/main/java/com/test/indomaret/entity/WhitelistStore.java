package com.test.indomaret.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "whitelist_stores", indexes = {
    @Index(name = "idx_whitelist_store_id", columnList = "store_id", unique = true),
    @Index(name = "idx_whitelist_active_deleted", columnList = "is_active, is_deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhitelistStore extends BaseEntity {

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "store_id", nullable = false, unique = true)
    private Store store;

    @Column(name = "reason", length = 255)
    private String reason;
}
