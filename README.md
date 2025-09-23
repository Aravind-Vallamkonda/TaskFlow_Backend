# TaskFlow Backend

## Initial Setup
1. Install Java 17 and ensure a recent version of Maven is available (the included Maven Wrapper can be used instead).
2. Clone the repository and change into the project directory.
3. Provision a local PostgreSQL instance and create a database named `taskflowdb` (or update the connection details in `src/main/resources/application.properties` to match your environment).
4. Update the database username, password, and any other secrets in `src/main/resources/application.properties` so that they align with your local credentials.
5. From the project root, run `./mvnw clean install` to download dependencies and build the application for the first time.

## Run the Application
1. Ensure PostgreSQL is running and accessible with the credentials configured above.
2. Start the Spring Boot application with `./mvnw spring-boot:run`.
3. Once the startup logs indicate that the application is ready, access the API at `http://localhost:8080` or modify the server port in `application.properties` if needed.
4. To stop the application, press `Ctrl+C` in the terminal session running the Maven command.

## Build and Run via Executable JAR
1. Package the service with `./mvnw clean package`.
2. Run the generated artifact using `java -jar target/TaskFlow-0.0.1-SNAPSHOT.jar`.

## Entity Relationship Diagram

The core relationships between users, teams, projects, and tasks are documented in [docs/er-diagram.mmd](docs/er-diagram.mmd). The file uses Mermaid syntax and can be rendered by any Mermaid-compatible tool or IDE plugin.
