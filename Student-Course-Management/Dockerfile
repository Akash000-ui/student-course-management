# Use Eclipse Temurin 17 as base image (official replacement for OpenJDK)
FROM eclipse-temurin:17-jdk-jammy

# Set working directory
WORKDIR /app

# Copy the Maven wrapper and pom.xml first (for better Docker layer caching)
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Create uploads directory
RUN mkdir -p uploads

# Expose port
EXPOSE 8080

# Set environment variables for production
ENV SPRING_PROFILES_ACTIVE=production
ENV FILE_UPLOAD_DIR=/app/uploads

# Run the jar file
CMD ["java", "-jar", "target/Student-Course-Management-0.0.1-SNAPSHOT.jar"]
