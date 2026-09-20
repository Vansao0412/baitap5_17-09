package vn.iotstar.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Category - Product API",
        version = "1.0",
        description = "RESTful API CRUD cho bài thực hành Spring Boot 3"))
public class OpenApiConfig {
}
