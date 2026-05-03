# BBB (Form Management API)

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

3. Dừng/Remove:

```bash
docker compose down
```

## Chạy local (không dùng Docker)

1) Tạo database MySQL (nếu chưa có):

```sql
CREATE DATABASE form_management;
```

2) Thiết lập biến môi trường (Windows examples):

CMD:

```bat
set SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/form_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
set SPRING_DATASOURCE_USERNAME=root
set SPRING_DATASOURCE_PASSWORD=root
```

PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3306/form_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
$env:SPRING_DATASOURCE_USERNAME='root'
$env:SPRING_DATASOURCE_PASSWORD='root'
```

3) Chạy ứng dụng bằng Maven Wrapper:

```bat
.\mvnw.cmd spring-boot:run
```

Hoặc build jar rồi chạy:

```bat
.\mvnw.cmd -DskipTests clean package
java -jar target\bbb-0.0.1-SNAPSHOT.jar
```

## Ghi chú về schema và migration

Lưu ý: trước đây `submissions` có cột `submitted_by`. Repository hiện tại đã loại bỏ trường `submittedBy` khỏi entity. Để tránh lỗi khi DB cũ vẫn có cột này, ứng dụng có `SchemaMigrationRunner` sẽ cố gắng drop cột `submitted_by` khi khởi động nếu có quyền `ALTER TABLE`.

Nếu DB không cho phép thay đổi schema tự động, bạn có thể chạy thủ công:

```sql
ALTER TABLE submissions DROP COLUMN submitted_by;
```

## Kiểm tra nhanh

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Hướng dẫn API (tóm tắt)

Base URL (local): `http://localhost:8080`
### Enums
- `FormStatus`: `ACTIVE`, `DRAFT`
- `FieldType`: `TEXT`, `NUMBER`, `DATE`, `COLOR`, `SELECT`
### Phân trang (Spring Data)
Các API trả về `Page<...>` hỗ trợ query params:
- `page` (mặc định 0)
- `size` (mặc định 20)
- `sort` (ví dụ: `sort=displayOrder,asc`)

### Định dạng lỗi chung
```json
{
	"message": "Validation failed",
	"field": "title",
	"error": "title is required"
}
```

### Endpoints chính

- `GET /api/forms` (paged) — danh sách forms (tóm tắt)
- `GET /api/forms/active` — trả về danh sách forms **chi tiết** (kèm `fields`)
- `GET /api/forms/{id}` — chi tiết 1 form
- `POST /api/forms` — tạo form
- `PUT /api/forms/{id}` — cập nhật form
- `DELETE /api/forms/{id}` — xóa form

- `POST /api/forms/{formId}/fields` — thêm field
- `PUT /api/forms/{formId}/fields/{fieldId}` — cập nhật field
- `DELETE /api/forms/{formId}/fields/{fieldId}` — xóa field

- `POST /api/forms/{formId}/submit` — nộp form
- `GET /api/forms/{formId}/submissions` — lấy submission của 1 form (paged)
- `GET /api/submissions?formId={formId}` — lấy tất cả submissions (có thể filter theo formId)

### Payload mẫu & rules

1) Tạo/Update Form (`FormUpsertRequest`):

```json
{
	"title": "Registration Form",
	"description": "Example form",
	"displayOrder": 1,
	"status": "ACTIVE",
	"fields": [
		{"label":"Full Name","type":"TEXT","displayOrder":1,"required":true},
		{"label":"Age","type":"NUMBER","displayOrder":2,"required":false}
	]
}
```

2) Thêm/Update Field (`FormFieldUpsertRequest`):

```json
{
	"label": "Country",
	"type": "SELECT",
	"displayOrder": 3,
	"required": false,
	"optionsJson": "[\"US\", \"CA\", \"VN\"]"
}
```

3) Submit form (`SubmitFormRequest`) — lưu ý: body chỉ còn `values` (map key là `fieldId` dạng string):

```json
{
	"values": {
		"1": "Nguyễn Văn A",
		"2": 30,
		"3": "#00A1FF",
		"4": "VN",
		"5": "Hà Nội"
	}
}
```

Quy tắc validate theo `FieldType`:

- `TEXT`: chuỗi, tối đa 200 ký tự
- `NUMBER`: số trong khoảng 0..100
- `DATE`: ISO `yyyy-MM-dd`, không được ở quá khứ
- `COLOR`: HEX `#RRGGBB` (ví dụ `#00A1FF`)
- `SELECT`: phải là 1 trong các giá trị trong `optionsJson`

Ghi chú:

- Key trong `values` phải là `fieldId` theo form (dạng string). Ví dụ: `"1"`.
- Field `required=true` bắt buộc có giá trị (không null/chuỗi rỗng).
- Nếu `values` chứa fieldId không thuộc form → API trả 400 (Unknown field id...).

Ví dụ submit cho `formId=1` (theo cấu trúc form bạn cung cấp):

```bash
curl -X POST "http://localhost:8080/api/forms/1/submit" \
	-H "Content-Type: application/json" \
	-d '{
		"values": {
			"1": "Nguyễn Văn A",
			"2": 30,
			"3": "#00A1FF",
			"4": "VN",
			"5": "Hà Nội"
		}
	}'
```

## Postman

Collection đã được cập nhật và nằm tại: [postman/FormManagement.postman_collection.json](postman/FormManagement.postman_collection.json)

## Chạy test

```bat
.\mvnw.cmd test
```

## Troubleshooting nhanh

- Lỗi kết nối DB: kiểm tra `SPRING_DATASOURCE_URL` và MySQL đang chạy
- Port 8080 bị chiếm: thay `SERVER_PORT` hoặc tắt ứng dụng đang chiếm
- Nếu DB có cột `submitted_by` và app không có quyền `ALTER`: hãy drop cột thủ công (hoặc cấp quyền) bằng câu lệnh SQL ở phần "Ghi chú về schema và migration".


