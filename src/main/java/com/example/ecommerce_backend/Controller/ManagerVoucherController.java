package com.example.ecommerce_backend.Controller;

import com.example.ecommerce_backend.Entity.Voucher;
import com.example.ecommerce_backend.Service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager/voucher")
public class ManagerVoucherController {
    @Autowired
    private VoucherService voucherService;

    @PostMapping("/create")
    public ResponseEntity<Voucher> create(@RequestBody Voucher voucher) {
        return ResponseEntity.ok(voucherService.createVoucher(voucher));
    }

    @GetMapping("/list")
    public ResponseEntity<List<Voucher>> list() {
        return ResponseEntity.ok(voucherService.getAllVoucher());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.ok("Xóa mã giảm giá thành công");
    }
}
