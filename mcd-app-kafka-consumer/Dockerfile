# Use the official Maven image as the build environment
FROM maven:3.8.4-openjdk-17-slim AS builder

# Set the working directory in the container
WORKDIR /app

# Copy the project files to the working directory
COPY . .

# Build the project
RUN mvn clean package -DskipTests

# Check if the JAR file is created
RUN ls -l /app/target/

# Use the official OpenJDK image for a minimal footprint deployment
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/mcd-app-kafka-consumer-1.0.0.jar /app/kafka-consumer.jar

# Check if the JAR file is copied correctly
RUN ls -l /app/

# Expose port 8080
EXPOSE 8080

# Command to run the JAR file
ENTRYPOINT ["java", "-jar", "kafka-consumer.jar"]
