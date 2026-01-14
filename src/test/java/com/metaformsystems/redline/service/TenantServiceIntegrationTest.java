package com.metaformsystems.redline.service;

import com.metaformsystems.redline.client.tenantmanager.v1alpha1.TenantManagerClient;
import com.metaformsystems.redline.dao.NewParticipantDeployment;
import com.metaformsystems.redline.dao.NewTenantRegistration;
import com.metaformsystems.redline.dao.VPAResource;
import com.metaformsystems.redline.model.Dataspace;
import com.metaformsystems.redline.model.ServiceProvider;
import com.metaformsystems.redline.repository.DataspaceRepository;
import com.metaformsystems.redline.repository.ParticipantRepository;
import com.metaformsystems.redline.repository.ServiceProviderRepository;
import com.metaformsystems.redline.repository.TenantRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@ActiveProfiles("dev-pg")
@Transactional
class TenantServiceIntegrationTest {

    private static MockWebServer mockWebServer;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private DataspaceRepository dataspaceRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private TenantManagerClient tenantManagerClient;

    private ServiceProvider serviceProvider;
    private Dataspace dataspace;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        registry.add("tenant-manager.url", () -> mockWebServer.url("/").toString());
    }

    @AfterAll
    static void tearDown() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @BeforeEach
    void setUp() {
        // Create test data
        serviceProvider = new ServiceProvider();
        serviceProvider.setName("Test Provider");
        serviceProvider = serviceProviderRepository.save(serviceProvider);

        dataspace = new Dataspace();
        dataspace.setName("Test Dataspace");
        dataspace = dataspaceRepository.save(dataspace);
    }

    @Test
    void shouldRegisterTenant() {
        // Arrange
        var registration = new NewTenantRegistration("Test Tenant", List.of(dataspace.getId()));

        // Act
        var tenant = tenantService.registerTenant(serviceProvider.getId(), registration);

        // Assert
        assertThat(tenant).isNotNull();
        assertThat(tenant.getName()).isEqualTo("Test Tenant");
        assertThat(tenant.getServiceProvider()).isEqualTo(serviceProvider);
        assertThat(tenant.getParticipants()).hasSize(1);

        var participant = tenant.getParticipants().iterator().next();
        assertThat(participant.getIdentifier()).isEqualTo("Test Tenant");
        assertThat(participant.getDataspaces()).hasSize(1);
        assertThat(participant.getDataspaces().iterator().next()).isEqualTo(dataspace);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void shouldDeployParticipant() {
        // Arrange
        var registration = new NewTenantRegistration("Test Tenant", List.of(dataspace.getId()));
        var tenant = tenantService.registerTenant(serviceProvider.getId(), registration);
        var participant = tenant.getParticipants().iterator().next();

        // Mock tenant creation response
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {
                           "id": "1e546b5a-0f7a-466b-bf54-aca8df8c3117",
                           "version": 0,
                           "properties": {
                             "location": "eu",
                             "name": "Consumer Tenant"
                           }
                         }
                        """)
                .addHeader("Content-Type", "application/json"));

        // Mock participant profile creation response
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {
                            "id": "dfe5b1c2-112c-4556-947a-ddbe92f9358a",
                            "version": 0,
                            "identifier": "did:web:example.com:participant",
                            "tenantId": "1e546b5a-0f7a-466b-bf54-aca8df8c3117",
                            "services": {},
                            "vpas": [
                                {
                                      "id": "ac77f6e0-e631-4fe8-904d-8dff5425a978",
                                      "version": 0,
                                      "state": "pending",
                                      "type": "cfm.connector",
                                      "cellId": "621bbfd1-7e97-4934-93f1-86d19954c9b1"
                                    },
                                    {
                                      "id": "6e36d1ca-ca41-4a71-a70c-bf0956e1a875",
                                      "version": 0,
                                      "state": "pending",
                                      "type": "cfm.credentialservice",
                                      "cellId": "621bbfd1-7e97-4934-93f1-86d19954c9b1"
                                    },
                                    {
                                      "id": "1ebcd321-cb01-47ee-add6-95199acea06e",
                                      "version": 0,
                                      "state": "pending",
                                      "type": "cfm.dataplane",
                                      "cellId": "621bbfd1-7e97-4934-93f1-86d19954c9b1"
                                    }
                            ]
                        }
                        """)
                .addHeader("Content-Type", "application/json"));

        var deployment = new NewParticipantDeployment(participant.getId(), "did:web:example.com:participant");

        // Act
        var result = tenantService.deployParticipant(deployment);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.identifier()).isEqualTo("did:web:example.com:participant");
        assertThat(result.agents()).hasSize(3);
        assertThat(result.agents()).anyMatch(agent -> agent.type() == VPAResource.Type.CONTROL_PLANE);
        assertThat(result.agents()).anyMatch(agent -> agent.type() == VPAResource.Type.CREDENTIAL_SERVICE);
        assertThat(result.agents()).anyMatch(agent -> agent.type() == VPAResource.Type.DATA_PLANE);

        // Verify database state
        var updatedTenant = tenantRepository.findById(tenant.getId()).orElseThrow();
        assertThatCode(() -> UUID.fromString(updatedTenant.getCorrelationId())).doesNotThrowAnyException();

        var updatedParticipant = participantRepository.findById(participant.getId()).orElseThrow();
        assertThatCode(() -> UUID.fromString(updatedParticipant.getCorrelationId())).doesNotThrowAnyException();
        assertThat(updatedParticipant.getIdentifier()).isEqualTo("did:web:example.com:participant");
        assertThat(updatedParticipant.getAgents()).hasSize(3);
    }

    @Test
    void shouldGetParticipant() {
        // Arrange
        var registration = new NewTenantRegistration("Test Tenant", List.of(dataspace.getId()));
        var tenant = tenantService.registerTenant(serviceProvider.getId(), registration);
        var participant = tenant.getParticipants().iterator().next();

        // Act
        var result = tenantService.getParticipant(participant.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.identifier()).isEqualTo("Test Tenant");
        assertThat(result.dataspaces()).containsExactly(dataspace.getId());
    }

    @Test
    void shouldGetTenant() {
        // Arrange
        var registration = new NewTenantRegistration("Test Tenant", List.of(dataspace.getId()));
        var tenant = tenantService.registerTenant(serviceProvider.getId(), registration);

        // Act
        var result = tenantService.getTenant(tenant.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Test Tenant");
        assertThat(result.participants()).hasSize(1);
        assertThat(result.participants().getFirst().identifier()).isEqualTo("Test Tenant");
    }

    @Test
    void shouldDeployParticipantWithExistingTenantCorrelationId() {
        // Arrange
        var registration = new NewTenantRegistration("Test Tenant", List.of(dataspace.getId()));
        var tenant = tenantService.registerTenant(serviceProvider.getId(), registration);
        tenant.setCorrelationId("existing-tenant-id");
        tenantRepository.save(tenant);

        var participant = tenant.getParticipants().iterator().next();

        // Mock only participant profile creation (tenant already exists)
        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {
                           "id": "dfe5b1c2-112c-4556-947a-ddbe92f9358a",
                           "version": 0,
                           "identifier": "did:web:example.com:participant2",
                           "tenantId": "1e546b5a-0f7a-466b-bf54-aca8df8c3117",
                           "participantRoles": {},
                           "vpas": [
                             {
                               "id": "ac77f6e0-e631-4fe8-904d-8dff5425a978",
                               "version": 0,
                               "state": "pending",
                               "type": "cfm.connector",
                               "cellId": "621bbfd1-7e97-4934-93f1-86d19954c9b1"
                             },
                             {
                               "id": "6e36d1ca-ca41-4a71-a70c-bf0956e1a875",
                               "version": 0,
                               "state": "pending",
                               "type": "cfm.credentialservice",
                               "cellId": "621bbfd1-7e97-4934-93f1-86d19954c9b1"
                             },
                             {
                               "id": "1ebcd321-cb01-47ee-add6-95199acea06e",
                               "version": 0,
                               "state": "pending",
                               "type": "cfm.dataplane",
                               "cellId": "621bbfd1-7e97-4934-93f1-86d19954c9b1"
                             }
                           ],
                           "error": false
                         }
                        """)
                .addHeader("Content-Type", "application/json"));

        var deployment = new NewParticipantDeployment(participant.getId(), "did:web:example.com:participant2");

        // Act
        var result = tenantService.deployParticipant(deployment);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.identifier()).isEqualTo("did:web:example.com:participant2");

//         Verify only one request was made (no tenant creation)
        assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
    }
}
