FROM registry.web3tech.ru/library/openjdk17:17-jdk-alpine-a67829bf
MAINTAINER Waves Enterprise <>

ENV JAVA_OPTS="-Xmx256M"

ADD *.jar app.jar

RUN eval $SET_ENV_CMD
CMD ["/bin/sh", "-c", "eval ${SET_ENV_CMD} ; /usr/bin/java -jar app.jar"]