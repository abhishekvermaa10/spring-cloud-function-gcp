# EventPulse - Spring Cloud Function on Google Cloud Platform

EventPulse is a small event-registration demo built with Java, Spring Boot, and Spring Cloud Function.

It demonstrates three functional interfaces:

* **Supplier --- `publishEvents`:** returns the available events.
* **Function --- `registerParticipant`:** accepts an event ID and participant email, then returns a registration result.
* **Consumer --- `sendRegistrationEmail`:** logs a mock confirmation email for a new registration. It skips the email when the participant is already registered.

The repository uses in-memory data, so registrations are not persisted after the application restarts or a new function instance handles the request.

## Contents

* [Prerequisites](#prerequisites)
* [Clone the project](#clone-the-project)
* [Project structure](#project-structure)
* [Run locally with HTTP endpoints](#run-locally-with-http-endpoints)
* [Test the local endpoints](#test-the-local-endpoints)
* [Prepare the GCP deployment build](#prepare-the-gcp-deployment-build)
* [Prepare Google Cloud Platform](#prepare-google-cloud-platform)
* [Deploy to Google Cloud Platform](#deploy-to-google-cloud-platform)
* [Test the deployed functions](#test-the-deployed-functions)
* [Clean up Google Cloud Platform](#clean-up-google-cloud-platform)
* [Troubleshooting](#troubleshooting)
* [Important demo limitations](#important-demo-limitations)
* [Version updates and contributions](#version-updates-and-contributions)
* [References](#learn-more)
* [Connect with me](#connect-with-me)

## Prerequisites

Install or prepare the following:

* **JDK 25**
* **Maven** (or use the included Maven Wrapper if the repository contains `mvnw`)
* **Git**
* **Google Cloud Platform account**

Check your local Java and Maven installations:

``` bash
java -version
mvn -version
```

Check `pom.xml` for the exact Spring Boot, Spring Cloud Function, and Java versions used by the current working revision. Versions evolve, so use the versions in the repository rather than copying version numbers from an older tutorial.

## Clone the project

``` bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd EventPulse
```

## Project structure

The project is organized around the following responsibilities:

``` text
EventPulse/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── .../
        │       ├── EventPulseApplication.java
        │       ├── config/
        │       │   ├── FunctionConfig.java
        │       │   └── GsonConfig.java
        │       ├── model/
        │       │   ├── ConferenceEvent.java
        │       │   ├── RegistrationRequest.java
        │       │   ├── RegistrationResponse.java
        │       │   └── RegistrationStatus.java
        │       ├── repository/
        │       │   ├── EventRepository.java
        │       │   ├── RegistrationRepository.java
        │       │   ├── impl/
        │       │       ├── EventRepositoryImpl.java
        │       │       └── RegistrationRepositoryImpl.java
        │       └── service/
        │           ├── EventService.java
        │           ├── impl/
        │               └── EventServiceImpl.java
        └── resources/
            └── application.properties 
            └── META-INF
                └── MANIFEST.MF
```

`FunctionConfig` declares the `Supplier`, `Function`, and `Consumer` beans. The service layer contains the registration workflow, while repository implementations provide the demo's in-memory data. `pom.xml` controls whether the project is configured for local deployment or for the Google Cloud Platform deployment. Your checked-out revision is the source of truth if a file or package is named differently.

## Run locally with HTTP endpoints

The repository is configured for **local testing by default**. In `pom.xml`, keep `spring-boot-starter` and `spring-cloud-starter-function-web` enabled, and keep the GCP-specific dependency and plugin configuration commented out.

If you are switching back from the GCP setup, make these changes in `pom.xml` first:

1.  Uncomment the `spring-cloud-starter-function-web` dependency.
2.  Comment out the `spring-cloud-function-adapter-gcp` dependency.
3.  Comment out the GCP-specific build configuration: the GCP adapter dependency nested under `spring-boot-maven-plugin`, and the `function-maven-plugin` block.
4.  Comment out the Gson bean in `GsonConfig.java`.

Save the file, then start the application:

``` bash
mvn spring-boot:run
```

If you use the Maven Wrapper, run `./mvnw spring-boot:run` on macOS/Linux or `.\mvnw.cmd spring-boot:run` on Windows.

Keep the application running while you test the endpoints. Spring Cloud Function's web adapter exposes the function beans over HTTP. The examples below assume the function bean names in `FunctionConfig` are:

* `publishEvents`
* `registerParticipant`
* `sendRegistrationEmail`

If you change those bean names, update the URLs and deployment environment variables accordingly.

## Test the local endpoints

You can use `curl`, Postman, or another HTTP client. The examples below assume the application is listening on `http://localhost:8080`. If your local application uses a different port, change the URL.

### A. List events --- Supplier

``` bash
curl.exe --location --request GET "http://localhost:8080/publishEvents"
```

Expected result: a JSON array of `ConferenceEvent` objects, each containing `id`, `name`, `startDate`, `endDate`, and `location`.

Example response shape:

``` json
[
    {
        "id": "EVT-101",
        "name": "Java2Days",
        "startDate": "2022-03-08",
        "endDate": "2022-03-11",
        "location": "Online"
    },
    {
        "id": "EVT-102",
        "name": "Devoxx",
        "startDate": "2026-10-05",
        "endDate": "2026-10-09",
        "location": "Antwerp, Belgium"
    },
    {
        "id": "EVT-103",
        "name": "GDG Cloud Chandigarh - DevFest & Code for Communities 2026",
        "startDate": "2026-10-23",
        "endDate": "2026-10-24",
        "location": "Chandigarh, India"
    }
]
```

The exact list depends on the events configured in `EventRepositoryImpl`.

### B. Register a participant --- Function

Use an event ID returned by the Supplier:

``` bash
curl.exe --location --request POST "http://localhost:8080/registerParticipant" \
--header 'Content-Type: application/json' \
--data-raw '{
    "eventId": "EVT-101",
    "emailId": "participant@scaleupindiayt.com"
}'
```

Expected result: a `RegistrationResponse` containing `registrationId`, `eventId`, `emailId`, and `status`.

On the first registration, the status should be `REGISTERED`. Repeat the same request with the same event ID and email to see `ALREADY_REGISTERED`; the existing registration ID should be returned.

### C. Register a participant and trigger the Consumer --- Function + Consumer

The composed function accepts a `RegistrationRequest`, invokes `registerParticipant`, and automatically passes the resulting `RegistrationResponse` to `sendRegistrationEmail`.

Send a registration request:

``` bash
curl.exe --location --request POST 'http://localhost:8080/registerParticipant,sendRegistrationEmail' \
--header 'Content-Type: application/json' \
--data-raw '{
    "eventId": "EVT-101",
    "emailId": "participant@scaleupindiayt.com"
}'
```

Check the application logs. A new registration should produce a mock confirmation-email message. Repeating the same request should produce an ALREADY_REGISTERED response, and the Consumer should skip the email.

Repeat the same request with the same event ID and email. The Function should detect the existing registration and produce an `ALREADY_REGISTERED` result. The Consumer will receive that result and skip the mock email. Check the application logs to verify this behavior.

> **Note:** This composed endpoint accepts the Function input (RegistrationRequest). The Function creates the RegistrationResponse and passes it to the Consumer automatically.

## Prepare the GCP deployment build

After you have tested the application locally, stop it and switch `pom.xml` to the GCP configuration. **Do not leave the local and GCP configurations enabled together.**

1.  Comment out the `spring-cloud-starter-function-web` dependency.
2.  Uncomment the `spring-cloud-function-adapter-gcp` dependency.
3.  Uncomment the GCP-specific build configuration: the GCP adapter dependency nested under `spring-boot-maven-plugin`, and the `function-maven-plugin` block.
4.  Uncomment the Gson bean in `GsonConfig.java`.
5.  Ensure that Main-Class variable exists in `MANIFEST.MF`.

Keep the core `spring-boot-starter` dependency enabled. Save `pom.xml`.

### Verify the GCP adapter locally before deployment

Before packaging or deploying, verify the GCP configuration with the Google Functions Framework. The `function-maven-plugin` supports this local verification; it is not required by the `gcloud functions deploy` command itself.

1.  Set the function definition and start the local Functions Framework.

On macOS/Linux:

``` bash
SPRING_CLOUD_FUNCTION_DEFINITION=publishEvents
mvn function:run
```

On Windows PowerShell:

``` powershell
$env:SPRING_CLOUD_FUNCTION_DEFINITION="publishEvents"
mvn function:run
```

The server listens on `http://localhost:8080` with the configured plugin settings. Keep this terminal running.

2.  In a second terminal, invoke the selected function:

``` bash
curl -X GET http://localhost:8080/
```

For `publishEvents`, confirm that the response contains the configured events. To verify `registerParticipant` or `sendRegistrationEmail`, stop the server, change `SPRING_CLOUD_FUNCTION_DEFINITION` to the desired bean name, restart `mvn function:run`, and send the corresponding JSON request payload to `http://localhost:8080/`. Test one function at a time.

3.  Stop the local Functions Framework server before continuing.

If `mvn function:run` fails, confirm that the GCP adapter dependency and the `function-maven-plugin` block are enabled in `pom.xml`, and that `functionTarget` is set to `org.springframework.cloud.function.adapter.gcp.GcfJarLauncher`.

Once verification succeeds, build the deployment JAR:

``` bash
mvn clean package
```

The GCP configuration packages the deployable JAR under `target`. Confirm the JAR exists:

``` bash
ls target
```

The expected artifact path is `target/EventPulse.jar`. If the JAR is not present, inspect the Maven build output and confirm that the GCP packaging configuration is active.

## Prepare Google Cloud Platform

Open [Google Cloud Console](https://console.cloud.google.com/) and start **Cloud Shell**. The commands below run inside Cloud Shell.

Choose a globally unique project ID dedicated to this demo. The examples use `eventpulse-demo`; replace it if that ID is unavailable or you prefer another ID.

### A. Create and select the project

``` bash
gcloud projects create eventpulse-demo --name="EventPulse Demo"
gcloud config set project eventpulse-demo
```

If your organization requires a parent organization or folder, follow your organization's project-creation requirements. If you already created a project, skip the creation command and set the correct project instead.

### B. Link billing

List billing accounts you can use:

``` bash
gcloud billing accounts list
```

Link an active billing account to the project, replacing the placeholder with an account ID from the previous command:

``` bash
gcloud billing projects link eventpulse-demo \
  --billing-account=YOUR_BILLING_ACCOUNT_ID
```

Verify billing:

``` bash
gcloud billing projects describe eventpulse-demo
```

Confirm that the output reports `billingEnabled: true`. Billing is required for the resources used by this demo. Charges may apply while resources exist, so follow the cleanup section at the end.

### C. Enable the required APIs

``` bash
gcloud services enable \
  cloudfunctions.googleapis.com \
  run.googleapis.com \
  cloudbuild.googleapis.com \
  artifactregistry.googleapis.com \
  logging.googleapis.com \
  storage.googleapis.com \
  eventarc.googleapis.com \
  --project=eventpulse-demo
```

Google may enable some APIs automatically during deployment. Enabling them ahead of time helps surface permission and billing issues early. If an API is unavailable or your account lacks permission, follow the error message and your organization's Google Cloud setup requirements.

See [Google Cloud Run functions deployment prerequisites](https://docs.cloud.google.com/run/docs/quickstarts/functions/deploy-functions-gcloud).

## Deploy to Google Cloud Platform

The commands below deploy three separate HTTP-triggered functions from the same JAR. Each deployment selects a function using `SPRING_CLOUD_FUNCTION_DEFINITION`. Complete **Prepare the GCP deployment build** and **Prepare Google Cloud Platform** before following these steps.

The examples use `us-central1`. You can choose another region closer to your audience, provided Cloud Run functions is available there and the region supports the selected runtime. Keep the same region for deployment, testing, logs, and cleanup. Check [available Cloud Run functions locations](https://cloud.google.com/run/docs/locations) before choosing a different region.

Set the project and region once in Cloud Shell:

``` bash
gcloud config set project eventpulse-demo
PROJECT_ID=eventpulse-demo
REGION=us-central1
```

### A. Confirm your Google Cloud project

``` bash
gcloud config get-value project
```

Make sure it prints the EventPulse project ID. If it does not, run:

``` bash
gcloud config set project YOUR_PROJECT_ID
```

Replace `YOUR_PROJECT_ID` with your actual Google Cloud project ID. If deployment reports that an API is disabled, enable the API named in the error and retry.

### B. Upload the JAR to Cloud Shell

Build the deployment JAR locally in **Prepare the GCP deployment build**. In the Cloud Shell toolbar, choose **Upload** and upload `target/EventPulse.jar` from your computer. Cloud Shell's upload dialog is the only UI step in this part of the workflow; the remaining setup and deployment use commands.

After the upload finishes, confirm the file is in your Cloud Shell home directory:

``` bash
ls -lh ~/EventPulse.jar
```

Create a deployment directory and move the JAR into it:

``` bash
mkdir -p ~/eventpulse-deploy
mv ~/EventPulse.jar ~/eventpulse-deploy/
ls -lh ~/eventpulse-deploy
```

The source directory passed to `gcloud functions deploy` must contain `EventPulse.jar`. If Cloud Shell saved the file elsewhere or with a different name, adjust the `mv` command.

### C. Deploy the Supplier: publish events

``` bash
gcloud functions deploy eventpulse-publish-events \
  --gen2 \
  --runtime java25 \
  --region "$REGION" \
  --source "$HOME/eventpulse-deploy" \
  --entry-point org.springframework.cloud.function.adapter.gcp.GcfJarLauncher \
  --trigger-http \
  --allow-unauthenticated \
  --memory 512MiB \
  --set-env-vars SPRING_CLOUD_FUNCTION_DEFINITION=publishEvents
```

This function returns the list of events.

### D. Deploy the Function: register a participant

``` bash
gcloud functions deploy eventpulse-register-participant \
  --gen2 \
  --runtime java25 \
  --region "$REGION" \
  --source "$HOME/eventpulse-deploy" \
  --entry-point org.springframework.cloud.function.adapter.gcp.GcfJarLauncher \
  --trigger-http \
  --allow-unauthenticated \
  --memory 512MiB \
  --set-env-vars SPRING_CLOUD_FUNCTION_DEFINITION=registerParticipant
```

This function validates the request, checks the event, and returns the registration outcome.

### E. Deploy the composed Function + Consumer

``` bash
gcloud functions deploy eventpulse-register-and-notify \
  --gen2 \
  --runtime java25 \
  --region "$REGION" \
  --source "$HOME/eventpulse-deploy" \
  --entry-point org.springframework.cloud.function.adapter.gcp.GcfJarLauncher \
  --trigger-http \
  --allow-unauthenticated \
  --memory 512MiB \
  --set-env-vars 'SPRING_CLOUD_FUNCTION_DEFINITION=registerParticipant|sendRegistrationEmail'
```

This composition invokes `registerParticipant` and passes its result to `sendRegistrationEmail`. A newly registered participant produces a mock confirmation-email log; a duplicate registration causes the Consumer to skip the email.

> These commands preserve the deployment approach from the demo, with the function names updated to match `FunctionConfig`. Google Cloud Platform's deployment requirements can change; if a command fails, consult the current Cloud Run functions documentation and the specific error output.

## Test the deployed functions

After each deployment completes, retrieve its HTTPS URLs:

``` bash
gcloud functions describe eventpulse-publish-events \
  --gen2 \
  --region "$REGION" \
  --format="value(serviceConfig.uri)"
```

``` bash
gcloud functions describe eventpulse-register-participant \
  --gen2 \
  --region "$REGION" \
  --format="value(serviceConfig.uri)"
```

``` bash
gcloud functions describe eventpulse-register-and-notify \
  --gen2 \
  --region "$REGION" \
  --format="value(serviceConfig.uri)"
```

Copy each URL and use it in the commands below.

### A. List events

``` bash
curl.exe --location --request GET "SUPPLIER_FUNCTION_URL"
```

### B. Register a participant

``` bash
curl.exe --location --request POST "REGISTRATION_FUNCTION_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "EVT-101",
    "emailId": "student@scaleupindiayt.com"
  }'
```

Repeat the same request to observe the duplicate-registration response **when the same function instance retains its in-memory state**.

### C. Register and trigger the mock email Consumer

``` bash
curl.exe --location --request POST "REGISTER_AND_NOTIFY_FUNCTION_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "EVT-101",
    "emailId": "student@scaleupindiayt.com"
  }'
```

The composed function's HTTP response may be empty because the final stage is a `Consumer`. Use the logs to verify the mock email behavior.

Inspect the function logs:

``` bash
gcloud functions logs read eventpulse-register-and-notify \
  --gen2 \
  --region "$REGION" \
  --limit 50
```

## Clean up Google Cloud Platform

**Do not skip cleanup.** After you finish the demo, delete all three functions and then delete the dedicated `eventpulse-demo` project. Deleting the project shuts down its resources, but charges already incurred may still appear on your bill. Do not use this project for unrelated work.

Confirm the active project and region first:

``` bash
gcloud config get-value project
echo "$REGION"
```

Make sure the project is the dedicated demo project. If needed, set the variables again:

``` bash
PROJECT_ID=eventpulse-demo
REGION=us-central1
gcloud config set project "$PROJECT_ID"
```

### A. Delete all deployed functions

List functions in the selected region:

``` bash
gcloud functions list --regions="$REGION" --project="$PROJECT_ID"
```

Delete each function created for this demo:

``` bash
gcloud functions delete eventpulse-publish-events \
  --region="$REGION" --project="$PROJECT_ID" --quiet
```

``` bash
gcloud functions delete eventpulse-register-participant \
  --region="$REGION" --project="$PROJECT_ID" --quiet
```

``` bash
gcloud functions delete eventpulse-register-and-notify \
  --region="$REGION" --project="$PROJECT_ID" --quiet
```

If you deployed any additional functions or used a different region, list and delete those too. Verify the demo functions are gone:

``` bash
gcloud functions list --regions="$REGION" --project="$PROJECT_ID"
```

### B. Delete the Google Cloud project

**This permanently removes the entire demo project and its resources---not just the EventPulse functions.** Confirm that the project contains nothing you want to keep before proceeding.

``` bash
gcloud projects delete "$PROJECT_ID"
```

Confirm the deletion if prompted. You can check the project lifecycle state with:

``` bash
gcloud projects describe "$PROJECT_ID"
```

The project may remain in a recoverable deletion state for a limited period before permanent deletion. Deleting the project prevents further use of its resources, but charges already incurred or still being finalized may appear afterward.

If the project was created exclusively for this demo, deleting the project is the most comprehensive cleanup step. Deleting the functions first makes the teardown explicit and verifies that the function resources have been removed before project deletion.

## Troubleshooting

### The function returns 404 or the wrong function output

Check that the deployed function uses the correct definition:

* `publishEvents`
* `registerParticipant`
* `registerParticipant|sendRegistrationEmail`

The names must match the `@Bean` method names in `FunctionConfig`.

### The deployment cannot find the JAR

Verify that `~/eventpulse-deploy` contains `EventPulse.jar`. The `--source` directory must contain the JAR expected by the GCP adapter.

### The local HTTP endpoint does not start

Confirm that `spring-cloud-starter-function-web` is enabled and the GCP adapter dependency and GCP-specific build configuration are commented out in `pom.xml`. Then restart the application. The local web adapter is separate from the GCP deployment adapter.

### The GCP deployment JAR is missing or the deployment fails

Confirm that `spring-cloud-starter-function-web` is commented out and the GCP adapter dependency, the adapter dependency nested under `spring-boot-maven-plugin`, and the `function-maven-plugin` block are uncommented. Run `mvn clean package` again and verify that `target/EventPulse.jar` exists before uploading it.

### A registration appears to be lost

This demo stores events and registrations in memory. Serverless platforms may create new instances or restart existing instances, so in-memory registration state is not durable or shared across instances. Use a persistent datastore for production.

### The Consumer does not send a real email

That is intentional. `sendRegistrationEmail` currently writes a mock confirmation message to the application logs. Integrate an email provider if you want actual email delivery.

## Important demo limitations

* **In-memory storage:** event data is fixed in the repository; registrations are not persisted.
* **Mock email:** the Consumer logs a message instead of sending an email.
* **Unauthenticated endpoints:** `--allow-unauthenticated` makes the deployed HTTP endpoints publicly accessible. Use authentication and appropriate access controls for real applications.
* **Demo-scale configuration:** `512MiB` is the memory setting used in these example commands, not a production sizing recommendation.
* **Function composition:** the `registerParticipant|sendRegistrationEmail` deployment connects the Function output to the Consumer. The three separately deployed functions do not automatically call one another.
* **Dates:** update the sample event dates in `EventRepositoryImpl` before presenting the list as current event data.

## Version updates and contributions

Please check the repository periodically for dependency and runtime updates. You are welcome to **fork this repository and open a pull request** with improvements, including version updates.

When proposing a version change, make sure the versions work together and that the project builds and the demo runs successfully. Include relevant build/test results and update this README if commands or setup steps change. Avoid submitting version bumps that have not been verified to work.

I will continue reviewing and updating dependency versions when possible, while prioritizing versions that are known to work with the demo. The versions in the current `pom.xml` and the latest verified repository revision are the source of truth.

## Learn more

* [Spring Cloud Function reference documentation](https://docs.spring.io/spring-cloud-function/reference/)
* [Google Cloud Run functions documentation](https://cloud.google.com/functions/docs)
* [Google Cloud Functions deployment documentation](https://docs.cloud.google.com/sdk/gcloud/reference/functions/deploy)

## Stay healthy, keep learning, and keep growing!

## Connect with me

* [Website](https://abhishekvermaa10.github.io)
* [YouTube](https://www.youtube.com/@abhishekvermaa10)
* [LinkedIn](https://linkedin.com/in/abhishekvermaa10)
* [Instagram](https://instagram.com/abhishekvermaa10)
* [X ](https://x.com/ytabhishekverma)
* [Topmate](https://topmate.io/abhishekvermaa10)
* [GitHub](https://github.com/abhishekvermaa10)
* [Mail](mailto:scaleupindiayt@gmail.com)

Copyright © Abhishek Verma - All Rights Reserved
