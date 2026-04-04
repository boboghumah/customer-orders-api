# customer-orders-api

Spring Boot microservice for managing customers and their orders, packaged as a coursework CI/CD example using GitHub, Jenkins, SonarQube, "Maven", and "Docker".

## Application Overview

The service exposes a small REST API for creating, listing, updating, and deleting customers and orders. It uses:

- Spring Boot 4
- Java 17
- Spring Web MVC
- Spring Data JPA
- H2 in-memory database
- Spring Boot Actuator

## API Endpoints

### Customers

- `POST /api/customers`
- `GET /api/customers`
- `GET /api/customers/{id}`
- `DELETE /api/customers/{id}`

### Orders

- `POST /api/customers/{customerId}/orders`
- `GET /api/customers/{customerId}/orders`
- `GET /api/customers/{customerId}/orders/{orderId}`
- `PUT /api/customers/{customerId}/orders/{orderId}`
- `DELETE /api/customers/{customerId}/orders/{orderId}`

### Operational Endpoint

- `GET /actuator/health`

## API Notes

### Date Format

All dates use `yyyy-MM-dd` such as `2024-06-30`.

### Pagination

List endpoints accept standard Spring Data pagination parameters:

- `page` starting at `0`
- `size` with default `20`
- `sort` such as `sort=orderDate,desc`

Responses include metadata such as `size`, `number`, `totalElements`, and `totalPages`.

## Run Locally

### 1. Build and test

```bash
./mvnw clean verify
```

This command compiles the application, runs the test suite, generates JaCoCo coverage, enforces the coverage rule, and runs Checkstyle.

### 2. Start the application directly

```bash
./mvnw spring-boot:run
```

The application runs on `http://localhost:8080`.

### 3. Check health

```bash
curl http://localhost:8080/actuator/health
```

## Docker

### Build the application JAR

```bash
./mvnw clean package
```

### Build the image

```bash
docker build -t customer-orders-api:local .
```

### Run the container

```bash
docker run --name customer-orders-api -p 8080:8080 customer-orders-api:local
```

### Deploy with the helper script

```bash
./scripts/deploy-local.sh
```

The deployment script:

- removes an existing `customer-orders-api` container if present
- starts the selected image
- waits for the service to finish booting
- verifies `GET /actuator/health`

## Local Jenkins and SonarQube Stack

The repository includes Docker Compose infrastructure for a local CI/CD environment under [coursework/cicd/compose.yaml](/Users/boboghumah/IdeaProjects/customer-orders-api/coursework/cicd/compose.yaml).

### Start the stack

```bash
docker compose -f coursework/cicd/compose.yaml up -d --build
```

### Services

- Jenkins: `http://localhost:8081`
- SonarQube: `http://localhost:9000`

## Jenkins Pipeline

The pipeline is defined in [Jenkinsfile](/Users/boboghumah/IdeaProjects/customer-orders-api/Jenkinsfile).

Main stages:

1. Checkout
2. Build and Test
3. SonarQube Analysis
4. Quality Gate
5. Docker Build
6. Deploy
7. Smoke Test

`main` is the intended deployment branch. Feature branches can still run the build, test, and analysis stages.

## Quality Gates

The Maven build includes:

- JaCoCo line coverage enforcement at `85%`
- Checkstyle verification during `verify`

SonarQube then provides:

- code-quality analysis
- issue reporting
- coverage import from JaCoCo
- quality gate status returned to Jenkins
