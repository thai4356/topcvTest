# (Form Management API)

Dự án Spring Boot (Java 21) cung cấp REST API để quản lý Form, Field và Submission.

Mục tiêu README này: hướng dẫn cài đặt, chạy nhanh (Docker / local), và mô tả API (endpoints, payload mẫu, lỗi thường gặp).

## Yêu cầu

- Java 21 (JDK 21)
- Maven (nếu không dùng `mvnw`) hoặc dùng `mvnw` có sẵn trong repo
- (Tuỳ chọn) Docker & Docker Compose để chạy MySQL + app nhanh

## Cấu hình

Ứng dụng đọc một số cấu hình từ biến môi trường (có giá trị mặc định trong `src/main/resources/application.properties`):

- `SPRING_DATASOURCE_URL` — kết nối JDBC (mặc định: `jdbc:mysql://localhost:3306/form_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`)
- `SPRING_DATASOURCE_USERNAME` — (mặc định: `root`)
- `SPRING_DATASOURCE_PASSWORD` — (mặc định: `root`)
- `SPRING_JPA_HIBERNATE_DDL_AUTO` — (mặc định: `update`)
- `SERVER_PORT` — (mặc định: `8080`)

## Chạy bằng Docker Compose (khuyến nghị)

1. Khởi động MySQL và app:

```bash
docker compose up --build
```

2. Mặc định:

- MySQL: `localhost:3306` (root/password: `password` theo `docker-compose.yml`)
- App: `http://localhost:8080`

nhớ là các cổng đang bị chiểm phải tắt đi

3. Dừng/Remove:

```bash
docker compose down
```

## Chạy local (không dùng Docker)

1) Tạo database MySQL (nếu chưa có):

```sql
CREATE DATABASE form_management;
```
rồi import file sql trong bài 

2) Thiết lập biến môi trường (Windows examples):

CMD:

Cài apache maven + jdk 21 + mysql rồi cài đặt biến môi trường ( thường thì khi cài đặt thì path sẽ auto được thêm vào - có thể xem video hướng dẫn trên mạng để cài)

3) Chạy ứng dụng bằng Maven Wrapper:

bật cmd ( phải chuẩn đường dẫn project - nơi chứa file src chứ không phải bên trong src)
mvn clean install
mvn spring-boot:run

## Ghi chú về schema và migration

## Kiểm tra nhanh

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
Hướng dẫn API (theo Postman collection)

Base URL:
http://localhost:8080

Chuẩn: JSON, UTF-8

1. Form

1.1 Lấy danh sách form (có phân trang)

GET /api/forms?page=0&size=10

Mô tả: Trả về danh sách form theo trang (Page). Tham số query:
- `page` — trang (0-based)
- `size` — số phần tử/trang
- `sort` — (tuỳ chọn) ví dụ `title,asc`

Response: HTTP 200 — JSON dạng Page với các trường `content`, `totalElements`, `totalPages`, `pageNumber`.

1.2 Tạo form

POST /api/forms

Request body (ví dụ):

{
  "title": "Form 1",
  "description": "First test form",
  "displayOrder": 1,
  "status": "ACTIVE",
  "fields": [
    {
      "label": "Full Name",
      "type": "TEXT",
      "displayOrder": 1,
      "required": true
    },
	{
      "label": "Age",
      "type": "NUMBER",
      "displayOrder": 2,
      "required": true
    },
	{
      "label": "Favourite Color",
      "type": "TEXT",
      "displayOrder": 3,
      "required": false
    }
  ]
}

**Quy tắc quan trọng

- `displayOrder` phải unique trong cùng cấp.
- `submission` lưu dạng map: fieldId → value.

**Field types & validation**

- `TEXT` — chuỗi ký tự; tối đa 200 ký tự. Nếu vượt quá sẽ trả về lỗi validate.
- `NUMBER` — số hợp lệ (parse được thành BigDecimal); giá trị phải trong khoảng `0` đến `100` (bao gồm).
- `DATE` — chuỗi theo định dạng ISO `yyyy-MM-dd`; ngày không được là quá khứ (so với ngày hiện tại).
- `COLOR` — mã HEX hợp lệ theo mẫu `#RRGGBB` (ví dụ `#00A1FF`); lưu trữ ở dạng chữ hoa.
- `SELECT` — bắt buộc có `optionsJson` (một JSON array chứa các option chuỗi). Giá trị gửi lên phải là một trong các option đã cấu hình; nếu `optionsJson` không hợp lệ hoặc không chứa giá trị sẽ trả lỗi validate.

Lưu ý về lỗi:
- Các lỗi validate field sẽ ném `FieldValidationException` và trả về `400 Bad Request` với chi tiết lỗi (xem `ApiErrorResponse`).
- Vi phạm ràng buộc duy nhất (ví dụ `displayOrder`) có thể trả `409 Conflict`.

Mô tả: Tạo form mới (có thể kèm danh sách field). Response: `201 Created` + body là Form DTO; header `Location` chỉ tới tài nguyên mới.

Lưu ý validate:
- `displayOrder` phải unique trong cùng cấp.
- `SELECT` field bắt buộc có `optionsJson` (mảng các option).

1.3 Lấy form theo id

GET /api/forms/{id}

Mô tả: Trả về chi tiết form (kèm fields). Response: `200 OK` hoặc `404 Not Found` nếu không tồn tại.

1.4 Cập nhật form

PUT /api/forms/{id}

Request body (ví dụ cập nhật):

{
  "title": "Updated Form Name",
  "description": "Updated description",
  "status": "ACTIVE",
  "displayOrder": 2
}

Mô tả: Cập nhật thông tin form. Response: `200 OK` + updated Form DTO.

1.5 Xoá form

DELETE /api/forms/{id}

Mô tả: Xoá form. Response: `204 No Content` hoặc `404 Not Found` nếu không tồn tại.

Các lỗi phổ biến:
- `400 Bad Request` — lỗi validate dữ liệu (xem `ApiErrorResponse`).
- `404 Not Found` — tài nguyên không tồn tại.
- `409 Conflict` — vi phạm ràng buộc duy nhất (ví dụ `displayOrder`).

Ghi chú chung:
- Field types hỗ trợ: `TEXT`, `NUMBER`, `COLOR`, `SELECT`.
- `displayOrder` phải unique trong cùng cấp để đảm bảo thứ tự hiển thị.
- Xem Swagger UI (`/swagger-ui.html`) hoặc OpenAPI (`/v3/api-docs`) để biết chi tiết contract.

2. Field
2.1 Thêm field vào form

POST /api/forms/{formId}/fields

{
  "label": "Sex",
  "type": "SELECT",
  "required": true,
  "displayOrder": 4,
  "optionsJson": "[\"Male\", \"Female\", \"Other\"]"
}

nó là gì: thêm field vào form
để làm gì: mở rộng cấu trúc form

2.2 Update field

PUT /api/forms/{formId}/fields/{fieldId}

{
  "label": "Country of Residence",
  "required": true
}

nó là gì: cập nhật field
để làm gì: sửa label / required / config

2.3 Xoá field

DELETE /api/forms/{formId}/fields/{fieldId}

nó là gì: xoá field
để làm gì: loại bỏ field không cần

3. Submission
3.1 Lấy form active

GET /api/forms/active

nó là gì: lấy danh sách form đang bật
để làm gì: hiển thị cho user điền

3.2 Submit form

POST /api/forms/{formId}/submit

{
  "values": {
    "1": "Nguyễn Văn A",
    "2": 30,
    "3": "#00A1FF"
  }
}

nó là gì: gửi dữ liệu form
để làm gì: lưu câu trả lời của user

3.3 Lấy submission

GET /api/forms/{formId}/submissions

nó là gì: lấy toàn bộ dữ liệu đã submit
để làm gì: admin xem kết quả



5. Luồng chuẩn hệ thống

tạo form → thêm field → publish (ACTIVE) → user submit → admin xem submission


