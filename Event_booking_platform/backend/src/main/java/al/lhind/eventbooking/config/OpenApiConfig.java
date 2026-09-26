package al.lhind.eventbooking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI eventBookingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Event Booking API")
                        .version("v1")
                        .description("REST API for event discovery, bookings, reviews, "
                                + "organizer management, and administration."))
                .components(new Components().addSecuritySchemes(
                        BEARER_AUTH,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Use the accessToken returned by POST /api/v1/auth/login.")));
    }
}
