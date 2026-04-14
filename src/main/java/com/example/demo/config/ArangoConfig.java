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
        } catch (Exception ex) {
            // Keep the web app bootable even when ArangoDB is down.
            log.warn("ArangoDB is not reachable during startup. Skipping init for now: {}", ex.getMessage());
        } finally {
            client.shutdown();
        }
    }
}
