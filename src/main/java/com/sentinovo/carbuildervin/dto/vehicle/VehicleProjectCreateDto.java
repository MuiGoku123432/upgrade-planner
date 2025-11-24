package com.sentinovo.carbuildervin.dto.vehicle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a project vehicle (without VIN)")
public class VehicleProjectCreateDto {

    @NotBlank(message = "Nickname is required for project vehicles")
    @Size(max = 100, message = "Nickname cannot exceed 100 characters")
    @Schema(description = "Project vehicle nickname", example = "Dream Overlander")
    private String nickname;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Schema(description = "Project notes", example = "Future overland build project")
    private String notes;
}