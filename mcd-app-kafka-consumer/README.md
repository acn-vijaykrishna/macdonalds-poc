## Kafka Consumer App Deployed as Docker Image to EKS

### Build and Push Docker Image

Build the Docker image:

```jsunicoderegexp
docker build -t kafka-consumer:latest .
```

Push the Docker image to Docker Hub:
```jsunicoderegexp
docker push kafka-consumer:latest
```

### Deploy to AWS EKS

Apply the Kubernetes configuration:
```jsunicoderegexp
kubectl apply -f deployment.yaml
```

Verify the deployment and service:
```jsunicoderegexp
kubectl get deployments
kubectl get services
```