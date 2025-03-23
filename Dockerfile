FROM openjdk:17-jdk-slim

WORKDIR /app

COPY . .

RUN ./gradlew clean build -x test

CMD ["./gradlew", "run", "--args=migrate"]
