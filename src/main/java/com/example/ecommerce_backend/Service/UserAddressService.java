package com.example.ecommerce_backend.Service;

import com.example.ecommerce_backend.Entity.User;
import com.example.ecommerce_backend.Entity.UserAddress;
import com.example.ecommerce_backend.Repository.UserAddressRepository;
import com.example.ecommerce_backend.dto.AddressRequestDTO;
import com.example.ecommerce_backend.dto.AddressResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserAddressService {

    private final UserAddressRepository addressRepository;

    public UserAddressService(UserAddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public List<AddressResponseDTO> getUserAddresses(Long userId) {
        return addressRepository.findByUserIdOrderByDefaultDesc(userId)
                .stream()
                .map(AddressResponseDTO::new)
                .toList();
    }

    @Transactional
    public AddressResponseDTO createAddress(User user, AddressRequestDTO request) {
        validateAddressRequest(request);

        List<UserAddress> existingAddresses = addressRepository.findByUserIdOrderByDefaultDesc(user.getId());

        UserAddress address = new UserAddress();
        address.setUser(user);
        address.setFullName(request.getFullName().trim());
        address.setPhone(request.getPhone().trim());
        address.setAddressDetail(request.getAddressDetail().trim());
        address.setAddressType(request.getAddressType() != null ? request.getAddressType().trim() : "OTHER");

        // Nếu là địa chỉ đầu tiên hoặc được yêu cầu làm mặc định
        boolean shouldBeDefault = existingAddresses.isEmpty() || (request.getIsDefault() != null && request.getIsDefault());

        if (shouldBeDefault) {
            for (UserAddress ua : existingAddresses) {
                if (ua.getIsDefault()) {
                    ua.setIsDefault(false);
                    addressRepository.save(ua);
                }
            }
            address.setIsDefault(true);
        } else {
            address.setIsDefault(false);
        }

        UserAddress saved = addressRepository.save(address);
        return new AddressResponseDTO(saved);
    }

    @Transactional
    public AddressResponseDTO updateAddress(User user, Long addressId, AddressRequestDTO request) {
        validateAddressRequest(request);

        UserAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại với ID: " + addressId));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền sửa địa chỉ này!");
        }

        address.setFullName(request.getFullName().trim());
        address.setPhone(request.getPhone().trim());
        address.setAddressDetail(request.getAddressDetail().trim());
        address.setAddressType(request.getAddressType() != null ? request.getAddressType().trim() : "OTHER");

        boolean wantDefault = request.getIsDefault() != null && request.getIsDefault();
        boolean isCurrentlyDefault = address.getIsDefault();

        if (wantDefault && !isCurrentlyDefault) {
            List<UserAddress> existingAddresses = addressRepository.findByUserIdOrderByDefaultDesc(user.getId());
            for (UserAddress ua : existingAddresses) {
                if (!ua.getId().equals(addressId) && ua.getIsDefault()) {
                    ua.setIsDefault(false);
                    addressRepository.save(ua);
                }
            }
            address.setIsDefault(true);
        } else if (!wantDefault && isCurrentlyDefault) {
            List<UserAddress> existingAddresses = addressRepository.findByUserIdOrderByDefaultDesc(user.getId());
            if (existingAddresses.size() <= 1) {
                address.setIsDefault(true);
            } else {
                UserAddress alternative = existingAddresses.stream()
                        .filter(ua -> !ua.getId().equals(addressId))
                        .findFirst()
                        .orElse(null);
                if (alternative != null) {
                    alternative.setIsDefault(true);
                    addressRepository.save(alternative);
                }
                address.setIsDefault(false);
            }
        }

        UserAddress saved = addressRepository.save(address);
        return new AddressResponseDTO(saved);
    }

    @Transactional
    public void deleteAddress(User user, Long addressId) {
        UserAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại với ID: " + addressId));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền xóa địa chỉ này!");
        }

        boolean wasDefault = address.getIsDefault();
        addressRepository.delete(address);

        if (wasDefault) {
            List<UserAddress> remaining = addressRepository.findByUserIdOrderByDefaultDesc(user.getId());
            if (!remaining.isEmpty()) {
                UserAddress newDefault = remaining.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
            }
        }
    }

    private void validateAddressRequest(AddressRequestDTO request) {
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Họ tên người nhận không được để trống");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new RuntimeException("Số điện thoại nhận hàng không được để trống");
        }
        if (request.getAddressDetail() == null || request.getAddressDetail().trim().isEmpty()) {
            throw new RuntimeException("Địa chỉ chi tiết không được để trống");
        }
    }
}
