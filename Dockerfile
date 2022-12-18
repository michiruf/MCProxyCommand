FROM openjdk:17-slim
WORKDIR /app
ADD . /app
RUN ./gradlew :build
