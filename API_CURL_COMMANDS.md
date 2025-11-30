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
curl -X POST http://localhost:8080/api/auth/register \
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
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "USER",
    "isActive": true,
    "createdAt": "2025-11-27T10:00:00",
    "updatedAt": "2025-11-27T10:00:00"
  },
  "success": true
}
```

### Login User
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "USER",
    "isActive": true,
    "createdAt": "2025-11-27T10:00:00",
    "updatedAt": "2025-11-27T10:00:00"
  },
  "success": true
}
```

### Get Current User Profile
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <TOKEN>"
```

**Response (200):**
```json
{
  "message": "Success",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "USER",
    "isActive": true,
    "createdAt": "2025-11-27T10:00:00",
    "updatedAt": "2025-11-27T10:00:00"
  },
  "success": true
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
  "success": true
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
  "message": "Success",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": "USER",
    "isActive": true,
    "createdAt": "2025-11-27T10:00:00",
    "updatedAt": "2025-11-27T10:00:00"
  },
  "success": true
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
  "success": true
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
  "message": "User deleted successfully",
  "data": null,
  "success": true
}
```

---

## Authentication Notes

- Replace `<TOKEN>` with the actual JWT token received from login/register
- Replace `<ADMIN_TOKEN>` with a JWT token from an admin user
- All user management endpoints (`/api/v1/users/**`) require ADMIN role
- All endpoints that require authentication expect the token in the `Authorization` header with the format: `Bearer <TOKEN>`
- The JWT token expires after the configured expiration time (default: 24 hours)
- If the token is invalid or expired, you'll receive a 401 Unauthorized response

---

## Error Responses

### 401 Unauthorized
```json
{
  "message": "Unauthorized",
  "data": null,
  "success": false
}
```

### 404 Not Found
```json
{
  "message": "User not found",
  "data": null,
  "success": false
}
```

### 400 Bad Request (Registration failure)
```json
{
  "message": "Email already exists",
  "data": null,
  "success": false
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
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "password": "Test1234!",
    "fullName": "Test User"
  }'
```
Save the token from the response.

### 3. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "password": "Test1234!"
  }'
```

### 4. Get Current User
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <TOKEN_FROM_LOGIN>"
```

### 5. Get All Users (with Pagination)
```bash
# Default pagination
curl -X GET "http://localhost:8080/api/v1/users" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"

# Custom pagination
curl -X GET "http://localhost:8080/api/v1/users?page=0&size=20&sortBy=email&sortDirection=ASC" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

### 6. Update User
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
   - `TOKEN`: (set this after login/register)

Then reference these variables in your requests:
- URL: `{{BASE_URL}}/api/users`
- Header: `Authorization: Bearer {{TOKEN}}`
