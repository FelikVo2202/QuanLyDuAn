# MixedupIS
This is repo for academic subject in school include IS208, IS210, IS201, IS216

---

## ⚙️ Backend

---

### 🛠 Yêu cầu cài đặt

* **Tải [IntelliJ IDEA Ultimate](https://www.jetbrains.com/idea/download/)** > [Đăng ký bản quyền miễn phí](https://www.jetbrains.com/shop/eform/students) > *Mở app > Icon bánh răng > Manage Subscription > Activate*
* **Tải [Docker Desktop](https://www.docker.com/products/docker-desktop/)** (Chạy database Oracle)

---

### 🚀 Hướng dẫn chạy

#### Bước 1: Chạy database Oracle

Mở terminal tại thư mục gốc > `docker-compose up -d` để tạo database (Lần đầu hơi lâu tý)

---

#### Bước 2: Cấu hình IntelliJ

##### 2.0 Cài đặt JDK
1. Mở code
2. Vào **File > Project Structure > SDK > Download JDK**
3. Chọn Version: **21** > Vendor: **Eclipse Temurin** > Download

##### 2.1 Maven Project
- Nếu icon của file `/backend/pom.xml` không chuyển thành chữ m: Right click > Add as Maven Project

##### 2.2 Bật Annotation Processing
1. Vào **Settings > Build, Execution, Deployment > Compiler > Annotation Processors**
2. Tick chọn ✅ **Enable annotation processing**

##### 2.3 Nạp biến môi trường
1. Copy file `.env.example` và đổi tên bản sao thành `.env`. (Các biến giữ nguyên giá trị mặc định hoặc đổi tùy thích.)
2. Nhấn vào **tên file main** ở góc trên bên phải > **Edit Configurations**
3. Chọn **Modify options > Environment variables**
4. Trỏ đến file **`.env`** đã tạo ở Bước 1

##### 2.4 Kết nối Database
1. Mở tab **Database** ở góc phải màn hình
2. Chọn **Create data source > Oracle**
3. Nếu thấy thông báo **"Missing driver files"** > nhấn Download để tải driver
4. Điền thông tin kết nối:
    - **URL**: copy từ `src/main/resources/application.yaml`
    - **Username / Password**: copy từ file `.env`
5. Nhấn **Test Connection** để kiểm tra > **OK**

---

#### Bước 3: Chạy & Kiểm tra
1. Chạy bằng nút hình tam giác màu xanh lá.
2. Truy cập: [http://localhost:8080/](http://localhost:8080/) (Thấy trang **"Whitelabel Error Page"** là được.)

---

### 🛢️ Làm việc với database

#### 🟢 Không mất dữ liệu

* **Vào code:** Mở Docker Desktop > Mở code > `docker compose up -d`
* **Tạm nghỉ ngắn:** `docker compose stop` > `docker compose start`
* **Trước khi tắt máy:** `docker compose down`
* **Thu hồi tài nguyên (Khi cần làm tác vụ nặng khác):** `docker compose down` > Right click Docker dưới khay hệ thống > **Quit Docker Desktop** > `wsl --shutdown`

---

#### 🔴 Mất dữ liệu

* **Database lỗi:** `docker compose down -v` > `docker compose up -d`
