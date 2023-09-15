package it.apeiron.pitagora.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;


@SpringBootApplication
@EnableMongoAuditing
public class PitagoraCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(PitagoraCoreApplication.class, args);
    }

}

