package com.ecommerce.user.controller;

import com.ecommerce.user.dto.RoleDto;
import com.ecommerce.user.entity.Permission;
import com.ecommerce.user.entity.Role;
import com.ecommerce.user.service.RoleService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/roles")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        Optional<Role> role = roleService.getRoleById(id);
        return role.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Role> getRoleByName(@PathVariable String name) {
        Optional<Role> role = roleService.getRoleByName(name);
        return role.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createRole(@Valid @RequestBody RoleDto roleDto) {
        try {
            Role createdRole = roleService.createRole(roleDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDto roleDto) {
        try {
            Role updatedRole = roleService.updateRole(id, roleDto);
            return ResponseEntity.ok(updatedRole);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/assign")
    public ResponseEntity<?> assignRoleToUser(@RequestParam Long userId, @RequestParam String roleName) {
        try {
            roleService.assignRoleToUser(userId, roleName);
            return ResponseEntity.ok().body("Role assigned successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeRoleFromUser(@RequestParam Long userId, @RequestParam String roleName) {
        try {
            roleService.removeRoleFromUser(userId, roleName);
            return ResponseEntity.ok().body("Role removed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}/permissions")
    public ResponseEntity<List<Permission>> getUserPermissions(@PathVariable Long userId) {
        List<Permission> permissions = roleService.getUserPermissions(userId);
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/user/{userId}/check")
    public ResponseEntity<Boolean> checkUserPermission(
            @PathVariable Long userId,
            @RequestParam String resource,
            @RequestParam String action) {
        boolean hasPermission = roleService.hasPermission(userId, resource, action);
        return ResponseEntity.ok(hasPermission);
    }
}