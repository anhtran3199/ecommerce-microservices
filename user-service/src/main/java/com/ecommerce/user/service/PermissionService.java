package com.ecommerce.user.service;

import com.ecommerce.user.dto.PermissionDto;
import com.ecommerce.user.entity.Permission;
import com.ecommerce.user.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    public Optional<Permission> getPermissionById(Long id) {
        return permissionRepository.findById(id);
    }

    public Optional<Permission> getPermissionByName(String name) {
        return permissionRepository.findByName(name);
    }

    public List<Permission> getPermissionsByResource(String resource) {
        return permissionRepository.findByResource(resource);
    }

    public List<Permission> getPermissionsByAction(String action) {
        return permissionRepository.findByAction(action);
    }

    @Transactional
    public Permission createPermission(PermissionDto permissionDto) {
        if (permissionRepository.existsByName(permissionDto.getName())) {
            throw new RuntimeException("Permission with name '" + permissionDto.getName() + "' already exists");
        }

        Optional<Permission> existingPermission = permissionRepository.findByResourceAndAction(
                permissionDto.getResource(), permissionDto.getAction());
        if (existingPermission.isPresent()) {
            throw new RuntimeException("Permission for resource '" + permissionDto.getResource() +
                    "' and action '" + permissionDto.getAction() + "' already exists");
        }

        Permission permission = Permission.builder()
                .name(permissionDto.getName())
                .description(permissionDto.getDescription())
                .resource(permissionDto.getResource())
                .action(permissionDto.getAction())
                .build();

        return permissionRepository.save(permission);
    }

    @Transactional
    public Permission updatePermission(Long id, PermissionDto permissionDto) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));

        if (!permission.getName().equals(permissionDto.getName()) &&
            permissionRepository.existsByName(permissionDto.getName())) {
            throw new RuntimeException("Permission with name '" + permissionDto.getName() + "' already exists");
        }

        permission.setName(permissionDto.getName());
        permission.setDescription(permissionDto.getDescription());
        permission.setResource(permissionDto.getResource());
        permission.setAction(permissionDto.getAction());

        return permissionRepository.save(permission);
    }

    @Transactional
    public void deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));

        permissionRepository.delete(permission);
    }
}