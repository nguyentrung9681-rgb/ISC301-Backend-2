package com.example.ecommerce_backend.dto;

import com.example.ecommerce_backend.Entity.UserAddress;

public class AddressResponseDTO {
    private Long id;
    private String fullName;
    private String phone;
    private String addressDetail;
    private String addressType;
    private Boolean isDefault;

    public AddressResponseDTO() {
    }

    public AddressResponseDTO(UserAddress address) {
        this.id = address.getId();
        this.fullName = address.getFullName();
        this.phone = address.getPhone();
        this.addressDetail = address.getAddressDetail();
        this.addressType = address.getAddressType();
        this.isDefault = address.getIsDefault();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddressDetail() {
        return addressDetail;
    }

    public void setAddressDetail(String addressDetail) {
        this.addressDetail = addressDetail;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
}
