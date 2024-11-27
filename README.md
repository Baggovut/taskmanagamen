# Task Management System Application
Test task.

### Project requirements
* [Java SE Development Kit 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
* [Apache Maven](https://maven.apache.org/download.cgi)
* [Git](https://git-scm.com/downloads)
* [Docker](https://www.docker.com/products/docker-desktop/)

### Security
#### Roles:
1) **Anonymous user** - non-authenticated user сan register and log in.
2) **User** - can change status and make comments to owned tasks and view any task.
3) **Admin** - can view, create, modify and delete tasks and make comments to any task.
4) **Root Admin** - сan grant Admin role to users.

### Starting locally
* Download, install and configure project tools.
* Execute `git clone https://github.com/Baggovut/taskmanagement.git` and wait for the project to download.
* Execute `mvn compile exec:java` in project root directory to run the application. Swagger UI would be available at 'http://localhost:8080/swagger-ui/index.html' while application is running.
* Execute `mvn test` in project root directory to run tests. Results would be in '/target/site/jacoco/index.html'