/*
 *  Copyright (c) 2026 Metaform Systems, Inc.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Metaform Systems, Inc. - initial API and implementation
 *
 */

package com.metaformsystems.redline.api.dto.request;

import java.util.List;

public record DataPlaneRegistrationRequest(String id, List<String> allowedTransferTypes, List<String> labels, String url) {

    public static DataPlaneRegistrationRequest ofDefault() {
        return new DataPlaneRegistrationRequest("dataplane-%s",
                List.of("HttpData-PULL"),
                List.of(),
                "http://siglet.edc-v.svc.cluster.local:8081/api/v1/%s/dataflows");
    }
}
