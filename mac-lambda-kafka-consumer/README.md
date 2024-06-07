### Maven Build & Package
mvn clean package

### AWS Configure

- Export AWS credentials into terminal
- First time create Lambda
aws lambda create-function --function-name KafkaConsumerLambda \
--runtime java21 --role arn:aws:iam::992382542338:role/lambda_access_role \
--handler com.mac.KafkaLambdaHandler::handleRequest \
--zip-file fileb://target/mac-lambda-kafka-consumer-1.0-SNAPSHOT.jar

- Deploy
aws lambda update-function-code --function-name KafkaConsumerLambda --zip-file fileb://target/mac-lambda-kafka-consumer-1.0-SNAPSHOT.jar

## Mac export command
export AWS_ACCESS_KEY_ID=<aws key from secrets.txt>
export AWS_SECRET_ACCESS_KEY=<aws secret access key from secrets.txt>

## Windows command
set AWS_ACCESS_KEY_ID=<aws key from secrets.txt>
set AWS_SECRET_ACCESS_KEY=<aws secret access key from secrets.txt>
