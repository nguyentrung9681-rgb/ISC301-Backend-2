package com.example.ecommerce_backend.Service;

import com.example.ecommerce_backend.Entity.Order;
import com.example.ecommerce_backend.Entity.OrderItem;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Async
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

    // Hàm helper xây dựng bảng chi tiết sản phẩm HTML dùng chung
    private String buildOrderItemsTable(Order order) {
        StringBuilder table = new StringBuilder();
        table.append("<table style='width: 100%; border-collapse: collapse; margin-bottom: 15px;'>");
        table.append("<tr style='background-color: #f8f9fa;'>")
             .append("<th style='padding: 8px; border: 1px solid #ddd; text-align: left;'>Sản phẩm</th>")
             .append("<th style='padding: 8px; border: 1px solid #ddd; text-align: center;'>SL</th>")
             .append("<th style='padding: 8px; border: 1px solid #ddd; text-align: right;'>Đơn giá</th>")
             .append("</tr>");

        java.math.BigDecimal subTotal = java.math.BigDecimal.ZERO;
        for (OrderItem item : order.getOrderItems()) {
            subTotal = subTotal.add(item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())));
            table.append("<tr>")
                 .append("<td style='padding: 8px; border: 1px solid #ddd;'>").append(item.getProduct().getProductName()).append("</td>")
                 .append("<td style='padding: 8px; border: 1px solid #ddd; text-align: center;'>").append(item.getQuantity()).append("</td>")
                 .append("<td style='padding: 8px; border: 1px solid #ddd; text-align: right;'>").append(String.format("%,.0fđ", item.getPrice())).append("</td>")
                 .append("</tr>");
        }
        table.append("</table>");

        java.math.BigDecimal shippingFee = java.math.BigDecimal.ZERO;
        if (subTotal.compareTo(java.math.BigDecimal.valueOf(500000)) < 0 && subTotal.compareTo(java.math.BigDecimal.ZERO) > 0) {
            shippingFee = java.math.BigDecimal.valueOf(30000);
        }

        java.math.BigDecimal discountAmount = subTotal.add(shippingFee).subtract(order.getTotalAmount());
        if (discountAmount.compareTo(java.math.BigDecimal.ZERO) < 0) {
            discountAmount = java.math.BigDecimal.ZERO;
        }

        table.append("<div style='text-align: right; line-height: 1.6; font-size: 14px;'>");
        table.append("<div>Tạm tính: <strong>").append(String.format("%,.0fđ", subTotal)).append("</strong></div>");
        table.append("<div>Phí vận chuyển: <strong>").append(shippingFee.compareTo(java.math.BigDecimal.ZERO) > 0 ? String.format("%,.0fđ", shippingFee) : "Miễn phí").append("</strong></div>");
        if (discountAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
            table.append("<div style='color: #27ae60;'>Giảm giá: <strong>-").append(String.format("%,.0fđ", discountAmount)).append("</strong></div>");
        }
        table.append("<h3 style='color: #e74c3c; margin-top: 10px; margin-bottom: 0;'>Tổng cộng: ")
             .append(String.format("%,.0fđ", order.getTotalAmount())).append("</h3>");
        table.append("</div>");

        return table.toString();
    }

    // 🌟 HÀM NÂNG CẤP: Gửi Email Hóa đơn HTML kèm mã QR Đơn hàng khi đã THANH TOÁN (PAID)
    public void sendOrderConfirmationEmail(String toEmail, Order order, byte[] qrCodeImage) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[JustLife] Hóa đơn thanh toán thành công - Đơn hàng #" + order.getId());

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
            htmlContent.append(buildOrderItemsTable(order));

            // Nhúng thẻ hình ảnh QR bằng mã CID (Content Identifier)
            htmlContent.append("<div style='text-align: center; margin-top: 30px; padding: 15px; background-color: #f9f9f9;'>");
            htmlContent.append("<p style='margin-bottom: 5px; font-weight: bold;'>MÃ QR TRA CỨU ĐƠN HÀNG VẬN CHUYỂN</p>");
            htmlContent.append("<img src='cid:qrCodeImageInline' width='180' height='180' style='border: 1px solid #ccc;'/>");
            htmlContent.append("<p style='font-size: 12px; color: #7f8c8d; margin-top: 5px;'>Quét mã để kiểm tra thông tin hoặc check-in nhận hàng</p>");
            htmlContent.append("</div>");
            htmlContent.append("</div>");

            helper.setText(htmlContent.toString(), true);
            helper.addInline("qrCodeImageInline", new ByteArrayResource(qrCodeImage), "image/png");

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException("Gặp lỗi khi gửi email hóa đơn: " + e.getMessage());
        }
    }

    // 🌟 Gửi Email xác nhận đơn hàng mới (PENDING)
    public void sendOrderPendingEmail(String toEmail, Order order) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[JustLife] Xác nhận đơn hàng mới - Đơn hàng #" + order.getId());

            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0;'>");
            htmlContent.append("<h2 style='color: #2c3e50; text-align: center;'>XÁC NHẬN ĐƠN HÀNG MỚI!</h2>");
            htmlContent.append("<p>Xin chào <strong>").append(order.getUser().getFullName()).append("</strong>,</p>");
            htmlContent.append("<p>Cảm ơn bạn đã mua sắm tại JustLife. Đơn hàng của bạn đã được ghi nhận thành công trên hệ thống và đang chờ xử lý.</p>");

            // Thông tin giao hàng
            htmlContent.append("<h3>📍 Thông tin giao hàng</h3>");
            htmlContent.append("<p><strong>Người nhận:</strong> ").append(order.getUser().getFullName()).append("</p>");
            htmlContent.append("<p><strong>Số điện thoại:</strong> ").append(order.getPhoneNumber()).append("</p>");
            htmlContent.append("<p><strong>Địa chỉ giao:</strong> ").append(order.getShippingAddress()).append("</p>");
            htmlContent.append("<p><strong>Phương thức thanh toán:</strong> ").append(order.getPaymentMethod()).append("</p>");
            htmlContent.append("<p><strong>Trạng thái:</strong> <span style='background-color: #f1c40f; color: #fff; padding: 3px 8px; border-radius: 3px; font-weight: bold;'>ĐANG CHỜ XỬ LÝ (PENDING)</span></p>");

            // Chi tiết sản phẩm
            htmlContent.append("<h3>🛍️ Chi tiết sản phẩm</h3>");
            htmlContent.append(buildOrderItemsTable(order));

            htmlContent.append("<hr style='border: 0; border-top: 1px solid #eee; margin: 20px 0;'>");
            htmlContent.append("<p style='font-size: 13px; color: #7f8c8d; text-align: center;'>Chúng tôi sẽ thông báo cho bạn khi đơn hàng được bàn giao cho đối tác vận chuyển.</p>");
            htmlContent.append("</div>");

            helper.setText(htmlContent.toString(), true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email xác nhận đặt hàng: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 🌟 Gửi Email thông báo cập nhật trạng thái giao hàng (SHIPPING hoặc DELIVERED)
    public void sendOrderStatusUpdateEmail(String toEmail, Order order, String newStatus) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String statusVi = newStatus.equalsIgnoreCase("SHIPPING") ? "ĐANG GIAO HÀNG" : "ĐÃ GIAO THÀNH CÔNG";
            String badgeColor = newStatus.equalsIgnoreCase("SHIPPING") ? "#2980b9" : "#27ae60";

            helper.setTo(toEmail);
            helper.setSubject("[JustLife] Cập nhật trạng thái đơn hàng #" + order.getId() + " - " + statusVi);

            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0;'>");
            htmlContent.append("<h2 style='color: #2c3e50; text-align: center;'>CẬP NHẬT HÀNH TRÌNH ĐƠN HÀNG</h2>");
            htmlContent.append("<p>Xin chào <strong>").append(order.getUser().getFullName()).append("</strong>,</p>");
            
            if (newStatus.equalsIgnoreCase("SHIPPING")) {
                htmlContent.append("<p>Đơn hàng của bạn đã được đóng gói và bàn giao cho đối tác vận chuyển để giao tới bạn.</p>");
            } else {
                htmlContent.append("<p>Tuyệt vời! Đơn hàng của bạn đã được giao thành công. Cảm ơn bạn đã lựa chọn JustLife!</p>");
            }

            // Thông tin giao hàng
            htmlContent.append("<h3>📍 Chi tiết giao hàng</h3>");
            htmlContent.append("<p><strong>Người nhận:</strong> ").append(order.getUser().getFullName()).append("</p>");
            htmlContent.append("<p><strong>Số điện thoại:</strong> ").append(order.getPhoneNumber()).append("</p>");
            htmlContent.append("<p><strong>Địa chỉ giao:</strong> ").append(order.getShippingAddress()).append("</p>");
            htmlContent.append("<p><strong>Trạng thái hiện tại:</strong> <span style='background-color: ").append(badgeColor).append("; color: #fff; padding: 3px 8px; border-radius: 3px; font-weight: bold;'>").append(statusVi).append("</span></p>");

            // Chi tiết sản phẩm
            htmlContent.append("<h3>🛍️ Chi tiết sản phẩm</h3>");
            htmlContent.append(buildOrderItemsTable(order));

            htmlContent.append("</div>");

            helper.setText(htmlContent.toString(), true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email cập nhật trạng thái đơn hàng: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 🌟 Gửi Email thông báo hủy đơn hàng (CANCELLED)
    public void sendOrderCancelledEmail(String toEmail, Order order) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[JustLife] Thông báo hủy đơn hàng #" + order.getId());

            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0;'>");
            htmlContent.append("<h2 style='color: #c0392b; text-align: center;'>ĐƠN HÀNG ĐÃ BỊ HỦY</h2>");
            htmlContent.append("<p>Xin chào <strong>").append(order.getUser().getFullName()).append("</strong>,</p>");
            htmlContent.append("<p>Chúng tôi rất tiếc phải thông báo rằng đơn hàng <strong>#").append(order.getId()).append("</strong> của bạn đã bị hủy trên hệ thống.</p>");
            htmlContent.append("<p>Nếu bạn đã thực hiện thanh toán chuyển khoản trước đó, số tiền hoàn trả sẽ được xử lý theo quy trình hoàn tiền của chúng tôi. Vui lòng liên hệ bộ phận hỗ trợ khách hàng nếu cần trợ giúp gấp.</p>");

            // Thông tin giao hàng
            htmlContent.append("<h3>📍 Chi tiết đơn hàng bị hủy</h3>");
            htmlContent.append("<p><strong>Người nhận:</strong> ").append(order.getUser().getFullName()).append("</p>");
            htmlContent.append("<p><strong>Trạng thái:</strong> <span style='background-color: #e74c3c; color: #fff; padding: 3px 8px; border-radius: 3px; font-weight: bold;'>ĐÃ HỦY (CANCELLED)</span></p>");

            // Chi tiết sản phẩm
            htmlContent.append("<h3>🛍️ Chi tiết sản phẩm</h3>");
            htmlContent.append(buildOrderItemsTable(order));

            htmlContent.append("</div>");

            helper.setText(htmlContent.toString(), true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email báo hủy đơn hàng: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
