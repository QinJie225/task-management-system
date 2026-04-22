package org.example.internshipassignmentkafka;

import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=kafka:9092",
        "spring.kafka.admin.properties.bootstrap.servers=kafka:9092",
        "spring.kafka.admin.auto-create=false",
        "spring.kafka.listener.auto-startup=false",
        "spring.mongodb.uri=mongodb://mongo:27017/TaskManagementSystem",
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/internship-task-realm"
})
@EnableAutoConfiguration(exclude = {EmbeddedMongoAutoConfiguration.class})
class InternshipAssignmentKafkaApplicationTests {

    @Test
    void contextLoads() {
    }

}
