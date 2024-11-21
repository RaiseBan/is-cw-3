package com.example.prac.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class DatabaseService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Метод для создания GIN-индекса для полнотекстового поиска.
     */
    @Transactional
    public void createGinIndexIfNotExists() {
        String createIndexSql = "CREATE INDEX IF NOT EXISTS idx_parsed_product_name " +
                "ON parsed_product USING gin(to_tsvector('russian', name))";

        entityManager.createNativeQuery(createIndexSql).executeUpdate();
        System.out.println("Индекс создан или уже существует.");
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
