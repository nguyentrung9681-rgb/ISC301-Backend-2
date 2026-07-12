package com.example.ecommerce_backend.dto;

import com.example.ecommerce_backend.Entity.Role;

public class UpdateUserRoleRequest {
    private Role role;

    public UpdateUserRoleRequest() {
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
