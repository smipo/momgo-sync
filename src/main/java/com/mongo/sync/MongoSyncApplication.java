package com.mongo.sync;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MongoSyncApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(MongoSyncApplication.class, args);
    }

    @Override
    public void run(String... args){

    }
}
