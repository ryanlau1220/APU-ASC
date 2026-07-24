# Build Stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml and source files
COPY apps/backend/pom.xml ./apps/backend/
COPY apps/backend/src ./apps/backend/src

# Build executable fat JAR
WORKDIR /app/apps/backend
RUN mvn clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy fat JAR from build stage
COPY --from=build /app/apps/backend/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
