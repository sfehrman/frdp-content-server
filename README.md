# frdp-content-server

ForgeRock Demonstration Platform : **Content Server** : A deployable web service that provides REST / JSON operations (CRUD) to manage JSON documents.  The service is implemented using the JAX-RS/Jersey REST API and MongoDB for document persistance.

`git clone https://github.com/ForgeRock/frdp-content-server.git`

# Disclaimer

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

# License

[MIT](/LICENSE)

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

```bash
mvn clean
mvn compile 
mvn package
```

The *package* process creates a deployable war file, in the current directory: `./target/content-server.war`: 

```bash
ls -la ./target
total 11888
drwxrwxr-x 5 forgerock forgerock       73 Dec 19 16:00 .
drwxrwxr-x 5 forgerock forgerock      118 Dec 19 15:59 ..
drwxrwxr-x 3 forgerock forgerock       16 Dec 19 15:59 classes
drwxrwxr-x 4 forgerock forgerock       51 Dec 19 16:00 content-server
-rw-rw-r-- 1 forgerock forgerock 12170899 Dec 19 16:00 content-server.war
drwxrwxr-x 2 forgerock forgerock       27 Dec 19 16:00 maven-archiver
```

# Configure MongoDB

MongoDB needs to be configured for the **content** database / collection.

1. Access MongoDB system \
\
`ssh root@hostname`

1. Connect as "root" MongoDB user to create database and collection \
\
`mongo --username "root" --password "<ROOT_PASSWORD>" --authenticationDatabase "admin" admin`

1. We need to do some database initialization ... 
Specify the database name: `content-server`.
Drop database if it already exists. 
Create an admin user, remove first, for the database: `contenteadmin`. 
Create one collection: `content`. Quit MongoDB. \
\
`use content-server;` \
`db.dropDatabase();` \
`db.dropUser("contentadmin");` \
`db.createUser({user:"contentadmin",pwd:"password",roles:["readWrite","dbAdmin"]});` \
`db.createCollection("content");` \
`quit();`
1. Connect as the "contentadmin" user for the `content-server` database.\
\
`mongo --username "contentadmin" --password "password" --authenticationDatabase "content-server" content-server`
1. Create index for the `content` collection.
Insert test document into the collection. 
Read the document from the collection. Quit MongoDB. \
\
`db.content.createIndex({"uid":1});` \
`db.content.insert({"comment": "This is a test document"});` \
`db.content.find();` \
`db.content.find().pretty();` \
`quit();`

# Install

This example deploys the `content-server.war` file to an Apache Tomcat 8.x environment.

## Deploy war file

Copy the `content-server.war` file to the `webapps` folder in the Tomcat server installation.  The running Tomcat server will automatically unpack the war file.

```bash
cp ./target/content-server.war TOMCAT_INSTALLATION/webapps
```

The deployed application needs to be configured for the MongoDB installation.  Edit the `content-server.json` file and change /check the values.

```bash
cd TOMCAT_INSTALLATION/webapps/content-server/WEB-INF/config
vi content-server.json
```

The default values:

```json
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

```bash
curl -v -X POST \
-H "Content-type: application/json" \
-d '{"firstname": "James","lastname": "Bond"}' \
http://FQDN/content-server/rest/content-server/content
```

### Response:

The JSON document is created in the MongoDB database / collect (`content-server/content`).  The new documents `uid` id returned in the HTTP Response Header `Location`

```bash
> POST /content-server/rest/content-server/content HTTP/1.1
> Content-type: application/json
> Content-Length: 41

< HTTP/1.1 201 
< Location: http://FQDN/content-server/rest/content-server/content/e538bad7-8066-46f7-8e8f-ac36185aa2dc
```

## SEARCH

Use `curl` to execute a HTTP GET method. The URL will end at the "collection" (`content`) level.  

### Request:

```bash
curl -v -X GET \
-H "Accept: application/json" \
http://FQDN/content-server/rest/content-server/content
```

### Response:

All of the JSON document unique identifiers will be returned.  The response will be a `application/json` object that contains:

- `quanity`: Attribute: number of returned document uids
- `results`: Array : document uids

```bash
> GET /charlie/content-server/rest/content-server/content HTTP/1.1
> Accept: application/json

< HTTP/1.1 200 
< Content-Type: application/json
< Content-Length: 65

{"quantity":1,"results":["e538bad7-8066-46f7-8e8f-ac36185aa2dc"]}
```

Formatted JSON output:

```json
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

```bash
curl -v -X GET \
-H "Accept: application/json" \
http://FQDN/content-server/rest/content-server/content/e538bad7-8066-46f7-8e8f-ac36185aa2dc
```

### Response:

The JSON document for the specified unique identifiers will be returned.  The response will be a `application/json` object that contains:

- `uid`: Attribute: MongoDB generated uid
- `data`: Object: document data
- `timestamps`: Object: date/time Attributes (like `created`)

```bash
> GET /content-server/rest/content-server/content/e538bad7-8066-46f7-8e8f-ac36185aa2dc HTTP/1.1
> Accept: application/json

< HTTP/1.1 200 
< Content-Type: application/json
< Content-Length: 149

{"uid":"e538bad7-8066-46f7-8e8f-ac36185aa2dc","data":{"firstname":"James","lastname":"Bond"},"timestamps":{"created":"2019-12-19T16:30:39.162-0600"}}
```

Formatted JSON output:

```json
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

```bash
curl -v -X PUT \
-H "Content-type: application/json" \
-d '{"firstname": "James","lastname": "Bond","title": "Secret Agent","org": "MI6","weapon": "Walther PPK"}' \
http://FQDN/content-server/rest/content-server/content/e538bad7-8066-46f7-8e8f-ac36185aa2dc
```

### Response:

```bash
> PUT /content-server/rest/content-server/content/e538bad7-8066-46f7-8e8f-ac36185aa2dc HTTP/1.1
> Content-type: application/json
> Content-Length: 102

< HTTP/1.1 204 
```

Read the document after the data is replaced:

```json
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

```bash
curl -v -X DELETE \
http://FQDN/content-server/rest/content-server/content/e538bad7-8066-46f7-8e8f-ac36185aa2dc
```

### Response:

```bash
> DELETE /charlie/content-server/rest/content-server/content/e538bad7-8066-46f7-8e8f-ac36185aa2dc HTTP/1.1

< HTTP/1.1 204 
```

# Verify

Connect to the MongoDB to verify the stored JSON document:

```bash
mongo --username "contentadmin" --password "password" --authenticationDatabase "content-server" content-server
```

Issue command to *find* all the documents in the `content` collection:

```bash
db.content.find().pretty();
```

Output:

```json
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

```bash
quit();
```
