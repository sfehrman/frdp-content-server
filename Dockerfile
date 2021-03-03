# Dockerfile : Content Server
#
# This dockerfile has two stages:
# 1: maven, gets source code and compiles/packages into web app
# 2: tomcat, copies web app, adds config
#
# Commands:
# docker build -t frdp-content-server:1.2.0 .
# docker run --name content-server --rm -p 8080:8080 frdp-content-server:1.2.0
# docker exec -it content-server /bin/bash
# docker login -u USER -p PASSWORD
# docker tag frdp-content-server:1.2.0 USER/frdp-content-server:1.2.0
# docker push USER/frdp-content-server:1.2.0

# Get a container (maven) for compiling source code

FROM maven:3-openjdk-11 AS build

# Get the required projects from github

RUN git clone --branch 1.2.0 --progress --verbose https://github.com/ForgeRock/frdp-framework 
RUN git clone --branch 1.2.0 --progress --verbose https://github.com/ForgeRock/frdp-dao-mongo

# run maven (mvn) to compile jar files and package the war file

WORKDIR "/frdp-framework"
RUN mvn compile install

WORKDIR "/frdp-dao-mongo"
RUN mvn compile install

RUN mkdir /frdp-content-server
COPY . /frdp-content-server

WORKDIR "/frdp-content-server"
RUN mvn compile package 

# Get a container (tomcat) to run the application

FROM tomcat:9-jdk11-adoptopenjdk-hotspot

# Remove default applicatons

RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the expanded application folder

COPY --from=build /frdp-content-server/target/content-server /usr/local/tomcat/webapps/content-server

# EXPOSE 8085:8080
EXPOSE 8080

CMD ["catalina.sh", "run"]
