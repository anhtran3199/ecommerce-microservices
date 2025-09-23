# Ecommerce Microservices

Dự án thương mại điện tử sử dụng kiến trúc microservices với Spring Boot 3, Spring Cloud và PostgreSQL.

## Kiến trúc hệ thống

Hệ thống bao gồm các microservices sau:

### 1. Service Discovery (Eureka Server)
- **Port**: 8761
- **Chức năng**: Quản lý đăng ký và khám phá các microservices

### 2. API Gateway
- **Port**: 8080
- **Chức năng**: Định tuyến request đến các microservices thích hợp
- **Routes**:
  - `/api/users/**` → User Service
  - `/api/products/**` → Product Service
  - `/api/orders/**` → Order Service
  - `/api/payments/**` → Payment Service

### 3. User Service
- **Port**: 8081
- **Database**: PostgreSQL (port 5432)
- **Chức năng**: Quản lý người dùng, đăng ký, đăng nhập

### 4. Product Service
- **Port**: 8082
- **Database**: PostgreSQL (port 5433)
- **Chức năng**: Quản lý sản phẩm, danh mục, tồn kho

### 5. Order Service
- **Port**: 8083
- **Database**: PostgreSQL (port 5434)
- **Chức năng**: Quản lý đơn hàng, tích hợp với Product Service

### 6. Payment Service
- **Port**: 8084
- **Database**: PostgreSQL (port 5435)
- **Chức năng**: Xử lý thanh toán, tích hợp với Order Service

### 7. ELK Stack (Monitoring & Logging)
- **Elasticsearch**: 9200 - Lưu trữ và tìm kiếm logs
- **Logstash**: 5044, 5000 - Xử lý và chuyển đổi logs
- **Kibana**: 5601 - Visualization và dashboard
- **Filebeat**: Thu thập logs từ Docker containers

## Công nghệ sử dụng

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Spring Cloud**: 2023.0.0
- **Database**: PostgreSQL 15
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Communication**: OpenFeign
- **Security**: Spring Security + JWT Authentication
- **Password Encryption**: BCrypt
- **Monitoring**: ELK Stack (Elasticsearch, Logstash, Kibana, Filebeat)
- **Logging**: Logback with Logstash encoder
- **Containerization**: Docker & Docker Compose

## Cách chạy dự án

### Yêu cầu hệ thống
- Java 17+
- Maven 3.6+
- Docker & Docker Compose

### 1. Build tất cả services

#### Trên Windows:
```bash
./build-all.bat
```

#### Trên Linux/Mac:
```bash
chmod +x build-all.sh
./build-all.sh
```

### 2. Chạy với Docker Compose

```bash
docker-compose up -d
```

### 3. Kiểm tra services

- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **User Service**: http://localhost:8081
- **Product Service**: http://localhost:8082
- **Order Service**: http://localhost:8083
- **Payment Service**: http://localhost:8084
- **Kibana Dashboard**: http://localhost:5601
- **Elasticsearch**: http://localhost:9200

## Authentication & Security

Dự án sử dụng **JWT (JSON Web Token)** để xác thực và phân quyền. Tất cả API endpoints đều được bảo vệ trừ những endpoint công khai.

### Authentication Endpoints

```
POST   /api/auth/login              # Đăng nhập
POST   /api/auth/register           # Đăng ký
GET    /api/auth/validate?token={}  # Validate JWT token
```

### Cách sử dụng Authentication

1. **Đăng ký tài khoản**:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "0123456789"
  }'
```

2. **Đăng nhập**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'
```

3. **Sử dụng JWT Token**:
```bash
# Response từ login/register sẽ chứa accessToken
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Public Endpoints (không cần authentication)

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/products/active`
- `GET /api/products/category/{category}`
- `GET /api/products/search`

### Protected Endpoints (cần JWT token)

Tất cả các endpoint khác đều yêu cầu JWT token trong header:
```
Authorization: Bearer YOUR_JWT_TOKEN
```

## API Endpoints

### Authentication Service

```
POST   /api/auth/login              # Đăng nhập
POST   /api/auth/register           # Đăng ký
GET    /api/auth/validate           # Validate token
```

### User Service (qua API Gateway: http://localhost:8080)

```
GET    /api/users                    # Lấy danh sách người dùng
GET    /api/users/{id}               # Lấy thông tin người dùng theo ID
GET    /api/users/username/{username} # Lấy người dùng theo username
GET    /api/users/email/{email}      # Lấy người dùng theo email
POST   /api/users                    # Tạo người dùng mới
PUT    /api/users/{id}               # Cập nhật thông tin người dùng
DELETE /api/users/{id}               # Xóa người dùng
```

### Product Service

```
GET    /api/products                 # Lấy tất cả sản phẩm
GET    /api/products/active          # Lấy sản phẩm đang hoạt động
GET    /api/products/{id}            # Lấy sản phẩm theo ID
GET    /api/products/category/{category} # Lấy sản phẩm theo danh mục
GET    /api/products/search?name={name}  # Tìm kiếm sản phẩm theo tên
POST   /api/products                 # Tạo sản phẩm mới
PUT    /api/products/{id}            # Cập nhật sản phẩm
PUT    /api/products/{id}/stock?quantity={qty} # Cập nhật tồn kho
DELETE /api/products/{id}            # Xóa sản phẩm
```

### Order Service

```
GET    /api/orders                   # Lấy tất cả đơn hàng
GET    /api/orders/{id}              # Lấy đơn hàng theo ID
GET    /api/orders/user/{userId}     # Lấy đơn hàng theo người dùng
POST   /api/orders                   # Tạo đơn hàng mới
PUT    /api/orders/{id}/status?status={status} # Cập nhật trạng thái đơn hàng
DELETE /api/orders/{id}              # Xóa đơn hàng
```

### Payment Service

```
GET    /api/payments                 # Lấy tất cả giao dịch
GET    /api/payments/{id}            # Lấy giao dịch theo ID
GET    /api/payments/user/{userId}   # Lấy giao dịch theo người dùng
GET    /api/payments/order/{orderId} # Lấy giao dịch theo đơn hàng
POST   /api/payments/process         # Xử lý thanh toán
PUT    /api/payments/{id}/status?status={status} # Cập nhật trạng thái thanh toán
```

## Cấu trúc dữ liệu

### User Entity
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "0123456789",
  "createdAt": "2023-12-01T10:00:00",
  "updatedAt": "2023-12-01T10:00:00"
}
```

### Product Entity
```json
{
  "id": 1,
  "name": "iPhone 15",
  "description": "Latest iPhone model",
  "price": 999.99,
  "stockQuantity": 50,
  "category": "Electronics",
  "imageUrl": "http://example.com/image.jpg",
  "active": true,
  "createdAt": "2023-12-01T10:00:00",
  "updatedAt": "2023-12-01T10:00:00"
}
```

### Order Entity
```json
{
  "id": 1,
  "userId": 1,
  "totalAmount": 1999.98,
  "status": "PENDING",
  "shippingAddress": "123 Main St, City",
  "orderItems": [
    {
      "productId": 1,
      "quantity": 2,
      "price": 999.99
    }
  ],
  "createdAt": "2023-12-01T10:00:00",
  "updatedAt": "2023-12-01T10:00:00"
}
```

### Payment Entity
```json
{
  "id": 1,
  "orderId": 1,
  "userId": 1,
  "amount": 1999.98,
  "paymentMethod": "CREDIT_CARD",
  "status": "COMPLETED",
  "transactionId": "txn_12345",
  "createdAt": "2023-12-01T10:00:00",
  "updatedAt": "2023-12-01T10:00:00"
}
```

## Dừng và dọn dẹp

```bash
# Dừng tất cả containers
docker-compose down

# Dừng và xóa volumes
docker-compose down -v

# Xóa images
docker-compose down --rmi all
```

## Phát triển thêm

### Thêm tính năng mới
1. Tạo microservice mới trong thư mục riêng
2. Thêm vào `pom.xml` chính
3. Cấu hình trong `docker-compose.yml`
4. Thêm routes trong API Gateway

## Monitoring và Logging với ELK Stack

Dự án đã tích hợp sẵn ELK Stack để monitoring và quản lý logs tập trung.

### Truy cập Monitoring Tools

1. **Kibana Dashboard**: http://localhost:5601
   - Xem logs realtime từ tất cả microservices
   - Dashboard tổng quan hệ thống
   - Phân tích lỗi và performance

2. **Elasticsearch**: http://localhost:9200
   - REST API để query logs trực tiếp
   - Kiểm tra health: `GET /_cluster/health`

### Các Dashboard có sẵn

1. **Microservices Overview Dashboard**:
   - Phân bố log levels (INFO, DEBUG, ERROR, WARN)
   - Hoạt động của từng microservice
   - Timeline logs theo thời gian

2. **Index Pattern**: `ecommerce-logs-*`
   - Tự động tạo index theo ngày
   - Structured logs với metadata

### Cấu trúc Log

Mỗi log entry chứa:
```json
{
  "@timestamp": "2023-12-01T10:00:00.000Z",
  "service": "user-service",
  "microservice": "user-service",
  "log_level": "INFO",
  "message": "User created successfully",
  "environment": "docker",
  "thread": "http-nio-8081-exec-1",
  "class": "com.ecommerce.user.service.UserService"
}
```

### Các loại logs được thu thập

1. **Application Logs**: Logs từ business logic
2. **HTTP Access Logs**: Request/response logs
3. **Database Logs**: SQL queries và performance
4. **Error Logs**: Exception và stack traces

### Tìm kiếm và Filter

Trong Kibana, bạn có thể:
- Filter theo service: `microservice:user-service`
- Filter theo log level: `log_level:ERROR`
- Tìm kiếm text: `message:"User created"`
- Kết hợp filters: `microservice:order-service AND log_level:ERROR`

### Troubleshooting

1. **Nếu không thấy logs trong Kibana**:
   - Kiểm tra container logs: `docker logs logstash`
   - Kiểm tra Elasticsearch: `curl localhost:9200/_cat/indices`
   - Restart Filebeat: `docker restart filebeat`

2. **Nếu Kibana không khởi động**:
   - Kiểm tra Elasticsearch health: `curl localhost:9200/_cluster/health`
   - Tăng memory limit cho Docker

### Monitoring nâng cao
- Tích hợp Zipkin cho distributed tracing
- Prometheus + Grafana cho system metrics
- AlertManager cho cảnh báo

### Security
- Tích hợp Spring Security
- JWT tokens cho authentication
- OAuth2 cho authorization

### Database Migration
- Sử dụng Flyway hoặc Liquibase
- Thay đổi `ddl-auto` từ `create-drop` thành `validate` trong production