package com.example.ecommerce_backend.Service;

import com.example.ecommerce_backend.Entity.Order;
import com.example.ecommerce_backend.Entity.OrderItem;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendResetPasswordEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[JustLife] Yêu cầu đặt lại mật khẩu tài khoản của bạn");
        message.setText("Chào bạn,\n\n"
                + "Chúng tôi nhận được yêu cầu khôi phục mật khẩu từ bạn.\n"
                + "Vui lòng nhấp vào đường liên kết dưới đây để tiến hành thiết lập mật khẩu mới (Mã có hiệu lực trong 15 phút):\n"
                + resetLink + "\n\n"
                + "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.\n"
                + "Trân trọng,\nĐội ngũ vận hành JustLife.");

        mailSender.send(message);
    }

    // 🌟 HÀM NÂNG CẤP: Gửi Email Hóa đơn HTML kèm mã QR Đơn hàng
    public void sendOrderConfirmationEmail(String toEmail, Order order, byte[] qrCodeImage) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            // Bật chế độ multipart = true để đính kèm được dữ liệu hình ảnh inline
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[JustLife] Hóa đơn thanh toán thành công - Đơn hàng #" + order.getId());

            // 1. Thiết kế cấu trúc giao diện Email bằng HTML bảng biểu
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0;'>");
            htmlContent.append("<h2 style='color: #2c3e50; text-align: center;'>CẢM ƠN BẠN ĐÃ MUA SẮM TẠI JUSTLIFE!</h2>");
            htmlContent.append("<p>Xin chào, đơn hàng của bạn đã được thanh toán thành công và đang được chuẩn bị để bàn giao cho đơn vị vận chuyển.</p>");

            // Thông tin nhận hàng
            htmlContent.append("<h3>📍 Thông tin giao hàng</h3>");
            htmlContent.append("<p><strong>Người nhận:</strong> ").append(order.getUser().getFullName()).append("</p>");
            htmlContent.append("<p><strong>Số điện thoại:</strong> ").append(order.getPhoneNumber()).append("</p>");
            htmlContent.append("<p><strong>Địa chỉ:</strong> ").append(order.getShippingAddress()).append("</p>");
            htmlContent.append("<p><strong>Phương thức thanh toán:</strong> ").append(order.getPaymentMethod()).append("</p>");

            // Danh sách mặt hàng mua sắm
            htmlContent.append("<h3>🛍️ Chi tiết sản phẩm</h3>");
            htmlContent.append("<table style='width: 100%; border-collapse: collapse; margin-bottom: 15px;'>");
            htmlContent.append("<tr style='background-color: #f8f9fa;'>")
                    .append("<th style='padding: 8px; border: 1px solid #ddd; text-align: left;'>Sản phẩm</th>")
                    .append("<th style='padding: 8px; border: 1px solid #ddd; text-align: center;'>SL</th>")
                    .append("<th style='padding: 8px; border: 1px solid #ddd; text-align: right;'>Đơn giá</th>")
                    .append("</tr>");

            for (OrderItem item : order.getOrderItems()) {
                htmlContent.append("<tr>")
                        .append("<td style='padding: 8px; border: 1px solid #ddd;'>").append(item.getProduct().getProductName()).append("</td>")
                        .append("<td style='padding: 8px; border: 1px solid #ddd; text-align: center;'>").append(item.getQuantity()).append("</td>")
                        .append("<td style='padding: 8px; border: 1px solid #ddd; text-align: right;'>").append(String.format("%,.0fđ", item.getPrice())).append("</td>")
                        .append("</tr>");
            }
            htmlContent.append("</table>");

            // Tổng tiền
            htmlContent.append("<h3 style='text-align: right; color: #e74c3c;'>Tổng cộng: ")
                    .append(String.format("%,.0fđ", order.getTotalAmount())).append("</h3>");

            // Nhúng thẻ hình ảnh QR bằng mã CID (Content Identifier)
            htmlContent.append("<div style='text-align: center; margin-top: 30px; padding: 15px; background-color: #f9f9f9;'>");
            htmlContent.append("<p style='margin-bottom: 5px; font-weight: bold;'>MÃ QR TRA CỨU ĐƠN HÀNG VẬN CHUYỂN</p>");
            htmlContent.append("<img src='cid:qrCodeImageInline' width='180' height='180' style='border: 1px solid #ccc;'/>");
            htmlContent.append("<p style='font-size: 12px; color: #7f8c8d; margin-top: 5px;'>Quét mã để kiểm tra thông tin hoặc check-in nhận hàng</p>");
            htmlContent.append("</div>");
            htmlContent.append("</div>");

            // Thiết lập nội dung Email dưới dạng HTML
            helper.setText(htmlContent.toString(), true);

            // 2. Thực hiện gắn mảng byte ảnh QR Code khớp với từ khóa Content-ID "qrCodeImageInline" ở trên
            helper.addInline("qrCodeImageInline", new ByteArrayResource(qrCodeImage), "image/png");

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException("Gặp lỗi khi gửi email hóa đơn: " + e.getMessage());
        }
    }
}
