# 🔐 Authentication Flow & Authorization

> Mô tả cách **Frontend – Backend** phối hợp qua bộ API Auth, bảng phân quyền API và hướng dẫn Test API trên giao diện Scalar

---

## 📋 Tổng quan Authentication Flow

| Bước | Trạng thái             | Mô tả                                                        |
|------|------------------------|--------------------------------------------------------------|
| 1    | Chưa đăng nhập         | Gọi API login, nhận `accessToken` + `refreshToken`           |
| 2    | Đã đăng nhập           | Gọi các API khác với `accessToken` trong header              |
| 3    | `accessToken`  hết hạn | Server trả `401 Unauthorized` khi gọi các API                |
| 4    | Silent Refresh         | Đổi `accessToken` mới bằng `refreshToken`                    |
| 5    | `refreshToken` hết hạn | Gọi API logout, xóa `accessToken`, điều hướng về trang login |

---

### Bước 1 · Đăng nhập

> ⚠️ Khi chưa đăng nhập, mọi API khác đều bị chặn `401 Unauthorized`

**Request**

💡 Bắt buộc dùng { withCredentials: true } để trình duyệt tự gửi Cookie lên.

```
POST /api/auth/login
```

```json
{
  "username": "manager",
  "password": "123456"
}
```

**Response `200 OK`**

| Nơi trả về  | Nội dung                                                  |
|-------------|-----------------------------------------------------------|
| Body (JSON) | `accessToken` + thông tin nhân viên                       |
| Cookie (ẩn) | `refreshToken` — Server tự gắn vào trình duyệt (HttpOnly) |

**✅ Cần:**

1. Lưu `accessToken` vào biến JavaScript
2. Bỏ qua `refreshToken` — trình duyệt tự quản lý

---

### Bước 2 · Gọi API bình thường

Gắn header sau vào **mọi request** sau khi đăng nhập:

```
Authorization: Bearer <accessToken>
```

**Ví dụ — Lấy thông tin cá nhân:**

```
GET /api/auth/me
```

> Server tự nhận diện người dùng từ token — không cần truyền ID.

---

### Bước 3 · Access Token hết hạn

> 🕐 `accessToken` mặc định **hết hạn sau 15 phút**

Khi gửi request với token cũ, Server trả về `401 Unauthorized`

---

### Bước 4 · Làm mới Token (Silent Refresh)

> ❗ Khi nhận `401`, **không đẩy người dùng về trang Login ngay** — thử đổi token ngầm trước bằng Axios Interceptor.

**Request**

💡 Bắt buộc dùng { withCredentials: true } để trình duyệt tự gửi Cookie lên.

```
POST /api/auth/refresh
```

- Không cần Body
- Không cần header `Authorization`

**Kết quả:**

| Response           | Hành động                                                     |
|--------------------|---------------------------------------------------------------|
| `200 OK`           | Lưu `accessToken` mới → Thực hiện lại request bị lỗi ở Bước 3 |
| `401 Unauthorized` | `refreshToken` hết hạn → Chuyển sang Bước 5                   |

---

### Bước 5 · Kết thúc phiên làm việc (Đăng xuất hoặc Refresh thất bại)

**Request**

💡 Bắt buộc dùng { withCredentials: true } để trình duyệt tự gửi Cookie lên.

```
POST /api/auth/logout
```

**Response `200 OK`**

**✅ Cần:**

1. Xóa biến accessToken (gán bằng chuỗi rỗng)
2. Chuyển hướng người dùng về trang `/login`

---

### 📌 Bảng tổng kết API

| Chức năng         | Method | Endpoint            | Xác thực               |
|-------------------|--------|---------------------|------------------------|
| Đăng nhập         | `POST` | `/api/auth/login`   | Không                  |
| Làm mới token     | `POST` | `/api/auth/refresh` | Không (Cookie)         |
| Đăng xuất         | `POST` | `/api/auth/logout`  | Không (Cookie)         |
| Thông tin cá nhân | `GET`  | `/api/auth/me`      | `Bearer <accessToken>` |

## 📋 Phân quyền

**Cách hoạt động:**

- Quyền của nhân viên được mã hóa trong `accessToken`
- Khi server đọc `accessToken` được gửi kèm trong API header nó sẽ biết staff đang gửi API có quyền gì
- Từ đó server hoặc cho phép staff gọi API hoặc chặn lại, trả về `403 Forbidden`

**Public:**

| Method | Endpoint            |
|--------|---------------------|
| `POST` | `/api/auth/login`   |
| `POST` | `/api/auth/refresh` |
| `POST` | `/api/auth/logout`  |

**Private:**

| Method   | Endpoint                     | STYLIST | RECEPTIONIST | MANAGER |
|----------|------------------------------|:-------:|:------------:|:-------:|
| `*`      | `/api/staffs/**`             |    ❌    |      ❌       |    ✅    |
| `DELETE` | `/api/customers/**`          |    ❌    |      ❌       |    ✅    |
| `POST`   | `/api/appointments/**`       |    ❌    |      ✅       |    ✅    |
| `PATCH`  | `/api/appointments/*/cancel` |    ❌    |      ✅       |    ✅    |
| `*`      | Các API còn lại              |    ✅    |      ✅       |    ✅    |

## 🚀 Hướng dẫn Test API trên giao diện Scalar

Scalar cung cấp Client tích hợp sẵn để bạn test nhanh các API. Dưới đây là luồng thao tác:

### 0. Khởi động

Sau khi chạy backend, mở [http://localhost:8080/scalar](http://localhost:8080/scalar)

### 1. Lấy Access Token

- Lấy username/password:
    - password: `123456`
    - username: IntelliJ > Thanh công cụ bên phải > Database 🛢️ > SALON_APP > tables > đúp chuột STAFF > Thấy danh sách
      nhân viên > Copy một cái username của MANAGER
- Mở endpoint `POST /api/auth/login` trên Scalar.
- Bấm **Test Request**
- Nhập Request Body chứa username/password rồi bấm **Send**
- Trong khung Response Body, copy toàn bộ chuỗi `accessToken`.

> **Lưu ý:** `refreshToken` đã được set ngầm vào Cookie của trình duyệt, bạn không cần bận tâm.

### 2. Gắn Token để gọi API Private

- Lên đầu trang Scalar
- Paste `accessToken` vào Authentication > Bearer Token để Scalar tự điền `Authorization: Bearer <accessToken>` vào
  Header cho toàn bộ API (đỡ phải nhập thủ công mỗi lần gọi API)

> **Lưu ý:** Chỉ dán mã token, **KHÔNG** cần tự gõ chữ `Bearer` vì Scalar sẽ tự động nối vào header.

### 3. Test gọi API

Sau khi đã gắn Token thành công, bạn có thể bắt đầu test các API trong hệ thống.

* **Bước 3.1: Chọn API & Mở giao diện Test**
    * Chọn một endpoint bất kỳ từ menu bên trái (Ví dụ: `GET /api/customers` hoặc `POST /api/appointments`).
    * Bấm nút **Test Request** để mở khung giao diện Client tích hợp.


* **Bước 3.2: Chuẩn bị Dữ liệu (Parameters & Body)**

  Tùy thuộc vào từng API, bạn sẽ cần (hoặc không cần) truyền thêm dữ liệu. Hãy chú ý các ô sau trên Scalar:
    * **Không cần nhập gì cả:** Rất nhiều API chỉ cần gọi là chạy (Ví dụ: `GET /api/auth/me` hoặc lấy danh sách cơ bản).
      Nếu không thấy ô nhập liệu nào bắt buộc, bạn có thể bỏ qua bước này và bấm Send luôn.
    * **Path Variables:** Các biến bắt buộc nằm trực tiếp trong URL (Ví dụ: với `/api/customers/{id}`, bạn cần nhập giá
      trị thực tế thay cho `id`).
    * **Request Body:** Dữ liệu định dạng JSON gửi lên server (thường dùng với `POST`, `PUT`, `PATCH`). *Mẹo: Scalar
      thường đã sinh sẵn một cục JSON mẫu, bạn chỉ cần sửa lại các giá trị cho đúng thực tế.*
    * **Query Parameters:** Các tham số dùng trên URL để lọc, tìm kiếm hoặc phân trang.
      *💡 Ví dụ với API `GET /api/appointments`:*

      | `startDate`     | `endDate`    | Kết quả trả về                                      |
      |-----------------|--------------|-----------------------------------------------------|
      | ❌ Bỏ trống      | ❌ Bỏ trống   | Các cuộc hẹn của **hôm nay**                        |
      | ✅ Có nhập       | ❌ Bỏ trống   | Các cuộc hẹn **chính xác trong ngày** `startDate`   |
      | ❌ Bỏ trống      | ✅ Có nhập    | Các cuộc hẹn từ **hôm nay** đến `endDate`           |
      | ✅ Có nhập       | ✅ Có nhập    | Các cuộc hẹn từ **`startDate`** đến **`endDate`**   |

* **Bước 3.3: Gửi Request & Đọc Kết quả**
    * Bấm nút **Send** để thực thi.
    * Nhìn xuống khu vực **Response** để kiểm tra mã trạng thái (HTTP Status Code) và kết luận:
        * ✅ `200 OK` / `201 Created`: Gọi thành công! Nếu đây là API Private, chứng tỏ Token của bạn hợp lệ và tài khoản
          đủ quyền (Role).
        * ❌ `400 Bad Request`: Dữ liệu bạn nhập vào bị sai (sai định dạng, thiếu field bắt buộc, hoặc vi phạm validate).
          Hãy đọc dòng thông báo lỗi trong body để sửa lại.
        * 🚫 `401 Unauthorized`: Token bị thiếu, bị sai hoặc đã hết hạn. Bạn cần làm mới token (bằng cách refresh hoặc
          login)
        * ⛔ `403 Forbidden`: Token hợp lệ nhưng tài khoản của bạn **không đủ quyền** thực hiện hành động này (Ví dụ: Lấy
          account Stylist đi xóa thông tin Customer).
        * 💥 `500 Internal Server Error`: Lỗi chưa được cover. Quay lại IntelliJ xem log lỗi.
      
      > **Lưu ý:** Quen rồi thì vào `backend/src/main/resources/application.yaml` chỉnh
      access-token-expiration lên lâu lâu thay vì 15m để tập trung code khỏi phải đổi token hoài. Nhưng nhớ đưa về
      15m trước khi tạo Pull Request.