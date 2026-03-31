# Screencast Runbook (Max 10 minutes)

## Goal

Record a short walkthrough showing that the repository now has a working CI/CD pipeline using GitHub, Jenkins, SonarQube, Maven, and Docker, and that a code push can flow through build, analysis, quality gate, deployment, and smoke testing.

## Before Recording

Prepare these windows in advance:

- GitHub repository page
- Jenkins pipeline job and stage view
- SonarQube project dashboard
- Terminal in the project root
- Browser tab for `http://localhost:8080/actuator/health`

Have Jenkins and SonarQube already running locally:

```bash
docker compose -f coursework/cicd/compose.yaml up -d --build
```

## Suggested Timeline

### 0:00 to 0:45 - Introduction

Say:

“Hi, I’m Oghumah Theopee Bob, and this is my Continuous Build and Delivery assignment. I’ll demonstrate a Spring Boot microservice integrated with GitHub, Jenkins, SonarQube, Maven, and Docker. The pipeline builds, tests, analyzes, deploys, and then smoke-tests the running application.”

Show:

- repository root
- `README.md`
- `Jenkinsfile`

### 0:45 to 1:45 - Briefly explain the application

Say:

“`customer-orders-api` is a REST API for managing customers and their orders. It includes customer endpoints, nested order endpoints, and an actuator health endpoint used by the deployment smoke test.”

Show:

- [CustomerController.java](/Users/boboghumah/IdeaProjects/customer-orders-api/src/main/java/com/example/customerordersapi/controller/CustomerController.java)
- [OrderController.java](/Users/boboghumah/IdeaProjects/customer-orders-api/src/main/java/com/example/customerordersapi/controller/OrderController.java)

### 1:45 to 2:45 - Explain the pipeline design

Say:

“Jenkins reads the pipeline from the repository. It checks out the code, runs `./mvnw clean verify`, sends analysis to SonarQube, waits for the quality gate, builds the Docker image, deploys the container locally, and verifies the live service using `/actuator/health`.”

Show:

- [Jenkinsfile](/Users/boboghumah/IdeaProjects/customer-orders-api/Jenkinsfile)
- [docs/pipeline.mmd](/Users/boboghumah/IdeaProjects/customer-orders-api/docs/pipeline.mmd)

### 2:45 to 3:30 - Make a small visible change

Make a tiny safe change that is easy to explain, such as:

- updating wording in `README.md`
- updating a comment in the report
- adjusting a non-functional documentation line

Say:

“Here I’m making a small change so I can demonstrate the full push-to-pipeline flow.”

### 3:30 to 4:15 - Commit and push

In the terminal run:

```bash
git add .
git commit -m "Demo CI/CD pipeline trigger"
git push
```

Say:

“This push triggers Jenkins automatically through the GitHub webhook.”

Show:

- the terminal commit and push
- the GitHub commit appearing in the repository

### 4:15 to 6:00 - Jenkins pipeline execution

Show Jenkins as the new build starts.

Say:

“Jenkins has started the pipeline automatically. The Build and Test stage runs `./mvnw clean verify`, which executes the full test suite, Checkstyle, and JaCoCo coverage checks. After that, SonarQube analysis runs and Jenkins waits for the quality gate result.”

Highlight:

- successful `Build and Test`
- successful `SonarQube Analysis`
- successful `Quality Gate`

### 6:00 to 7:00 - SonarQube quality results

Switch to SonarQube.

Say:

“SonarQube receives the analysis from Jenkins and applies the quality gate. It also imports JaCoCo coverage from the Maven build so test coverage is visible in the dashboard.”

Show:

- project overview
- quality gate status
- coverage and issues summary

### 7:00 to 8:00 - Docker build and deployment

Return to Jenkins and show the later stages.

Say:

“Once the quality gate passes, Jenkins builds the Docker image, deploys the container locally with a helper script, and then runs a smoke test against the actuator health endpoint.”

Highlight:

- `Docker Build`
- `Deploy`
- `Smoke Test`

### 8:00 to 8:45 - Show the running application

In the terminal or browser run:

```bash
curl http://localhost:8080/actuator/health
```

Say:

“This confirms the deployed container is running and healthy after the pipeline completes.”

### 8:45 to 9:30 - Evaluation

Say:

“A strength of this solution is that quality checks are enforced before deployment. The pipeline does not stop at compilation; it also runs tests, style checks, coverage checks, SonarQube analysis, and a real deployment verification. A limitation is that the deployment target is still the local machine rather than a shared staging environment.”

### 9:30 to 10:00 - Close

Say:

“That completes the demonstration of the CI/CD workflow for `customer-orders-api`. Thank you.”
