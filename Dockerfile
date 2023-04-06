 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.4.8

# Application image

FROM hmctspublic.azurecr.io/base/java:11-distroless

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/ia-home-office-integration-api.jar /opt/app/

EXPOSE 8094
CMD [ "ia-home-office-integration-api.jar" ]
