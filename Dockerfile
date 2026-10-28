# Build multi-stage: o JDK e o Maven ficam só no estágio de build; a imagem final leva apenas o JRE + jar.
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
# mvnw pode vir com CRLF de um checkout no Windows
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src src
# os testes rodam no pipeline de CI (mvnw verify, com o gate de 100% do Jacoco) antes da imagem
RUN ./mvnw -B -q package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 1001 spring
COPY --from=build /app/target/*.jar app.jar
USER spring
EXPOSE 8080
# liveness/readiness: /actuator/health/liveness e /actuator/health/readiness (probes do Kubernetes)
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
