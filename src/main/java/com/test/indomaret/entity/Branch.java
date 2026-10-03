package com.test.indomaret.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "branches", indexes = {
    @Index(name = "idx_branch_name", columnList = "name"),
    @Index(name = "idx_branch_code", columnList = "code", unique = true),
    @Index(name = "idx_branch_province_id", columnList = "province_id"),
    @Index(name = "idx_branch_active_deleted", columnList = "is_active, is_deleted"),
    @Index(name = "idx_branch_prov_active_del", columnList = "province_id, is_active, is_deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Branch extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "address", length = 500)
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id", nullable = false)
    private Province province;

    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Store> stores = new ArrayList<>();
}
