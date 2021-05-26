package no.fdk.referencedata.mongo;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.GenericContainer;

import static no.fdk.referencedata.mongo.AbstractMongoDbContainerTest.Initializer;

@SpringBootTest
@ContextConfiguration(initializers = Initializer.class)
public class AbstractMongoDbContainerTest {

    private static final int MONGO_PORT = 27017;

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        static final GenericContainer<?> mongodb = new GenericContainer<>("mongo:latest")
            .withExposedPorts(MONGO_PORT)
            .withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
            .withEnv("MONGO_INITDB_ROOT_PASSWORD", "password")
            .withReuse(true);

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            // Start container
            mongodb.start();

            // Override Redis configuration
            String mongoDbContainerIP = "spring.data.mongodb.host=" + mongodb.getContainerIpAddress();
            String mongoDbContainerPort = "spring.data.mongodb.port=" + mongodb.getMappedPort(MONGO_PORT); // <- This is how you get the random port.

            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,  mongoDbContainerIP, mongoDbContainerPort); // <- This is how you override the configuration in runtime.
        }
    }
}