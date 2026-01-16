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

package com.metaformsystems.redline.service;

import com.metaformsystems.redline.client.management.dto.Criterion;
import com.metaformsystems.redline.client.management.dto.NewContractDefinition;
import com.metaformsystems.redline.client.management.dto.NewPolicyDefinition;

import java.util.List;
import java.util.Set;

public interface Constants {
    String ASSET_PERMISSION = "membership_asset";
    String MEMBERSHIP_POLICY_ID = "membership_policy";

    // all files that are uploaded fall under this policy: the MembershipCredential must be presented to view the EDC asset
    NewPolicyDefinition MEMBERSHIP_POLICY = NewPolicyDefinition.Builder.aNewPolicyDefinition()
            .id(MEMBERSHIP_POLICY_ID)
            .policy(new NewPolicyDefinition.PolicySet(List.of(new NewPolicyDefinition.PolicySet.Permission("use",
                    List.of(new NewPolicyDefinition.PolicySet.Constraint("MembershipCredential", "eq", "active"))))))
            .build();

    // all new assets must have privateProperties: "permission" - "membership_asset", so that they are affected by this contract def
    NewContractDefinition MEMBERSHIP_CONTRACT_DEFINITION = NewContractDefinition.Builder.aNewContractDefinition()
            .accessPolicyId(MEMBERSHIP_POLICY_ID)
            .contractPolicyId(MEMBERSHIP_POLICY_ID)
            .assetsSelector(Set.of(new Criterion("privateProperties.'https://w3id.org/edc/v0.0.1/ns/permission'", "=", ASSET_PERMISSION)))
            .build();


}
