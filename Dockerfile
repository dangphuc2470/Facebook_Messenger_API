# Use a slim Maven image with temurin JDK
FROM maven:3-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src ./src

# Build the application with skipTests (optional, adjust as needed)
RUN mvn clean package -DskipTests

# Use a slim OpenJDK image for runtime
FROM eclipse-temurin:17-alpine AS runner

# Set working directory
WORKDIR /app

# Copy JAR file from build stage
COPY --from=build /target/*.jar app.jar

# Expose port for the application (adjust if needed)
EXPOSE 8080

# Set environment variable for Spring profile (optional)
ENV SPRING_PROFILES_ACTIVE=render

# Start the application using java command
ENTRYPOINT ["java", "-jar", "app.jar"]
