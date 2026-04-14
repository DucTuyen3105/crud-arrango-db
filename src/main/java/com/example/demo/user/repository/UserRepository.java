package com.example.demo.user.repository;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.AqlQueryOptions;
import com.example.demo.user.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepository {

    private final ArangoDatabase arangoDatabase;

    public UserRepository(ArangoDatabase arangoDatabase) {
        this.arangoDatabase = arangoDatabase;
    }

    public List<User> findAll() {
        String query = "FOR u IN users SORT u._key DESC RETURN u";
        ArangoCursor<BaseDocument> cursor = arangoDatabase.query(
                query,
                BaseDocument.class,
                null,
                new AqlQueryOptions()
        );

        List<User> users = new ArrayList<>();
        cursor.forEachRemaining(doc -> users.add(toUser(doc)));
        return users;
    }

    public Optional<User> findById(String key) {
        String query = "FOR u IN users FILTER u._key == @key LIMIT 1 RETURN u";
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("key", key);
        ArangoCursor<BaseDocument> cursor = arangoDatabase.query(
                query,
                BaseDocument.class,
                bindVars,
                new AqlQueryOptions()
        );
        if (!cursor.hasNext()) {
            return Optional.empty();
        }
        return Optional.of(toUser(cursor.next()));
    }

    public User create(User user) {
        String query = "INSERT { name: @name, email: @email, age: @age } IN users RETURN NEW";
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("name", user.getName());
        bindVars.put("email", user.getEmail());
        bindVars.put("age", user.getAge());
        ArangoCursor<BaseDocument> cursor = arangoDatabase.query(
                query,
                BaseDocument.class,
                bindVars,
                new AqlQueryOptions()
        );
        return toUser(cursor.next());
    }

    public Optional<User> update(String key, User user) {
        String query = """
                UPDATE @key WITH {
                  name: @name,
                  email: @email,
                  age: @age
                } IN users
                OPTIONS { keepNull: false }
                RETURN NEW
                """;
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("key", key);
        bindVars.put("name", user.getName());
        bindVars.put("email", user.getEmail());
        bindVars.put("age", user.getAge());
        ArangoCursor<BaseDocument> cursor = arangoDatabase.query(
                query,
                BaseDocument.class,
                bindVars,
                new AqlQueryOptions()
        );
        if (!cursor.hasNext()) {
            return Optional.empty();
        }
        return Optional.of(toUser(cursor.next()));
    }

    public boolean deleteById(String key) {
        String query = """
                FOR u IN users
                  FILTER u._key == @key
                  REMOVE u IN users
                  RETURN OLD
                """;
        Map<String, Object> bindVars = new HashMap<>();
        bindVars.put("key", key);
        ArangoCursor<BaseDocument> cursor = arangoDatabase.query(
                query,
                BaseDocument.class,
                bindVars,
                new AqlQueryOptions()
        );
        return cursor.hasNext();
    }

    private User toUser(BaseDocument doc) {
        String key = doc.getKey();
        String name = (String) doc.getAttribute("name");
        String email = (String) doc.getAttribute("email");
        Integer age = null;
        Object ageObj = doc.getAttribute("age");
        if (ageObj instanceof Number number) {
            age = number.intValue();
        }
        return new User(key, name, email, age);
    }
}
