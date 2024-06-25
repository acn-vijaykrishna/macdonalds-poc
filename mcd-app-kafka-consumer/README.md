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

- Authenticate Docker to ECR
  aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin
  992382542338.dkr.ecr.us-east-1.amazonaws.com

- Create the ECR repository (if needed)
  aws ecr create-repository --repository-name kafka-consumer --region us-east-1

- Tag the Docker image
  docker tag kafka-consumer:latest 992382542338.dkr.ecr.us-east-1.amazonaws.com/kafka-consumer:latest

- Push the Docker image to ECR
  docker push 992382542338.dkr.ecr.us-east-1.amazonaws.com/kafka-consumer:latest

- If you get this error while pushing image, goto AWS ECR container and update permissions on that registry
  ```error parsing HTTP 403 response body: unexpected end of JSON input: ""```

### Apply the Kubernetes configuration:

```jsunicoderegexp
aws sts get-caller-identity
aws eks update-kubeconfig --name mcdonalds-eks --region us-east-1
aws eks update-kubeconfig --name mcdonalds-eks --region us-east-1 --role-arn arn:aws:iam::992382542338:role/mcdonalds-eks-role
  aws eks update-kubeconfig --region us-east-1 --name mcdonalds-eks --role-arn arn:aws:iam::992382542338:role/mcdonalds-eks-role

kubectl get nodes
kubectl get pods --all-namespaces

kubectl config view
kubectl apply -f deployment.yaml
kubectl apply -f role-deployment-manager.yaml

kubectl get svc 
```

Verify the deployment and service:

```jsunicoderegexp
kubectl get deployments
kubectl get services
```

Toubleshoot steps

```jsunicoderegexp

kubectl config current-context //  current contecxt
kubectl config view -o jsonpath="{.contexts[?(@.name == 'arn:aws:eks:us-east-1:992382542338:cluster/mcdonalds-eks')].context}"
kubectl config view -o jsonpath="{.users[?(@.name == 'arn:aws:eks:us-east-1:992382542338:cluster/mcdonalds-eks')].user}"
kubectl auth can-i get deployments --namespace=default
```
