package com.sentinovo.carbuildervin.entities.parts;

import com.sentinovo.carbuildervin.entities.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "sub_part")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubPart extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_part_id", nullable = false)
    private Part parentPart;

    @NotBlank(message = "Sub-part name is required")
    @Size(max = 200, message = "Sub-part name must not exceed 200 characters")
    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Size(max = 100, message = "Brand must not exceed 100 characters")
    @Column(name = "brand", length = 100)
    private String brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_code")
    private PartCategory partCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_code")
    private PartTier partTier;

    @Size(max = 500, message = "Product URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://).*", message = "Product URL must start with http:// or https://")
    @Column(name = "product_url", length = 500)
    private String productUrl;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 digits before decimal and 2 after")
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Size(max = 3, message = "Currency code must be 3 characters")
    @Builder.Default
    @Column(name = "currency_code", length = 3)
    private String currencyCode = "USD";

    @Builder.Default
    @Column(name = "is_required")
    private Boolean isRequired = true;

    @Size(max = 30, message = "Status must not exceed 30 characters")
    @Builder.Default
    @Column(name = "status", length = 30)
    private String status = "PLANNED";

    @Min(value = 1, message = "Priority value must be between 1 and 10")
    @Max(value = 10, message = "Priority value must be between 1 and 10")
    @Builder.Default
    @Column(name = "priority_value")
    private Integer priorityValue = 5;

    @Column(name = "target_purchase_date")
    private LocalDate targetPurchaseDate;

    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public enum Status {
        PLANNED,
        RESEARCHING,
        ORDERED,
        DELIVERED,
        INSTALLED,
        CANCELLED
    }
}