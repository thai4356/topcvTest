# BBB (Form Management API)

Dự án Spring Boot (Java 21) cung cấp API quản lý Form/Submission, dùng MySQL và có Swagger UI.

## Yêu cầu

- Java **21**
- (Tuỳ chọn) Docker + Docker Compose (để chạy MySQL + app nhanh)

## Cấu hình

App đọc cấu hình DB qua biến môi trường (có giá trị mặc định trong `src/main/resources/application.properties`):

- `SPRING_DATASOURCE_URL` (mặc định: `jdbc:mysql://localhost:3306/form_management?...`)
- `SPRING_DATASOURCE_USERNAME` (mặc định: `root`)
- `SPRING_DATASOURCE_PASSWORD` (mặc định: `root`)
- `SPRING_JPA_HIBERNATE_DDL_AUTO` (mặc định: `update`)
- `SERVER_PORT` (mặc định: `8080`)

## Chạy bằng Docker Compose (khuyến nghị)

Chạy MySQL và ứng dụng bằng container:

```bash
docker compose up --build
```

Mặc định:
- MySQL: `localhost:3306` (root/password), database `form_management`
- App: `http://localhost:8080`

Tắt containers:

```bash
docker compose down
```

## Chạy local (không dùng Docker)

### 1) Chuẩn bị MySQL

Bạn cần MySQL đang chạy và có database `form_management`.

Ví dụ tạo DB:

```sql
CREATE DATABASE form_management;
```

### 2) Cấu hình biến môi trường (nếu cần)

**CMD (Windows):**

```bat
set SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/form_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
set SPRING_DATASOURCE_USERNAME=root
set SPRING_DATASOURCE_PASSWORD=root
```

**PowerShell (Windows):**

```powershell
$env:SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3306/form_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
$env:SPRING_DATASOURCE_USERNAME='root'
$env:SPRING_DATASOURCE_PASSWORD='root'
```

### 3) Chạy app bằng Maven Wrapper

```bat
.\mvnw.cmd spring-boot:run
```

Hoặc build jar và chạy:

```bat
.\mvnw.cmd -DskipTests clean package
java -jar target\bbb-0.0.1-SNAPSHOT.jar
```

## Kiểm tra nhanh

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Hướng dẫn API

Base URL (local): `http://localhost:8080`

### Enum giá trị

- `FormStatus`: `ACTIVE`, `DRAFT`
- `FieldType`: `TEXT`, `NUMBER`, `DATE`, `COLOR`, `SELECT`

### Phân trang (Spring Data)

Các API trả về `Page<...>` hỗ trợ query params:

- `page` (mặc định 0)
- `size` (mặc định 20)
- `sort` (vd: `sort=displayOrder,asc`)

Ví dụ:

`GET /api/forms?page=0&size=10&sort=displayOrder,asc`

### Lỗi trả về

Khi lỗi (400/404/500) API trả về dạng:

```json
{
	"message": "Validation failed",
	"field": "title",
	"error": "title is required"
}
```

### Forms

- `GET /api/forms` (paged)
- `GET /api/forms/active` (trả về chi tiết form + danh sách fields)
- `POST /api/forms`
- `GET /api/forms/{id}`
- `PUT /api/forms/{id}`
- `DELETE /api/forms/{id}`

Tạo form (ví dụ `curl`):

```bash
curl -X POST "http://localhost:8080/api/forms" \
	-H "Content-Type: application/json" \
	-d "{\
		\"title\": \"Registration Form\",\
		\"description\": \"Example form\",\
		\"displayOrder\": 1,\
		\"status\": \"ACTIVE\",\
		\"fields\": [\
			{\"label\":\"Full Name\",\"type\":\"TEXT\",\"displayOrder\":1,\"required\":true},\
			{\"label\":\"Age\",\"type\":\"NUMBER\",\"displayOrder\":2,\"required\":false},\
			{\"label\":\"Country\",\"type\":\"SELECT\",\"displayOrder\":3,\"required\":false,\"optionsJson\":\"[\\\"VN\\\",\\\"US\\\"]\"}\
		]\
	}"
```

Ghi chú:
- `displayOrder` của form phải **unique**.
- `fields[*].displayOrder` phải **unique** trong cùng 1 form.

### Fields (trong 1 form)

- `POST /api/forms/{formId}/fields`
- `PUT /api/forms/{formId}/fields/{fieldId}`
- `DELETE /api/forms/{formId}/fields/{fieldId}`

Update field lưu ý: request body là `FormFieldUpsertRequest` nên cần gửi đủ các field bắt buộc: `label`, `type`, `displayOrder`, `required` (không hỗ trợ partial update).

### Submissions

- `POST /api/forms/{formId}/submit`
- `GET /api/forms/{formId}/submissions` (paged)
- `GET /api/submissions?formId={formId}` (paged)

Submit form:

1) Lấy danh sách field id của form qua `GET /api/forms/{id}`
2) Submit bằng map `values` với key là **fieldId dạng string**

Ví dụ:

```bash
curl -X POST "http://localhost:8080/api/forms/1/submit" \
	-H "Content-Type: application/json" \
	-d "{\
		\"submittedBy\": \"admin\",\
		\"values\": {\
			\"10\": \"Nguyễn Văn A\",\
			\"11\": 30\
		}\
	}"
```

Ghi chú:
- Chỉ submit được khi form có `status=ACTIVE`.
- Nếu `values` chứa fieldId không tồn tại trong form, API trả 400 (Unknown field id...).

## Chạy test

```bat
.\mvnw.cmd test
```

## Postman

Collection có sẵn tại: `postman/FormManagement.postman_collection.json`
