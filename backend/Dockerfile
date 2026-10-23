# Build en dos etapas: compila con el JDK completo, corre con un JRE liviano.
# Las pruebas (Testcontainers, necesitan Docker-in-Docker) NO corren acá -- ya corren en
# GitHub Actions en cada push; esta imagen solo empaqueta lo que CI ya validó.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
RUN chmod +x gradlew

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Render/Railway inyectan PORT; application.properties ya lo lee (server.port=${PORT:8080}).
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
