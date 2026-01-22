# Refactoring Implementation Guide

This document provides concrete code examples for implementing the key recommendations from the refactoring strategy.

---

## 1. Service Layer Refactoring: Splitting TenantService

### Current Structure Problem

The `TenantService` (485 lines) handles too many concerns:

- Tenant lifecycle
- Participant deployment
- Data transfers
- Catalog management
- Contract negotiations

### Proposed Structure

#### ParticipantService.java

```java
package com.metaformsystems.redline.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing participants and their lifecycle.
 * Handles deployment, state management, and participant-specific operations.
 */
@Service
@Transactional
public class ParticipantService {

    private static final Logger log = LoggerFactory.getLogger(ParticipantService.class);

    private final ParticipantRepository participantRepository;
    private final TenantManagerClient tenantManagerClient;
    private final ManagementApiClient managementApiClient;

    public ParticipantService(ParticipantRepository participantRepository,
                              TenantManagerClient tenantManagerClient,
                              ManagementApiClient managementApiClient) {
        this.participantRepository = participantRepository;
        this.tenantManagerClient = tenantManagerClient;
        this.managementApiClient = managementApiClient;
    }

    /**
     * Deploy a new participant in the dataspace
     */
    public ParticipantResponse deployParticipant(Long tenantId, DeployParticipantRequest request) {
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        // Create participant profile in tenant manager
        var participantProfile = createParticipantProfile(request);
        var deployment = tenantManagerClient.deployParticipant(participantProfile);

        // Create local participant entity
        var participant = new Participant();
        participant.setTenant(tenant);
        participant.setName(request.name());
        participant.setControlPlaneUrl(deployment.controlPlaneUrl());
        participant.setDataPlaneUrl(deployment.dataPlaneUrl());
        participant.setState(DeploymentState.DEPLOYED);

        var saved = participantRepository.save(participant);
        log.info("Deployed participant {} for tenant {}", saved.getId(), tenantId);

        return ParticipantMapper.toResponse(saved);
    }

    /**
     * Get participant details
     */
    public ParticipantResponse getParticipant(Long participantId) {
        var participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        return ParticipantMapper.toResponse(participant);
    }

    private V1Alpha1ParticipantProfile createParticipantProfile(DeployParticipantRequest request) {
        return V1Alpha1ParticipantProfile.builder()
                .name(request.name())
                .description(request.description())
                .build();
    }
}
```

#### CatalogService.java

```java
package com.metaformsystems.redline.application.service;

import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

/**
 * Service for managing catalog requests and caching.
 * Handles catalog discovery from counterparties.
 */
@Service
public class CatalogService {

    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);

    private final ManagementApiClient managementApiClient;
    private final WebDidResolver didResolver;

    public CatalogService(ManagementApiClient managementApiClient,
                          WebDidResolver didResolver) {
        this.managementApiClient = managementApiClient;
        this.didResolver = didResolver;
    }

    /**
     * Request catalog from counterparty with optional caching
     */
    @Cacheable(
            value = "catalogs",
            key = "#participantId + '_' + #counterPartyIdentifier",
            unless = "#cacheControl.contains('no-cache')"
    )
    public Catalog requestCatalog(String participantId, String counterPartyIdentifier, String cacheControl) {
        log.debug("Requesting catalog from {} for participant {}", counterPartyIdentifier, participantId);

        // Resolve counterparty endpoint from DID
        var counterPartyEndpoint = didResolver.resolveProtocolEndpoints(counterPartyIdentifier)
                .stream()
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException("Cannot resolve counterparty"));

        // Request catalog
        return managementApiClient.requestCatalog(participantId, counterPartyEndpoint);
    }

    /**
     * Invalidate catalog cache for a participant
     */
    @CacheEvict(value = "catalogs", key = "#participantId + '_' + #counterPartyId")
    public void invalidateCatalog(String participantId, String counterPartyId) {
        log.info("Invalidated catalog cache for {} -> {}", participantId, counterPartyId);
    }
}
```

#### DataTransferService.java

```java
package com.metaformsystems.redline.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for managing data transfers (file uploads/downloads).
 */
@Service
@Transactional
public class DataTransferService {

    private static final Logger log = LoggerFactory.getLogger(DataTransferService.class);

    private final DataPlaneApiClient dataPlaneApiClient;
    private final ManagementApiClient managementApiClient;
    private final ParticipantRepository participantRepository;

    public DataTransferService(DataPlaneApiClient dataPlaneApiClient,
                               ManagementApiClient managementApiClient,
                               ParticipantRepository participantRepository) {
        this.dataPlaneApiClient = dataPlaneApiClient;
        this.managementApiClient = managementApiClient;
        this.participantRepository = participantRepository;
    }

    /**
     * Upload a file for a participant
     */
    public UploadedFileResponse uploadFile(Long participantId, MultipartFile file, Map<String, Object> metadata) {
        var participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        if (file.isEmpty()) {
            throw new InvalidRequestException("File cannot be empty");
        }

        try {
            // Create CEL expression for access control
            var celExpression = createAccessControlExpression(metadata);
            managementApiClient.createCelExpression(participant.getId().toString(), celExpression);

            // Create asset
            var asset = createAsset(file.getOriginalFilename(), metadata);
            managementApiClient.createAsset(participant.getId().toString(), asset);

            // Create policy
            var policy = createPolicy(metadata);
            managementApiClient.createPolicy(participant.getId().toString(), policy);

            // Create contract definition
            var contractDef = createContractDefinition(asset);
            managementApiClient.createContractDefinition(participant.getId().toString(), contractDef);

            // Upload to data plane
            var uploadResponse = dataPlaneApiClient.uploadFile(participant.getDataPlaneUrl(), file);

            log.info("Uploaded file {} for participant {}", file.getOriginalFilename(), participantId);

            return new UploadedFileResponse(
                    uploadResponse.getId(),
                    file.getOriginalFilename(),
                    file.getSize(),
                    System.currentTimeMillis()
            );
        } catch (Exception e) {
            log.error("Failed to upload file for participant {}", participantId, e);
            throw new ExternalServiceException("File upload failed: " + e.getMessage());
        }
    }

    private NewCelExpression createAccessControlExpression(Map<String, Object> metadata) {
        // Implementation
        return null;
    }

    private NewAsset createAsset(String filename, Map<String, Object> metadata) {
        // Implementation
        return null;
    }

    private NewPolicyDefinition createPolicy(Map<String, Object> metadata) {
        // Implementation
        return null;
    }

    private NewContractDefinition createContractDefinition(NewAsset asset) {
        // Implementation
        return null;
    }
}
```

#### ContractNegotiationService.java

```java
package com.metaformsystems.redline.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing contract negotiations.
 */
@Service
@Transactional
public class ContractNegotiationService {

    private static final Logger log = LoggerFactory.getLogger(ContractNegotiationService.class);

    private final ManagementApiClient managementApiClient;
    private final WebDidResolver didResolver;
    private final ParticipantRepository participantRepository;

    public ContractNegotiationService(ManagementApiClient managementApiClient,
                                      WebDidResolver didResolver,
                                      ParticipantRepository participantRepository) {
        this.managementApiClient = managementApiClient;
        this.didResolver = didResolver;
        this.participantRepository = participantRepository;
    }

    /**
     * Initiate a contract negotiation
     */
    public String initiateContractNegotiation(Long participantId, ContractNegotiationRequest request) {
        var participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        // Resolve counterparty address from DID
        var counterPartyAddress = didResolver.resolveProtocolEndpoints(request.counterPartyIdentifier())
                .stream()
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException("Cannot resolve counterparty"));

        // Create contract request
        var contractRequest = new ContractRequest.Builder()
                .counterPartyAddress(counterPartyAddress)
                .counterPartyId(request.counterPartyId())
                .assetId(request.assetId())
                .offerId(request.offerId())
                .protocol("dataspace-protocol-http")
                .providerId(request.providerId())
                .build();

        var negotiationId = managementApiClient.initiateContractNegotiation(
                participant.getId().toString(),
                contractRequest
        );

        log.info("Initiated contract negotiation {} for participant {}", negotiationId, participantId);
        return negotiationId;
    }

    /**
     * Get contract negotiation details
     */
    public ContractNegotiationResponse getContractNegotiation(Long participantId, String negotiationId) {
        var participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        var negotiation = managementApiClient.getContractNegotiation(
                participant.getId().toString(),
                negotiationId
        );

        return ContractNegotiationMapper.toResponse(negotiation);
    }

    /**
     * List contracts for a participant
     */
    public List<ContractNegotiationResponse> listContracts(Long participantId) {
        var participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        var contracts = managementApiClient.queryContractNegotiations(
                participant.getId().toString()
        );

        return contracts.stream()
                .map(ContractNegotiationMapper::toResponse)
                .toList();
    }

    /**
     * Get transfer processes
     */
    public List<TransferProcessResponse> listTransferProcesses(Long participantId) {
        var participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

        var transfers = managementApiClient.queryTransferProcesses(
                participant.getId().toString()
        );

        return transfers.stream()
                .map(TransferProcessMapper::toResponse)
                .toList();
    }
}
```

#### Updated TenantManagementService.java

```java
package com.metaformsystems.redline.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for tenant management (CRUD operations only).
 * Related business logic delegated to specialized services.
 */
@Service
@Transactional
public class TenantManagementService {

    private static final Logger log = LoggerFactory.getLogger(TenantManagementService.class);

    private final TenantRepository tenantRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final TenantManagerClient tenantManagerClient;

    public TenantManagementService(TenantRepository tenantRepository,
                                   ServiceProviderRepository serviceProviderRepository,
                                   TenantManagerClient tenantManagerClient) {
        this.tenantRepository = tenantRepository;
        this.serviceProviderRepository = serviceProviderRepository;
        this.tenantManagerClient = tenantManagerClient;
    }

    /**
     * Register a new tenant
     */
    public TenantResponse registerTenant(Long serviceProviderId, RegisterTenantRequest request) {
        var serviceProvider = serviceProviderRepository.findById(serviceProviderId)
                .orElseThrow(() -> new ResourceNotFoundException("Service provider not found"));

        // Create tenant in tenant manager
        var tenantRequest = new V1Alpha1NewTenant(
                request.name(),
                null // other fields
        );
        var result = tenantManagerClient.registerTenant(tenantRequest);

        // Create local tenant entity
        var tenant = new Tenant();
        tenant.setName(request.name());
        tenant.setServiceProvider(serviceProvider);
        tenant.setCorrelationId(result.correlationId());
        tenant.setProperties(request.properties());

        var saved = tenantRepository.save(tenant);
        log.info("Registered tenant {} for service provider {}", saved.getId(), serviceProviderId);

        return TenantMapper.toResponse(saved);
    }

    /**
     * Get tenant details
     */
    public TenantResponse getTenant(Long tenantId) {
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        return TenantMapper.toResponse(tenant);
    }

    /**
     * List all tenants for a service provider
     */
    public List<TenantResponse> getTenantsByServiceProvider(Long serviceProviderId) {
        return tenantRepository.findByServiceProviderId(serviceProviderId)
                .stream()
                .map(TenantMapper::toResponse)
                .toList();
    }

    /**
     * Update tenant properties
     */
    public TenantResponse updateTenant(Long tenantId, UpdateTenantRequest request) {
        var tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        if (request.properties() != null) {
            tenant.setProperties(request.properties());
        }

        var updated = tenantRepository.save(tenant);
        log.info("Updated tenant {}", tenantId);

        return TenantMapper.toResponse(updated);
    }
}
```

---

## 2. API Response Wrapper Pattern

### Create Consistent Response Structure

#### ApiResponse.java

```java
package com.metaformsystems.redline.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Standard API response wrapper for all endpoints.
 * @param <T> Type of response data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        long timestamp
) {
    /**
     * Create a success response with data
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, System.currentTimeMillis());
    }

    /**
     * Create a success response with data and message
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, System.currentTimeMillis());
    }

    /**
     * Create an error response
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, System.currentTimeMillis());
    }
}
```

#### ErrorResponse.java

```java
package com.metaformsystems.redline.api.dto.response;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String message,
        Map<String, String> errors,
        long timestamp
) {
    public ErrorResponse(String message) {
        this(message, null, System.currentTimeMillis());
    }

    public ErrorResponse(String message, Map<String, String> errors) {
        this(message, errors, System.currentTimeMillis());
    }
}
```

#### Updated GlobalExceptionHandler.java

```java
package com.metaformsystems.redline.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        var fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage(),
                        (existing, replacement) -> existing + "; " + replacement
                ));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Validation failed", fieldErrors));
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalServiceError(ExternalServiceException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal server error: " + ex.getMessage()));
    }
}
```

#### Updated TenantController.java

```java
package com.metaformsystems.redline.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API controller for tenant operations.
 */
@RestController
@RequestMapping("/api/ui/service-providers/{serviceProviderId}/tenants")
@Tag(name = "Tenant Management", description = "Tenant registration and management")
public class TenantController {

    private final TenantManagementService tenantManagementService;

    public TenantController(TenantManagementService tenantManagementService) {
        this.tenantManagementService = tenantManagementService;
    }

    @PostMapping
    @Operation(
            summary = "Register a new tenant",
            operationId = "registerTenant"
    )
    @ApiResponse(responseCode = "201", description = "Tenant created successfully")
    @ApiResponse(responseCode = "404", description = "Service provider not found")
    public ResponseEntity<ApiResponse<TenantResponse>> registerTenant(
            @PathVariable Long serviceProviderId,
            @Valid @RequestBody RegisterTenantRequest request
    ) {
        var response = tenantManagementService.registerTenant(serviceProviderId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tenant registered successfully"));
    }

    @GetMapping("{tenantId}")
    @Operation(
            summary = "Get tenant details",
            operationId = "getTenant"
    )
    @ApiResponse(responseCode = "200", description = "Tenant retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Tenant not found")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenant(
            @PathVariable Long serviceProviderId,
            @PathVariable Long tenantId
    ) {
        var response = tenantManagementService.getTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

---

## 3. DTO Conversion with Records and MapStruct

### Request/Response DTOs Using Records

#### TenantDTOs.java

```java
package com.metaformsystems.redline.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

// Request
public record RegisterTenantRequest(
        @NotBlank(message = "Tenant name is required")
        String name,

        Map<String, Object> properties
) {
}

public record UpdateTenantRequest(
        String name,
        Map<String, Object> properties
) {
}

// Response
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TenantResponse(
        Long id,
        String name,
        String correlationId,
        Map<String, Object> properties,
        Long serviceProviderId,
        long createdAt,
        long updatedAt
) {
}
```

#### ParticipantDTOs.java

```java
package com.metaformsystems.redline.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

// Request
public record DeployParticipantRequest(
        @NotBlank(message = "Participant name is required")
        String name,

        String description,
        Map<String, Object> properties
) {
}

// Response
public record ParticipantResponse(
        Long id,
        String name,
        String description,
        String controlPlaneUrl,
        String dataPlaneUrl,
        String state,
        Map<String, Object> properties,
        long createdAt
) {
}
```

### MapStruct Mappers

#### TenantMapper.java

```java
package com.metaformsystems.redline.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TenantMapper {

    TenantResponse toResponse(Tenant entity);

    TenantResponse toResponse(Tenant entity, ServiceProvider provider);

    Tenant toEntity(RegisterTenantRequest request);
}
```

#### ParticipantMapper.java

```java
package com.metaformsystems.redline.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ParticipantMapper {

    ParticipantResponse toResponse(Participant entity);

    Participant toEntity(DeployParticipantRequest request);
}
```

---

## 4. Exception Hierarchy

#### ApplicationException.java

```java
package com.metaformsystems.redline.application.exception;

import org.springframework.http.HttpStatus;

/**
 * Base class for application exceptions.
 */
public abstract class ApplicationException extends RuntimeException {

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Get HTTP status for this exception
     */
    public abstract HttpStatus getHttpStatus();
}
```

#### ResourceNotFoundException.java

```java
package com.metaformsystems.redline.application.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApplicationException {

    public ResourceNotFoundException(String resourceType, Long id) {
        super(String.format("%s with id %d not found", resourceType, id));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
```

#### InvalidRequestException.java

```java
package com.metaformsystems.redline.application.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends ApplicationException {

    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
```

#### ExternalServiceException.java

```java
package com.metaformsystems.redline.application.exception;

import org.springframework.http.HttpStatus;

public class ExternalServiceException extends ApplicationException {

    public ExternalServiceException(String serviceName, String message) {
        super(String.format("External service error [%s]: %s", serviceName, message));
    }

    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.SERVICE_UNAVAILABLE;
    }
}
```

---

## 5. Configuration Properties

#### AppConfiguration.java

```java
package com.metaformsystems.redline.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * Centralized application configuration properties.
 */
@Component
@ConfigurationProperties(prefix = "app")
public class AppConfiguration {

    private Cors cors = new Cors();

    @NestedConfigurationProperty
    private Security security = new Security();

    @NestedConfigurationProperty
    private ExternalServices externalServices = new ExternalServices();

    // Getters
    public Cors getCors() {
        return cors;
    }

    public Security getSecurity() {
        return security;
    }

    public ExternalServices getExternalServices() {
        return externalServices;
    }

    // Nested classes
    public static class Cors {
        private String allowedOrigins;

        public String getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(String allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class Security {
        private String jwkSetUri;
        private String issuerUri;

        public String getJwkSetUri() {
            return jwkSetUri;
        }

        public void setJwkSetUri(String jwkSetUri) {
            this.jwkSetUri = jwkSetUri;
        }

        public String getIssuerUri() {
            return issuerUri;
        }

        public void setIssuerUri(String issuerUri) {
            this.issuerUri = issuerUri;
        }
    }

    public static class ExternalServices {
        private ServiceConfig tenantManager = new ServiceConfig();
        private ServiceConfig controlPlane = new ServiceConfig();
        private ServiceConfig dataPlane = new ServiceConfig();

        public ServiceConfig getTenantManager() {
            return tenantManager;
        }

        public ServiceConfig getControlPlane() {
            return controlPlane;
        }

        public ServiceConfig getDataPlane() {
            return dataPlane;
        }
    }

    public static class ServiceConfig {
        private String url;
        private int timeout = 30;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
    }
}
```

#### Updated application.yml

```yaml
spring:
  application:
    name: redline

  # ... other config ...

app:
  cors:
    allowed-origins: http://localhost:4200

  security:
    jwk-set-uri: ${KEYCLOAK_JWK_SET_URI:http://localhost:8080/realms/redline/protocol/openid-connect/certs}
    issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:8080/realms/redline}

  external-services:
    tenant-manager:
      url: ${TENANT_MANAGER_URL:http://localhost:8082}
      timeout: 30
    control-plane:
      url: ${CONTROL_PLANE_URL:http://localhost:8083}
      timeout: 60
    data-plane:
      url: ${DATA_PLANE_URL:http://localhost:8084}
      timeout: 60
```

---

## Summary of Changes

These examples demonstrate:

1. **Separation of Concerns** - Large service split into focused, single-responsibility services
2. **Consistent API Responses** - Uniform response structure across all endpoints
3. **Modern Java Features** - Records for immutable DTOs, proper use of Java 21 capabilities
4. **Exception Handling** - Custom exception hierarchy with appropriate HTTP status codes
5. **Configuration Management** - Type-safe, centralized configuration properties
6. **Mapping** - MapStruct for clean entity-to-DTO conversion

This provides a solid foundation for further refactoring and modernization of the codebase.
