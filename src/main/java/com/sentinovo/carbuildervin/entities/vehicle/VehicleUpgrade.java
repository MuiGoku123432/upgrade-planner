package com.sentinovo.carbuildervin.entities.vehicle;

import com.sentinovo.carbuildervin.entities.BaseEntity;
import com.sentinovo.carbuildervin.entities.parts.Part;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "vehicle_upgrade")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleUpgrade extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "upgrade_category_id", nullable = false)
    private UpgradeCategory upgradeCategory;

    @NotBlank(message = "Upgrade name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Size(max = 150, message = "Slug must not exceed 150 characters")
    @Column(name = "slug", length = 150)
    private String slug;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Min(value = 1, message = "Priority level must be between 1 and 10")
    @Max(value = 10, message = "Priority level must be between 1 and 10")
    @Builder.Default
    @Column(name = "priority_level")
    private Integer priorityLevel = 1;

    @Column(name = "target_completion_date")
    private LocalDate targetCompletionDate;

    @Size(max = 30, message = "Status must not exceed 30 characters")
    @Builder.Default
    @Column(name = "status", length = 30)
    private String status = "PLANNED";

    @Builder.Default
    @Column(name = "is_primary_for_category")
    private Boolean isPrimaryForCategory = false;

    @OneToMany(mappedBy = "vehicleUpgrade", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Part> parts = new HashSet<>();

    public void addPart(Part part) {
        parts.add(part);
        part.setVehicleUpgrade(this);
    }

    public void removePart(Part part) {
        parts.remove(part);
        part.setVehicleUpgrade(null);
    }

    @PrePersist
    @PreUpdate
    private void generateSlug() {
        if (slug == null || slug.trim().isEmpty()) {
            if (name != null) {
                this.slug = name.toLowerCase()
                    .replaceAll("[^a-z0-9\\s-]", "")
                    .replaceAll("\\s+", "-")
                    .replaceAll("-+", "-")
                    .replaceAll("^-|-$", "");
            }
        }
    }

    public enum Status {
        PLANNED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        ON_HOLD
    }
}