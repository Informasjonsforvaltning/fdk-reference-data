package no.fdk.referencedata.security;

import no.fdk.referencedata.ApplicationSettings;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
public class WebSecurityConfig {

    private final ApplicationSettings applicationSettings;

    @Value("${application.cors.originPatterns}")
    List<String> corsOriginPatterns;

    @Autowired
    public WebSecurityConfig(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        final APIKeyAuthFilter filter = new APIKeyAuthFilter("X-API-KEY");
        filter.setAuthenticationManager(authentication -> {
            String principal = (String) authentication.getPrincipal();
            if (StringUtils.isEmpty(principal) || !applicationSettings.getApiKey().equals(principal)) {
                throw new BadCredentialsException("The API key was not found or not the expected value.");
            }
            authentication.setAuthenticated(true);
            return authentication;
        });

        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement((sessionConfig) -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors((cors) -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowCredentials(false);
                    config.setAllowedHeaders(List.of("*"));
                    config.setMaxAge(3600L);
                    config.setAllowedOriginPatterns(corsOriginPatterns);
                    config.setAllowedMethods(List.of("GET", "POST", "OPTIONS", "DELETE", "PUT"));

                    return config;
                }))
                .addFilter(filter)
                .authorizeHttpRequests((authorize) ->
                    authorize.requestMatchers(antMatcher("/actuator/**")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.POST, "/eu/**")).authenticated()
                        .requestMatchers(antMatcher(HttpMethod.POST, "/iana/**")).authenticated()
                        .requestMatchers(antMatcher(HttpMethod.POST, "/geonorge/**")).authenticated()
                        .requestMatchers(antMatcher(HttpMethod.POST, "/digdir/**")).authenticated()
                        .requestMatchers(antMatcher(HttpMethod.POST, "/los/**")).authenticated()
                        .requestMatchers(antMatcher(HttpMethod.POST, "/ssb/**")).authenticated()
                        .anyRequest().permitAll()
                );
        return http.build();
    }

}
