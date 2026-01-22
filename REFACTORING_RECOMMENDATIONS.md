# Redline Codebase Refactoring Recommendations

## Executive Summary

The Redline project is a Spring Boot 3.4.1 application that demonstrates solid foundational architecture, but there are
opportunities to align with modern Spring Boot best practices and clean code principles. Below are comprehensive
recommendations organized by priority and category.

---

## 1. ARCHITECTURE & LAYERING

### 1.1 Domain-Driven Design (DDD) Separation (HIGH PRIORITY)

**Current State:** Models and DTOs are mixed; no clear separation between entities, value objects, and DTOs.

**Recommendations:**

- **Create a dedicated domain layer** with clear boundaries:
    - `domain/model/` - JPA entities (persistence concerns only)
    - `domain/entity/` - Pure domain objects (no annotations)
    - `api/dto/request/` - Request DTOs
    - `api/dto/response/` - Response DTOs
    - `application/` - Application/service layer (use cases)

**Example Structure:**

```
src/main/java/com/metaformsystems/redline/
├── api/
│   ├── controller/
│   └── dto/
│       ├── request/
│       └── response/
├── application/
│   ├── service/
│   └── exception/
├── domain/
│   ├── entity/
│   ├── aggregate/
│   ├── repository/
│   ├── service/
│   └── exception/
├── infrastructure/
│   ├── persistence/
│   ├── client/
│   └── config/
└── shared/
    ├── event/
    └── util/
```

**Benefits:**

- Clear separation of concerns
- Easier to test and maintain
- Independent domain logic from frameworks
- Better for microservices evolution

---

## 2. SERVICE LAYER REFACTORING

### 2.1 TenantService Size & Responsibilities (HIGH PRIORITY)

**Current State:** `TenantService` is extremely large (485 lines) with multiple responsibilities:

- Tenant management
- Participant deployment
- Data transfer orchestration
- Catalog management
- Contract negotiation
- File uploads

**Recommendations:**
Split into focused services following Single Responsibility Principle:

1. **TenantManagementService** - Create, read, update tenants
2. **ParticipantService** - Manage participants and deployments
3. **DataTransferService** - Handle file uploads and downloads
4. **ContractNegotiationService** - Manage contract negotiations
5. **CatalogService** - Handle catalog requests and caching

**Example:**

```java
// Before: One large service
@Service
public class TenantService {
    public void registerTenant(...) {
    }

    public void deployParticipant(...) {
    }

    public void uploadFile(...) {
    }

    public void requestCatalog(...) {
    }

    public void initiateContractNegotiation(...) {
    }
}

// After: Focused services
@Service
public class TenantManagementService {
    public TenantResource registerTenant(...) {
    }
}

@Service
public class ParticipantService {
    public ParticipantResource deployParticipant(...) {
    }
}

@Service
public class DataTransferService {
    public UploadedFile uploadFile(...) {
    }
}
```

---

## 3. DAO/ENTITY LAYER IMPROVEMENTS

### 3.1 Naming Convention Clarification (MEDIUM PRIORITY)

**Current State:** Confusing naming - `dao/` package contains DTOs/transfer objects, not Data Access Objects.

**Recommendations:**

- Rename `dao/` package to `api/dto/` or `api/request/`
- Update naming to be explicit:
    - `NewTenantRegistration` → `RegisterTenantRequest`
    - `TenantResource` → `TenantResponse`
    - `DataspaceResource` → `DataspaceResponse`

**Rationale:** Aligns with Spring naming conventions and clarifies intent.

### 3.2 Use Records for Immutable DTOs (MEDIUM PRIORITY)

**Current State:** DTOs are mutable POJOs using getters/setters.

**Recommendations:**

```java
// Before: Mutable POJO
public class NewTenantRegistration {
    private String name;
    private Map<String, Object> properties;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

// After: Immutable record
public record RegisterTenantRequest(
        String name,
        Map<String, Object> properties
) {
}
```

**Benefits:**

- Immutability by default
- Less boilerplate (80% less code)
- Thread-safe
- Better for JSON serialization

**Note:** Java 21 is already in use, making records a perfect fit.

### 3.3 Separate Entity Converters (MEDIUM PRIORITY)

**Current State:** DAO classes used for conversion; entity mapping scattered.

**Recommendations:**

```java
// Create mapper classes for entity ↔ DTO conversion
@Component
public class TenantMapper {
    public TenantResponse toResponse(Tenant entity) {
    }

    public Tenant toEntity(RegisterTenantRequest request) {
    }
}
```

Use MapStruct for complex mappings:

```gradle
implementation 'org.mapstruct:mapstruct:1.6.0'
annotationProcessor 'org.mapstruct:mapstruct-processor:1.6.0'
```

---

## 4. ERROR HANDLING & VALIDATION

### 4.1 Enhance GlobalExceptionHandler (MEDIUM PRIORITY)

**Current State:** Basic exception handling; missing validation exception handling.

**Recommendations:**

```java

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage
                ));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("Validation failed", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("Invalid request body"));
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleExternalApiError(WebClientResponseException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ErrorResponse("External service error: " + ex.getMessage()));
    }
}
```

### 4.2 Create Custom Exception Hierarchy (MEDIUM PRIORITY)

**Current State:** Only `ObjectNotFoundException` exists.

**Recommendations:**

```java
public abstract class ApplicationException extends RuntimeException {
    public ApplicationException(String message) {
        super(message);
    }

    public abstract HttpStatus getHttpStatus();
}

public class ResourceNotFoundException extends ApplicationException {
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}

public class InvalidRequestException extends ApplicationException {
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}

public class ExternalServiceException extends ApplicationException {
    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.SERVICE_UNAVAILABLE;
    }
}
```

### 4.3 Input Validation with Annotations (MEDIUM PRIORITY)

**Current State:** Minimal validation in request DTOs.

**Recommendations:**

```java
public record RegisterTenantRequest(
        @NotBlank(message = "Tenant name is required")
        String name,

        @NotNull(message = "Properties cannot be null")
        Map<String, Object> properties
) {
}

@RestController
@RequestMapping("/api/ui")
public class TenantController {

    @PostMapping("service-providers/{serviceProviderId}/tenants")
    public ResponseEntity<TenantResponse> registerTenant(
            @PathVariable Long serviceProviderId,
            @Valid @RequestBody RegisterTenantRequest request
    ) {
        return ResponseEntity.ok(service.registerTenant(serviceProviderId, request));
    }
}
```

---

## 5. CONTROLLER IMPROVEMENTS

### 5.1 Consistent API Response Structure (HIGH PRIORITY)

**Current State:** Inconsistent response patterns; some endpoints return resources, some return strings.

**Recommendations:**

```java
// Create consistent response wrapper
public record ApiResponse<T>(
                boolean success,
                T data,
                @Nullable String message,
                long timestamp
        ) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, System.currentTimeMillis());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, System.currentTimeMillis());
    }
}

// Use in controllers
@GetMapping("dataspaces")
public ResponseEntity<ApiResponse<List<DataspaceResponse>>> getAllDataspaces() {
    var dataspaces = service.getAllDataspaces();
    return ResponseEntity.ok(ApiResponse.success(dataspaces));
}
```

### 5.2 Extract Common Path Patterns (MEDIUM PRIORITY)

**Current State:** Deeply nested paths with repetitive `{providerId}/{tenantId}/{participantId}`.

**Recommendations:**

```java
// Option 1: Create base controller with path variable extraction
@RestController
@RequestMapping("/api/ui/service-providers/{providerId}/tenants/{tenantId}/participants/{participantId}")
public class ParticipantOperationsController {

    @PostMapping("/files")
    public ResponseEntity<UploadedFileResponse> uploadFile(
            @PathVariable Long providerId,
            @PathVariable Long tenantId,
            @PathVariable Long participantId,
            @RequestParam MultipartFile file
    ) {
        return ResponseEntity.ok(service.uploadFile(participantId, file));
    }
}

// Option 2: Use path variable validation
@RestController
@RequestMapping("/api/ui")
public class FileOperationsController {

    private final PathValidationService pathValidator;

    @PostMapping("service-providers/{providerId}/tenants/{tenantId}/participants/{participantId}/files")
    public ResponseEntity<UploadedFileResponse> uploadFile(
            @PathVariable Long providerId,
            @PathVariable Long tenantId,
            @PathVariable Long participantId,
            @RequestParam MultipartFile file
    ) {
        pathValidator.validateParticipantPath(providerId, tenantId, participantId);
        return ResponseEntity.ok(service.uploadFile(participantId, file));
    }
}
```

### 5.3 Separate Controllers by Concern (MEDIUM PRIORITY)

**Current State:** `TenantController` handles dataspaces, service providers, tenants, and participants.

**Recommendations:**

```
controller/
├── DataspaceController.java        // GET /api/ui/dataspaces
├── ServiceProviderController.java  // /api/ui/service-providers
├── TenantController.java           // /api/ui/service-providers/{id}/tenants
├── ParticipantController.java      // /api/ui/.../participants
├── FileController.java             // File upload/download
└── ContractController.java         // Contract negotiations
```

---

## 6. CONFIGURATION & DEPENDENCY INJECTION

### 6.1 Consolidate Configuration Classes (MEDIUM PRIORITY)

**Current State:** 8 separate configuration classes for different clients and features.

**Recommendations:**

```java
// Create logical grouping
config/
        ├──SecurityConfiguration.java
├──client/
        │   ├──ExternalClientConfiguration.java    // All external clients
│   └──WebClientConfiguration.java
└──database/
        └──PersistenceConfiguration.java

// Example consolidated client config
@Configuration

public class ExternalClientConfiguration {

    @Bean
    public TenantManagerClient tenantManagerClient(WebClient webClient) {
    }

    @Bean
    public ManagementApiClient managementApiClient(WebClient webClient) {
    }

    @Bean
    public DataPlaneApiClient dataPlaneApiClient(WebClient webClient) {
    }
}
```

### 6.2 Use @ConfigurationProperties (MEDIUM PRIORITY)

**Current State:** Configuration scattered; some values injected via `@Value`.

**Recommendations:**

```java
// Create typed configuration objects
@ConfigurationProperties(prefix = "app")
public record AppConfiguration(
                Cors cors,
                Security security,
                ExternalServices externalServices
        ) {
    public record Cors(String allowedOrigins) {
    }

    public record Security(String jwkSetUri) {
    }

    public record ExternalServices(ExternalServiceConfig tenantManager) {
    }

    public record ExternalServiceConfig(String url) {
    }
}

// In SecurityConfig
@Configuration
public class SecurityConfig {

    private final AppConfiguration config;

    public SecurityConfig(AppConfiguration config) {
        this.config = config;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(List.of(config.cors().allowedOrigins()));
        // ...
        return source;
    }
}
```

**Benefits:**

- Type-safe configuration
- IDE autocomplete support
- Centralized configuration management
- Easier to extend

---

## 7. TESTING IMPROVEMENTS

### 7.1 Organize Test Structure (MEDIUM PRIORITY)

**Current State:** All tests in single `test/` directory.

**Recommendations:**

```
src/test/java/com/metaformsystems/redline/
├── unit/
│   ├── service/
│   ├── controller/
│   └── util/
├── integration/
│   ├── controller/
│   └── service/
└── testfixtures/
    ├── builders/
    └── factories/
```

### 7.2 Use Test Fixtures & Builders (MEDIUM PRIORITY)

**Current State:** Test data creation scattered across tests.

**Recommendations:**

```java
// Create test fixtures for common entities
public class TenantTestFixtures {
    public static Tenant.TenantBuilder defaultTenant() {
        return Tenant.builder()
                .name("Test Tenant")
                .correlationId(UUID.randomUUID().toString());
    }
}

// Use builders in tests
@Test
void shouldRegisterTenant() {
    var tenant = TenantTestFixtures.defaultTenant()
            .name("Custom Tenant")
            .build();

    var result = service.registerTenant(tenant);

    assertThat(result).isNotNull();
}
```

### 7.3 Adopt TestContainers (MEDIUM PRIORITY)

**Current State:** Uses H2 for testing; no real PostgreSQL testing.

**Recommendations:**

```gradle
testImplementation 'org.testcontainers:testcontainers:1.19.7'
testImplementation 'org.testcontainers:postgresql:1.19.7'
```

```java

@SpringBootTest
@Testcontainers
public class TenantServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("redlinetest")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void shouldPersistTenant() {
        // Uses real PostgreSQL container
    }
}
```

---

## 8. ENTITY & JPA IMPROVEMENTS

### 8.1 Use Lombok for Entity Boilerplate (LOW PRIORITY)

**Current State:** Manual getters/setters in entities.

**Recommendations:**

```gradle
compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'
```

```java

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
public class Tenant extends VersionedEntity {

    @NotBlank
    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "service_provider_id")
    private ServiceProvider serviceProvider;
}
```

### 8.2 Add Auditing Support (MEDIUM PRIORITY)

**Current State:** Manual timestamp management in `VersionedEntity`.

**Recommendations:**

```java

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditedEntity {

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(nullable = false)
    private String modifiedBy;
}
```

Enable auditing in configuration:

```java

@Configuration
@EnableJpaAuditing
public class PersistenceConfiguration {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of(
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName()
        );
    }
}
```

### 8.3 Improve Relationship Management (MEDIUM PRIORITY)

**Current State:** Cascade settings may cause unintended deletes.

**Recommendations:**

```java
// Review and specify cascade operations explicitly
@Entity
public class Tenant {

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.PERSIST)
    private Set<Participant> participants = new HashSet<>();

    public void addParticipant(Participant participant) {
        participants.add(participant);
        participant.setTenant(this);
    }

    public void removeParticipant(Participant participant) {
        participants.remove(participant);
        participant.setTenant(null);
    }
}
```

---

## 9. CACHING & PERFORMANCE

### 9.1 Implement Spring Cache Abstraction (MEDIUM PRIORITY)

**Current State:** Manual `ConcurrentLruCache` used in `TenantService`.

**Recommendations:**

```gradle
implementation 'org.springframework.boot:spring-boot-starter-cache'
```

```java

@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("catalogs", "policies", "participants");
    }
}

// Use in service
@Service
public class CatalogService {

    @Cacheable(value = "catalogs", key = "#participantId + '_' + #counterPartyId")
    public Catalog requestCatalog(String participantId, String counterPartyId) {
        return fetchCatalogFromExternal(participantId, counterPartyId);
    }

    @CacheEvict(value = "catalogs", key = "#participantId + '_*'")
    public void invalidateParticipantCache(String participantId) {
    }
}
```

**Benefits:**

- Cleaner than manual caching
- Declarative approach
- Easy to switch cache implementations
- Monitoring support

---

## 10. LOGGING & OBSERVABILITY

### 10.1 Structured Logging (MEDIUM PRIORITY)

**Current State:** Basic SLF4J logging.

**Recommendations:**

```gradle
implementation 'io.micrometer:micrometer-tracing-bridge-brave'
implementation 'io.zipkin.brave:brave-instrumentation-http'
```

```java

@Component
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) {
        var startTime = System.currentTimeMillis();
        var response = execution.execute(request, body);
        var duration = System.currentTimeMillis() - startTime;

        log.info("External API call",
                "method", request.getMethod(),
                "url", request.getURI(),
                "status", response.getStatusCode(),
                "duration_ms", duration
        );

        return response;
    }
}
```

### 10.2 Add Metrics with Micrometer (LOW PRIORITY)

**Current State:** No application metrics.

**Recommendations:**

```gradle
implementation 'io.micrometer:micrometer-registry-prometheus'
```

```java

@Component
public class TenantMetrics {

    private final MeterRegistry meterRegistry;

    public TenantMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordTenantCreation() {
        meterRegistry.counter("tenants.created").increment();
    }

    public void recordDeploymentTime(long durationMs) {
        meterRegistry.timer("deployment.duration.ms").record(durationMs, TimeUnit.MILLISECONDS);
    }
}
```

---

## 11. DOCUMENTATION & DEVELOPER EXPERIENCE

### 11.1 OpenAPI/Swagger Improvements (MEDIUM PRIORITY)

**Current State:** Good OpenAPI coverage, but missing operation IDs.

**Recommendations:**

```java

@PostMapping("service-providers/{providerId}/tenants")
@Operation(
        summary = "Register a new tenant",
        description = "Creates and registers a new tenant for a service provider",
        operationId = "registerTenant"
)
@ApiResponse(responseCode = "201", description = "Tenant created successfully")
public ResponseEntity<TenantResponse> registerTenant(
        @PathVariable(description = "Service provider ID") Long providerId,
        @Valid @RequestBody RegisterTenantRequest request
) {
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.registerTenant(providerId, request));
}
```

### 11.2 Add README for Architecture (LOW PRIORITY)

**Recommendations:**
Create `docs/ARCHITECTURE.md`:

- Component diagrams
- Data flow diagrams
- API design principles
- Extension points for developers

---

## 12. DEPENDENCY MANAGEMENT

### 12.1 Add Missing Spring Boot Starters (MEDIUM PRIORITY)

**Recommendations:**

```gradle
dependencies {
    // ...existing...
    
    // Better error handling and validation
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    
    // Structured logging and tracing
    implementation 'io.micrometer:micrometer-tracing-bridge-brave:1.3.0'
    
    // Caching support
    implementation 'org.springframework.boot:spring-boot-starter-cache'
    
    // Monitoring
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    
    // Mapping between DTOs and entities
    implementation 'org.mapstruct:mapstruct:1.6.0'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.6.0'
}
```

### 12.2 Separate Dependency Management Files (LOW PRIORITY)

**Recommendations:**
Create `gradle/dependencies.gradle` for centralized version management:

```gradle
ext {
    versions = [
        spring: '3.4.1',
        springdoc: '2.7.0',
        mapstruct: '1.6.0',
        testcontainers: '1.19.7'
    ]
    
    dependencies = [
        // Spring
        'spring-boot-starter-web': "org.springframework.boot:spring-boot-starter-web:${versions.spring}",
        // etc.
    ]
}
```

---

## IMPLEMENTATION ROADMAP

### Phase 1: Foundation (Weeks 1-2)

- [ ] Refactor TenantService into focused services
- [ ] Create API response wrapper structure
- [ ] Rename `dao/` package to `api/dto/`
- [ ] Add input validation with annotations

### Phase 2: Architecture (Weeks 3-4)

- [ ] Implement DDD structure with domain layer
- [ ] Create exception hierarchy
- [ ] Enhance GlobalExceptionHandler
- [ ] Add configuration properties classes

### Phase 3: Modernization (Weeks 5-6)

- [ ] Convert DTOs to Records
- [ ] Add MapStruct for entity mapping
- [ ] Implement Spring Cache abstraction
- [ ] Add @ConfigurationProperties

### Phase 4: Testing & Quality (Weeks 7-8)

- [ ] Reorganize test structure
- [ ] Add TestContainers
- [ ] Create test fixtures
- [ ] Add structured logging

### Phase 5: Polish (Weeks 9-10)

- [ ] Add Lombok to entities
- [ ] Improve OpenAPI documentation
- [ ] Add audit support
- [ ] Performance optimization

---

## QUICK WINS (Low Effort, High Value)

1. **Add HTTP Status 201 for POST endpoints** - Currently returns 200
2. **Uncomment security annotations** - `@PreAuthorize` comments found in code
3. **Fix response codes** - Some operations should return 201 (Created) not 200
4. **Add constructor validation** - Ensure non-null dependencies in services
5. **Document API response format** - Add examples to OpenAPI
6. **Add transaction boundaries** - Missing `@Transactional` on some write operations
7. **Consistent field naming** - Use `id` not `Id` as suffix

---

## TECHNICAL DEBT SUMMARY

| Issue                        | Severity | Category      | Effort |
|------------------------------|----------|---------------|--------|
| Oversized TenantService      | HIGH     | Architecture  | HIGH   |
| Mixed DAO/DTO naming         | MEDIUM   | Clarity       | LOW    |
| Inconsistent response format | HIGH     | API           | MEDIUM |
| No DDD structure             | MEDIUM   | Architecture  | HIGH   |
| Manual caching logic         | MEDIUM   | Performance   | LOW    |
| Sparse input validation      | MEDIUM   | Quality       | LOW    |
| Limited error handling       | MEDIUM   | Reliability   | MEDIUM |
| No structured logging        | MEDIUM   | Observability | MEDIUM |
| Mutable DTOs                 | LOW      | Design        | MEDIUM |

---

## CONCLUSION

The Redline codebase has a solid foundation with good use of Spring Boot, security, and API documentation. The main
areas for improvement are:

1. **Service layer decomposition** - Break down large services
2. **Clear architectural boundaries** - Implement DDD principles
3. **Modern Java features** - Use Records for DTOs
4. **Enhanced error handling** - Better validation and exception management
5. **Improved observability** - Structured logging and metrics

Implementing these recommendations will make the codebase more maintainable, testable, and aligned with current Spring
Boot best practices.

