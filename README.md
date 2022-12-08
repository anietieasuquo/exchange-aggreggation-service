# Exchange Aggregation Service for TradeSoft

## Task Description
This is a simple service that integrates with crypto exchanges to get symbols and order books. It also stores and retrieves exchange metadata.

# Design decisions and constraints
- First of all, I've not used Kotlin for about a year now, so pardon the inelegance
- Most of the following design decisions are due to time-constraint, in a real world scenario some things would be done differently.
- In a real-world application, I would have implemented a Authentication/Authorization, like OAuth2 here. And I would have implemented it here but I'm short on time.
- I couldn't determine the possible rate of exchange metadata uploads as well as the metadata size for an exchange (something I would aks a Product Manager or someone from Demand side in a real world application). So I decided to implement the metadata upload asynchronously using Kafka to avoid parsing large CSV files in the synchronous flow. Of course this may not be a problem if we are sure the metadata CSV files would be relatively small.   
- Due to time constraints there isn't a 100% test coverage, but in a real application there would be.
- This seems like a service that would be more read-intensive than "write", so I decided to use a Redis cache for the metadata and metadata uploads.
- After creating a metadata upload to be processed later, I decided to return a 200 HTTP status instead of the standard 201. Because the actual resource (exchange metadata) has not been created yet, but rather would be created later. I appreciate that this may be an opinionated matter since a resource is still created (metadata upload), but not the resource the caller intended to create.
- Kafka has been set-up to use PLAINTEXT but of course in a real world application this would not be so.
- Due to time constraints I couldn't implement DB locking for the metadata entity
- Please note that all Resilience4J configs such as retries and timeouts are configurable in properties file using the config keys and handles I created
- Please see the test resources folder for a sample CSV file to know how to structure the CSV file expected by the service. And this would be what I would send to the Frontend dev in a real-world scenario.


The project is based on a small web service which uses the following technologies:

* Java 17
* Spring Boot 3.0
* Postgres for database (could also have been a No-SQL database like MongoDB given the simple use-case)
* Maven
* Redis cache (could also have been something small like Caffeine, but Redis is a lot easier to scale)
* JUnit Jupiter, Mockito for tests
* Docker, compose for deployment
* Kafka for messaging
* Swagger for documentation
* Liquibase for DB provisioning and future migrations

## How to run
The easiest way to run the application would be with the following;
### Please ensure you have Docker installed first

1. Run `./start` on the terminal (this small script will build, compose, and run the project)
2. The application will be run on port `8081` (`http://localhost:8081`)
3. Please navigate to the URL on your browser (http://localhost:8081), and you will see the SwaggerUI.
4. Click on "Exchange Controller" on the SwaggerUI to open the endpoints.
5. To run the tests, the good-ol `mvn test` will do.

### If for any reason the bash script does not work, you can run the application manually using the following commands;
`docker-compose up --build`

---

Thank you!
##### I would be happy to discuss the design decisions here in more detail.
