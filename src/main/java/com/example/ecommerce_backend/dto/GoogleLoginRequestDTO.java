package com.example.ecommerce_backend.dto;

public class GoogleLoginRequestDTO {
    private String idToken;

    public GoogleLoginRequestDTO() {}

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
