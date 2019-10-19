FROM openjdk:8-jdk-alpine
MAINTAINER dubedivine@gmail.com
VOLUME /tmp
ARG JAR_FILE=target/networks_server-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]