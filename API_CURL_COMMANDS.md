# API Curl Commands - MVC Spring Boot System

## Base URL
```
http://localhost:8080
```

---

## 1. Health Check

### Get Application Health
```bash
curl -X GET http://localhost:8080/api/v1/health
```

**Response (200):**
```json
{
  "status": "UP",
  "message": "Application is running"
}
```

---

## 2. Authentication APIs

### Register User
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "fullName": "John Doe"
  }'
```

**Response (201):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "USER",
    "isActive": true,
    "createdAt": "2025-11-27T10:00:00",
    "updatedAt": "2025-11-27T10:00:00"
  },
  "message": "Register successfully",
  "success": true
}
```

### Login User
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

**Response (200):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "USER",
    "isActive": true,
    "createdAt": "2025-11-27T10:00:00",
    "updatedAt": "2025-11-27T10:00:00"
  },
  "message": "Login successfully",
  "success": true
}
```

### Refresh Access Token
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'
```

**Response (200):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "USER",
    "isActive": true,
    "createdAt": "2025-11-27T10:00:00",
    "updatedAt": "2025-11-27T10:00:00"
  },
  "message": "Token refreshed successfully",
  "success": true
}
```

### Get Current User Profile
```bash
curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

**Response (200):**
```json
{
  "code": 200,
  "message": "User info retrieved successfully",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "USER",
    "isActive": true,
    "createdAt": "2025-11-27T10:00:00",
    "updatedAt": "2025-11-27T10:00:00"
  },
  "success": true,
  "timestamp": "2025-11-30T10:30:00"
}
```

---

## 3. User Management APIs

### Get All Users (with Pagination)
Requires ADMIN role authentication

```bash
# Get first page with default settings (page=0, size=10, sortBy=id, sortDirection=ASC)
curl -X GET "http://localhost:8080/api/v1/users" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"

# Get specific page with custom size
curl -X GET "http://localhost:8080/api/v1/users?page=0&size=20" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"

# Sort by fullName in descending order
curl -X GET "http://localhost:8080/api/v1/users?sortBy=fullName&sortDirection=DESC" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"

# Combined parameters - page 2, 15 items per page, sorted by createdAt descending
curl -X GET "http://localhost:8080/api/v1/users?page=1&size=15&sortBy=createdAt&sortDirection=DESC" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

**Query Parameters:**
- `page` (optional, default: 0): Page number (zero-based)
- `size` (optional, default: 10): Number of items per page
- `sortBy` (optional, default: "id"): Field to sort by (id, email, fullName, createdAt, updatedAt)
- `sortDirection` (optional, default: "ASC"): Sort direction (ASC or DESC)

**Response (200):**
```json
{
  "code": 200,
  "message": "Users retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "email": "user1@example.com",
        "fullName": "John Doe",
        "role": "USER",
        "isActive": true,
        "createdAt": "2025-11-27T10:00:00",
        "updatedAt": "2025-11-27T10:00:00"
      },
      {
        "id": 2,
        "email": "user2@example.com",
        "fullName": "Jane Smith",
        "role": "ADMIN",
        "isActive": true,
        "createdAt": "2025-11-27T10:05:00",
        "updatedAt": "2025-11-27T10:05:00"
      }
    ],
    "pageable": {
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      },
      "pageNumber": 0,
      "pageSize": 10,
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalPages": 5,
    "totalElements": 45,
    "last": false,
    "number": 0,
    "size": 10,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "numberOfElements": 10,
    "first": true,
    "empty": false
  },
  "success": true,
  "timestamp": "2025-11-30T10:30:00"
}
```

### Get User by ID
Requires ADMIN role authentication

```bash
curl -X GET http://localhost:8080/api/v1/users/1 \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

**Response (200):**
```json
{
  "code": 200,
  "message": "User retrieved successfully",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "USER",
    "isActive": true,
    "createdAt": "2025-11-27T10:00:00",
    "updatedAt": "2025-11-27T10:00:00"
  },
  "success": true,
  "timestamp": "2025-11-30T10:30:00"
}
```

### Update User
Requires ADMIN role authentication

```bash
curl -X PUT http://localhost:8080/api/v1/users/1 \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Updated",
    "isActive": true
  }'
```

**Response (200):**
```json
{
  "code": 200,
  "message": "User updated successfully",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "John Updated",
    "role": "USER",
    "isActive": true,
    "createdAt": "2025-11-27T10:00:00",
    "updatedAt": "2025-11-27T10:15:00"
  },
  "success": true,
  "timestamp": "2025-11-30T10:30:00"
}
```

### Delete User
Requires ADMIN role authentication

```bash
curl -X DELETE http://localhost:8080/api/v1/users/1 \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

**Response (200):**
```json
{
  "code": 200,
  "message": "User deleted successfully",
  "data": null,
  "success": true,
  "timestamp": "2025-11-30T10:30:00"
}
```

---

## Authentication Notes

- Replace `<ACCESS_TOKEN>` with the actual JWT access token received from login/register
- Replace `<ADMIN_TOKEN>` with a JWT access token from an admin user
- All user management endpoints (`/api/v1/users/**`) require ADMIN role
- All endpoints that require authentication expect the token in the `Authorization` header with the format: `Bearer <ACCESS_TOKEN>`
- The access token expires after the configured expiration time (default: 24 hours / 86400000ms)
- The refresh token expires after 7 days (604800000ms)
- Use the `/api/v1/auth/refresh` endpoint to get a new access token using your refresh token
- If the token is invalid or expired, you'll receive a 401 Unauthorized response

---

## Error Responses

### 401 Unauthorized
```json
{
  "code": "UNAUTHORIZED",
  "message": "Unauthorized",
  "status": 401,
  "timestamp": "2025-11-30T10:30:00",
  "path": "/api/v1/users",
  "details": {}
}
```

### 403 Forbidden (Access Denied)
```json
{
  "code": "FORBIDDEN",
  "message": "Access denied",
  "status": 403,
  "timestamp": "2025-11-30T10:30:00",
  "path": "/api/v1/users",
  "details": {}
}
```

### 404 Not Found
```json
{
  "code": "NOT_FOUND",
  "message": "User not found",
  "status": 404,
  "timestamp": "2025-11-30T10:30:00",
  "path": "/api/v1/users/1",
  "details": {}
}
```

### 400 Bad Request (Registration failure)
```json
{
  "code": "BAD_REQUEST",
  "message": "Email already exists",
  "status": 400,
  "timestamp": "2025-11-30T10:30:00",
  "path": "/api/v1/auth/register",
  "details": {}
}
```

---

## Testing Workflow

### 1. Health Check
```bash
curl -X GET http://localhost:8080/api/v1/health
```

### 2. Register New User
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "password": "Test1234!",
    "fullName": "Test User"
  }'
```
Save the accessToken and refreshToken from the response.

### 3. Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "password": "Test1234!"
  }'
```

### 4. Refresh Token
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<REFRESH_TOKEN_FROM_LOGIN>"
  }'
```

### 5. Get Current User
```bash
curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer <ACCESS_TOKEN_FROM_LOGIN>"
```

### 6. Get All Users (with Pagination)
```bash
# Default pagination
curl -X GET "http://localhost:8080/api/v1/users" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"

# Custom pagination
curl -X GET "http://localhost:8080/api/v1/users?page=0&size=20&sortBy=email&sortDirection=ASC" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

### 7. Update User
```bash
curl -X PUT http://localhost:8080/api/v1/users/1 \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Updated Test User",
    "isActive": true
  }'
```

---

## Using with Postman or Insomnia

For easier testing, you can import these requests into Postman or Insomnia by:

1. Creating a new collection
2. Adding requests for each endpoint
3. Setting environment variables for:
   - `BASE_URL`: http://localhost:8080
   - `ACCESS_TOKEN`: (set this after login/register)
   - `REFRESH_TOKEN`: (set this after login/register)
   - `ADMIN_TOKEN`: (set this after admin login)

Then reference these variables in your requests:
- URL: `{{BASE_URL}}/api/v1/users`
- Header: `Authorization: Bearer {{ACCESS_TOKEN}}`
