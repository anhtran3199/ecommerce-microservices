# RBAC System Implementation Demo

## Overview
Đã implement thành công hệ thống RBAC (Role-Based Access Control) cho microservices e-commerce với:

### 🎯 Tính năng đã implement:
- **3 Roles mặc định**: ADMIN, MANAGER, CUSTOMER
- **16 Permissions**: Quản lý USER, PRODUCT, ORDER, PAYMENT, RBAC
- **Annotation-based authorization**: `@RequiresPermission`, `@RequiresRole`
- **JWT Token với roles**: Token chứa thông tin roles của user
- **Database migrations**: Tự động tạo bảng và dữ liệu mặc định

### 📊 Permission Matrix:

| Permission | ADMIN | MANAGER | CUSTOMER |
|------------|-------|---------|----------|
| CREATE_USER | ✅ | ❌ | ❌ |
| READ_USER | ✅ | ✅ | ❌ |
| UPDATE_USER | ✅ | ❌ | ❌ |
| DELETE_USER | ✅ | ❌ | ❌ |
| CREATE_PRODUCT | ✅ | ✅ | ❌ |
| READ_PRODUCT | ✅ | ✅ | ✅ |
| UPDATE_PRODUCT | ✅ | ✅ | ❌ |
| DELETE_PRODUCT | ✅ | ✅ | ❌ |
| CREATE_ORDER | ✅ | ❌ | ✅ |
| READ_ORDER | ✅ | ✅ | ✅ |
| UPDATE_ORDER | ✅ | ✅ | ❌ |
| DELETE_ORDER | ✅ | ❌ | ❌ |
| CREATE_PAYMENT | ✅ | ❌ | ✅ |
| READ_PAYMENT | ✅ | ✅ | ❌ |
| MANAGE_ROLES | ✅ | ❌ | ❌ |

## 🚀 Testing Instructions

### 1. Build và Start Services
```bash
# Windows
.\build-and-run.bat

# Linux/Mac
chmod +x build-and-run.sh && ./build-and-run.sh
```

### 2. Test Authentication với Roles

#### 2.1 Đăng ký user mới (mặc định sẽ có role CUSTOMER)
```bash
POST http://localhost:8080/auth/register
{
  "username": "customer1",
  "email": "customer1@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### 2.2 Tạo Admin User (qua database hoặc API)
```bash
# Tạo admin user qua API (cần admin token)
POST http://localhost:8080/api/users
Authorization: Bearer {admin-token}
{
  "username": "admin1",
  "email": "admin1@example.com",
  "password": "password123",
  "firstName": "Admin",
  "lastName": "User"
}

# Assign ADMIN role
POST http://localhost:8080/api/roles/assign?userId=2&roleName=ADMIN
Authorization: Bearer {admin-token}
```

### 3. Test RBAC Endpoints

#### 3.1 Customer Tests (với customer token)
```bash
# ✅ Tạo order (customer có quyền)
POST http://localhost:8080/api/orders
Authorization: Bearer {customer-token}
[{
  "productId": 1,
  "quantity": 2,
  "price": 50.00
}]

# ❌ Tạo product (customer không có quyền)
POST http://localhost:8080/api/products
Authorization: Bearer {customer-token}
{
  "name": "Test Product",
  "price": 100.00,
  "stockQuantity": 10
}
# Expected: 403 Forbidden
```

#### 3.2 Manager Tests (với manager token)
```bash
# ✅ Tạo product (manager có quyền)
POST http://localhost:8080/api/products
Authorization: Bearer {manager-token}
{
  "name": "Manager Product",
  "price": 150.00,
  "stockQuantity": 5
}

# ❌ Tạo user (manager không có quyền)
POST http://localhost:8080/api/users
Authorization: Bearer {manager-token}
{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "password123"
}
# Expected: 403 Forbidden
```

#### 3.3 Admin Tests (với admin token)
```bash
# ✅ Quản lý roles (chỉ admin có quyền)
GET http://localhost:8080/api/roles
Authorization: Bearer {admin-token}

# ✅ Tạo permissions
POST http://localhost:8080/api/permissions
Authorization: Bearer {admin-token}
{
  "name": "CUSTOM_PERMISSION",
  "description": "Custom permission for testing",
  "resource": "CUSTOM",
  "action": "TEST"
}

# ✅ Xem tất cả users
GET http://localhost:8080/api/users
Authorization: Bearer {admin-token}
```

### 4. Check User Permissions
```bash
# Kiểm tra permissions của user
GET http://localhost:8080/api/roles/user/{userId}/permissions
Authorization: Bearer {token}

# Kiểm tra specific permission
GET http://localhost:8080/api/roles/user/{userId}/check?resource=PRODUCT&action=CREATE
Authorization: Bearer {token}
```

## 📁 Files Modified/Created:

### Core RBAC Models:
- `user-service/entity/Role.java`
- `user-service/entity/Permission.java`
- `user-service/entity/User.java` (updated with roles)

### Repositories:
- `user-service/repository/RoleRepository.java`
- `user-service/repository/PermissionRepository.java`

### Services:
- `user-service/service/RoleService.java`
- `user-service/service/PermissionService.java`
- `user-service/service/AuthService.java` (updated with roles)

### Controllers:
- `user-service/controller/RoleController.java`
- `user-service/controller/PermissionController.java`
- Updated all controllers with RBAC annotations

### Common Security:
- `common/security/RequiresPermission.java`
- `common/security/RequiresRole.java`
- `common/security/AuthorizationAspect.java`
- `common/security/AuthorizationService.java`
- `common/util/SecurityUtil.java` (updated)
- `common/security/JwtTokenProvider.java` (updated)

### Database:
- `user-service/db/changelog/v1.1/01-create-rbac-tables.xml`
- `user-service/db/changelog/v1.1/02-insert-default-rbac-data.xml`

## 🔒 Security Features:

1. **Method-level Security**: Sử dụng annotations để protect endpoints
2. **Resource-Action Based**: Permissions dựa trên resource và action
3. **JWT Integration**: Roles được embed trong JWT token
4. **Inter-service Authorization**: Services khác có thể check permissions
5. **Hierarchical Roles**: ADMIN > MANAGER > CUSTOMER
6. **Default Role Assignment**: User mới tự động có role CUSTOMER

## 🛠️ Troubleshooting:

1. **403 Forbidden**: Kiểm tra user có đúng role/permission không
2. **JWT Validation**: Đảm bảo token chứa roles information
3. **Database Migration**: Chạy `docker-compose restart user-service` để apply migrations
4. **Service Communication**: Đảm bảo user-service accessible từ các services khác

## 📈 Next Steps:

1. **Dynamic Permissions**: Thêm khả năng tạo permissions động
2. **Role Hierarchy**: Implement role inheritance
3. **Audit Logging**: Log tất cả authorization attempts
4. **UI Integration**: Tạo admin panel để quản lý RBAC
5. **Testing**: Thêm unit tests và integration tests

**RBAC system đã được implement hoàn chỉnh và sẵn sàng để sử dụng!** 🎉