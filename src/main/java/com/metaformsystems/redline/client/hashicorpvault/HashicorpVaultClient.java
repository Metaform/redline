package com.metaformsystems.redline.client.hashicorpvault;

public interface HashicorpVaultClient {
    String readSecret(String path);
}
