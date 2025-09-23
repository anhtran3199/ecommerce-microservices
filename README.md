# E-commerce Microservices

Dự án thương mại điện tử sử dụng kiến trúc microservices với Spring Boot 3, Spring Cloud và PostgreSQL.

## Kiến trúc

### Services
- **Eureka Server** - Service Discovery (Port 8761)
- **API Gateway** - Central entry point (Port 8080)
- **User Service** - Quản lý người dùng & authentication
- **Product Service** - Quản lý sản phẩm
- **Order Service** - Xử lý đơn hàng
- **Payment Service** - Xử lý thanh toán

### Infrastructure
- **PostgreSQL** - Database riêng cho mỗi service (Ports 5432-5435)
- **ELK Stack** - Elasticsearch, Logstash, Kibana cho logging
- **Filebeat** - Thu thập logs

## Quick Start

### Yêu cầu
- Java 17
- Maven 3.9+
- Docker & Docker Compose

### Chạy ứng dụng

**Windows:**
```bash
.\build-and-run.bat
```

**Linux/Mac:**
```bash
chmod +x build-and-run.sh && ./build-and-run.sh
```

## Endpoints

### Core Services
- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080

### Authentication
```bash
# Đăng ký user
POST http://localhost:8080/auth/signup
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123",
  "firstName": "Test",
  "lastName": "User"
}

# Đăng nhập
POST http://localhost:8080/auth/signin
{
  "username": "testuser",
  "password": "password123"
}
```

### Business Operations
```bash
# Lấy danh sách sản phẩm
GET http://localhost:8080/api/products

# Tạo đơn hàng
POST http://localhost:8080/api/orders
Authorization: Bearer {jwt-token}

# Thanh toán
POST http://localhost:8080/api/payments
Authorization: Bearer {jwt-token}
```

## Monitoring

- **Elasticsearch**: http://localhost:9200
- **Kibana**: http://localhost:5601

## Troubleshooting

```bash
# Kiểm tra trạng thái services
docker-compose ps

# Xem logs
docker-compose logs -f [service-name]

# Restart tất cả
docker-compose restart

# Clean restart
docker-compose down && docker-compose up -d --build
```

## Tính năng

- ✅ Service Discovery với Eureka
- ✅ API Gateway với Spring Cloud Gateway
- ✅ JWT Authentication
- ✅ Database per Service với PostgreSQL
- ✅ Liquibase migrations
- ✅ JPA Auditing
- ✅ Centralized logging với ELK Stack
- ✅ Health checks và restart policies

## Cấu trúc project

```
Microservices/
├── common/           # Shared utilities
├── eureka-server/    # Service discovery
├── api-gateway/      # API gateway
├── user-service/     # User management
├── product-service/  # Product catalog
├── order-service/    # Order processing
├── payment-service/  # Payment handling
├── elk/              # ELK configuration
└── docker-compose.yml
```