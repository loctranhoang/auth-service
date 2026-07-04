# Auth Service

A Spring Boot-based authentication service for managing authorization and user authentication within your application ecosystem.

## Project Overview

The Auth Service provides essential authentication and authorization functionalities for microservice architectures.

- **Language:** Java
- **Framework:** Spring Boot
- **Repository:** `auth-service`

## Features

- User registration and login endpoints
- JWT-based authentication
- Secure password handling
- Role-based access control
- Extensible design for integration with other services

## Getting Started

### Prerequisites
- Java 17 or later
- Maven 3.8.x or later

### Building the Project

Clone the repository and build using Maven:

```sh
git clone https://github.com/your-org/auth-service.git
cd auth-service
mvn clean install
```

### Running the Service

To start the service locally:

```sh
mvn spring-boot:run
```

Or build a JAR and run it:

```sh
mvn package
java -jar target/auth-service-*.jar
```

By default, the service will be available at `http://localhost:8080`.

### Local Platform-Infra Environment

Use `.env.example` as the safe local template for running Auth Service against the local Platform-Infra PostgreSQL and Keycloak services. The example uses the `local` Spring profile, points the database URL at the Platform-Infra PostgreSQL host port, and points Keycloak at `http://localhost:8080` with realm `az`.

Copy the example and replace each `PLACEHOLDER_*` value with local-only credentials or secrets:

```powershell
Copy-Item .env.example .env
```

Keep `.env` uncommitted. Export the variables from `.env` in your shell or IDE before starting the service. For PowerShell:

```powershell
Get-Content .env |
  Where-Object { $_ -and $_ -notmatch '^\s*#' } |
  ForEach-Object {
    $name, $value = $_ -split '=', 2
    Set-Item -Path "Env:$name" -Value $value
  }

mvn spring-boot:run
```

The example sets `SERVER_PORT=8081` so the service can run locally while Platform-Infra Keycloak uses `http://localhost:8080`.

## Usage Example

Typical authentication request (example endpoint):

```http
POST /api/auth/login
Content-Type: application/json
{
  "username": "youruser",
  "password": "yourpassword"
}
```

A successful response will include a JWT token for use in subsequent requests.

## Testing

Run all tests using Maven:

```sh
mvn test
```

## Configuration

All environment and profile-specific configuration files are located under `src/main/resources` (e.g., `application.yml`).
You can override configuration by creating profile-specific files (e.g., `application-dev.yml`).

## Local Kubernetes Platform

The supported local Kubernetes platform for this service is a single-node kind cluster with ingress-nginx and cert-manager. The application manifests stay distribution-neutral: `k8s/deployment.yaml`, `k8s/service.yaml`, and `k8s/ingress.yaml` do not contain kind-specific objects, while `k8s/certificates.yaml` provides the local cert-manager issuer and certificate needed for TLS validation.

Prerequisites:

- Docker
- kind
- kubectl
- Maven

Create the local cluster with HTTP and HTTPS routed from the host into the kind control-plane node:

```powershell
$kindConfig = Join-Path $env:TEMP "auth-service-kind.yaml"

@"
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
  - role: control-plane
    kubeadmConfigPatches:
      - |
        kind: InitConfiguration
        nodeRegistration:
          kubeletExtraArgs:
            node-labels: "ingress-ready=true"
    extraPortMappings:
      - containerPort: 80
        hostPort: 80
        protocol: TCP
      - containerPort: 443
        hostPort: 443
        protocol: TCP
"@ | Set-Content -Path $kindConfig -Encoding utf8

kind create cluster --name auth-service --config $kindConfig
kubectl cluster-info --context kind-auth-service
```

Install the platform add-ons:

```powershell
$INGRESS_NGINX_VERSION = "controller-v1.15.1"
$CERT_MANAGER_VERSION = "v1.20.2"

kubectl apply -f "https://raw.githubusercontent.com/kubernetes/ingress-nginx/$INGRESS_NGINX_VERSION/deploy/static/provider/kind/deploy.yaml"
kubectl wait --namespace ingress-nginx --for=condition=ready pod --selector=app.kubernetes.io/component=controller --timeout=180s

kubectl apply -f "https://github.com/cert-manager/cert-manager/releases/download/$CERT_MANAGER_VERSION/cert-manager.yaml"
kubectl wait --namespace cert-manager --for=condition=Available deployment/cert-manager --timeout=180s
kubectl wait --namespace cert-manager --for=condition=Available deployment/cert-manager-cainjector --timeout=180s
kubectl wait --namespace cert-manager --for=condition=Available deployment/cert-manager-webhook --timeout=180s
```

Map the ingress host to the local machine. On Windows this requires an elevated shell:

```powershell
Add-Content -Path "$env:SystemRoot\System32\drivers\etc\hosts" -Value "`n127.0.0.1 auth-service.example.com"
```

Build and load a local image without changing the committed deployment manifest:

```powershell
mvn -DskipTests package
docker build -t ghcr.io/loctranhoang/auth-service:local .
kind load docker-image ghcr.io/loctranhoang/auth-service:local --name auth-service
```

Deploy the service resources:

```powershell
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/service-account.yaml
kubectl apply -f k8s/image-pull-secret.yaml
kubectl apply -f k8s/certificates.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml
kubectl set image -f k8s/deployment.yaml auth-service=ghcr.io/loctranhoang/auth-service:local --local -o yaml | kubectl apply -f -

kubectl -n auth-service wait certificate/auth-service-tls --for=condition=Ready --timeout=180s
kubectl -n auth-service rollout status deployment/auth-service --timeout=180s
kubectl -n auth-service get ingress,service,deployment,certificate
```

Verify ingress routing through TLS:

```powershell
curl.exe -k -i https://auth-service.example.com/
```

The current application has no root controller, so an HTTP response from Spring Boot, including a 404 for `/`, confirms that ingress reached the service. Connection failures, certificate secret errors, or an ingress controller default-backend response indicate a platform or routing issue.

Update the local deployment by building and loading a new tag, then updating only the live Kubernetes deployment:

```powershell
mvn -DskipTests package
docker build -t ghcr.io/loctranhoang/auth-service:local-2 .
kind load docker-image ghcr.io/loctranhoang/auth-service:local-2 --name auth-service
kubectl -n auth-service set image deployment/auth-service auth-service=ghcr.io/loctranhoang/auth-service:local-2
kubectl -n auth-service rollout status deployment/auth-service --timeout=180s
```

Remove the application resources:

```powershell
kubectl delete -f k8s/ingress.yaml --ignore-not-found
kubectl delete -f k8s/service.yaml --ignore-not-found
kubectl delete -f k8s/deployment.yaml --ignore-not-found
kubectl delete -f k8s/certificates.yaml --ignore-not-found
kubectl delete -f k8s/image-pull-secret.yaml --ignore-not-found
kubectl delete -f k8s/service-account.yaml --ignore-not-found
kubectl delete -f k8s/namespace.yaml --ignore-not-found
```

Remove the local platform when it is no longer needed:

```powershell
kind delete cluster --name auth-service
```

For remote Kubernetes environments, keep the same application manifests and provide environment-specific infrastructure outside the base resources: an ingress controller for the `nginx` ingress class, a TLS secret named `auth-service-tls`, registry credentials for GHCR, and any DNS records required by the target cluster. Do not commit local image tags or local host mappings to the base manifests.

## Kubernetes GHCR Image Pulls

The Kubernetes service account in `k8s/service-account.yaml` references the `ghcr-auth-service-pull` image pull secret. Create the secret in the `auth-service` namespace before deploying manifests that pull `ghcr.io/loctranhoang/auth-service`.

Use a GitHub token with `read:packages` permission, and do not commit the token or generated secret data:

```sh
kubectl create secret docker-registry ghcr-auth-service-pull \
  --namespace auth-service \
  --docker-server=ghcr.io \
  --docker-username=<github-username> \
  --docker-password=<github-token> \
  --docker-email=<email-address> \
  --dry-run=client \
  -o yaml | kubectl apply -f -
```

The committed `k8s/image-pull-secret.yaml` file is a credentials-free template for manifest validation only. Replace it at deploy time with a real cluster secret created by the command above or by your external secret-management process.

## Contribution Guidelines

We welcome contributions! Please open issues or pull requests as needed. See `CONTRIBUTING.md` if available, or contact the maintainers for guidance.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
