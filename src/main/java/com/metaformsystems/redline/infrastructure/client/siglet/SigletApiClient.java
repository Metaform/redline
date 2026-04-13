package com.metaformsystems.redline.infrastructure.client.siglet;

import java.util.Map;

public interface SigletApiClient {

    Map<String, Object> getDataAddress(String participantContextId, String transferProcessId);

}
