# ia-home-office-integration-api
Service to trigger Home Office API calls (ATLAS)

### Background
There are Business needs to call Home Office, through their API. 
`ia-home-office-integration-api` allows HMCTS to
Get case related data from Home Office and validate the appeal submitted.
HMCTS will notify Home Office on certain notifications and directions.

### Prerequisites

To run the project you will need to have the following installed:

* Java 17
* Docker (optional)

For information about the software versions used to build this API and a complete list of it's dependencies see build.gradle

### Running application

`ia-home-office-integration-api` is common Spring Boot application. Command to run:
```
./gradlew clean bootRun
```

### Testing application
Unit tests and code style checks:
```
./gradlew clean build
```

Integration tests use Wiremock and Spring MockMvc framework:
```
./gradlew integration
```

Functional tests use started application instance:
```
./gradlew functional
```

In order for these tests to run successfully you will need its dependencies to be running.

Firstly, the ia-home-office-mock-api needs to be running (port 8098), this api along with instructions to run locally can be found at https://github.com/hmcts/ia-home-office-mock-api 

Environment variable HOME_OFFICE_ENDPOINT should point to http://localhost:8098

To successfully interact with the above dependencies a few environment variables need to be set as below. The examples (the values below are not real, replace them with values matching those in the latest CCD Definition spreadsheet):

| Environment Variable | *Example values*  |
|----------------------|----------|
| TEST_CASEOFFICER_USERNAME         |  ia-caseofficer@example.com           |
| TEST_CASEOFFICER_PASSWORD         |  password                             |
| TEST_LAW_FIRM_A_USERNAME          |  ia-law-firm-a@example.com            |
| TEST_LAW_FIRM_A_PASSWORD          |  password                             |
| IA_IDAM_CLIENT_ID                 |  some-idam-client-id                  |
| IA_IDAM_SECRET                    |  some-idam-secret                     |
| IA_IDAM_REDIRECT_URI              |  http://localhost:3451/oauth2redirect |
| IA_S2S_SECRET                     |  some-s2s-secret                      |
| IA_S2S_MICROSERVICE               |  some-s2s-gateway                     |
| HOME_OFFICE_ENDPOINT              |  http://localhost:8098                |

If you want to run a specific scenario use this command:

```
./gradlew functional --tests CcdScenarioRunnerTest --info -Dscenario=RIA-3271
```

### Running smoke tests:

If the API is running (either inside a Docker container or via `gradle bootRun`) you can run the *smoke tests* as follows:

```
./gradlew smoke
```

### Running contract or pact tests:

You can run contract or pact tests as follows:

```
./gradlew contract
```

### Using the application

To understand if the application is working, you can call it's health endpoint:

```
curl http://localhost:8094/health
```

If the API is running, you should see this response:

```
{"status":"UP"}
```

### Usage
API details about usages and error statuses are placed in [Swagger UI](http://ia-home-office-integration-api-aat.service.core-compute-aat.internal/swagger-ui.html)

### Implementation

`ia-home-office-integration-api` has finite retry policy and it tries configurable number of times to submit given CCD Event.

Authentication is defined as any other Reform application with Idam `Authorization` token and S2S `ServiceAuthorization` token.

Every Business logic and validation have to be implemented in scheduled CCD Event. `ia-home-office-integration-api` is not responsible for checking case state data.
 
For example: Home Office case data may become not needed anymore after appeal is moved from appealSubmitted state. 

In this case downstream application (ia-case-api) must implement robust logic to prevent unsuspected behaviour and handle future CCD Event submission gracefully. 


## Adding Git Conventions

### Include the git conventions.
* Make sure your git version is at least 2.9 using the `git --version` command
* Run the following command:
```
git config --local core.hooksPath .git-config/hooks
```
Once the above is done, you will be required to follow specific conventions for your commit messages and branch names.

If you violate a convention, the git error message will report clearly the convention you should follow and provide
additional information where necessary.

*Optional:*
* Install this plugin in Chrome: https://github.com/refined-github/refined-github

  It will automatically set the title for new PRs according to the first commit message, so you won't have to change it manually.

  Note that it will also alter other behaviours in GitHub. Hopefully these will also be improvements to you.

*In case of problems*

1. Get in touch with your Technical Lead and inform them, so they can adjust the git hooks accordingly
2. Instruct IntelliJ not to use Git Hooks for that commit or use git's `--no-verify` option if you are using the command-line
3. If the rare eventuality that the above is not possible, you can disable enforcement of conventions using the following command

   `git config --local --unset core.hooksPath`

   Still, you shouldn't be doing it so make sure you get in touch with a Technical Lead soon afterwards.


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
