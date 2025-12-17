# Token Authentication Guide

This guide shows how to obtain tokens needed to call the `/home-office-statutory-timeframe-status` endpoint.

## Required Tokens

The endpoint requires **two tokens**:
1. **S2S Token** - Service-to-Service authentication (in `ServiceAuthorization` header, no Bearer prefix)
2. **IDAM Token** - User authentication (in `Authorization` header, with Bearer prefix)

## 1. Get S2S Token

### Easy Method (Use the endpoint)
```bash
S2S_TOKEN=$(curl -s https://ia-case-api-pr-2908-home-office-integration-api.preview.platform.hmcts.net/s2stoken)
```

### Direct Method (From S2S service)
```bash
# Complex - requires signing JWT with IA_S2S_SECRET
curl -X POST \
  "https://rpe-service-auth-provider-preview.platform.hmcts.net/lease" \
  -H "Content-Type: application/json" \
  -d '{"microservice": "ia-home-office-integration-api"}'
```

## 2. Get IDAM Token

```bash
curl -X POST \
  "https://idam-api.preview.platform.hmcts.net/o/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "redirect_uri=http://localhost:3002/oauth2/callback" \
  -d "client_id=${IA_IDAM_CLIENT_ID}" \
  -d "client_secret=${IA_IDAM_CLIENT_SECRET}" \
  -d "username=${IA_SYSTEM_USERNAME}" \
  -d "password=${IA_SYSTEM_PASSWORD}" \
  -d "scope=openid profile authorities acr roles create-user manage-user search-user"
```

**Response:**
```json
{
  "access_token": "eyJhbGc...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

Extract the `access_token` field - that's your IDAM token.

## 3. Call the Endpoint

```bash
# Get tokens
S2S_TOKEN=$(curl -s https://ia-case-api-pr-2908-home-office-integration-api.preview.platform.hmcts.net/s2stoken)
IDAM_TOKEN="<access_token from IDAM response>"

# Call endpoint
curl -X POST \
  https://ia-case-api-pr-2908-home-office-integration-api.preview.platform.hmcts.net/home-office-statutory-timeframe-status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${IDAM_TOKEN}" \
  -H "ServiceAuthorization: ${S2S_TOKEN}" \
  -d '{
    "ccdCaseId": 1765790176250362,
    "uan": "1234-5678-9012-3556",
    "familyName": "Smith",
    "givenNames": "John",
    "dateOfBirth": "1990-01-15",
    "stf24weeks": {
      "status": "Yes",
      "caseType": "EEA"
    },
    "timeStamp": "2025-12-02T10:30:00"
  }' \
  -w "\nStatus: %{http_code}\n"
```

## Required Environment Variables

Get these from Azure Key Vault or Kubernetes secrets:

```bash
# For IDAM token
IA_IDAM_CLIENT_ID=<from secrets>
IA_IDAM_CLIENT_SECRET=<from secrets>
IA_SYSTEM_USERNAME=<from secrets>
IA_SYSTEM_PASSWORD=<from secrets>

# For S2S token (direct method only)
IA_S2S_SECRET=<from secrets>
```

### Get secrets from Kubernetes

```bash
# Connect to preview
az aks get-credentials --resource-group cft-preview-01-rg --name cft-preview-01-aks --subscription DCD-CFTAPPS-DEV

# Get credentials
kubectl -n ia get secret ia-home-office-integration-api-pr-XXXX -o jsonpath='{.data.IA_SYSTEM_USERNAME}' | base64 -d
kubectl -n ia get secret ia-home-office-integration-api-pr-XXXX -o jsonpath='{.data.IA_SYSTEM_PASSWORD}' | base64 -d
```

## Troubleshooting

If you get 401 Unauthorized, check the logs (added in SecurityConfiguration.java):
```
Authentication failed for request to /home-office-statutory-timeframe-status
Authorization header: null  ← Missing IDAM token
ServiceAuthorization header: eyJhbGc...  ← S2S token present
```

Both headers must be present for the request to reach the controller.
