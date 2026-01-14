package com.metaformsystems.redline.service;

import com.metaformsystems.redline.client.TokenProvider;
import com.metaformsystems.redline.dao.DataplaneRegistration;
import com.metaformsystems.redline.dao.NewAsset;
import com.metaformsystems.redline.dao.NewCelExpression;
import com.metaformsystems.redline.dao.NewContractDefinition;
import com.metaformsystems.redline.dao.NewPolicyDefinition;
import com.metaformsystems.redline.model.ClientCredentials;
import com.metaformsystems.redline.repository.ParticipantRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ControlPlaneService {
    private final WebClient controlPlaneWebClient;
    private final TokenProvider tokenProvider;
    private final ParticipantRepository participantRepository;
    private final ClientCredentials adminCredentials;

    public ControlPlaneService(WebClient controlPlaneWebClient,
                               TokenProvider tokenProvider,
                               ParticipantRepository participantRepository,
                               @Value("${controlplane.admin.client-id:admin}") String adminClientId,
                               @Value("${controlplane.admin.client-secret:edc-v-admin-secret}") String adminClientSecret) {
        this.controlPlaneWebClient = controlPlaneWebClient;
        this.tokenProvider = tokenProvider;
        this.participantRepository = participantRepository;
        this.adminCredentials = new ClientCredentials(adminClientId, adminClientSecret);
    }

    public void createCelExpression(NewCelExpression celExpression) {

        var token = tokenProvider.getToken(adminCredentials.clientId(), adminCredentials.clientSecret(), "management-api:write management-api:read");
        controlPlaneWebClient.post()
                .uri("/celexpressions")
                .header("Authorization", "Bearer %s".formatted(token))
                .bodyValue(celExpression)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public void createAsset(String participantContextId, NewAsset asset) {
        var token = getToken(participantContextId);

        controlPlaneWebClient.post()
                .uri("/participants/%s/assets".formatted(participantContextId))
                .header("Authorization", "Bearer %s".formatted(token))
                .bodyValue(asset)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public void createContractDefinition(String participantContextId, NewContractDefinition contractDefinition) {
        controlPlaneWebClient.post()
                .uri("/participants/%s/contractdefinitions".formatted(participantContextId))
                .header("Authorization", "Bearer %s".formatted(getToken(participantContextId)))
                .bodyValue(contractDefinition)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public void createPolicy(String participantContextId, NewPolicyDefinition policy) {
        controlPlaneWebClient.post()
                .uri("/participants/%s/policydefinitions".formatted(participantContextId))
                .header("Authorization", "Bearer %s".formatted(getToken(participantContextId)))
                .bodyValue(policy)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public void prepareDataplane(String participantContextId, DataplaneRegistration dataplaneRegistration) {
        controlPlaneWebClient.post()
                .uri("/dataplanes/%s".formatted(participantContextId))
                .header("Authorization", "Bearer %s".formatted(getToken(participantContextId)))
                .bodyValue(dataplaneRegistration)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    private String getToken(String participantContextId) {
        var participantProfile = participantRepository.findByParticipantContextId(participantContextId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found with context id: " + participantContextId));

        var token = tokenProvider.getToken(participantProfile.getClientCredentials().clientId(), participantProfile.getClientCredentials().clientSecret(), "management-api:write management-api:read");
        return token;
    }
}
