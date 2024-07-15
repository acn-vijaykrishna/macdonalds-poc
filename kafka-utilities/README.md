
## Kafka Utilities:

### Download Kafka from here

https://downloads.apache.org/kafka/

Extract the downloaded tar file & Navigate to the Kafka directory.

### Start Kafka and Zookeeper

Kafka requires Zookeeper to be running. The Kafka package includes a built-in Zookeeper instance for convenience.

```
bin/zookeeper-server-start.sh config/zookeeper.properties
```

Open a new terminal and navigate to the Kafka directory again. Start Kafka:

```
bin/kafka-server-start.sh config/server.properties
```

### script to run

```
kafka-console-producer --broker-list pkc-921jm.us-east-2.aws.confluent.cloud:9092 --topic Performance_raw_STLD --producer.config client.properties
```

## Python:

python -m venv venv
source venv/bin/activate
pip install confluent_kafka

python .\src\test_producer.py
