package com.metaformsystems.redline.infrastructure.client.hashicorpvault;

public interface HashicorpVaultClient {
    String readSecret(String path);
}
