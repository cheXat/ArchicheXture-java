# ArchicheXture-java
## Introduction
Started as a security layer between frontend and backend, ArchicheXture grew to being a generic and easy REST framework for the JavaEE stack.
Having the frontend consume standardized services instead of the need to have full access to a production database creates an additional layer of security, as one only exposes those functions and data needed at that time.

This was made to separate frontend/UI layers from complex business logic and therefore be able to utilize the full JavaEE stack with all available libraries without having the need of developing the frontend in JSF or anything similarly painful. Powerful caching, optimized connections to various databases at the same time, as well, as distributed transactions are just a small selection of the benefits.
Integrating ArchiCheXture to a large project offers the ability to use the same server code for all frontends at the same time as ArchicheXture currently is available for php/Yii, Android and Swift. Any other future technology is welcome to the stack, as it just needs to adhere to the contracts.

## REST API
ArchiCheXture usually runs in a JavaEE environment (wildfly, glassfish, TomEE, etc), if done so, the API is exposed via JaxRS.
### Filtering data
To query data, one can simply append query parameter (URL encoded) to the GET URL. 
http://........../entity/?title=My%25
http://........../entity/?sub_entity=77

This works (currently) just in 1 dimension, that means, that in the upper example one cannot simply query for operated_by.title. Should use the corresponding controller for that query and use the ids.
“Or” criterias are just concatenated via “,”
http://........../entity/42?sub_entity=101,42,1337

By appending an ID to the URL, one can get a single entity.
http://........../entity/42

### Paging
Pagination can be achieved by using the known SQL keywords limit and offset just as query parameters.
### Inserting or Updating data
By executing POST on the URL with a json or urlencoded formdata, one can create or update an entity. 
POST: http://........../entity
{title:"My new title"}
POST: http://........../entity/42
{title:"My updated title"}

This currently only works 1 dimensional, that means, instead of a complete entity to update, one just provides the id and appends “_id” to the field name.
POST: http://........../entity/42
{operated_by_id:2}

### Example JSON
{
  "id": 42,
  "title": "My Entity",
  "my_number": 4711,
  "sub_entity": [{
    "id": 77,
    "title": "Sub"
  }]
}

