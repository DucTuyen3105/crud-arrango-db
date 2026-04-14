package com.example.demo.user.model;

public class User {
    private String key;
    private String name;
    private String email;
    private Integer age;

    public User() {
    }

    public User(String key, String name, String email, Integer age) {
        this.key = key;
        this.name = name;
        this.email = email;
        this.age = age;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
