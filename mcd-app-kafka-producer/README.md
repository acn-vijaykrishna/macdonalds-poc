## Maven Build & Package

```jsunicoderegexp
mvn clean package
mvn clean install
mvn spring-boot:run
mvn test
mvn clean install // to convert avro format to pojo classes
```

## AWS Configure

- Export AWS credentials into terminal

## Mac export command

export AWS_ACCESS_KEY_ID=<aws key from secrets.txt>
export AWS_SECRET_ACCESS_KEY=<aws secret access key from secrets.txt>

## Windows command

set AWS_ACCESS_KEY_ID=<aws key from secrets.txt>
set AWS_SECRET_ACCESS_KEY=<aws secret access key from secrets.txt>

## Kafka Producer App Deployed as Docker Image to EKS

### Build and Push Docker Image

Build, run & test the Docker image:

```jsunicoderegexp
docker build -t kafka-producer:latest .
docker images
docker run -p 8080:8080 kafka-producer:latest  
```

Push the Docker image to Docker Hub:

```jsunicoderegexp
docker push kafka-producer:latest
```

### Deploy to AWS EKS

- Authenticate Docker to ECR
- 
```jsunicoderegexp
docker login --username AWS --password-stdin 992382542338.dkr.ecr.us-east-1.amazonaws.com <<< "$(aws ecr get-login-password --region us-east-1)"
```
  
- Create the ECR repository (if needed)
```jsunicoderegexp
  aws ecr create-repository --repository-name kafka-producer --region us-east-1
```

- Tag the Docker image
```jsunicoderegexp
  docker tag kafka-producer:latest 992382542338.dkr.ecr.us-east-1.amazonaws.com/kafka-producer:latest
```
- Push the Docker image to ECR
```jsunicoderegexp
  docker push 992382542338.dkr.ecr.us-east-1.amazonaws.com/kafka-producer:latest
```
- If you get this error while pushing image, goto AWS ECR container and update permissions on that registry
  ```error parsing HTTP 403 response body: unexpected end of JSON input: ""```

### Apply the Kubernetes configuration for EKS:

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
### Working with ECS

Stopping Service and Tasks:

```jsunicoderegexp
aws ecs stop-task --cluster acn-mcd-ecs-app-cluster --task arn:aws:ecs:us-east-1:992382542338:task/acn-mcd-ecs-app-cluster/8dabbc88cc314f148e47318fbedf7ec2
aws ecs update-service --cluster acn-mcd-ecs-app-cluster --service kafkaProducerAppService --desired-count 0
```

### Install Kafka

```jsunicoderegexp
curl -sL --http1.1 https://cnfl.io/cli | sh -s -- latest // Install confluent
confluent login --save
confluent environment use env-r0vy79
confluent kafka cluster use lkc-k6y00p
kafka-topics --bootstrap-server pkc-921jm.us-east-2.aws.confluent.cloud:9092 --list | grep curated_loyalty_transactions
```


