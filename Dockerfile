# Stage 1: Build stage
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the jar package skipping tests
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the compiled JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose backend port
EXPOSE 8080

# Create upload directory
RUN mkdir -p uploads

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]
