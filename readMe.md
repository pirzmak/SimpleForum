# Simple forum

Simple REST service to add posts/topics on forum.

## Getting Started

To build and start application you can use script file start.sh.  
To start application with initial values use argument -init. To initialize example values you need delete old DB tables 
with names TOPICS and POSTS.

```
./start.sh
./start_init.sh
```
or use sbt command run 
```
sbt "run"
sbt "run -init"
```

### Prerequisites

To build a project will be needed sbt. To download sbt on Linux :

```
echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
sudo apt-key adv --keyserver hkps://keyserver.ubuntu.com:443 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
sudo apt-get update
sudo apt-get install sbt
```

### Build

To build application use sbt script
```
./build.sh
```
or use sbt command build
```
sbt build
```

### Config 
Config file (application.conf) has information about server, database and validations. Config file format:
```
host = "localhost"
port = 8080

timeout = 60 //in sec

pagination = {
  maxLimit = 100
  default = 20
}

validations = {
    emailRegex = """(\w+)@([\w\.]+)"""
    postMinLength = 3
    postMaxLength = 400
    topicTitleMinLength = 3
    topicTitleMaxLength = 150
    nicknameMinLength = 3
    nicknameMaxLength = 30
}

forum_db = {
  driver = "slick.driver.PostgresDriver$"

  db = {
    url = "jdbc:postgresql://localhost/forum"
    driver = org.postgresql.Driver
    connectionPool = HikariCP
    user = user //add your user_name
    password = password //add your password
  }
}

h2mem1 = {
  driver = "slick.driver.H2Driver$"

  db = {
    url = "jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE"
    driver = org.h2.Driver
    connectionPool = disabled
    keepAliveConnection = true
    }
}
```

## REST API

#### GET: Getting topics list

```
localhost:8080/forum/topics?offset=<offset>&limit=<limit>
```
##### Parameters:
offset - number. Optional value.  
limit - number(max limit is stored in config file). Optional value.

##### Response: 
List of topics. Topic object format:

```
{
  id: {value: int} - topic id
  title: string - topic title
  text: string - topic text 
  creator: {
    nickName: string,
    email: string 
  } - creator info
  lastModified: <YYYY-mm-ddTHH:MM:SS> - time of last modification
}
```

##### Example 
```
curl -i -X GET 'localhost:8080/forum/topics?offset=0&limit=20'
```

#### GET: Getting posts list

```
localhost:8080/forum/topics/<topicId>/posts?postId=<post_id>&before=<before>&after=<after>
```
##### Parameters:
postId - number. Optional value.  
before - number(max limit is stored in config file). Optional value.  
after - number(max limit is stored in config file). Optional value.

##### Response: 
List of posts from topic. Post object format:
```
{
  id: {value: int} - post id
  topicId: {value: int} - topic id
  message: string - post text 
  creator: {
    nickName: string,
    email: string 
  } - creator info
  createdTime: <YYYY-mm-ddTHH:MM:SS> - time of create
  lastModification: <YYYY-mm-ddTHH:MM:SS> - time of last modification
}
```

##### Example 
```
curl -i -X GET 'localhost:8080/forum/topics/1/posts?postId=1&before=1&after=1'
```
#### POST: Create topic

```
localhost:8080/forum/topics
```
##### Body:
```
{
  title: string - topic title
  text: string - topic text 
  creator: {
    nickName: string,
    email: string 
  } - creator info
}
```
##### Response: 
Topic id:
```
{
  topicId: {value: int} - topic Id
}
```

##### Example 
```
curl -i -H "Content-Type: application/json" -X POST localhost:8080/forum/topics -d '{"title": "Topic title", "text": "Topic text", "creator": {"nickName": "Jon", "email": "jon@doe.com"}}'
```
#### POST: Create post

```
localhost:8080/forum/topics/<topicId>/posts
```
##### Body:
```
{
  message: string - post text 
  creator: {
    nickName: string,
    email: string 
  } - creator info
}
```
##### Response: 
Post secret:
```
{
  postSecret: {secret: string} - post secret
}
```

##### Example 
```
curl -i -H "Content-Type: application/json" -X POST localhost:8080/forum/topics/1/posts -d '{"message": "Post text", "creator": {"nickName": "Jon", "email": "jon@doe.com"}}'
```
#### PUT: Edit post

```
localhost:8080/forum/topics/<topicId>/posts/<post_secret>
```
##### Body:
```
{
  newMessage: string - post text 
}
```
##### Response: 
Post secret:
```
{
  postSecret: {secret: string} - post secret
}
```

##### Example 
```
curl -i -H "Content-Type: application/json" -X PUT localhost:8080/forum/topics/1/posts/7fffffff-ffff-ffff-ffff-ffff80000000 -d '{"message": "Post text"}'
```

#### DELETE: Delete post

```
localhost:8080/forum/topics/<topicId>/posts/<post_secret>
```

##### Response: 
Post secret:
```
{
  postSecret: {secret: string} - post secret
}
```

##### Example 
```
curl -i -H "Content-Type: application/json" -X Delete localhost:8080/forum/topics/1/posts/7fffffff-ffff-ffff-ffff-ffff80000000
```