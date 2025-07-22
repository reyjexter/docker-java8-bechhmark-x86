FROM openjdk:8-jdk-slim

WORKDIR /app

COPY Benchmark.java .

RUN javac Benchmark.java

CMD ["java", "Benchmark"]
