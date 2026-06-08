package org.example.springwithhibernatetestapp;

import org.bson.UuidRepresentation;
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    // Customize the single Spring-managed MongoClient that the JPA integration borrows (no second pool).
    // STANDARD UUID representation is required for the entity's UUID @Id; the database name now comes from
    // spring.mongodb.uri. This is the Spring-idiomatic customization hook — NOT Spring Data MongoDB.
    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer() {
        return builder -> builder.uuidRepresentation(UuidRepresentation.STANDARD)
                .applyToConnectionPoolSettings(pool -> pool.maxSize(10));
    }
}
