# Bookstore Flutter

Ứng dụng mẫu cho đăng nhập, xem sách, giỏ hàng, đặt hàng và xem đơn đã đặt từ API ở `../backend`. Xem [README chính](../README.md) để biết cấu trúc toàn dự án và cách chuẩn bị cơ sở dữ liệu.

1. Chạy Spring Boot API tại cổng 8080.
2. Trong thư mục này chạy `flutter pub get` rồi `flutter run`.
3. Android Emulator dùng `http://10.0.2.2:8080/api`; Windows và web dùng `http://localhost:8080/api`.
4. Với điện thoại thật, chạy `flutter run --dart-define=API_BASE_URL=http://<IP-máy-tính>:8080/api`.

Sau khi sửa backend, dừng API cũ bằng `Ctrl+C` rồi chạy lại `.\mvnw.cmd spring-boot:run` trong `../backend`. Nếu Flutter đang chạy, nhấn `R` trong terminal Flutter để khởi động lại app.

Trong app: đăng nhập → bấm nút thêm sách → mở Giỏ hàng, chỉnh số lượng và nhập địa chỉ, điện thoại → Đặt hàng → xem mục Đơn hàng. Giỏ hàng được lưu trên thiết bị theo từng tài khoản. Tồn kho được trừ khi đặt thành công; hủy hoặc từ chối đơn sẽ hoàn kho. API kiểm tra tồn kho có điều kiện để không nhận hai đơn vượt quá số lượng còn lại.

Trên Windows, Flutter cần bật **Developer Mode** để tạo symlink cho plugin `shared_preferences` trước khi chạy ứng dụng.

Các file Dart ở thư mục gốc là bản bạn cung cấp. Mã ứng dụng chạy nằm trong `frontend/lib`.
