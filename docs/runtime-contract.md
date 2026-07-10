# Auth-Service Runtime Contract

This document is the authoritative runtime integration contract for
`auth-service`. External repositories, deployment automation, CI/CD pipelines,
and orchestration tooling should use this contract when integrating the service
instead of inferring runtime behavior from source code, Docker configuration, or
Kubernetes manifests.

The contract describes stable runtime interfaces and required operating
conditions. It does not document internal implementation, source structure, or
business logic.

## Contract Status

- Service name: `auth-service`
- Runtime type: Spring Boot HTTP service packaged as an executable Java JAR
- Java runtime: Java 21
- Build tool: Maven
- Artifact name: `auth-service.jar`
- Container image entrypoint: `java -jar /app.jar`
- Primary protocol: HTTP
- Stable API base path: `/api/auth`

The contract currently documents the supported local runtime and the repository
owned Kubernetes integration shape. Future development, staging, and production
environment contracts should extend this document as those environments are
formalized.

## Startup Contract

The service can be started locally from the repository root with Maven:

```sh
mvn spring-boot:run
```

The service can also be started from a packaged JAR:

```sh
mvn package
java -jar target/auth-service.jar
```

Containerized runtimes must provide `target/auth-service.jar` to the Docker
build context. The repository Dockerfile copies that artifact to `/app.jar`,
exposes container port `8080`, and starts it with:

```sh
java -jar /app.jar
```

## Runtime Dependencies

`auth-service` requires the following external runtime dependencies:

| Dependency | Purpose | Required for |
| --- | --- | --- |
| PostgreSQL | Application datasource | Service startup and persistence access |
| Keycloak | User registration and login integration | `/api/auth/register` and `/api/auth/login` |

The service expects PostgreSQL to be reachable at the configured JDBC URL and
Keycloak to be reachable at the configured Keycloak server URL. Runtime
environments are responsible for provisioning those services and supplying valid
credentials.

## Configuration Contract

Runtime configuration is supplied through Spring Boot configuration and
environment variables. External consumers should treat the following variables
as the supported configuration interface.

| Variable | Purpose | Default | Required for |
| --- | --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Selects the active Spring profile | `default` | Environment-specific runtime behavior |
| `SERVER_PORT` | Overrides the local profile HTTP port | `8081` for `local` profile | Local runtime port selection |
| `AUTH_DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/authdb` | Runtime datasource |
| `AUTH_DB_USERNAME` | PostgreSQL username | `auth_user` in default profile, empty in local profile | Runtime datasource |
| `AUTH_DB_PASSWORD` | PostgreSQL password | `auth_password` in default profile, empty in local profile | Runtime datasource |
| `AUTH_KEYCLOAK_SERVER_URL` | Keycloak server URL | `http://localhost:8080` | Keycloak integration |
| `AUTH_KEYCLOAK_REALM` | Keycloak realm used for application users | `auth`, `az` in local profile | Registration and login |
| `AUTH_KEYCLOAK_CLIENT_ID` | Keycloak client used for login | `auth-service` | Login |
| `AUTH_KEYCLOAK_CLIENT_SECRET` | Optional Keycloak login client secret | Empty | Login when the Keycloak client requires a secret |
| `AUTH_KEYCLOAK_ADMIN_REALM` | Keycloak realm used for admin authentication | `master` | Registration |
| `AUTH_KEYCLOAK_ADMIN_CLIENT_ID` | Keycloak admin client ID | `auth-service-admin` | Registration |
| `AUTH_KEYCLOAK_ADMIN_CLIENT_SECRET` | Keycloak admin client secret | Empty | Registration when using client credentials |
| `AUTH_KEYCLOAK_ADMIN_USERNAME` | Keycloak admin username | Empty | Registration when using password credentials |
| `AUTH_KEYCLOAK_ADMIN_PASSWORD` | Keycloak admin password | Empty | Registration when using password credentials |

Registration requires one supported Keycloak admin credential mode:

- `AUTH_KEYCLOAK_ADMIN_CLIENT_ID` with `AUTH_KEYCLOAK_ADMIN_CLIENT_SECRET`
- `AUTH_KEYCLOAK_ADMIN_CLIENT_ID` with `AUTH_KEYCLOAK_ADMIN_USERNAME` and
  `AUTH_KEYCLOAK_ADMIN_PASSWORD`

Secrets must be supplied by the runtime environment or secret-management system.
They must not be committed to the repository.

## Local Development Contract

The supported local development profile is `local`.

Use `.env.example` as the local configuration template, copy it to `.env`, and
replace the `PLACEHOLDER_*` values with local-only credentials before exporting
the variables into the shell or IDE.

The current local contract is:

| Setting | Local value |
| --- | --- |
| Active profile | `SPRING_PROFILES_ACTIVE=local` |
| Service URL | `http://localhost:8081` |
| PostgreSQL URL | `jdbc:postgresql://localhost:5432/platform` |
| Keycloak URL | `http://localhost:8080` |
| Keycloak realm | `az` |
| Keycloak login client | `auth-service` |
| Keycloak admin client | `auth-service-admin` |

The local service port is `8081` so that local Keycloak can use
`http://localhost:8080`.

## Network Contract

### Process Runtime

For the default profile, the service listens on HTTP port `8080`.

For the local profile, the service listens on `SERVER_PORT`, defaulting to
`8081`.

### Kubernetes Runtime

The repository-owned Kubernetes manifests define the following integration
shape:

| Interface | Value |
| --- | --- |
| Namespace | `auth-service` |
| Workload | Deployment `auth-service` |
| Container port | `8080` named `http` |
| Service | ClusterIP Service `auth-service` |
| Service port | `8080` targeting named port `http` |
| Ingress class | `nginx` |
| Ingress host | `auth-service.example.com` |
| Ingress path | `/` with `Prefix` matching |
| TLS secret | `auth-service-tls` |
| Image pull secret | `ghcr-auth-service-pull` |

Remote Kubernetes environments must provide an ingress controller for the
`nginx` ingress class, DNS for the configured host, a TLS secret named
`auth-service-tls`, and valid registry credentials for pulling the service image
when the image is not already present in the cluster.

## HTTP Interface Contract

The stable externally visible API base path is:

```text
/api/auth
```

The currently exposed endpoints are:

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Register a user in Keycloak |
| `POST` | `/api/auth/login` | Authenticate a user through Keycloak and return token data |

External consumers should not rely on undocumented routes or internal service
classes as integration surfaces.

## Health and Readiness Contract

The current runtime does not define a dedicated HTTP health endpoint.

Kubernetes readiness and liveness are represented by TCP probes against the
named `http` container port:

- readiness probe: TCP socket on `http`, initial delay 15 seconds, period 10
  seconds
- liveness probe: TCP socket on `http`, initial delay 30 seconds, period 20
  seconds

For local ingress validation, an HTTP response from the Spring Boot application
confirms that traffic reached the service. A `404` response for `/` is valid for
the current runtime because no root HTTP controller is part of the public
contract.

## Stable Integration Interfaces

External consumers may rely on:

- the service name `auth-service`
- Java 21 executable JAR runtime
- Maven build and test entrypoints
- container startup through `java -jar /app.jar`
- HTTP service exposure on the configured server port
- the `/api/auth` API base path
- the documented configuration variables
- PostgreSQL and Keycloak as required runtime dependencies
- Kubernetes Service `auth-service` on port `8080`
- Kubernetes TCP readiness and liveness probes on the named `http` port

External consumers must not rely on:

- internal Java package names, classes, or method names
- undocumented endpoints
- checked-in non-local credentials or placeholder secrets
- source-level implementation details
- Dockerfile or manifest inference that conflicts with this contract

## Validation Contract

The configured repository validation command is:

```sh
mvn test
```

Validation environments must provide the JDK and Maven versions required by the
build. Test-time datasource configuration is supplied through the `test` profile
defaults unless overridden by the environment.
