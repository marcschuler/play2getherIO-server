FROM openjdk:12
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} play2gether-server.jar
EXPOSE 3101
ENTRYPOINT ["java","-jar","/play2gether-server.jar"]
