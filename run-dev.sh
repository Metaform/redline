#!/bin/bash

# Quick start script for development mode with H2 database
# This will run the application without requiring PostgreSQL or Keycloak

echo "Starting Redline in development mode..."
echo "Using H2 in-memory database"
echo "Server will start on http://localhost:8081"
echo ""
echo "Public endpoints (no auth required):"
echo "  - http://localhost:8081/api/public/redline"
echo "  - http://localhost:8081/api/public/health"
echo "  - http://localhost:8081/api/public/info"
echo "  - http://localhost:8081/h2-console"
echo ""
echo "Note: Protected endpoints require Keycloak authentication"
echo ""

./gradlew bootRun --args='--spring.profiles.active=dev'
