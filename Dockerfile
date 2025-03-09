FROM openjdk:17-jdk-slim

WORKDIR /app

COPY . .

RUN ./gradlew build -x test

CMD ["java", "-jar", "build/libs/migration-tool.jar"]