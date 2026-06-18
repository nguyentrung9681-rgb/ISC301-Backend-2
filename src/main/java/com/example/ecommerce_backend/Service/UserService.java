package com.example.ecommerce_backend.Service;

import com.example.ecommerce_backend.dto.UserResponseDTO;
import com.example.ecommerce_backend.Entity.AccountStatus;
import com.example.ecommerce_backend.Entity.Role;
import com.example.ecommerce_backend.Entity.User;
import com.example.ecommerce_backend.Repository.UserRepository;
import org.springframework.stereotype.Service;
import com.example.ecommerce_backend.dto.RegisterRequestDTO;
import com.example.ecommerce_backend.dto.LoginRequestDTO;
import com.example.ecommerce_backend.dto.UpdateProfileRequestDTO;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponseDTO updateUserProfile(Long id, UpdateProfileRequestDTO request) {
        // 1. Kiểm tra User có tồn tại không
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với mã ID: " + id));

        // 2. Validate dữ liệu đầu vào (Ví dụ: Họ tên không được để trống)
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Họ tên không được để trống");
        }

        // 3. Tiến hành cập nhật thông tin mới
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);

        // 4. Lưu lại vào Database
        User updatedUser = userRepository.save(user);

        // 5. Trả về thông tin User đã được cập nhật dưới dạng DTO an toàn
        return new UserResponseDTO(updatedUser);
    }

    public UserResponseDTO getUserProfile(Long id) {
        // Tìm kiếm user theo ID, nếu không thấy sẽ ném ra ngoại lệ
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với mã ID: " + id));

        // Trả về DTO chứa thông tin an toàn (Họ tên, Email, SĐT, Role, Status)
        return new UserResponseDTO(user);
    }

    public UserResponseDTO loginUser(LoginRequestDTO request) {
        // 1. Kiểm tra dữ liệu đầu vào bắt buộc
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email không được để trống");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Mật khẩu không được để trống");
        }
        // 2. Tìm người dùng dựa trên Email
        User user = userRepository.findByEmail(request.getEmail().trim())
                .orElseThrow(() -> new RuntimeException("Email hoặc mật khẩu không chính xác"));
        // 3. Kiểm tra mật khẩu (So sánh chuỗi trực tiếp)
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email hoặc mật khẩu không chính xác");
        }

        if (user.getAccountStatus() == AccountStatus.BANNED) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên");
        }

        return new UserResponseDTO(user);
    }
    public UserResponseDTO registerUser(RegisterRequestDTO request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email không được để trống");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Mật khẩu không được để trống");
        }

        // 2. Kiểm tra xem email đã tồn tại hay chưa
        if (userRepository.findByEmail(request.getEmail().trim()).isPresent()) {
            throw new RuntimeException("Email này đã được đăng ký sử dụng trong hệ thống");
        }

        // 3. Tạo đối tượng User mới từ request dữ liệu gửi lên
        User user = new User();
        user.setEmail(request.getEmail().trim());
        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());
        user.setPassword(hashedPassword);

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(Role.BUYER);
        user.setAccountStatus(AccountStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        return new UserResponseDTO(savedUser);
    }

    // View user: Manager xem toàn bộ khách hàng
    public List<UserResponseDTO> getAllBuyers() {
        return userRepository.findByRole(Role.BUYER)
                .stream()
                .map(UserResponseDTO::new)
                .toList();
    }

    // Search user: Manager tìm khách hàng theo email hoặc số điện thoại
    public List<UserResponseDTO> searchBuyers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBuyers();
        }

        return userRepository.searchUsersByEmailOrPhone(keyword.trim(), Role.BUYER)
                .stream()
                .map(UserResponseDTO::new)
                .toList();
    }

    // Update user status: ACTIVE hoặc BANNED
    public UserResponseDTO updateUserStatus(Long userId, AccountStatus status) {
        if (status == null) {
            throw new RuntimeException("Account status cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (user.getRole() != Role.BUYER) {
            throw new RuntimeException("Only buyer accounts can be updated in User Management");
        }

        user.setAccountStatus(status);
        User updatedUser = userRepository.save(user);

        return new UserResponseDTO(updatedUser);
    }
}
