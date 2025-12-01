# MVC Spring Boot API

A modern Spring Boot REST API application with PostgreSQL database integration.

## 📋 Project Structure

```
src/
├── main/
│   ├── java/com/mvcCore/
│   │   ├── Application.java          - Main Spring Boot Application
│   │   ├── config/
│   │   │   └── AppConfig.java        - CORS & Application Configuration
│   │   ├── controller/
│   │   │   ├── HealthController.java - Health Check Endpoint
│   │   │   ├── UserController.java   - User REST API
│   │   │   ├── ApiResponse.java      - Standard API Response Wrapper
│   │   │   └── GlobalExceptionHandler.java - Exception Handling
│   │   ├── service/
│   │   │   ├── BaseService.java      - Generic Service Interface
│   │   │   ├── UserService.java      - User Business Logic Interface
│   │   │   └── impl/
│   │   │       └── UserServiceImpl.java - User Service Implementation
│   │   ├── repository/
│   │   │   ├── BaseRepository.java   - Generic Repository Interface
│   │   │   └── UserRepository.java   - User Data Access Layer
│   │   └── model/
│   │       ├── BaseEntity.java       - Base Entity with Timestamps
│   │       └── User.java             - User Entity Model
│   └── resources/
│       └── application.properties    - Application Configuration
└── test/
    └── java/com/mvcCore/
        └── ApplicationTests.java     - Basic Unit Tests
```

## 🛠️ Technology Stack

- **Java 17**
- **Spring Boot 3.1.5**
- **Spring Data JPA**
- **PostgreSQL 15+**
- **Maven 3.6+**
- **Lombok**
- **JUnit 5**

## 📦 Prerequisites

Before running the project, ensure you have:

- Java 17 or higher installed
- Maven 3.6 or higher installed
- PostgreSQL 15+ running
- Git (for version control)

### Check installations:

```bash
java -version
mvn -version
psql --version
```

## 🚀 Getting Started

### 1. Clone the Repository

```bash
cd /path/to/1-mvc-spring-boot
```

### 2. Configure Environment Variables

Create a `.env` file from the example:

```bash
cp .env.example .env
```

Edit `.env` with your database credentials:

```properties
DB_URL=jdbc:postgresql://localhost:5432/mvc_db
DB_USERNAME=postgres
DB_PASSWORD=your_password
```

**For Neon Cloud PostgreSQL:**

```properties
DB_URL=jdbc:postgresql://ep-shy-voice-a1bz2c8m-pooler.ap-southeast-1.aws.neon.tech/dev
DB_USERNAME=test-db_owner
DB_PASSWORD=qrEpk9Gisy6n
```

### 3. Set Environment Variables

**On macOS/Linux:**

```bash
export DB_URL=jdbc:postgresql://localhost:5432/mvc_db
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
```

**On Windows (PowerShell):**

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/mvc_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_password"
```

### 4. Build the Project

```bash
mvn clean install
```

### 5. Run the Application

**Option A: Using Maven**

```bash
mvn spring-boot:run
```

**Option B: Using Java directly**

```bash
java -jar target/mvc-core-1.0.0.jar
```

**Option C: Using IDE**

- Right-click on `Application.java`
- Select "Run" or "Run As" → "Java Application"

### 6. Verify Application is Running

```bash
curl http://localhost:8080/api/v1/health
```

Expected response:

```json
{
  "code": 200,
  "message": "Application is running",
  "data": {
    "status": "UP"
  },
  "timestamp": "2025-11-26T10:30:00"
}
```

## 📡 API Endpoints

### Health Check

```http
GET /api/v1/health
```

### User Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/users` | Get all users |
| GET | `/api/v1/users/{id}` | Get user by ID |
| GET | `/api/v1/users/email/{email}` | Get user by email |
| GET | `/api/v1/users/active/list` | Get all active users |
| POST | `/api/v1/users` | Create new user |
| PUT | `/api/v1/users/{id}` | Update user |
| DELETE | `/api/v1/users/{id}` | Delete user |

### Example Requests

**Create User:**

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "secure123",
    "fullName": "John Doe",
    "phoneNumber": "0123456789"
  }'
```

**Get All Users:**

```bash
curl http://localhost:8080/api/v1/users
```

**Get User by ID:**

```bash
curl http://localhost:8080/api/v1/users/1
```

**Update User:**

```bash
curl -X PUT http://localhost:8080/api/v1/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe_updated",
    "email": "newemail@example.com",
    "fullName": "John Doe Updated"
  }'
```

**Delete User:**

```bash
curl -X DELETE http://localhost:8080/api/v1/users/1
```

## 🗄️ Database Setup

### For Local PostgreSQL

1. Create database:

```sql
CREATE DATABASE mvc_db;
```

2. Connect to database:

```bash
psql -U postgres -d mvc_db
```

3. Spring will auto-create tables on startup (due to `spring.jpa.hibernate.ddl-auto=update`)

### For Neon Cloud PostgreSQL

1. Go to [Neon](https://neon.tech)
2. Create a new project and database
3. Copy the connection string and credentials
4. Update `.env` file with your credentials

## 🧪 Testing

### Run Tests

```bash
mvn test
```

### Run Tests with Coverage

```bash
mvn test jacoco:report
```

## ⚡ Quick Code Generation (Codegen)

This project includes a powerful code generation script that automatically generates all layers of a complete REST API (Model, DTO, Repository, Mapper, Service, Controller) from templates.

### 🎯 Features

- ✅ Generates complete CRUD REST API in seconds
- ✅ Creates 7 files: Entity, DTO, Repository, Mapper, Service, ServiceImpl, Controller
- ✅ Auto-converts entity names to proper table names (e.g., `ProductCategory` → `product_categories`)
- ✅ Interactive prompts for file replacement
- ✅ Colored output with summary report
- ✅ Follows project conventions and patterns

### 🚀 Quick Start

**Generate a new entity with full REST API:**

```bash
./codegen/generate.sh EntityName
```

**Example:**

```bash
./codegen/generate.sh Product
# Generates: Model, DTO, Repository, Mapper, Service, ServiceImpl, Controller
# API endpoint: /api/v1/products
```

```bash
./codegen/generate.sh OrderItem
# Generates: Model, DTO, Repository, Mapper, Service, ServiceImpl, Controller
# API endpoint: /api/v1/orderItems
# Table name: order_items
```

### 📋 What Gets Generated

For entity `Product`, the script creates:

```
src/main/java/com/coremvc/
├── model/Product.java                    # JPA Entity with table mapping
├── dto/ProductDto.java                   # Data Transfer Object
├── repository/ProductRepository.java      # JPA Repository interface
├── mapper/ProductMapper.java             # MapStruct entity-DTO mapper
├── service/ProductService.java           # Service interface
├── service/impl/ProductServiceImpl.java  # Service implementation
└── controller/ProductController.java     # REST API Controller
```

### 🎨 Generated Features

Each generated entity includes:

**Model (Entity):**
- Extends `BaseEntity` (id, createdAt, updatedAt)
- JPA annotations (`@Entity`, `@Table`)
- Lombok annotations (`@Data`, `@Builder`)
- Auto-generated table name

**DTO:**
- Clean data transfer object
- Lombok annotations
- JSON serialization ready

**Repository:**
- Extends `JpaRepository`
- Custom query methods ready to add

**Mapper:**
- MapStruct interface
- Entity ↔ DTO conversion methods
- List conversion support

**Service:**
- CRUD operation interfaces
- findAll, findById, create, update, delete

**Service Implementation:**
- Complete business logic
- Exception handling
- Transaction management

**Controller:**
- RESTful API endpoints
- Standard API response format
- Request validation
- CRUD endpoints:
  - `GET /api/v1/{entity}s` - Get all
  - `GET /api/v1/{entity}s/{id}` - Get by ID
  - `POST /api/v1/{entity}s` - Create
  - `PUT /api/v1/{entity}s/{id}` - Update
  - `DELETE /api/v1/{entity}s/{id}` - Delete

### 📝 Usage Examples

**1. Generate Settings Entity:**

```bash
./codegen/generate.sh Setting
```

Output:
```
🚀 Generating Spring Boot Entity: Setting
📦 Package: com.coremvc
📄 Table name: settings

✓ Created Model
✓ Created DTO
✓ Created Repository
✓ Created Mapper
✓ Created Service Interface
✓ Created Service Implementation
✓ Created Controller

✅ Generation completed!

📊 Summary Report:
  ✓ Created: 7 file(s)
  🔄 Replaced: 0 file(s)
  ↪ Skipped: 0 file(s)

🎯 API Endpoints: /api/v1/settings
```

**2. Generate Category Entity:**

```bash
./codegen/generate.sh Category
```

**3. Replace Existing Entity:**

If files already exist, you'll be prompted:

```bash
./codegen/generate.sh Product
⚠️  File already exists: Model
   Replace it? (y/n): y
   ✓ Replaced Model
```

### 🔧 Customization

After generation, you can customize the generated files:

**1. Add Entity Fields:**

Edit `model/{Entity}.java`:
```java
@Entity
@Table(name = "products")
public class Product extends BaseEntity {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    // Add more fields as needed
}
```

**2. Add Custom Queries:**

Edit `repository/{Entity}Repository.java`:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContaining(String name);
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
}
```

**3. Add Business Logic:**

Edit `service/impl/{Entity}ServiceImpl.java`:
```java
public List<ProductDto> searchByName(String name) {
    return productRepository.findByNameContaining(name)
        .stream()
        .map(productMapper::toDto)
        .collect(Collectors.toList());
}
```

**4. Add Custom Endpoints:**

Edit `controller/{Entity}Controller.java`:
```java
@GetMapping("/search")
public ApiResponse<List<ProductDto>> search(@RequestParam String name) {
    return ApiResponse.success(productService.searchByName(name));
}
```

### 📂 Template Location

Templates are located in:
```
codegen/templates/
├── Model.java.template
├── Dto.java.template
├── Repository.java.template
├── Mapper.java.template
├── Service.java.template
├── ServiceImpl.java.template
└── Controller.java.template
```

You can modify these templates to match your specific requirements.

### ⚠️ Important Notes

- **Entity Naming:** Use PascalCase (e.g., `Product`, `OrderItem`, `UserProfile`)
- **Table Naming:** Auto-converted to snake_case plural (e.g., `products`, `order_items`, `user_profiles`)
- **API Endpoints:** Auto-generated as `/api/v1/{entityLowerCase}s`
- **Validation:** Add validation annotations to DTO fields after generation
- **Relationships:** Add JPA relationships (`@ManyToOne`, `@OneToMany`) manually

### 🎯 Next Steps After Generation

1. ✅ Add entity fields and relationships
2. ✅ Update DTO with validation annotations
3. ✅ Add custom repository queries
4. ✅ Implement business logic in service
5. ✅ Add custom controller endpoints
6. ✅ Run the application and test endpoints
7. ✅ Write unit tests

## 🔧 Configuration

### application.properties

```properties
# Server
spring.application.name=mvc-core
server.port=8080

# Database
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/mvc_db}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:}

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Logging
logging.level.root=INFO
logging.level.com.coremvc=DEBUG
```

## 📚 Documentation

### Response Format

All API responses follow this structure:

```json
{
  "code": 200,
  "message": "Success message",
  "data": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com"
  },
  "timestamp": "2025-11-26T10:30:00"
}
```

### Error Response

```json
{
  "code": 400,
  "message": "Error description",
  "data": null,
  "timestamp": "2025-11-26T10:30:00"
}
```

## 🔐 Security Notes

- ⚠️ **Never commit `.env` file to Git**
- ✅ Use `.env.example` as template only
- ✅ Store credentials in environment variables
- ✅ Use HTTPS in production
- ✅ Implement authentication/authorization as needed

## 🐛 Troubleshooting

### Connection Refused

```
Error: Connection refused
```

**Solution:** Ensure PostgreSQL is running and credentials are correct.

```bash
# Check if PostgreSQL is running (macOS)
brew services list | grep postgresql
```

### Port Already in Use

```
Error: Address already in use: bind
```

**Solution:** Change port in `application.properties` or kill process:

```bash
lsof -i :8080
kill -9 <PID>
```

### Database Connection Failed

```
Error: Failed to initialize pool
```

**Solution:** Verify database URL, username, and password in `.env` file.

## 📝 Build & Package

### Build JAR

```bash
mvn clean package
```

### Build Without Tests

```bash
mvn clean package -DskipTests
```

### Find JAR File

```bash
find target -name "*.jar" -type f
```

## 🚢 Deployment

### Docker Support (Optional)

Create `Dockerfile`:

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/mvc-core-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and run:

```bash
docker build -t mvc-api .
docker run -p 8080:8080 -e DB_URL=... mvc-api
```

## 📄 License

MIT License - Feel free to use this project for learning and development.

## 👨‍💻 Author

Spring Boot MVC API Project

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📞 Support

For issues and questions, please open an GitHub issue.

---

**Last Updated:** November 26, 2025
