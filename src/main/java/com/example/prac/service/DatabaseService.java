package com.example.prac.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Service
public class DatabaseService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Метод для создания GIN-индекса для полнотекстового поиска.
     */
    @Transactional
    public void createGinIndexIfNotExists() {
        try {
            // Читаем SQL из файла
            ClassPathResource resource = new ClassPathResource("db_sql/gin_index.sql");
            String sql = new BufferedReader(new InputStreamReader(resource.getInputStream()))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Выполняем SQL
            entityManager.createNativeQuery(sql).executeUpdate();
            System.out.println("Индекс создан или уже существует.");
        } catch (Exception e) {
            System.err.println("Ошибка при создании индекса: " + e.getMessage());
            throw new RuntimeException("Не удалось выполнить скрипт создания индекса", e);
        }
    }

    /**
     * Метод для выполнения SQL-скрипта.
     *
     * @param filePath путь к SQL-файлу
     */
    @Transactional
    public void executeSqlScript(String filePath) {
        try {
            // Чтение SQL-файла из ресурсов
            ClassPathResource resource = new ClassPathResource(filePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

            StringBuilder script = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                script.append(line).append("\n");
            }

            // Выполнение скрипта
            entityManager.createNativeQuery(script.toString()).executeUpdate();
            System.out.println("SQL-скрипт выполнен: " + filePath);
        } catch (Exception e) {
            System.err.println("Ошибка выполнения SQL-скрипта: " + filePath);
            e.printStackTrace();
        }
    }
}
