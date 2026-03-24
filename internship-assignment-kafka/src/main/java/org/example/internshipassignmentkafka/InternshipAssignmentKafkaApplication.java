package org.example.internshipassignmentkafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

import java.util.TimeZone;

@SpringBootApplication
@EnableReactiveMongoAuditing
public class InternshipAssignmentKafkaApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
        SpringApplication.run(InternshipAssignmentKafkaApplication.class, args);
    }

}
