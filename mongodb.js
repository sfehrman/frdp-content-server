//
// Login as the default administrator using the default authentication database
// mongo --username "root" --password "password" --authenticationDatabase "admin" admin < mongodb.js
//
use content-server;
db.dropDatabase();
db.dropUser("contentadmin");
db.createUser({user:"contentadmin",pwd:"password",roles:["readWrite","dbAdmin"]});
db.createCollection("content");
db.content.createIndex({"uid":1});
db.content.insert({"comment": "This is a test document"});
//
// Login as the administrator for the application database
// mongo --username "contentadmin" --password "password" --authenticationDatabase "content-server" content-server
//
// show the available databases
show dbs;
// show the collections in the current database
show collections;
// find all the documents in the collection
db.content.find();
db.content.find().pretty();
