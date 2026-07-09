# Auth-Service Runtime Contract

## Purpose

This document is the authoritative runtime integration contract for `auth-service`.
External infrastructure, deployment automation, CI/CD pipelines, and service
consumers should use this contract instead of inferring runtime behavior from
source code, Docker configuration, or Kubernetes manifests.

The contract describes stable runtime expectations and externally visible
interfaces. It does not describe internal implementation, source layout, or
business logic.

## Stable Integration Surface

External consumers may rely on the following runtime characteristics:

- Service name: `auth-service`.
- Runtime type: Spring Boot HTTP service packaged as an executable JAR.
- Container process: `java -jar /app.jar`.
- Default HTTP container port: `8080`.
- Local development profile: `local`.
- Local development HTTP port: `8081` when `SPRING_PROFILES_ACTIVE=local` and
  `SERVER_PORT` is not overridden.
- HTTP API base path: `/api/auth`.
- Kubernetes service name: `auth-service`.
- Kubernetes namespace: `auth-service`.
- Kubernetes service port name: `http`.
- Kubernetes service port: `8080`.
- Kubernetes ingress host currently documented for local platform validation:
  `auth-service.example.com`.

Any change to these values is a runtime contract change and should be reviewed
as an integration-impacting update.

## Runtime Startup

### Local Maven Runtime

For local development, start the service with Maven:

```sh
mvn spring-boot:run
```

The supported local runtime profile is:

```text
SPRING_PROFILES_ACTIVE=local
```

With the local profile, the service listens on `SERVER_PORT` when supplied and
otherwise listens on `8081`.

### Executable JAR Runtime

The service may also be started from the packaged JAR:

```sh
mvn package
java -jar target/auth-service.jar
```

Without a profile override, the default HTTP port is `8080`.

### Container Runtime

The container contract expects a packaged JAR named `target/auth-service.jar`
in the Docker build context. The image starts the service with:

```text
java -jar /app.jar
```

The image exposes port `8080`.

## Runtime Dependencies

The service requires the following runtime dependencies:

| Dependency | Required For | Contract |
| --- | --- | --- |
| Java runtime | Service process | Java 21-compatible runtime. |
| PostgreSQL | Application datasource | A reachable PostgreSQL database URL, username, and password must be supplied for the active environment. |
| Keycloak | Authentication and registration | A reachable Keycloak server and realm must be supplied. Login uses the configured client. Registration requires configured admin credentials. |

Local development against the Platform-Infra stack expects:

- PostgreSQL reachable at `jdbc:postgresql://localhost:5432/platform` unless
  `AUTH_DB_URL` is overridden.
- Keycloak reachable at `http://localhost:8080` unless
  `AUTH_KEYCLOAK_SERVER_URL` is overridden.
- Keycloak realm `az` unless `AUTH_KEYCLOAK_REALM` is overridden.

## Configuration Contract

The service is configured through Spring Boot configuration and environment
variables. External runtimes should provide environment-specific values rather
than relying on development defaults.

### Required Runtime Selection

| Name | Purpose | Local Default |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Selects the runtime profile. Use `local` for local Platform-Infra development. | `default` when not supplied |
| `SERVER_PORT` | Overrides the HTTP listening port. | `8081` for `local`; `8080` for default |

### Required Database Configuration

| Name | Purpose | Local Default |
| --- | --- | --- |
| `AUTH_DB_URL` | JDBC URL for the PostgreSQL datasource. | `jdbc:postgresql://localhost:5432/platform` under `local` |
| `AUTH_DB_USERNAME` | PostgreSQL username. | No usable local credential default |
| `AUTH_DB_PASSWORD` | PostgreSQL password. | No usable local credential default |

### Required Keycloak Configuration

| Name | Purpose | Local Default |
| --- | --- | --- |
| `AUTH_KEYCLOAK_SERVER_URL` | Base URL of the Keycloak server. | `http://localhost:8080` |
| `AUTH_KEYCLOAK_REALM` | Realm used for user login and registration. | `az` under `local` |
| `AUTH_KEYCLOAK_CLIENT_ID` | Keycloak client used for user login. | `auth-service` |
| `AUTH_KEYCLOAK_CLIENT_SECRET` | Optional client secret for user login when the configured client requires one. | Empty |
| `AUTH_KEYCLOAK_ADMIN_REALM` | Realm used to authenticate administrative registration calls. | `master` |
| `AUTH_KEYCLOAK_ADMIN_CLIENT_ID` | Keycloak admin client used for registration. | `auth-service-admin` |
| `AUTH_KEYCLOAK_ADMIN_CLIENT_SECRET` | Admin client secret for registration when using client credentials. | Empty |
| `AUTH_KEYCLOAK_ADMIN_USERNAME` | Admin username for registration when using username/password credentials. | Empty |
| `AUTH_KEYCLOAK_ADMIN_PASSWORD` | Admin password for registration when using username/password credentials. | Empty |

Registration requires either:

- `AUTH_KEYCLOAK_ADMIN_CLIENT_SECRET`; or
- both `AUTH_KEYCLOAK_ADMIN_USERNAME` and `AUTH_KEYCLOAK_ADMIN_PASSWORD`.

Sensitive values must be supplied by the runtime environment or secret-management
system. They must not be committed to repository configuration.

## Network Contract

### Local Development

When using `.env.example` as the local template:

- `auth-service` listens on `http://localhost:8081`.
- Keycloak listens on `http://localhost:8080`.
- PostgreSQL is reached through `localhost:5432`.

### Kubernetes

The current Kubernetes-facing contract is:

| Interface | Value |
| --- | --- |
| Namespace | `auth-service` |
| Workload | `Deployment/auth-service` |
| Service | `Service/auth-service` |
| Service type | `ClusterIP` |
| Service port | `8080` |
| Service port name | `http` |
| Container port | `8080` |
| Ingress class | `nginx` |
| Ingress host | `auth-service.example.com` |
| Ingress TLS secret | `auth-service-tls` |

Remote environments may provide different DNS, TLS, image tags, and secret
material, but must preserve the service's HTTP reachability contract or document
the environment-specific override.

## HTTP Interfaces

The externally visible HTTP API base path is:

```text
/api/auth
```

Current authentication endpoints:

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Register a user through the configured Keycloak realm. |
| `POST` | `/api/auth/login` | Authenticate a user through the configured Keycloak realm and client. |

Requests and responses are JSON over HTTP.

The service may return stable error response bodies with:

- `code`
- `message`

Infrastructure consumers should treat HTTP status codes as the primary routing
and retry signal. Application error codes are part of the service API surface
and should be consumed by API clients, not by platform readiness checks.

## Health and Readiness Contract

The service does not currently publish a dedicated HTTP health endpoint such as
Spring Boot Actuator `/actuator/health`.

Current readiness and liveness are determined by TCP reachability on the HTTP
port:

- Kubernetes readiness probe: TCP socket probe on the named `http` port.
- Kubernetes liveness probe: TCP socket probe on the named `http` port.

For ingress validation, an HTTP response from the Spring Boot service confirms
that routing reached `auth-service`. A `404` response for `/` is acceptable for
routing validation because the root path is not an application endpoint.

Platform automation must not assume that a successful TCP probe proves Keycloak
or PostgreSQL business-operation readiness. End-to-end readiness for registration
or login requires the configured PostgreSQL and Keycloak dependencies to be
reachable and correctly provisioned.

## Local Development Contract

The repository provides `.env.example` as the local environment template.
External local automation may rely on these local expectations:

- Copy `.env.example` to `.env`.
- Replace all `PLACEHOLDER_*` values with local-only credentials or secrets.
- Export the `.env` values before starting the service.
- Use `SPRING_PROFILES_ACTIVE=local`.
- Use `SERVER_PORT=8081` when running beside local Platform-Infra Keycloak on
  port `8080`.

The `.env` file must remain uncommitted.

## Integration Boundaries

External consumers may rely on this document for:

- how to start the service;
- which runtime dependencies are required;
- which configuration must be supplied;
- how the service is reached over the network;
- how process readiness is currently determined; and
- which runtime values are stable integration interfaces.

External consumers must not rely on:

- source package layout;
- internal classes or method names;
- development-only placeholder credentials;
- generated build output paths other than the documented packaged JAR contract;
- undocumented profile-specific values; or
- Dockerfile or Kubernetes manifest details that conflict with this contract.

## Evolution

This document should evolve with the service. Any future change to startup,
configuration, dependency expectations, networking, health checks, or stable
runtime endpoints should update this contract in the same change set.
