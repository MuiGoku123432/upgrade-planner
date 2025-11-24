package com.sentinovo.carbuildervin.entities.parts;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "part_category")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartCategory {

    @Id
    @Size(max = 50, message = "Category code must not exceed 50 characters")
    @Column(name = "code", length = 50)
    private String code;

    @NotBlank(message = "Category label is required")
    @Size(max = 100, message = "Category label must not exceed 100 characters")
    @Column(name = "label", length = 100, nullable = false)
    private String label;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @OneToMany(mappedBy = "partCategory", fetch = FetchType.LAZY)
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Part> parts = new HashSet<>();

    @OneToMany(mappedBy = "partCategory", fetch = FetchType.LAZY)
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<SubPart> subParts = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartCategory that = (PartCategory) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "PartCategory{code='" + code + "', label='" + label + "'}";
    }
}