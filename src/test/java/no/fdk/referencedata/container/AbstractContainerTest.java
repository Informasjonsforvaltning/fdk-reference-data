package no.fdk.referencedata.container;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static no.fdk.referencedata.container.AbstractContainerTest.Initializer;

@SpringBootTest
@ContextConfiguration(initializers = Initializer.class)
public class AbstractContainerTest {

    private static final int WIREMOCK_PORT = 8080;

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @SuppressWarnings("resource")
        static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16")
            .withDatabaseName("reference_data")
            .withUsername("root")
            .withPassword("password")
            .withReuse(true);

        @SuppressWarnings("resource")
        static final GenericContainer<?> wiremock = new GenericContainer<>("wiremock/wiremock")
                .withExposedPorts(WIREMOCK_PORT)
                .withClasspathResourceMapping("wiremock", "/home/wiremock", BindMode.READ_ONLY)
                .withReuse(true);

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            postgres.start();
            wiremock.start();

            String postgresUrl = "spring.datasource.url=" + postgres.getJdbcUrl();
            String postgresUsername = "spring.datasource.username=" + postgres.getUsername();
            String postgresPassword = "spring.datasource.password=" + postgres.getPassword();
            String wiremockContainerIP = "wiremock.host=" + wiremock.getHost();
            String wiremockContainerPort = "wiremock.port=" + wiremock.getMappedPort(WIREMOCK_PORT);

            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                    postgresUrl,
                    postgresUsername,
                    postgresPassword,
                    wiremockContainerIP,
                    wiremockContainerPort);
        }
    }
}
