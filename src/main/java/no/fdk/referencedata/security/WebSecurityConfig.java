package no.fdk.referencedata.security;

import no.fdk.referencedata.ApplicationSettings;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private ApplicationSettings applicationSettings;

    @Autowired
    public WebSecurityConfig(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        final APIKeyAuthFilter filter = new APIKeyAuthFilter("X-API-KEY");
        filter.setAuthenticationManager(authentication -> {
            String principal = (String) authentication.getPrincipal();
            if (Strings.isBlank(principal) || !applicationSettings.getApiKey().equals(principal)) {
                throw new BadCredentialsException("The API key was not found or not the expected value.");
            }
            authentication.setAuthenticated(true);
            return authentication;
        });

        httpSecurity
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .cors().configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues())
                .and().addFilter(filter).authorizeRequests()
                    .antMatchers("/actuator/**").permitAll()
                    .antMatchers(HttpMethod.POST, "/eu/access-rights/**").authenticated()
                    .antMatchers(HttpMethod.POST, "/eu/data-themes/**").authenticated()
                    .antMatchers(HttpMethod.POST, "/eu/eurovocs/**").authenticated()
                    .antMatchers(HttpMethod.POST, "/eu/file-types/**").authenticated()
                    .antMatchers(HttpMethod.POST, "/iana/media-types/**").authenticated()
                    .anyRequest().permitAll();
    }


}
