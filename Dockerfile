FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B verify
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 10001 clinic
COPY --from=build /workspace/target/smart-clinic-1.0.0.jar app.jar
USER 10001
ENV PORT=8086 SERVER_ADDRESS=0.0.0.0
EXPOSE 8086
ENTRYPOINT ["java","-jar","app.jar"]
