FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew

RUN ./gradlew build --no-daemon -x test

COPY . .

RUN ./gradlew bootJar --no-daemon -x test

EXPOSE 8080

CMD ["java", "-jar", "build/libs/Arrowhead-backend-0.0.1-SNAPSHOT.jar"]
