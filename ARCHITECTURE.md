# 🏗️ MVC Spring Boot API - Hướng Dẫn Kiến Trúc

## Mục Lục
1. [Tổng Quan Kiến Trúc](#tổng-quan-kiến-trúc)
2. [Các Lớp Chính (Layers)](#các-lớp-chính)
3. [Các Controllers](#các-controllers)
4. [Flow Xử Lý](#flow-xử-lý)
5. [Tương Tác Giữa Các Lớp](#tương-tác-giữa-các-lớp)
6. [Database Schema](#database-schema)
7. [Security Architecture](#security-architecture)

---

## 📊 Tổng Quan Kiến Trúc

Ứng dụng sử dụng **Spring Boot MVC Architecture** với cấu trúc **3-Layer Pattern**:

```
┌─────────────────────────────────────────────────────┐
│           REST API Clients (Web/Mobile)             │
└─────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│  HTTP Request → Spring Dispatcher Servlet            │
│  ↓                                                   │
│  JwtFilter (Authentication) → SecurityContext       │
│  ↓                                                   │
│  LoggingInterceptor (Request Logging)               │
│  ↓                                                   │
│  GlobalExceptionHandler (Error Handling)            │
└─────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│           CONTROLLER LAYER                          │
│  ├─ AuthController (xác thực/đăng ký)              │
│  ├─ UserController (quản lý user)                  │
│  └─ HealthController (kiểm tra trạng thái)         │
└─────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│           SERVICE LAYER (Business Logic)            │
│  ├─ AuthService/AuthServiceImpl                     │
│  └─ UserService/UserServiceImpl                     │
└─────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│         REPOSITORY LAYER (Data Access)              │
│  ├─ UserRepository                                 │
│  └─ Spring Data JPA (EntityManager)                │
└─────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────┐
│        DATABASE LAYER                               │
│  └─ PostgreSQL Database                            │
└─────────────────────────────────────────────────────┘
```

---

## 🔧 Các Lớp Chính

### 1. **Controller Layer** 🎯

#### 📍 AuthController
```
📁 AuthController.java
├── POST /api/v1/auth/register    → Đăng ký tài khoản mới
├── POST /api/v1/auth/login       → Đăng nhập User/Admin
├── POST /api/v1/auth/refresh     → Làm mới access token
└── GET  /api/v1/auth/me          → Lấy thông tin user hiện tại (cần token)
```

**Chức năng chính:**
- Xác nhận request (validation)
- Gọi AuthService để xử lý
- Trả về response với Access Token và Refresh Token

---

#### 📍 UserController
```
📁 UserController.java
├── GET    /api/v1/users              → Danh sách users (pagination, có role ADMIN)
├── GET    /api/v1/users/{id}         → Lấy user theo ID
├── PUT    /api/v1/users/{id}         → Cập nhật thông tin user
└── DELETE /api/v1/users/{id}         → Xóa user
```

**Chức năng chính:**
- Kiểm tra quyền (Authorization với @PreAuthorize)
- Xử lý pagination & sorting
- Gọi UserService
- Định dạng response

---

#### 📍 HealthController
```
📁 HealthController.java
└── GET /api/v1/health → Kiểm tra ứng dụng có chạy hay không
```

---

### 2. **Service Layer** 🔨

#### 📍 AuthService (Interface)
```java
interface AuthService {
    AuthResponse register(RegisterRequest request)
    AuthResponse login(LoginRequest request)
    AuthResponse refreshToken(String refreshToken)
    UserDto getMe(String uuid)
}
```

#### 📍 AuthServiceImpl
```
Chức năng:
├─ register()
│  ├─ Validate request (email, username không trùng)
│  ├─ Mã hóa password (BCrypt)
│  ├─ Lưu User vào Database
│  ├─ Generate Access Token (JWT)
│  ├─ Generate Refresh Token (JWT)
│  └─ Return AuthResponse {accessToken, refreshToken, user, message}
│
├─ login()
│  ├─ Tìm User bằng email
│  ├─ So sánh password (BCrypt)
│  ├─ Generate Access Token (JWT)
│  ├─ Generate Refresh Token (JWT)
│  └─ Return AuthResponse {accessToken, refreshToken, user, message}
│
├─ refreshToken(refreshToken)
│  ├─ Validate refresh token
│  ├─ Extract UUID từ refresh token
│  ├─ Tìm User trong database
│  ├─ Generate Access Token mới
│  ├─ Generate Refresh Token mới
│  └─ Return AuthResponse {accessToken, refreshToken, user, message}
│
└─ getMe(uuid)
   ├─ Tìm User trong database bằng UUID
   └─ Return UserDto
```

---

#### 📍 UserService (Interface)
```java
interface UserService {
    List<UserDto> getAllUsers()
    Page<UserDto> getAllUsers(Pageable pageable)
    UserDto getUserById(Long id)
    UserDto updateUser(Long id, UserDto userDto)
    void deleteUser(Long id)
}
```

#### 📍 UserServiceImpl
```
Chức năng:
├─ getAllUsers()
│  ├─ Gọi UserRepository.findAll()
│  └─ Map User Entity → UserDto
│
├─ getAllUsers(pageable)
│  ├─ Gọi UserRepository.findAll(pageable)
│  └─ Return Page<UserDto> (hỗ trợ pagination)
│
├─ getUserById(id)
│  ├─ Tìm User theo ID
│  ├─ Throw ResourceNotFoundException nếu không tìm thấy
│  └─ Return UserDto
│
├─ updateUser(id, userDto)
│  ├─ Tìm User theo ID
│  ├─ Update các field (fullName, isActive)
│  ├─ Lưu vào database
│  └─ Return UserDto cập nhật
│
└─ deleteUser(id)
   ├─ Tìm User theo ID
   ├─ Xóa khỏi database
   └─ Throw exception nếu không tìm thấy
```

---

### 3. **Repository Layer** 💾

#### 📍 UserRepository
```java
interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username)
    Optional<User> findByEmail(String email)
    Optional<User> findByEmailAndPassword(String email, String password)
    // ... các custom queries
}
```

**Mở rộng JpaRepository cung cấp:**
- CRUD operations (Create, Read, Update, Delete)
- Pagination & Sorting
- Custom Query methods

---

### 4. **Model Layer** 📦

#### 📍 BaseEntity
```java
@MappedSuperclass
class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id

    @CreationTimestamp
    private LocalDateTime createdAt

    @UpdateTimestamp
    private LocalDateTime updatedAt
}
```

#### 📍 User
```java
@Entity
@Table(name = "users")
class User extends BaseEntity {
    String username           (unique)
    String email             (unique)
    String password          (mã hóa)
    String fullName
    String phoneNumber
    Boolean isActive         (default: true)
    Role role               (enum: USER, ADMIN)
}
```

#### 📍 Role (Enum)
```java
enum Role {
    USER,
    ADMIN
}
```

---

### 5. **Security Layer** 🔐

#### 📍 JwtFilter
```
Luồng hoạt động:
1. HTTP Request tới
   ↓
2. JwtFilter.doFilterInternal()
   ├─ Check: Path có phải public endpoint? 
   │  ├─ YES → Pass qua (không cần JWT)
   │  │  Ví dụ: /api/v1/auth/*, /api/v1/health
   │  └─ NO → Tiếp tục
   │
   ├─ Extract JWT từ Header "Authorization: Bearer <token>"
   │  ├─ Token không tìm thấy → Pass qua (sẽ lỗi ở SecurityConfig)
   │  └─ Token found → Tiếp tục
   │
   ├─ Validate JWT token
   │  ├─ Invalid/Expired → Log warning, pass qua
   │  └─ Valid → Tiếp tục
   │
   ├─ Extract username & role từ JWT
   │
   ├─ Tạo UsernamePasswordAuthenticationToken
   │
   ├─ Set vào SecurityContext
   │
   └─ filterChain.doFilter() → tiếp tục xử lý request
```

---

#### 📍 JwtUtil
```
Chức năng:
├─ generateToken(username, role)
│  └─ Tạo JWT với payload: {username, role, issuedAt, expiration}
│
├─ extractUsername(token)
│  └─ Parse JWT & lấy subject (username)
│
├─ extractRole(token)
│  └─ Parse JWT & lấy claim "role"
│
└─ validateToken(token)
   └─ Verify JWT signature & expiration
```

**JWT Token Structure:**
```
Access Token:
Header: {
  "alg": "HS256",
  "typ": "JWT"
}

Payload: {
  "sub": "user-uuid-here",        // user UUID
  "role": "ADMIN",
  "iat": 1701234567,              // issued at
  "exp": 1701320967               // expiration (24h)
}

Signature: HMACSHA256(header.payload, secret)

Refresh Token:
Header: {
  "alg": "HS256",
  "typ": "JWT"
}

Payload: {
  "sub": "user-uuid-here",        // user UUID
  "iat": 1701234567,              // issued at
  "exp": 1701839367               // expiration (7 days)
}

Signature: HMACSHA256(header.payload, secret)
```

---

### 6. **Exception Handling** ⚠️

#### 📍 GlobalExceptionHandler
```
Các exception xử lý:
├─ ResourceNotFoundException
│  └─ HTTP 404 Not Found
│
├─ UnauthorizedException
│  └─ HTTP 401 Unauthorized
│
├─ BadRequestException
│  └─ HTTP 400 Bad Request
│
├─ MethodArgumentNotValidException (Validation)
│  └─ HTTP 400 Bad Request + chi tiết field errors
│
└─ Exception (Generic)
   └─ HTTP 500 Internal Server Error
```

**Response Format:**
```json
{
  "code": "ERROR_CODE",
  "message": "Chi tiết lỗi",
  "status": 400,
  "timestamp": "2025-11-30T10:30:00",
  "path": "/api/v1/users",
  "details": {}
}
```

---

### 7. **DTO & Mapper** 🔄

#### 📍 DTOs (Data Transfer Objects)
```
├─ UserDto              → Response User info
├─ AuthResponse         → Response khi login/register (có accessToken & refreshToken)
├─ LoginRequest         → Request login
├─ RegisterRequest      → Request register
├─ RefreshTokenRequest  → Request refresh token
└─ UpdateUserRequest    → Request update user
```

#### 📍 UserMapper (MapStruct)
```java
@Mapper(componentModel = "spring")
interface UserMapper {
    UserDto toDto(User user)    // Entity → DTO
    User toEntity(UserDto dto)  // DTO → Entity
}
```

---

## 🔄 Flow Xử Lý

### Flow 1: Đăng Ký (Register)

```
CLIENT                                  
    │
    ├─ POST /api/v1/auth/register
    │  └─ Body: {email, username, password, fullName, phoneNumber}
    │
    ↓
AuthController
    │
    ├─ Validate request (@Valid)
    │
    └─ authService.register(request)
    │
    ↓
AuthServiceImpl
    │
    ├─ Check duplicate email/username
    │  └─ Nếu trùng → throw BadRequestException
    │
    ├─ Hash password: passwordEncoder.encode(password)
    │
    ├─ Tạo User entity
    │  └─ User.builder()
    │     .email(email)
    │     .username(username)
    │     .password(hashedPassword)
    │     .fullName(fullName)
    │     .phoneNumber(phoneNumber)
    │     .isActive(true)
    │     .role(Role.USER)
    │     .build()
    │
    ├─ userRepository.save(user)
    │
    ├─ Generate JWT token
    │  ├─ jwtUtil.generateToken(user.getUuid(), "USER")
    │  └─ jwtUtil.generateRefreshToken(user.getUuid())
    │
    └─ return AuthResponse {accessToken, refreshToken, user, message}
    │
    ↓
AuthController
    │
    └─ ResponseEntity.status(201).body(response)
    │
    ↓
CLIENT
    │
    └─ Nhận response:
       {
         "accessToken": "eyJhbGc...",
         "refreshToken": "eyJhbGc...",
         "user": {
           "id": 1,
           "username": "john_doe",
           "email": "john@example.com",
           "fullName": "John Doe"
         },
         "message": "Register successfully",
         "success": true
       }
```

---

### Flow 2: Đăng Nhập (Login)

```
CLIENT
    │
    ├─ POST /api/v1/auth/login
    │  └─ Body: {email, password}
    │
    ↓
AuthController
    │
    ├─ Validate request (@Valid)
    │
    └─ authService.login(request)
    │
    ↓
AuthServiceImpl
    │
    ├─ userRepository.findByEmail(email)
    │  └─ Nếu không tìm thấy → throw UnauthorizedException
    │
    ├─ passwordEncoder.matches(plainPassword, hashedPassword)
    │  └─ Nếu sai → throw UnauthorizedException
    │
    ├─ Generate JWT token
    │  ├─ jwtUtil.generateToken(user.getUuid(), role)
    │  └─ jwtUtil.generateRefreshToken(user.getUuid())
    │
    └─ return AuthResponse {accessToken, refreshToken, user, message}
    │
    ↓
AuthController
    │
    └─ ResponseEntity.ok(response)
    │
    ↓
CLIENT
    │
    └─ Nhận response + JWT tokens
       ├─ Lưu accessToken vào localStorage/sessionStorage
       ├─ Lưu refreshToken vào localStorage (hoặc httpOnly cookie)
       └─ Dùng accessToken để gửi kèm trong header: "Authorization: Bearer <accessToken>"
```

---

### Flow 3: Request Protected Resource

```
CLIENT
    │
    ├─ GET /api/v1/users
    │  ├─ Header: "Authorization: Bearer eyJhbGc..."
    │  └─ Header: "Content-Type: application/json"
    │
    ↓
HTTP Request → Dispatcher Servlet
    │
    ↓
JwtFilter.doFilterInternal()
    │
    ├─ Check: Path có phải public? 
    │  └─ NO → /api/v1/users cần authentication
    │
    ├─ Extract token từ header
    │  └─ "Bearer eyJhbGc..." → "eyJhbGc..."
    │
    ├─ jwtUtil.validateToken(token)
    │  ├─ Parse & verify signature ✓
    │  └─ Check expiration ✓
    │
    ├─ Extract username & role
    │  ├─ username = "john_doe"
    │  └─ role = "USER"
    │
    ├─ Set SecurityContext
    │  └─ Principal = "john_doe" with authorities = [ROLE_USER]
    │
    └─ filterChain.doFilter()
    │
    ↓
SecurityConfig
    │
    ├─ Check: /api/v1/users có phải protected?
    │  └─ YES → Cần authenticated() ✓
    │
    └─ filterChain.doFilter()
    │
    ↓
UserController.getAllUsers()
    │
    ├─ Check @PreAuthorize("hasRole('ADMIN')")
    │  ├─ Current user role = USER
    │  └─ Cần role = ADMIN → throw AccessDeniedException
    │
    ↓
GlobalExceptionHandler.handleAccessDenied()
    │
    └─ ResponseEntity.status(403).body(ErrorResponse)
    │
    ↓
CLIENT
    │
    └─ HTTP 403 Forbidden
       {
         "code": "FORBIDDEN",
         "message": "Access denied",
         "status": 403
       }
```

---

### Flow 4: Refresh Token

```
CLIENT
    │
    ├─ POST /api/v1/auth/refresh
    │  ├─ Body: {"refreshToken": "eyJhbGc..."}
    │  └─ Header: "Content-Type: application/json"
    │
    ↓
AuthController.refreshToken(RefreshTokenRequest)
    │
    ├─ Validate request (@Valid)
    │
    └─ authService.refreshToken(refreshToken)
    │
    ↓
AuthServiceImpl.refreshToken(refreshToken)
    │
    ├─ jwtUtil.validateToken(refreshToken)
    │  └─ Nếu invalid/expired → throw UnauthorizedException
    │
    ├─ Extract UUID từ refresh token
    │  └─ String uuid = jwtUtil.extractUsername(refreshToken)
    │
    ├─ userRepository.findByUuid(uuid)
    │  └─ Nếu không tìm thấy → throw UnauthorizedException
    │
    ├─ Generate new tokens
    │  ├─ String newAccessToken = jwtUtil.generateToken(uuid, role)
    │  └─ String newRefreshToken = jwtUtil.generateRefreshToken(uuid)
    │
    └─ return AuthResponse {accessToken, refreshToken, user, message}
    │
    ↓
AuthController
    │
    └─ ResponseEntity.ok(response)
    │
    ↓
CLIENT
    │
    └─ Nhận new tokens
       ├─ Lưu accessToken mới
       ├─ Lưu refreshToken mới
       └─ Tiếp tục sử dụng
```

---

### Flow 5: Lấy Thông Tin User (Get Me)

```
CLIENT
    │
    ├─ GET /api/v1/auth/me
    │  └─ Header: "Authorization: Bearer <token>"
    │
    ↓
JwtFilter
    │
    └─ Skip (public endpoint /api/v1/auth/*) ✓
    │
    ↓
AuthController.getMe()
    │
    ├─ Extract UUID từ SecurityContext
    │  └─ String uuid = SecurityContextHolder.getContext().getAuthentication().getName()
    │
    ├─ Validate UUID không null/empty
    │  └─ Nếu invalid → throw UnauthorizedException
    │
    ├─ authService.getMe(uuid)
    │  │
    │  ↓
    │  AuthServiceImpl
    │  │
    │  ├─ userRepository.findByUuid(uuid)
    │  │
    │  ├─ userMapper.toDto(user)
    │  │
    │  └─ return UserDto
    │
    └─ return ApiResponse {message, user, success, timestamp}
    │
    ↓
CLIENT
    │
    └─ HTTP 200 OK
       {
         "code": 200,
         "message": "User info retrieved successfully",
         "data": {
           "id": 1,
           "username": "john_doe",
           "email": "john@example.com",
           "fullName": "John Doe"
         },
         "timestamp": "2025-11-30T10:30:00"
       }
```

---

## 🔗 Tương Tác Giữa Các Lớp

### Dependency Injection Diagram

```
┌──────────────────────────────────────────────────────┐
│           Spring Application Context                  │
└──────────────────────────────────────────────────────┘
                          
    ↓ @Bean / @Autowired
    
┌──────────────────────────────────────────────────────┐
│           Configuration Beans                         │
├──────────────────────────────────────────────────────┤
│ ├─ PasswordEncoder (BCryptPasswordEncoder)           │
│ ├─ UserMapper (MapStruct)                           │
│ ├─ JwtUtil                                          │
│ ├─ JwtFilter                                        │
│ └─ SecurityFilterChain                              │
└──────────────────────────────────────────────────────┘
    ↓ @Autowired / Constructor Injection
┌──────────────────────────────────────────────────────┐
│           Controllers                                 │
├──────────────────────────────────────────────────────┤
│ AuthController (@RequiredArgsConstructor)           │
│ ├─ final AuthService authService                    │
│ └─ final JwtUtil jwtUtil                            │
│                                                      │
│ UserController (@RequiredArgsConstructor)           │
│ └─ final UserService userService                    │
└──────────────────────────────────────────────────────┘
    ↓
┌──────────────────────────────────────────────────────┐
│           Services (Interface + Impl)                │
├──────────────────────────────────────────────────────┤
│ AuthServiceImpl (@Service)                          │
│ ├─ @Autowired UserRepository userRepository        │
│ ├─ @Autowired PasswordEncoder passwordEncoder      │
│ └─ @Autowired JwtUtil jwtUtil                      │
│                                                      │
│ UserServiceImpl (@Service)                          │
│ ├─ @Autowired UserRepository userRepository        │
│ └─ @Autowired UserMapper userMapper                │
└──────────────────────────────────────────────────────┘
    ↓
┌──────────────────────────────────────────────────────┐
│           Repositories (Data Access)                 │
├──────────────────────────────────────────────────────┤
│ UserRepository extends JpaRepository                │
│ └─ Tương tác với EntityManager/Hibernate           │
└──────────────────────────────────────────────────────┘
    ↓ SQL Queries
┌──────────────────────────────────────────────────────┐
│           Database (PostgreSQL)                      │
├──────────────────────────────────────────────────────┤
│ Table: users                                         │
│ └─ id, username, email, password, fullName, ...     │
└──────────────────────────────────────────────────────┘
```

---

### Call Stack Example: Login Request

```
HTTP POST /api/v1/auth/login

1️⃣  DispatcherServlet
      └─> Tìm mapping controller

2️⃣  AuthController.login(LoginRequest)
      └─> @PostMapping("/login")

3️⃣  AuthServiceImpl.login(LoginRequest)
      ├─> userRepository.findByEmail(email)
      │    └─> Database Query: SELECT * FROM users WHERE email = ?
      │
      ├─> passwordEncoder.matches(plain, hashed)
      │    └─> BCrypt verify
      │
      └─> jwtUtil.generateToken(username, role)
           ├─> Jwts.builder()
           ├─> .subject(username)
           ├─> .claim("role", role)
           ├─> .expiration(...)
           ├─> .signWith(signingKey)
           └─> .compact()

4️⃣  AuthController.login() - Return Response
      └─> ResponseEntity.ok(AuthResponse)

5️⃣  Client nhận JWT Token
```

---

## 📊 Database Schema

### ERD (Entity Relationship Diagram)

```
┌─────────────────────────────────┐
│          users Table            │
├─────────────────────────────────┤
│ id (PK)          │ BIGINT        │
│ username (UK)    │ VARCHAR(255)  │
│ email (UK)       │ VARCHAR(255)  │
│ password         │ VARCHAR(255)  │ ← Hashed (BCrypt)
│ full_name        │ VARCHAR(255)  │
│ phone_number     │ VARCHAR(20)   │
│ is_active        │ BOOLEAN       │ ← Default: true
│ role             │ VARCHAR(20)   │ ← ENUM: USER, ADMIN
│ created_at       │ TIMESTAMP     │ ← AUTO
│ updated_at       │ TIMESTAMP     │ ← AUTO
└─────────────────────────────────┘
```

### SQL Schema Creation

```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    phone_number VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT role_check CHECK (role IN ('USER', 'ADMIN'))
);

-- Indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);
```

---

## 🔐 Security Architecture

### Authentication & Authorization Flow

```
┌─────────────────────────────────────────────────────┐
│              PUBLIC ENDPOINTS (No Auth Needed)       │
│  ├─ POST   /api/v1/auth/register                   │
│  ├─ POST   /api/v1/auth/login                      │
│  ├─ POST   /api/v1/auth/refresh                    │
│  └─ GET    /api/v1/health                          │
└─────────────────────────────────────────────────────┘

                          ↓ 
                          
┌─────────────────────────────────────────────────────┐
│      AUTHENTICATED ENDPOINTS (Auth Required)         │
│  └─ GET    /api/v1/auth/me        [USER/ADMIN]    │
└─────────────────────────────────────────────────────┘

                          ↓
                          
┌─────────────────────────────────────────────────────┐
│           PROTECTED ENDPOINTS (ADMIN Only)           │
│  ├─ GET    /api/v1/users          [ADMIN]          │
│  ├─ GET    /api/v1/users/{id}     [ADMIN]          │
│  ├─ PUT    /api/v1/users/{id}     [ADMIN]          │
│  └─ DELETE /api/v1/users/{id}     [ADMIN]          │
└─────────────────────────────────────────────────────┘

                          ↓
                          
┌─────────────────────────────────────────────────────┐
│           AUTHENTICATION CHECK                       │
│  1. JwtFilter: Validate JWT token                   │
│  2. Extract username & role                         │
│  3. Set SecurityContext (Principal)                 │
│  4. Pass request to controller                      │
└─────────────────────────────────────────────────────┘

                          ↓
                          
┌─────────────────────────────────────────────────────┐
│           AUTHORIZATION CHECK                        │
│  @PreAuthorize("hasRole('ADMIN')")                  │
│  → Kiểm tra user có role = ADMIN không?             │
│  → YES: Cho phép execute controller method          │
│  → NO:  Throw AccessDeniedException (403)           │
└─────────────────────────────────────────────────────┘
```

### Password Security

```
User enters password: "MyPassword123"
         ↓
PasswordEncoder.encode("MyPassword123")
    ├─ Hash algorithm: BCrypt
    ├─ Salt: Random generated
    ├─ Iterations: 10 (default)
    └─ Result: $2a$10$...hash...

Database: $2a$10$...hash...

Login verification:
  passwordEncoder.matches("MyPassword123", "$2a$10$...hash...")
    ├─ Extract salt từ hash
    ├─ Hash input password với salt
    ├─ Compare kết quả với stored hash
    └─ Return true/false
```

### JWT Token Security

```
Configuration:
├─ Secret Key: 256-bit minimum (app.properties: jwt.secret)
├─ Algorithm: HS256 (HMAC SHA-256)
├─ Access Token Expiration: 24 hours (jwt.expiration: 86400000ms)
├─ Refresh Token Expiration: 7 days (jwt.refresh-expiration: 604800000ms)
└─ Claims:
   Access Token:
   ├─ sub (subject): user UUID
   ├─ role: USER hoặc ADMIN
   ├─ iat (issued at): timestamp tạo
   └─ exp (expiration): timestamp hết hạn
   
   Refresh Token:
   ├─ sub (subject): user UUID
   ├─ iat (issued at): timestamp tạo
   └─ exp (expiration): timestamp hết hạn (7 days)

Signature:
  HMACSHA256(header.payload, secret_key)

Verification:
  1. Parse JWT
  2. Verify signature (check không bị tamper)
  3. Check expiration (đã hết hạn?)
  4. Extract claims (lấy thông tin)
```

---

## 🎯 Role-Based Access Control (RBAC)

```
USER Role (Người dùng thường)
├─ Có thể: Đăng ký, Đăng nhập, Lấy thông tin cá nhân, Refresh token
├─ Không thể: Xem danh sách users, Cập nhật user khác, Xóa user
└─ Endpoints:
   ├─ POST /api/v1/auth/register      ✓
   ├─ POST /api/v1/auth/login         ✓
   ├─ POST /api/v1/auth/refresh       ✓
   └─ GET  /api/v1/auth/me            ✓

ADMIN Role (Quản trị viên)
├─ Có thể: Làm tất cả + quản lý users
├─ Endpoints:
   ├─ POST /api/v1/auth/register      ✓
   ├─ POST /api/v1/auth/login         ✓
   ├─ POST /api/v1/auth/refresh       ✓
   ├─ GET  /api/v1/auth/me            ✓
   ├─ GET  /api/v1/users              ✓
   ├─ GET  /api/v1/users/{id}         ✓
   ├─ PUT  /api/v1/users/{id}         ✓
   └─ DELETE /api/v1/users/{id}       ✓
```

---

## 🔄 Data Flow Diagram

```
┌──────────────┐
│   CLIENT     │ (Web Browser / Mobile App)
│  (Frontend)  │
└──────┬───────┘
       │
       │ HTTP Request
       │ (JSON)
       ↓
┌──────────────────────────────────────────────────┐
│    REST API (Spring Boot Application)             │
│  ┌──────────────────────────────────────────┐    │
│  │ Input: JSON Request                      │    │
│  │ Process:                                  │    │
│  │  1. Parse JSON → Java Object            │    │
│  │  2. Validate data                        │    │
│  │  3. Transform using Mapper              │    │
│  │  4. Execute business logic              │    │
│  │  5. Perform DB operations               │    │
│  │  6. Transform Entity → DTO              │    │
│  │  7. Wrap in ApiResponse                 │    │
│  │  8. Convert to JSON                     │    │
│  │ Output: JSON Response                    │    │
│  └──────────────────────────────────────────┘    │
└──────┬───────────────────────────────────────────┘
       │
       │ HTTP Response
       │ (JSON)
       ↓
┌──────────────┐
│   CLIENT     │ (Display data / Update UI)
│  (Frontend)  │
└──────────────┘
```

---

## 📈 Request Lifecycle

```
1. REQUEST ARRIVES
   ├─ HTTP method, URL, Headers, Body
   └─ Spring DispatcherServlet nhận request

2. FILTER CHAIN
   ├─ JwtFilter
   │  ├─ Check public endpoint? 
   │  ├─ Extract & validate JWT
   │  └─ Set SecurityContext
   │
   ├─ LoggingInterceptor
   │  └─ Log request details
   │
   └─ GlobalExceptionHandler
      └─ Wrap toàn bộ controller layer

3. DISPATCHER
   ├─ Tìm @RequestMapping phù hợp
   └─ Gọi AuthController hoặc UserController

4. CONTROLLER
   ├─ Parse & validate request
   ├─ Gọi Service layer
   └─ Format response

5. SERVICE
   ├─ Implement business logic
   ├─ Gọi Repository layer
   └─ Return result

6. REPOSITORY
   ├─ Build SQL query
   ├─ Execute qua Hibernate/JPA
   └─ Return data từ DB

7. RESPONSE BUILD
   ├─ Map Entity → DTO
   ├─ Wrap trong ApiResponse
   ├─ Serialize to JSON
   └─ Set HTTP headers

8. RESPONSE SEND
   ├─ HTTP Status code
   ├─ Response headers
   ├─ JSON body
   └─ Return to client
```

---

## 🚀 Technology Stack Details

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Web Framework** | Spring Boot 3.1.5 | MVC Framework |
| **Data Access** | Spring Data JPA | ORM & Database access |
| **ORM** | Hibernate | Object-Relational Mapping |
| **Database** | PostgreSQL | Data persistence |
| **Security** | Spring Security | Authentication & Authorization |
| **JWT** | JJWT 0.12.3 | Token generation & validation |
| **Password Hashing** | BCrypt | Secure password encryption |
| **Object Mapping** | MapStruct 1.5.5 | Entity ↔ DTO mapping |
| **Annotation** | Lombok 1.18.30 | Reduce boilerplate code |
| **Validation** | Jakarta Bean Validation | Input validation |
| **Testing** | JUnit 5 | Unit testing |
| **Build Tool** | Maven 3.6+ | Build & dependency management |
| **Runtime** | Java 17 JDK | Runtime environment |

---

## 📝 Common Annotations Used

```
@Configuration          → Spring config class
@Service               → Service layer (singleton)
@Repository            → Repository layer (singleton)
@RestController        → REST controller
@RequestMapping        → Route mapping
@GetMapping/@PostMapping → HTTP method mapping
@PathVariable          → Path parameter
@RequestParam          → Query parameter
@RequestBody           → Request body JSON
@ResponseEntity        → Custom HTTP response
@PreAuthorize          → Role-based authorization
@Validated             → Enable validation
@Valid                 → Validate object
@Autowired             → Dependency injection
@RequiredArgsConstructor → Lombok - inject final fields
@Entity                → JPA entity
@Table                 → Database table mapping
@Id                    → Primary key
@GeneratedValue        → Auto-increment ID
@Column                → Column mapping
@Enumerated            → Enum mapping
@Component             → Spring component
@Bean                  → Spring bean creation
```

---

## 🎓 Học Thêm

- **MVC Pattern**: Model-View-Controller architecture
- **REST API**: Representational State Transfer
- **JWT**: JSON Web Token authentication
- **Spring Security**: Framework bảo mật
- **JPA/Hibernate**: ORM (Object-Relational Mapping)
- **DTO Pattern**: Data Transfer Object design pattern
- **Exception Handling**: Centralized error management

---

**Tài liệu được cập nhật lần cuối:** 30 tháng 11 năm 2025
