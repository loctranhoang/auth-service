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
- Java 21 or later
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

## Local Kubernetes Workflow

The supported local Kubernetes platform for this project is a single-node kind cluster with ingress-nginx and cert-manager. The workflow uses the same manifests that are intended for remote Kubernetes environments; local image loading and host resolution happen outside the manifests.

### Local Platform Prerequisites

- Docker
- kind
- kubectl

### Provision the Local Platform

Create a kind cluster that exposes HTTP and HTTPS from the ingress controller to the local machine:

```powershell
@"
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
  - role: control-plane
    extraPortMappings:
      - containerPort: 80
        hostPort: 80
        protocol: TCP
      - containerPort: 443
        hostPort: 443
        protocol: TCP
"@ | kind create cluster --name auth-service-local --config=-
```

Install ingress-nginx for kind and wait for the controller:

```powershell
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.11.3/deploy/static/provider/kind/deploy.yaml
kubectl wait --namespace ingress-nginx `
  --for=condition=Ready pod `
  --selector=app.kubernetes.io/component=controller `
  --timeout=120s
```

Install cert-manager for local certificate issuance and wait for it to become available:

```powershell
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.16.2/cert-manager.yaml
kubectl wait --namespace cert-manager `
  --for=condition=Ready pod `
  --selector=app.kubernetes.io/instance=cert-manager `
  --timeout=120s
```

### Build and Load a Local Image

Build the application JAR, build the container image using the same image reference used by the Deployment, and load it into kind:

```powershell
mvn -B -DskipTests package
docker build -t ghcr.io/loctranhoang/auth-service:latest .
kind load docker-image ghcr.io/loctranhoang/auth-service:latest --name auth-service-local
```

The Deployment uses `imagePullPolicy: IfNotPresent`, so a locally loaded image is used in kind while remote clusters can still pull the same image reference from GHCR when the image is not present locally.

### Deploy and Validate

Apply the Kubernetes resources:

```powershell
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/service-account.yaml
kubectl apply -f k8s/image-pull-secret.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/certificates.yaml
kubectl apply -f k8s/ingress.yaml
```

Wait for the application Pod and local TLS certificate:

```powershell
kubectl wait --namespace auth-service `
  --for=condition=Ready pod `
  --selector=app.kubernetes.io/name=auth-service,app.kubernetes.io/component=api `
  --timeout=120s

kubectl wait --namespace auth-service `
  --for=condition=Ready certificate/auth-service-tls `
  --timeout=120s
```

Check the deployed resources:

```powershell
kubectl get deployment,service,ingress,certificate --namespace auth-service
```

Validate ingress routing through the local ingress controller without modifying the manifest host:

```powershell
curl.exe -k --resolve auth-service.example.com:443:127.0.0.1 https://auth-service.example.com/
```

An HTTP response from the Spring Boot service confirms that ingress routing reached the application. A non-2xx application response still confirms routing when no endpoint is implemented for the requested path.

### Update the Local Deployment

After code changes, rebuild and reload the image, then restart the Deployment so kind uses the refreshed local image:

```powershell
mvn -B -DskipTests package
docker build -t ghcr.io/loctranhoang/auth-service:latest .
kind load docker-image ghcr.io/loctranhoang/auth-service:latest --name auth-service-local
kubectl rollout restart deployment/auth-service --namespace auth-service
kubectl rollout status deployment/auth-service --namespace auth-service --timeout=120s
```

### Remove Local Resources

Remove the project resources from the local cluster:

```powershell
kubectl delete -f k8s/ --ignore-not-found
```

Remove the local platform when it is no longer needed:

```powershell
kind delete cluster --name auth-service-local
```

### Portability Notes

- The shared Kubernetes manifests are not edited for local validation.
- The local workflow keeps host resolution outside the manifests by using `curl --resolve` or an equivalent local hosts entry.
- The local workflow keeps image selection outside the manifests by loading the same image reference into kind.
- `k8s/certificates.yaml` uses cert-manager resources to create the `auth-service-tls` secret consumed by the Ingress. Remote environments may use an equivalent cert-manager issuer or external certificate process that writes the same TLS secret name.

## Contribution Guidelines

We welcome contributions! Please open issues or pull requests as needed. See `CONTRIBUTING.md` if available, or contact the maintainers for guidance.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
