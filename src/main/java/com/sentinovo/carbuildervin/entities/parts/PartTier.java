package com.sentinovo.carbuildervin.entities.parts;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "part_tier")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartTier {

    @Id
    @Size(max = 50, message = "Tier code must not exceed 50 characters")
    @Column(name = "code", length = 50)
    private String code;

    @NotBlank(message = "Tier label is required")
    @Size(max = 100, message = "Tier label must not exceed 100 characters")
    @Column(name = "label", length = 100, nullable = false)
    private String label;

    @Min(value = 1, message = "Rank must be at least 1")
    @Max(value = 10, message = "Rank must not exceed 10")
    @Column(name = "rank", nullable = false)
    private Integer rank;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "partTier", fetch = FetchType.LAZY)
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Part> parts = new HashSet<>();

    @OneToMany(mappedBy = "partTier", fetch = FetchType.LAZY)
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<SubPart> subParts = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartTier partTier = (PartTier) o;
        return Objects.equals(code, partTier.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "PartTier{code='" + code + "', label='" + label + "', rank=" + rank + "}";
    }
}