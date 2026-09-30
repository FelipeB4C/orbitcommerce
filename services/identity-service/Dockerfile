FROM gradle:9.7.1-jdk25 AS build

WORKDIR /workspace
RUN chown gradle:gradle /workspace
COPY --chown=gradle:gradle . .
USER gradle
RUN gradle --no-daemon bootJar

FROM eclipse-temurin:25-jre

WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
