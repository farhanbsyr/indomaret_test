package com.test.indomaret.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stores", indexes = {
    @Index(name = "idx_store_name", columnList = "name"),
    @Index(name = "idx_store_code", columnList = "code", unique = true),
    @Index(name = "idx_store_branch_id", columnList = "branch_id"),
    @Index(name = "idx_store_active_deleted", columnList = "is_active, is_deleted"),
    @Index(name = "idx_store_branch_act_del", columnList = "branch_id, is_active, is_deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "address", length = 500)
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
}
