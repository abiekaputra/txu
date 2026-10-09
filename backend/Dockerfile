FROM gradle:9.7.1-jdk21-alpine AS build
WORKDIR /workspace
COPY . .
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S txu && adduser -S txu -G txu
WORKDIR /app
COPY --from=build /workspace/build/libs/*-SNAPSHOT.jar app.jar
USER txu
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

