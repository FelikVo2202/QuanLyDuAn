# MixedupIS
This is repo for academic subject in school include IS208, IS210, IS201, IS216

---

# 💇‍♂️ Hệ thống Quản lý Salon

Dự án Hệ thống Quản lý Salon (Fullstack) được xây dựng với **Spring Boot** (Backend), **Oracle Database** (Database), và **Nginx** (Web Server). Toàn bộ hệ thống được đóng gói và vận hành hoàn toàn tự động thông qua **Docker Compose**.

## ✨ Các tính năng chính
* 📅 Quản lý đặt lịch cắt tóc/dịch vụ.
* 👥 Quản lý nhân viên, thợ cắt tóc.
* 💇 Quản lý danh mục dịch vụ và sản phẩm.
* 📊 Thống kê doanh thu và báo cáo.

## 🚀 Công nghệ sử dụng
- **Backend:** Java 21, Spring Boot 4.x, Spring Data JPA, Spring Security (JWT), SpringDoc (OpenAPI/Scalar).
- **Frontend:** HTML/CSS/JS (Hosted by Nginx Alpine).
- **Database:** Oracle Database 23c Free (Slim Faststart).
- **Deployment:** Docker & Docker Compose (Multi-stage build).

---

## 📂 Cấu trúc thư mục

```text
QuanLyDuAn/
├── backend/                # Ứng dụng Spring Boot
│   ├── src/                # Mã nguồn
│   ├── Dockerfile          # Cấu hình build image cho Backend (Multi-stage)
│   ├── pom.xml             # Maven dependencies
│   └── README.md           # Hướng dẫn setup và chạy Backend trên IntelliJ
├── docs/                   # Tài liệu kỹ thuật
├── frontend/               # Mã nguồn Giao diện (HTML, CSS, JS)
├── .env.example            # File mẫu chứa các biến môi trường
├── .gitignore              # Cấu hình các file/thư mục bị Git bỏ qua
├── docker-compose.yml      # Cấu hình chạy toàn bộ hệ thống
└── README.md               # Tài liệu hướng dẫn (Bạn đang đọc nó)
```

## ⚙️ Hướng dẫn cài đặt và chạy dự án (Local)

### 1. Yêu cầu hệ thống

Đảm bảo máy tính của bạn đã cài đặt:
- **Git**
- **Docker Desktop** (Đã khởi động).
- **Terminal** hoặc **Git Bash**.

### 2. Tải mã nguồn
Mở Terminal và chạy các lệnh sau để tải project về máy:
```bash
git clone https://github.com/FelikVo2202/QuanLyDuAn.git
cd QuanLyDuAn
```

### 3. Thiết lập biến môi trường

Hệ thống cần các thông số bảo mật để chạy. Chạy lệnh sau để tạo file cấu hình `.env` từ file mẫu:

Trên Linux/macOS/Git Bash:
```bash
cp .env.example .env
```

Trên Windows Command Prompt:
```cmd
copy .env.example .env
```

> 💡 **Mẹo:** Các giá trị trong `.env` đã được cấu hình sẵn cho môi trường local. Bạn có thể mở file này ra để điều chỉnh nếu cần.

### 4. Build và Khởi chạy hệ thống

Tại thư mục gốc của dự án, chạy lệnh:

```bash
docker-compose --profile fullstack up -d --build
```

Lệnh này sẽ:
- Tải image Oracle và Nginx.
- Build code Java thành ứng dụng Spring Boot.
- Chạy ngầm cả 3 dịch vụ (Database, Backend, Frontend).

> 💡 **Lưu ý:** Lần chạy đầu tiên có thể mất vài phút để Oracle khởi tạo Database. Bạn có thể xem tiến trình khởi động của Backend bằng lệnh:

```bash
docker-compose logs -f backend
```

---

## 🌐 Đường dẫn & Tài khoản

### 📌 Các dịch vụ khả dụng
Sau khi các dịch vụ báo trạng thái **Up**, bạn có thể truy cập hệ thống tại:

| Dịch vụ | Đường dẫn truy cập | Chú thích |
|---|---|---|
| Giao diện Web (Frontend) | http://localhost | Cổng 80 - Chạy bằng Nginx |
| API Backend | http://localhost:8080/api/... | Các endpoint của hệ thống |
| Tài liệu API (Scalar) | http://localhost:8080/scalar | Tài liệu kỹ thuật tự động tạo |
| Kết nối Database (DBeaver) | `jdbc:oracle:thin:@localhost:1521/FREEPDB1` | Dùng user/pass trong file `.env` |

### 🔑 Tài khoản test mặc định
Sử dụng các tài khoản sau để đăng nhập vào hệ thống Web hoặc test API:

| Vai trò | Username | Password |
|---|---|---|
| Quản lý (Manager) | manager | 123456 |
| Nhân viên lễ tân (Receptionist) | receptionist | 123456 |
| Thợ cắt tóc (Stylist) | stylist | 123456 |

---

## 🛠️ Các lệnh quản lý Docker hữu ích

**Dừng hệ thống:**
```bash
docker-compose --profile fullstack stop
```

**Dừng và xóa container/network:**
```bash
docker-compose --profile fullstack down
```

**Xóa hoàn toàn (bao gồm cả volume dữ liệu Oracle) - Dùng khi muốn làm sạch/khôi phục dữ liệu gốc:**
```bash
docker-compose down -v
```