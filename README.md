# frdp-content-server

ForgeRock Demonstration Platform : **Content Server** : A deployable web service that provides REST / JSON operations (CRUD) to manage JSON documents.  The service is implemented using the JAX-RS/Jersey REST API and MongoDB for document persistance.

`git clone https://github.com/ForgeRock/frdp-content-server.git`

# Requirements

The following items must be installed:

1. [Apache Maven](https://maven.apache.org/) *(tested with 3.5.x, 3.6.x)*
1. [Java Development Kit 8](https://openjdk.java.net/)
1. [MongoDB](https://www.mongodb.com) *(tested with 3.2)*
1. [Apache Tomcat](https://tomcat.apache.org/index.html) *(tested with Tomcat 8.5.x)*

# Build

## Prerequisite:

The following items must be completed, in order:

1. [frdp-framework](https://github.com/ForgeRock/frdp-framework) ... clone / download then install using *Maven* (`mvn`)
1. [frdp-dao-mongo](https://github.com/ForgeRock/frdp-dao-mongo) ... clone / download then install using *Maven* (`mvn`)

## Clean, Compile, Package:

Run *Maven* (`mvn`) processes to clean, compile, package:

```
mvn clean
mvn compile 
mvn package
```

The *package* process creates a deployable war file, in the current directory: `./target/content-server.war`: 

```
ls -la ./target
total 11888
drwxrwxr-x 5 forgerock forgerock       73 Dec 19 16:00 .
drwxrwxr-x 5 forgerock forgerock      118 Dec 19 15:59 ..
drwxrwxr-x 3 forgerock forgerock       16 Dec 19 15:59 classes
drwxrwxr-x 4 forgerock forgerock       51 Dec 19 16:00 content-server
-rw-rw-r-- 1 forgerock forgerock 12170899 Dec 19 16:00 content-server.war
drwxrwxr-x 2 forgerock forgerock       27 Dec 19 16:00 maven-archiver
```

# Configure

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

# Install

This example deploys the `content-server.war` file to an Apache Tomcat 8.x environment.

## Deploy war file

Copy the `content-server.war` file to the `webapps` folder in the Tomcat server installation.  The running Tomcat server will automatically unpack the war file.

```
cp ./target/content.war TOMCAT_INSTALLATION/webapps
```

The deployed application needs to be configured for the MongoDB installation.  Edit the `content-server.json` file and change /check the values.

```
cd TOMCAT_INSTALLATION/webapps/content-server/WEB-INF/config
vi content-server.json
```

The default values:

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

# Test

## CREATE

Use `curl` to execute a HTTP POST method.  The URL will end at the "collection" (`content`) level. 

### Request:

```
curl -v -X POST \
-H "Content-type: application/json" \
-d '{"firstname": "James","lastname": "Bond"}' \
https://FQDN/tomcat/content-server/rest/content-server/content
```

### Response:

The JSON document is created in the MongoDB database / collect (`content-server/content`).  The new documents `uid` id returned in the HTTP Response Header `Location`

```
> POST /tomcat/content-server/rest/content-server/content HTTP/1.1
> Content-type: application/json
> Content-Length: 41

< HTTP/1.1 201 
< Location: https://FQDN/tomcat/content-server/rest/content-server/content/e538bad7-8066-46f7-8e8f-ac36185aa2dc
```

## SEARCH

Use `curl` to execute a HTTP GET method. The URL will end at the "collection" (`content`) level.  

### Request:

```
curl -v -X GET \
-H "Accept: application/json" \
https://FQDN/tomcat/content-server/rest/content-server/content
```

### Response:

All of the JSON document unique identifiers will be returned.  The response will be a `application/json` object that contains:

- `quanity`: Attribute: number of returned document uids
- `results`: Array : document uids

```
> GET /tomcat/charlie/content-server/rest/content-server/content HTTP/1.1
> Accept: application/json

< HTTP/1.1 200 
< Content-Type: application/json
< Content-Length: 65

{"quantity":1,"results":["e538bad7-8066-46f7-8e8f-ac36185aa2dc"]}
```

Formatted JSON output:

```
{
   "quantity":1,
   "results":[
      "e538bad7-8066-46f7-8e8f-ac36185aa2dc"
   ]
}
```

## READ

Use `curl` to execute a HTTP GET method. The URL will end at the "collection" (`content`) plus the uid value.

### Request:

```
curl -v -X GET \
-H "Accept: application/json" \
https://FQDN/tomcat/content-server/rest/content-server/content/e538bad7-8066-46f7-8e8f-ac36185aa2dc
```

### Response:

The JSON document for the specified unique identifiers will be returned.  The response will be a `application/json` object that contains:

- `uid`: Attribute: MongoDB generated uid
- `data`: Object: document data
- `timestamps`: Object: date/time Attributes (like `created`)

```
> GET /tomcat/content-server/rest/content-server/content/e538bad7-8066-46f7-8e8f-ac36185aa2dc HTTP/1.1
> Accept: application/json

< HTTP/1.1 200 
< Content-Type: application/json
< Content-Length: 149

{"uid":"e538bad7-8066-46f7-8e8f-ac36185aa2dc","data":{"firstname":"James","lastname":"Bond"},"timestamps":{"created":"2019-12-19T16:30:39.162-0600"}}
```

Formatted JSON output:

```
{
   "uid":"e538bad7-8066-46f7-8e8f-ac36185aa2dc",
   "data":{
      "firstname":"James",
      "lastname":"Bond"
   },
   "timestamps":{
      "created":"2019-12-19T16:30:39.162-0600"
   }
}
```

## REPLACE

Use `curl` to execute a HTTP PUT method. The URL will end at the "collection" (`content`) plus the uid value.  The *data* will be replaced.  On success, a 204 (No Content) is returned.

### Request:

```
curl -v -X PUT \
-H "Content-type: application/json" \
-d '{"firstname": "James","lastname": "Bond","title": "Secret Agent","org": "MI6","weapon": "Walther PPK"}' \
https://FQDN/tomcat/content-server/rest/content-server/content/e538bad7-8066-46f7-8e8f-ac36185aa2dc
```

### Response:

```
> PUT /tomcat/content-server/rest/content-server/content/e538bad7-8066-46f7-8e8f-ac36185aa2dc HTTP/1.1
> Content-type: application/json
> Content-Length: 102

< HTTP/1.1 204 
```

Read the document after the data is replaced:

```
{
  "uid": "e538bad7-8066-46f7-8e8f-ac36185aa2dc",
  "data": {
    "weapon": "Walther PPK",
    "firstname": "James",
    "org": "MI6",
    "title": "Secret Agent",
    "lastname": "Bond"
  },
  "timestamps": {
    "created": "2019-12-19T16:30:39.162-0600",
    "updated": "2019-12-19T17:20:41.941-0600"
  }
}
```

## DELETE

Use `curl` to execute a HTTP DELETE method. The URL will end at the "collection" (`content`) plus the uid value.  On success, a 204 (No Content) is returned.

### Request:

```
curl -v -X DELETE \
https://FQDN/tomcat/content-server/rest/content-server/content/e538bad7-8066-46f7-8e8f-ac36185aa2dc
```

### Response:

```
> DELETE /tomcat/charlie/content-server/rest/content-server/content/e538bad7-8066-46f7-8e8f-ac36185aa2dc HTTP/1.1

< HTTP/1.1 204 
```

# Verify

Connect to the MongoDB to verify the stored JSON document:

```
mongo --username "contentadmin" --password "password" --authenticationDatabase "content-server" content-server
```

Issue command to *find* all the documents in the `content` collection:

```
db.content.find().pretty();
```

Output:

```
{
	"_id" : ObjectId("5dfc0836943883b66728995c"),
	"data" : {
      "weapon": "Walther PPK",
      "firstname": "James",
      "org": "MI6",
      "title": "Secret Agent",
      "lastname": "Bond"
	},
	"uid" : "e538bad7-8066-46f7-8e8f-ac36185aa2dc",
	"timestamps" : {
      "created" : "2019-12-19T17:31:02.336-0600",
      "updated" : "2019-12-19T17:20:41.941-0600"
	}
}
```

Logout:

```
quit();
```