# vigilant-eureka

warning... you are about to read true nonsense!

this is a dope rest app  for the world!!!, sort of like stackover 
flow but better in my head...,seriously...

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

