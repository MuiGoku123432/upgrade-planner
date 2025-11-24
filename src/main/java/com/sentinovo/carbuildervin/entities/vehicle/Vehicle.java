package com.sentinovo.carbuildervin.entities.vehicle;

import com.sentinovo.carbuildervin.entities.BaseEntity;
import com.sentinovo.carbuildervin.entities.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "vehicle")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$", message = "VIN must be 17 characters and contain valid characters")
    @Column(name = "vin", length = 17, unique = true)
    private String vin;

    @Min(value = 1900, message = "Year must be 1900 or later")
    @Max(value = 2030, message = "Year must be 2030 or earlier")
    @Column(name = "year")
    private Integer year;

    @Size(max = 100, message = "Make must not exceed 100 characters")
    @Column(name = "make", length = 100)
    private String make;

    @Size(max = 100, message = "Model must not exceed 100 characters")
    @Column(name = "model", length = 100)
    private String model;

    @Size(max = 100, message = "Trim must not exceed 100 characters")
    @Column(name = "trim", length = 100)
    private String trim;

    @Size(max = 100, message = "Nickname must not exceed 100 characters")
    @Column(name = "nickname", length = 100)
    private String nickname;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(name = "is_archived", nullable = false)
    private Boolean isArchived = false;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<VehicleUpgrade> vehicleUpgrades = new HashSet<>();

    public void addVehicleUpgrade(VehicleUpgrade vehicleUpgrade) {
        vehicleUpgrades.add(vehicleUpgrade);
        vehicleUpgrade.setVehicle(this);
    }

    public void removeVehicleUpgrade(VehicleUpgrade vehicleUpgrade) {
        vehicleUpgrades.remove(vehicleUpgrade);
        vehicleUpgrade.setVehicle(null);
    }

    public String getDisplayName() {
        if (nickname != null && !nickname.trim().isEmpty()) {
            return nickname;
        }
        
        StringBuilder displayName = new StringBuilder();
        if (year != null) {
            displayName.append(year).append(" ");
        }
        if (make != null && !make.trim().isEmpty()) {
            displayName.append(make).append(" ");
        }
        if (model != null && !model.trim().isEmpty()) {
            displayName.append(model).append(" ");
        }
        if (trim != null && !trim.trim().isEmpty()) {
            displayName.append(trim);
        }
        
        String result = displayName.toString().trim();
        return result.isEmpty() ? "Unknown Vehicle" : result;
    }
}