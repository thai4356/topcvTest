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

## Hướng dẫn API (theo Postman collection)

Base URL (local): `http://localhost:8080`

Collection trong `postman/FormManagement.postman_collection.json` tổ chức theo 3 nhóm: `Form`, `Field`, `Submission`. Dưới đây là mô tả nhanh từng endpoint kèm payload mẫu (dựa trên collection).

---

Nhóm: Form

- `GET /api/forms?page={page}&size={size}` — lấy danh sách (paged)
	- Ví dụ: `GET /api/forms?page=0&size=10`

- `POST /api/forms` — tạo form
	- Payload mẫu:

```json
{
	"title": "Form 1",
	"description": "First test form",
	"displayOrder": 1,
	"status": "ACTIVE",
	"fields": [
		{"label":"Full Name","type":"TEXT","displayOrder":1,"required":true},
		{"label":"Age","type":"NUMBER","displayOrder":2,"required":false},
		{"label":"Favorite Color","type":"COLOR","displayOrder":3,"required":false}
	]
}
```

- `GET /api/forms/{id}` — lấy chi tiết form (kèm `fields`)

- `PUT /api/forms/{id}` — cập nhật form
	- Payload mẫu (phải bao gồm `title`/`displayOrder`/`status`/`fields` nếu muốn cập nhật fields):

```json
{
	"title": "Updated Form Title",
	"description": "Updated description",
	"displayOrder": 1,
	"status": "ACTIVE",
	"fields": [ /* ... */ ]
}
```

- `DELETE /api/forms/{id}` — xóa form

---

Nhóm: Field

- `POST /api/forms/{formId}/fields` — thêm field vào form `formId`
	- Payload mẫu:

```json
{
	"label": "Sex",
	"type": "SELECT",
	"required": true,
	"displayOrder": 4,
	"optionsJson": "[\"Male\", \"Female\", \"Other\"]"
}
```

- `PUT /api/forms/{formId}/fields/{fieldId}` — cập nhật field (gửi đủ các field bắt buộc)

- `DELETE /api/forms/{formId}/fields/{fieldId}` — xóa field

---

Nhóm: Submission

- `GET /api/forms/active` — lấy danh sách form active (chi tiết kèm fields)

- `POST /api/forms/{formId}/submit` — submit một form
	- Collection dùng ví dụ submit cho `formId=1` với payload:

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
	- Curl tương đương:

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

- `GET /api/forms/{formId}/submissions` — lấy submissions của form (paged)
- `GET /api/submissions?formId={formId}` — lấy submissions (tuỳ chọn filter formId)

---

Validation rules (tóm tắt):

- `TEXT`: chuỗi ≤ 200 ký tự (field `required=true` bắt buộc)
- `NUMBER`: số giữa 0..100
- `DATE`: `yyyy-MM-dd`, không được ở quá khứ
- `COLOR`: `#RRGGBB`
- `SELECT`: phải là một giá trị trong `optionsJson`

Lưu ý: `values` là map với key là `fieldId` (dạng string). Nếu gửi fieldId không thuộc form thì API trả 400.

---

Import collection vào Postman: mở Postman → Import → chọn file `postman/FormManagement.postman_collection.json`.

---


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


