### Maven Build & Package

mvn clean package

### AWS Configure

Run command and set detials or export as show below

```jsunicoderegexp
aws configure
```

- Export AWS credentials into terminal

- First time create Lambda
  aws lambda create-function --function-name KafkaConsumerLambda \
  --runtime java21 --role arn:aws:iam::992382542338:role/lambda_access_role \
  --handler com.mac.KafkaLambdaHandler::handleRequest \
  --zip-file fileb://target/mcd-lambda-kafka-consumer-1.0-SNAPSHOT.jar

- Deploy
```jsunicoderegexp
aws lambda update-function-code --function-name KafkaConsumerLambda --zip-file fileb://target/mcd-lambda-kafka-consumer-1.0-SNAPSHOT.jar --cli-connect-timeout 60000

```
  
## Mac export command

export AWS_ACCESS_KEY_ID=<aws key from secrets.txt>
export AWS_SECRET_ACCESS_KEY=<aws secret access key from secrets.txt>

## Windows command

set AWS_ACCESS_KEY_ID=<aws key from secrets.txt>
set AWS_SECRET_ACCESS_KEY=<aws secret access key from secrets.txt>