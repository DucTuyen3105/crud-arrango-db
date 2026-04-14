package com.example.demo.config;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ArangoConfig {
    private static final Logger log = LoggerFactory.getLogger(ArangoConfig.class);

    @Value("${arango.host:localhost}")
    private String host;

    @Value("${arango.port:8529}")
    private Integer port;

    @Value("${arango.username:root}")
    private String username;

    @Value("${arango.password:123456}")
    private String password;

    @Value("${arango.database:user_management}")
    private String databaseName;

    @Bean(destroyMethod = "shutdown")
    public ArangoDB arangoDB() {
        return new ArangoDB.Builder()
                .host(host, port)
                .user(username)
                .password(password)
                .build();
    }

    @Bean
    public ArangoDatabase arangoDatabase(ArangoDB arangoDB) {
        return arangoDB.db(databaseName);
    }

    @PostConstruct
    public void initDatabase() {
        int maxRetries = 10;
        int delaySeconds = 5;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            ArangoDB client = new ArangoDB.Builder()
                    .host(host, port)
                    .user(username)
                    .password(password)
                    .build();
            try {
                if (!client.db(databaseName).exists()) {
                    client.createDatabase(databaseName);
                }
                ArangoDatabase db = client.db(databaseName);
                if (!db.collection("users").exists()) {
                    db.createCollection("users");
                }
                log.info("ArangoDB initialized successfully.");
                return;
            } catch (Exception ex) {
                log.warn("ArangoDB init attempt {}/{} failed: {}", attempt, maxRetries, ex.getMessage());
                if (attempt < maxRetries) {
                    try { Thread.sleep(delaySeconds * 1000L); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return; }
                } else {
                    log.error("Could not initialize ArangoDB after {} attempts.", maxRetries);
                }
            } finally {
                client.shutdown();
            }
        }
    }
}
