package no.fdk.referencedata.security;

import no.fdk.referencedata.ApplicationSettings;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
public class WebSecurityConfig {

    private final ApplicationSettings applicationSettings;

    @Autowired
    public WebSecurityConfig(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception  {
        final APIKeyAuthFilter filter = new APIKeyAuthFilter("X-API-KEY");
        filter.setAuthenticationManager(authentication -> {
            String principal = (String) authentication.getPrincipal();
            if (StringUtils.isEmpty(principal) || !applicationSettings.getApiKey().equals(principal)) {
                throw new BadCredentialsException("The API key was not found or not the expected value.");
            }
            authentication.setAuthenticated(true);
            return authentication;
        });

        http.csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().cors().configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues())
                .and().addFilter(filter)
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
