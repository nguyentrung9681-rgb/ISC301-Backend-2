package com.example.ecommerce_backend.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_addresses")
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "full_name", length = 100, nullable = false, columnDefinition = "NVARCHAR(100)")
    private String fullName;

    @Column(name = "phone", length = 20, nullable = false)
    private String phone;

    @Column(name = "address_detail", length = 255, nullable = false, columnDefinition = "NVARCHAR(255)")
    private String addressDetail;

    @Column(name = "address_type", length = 20)
    private String addressType; // HOME, OFFICE, OTHER

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    public UserAddress() {
    }

    public UserAddress(Long id, User user, String fullName, String phone, String addressDetail, String addressType, Boolean isDefault) {
        this.id = id;
        this.user = user;
        this.fullName = fullName;
        this.phone = phone;
        this.addressDetail = addressDetail;
        this.addressType = addressType;
        this.isDefault = isDefault;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
