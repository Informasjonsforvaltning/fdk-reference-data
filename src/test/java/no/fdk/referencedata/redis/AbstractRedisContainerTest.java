package no.fdk.referencedata.redis;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.GenericContainer;

import static no.fdk.referencedata.redis.AbstractRedisContainerTest.Initializer;

@SpringBootTest
@ContextConfiguration(initializers = Initializer.class)
public class AbstractRedisContainerTest {

    private static final int REDIS_PORT = 6379;

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        static final GenericContainer<?> redis = new GenericContainer<>("redis:6-alpine")
            .withExposedPorts(REDIS_PORT)
            .withReuse(true);

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            // Start container
            redis.start();

            // Override Redis configuration
            String redisContainerIP = "spring.redis.host=" + redis.getContainerIpAddress();
            String redisContainerPort = "spring.redis.port=" + redis.getMappedPort(REDIS_PORT); // <- This is how you get the random port.

            System.out.println(redisContainerIP);
            System.out.println(redisContainerPort);
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,  redisContainerIP, redisContainerPort); // <- This is how you override the configuration in runtime.
        }
    }
}