package com.example.prac.service.parser;

import com.example.prac.dto.parser.ProductCategory;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class ProductParserRunner implements CommandLineRunner {

    private final ProductParserService productParserService;

    // Метод, содержащий логику обработки категорий
    public void processCategories() {
        List<String> categoryIds = List.of(
                "73C2336", "73C9831", "73C2327", "73C10314", "73C10315",
                "73C2339", "73C9727", "73C2333", "73C9769", "73C2335", "73C9786",
                "73C2334", "73C9802", "73C9811", "73C9820", "73C9841", "73C2326");

        for (String categoryId : categoryIds) {
            System.out.printf("Начинаем обработку категории: %s%n", categoryId);

            ProductCategory category = productParserService.fetchCategory(categoryId);

            if (category != null) {
                productParserService.saveProductsToDatabase(category);
            } else {
                System.out.printf("Не удалось обработать категорию: %s%n", categoryId);
            }
        }
    }

    // Запуск обработки при старте приложения
    @Override
    public void run(String... args) {
        System.out.println("Запуск обработки категорий при старте приложения...");
        processCategories();
    }

    // Запуск обработки каждые 4 часа
    @Scheduled(fixedRate = 14400000) // 4 часа в миллисекундах
    public void scheduleProcessing() {
        System.out.println("Периодический запуск обработки категорий...");
        processCategories();
    }
}

