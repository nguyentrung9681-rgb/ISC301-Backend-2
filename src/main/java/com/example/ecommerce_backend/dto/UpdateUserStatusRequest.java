package com.example.ecommerce_backend.dto;

import com.example.ecommerce_backend.Entity.AccountStatus;

public class UpdateUserStatusRequest {

    private AccountStatus status;

    public UpdateUserStatusRequest() {
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}