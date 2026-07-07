package com.example.ecommerce_backend.Service;

import com.example.ecommerce_backend.Entity.Voucher;
import com.example.ecommerce_backend.Repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoucherService {
    @Autowired
    private VoucherRepository voucherRepository;

    public Voucher createVoucher(Voucher voucher) {
        // Validate đầu vào
        if (voucher.getCode() == null || voucher.getCode().isBlank()) {
            throw new RuntimeException("Mã voucher không được để trống!");
        }
        if (voucher.getDiscountPercent() == null || voucher.getDiscountPercent() <= 0 || voucher.getDiscountPercent() > 100) {
            throw new RuntimeException("Phần trăm giảm giá phải từ 1 đến 100!");
        }
        if (voucher.getMaxUses() == null || voucher.getMaxUses() <= 0) {
            throw new RuntimeException("Số lượt dùng tối đa phải lớn hơn 0!");
        }
        if (voucherRepository.findByCode(voucher.getCode()).isPresent()) {
            throw new RuntimeException("Mã voucher '" + voucher.getCode() + "' đã tồn tại!");
        }

        voucher.setUsedCount(0);
        voucher.setActive(true);
        return voucherRepository.save(voucher);
    }

    public List<Voucher> getAllVoucher() {
        return voucherRepository.findAll();
    }

    public void deleteVoucher(Long id) {
        if (!voucherRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy voucher với id = " + id);
        }
        voucherRepository.deleteById(id);
    }

    // Client kiểm tra tính hợp lệ của mã giảm giá
    public Voucher validateVoucher(String code) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại!"));

        if (voucher.getActive() == null || !voucher.getActive()) {
            throw new RuntimeException("Mã giảm giá này đã bị vô hiệu hóa!");
        }

        if (voucher.getExpiryDate() != null && voucher.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã giảm giá này đã hết hạn sử dụng!");
        }

        if (voucher.getUsedCount() == null || voucher.getMaxUses() == null || voucher.getUsedCount() >= voucher.getMaxUses()) {
            throw new RuntimeException("Mã giảm giá này đã hết lượt sử dụng!");
        }

        return voucher;
    }

    public List<Voucher> getActiveVouchersForClient() {
        return voucherRepository.findActiveVouchers(LocalDateTime.now());
    }
}

