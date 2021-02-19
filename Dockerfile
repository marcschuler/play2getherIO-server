FROM maven:3-openjdk-15 as builder
COPY . .
RUN mvn package

FROM openjdk:15
ARG JAR_FILE=target/*.jar
COPY --from=builder ${JAR_FILE} play2gether-server.jar
EXPOSE 3101
ENTRYPOINT ["java","-jar","/play2gether-server.jar"]
