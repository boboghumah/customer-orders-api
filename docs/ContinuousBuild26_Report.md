# Continuous Build and Delivery Report

Date: 31/03/2026  
Name: OGHUMAH THEOPEE BOB  
Student ID: A00336612  
Student email: A00336612@student.tus.ie  
Repository: [github.com/boboghumah/customer-orders-api](https://github.com/boboghumah/customer-orders-api)

## 1. Introduction

This submission uses the `customer-orders-api` Spring Boot microservice as the application under assessment and builds a complete local CI/CD workflow around it. The solution uses GitHub for version control, Jenkins for orchestration, SonarQube for static analysis and quality gates, Maven for build and test automation, and Docker for packaging and deployment.

Continuous Integration focuses on automatically building and validating code every time changes are pushed to the repository. Continuous Delivery extends that process by producing a deployable artifact and automatically deploying it to a target environment once the required quality checks have passed. In this project, Jenkins clones the repository, runs `./mvnw clean verify`, sends the results to SonarQube, waits for the quality gate, builds a Docker image, deploys the service locally, and verifies the live application through `/actuator/health`.

## 2. Microservice Context

The application manages customers and the orders that belong to them. It exposes a REST API backed by Spring Data JPA and an H2 in-memory database. The main endpoints are:

- `POST /api/customers`
- `GET /api/customers`
- `GET /api/customers/{id}`
- `DELETE /api/customers/{id}`
- `POST /api/customers/{customerId}/orders`
- `GET /api/customers/{customerId}/orders`
- `GET /api/customers/{customerId}/orders/{orderId}`
- `PUT /api/customers/{customerId}/orders/{orderId}`
- `DELETE /api/customers/{customerId}/orders/{orderId}`
- `GET /actuator/health`

The service is small enough to explain clearly in a screencast, but rich enough to demonstrate layered testing, quality gates, image creation, and automated deployment.

## 3. User Stories and Acceptance Criteria

### User story 1: Create and manage customers

As an API consumer, I want to create and retrieve customers so that customer records can be stored and queried.

Acceptance criteria:

- A valid `POST /api/customers` request returns `201 Created`
- `GET /api/customers` returns a paginated list of customers
- `GET /api/customers/{id}` returns `404 Not Found` for a missing customer
- `DELETE /api/customers/{id}` returns `204 No Content` for a successful deletion

### User story 2: Create and manage orders for a customer

As an API consumer, I want to create, view, update, and delete customer orders so that order history can be maintained.

Acceptance criteria:

- A valid `POST /api/customers/{customerId}/orders` returns `201 Created`
- `GET /api/customers/{customerId}/orders` returns that customer's orders only
- `PUT /api/customers/{customerId}/orders/{orderId}` updates the existing order
- `DELETE /api/customers/{customerId}/orders/{orderId}` removes the order and returns `204 No Content`

### User story 3: Filter orders by date range

As an API consumer, I want to filter a customer’s orders by date range so that I can view orders within a specific period.

Acceptance criteria:

- `GET /api/customers/{customerId}/orders?from=yyyy-MM-dd&to=yyyy-MM-dd` returns matching orders
- Invalid date-range combinations return `400 Bad Request`

### User story 4: Receive fast deployment feedback

As a developer, I want Jenkins to build, test, analyze, deploy, and smoke-test the service so that I know whether a pushed change is safe to release.

Acceptance criteria:

- A push triggers Jenkins automatically through a GitHub webhook
- Jenkins fails immediately if the Maven build or test suite fails
- Jenkins fails if the SonarQube quality gate fails
- Jenkins builds and deploys the Docker image only after quality checks pass
- The deployed service responds successfully at `/actuator/health`

## 4. High-Level Architecture

The architecture consists of two connected parts: the application itself and the CI/CD toolchain around it.

The application layer contains controllers, services, repositories, entities, mappers, exception handling, and an in-memory H2 database. The operational layer contains GitHub, Jenkins, SonarQube, Docker, and a local deployment target on the same machine. GitHub acts as the system of record. Jenkins orchestrates the build and deployment workflow. SonarQube evaluates code quality and returns the quality gate result to Jenkins. Docker packages the service into a portable image and runs the deployed container locally.

The architecture diagram is stored in [docs/architecture.mmd](/Users/boboghumah/IdeaProjects/customer-orders-api/docs/architecture.mmd).

## 5. Test Strategy and Test Pyramid

The test strategy follows the Test Pyramid so that confidence is achieved without relying only on slow end-to-end tests.

### Unit tests

Unit tests target the service layer with Mockito-based isolation. Examples include:

- `CustomerServiceTest#create_savesCustomerWhenEmailUnique`
- `CustomerServiceTest#create_throwsWhenEmailExists`
- `OrderServiceTest#getOrder_throwsWhenCustomerMismatch`

These tests are fast and validate business logic and error handling without starting the full Spring context.

### Repository tests

Repository tests use JPA and H2 to verify data access behavior against a real persistence configuration. Examples include:

- `CustomerRepositoryTest#existsByEmail_returnsTrueWhenPresent`
- `OrderRepositoryTest#findByCustomerId_returnsOnlyThatCustomersOrders`
- `OrderRepositoryTest#findByCustomerIdAndOrderDateBetween_filtersByDateRange`

### Web slice tests

Web slice tests use `@WebMvcTest` and mocked services to validate HTTP behavior, request validation, and response payloads. Examples include:

- `CustomerControllerTest#create_returnsCreatedCustomer`
- `OrderControllerTest#list_withInvalidDateRange_returnsBadRequest`
- `OrderControllerTest#update_returnsUpdatedOrder`

### Integration tests

Integration tests use `@SpringBootTest` with `MockMvc` to validate the full application wiring. Examples include:

- `CustomerOrderIntegrationTest#createCustomerThenCreateOrderAndList`
- `CustomerOrderIntegrationTest#getMissingCustomer_returnsNotFound`

This layered approach provides fast feedback at lower levels and stronger confidence at higher levels.

## 6. CI/CD Pipeline Design

The pipeline is defined in [Jenkinsfile](/Users/boboghumah/IdeaProjects/customer-orders-api/Jenkinsfile) and is executed by a local Jenkins controller running in Docker. The pipeline diagram is stored in [docs/pipeline.mmd](/Users/boboghumah/IdeaProjects/customer-orders-api/docs/pipeline.mmd).

### Stage 1: Checkout

Jenkins pulls the project from GitHub and resolves the short commit SHA used for image tagging. This makes the deployment traceable to a specific commit.

### Stage 2: Build and Test

Jenkins runs:

```bash
./mvnw clean verify
```

This compiles the project, runs the unit, repository, web, integration, and context tests, generates JaCoCo coverage data, enforces the `85%` line coverage rule, and runs Checkstyle during the `verify` phase.

### Stage 3: SonarQube Analysis

Jenkins sends the project to SonarQube using the configured SonarQube server connection and token. JaCoCo XML coverage is imported into SonarQube so that the dashboard reflects test coverage as part of the quality assessment.

### Stage 4: Quality Gate

Jenkins pauses using `waitForQualityGate` until SonarQube returns a pass or fail status. This stage prevents packaging and deployment when the code does not meet the configured quality bar.

### Stage 5: Docker Build

If the quality gate passes, Jenkins builds the application image using the repository `Dockerfile`. The image is tagged with the short commit SHA to improve traceability in the build log and deployment steps.

### Stage 6: Deploy

Jenkins runs the deployment helper script, which removes any previous `customer-orders-api` container, starts the new version, and waits for the service to become healthy. This step turns the pipeline into a real delivery flow instead of stopping at artifact creation.

### Stage 7: Smoke Test

After deployment, Jenkins calls the actuator health endpoint to confirm that the running container is available and ready to serve traffic.

## 7. Tool Choices and Rationale

### GitHub

GitHub provides the remote source repository, commit history, and webhook integration for automated pipeline triggering.

### Jenkins

Jenkins was selected because it is a widely used CI/CD tool that clearly exposes each pipeline stage and is well suited for learning how build automation works in detail. It also supports direct integration with GitHub, SonarQube, Docker, and scripted deployment logic.

### SonarQube

SonarQube was chosen as the static analysis and quality-gate platform because it combines issues, code smells, coverage reporting, and gate enforcement in one place. This makes it easy to demonstrate quality control visually during the screencast.

### Docker

Docker was used to package the application into a portable image and to run the deployed version consistently on the local machine. It also simplifies the setup of Jenkins and SonarQube themselves.

### Maven

Maven remains the build entry point because it is already the native build tool for the Spring Boot project and provides a single command, `./mvnw clean verify`, for CI validation.

## 8. Evidence Produced

The final submission can include screenshots of:

- the GitHub commit and push that triggered the pipeline
- the Jenkins stage view showing a successful end-to-end run
- the SonarQube project dashboard and quality gate result
- the Docker container running locally
- the successful `/actuator/health` response

These screenshots align the report, repository, and screencast with the same working implementation.

## 9. Evaluation and Reflection

The finished solution meets the core goals of a continuous build and delivery workflow. The project now has a reproducible path from source control to a running deployed container, and Jenkins enforces meaningful quality checks before deployment is allowed to proceed.

One strength of the solution is that it demonstrates the complete lifecycle in a way that is easy to explain. The same repository contains the application, quality-gate configuration, Docker packaging, deployment script, and Jenkins pipeline definition. Another strength is that quality checks are layered rather than superficial. The pipeline does not only compile code; it also runs the full test suite, validates coverage, checks style rules, analyzes the code in SonarQube, and then verifies the deployed service through a smoke test.

The main limitation is that deployment still targets the local machine rather than a shared environment such as a cloud host or staging server. For a coursework submission this is acceptable because it keeps the infrastructure manageable and easy to demonstrate, but it would not be sufficient for a production release process. A logical improvement would be to add a dedicated staging environment and branch-based promotion rules so that deployment is separated from the Jenkins controller host.

Another improvement would be to formalize secrets management and environment-specific configuration rather than keeping the setup intentionally local and simple for learning purposes.

## 10. Conclusion

This project demonstrates a complete CI/CD implementation around the `customer-orders-api` microservice. GitHub stores the source, Jenkins orchestrates the pipeline, SonarQube enforces analysis and quality gates, Maven performs compilation and testing, Docker packages and deploys the service, and the actuator health endpoint confirms the release is live. The result is a practical and explainable continuous build and delivery workflow that matches the assignment brief and is suitable for a live screencast demonstration.
