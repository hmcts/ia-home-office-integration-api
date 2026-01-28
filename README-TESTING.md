# Manual Testing Guide for 24 Weeks Home Office Controller

This guide explains how to manually test the `SetHomeOfficeStatutoryTimeframeStatusController` and its associated service using an IA Case API PR.

## Setup ia-home-office-integration-api PR for testing

1. Make the endpoint `home-office-statutory-timeframe-status` available by adding it to the anonymous paths in `src/main/resources/application.yaml`:

```yaml
security:
  anonymousPaths:
    - "/home-office-statutory-timeframe-status"
```

2. Push this PR to the pipeline. It will create a new image of the application.
   - Example: PR 435 (https://github.com/hmcts/ia-home-office-integration-api/pull/435)

## Build the IA Case API PR using the Home Office image

1. Create a new IA Case API PR.

   - Example: PR 2856 (https://github.com/hmcts/ia-case-api/pull/2856)

2. In `/charts/ia-case-api/values.preview.template.yaml`, replace the latest Home Office image with the PR image created in the previous step:

```yaml
ia-home-office-integration-api:
  enabled: true
  java:
    imagePullPolicy: Always
    image: hmctspublic.azurecr.io/ia/home-office-integration-api:pr-435
```

3. Ensure in `src/main/resources/application.yaml` you have:

```yaml
caseworker-ia-system:
  - "addStatutoryTimeframe24Weeks"
```

4. Push your changes and build the PR.

## Test the integration

1. Update the CCD case definition as needed for your IA Case API PR.

2. Use Case Management to create and submit a test case.

3. Connect to the database and verify the case data does not have any `statutoryTimeframe24Weeks` fields populated initially.

   Example query:

   ```sql
   SELECT
       reference,
       jsonb_pretty(data->'statutoryTimeframe24Weeks') AS statutory_timeframe_24_weeks_formatted
   FROM case_data
   WHERE reference = 1764079412780130;
   ```

4. Check in the Case Management interface that `addNote` and `Statutory TimeFrame 24 weeks` are empty for this case.

5. Check the logs in Kubernetes:

   ```bash
   kubectl get pods -n ia | grep 2856
   kubectl logs -n ia -f ia-case-api-pr-2856-java-6dbc49997c-9h8vw
   kubectl logs -n ia -f ia-case-api-pr-2856-home-office-integration-api-568d84dfb7m7fb
   ```

6. Use curl to add the statutory timeframe 24 weeks data to the case:

   ```bash
   curl -X POST \
     https://ia-case-api-pr-2856-home-office-integration-api.preview.platform.hmcts.net/home-office-statutory-timeframe-status \
     -H "Content-Type: application/json" \
     -H "ServiceAuthorization: Bearer YOUR_S2S_TOKEN_HERE" \
     -d '{
       "ccdCaseId": 1764079412780130,
       "uan": "1234-5678-9012-3455",
       "familyName": "Smith",
       "givenNames": "John",
       "dateOfBirth": "1990-01-15",
       "stf24weeks": {
         "status": "Yes",
         "caseType": "EEA"
       },
       "timeStamp": "2025-11-24T10:30:00"
     }' \
     -w "\nStatus: %{http_code}\n"
   ```

## Update the API Case PR with a new Home Office API image

1. Push changes in the current Home Office PR.
2. After the new image is published, delete the Home Office pod to force a restart with the new image:
   ```bash
   kubectl delete pod -n ia ia-case-api-pr-2856-home-office-integration-api-568d84dfb7cr5fm
   ```

## Spring Security Configuration

When the home office endpoint is **not** configured as an anonymous path, it requires both a User Service Token and an S2S Token. The S2S Token must include the `home-office-integration` service name.

### Authentication Requirements

- **Authorization Header**: User Service Token
- **ServiceAuthorization Header**: S2S Token (with `home-office-integration` service name)

### Service Authorization Configuration

Check the `s2s-authorised` configuration in `application.yaml` to see:

- Services authorized to access all IAC endpoints
- Services authorized to access the `home-office-statutory-timeframe-status` endpoint

### Making Authenticated Requests

Example curl command with full authentication:

```bash
curl -X POST \
  https://ia-case-api-pr-2908-home-office-integration-api.preview.platform.hmcts.net/home-office-statutory-timeframe-status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <User Service Token>" \
  -H "ServiceAuthorization: Bearer <S2S Token>" \
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
    "timeStamp": "2025-12-15T10:30:00"
  }' \
  --max-time 30 \
  -v
```

### Simplified Testing (Temporary Endpoints)

For easier testing, temporary endpoints are available that return tokens for IAC. **These will be removed before merging the PR.**

Get S2S Token:

```bash
curl -X GET \
  https://ia-case-api-pr-2908-home-office-integration-api.preview.platform.hmcts.net/s2stoken \
  -H "Accept: text/plain" \
  -w "\nStatus: %{http_code}\n"
```

Get Service User Token:

```bash
curl -X GET \
  https://ia-case-api-pr-2908-home-office-integration-api.preview.platform.hmcts.net/serviceusertoken \
  -H "Accept: text/plain" \
  -w "\nStatus: %{http_code}\n"
```

### Standard Testing (Without Temporary Endpoints)

To obtain the necessary tokens without using the temporary endpoints, follow these steps:

#### Step 1: Retrieve Secrets from Azure Key Vault

First, authenticate with Azure:

```bash
az login
```

Then retrieve the required secrets:

```bash
# Get IDAM admin username
az keyvault secret show --vault-name ia-aat --name idam-admin-username --query value -o tsv

# Get IDAM admin password
az keyvault secret show --vault-name ia-aat --name idam-admin-password --query value -o tsv

# Get IDAM client secret
az keyvault secret show --vault-name ia-aat --name idam-secret --query value -o tsv
```

#### Step 2: Get Service User Token

Use curl to obtain the Service User Token from IDAM:

```bash
curl -X POST "https://idam-api.aat.platform.hmcts.net/o/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "redirect_uri=https://ia-case-api-aat.service.core-compute-aat.internal/oauth2/callback" \
  -d "client_id=iac" \
  -d "client_secret=<ADMIN_WEB_IDAM_SECRET>" \
  -d "username=<IA_CCD_ADMIN_USERNAME>" \
  -d "password=<IA_CCD_ADMIN_PASSWORD>" \
  -d "scope=openid profile roles"
```

Replace the placeholders:

- `<ADMIN_WEB_IDAM_SECRET>`: Value from `idam-secret`
- `<IA_CCD_ADMIN_USERNAME>`: Value from `idam-admin-username`
- `<IA_CCD_ADMIN_PASSWORD>`: Value from `idam-admin-password`

The response will contain several fields. Extract the `access_token` value - this is your `<User Service Token>`.

Example response:

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 28800
}
```

#### Step 3: Get S2S Token

First, install the OATH Toolkit on macOS (if not already installed):

```bash
brew install oath-toolkit
```

Retrieve the S2S secret from Azure Key Vault:

```bash
az keyvault secret show --vault-name s2s-aat --name microservicekey-iac --query value -o tsv
```

Use curl to obtain the S2S Token. The command generates a one-time password using the S2S secret and sends it to the S2S auth provider:

```bash
curl -X POST "http://rpe-service-auth-provider-aat.service.core-compute-aat.internal/lease" \
  -H "Content-Type: application/json" \
  -d '{"microservice":"iac","oneTimePassword":"'"$(oathtool --totp -b "<S2S_SECRET>")"'"}'
```

Replace `<S2S_SECRET>` with the value retrieved from the Key Vault in the previous step.

The response will contain the S2S token:

```json
{
  "token": "Bearer eyJhbGciOiJIUzUxMiJ9..."
}
```

Extract the token value (including "Bearer ") - this is your `<S2S Token>`.

**Note:** The S2S token already includes the "Bearer " prefix in the response, so use it as-is in the `ServiceAuthorization` header.

#### Step 4: Make the Authenticated Request

Now use both tokens to make the request:

```bash
curl -X POST \
  https://ia-case-api-pr-2908-home-office-integration-api.preview.platform.hmcts.net/home-office-statutory-timeframe-status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <User Service Token>" \
  -H "ServiceAuthorization: <S2S Token>" \
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
    "timeStamp": "2025-12-15T10:30:00"
  }' \
  --max-time 30 \
  -v
```

**Note:** The S2S token already includes "Bearer " prefix in the response, so use it as-is in the `ServiceAuthorization` header.
