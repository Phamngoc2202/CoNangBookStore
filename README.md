# CoNangBookStore

Ứng dụng quản lý hiệu sách gồm **Spring Boot API** (`backend/`, Java 17, Microsoft Access) và **Flutter** (`frontend/`). Ứng dụng cho phép đăng nhập, xem sách, lưu giỏ hàng trên thiết bị, đặt hàng và xem đơn đã đặt. Khi đặt hàng thành công, API trừ tồn kho; khi hủy hoặc từ chối đơn, API hoàn kho.

## Cấu trúc dự án

```text
CoNangBookStore/
├── backend/                  Spring Boot REST API, xử lý nghiệp vụ và dữ liệu
│   ├── src/main/java/com/example/demo/
│   │   ├── controller/       Nhận request và trả response HTTP
│   │   ├── service/          Đăng nhập, đặt hàng, kiểm tra và cập nhật tồn kho
│   │   ├── repository/       Truy vấn cơ sở dữ liệu Access
│   │   ├── model/ và dto/    Dữ liệu lưu trữ và dữ liệu gửi qua API
│   │   └── config/          JWT, bảo mật và kết nối cơ sở dữ liệu
│   ├── src/main/resources/   Cấu hình mẫu của API
│   └── data/                 Đặt file .accdb tại đây trên máy cá nhân
└── frontend/                 Ứng dụng Flutter
    ├── lib/main.dart         Khởi động app và màn hình đăng nhập
    ├── lib/home_page.dart    Màn hình sách, giỏ hàng và đơn hàng
    ├── lib/services/         Gọi API và lưu token, giỏ hàng cục bộ
    ├── lib/models/           Kiểu dữ liệu sách, giỏ hàng, đơn hàng
    └── lib/config/           Địa chỉ API cho web, Windows và Android
```

Luồng chính để giải thích: **Flutter → HTTP `/api/...` → controller → service → repository → Access**. API trả JSON cho Flutter hiển thị. Giỏ hàng và token được lưu cục bộ trên thiết bị; đơn hàng và tồn kho nằm trong Access.

Các phần thường cần trình bày:

- **Đăng nhập:** `frontend/lib/services/auth_service.dart` gọi `/api/auth/login`; `AuthController` và `AuthService` kiểm tra tài khoản, trả JWT. Flutter lưu JWT bằng `shared_preferences` và gửi kèm các request cần đăng nhập.
- **Xem sách:** `BookService` của Flutter gọi `/api/books`; backend đọc bảng `Books` qua `BookController` → `BookService` → `BookRepository`.
- **Giỏ hàng:** `frontend/lib/services/cart_store.dart` lưu giỏ theo người dùng bằng `shared_preferences`. Thêm vào giỏ chưa tạo đơn và chưa trừ kho.
- **Đặt hàng:** `frontend/lib/services/order_service.dart` gửi giỏ tới `/api/orders`. `OrderService` của backend kiểm tra và trừ số lượng sách khi tạo đơn; hủy hoặc từ chối đơn sẽ hoàn kho. Danh sách đơn của người dùng lấy từ `/api/orders/my-orders`.

## Chuẩn bị

- Cài **JDK 17** và **Flutter SDK**; kiểm tra bằng `java -version` và `flutter doctor`.
- Có tệp cơ sở dữ liệu Microsoft Access đúng cấu trúc dự án tại `backend/data/bookstore1.accdb`. Tệp này chứa dữ liệu cục bộ nên không được đưa lên GitHub. Nếu dùng đường dẫn khác, sửa `store.database.path` trong cấu hình API.
- Mở **hai terminal PowerShell**: một cho API và một cho Flutter. Các lệnh dưới đây chạy từ thư mục gốc của repository, trừ khi có chỉ dẫn khác.

## 1. Chạy API

Trong terminal thứ nhất:

```powershell
cd backend
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
cd frontend
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
cd backend
.\mvnw.cmd test
```

```powershell
cd frontend
flutter analyze
flutter test
```

Các lệnh trên chạy riêng trong thư mục tương ứng. Mã Flutter đang được dùng nằm ở `frontend/lib/`; các tệp `.dart` ở thư mục gốc là bản tham khảo cũ.

## Lưu ý về dữ liệu

Repository không chứa `*.accdb` hoặc `backend/src/main/resources/application.properties` vì chúng có thể chứa tài khoản, đơn hàng và khóa JWT. Để chạy sau khi clone, cần cung cấp cơ sở dữ liệu Access tương thích và tạo cấu hình từ `application.properties.example` theo phần **Chạy API**.
