package com.example.ecommerce_backend.dto;

import lombok.Data;

@Data
public class RegisterManagerRequestDTO {
    private String email;
    private String password;
    private String fullName;
    private String phone;
    private String address; //đồng bộ với entity user
}
