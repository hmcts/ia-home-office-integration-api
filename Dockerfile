ARG APP_INSIGHTS_AGENT_VERSION=2.5.1-BETA

# Application image

FROM hmctspublic.azurecr.io/base/java:openjdk-8-distroless-1.2

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/ia-homeoffice-integration-api.jar /opt/app/

EXPOSE 8098
CMD [ "ia-homeoffice-integration-api.jar" ]
