# 🔐 Authentication Flow & Authorization

> Mô tả cách **Frontend – Backend** phối hợp qua bộ API Auth và bảng phân quyền API

---

## 📋 Tổng quan Authentication Flow

| Bước | Trạng thái             | Mô tả                                              |
|------|------------------------|----------------------------------------------------|
| 1    | Chưa đăng nhập         | Gọi API login, nhận `accessToken` + `refreshToken` |
| 2    | Đã đăng nhập           | Gọi các API khác với `accessToken` trong header    |
| 3    | `accessToken`  hết hạn | Server trả `401 Unauthorized` khi gọi các API      |
| 4    | Silent Refresh         | Đổi `accessToken` mới bằng `refreshToken`          |
| 5    | `refreshToken` hết hạn | Xóa `accessToken`, điều hướng về trang login       |

---

### Bước 1 · Đăng nhập

> ⚠️ Khi chưa đăng nhập, mọi API khác đều bị chặn `401 Unauthorized`

**Request**

```
POST /api/auth/login
```

```json
{
  "username": "taimv2405",
  "password": "123456789"
}
```

**Response `200 OK`**

| Nơi trả về  | Nội dung                                                  |
|-------------|-----------------------------------------------------------|
| Body (JSON) | `accessToken` + thông tin nhân viên                       |
| Cookie (ẩn) | `refreshToken` — Server tự gắn vào trình duyệt (HttpOnly) |

**✅ Frontend cần làm:**

1. Lưu `accessToken` vào LocalStorage
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

---

### Bước 3 · Access Token hết hạn

> 🕐 `accessToken` mặc định **hết hạn sau 15 phút**

Khi gửi request với token cũ, Server trả về `401 Unauthorized`

---

### Bước 4 · Làm mới Token (Silent Refresh)

> ❗ Khi nhận `401`, **không đẩy người dùng về trang Login ngay** — thử đổi token ngầm trước.

**Request**

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

### Bước 5 · Kết thúc phiên làm việc

Khi `/refresh` cũng thất bại:

1. Xóa `accessToken` khỏi LocalStorage
2. Chuyển hướng người dùng về `/login`

---

### 📌 Bảng tổng kết API

| Chức năng         | Method | Endpoint            | Xác thực               |
|-------------------|--------|---------------------|------------------------|
| Đăng nhập         | `POST` | `/api/auth/login`   | Không                  |
| Làm mới token     | `POST` | `/api/auth/refresh` | Không (Cookie)         |
| Thông tin cá nhân | `GET`  | `/api/auth/me`      | `Bearer <accessToken>` |

## 📋 Phân quyền

**Cách hoạt động:**

- Quyền của nhân viên được mã hóa trong `accessToken`
- Khi server đọc `accessToken` được gửi kèm trong API header nó sẽ biết staff đang gửi API có quyền gì
- Từ đó server hoặc cho phép staff gọi API hoặc chặn lại, trả về `403 Fobidden`

**Public:**

| Method | Endpoint            |
|--------|---------------------|
| `POST` | `/api/auth/login`   |
| `POST` | `/api/auth/refresh` |

**Private:**

| Method   | Endpoint                     | STYLIST | RECEPTIONIST | MANAGER |
|----------|------------------------------|:-------:|:------------:|:-------:|
| `POST`   | `/api/auth/login`            |    ✅    |      ✅       |    ✅    |
| `POST`   | `/api/auth/refresh`          |    ✅    |      ✅       |    ✅    |
| `POST`   | `/api/staffs/**`             |    ❌    |      ❌       |    ✅    |
| `DELETE` | `/api/customers/**`          |    ❌    |      ❌       |    ✅    |
| `POST`   | `/api/appointments/**`       |    ❌    |      ✅       |    ✅    |
| `PATCH`  | `/api/appointments/*/cancel` |    ❌    |      ✅       |    ✅    |
| `*`      | Các API còn lại              |    ✅    |      ✅       |    ✅    |