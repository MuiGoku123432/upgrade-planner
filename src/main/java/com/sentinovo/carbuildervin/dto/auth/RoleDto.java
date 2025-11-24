package com.sentinovo.carbuildervin.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User role information")
public class RoleDto {

    @Schema(description = "Role ID", example = "1")
    private Integer id;

    @Schema(description = "Role name", example = "USER")
    private String name;

    @Schema(description = "Role description", example = "Standard user role")
    private String description;
}