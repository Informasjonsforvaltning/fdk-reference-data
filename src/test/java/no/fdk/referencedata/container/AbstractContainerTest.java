package no.fdk.referencedata.container;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

import static no.fdk.referencedata.container.AbstractContainerTest.Initializer;

@SpringBootTest
@ContextConfiguration(initializers = Initializer.class)
public class AbstractContainerTest {

    private static final int MONGO_PORT = 27017;
    private static final int WIREMOCK_PORT = 8080;

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        static final GenericContainer<?> mongodb = new GenericContainer<>("mongo:latest")
            .withExposedPorts(MONGO_PORT)
            .withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
            .withEnv("MONGO_INITDB_ROOT_PASSWORD", "password")
            .withReuse(true);

        static final GenericContainer<?> wiremock = new GenericContainer<>("wiremock/wiremock")
                .withExposedPorts(WIREMOCK_PORT)
                .withClasspathResourceMapping("wiremock", "/home/wiremock", BindMode.READ_ONLY)
                .withReuse(true);

        @Override
        public void initialize(@NonNull ConfigurableApplicationContext context) {
            // Start containers
            mongodb.start();
            wiremock.start();

            // Override configuration
            String mongoDbContainerIP = "spring.data.mongodb.host=" + mongodb.getContainerIpAddress();
            String mongoDbContainerPort = "spring.data.mongodb.port=" + mongodb.getMappedPort(MONGO_PORT); // <- This is how you get the random port.
            String wiremockContainerIP = "wiremock.host=" + wiremock.getContainerIpAddress();
            String wiremockContainerPort = "wiremock.port=" + wiremock.getMappedPort(WIREMOCK_PORT); // <- This is how you get the random port.

            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                    mongoDbContainerIP,
                    mongoDbContainerPort,
                    wiremockContainerIP,
                    wiremockContainerPort); // <- This is how you override the configuration in runtime.
        }
    }
}