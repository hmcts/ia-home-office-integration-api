 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.7.3

# Application image
FROM hmctspublic.azurecr.io/base/java:17-distroless

# Change to non-root privilege
USER hmcts

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/ia-home-office-integration-api.jar /opt/app/

EXPOSE 8094
CMD [ "ia-home-office-integration-api.jar" ]
