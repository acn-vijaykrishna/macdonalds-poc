## Maven Build & Package
mvn clean package

## AWS Configure

- Export AWS credentials into terminal
## Mac export command
export AWS_ACCESS_KEY_ID=<aws key from secrets.txt>
export AWS_SECRET_ACCESS_KEY=<aws secret access key from secrets.txt>

## Windows command
set AWS_ACCESS_KEY_ID=<aws key from secrets.txt>
set AWS_SECRET_ACCESS_KEY=<aws secret access key from secrets.txt>


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
