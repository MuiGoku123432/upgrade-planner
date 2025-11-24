package com.sentinovo.carbuildervin.services.user;

import com.sentinovo.carbuildervin.entities.user.Role;
import com.sentinovo.carbuildervin.exception.DuplicateResourceException;
import com.sentinovo.carbuildervin.exception.InvalidStateException;
import com.sentinovo.carbuildervin.exception.ResourceNotFoundException;
import com.sentinovo.carbuildervin.repository.user.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public Role findById(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
    }

    @Transactional(readOnly = true)
    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public Role getByName(String name) {
        return findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role", name));
    }

    @Transactional(readOnly = true)
    public List<Role> findAllRoles() {
        return roleRepository.findAllOrderByName();
    }

    @Transactional(readOnly = true)
    public List<Role> findRolesByNames(List<String> names) {
        return roleRepository.findByNameIn(names);
    }

    @Transactional(readOnly = true)
    public long countUsersByRole(UUID roleId) {
        return roleRepository.countUsersByRoleId(roleId);
    }

    public Role createRole(String name, String description) {
        log.info("Creating new role with name: {}", name);
        
        validateRoleCreation(name);
        
        Role role = Role.builder()
                .name(name)
                .description(description)
                .build();

        Role savedRole = roleRepository.save(role);
        log.info("Successfully created role with id: {}", savedRole.getId());
        return savedRole;
    }

    public Role updateRole(UUID roleId, String name, String description) {
        log.info("Updating role with id: {}", roleId);
        
        Role role = findById(roleId);
        
        if (name != null && !name.equals(role.getName())) {
            validateRoleNameUniqueness(name, roleId);
            role.setName(name);
        }
        
        if (description != null) {
            role.setDescription(description);
        }

        Role savedRole = roleRepository.save(role);
        log.info("Successfully updated role with id: {}", savedRole.getId());
        return savedRole;
    }

    public void deleteRole(UUID roleId) {
        log.info("Deleting role with id: {}", roleId);
        
        Role role = findById(roleId);
        
        long userCount = countUsersByRole(roleId);
        if (userCount > 0) {
            throw new InvalidStateException(
                String.format("Cannot delete role '%s' - it is assigned to %d user(s)", role.getName(), userCount)
            );
        }
        
        roleRepository.delete(role);
        log.info("Successfully deleted role with id: {}", roleId);
    }

    @Transactional(readOnly = true)
    public boolean isRoleNameAvailable(String name) {
        return !roleRepository.existsByName(name);
    }

    @Transactional(readOnly = true)
    public Role findUserRole() {
        return getByName("USER");
    }

    @Transactional(readOnly = true)
    public Role findAdminRole() {
        return getByName("ADMIN");
    }

    public void ensureDefaultRoles() {
        log.info("Ensuring default roles exist");
        
        if (findByName("USER").isEmpty()) {
            createRole("USER", "Default user role");
        }
        
        if (findByName("ADMIN").isEmpty()) {
            createRole("ADMIN", "Administrator role");
        }
        
        log.info("Default roles ensured");
    }

    private void validateRoleCreation(String name) {
        if (roleRepository.existsByName(name)) {
            throw new DuplicateResourceException("Role", "name", name);
        }
    }

    private void validateRoleNameUniqueness(String name, UUID roleId) {
        if (roleRepository.existsByNameAndIdNot(name, roleId)) {
            throw new DuplicateResourceException("Role", "name", name);
        }
    }
}