# CoNangBookStore

Ứng dụng quản lý hiệu sách gồm **Spring Boot API** (`demo/`, Java 17, Microsoft Access) và **Flutter** (`flutter_app/`). Ứng dụng cho phép đăng nhập, xem sách, lưu giỏ hàng trên thiết bị, đặt hàng và xem đơn đã đặt. Khi đặt hàng thành công, API trừ tồn kho; khi hủy hoặc từ chối đơn, API hoàn kho.

## Chuẩn bị

- Cài **JDK 17** và **Flutter SDK**; kiểm tra bằng `java -version` và `flutter doctor`.
- Có tệp cơ sở dữ liệu Microsoft Access đúng cấu trúc dự án tại `demo/data/bookstore1.accdb`. Tệp này chứa dữ liệu cục bộ nên không được đưa lên GitHub. Nếu dùng đường dẫn khác, sửa `store.database.path` trong cấu hình API.
- Mở **hai terminal PowerShell**: một cho API và một cho Flutter. Các lệnh dưới đây chạy từ thư mục gốc của repository, trừ khi có chỉ dẫn khác.

## 1. Chạy API

Trong terminal thứ nhất:

```powershell
cd demo
if (-not (Test-Path src/main/resources/application.properties)) {
  Copy-Item src/main/resources/application.properties.example src/main/resources/application.properties
}
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$rng.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
.\mvnw.cmd spring-boot:run
```

Nếu đã có `application.properties` trên máy, lệnh trên sẽ giữ nguyên tệp đó. Với cấu hình mẫu, biến `JWT_SECRET` phải được đặt trong **cùng terminal** mỗi lần khởi động API. Khi đổi khóa JWT, các phiên đăng nhập cũ sẽ hết hiệu lực.

API chạy tại `http://localhost:8080`. Thử `http://localhost:8080/api/books` trong trình duyệt. Nếu cổng 8080 đang được sử dụng, dừng tiến trình API cũ trước khi chạy lại.

## 2. Chạy Flutter

Trong terminal thứ hai, từ thư mục gốc:

```powershell
cd flutter_app
flutter pub get
flutter run -d chrome
```

Nếu muốn chạy ứng dụng Windows, dùng `flutter run -d windows`. Windows cần bật **Developer Mode** để Flutter tạo symlink cho plugin. Android Emulator dùng địa chỉ API mặc định `http://10.0.2.2:8080/api`; Chrome và Windows dùng `http://localhost:8080/api`. Với điện thoại thật cùng mạng Wi-Fi, chạy:

```powershell
flutter run --dart-define=API_BASE_URL=http://IP_MAY_TINH:8080/api
```

Thay `IP_MAY_TINH` bằng địa chỉ LAN của máy chạy API. Khi sửa mã Flutter, nhấn `r` trong terminal để hot reload; khi sửa API, dừng bằng `Ctrl+C` rồi chạy lại lệnh Maven.

## Tạo tài khoản và sử dụng

Màn hình Flutter hiện chỉ có đăng nhập; tài khoản mới được tạo qua API. Khi **API đã chạy**, mở terminal PowerShell khác ở **bất kỳ thư mục nào** và chạy:

```powershell
$account = @{
  username = 'demo_user'
  email = 'demo_user@example.com'
  password = 'DemoPass123!'
  fullName = 'Người dùng thử'
} | ConvertTo-Json
Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/register' -Method Post -ContentType 'application/json; charset=utf-8' -Body $account
```

Đổi `username`, `email` và `password` cho tài khoản của bạn; tên đăng nhập cần ít nhất 4 ký tự và mật khẩu ít nhất 8 ký tự. Sau đó nhập tên đăng nhập (hoặc email) và mật khẩu trên màn hình Flutter. Không có tài khoản hoặc mật khẩu mặc định trong repository.

Luồng kiểm tra: **Đăng nhập → Sách → thêm sách còn kinh doanh vào giỏ → Giỏ hàng → chỉnh số lượng, nhập địa chỉ và số điện thoại → Đặt hàng → Đơn hàng**. Sách ngừng kinh doanh hoặc hết hàng không thể thêm vào giỏ. Token đăng nhập và giỏ hàng được lưu trên thiết bị. Đơn hàng và tồn kho được lưu trong cơ sở dữ liệu Access.

## Kiểm tra mã nguồn

```powershell
cd demo
.\mvnw.cmd test
```

```powershell
cd flutter_app
flutter analyze
flutter test
```

Các lệnh trên chạy riêng trong thư mục tương ứng. Mã Flutter đang được dùng nằm ở `flutter_app/lib/`; các tệp `.dart` ở thư mục gốc là bản tham khảo cũ.

## Lưu ý về dữ liệu

Repository không chứa `*.accdb` hoặc `demo/src/main/resources/application.properties` vì chúng có thể chứa tài khoản, đơn hàng và khóa JWT. Để chạy sau khi clone, cần cung cấp cơ sở dữ liệu Access tương thích và tạo cấu hình từ `application.properties.example` theo phần **Chạy API**.
