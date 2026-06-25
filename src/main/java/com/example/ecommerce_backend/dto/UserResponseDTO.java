package com.example.ecommerce_backend.dto;

import com.example.ecommerce_backend.Entity.AccountStatus;
import com.example.ecommerce_backend.Entity.Role;
import com.example.ecommerce_backend.Entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;

public class UserResponseDTO {

    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private Role role;
    private AccountStatus accountStatus;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String token;

    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.phone = user.getPhone();
        this.address = user.getAddress();
        this.role = user.getRole();
        this.accountStatus = user.getAccountStatus();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public Role getRole() {
        return role;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}