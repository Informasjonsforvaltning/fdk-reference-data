package no.fdk.referencedata;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
    name = "apiKey",
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.HEADER,
    paramName = "X-API-KEY"
)
@OpenAPIDefinition(
    info = @Info(
        title = "FDK Reference data",
        version = "v2.0.0",
        description = "API documentation for fdk-reference-data"
    ),
    servers = {
        @Server(url = "https://fellesdatakatalog.digdir.no/reference-data", description = "Production server"),
        @Server(url = "https://demo.fellesdatakatalog.digdir.no/reference-data", description = "Demo test server"),
        @Server(url = "https://staging.fellesdatakatalog.digdir.no/reference-data", description = "Staging test server")
    }
)
public class OpenApiConfig { }
