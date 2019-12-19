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

The web service must be configured for the MongoDB database deployment.  Edit the `src/main/webapp/WEB-INF/config/content-server.json` file to match the deployment.

```
{
   "host": "127.0.0.1",
   "port": "27017",
   "database": "content-server",
   "authen": {
      "user": "contentadmin",
      "password": "password",
      "database": "content-server"
   },
   "collections": {
      "content": {
         "put": {
            "comment": "create = true, PUT will create if not exist, else return not found",
            "create": false
         }
      }
   }
}
```

### Clean, Compile, Package, Install:

Run *Maven* (`mvn`) processes to clean, compile, package and install:

```
mvn clean compile package install
```

Packages are added to the user's home folder `.m2/repository`: 

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

## Configure

MongoDB needs to be configured for the **content** database / collection.

1. Access MongoDB system \
`ssh root@hostname`
1. Connect as "root" user to create database and collection \
`mongo --username "root" --password "<ROOT_PASSWORD>" --authenticationDatabase "admin" admin`
1. Specify the database name \
`> use content-server;`
1. Drop existing database \
`> db.dropDatabase();`
1. Drop existing admin user \
`> db.dropUser("contentadmin");`
1. Create admin user \
`> db.createUser({user:"contentadmin",pwd:"password",roles:["readWrite","dbAdmin"]});`
1. Create collection \
`> db.createCollection("content");`
1. Logout as the "root" user \
`> quit();`
1. Connect as the "contentadmin" user \
`mongo --username "contentadmin" --password "password" --authenticationDatabase "content-server" content-server`
1. Create index in the collection for the "uid" attribute \
`> db.content.createIndex({"uid":1});`
1. Insert sample record into the collection \
`> db.content.insert({"comment": "This is a test document"});`
1. Display the sample record \
`> db.content.find();` \
`> db.content.find().pretty();`
1. Logout \
`> quit();`

---

## Install

