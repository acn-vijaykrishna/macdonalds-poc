# Use the official maven/Java image to create a build artifact.
# Use the latest LTS version of OpenJDK as the base image
FROM maven:3.9.2-openjdk-21-slim as builder
# Set the working directory in the container
WORKDIR /app
# Copy the project files to the working directory
COPY . .
# Build the project
RUN mvn clean package -DskipTests

# Use the official OpenJDK image for a minimal footprint deployment
FROM openjdk:21-jdk-slim
# Set the working directory in the container
WORKDIR /app
# Copy the JAR file from the builder stage
COPY --from=builder /app/target/kafka-consumer-1.0.0.jar /app/kafka-consumer.jar
# Expose port 8080
EXPOSE 8080
# Command to run the JAR file
ENTRYPOINT ["java", "-jar", "kafka-consumer.jar"]
