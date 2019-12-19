# frdp-content-server

ForgeRock Demonstration Platform : **Content Server** : A deployable web service that provides REST / JSON operations (CRUD) to manage JSON documents.  The service is implemented using the JAX-RS/Jersey REST API and MongoDB for document persistance.

`git clone https://github.com/ForgeRock/frdp-content-server.git`

---

## Requirements

The following items must be installed:

1. [Apache Maven](https://maven.apache.org/)
1. [Java Development Kit 8](https://openjdk.java.net/)

---

## Build

### Prerequisite:

The following items must be completed, in order:

1. [frdp-framework](https://github.com/ForgeRock/frdp-framework) ... clone / download then install using *Maven* (`mvn`)
1. [frdp-dao-mongo](https://github.com/ForgeRock/frdp-dao-mongo) ... clone / download then install using *Maven* (`mvn`)

### Edit configuration:

The web service must be configured for the MongoDB database deployment.  Edit the `src/main/webapp/WEB-INF/classes/content-server.properties` file to match the deployment.

### Clean, Compile, Install:

Run *Maven* (`mvn`) processes to clean, compile and install the package:

```
mvn clean compile package install
```

Packages are added to the user's home folder: 

```
find ~/.m2/repository/com/forgerock/frdp/frdp-content-server
/home/forgerock/.m2/repository/com/forgerock/frdp/frdp-content-server
/home/forgerock/.m2/repository/com/forgerock/frdp/frdp-content-server/1.0.0
/home/forgerock/.m2/repository/com/forgerock/frdp/frdp-content-server/1.0.0/frdp-content-server-1.0.0.war
/home/forgerock/.m2/repository/com/forgerock/frdp/frdp-content-server/1.0.0/frdp-content-server-1.0.0.pom
/home/forgerock/.m2/repository/com/forgerock/frdp/frdp-content-server/1.0.0/_remote.repositories
/home/forgerock/.m2/repository/com/forgerock/frdp/frdp-content-server/maven-metadata-local.xml
```

---

## Install

