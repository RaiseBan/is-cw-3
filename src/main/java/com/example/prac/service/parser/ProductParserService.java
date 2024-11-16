package com.example.prac.service.parser;

import com.example.prac.dto.parser.Product;
import com.example.prac.dto.parser.ProductCategory;
import com.example.prac.dto.parser.Subcategory;
import com.example.prac.model.data.ParsedProduct;
import com.example.prac.repository.data.ParsedProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class ProductParserService {

    private static final String BASE_URL = "https://5d.5ka.ru/api/catalog/v1/stores/Y232/categories/";
    private static final String DEFAULT_PARAMS = "/preview?mode=delivery&include_restrict=false&preview_products_count=6";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ParsedProductRepository parsedProductRepository;

    public ProductParserService(ParsedProductRepository parsedProductRepository) {
        this.parsedProductRepository = parsedProductRepository;
    }

    /**
     * Отправка запроса на сайт и парсинг JSON.
     *
     * @param categoryId ID категории продуктов.
     * @return Список категорий продуктов.
     */
    public ProductCategory fetchCategory(String categoryId) {
        try {
            String url = BASE_URL + categoryId + DEFAULT_PARAMS;
            System.out.printf("Отправка запроса на URL: %s%n", url);

            Connection.Response response = Jsoup.connect(url)
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .header("accept", "application/json, text/plain, */*")
                    .header("accept-encoding", "gzip, deflate, br, zstd")
                    .header("accept-language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("origin", "https://5ka.ru")
                    .header("priority", "u=1, i")
                    .header("sec-ch-ua", "\"Chromium\";v=\"130\", \"Google Chrome\";v=\"130\", \"Not?A_Brand\";v=\"99\"")
                    .header("sec-ch-ua-mobile", "?0")
                    .header("sec-ch-ua-platform", "\"Windows\"")
                    .header("sec-fetch-dest", "empty")
                    .header("sec-fetch-mode", "cors")
                    .header("sec-fetch-site", "same-site")
                    .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36")
                    .header("x-app-version", "0.1.1.dev")
                    .header("x-device-id", "1e110fee-7f3e-48de-bffc-5169cb9cd926")
                    .header("x-platform", "webapp")
                    .execute();

            System.out.printf("Код ответа: %d%n", response.statusCode());
            if (response.statusCode() == 200) {
                return MAPPER.readValue(response.body(), ProductCategory.class);
            } else {
                System.err.printf("Ошибка: %d. Тело ответа: %s%n", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            System.err.printf("Ошибка при выполнении запроса категории %s: %s%n", categoryId, e.getMessage());
        }
        return null;
    }

    /**
     * Сохранение данных о продуктах в базу данных.
     *
     * @param category Список категорий продуктов.
     */
    public void saveProductsToDatabase(ProductCategory category) {
        if (category == null) {
            System.out.println("Категория отсутствует или равна null.");
            return;
        }

        Set<String> existingProducts = new HashSet<>(parsedProductRepository.findAllNames());

        if (category.getSubcategories() != null) {
            for (Subcategory subcategory : category.getSubcategories()) {
                if (subcategory.getProducts() != null) {
                    for (Product product : subcategory.getProducts()) {
                        try {
                            String priceString = product.getPrices() != null ? product.getPrices().getRegular() : null;
                            if (priceString == null || priceString.isEmpty()) {
                                continue;
                            }

                            if (existingProducts.contains(product.getName())) {
//                                System.out.println("have an item");
                                continue;
                            }

                            Double price = Double.parseDouble(priceString);

                            ParsedProduct parsedProduct = new ParsedProduct();
                            parsedProduct.setName(product.getName());
                            parsedProduct.setPrice(price);

                            parsedProductRepository.save(parsedProduct);
                            existingProducts.add(product.getName());

                            System.out.printf("Сохранён продукт: %s, цена: %.2f%n", product.getName(), price);
                        } catch (Exception e) {
                            System.err.printf("Ошибка при обработке продукта: %s. Ошибка: %s%n", product.getName(), e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
