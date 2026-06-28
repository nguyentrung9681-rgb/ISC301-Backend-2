package com.example.ecommerce_backend.Controller;

import com.example.ecommerce_backend.Entity.User;
import com.example.ecommerce_backend.Service.UserAddressService;
import com.example.ecommerce_backend.dto.AddressRequestDTO;
import com.example.ecommerce_backend.dto.AddressResponseDTO;
import com.example.ecommerce_backend.dto.ApiResponse;
import com.example.ecommerce_backend.util.UserResolverHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/addresses")
public class UserAddressController {

    private final UserAddressService addressService;
    private final UserResolverHelper userResolverHelper;

    public UserAddressController(UserAddressService addressService, UserResolverHelper userResolverHelper) {
        this.addressService = addressService;
        this.userResolverHelper = userResolverHelper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAddresses(HttpServletRequest request) {
        User currentUser = userResolverHelper.resolveCurrentUser(request);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Yêu cầu đăng nhập"));
        }
        List<AddressResponseDTO> addresses = addressService.getUserAddresses(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(addresses));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createAddress(@RequestBody AddressRequestDTO dto, HttpServletRequest request) {
        User currentUser = userResolverHelper.resolveCurrentUser(request);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Yêu cầu đăng nhập"));
        }
        AddressResponseDTO address = addressService.createAddress(currentUser, dto);
        return ResponseEntity.ok(ApiResponse.ok("Đã thêm địa chỉ thành công", address));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateAddress(
            @PathVariable Long id,
            @RequestBody AddressRequestDTO dto,
            HttpServletRequest request
    ) {
        User currentUser = userResolverHelper.resolveCurrentUser(request);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Yêu cầu đăng nhập"));
        }
        AddressResponseDTO address = addressService.updateAddress(currentUser, id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Đã cập nhật địa chỉ thành công", address));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteAddress(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = userResolverHelper.resolveCurrentUser(request);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Yêu cầu đăng nhập"));
        }
        addressService.deleteAddress(currentUser, id);
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa địa chỉ thành công", null));
    }
}
