# Peerlink-REST-API

This is a Q&A Springboot REST server that simply allows students to ask questions from other students, they can ask questions using images, 1 min video and text they can also upload documents and discuss those documents  

#
its just QA rest app made using 
spring boot 
+ java
 + kotlin 
 + me 
 + idea 
 + mongo db 
 + gridfs 
 + google 
 + stackoverflow 
 + cool stuff 
 + the usual
 + elastic search

 #Really Pressing Issues

 we have optimise data base queries use the Criteria class like:
 ` Criteria criteria = Criteria.where("mappings.provider.name").is(provider.getName());
        return mongoTemplate.find(Query.query(criteria), Doc1.class);`

for mongo and similarly for elastic search

currently we are using a procedural method namely using a for loop because at first I expect the question NOT to hae many answers looks at the `AnswersController` under controllers package

