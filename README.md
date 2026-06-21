# Hướng dẫn cấu hình môi trường chạy Local (Developer Setup Guide)

Dự án đã được cấu hình để bảo mật các thông tin nhạy cảm (như thông tin email SMTP và API Key PayOS) bằng cách sử dụng các biến môi trường thay vì lưu trực tiếp trong file `application.properties`. 

Dưới đây là hướng dẫn chi tiết để bạn và các thành viên khác trong đội ngũ phát triển cấu hình chạy ứng dụng ở máy local.

---

## Cách 1: Sử dụng file `application-local.properties` (Khuyên dùng)

File `application-local.properties` đã được thêm vào danh sách bỏ qua của Git (`.gitignore`), nên bạn có thể thoải mái lưu thông tin cấu hình thật ở máy mình mà không sợ bị đẩy lên GitHub.

### Bước 1: Tạo file cấu hình local
Tạo một file mới tên là `application-local.properties` trong thư mục:
`src/main/resources/application-local.properties`

### Bước 2: Dán cấu hình từ file Notepad của bạn vào
Sao chép và điền các thông tin thực tế từ file Notepad của bạn vào file mới tạo:

```properties
# Cấu hình PayOS
payos.client-id=điền_client_id_payos_của_bạn
payos.api-key=điền_api_key_payos_của_bạn
payos.checksum-key=điền_checksum_key_payos_của_bạn

# Cấu hình Spring Mail Service
spring.mail.username=tên_đăng_nhập_email_của_bạn@gmail.com
spring.mail.password=mật_khẩu_ứng_dụng_gmail_của_bạn
```

### Bước 3: Kích hoạt profile `local` khi chạy ứng dụng
Để Spring Boot nạp cấu hình từ file `application-local.properties` này, bạn cần chạy ứng dụng với profile là `local`:

* **Nếu chạy trên IntelliJ IDEA / STS / Eclipse:**
  1. Vào phần cấu hình chạy (**Run/Debug Configurations**).
  2. Tại ô **Active profiles** (hoặc cấu hình VM Option), bạn điền: `local`
  3. Lưu lại và chạy ứng dụng.

* **Nếu chạy bằng dòng lệnh (Terminal):**
  ```bash
  mvn spring-boot:run -Dspring-boot.run.profiles=local
  ```

---

## Cách 2: Cấu hình biến môi trường (Environment Variables) trực tiếp trong IDE

Nếu không muốn tạo file mới, bạn có thể truyền các giá trị trực tiếp thông qua cài đặt của IDE:

1. Vào phần cấu hình chạy (**Run/Debug Configurations**) trong IDE của bạn.
2. Tìm mục **Environment variables** (Biến môi trường) và thêm các biến sau kèm giá trị thực tế từ Notepad của bạn:
   * `PAYOS_CLIENT_ID`
   * `PAYOS_API_KEY`
   * `PAYOS_CHECKSUM_KEY`
   * `SPRING_MAIL_USERNAME`
   * `SPRING_MAIL_PASSWORD`
3. Lưu lại và nhấn chạy.
