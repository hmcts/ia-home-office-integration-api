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
   - Example: PR 2856  (https://github.com/hmcts/ia-case-api/pull/2856)

2. In `/charts/ia-case-api/values.preview.template.yaml`, replace the latest Home Office image with the PR image created in the previous step:

```yaml
ia-home-office-integration-api:
  enabled: true
  java:
    imagePullPolicy: Always
    image: hmctspublic.azurecr.io/ia/home-office-integration-api:pr-435
```

3. Push your changes and build the PR.

## Test the integration

1. Update the CCD case definition as needed for your IA Case API PR.

2. Use Case Management to create and submit a test case.

3. Connect to the database and verify the case data does not have any `statutoryTimeframe24Weeks` fields populated initially.

4. Check the logs in kubernetes
