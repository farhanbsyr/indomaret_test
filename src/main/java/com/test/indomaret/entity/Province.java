package com.test.indomaret.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "provinces", indexes = {
    @Index(name = "idx_province_name", columnList = "name"),
    @Index(name = "idx_province_code", columnList = "code", unique = true),
    @Index(name = "idx_province_active_deleted", columnList = "is_active, is_deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Province extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @OneToMany(mappedBy = "province", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Branch> branches = new ArrayList<>();
}
