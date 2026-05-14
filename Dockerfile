FROM gradle:8.11-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean buildFatJar
RUN gradle buildFatJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*-all.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]