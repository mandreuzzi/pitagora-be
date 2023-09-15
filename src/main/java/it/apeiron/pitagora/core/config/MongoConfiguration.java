package it.apeiron.pitagora.core.config;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;


@Configuration
public class MongoConfiguration {

    @Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

    @Primary
    @Bean(name = "dbCoreProps")
    @ConfigurationProperties("spring.data.mongodb.core")
    public MongoProperties dbCoreProps() {
        return new MongoProperties();
    }

    @Bean(name = "dbDataProps")
    @ConfigurationProperties("spring.data.mongodb.data")
    public MongoProperties dbDataProps() {
        return new MongoProperties();
    }

    @Primary
    @Bean(name = "mongoTemplate")
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(dbCoreMongoDatabaseFactory(dbCoreProps()));
    }

    @Bean(name = "mongoTemplateData")
    public MongoTemplate mongoTemplateData() {
        return new MongoTemplate(dbDataMongoDatabaseFactory(dbDataProps()));
    }

    @Primary
    @Bean
    public MongoDatabaseFactory dbCoreMongoDatabaseFactory(MongoProperties props) {
        return new SimpleMongoClientDatabaseFactory(props.getUri());
    }

    @Bean
    public MongoDatabaseFactory dbDataMongoDatabaseFactory(MongoProperties props) {
        return new SimpleMongoClientDatabaseFactory(props.getUri());
    }
}
