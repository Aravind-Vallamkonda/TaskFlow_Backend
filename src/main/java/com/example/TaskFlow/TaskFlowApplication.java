package com.example.TaskFlow;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
                info = @Info(
                                title = "TaskFlow REST API",
                                version = "v1",
                                description = "API specification for the TaskFlow platform covering authentication, user management and token lifecycle operations.",
                                contact = @Contact(name = "TaskFlow Platform Team", email = "support@taskflow.example", url = "https://taskflow.example"),
                                license = @License(name = "Apache License 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0"),
                                termsOfService = "https://taskflow.example/terms"
                ),
                servers = {
                                @Server(url = "http://localhost:8080", description = "Local development server")
                }
)
@SecurityScheme(
                name = "bearerAuth",
                type = SecuritySchemeType.HTTP,
                bearerFormat = "JWT",
                scheme = "bearer"
)
@SpringBootApplication
public class TaskFlowApplication {

        public static void main(String[] args) {
                SpringApplication.run(TaskFlowApplication.class, args);
        }

}
