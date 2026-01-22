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

package com.metaformsystems.redline.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
@Tag(name = "Server Info", description = "Public endpoints for health checks and system information")
public class InfoController {

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns the health status of the application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application is healthy")
    })
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "application", "Redline",
                "version", "0.0.1-SNAPSHOT"
        ));
    }

    @GetMapping("/info")
    @Operation(summary = "System information", description = "Returns basic information about the Redline API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved system information")
    })
    public ResponseEntity<Map<String, String>> info() {
        return ResponseEntity.ok(Map.of(
                "name", "Redline API",
                "description", "UI API Server"
        ));
    }
}
