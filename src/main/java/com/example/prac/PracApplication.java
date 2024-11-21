package com.example.prac;

import com.example.prac.service.DatabaseService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@AllArgsConstructor
public class PracApplication implements CommandLineRunner{

    private final DatabaseService databaseService;

    public static void main(String[] args) {
        SpringApplication.run(PracApplication.class, args);
    }

    @Override
    public void run(String... args) {
        databaseService.createGinIndexIfNotExists();
        System.out.println("Создание индексов прошло успешно!");
        databaseService.executeSqlScript("db_sql/functions.sql");
        System.out.println("Создание функций прошло успешно!");
    }



}
