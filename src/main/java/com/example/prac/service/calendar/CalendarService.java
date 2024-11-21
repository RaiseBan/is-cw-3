package com.example.prac.service.calendar;

import com.example.prac.dto.data.DishResponseDTO;
import com.example.prac.dto.data.IngredientDTO;
import com.example.prac.dto.data.ShoppingListDTO;
import com.example.prac.model.authEntity.User;
import com.example.prac.model.data.*;
import com.example.prac.repository.data.CalendarRepository;
import com.example.prac.repository.data.ParsedProductRepository;
import com.example.prac.repository.data.ProductPriceRepository;
import com.example.prac.service.yandex.YandexTranslateService;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CalendarService {

    private final EntityManager entityManager;
    private final CalendarRepository calendarRepository;
    private final ParsedProductRepository parsedProductRepository;
    private final ProductPriceRepository productPriceRepository;
    private final YandexTranslateService yandexTranslateService;
    @Transactional
    public void addDishToCalendar(Long userId, Long dishId, String calendarDate) {
        // Добавляем блюдо в календарь
        entityManager.createNativeQuery("CALL add_dish_to_calendar(CAST(:userId AS INT), CAST(:dishId AS INT), CAST(:calendarDate AS TIMESTAMP))")
                .setParameter("userId", userId)
                .setParameter("dishId", dishId)
                .setParameter("calendarDate", LocalDateTime.parse(calendarDate))
                .executeUpdate();


        // Получаем список ингредиентов для блюда
        List<Ingredient> ingredients = entityManager.createQuery(
                        "SELECT i FROM Dish d JOIN d.ingredients i WHERE d.dishId = :dishId", Ingredient.class)
                .setParameter("dishId", dishId)
                .getResultList();

        // Обновляем цены на ингредиенты
        updateIngredientPrices(ingredients);
    }

    public void createCalendar(User user){
        Calendar calendar = new Calendar();
        calendar.setUser(user);
        calendarRepository.save(calendar);
    }

    @Transactional
    public void removeDishFromCalendar(Long userId, Long dishId) {
        Calendar calendar = calendarRepository.findById(userId).orElseThrow(()-> new RuntimeException("you have not your own calendar..."));
        boolean flag = false;
        for (CalendarDish calendarDish: calendar.getCalendarDishes()){
            if (Objects.equals(calendarDish.getDish().getDishId(), dishId)){
                flag = true;
                break;
            }
        }
        if (!flag){
            throw new RuntimeException("you have not this dish in your calendar");
        }

        entityManager.createNativeQuery("CALL remove_dish_from_calendar(:userId, :dishId)")
                .setParameter("userId", userId)
                .setParameter("dishId", dishId)
                .executeUpdate();
    }

    public List<DishResponseDTO> getDishesFromCalendar(Long userId) {
        Calendar calendar = calendarRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Календарь пользователя не найден"));

        return calendar.getCalendarDishes().stream()
                .map(calendarDish -> {
                    var dish = calendarDish.getDish();
                    var response = new DishResponseDTO();
                    response.setId(dish.getDishId());
                    response.setName(dish.getName());
                    response.setInstructions(dish.getInstructions());
                    response.setIngredients(
                            dish.getIngredients().stream()
                                    .map(ingredient -> new IngredientDTO(ingredient.getName(), ingredient.getUnit()))
                                    .collect(Collectors.toList())
                    );
                    return response;
                })
                .collect(Collectors.toList());
    }


    /**
     * Обновление цен на ингредиенты.
     *
     * @param ingredients список ингредиентов.
     */
    private void updateIngredientPrices(List<Ingredient> ingredients) {
        for (Ingredient ingredient : ingredients) {
            // Ищем цену для ингредиента
            System.out.println("INGREDIENT: " + ingredient);
            String proxyName = yandexTranslateService.translate(ingredient.getName());

            List<ParsedProduct> parsedProducts = parsedProductRepository.findByFullTextSearch(proxyName.toLowerCase());

            if (parsedProducts.size() != 0) {
                Double totalPrice = 0d;

                for (ParsedProduct parsedProduct: parsedProducts){
                    System.out.println("price for " + parsedProduct.getName() + ": " + parsedProduct.getPrice());
                    totalPrice += parsedProduct.getPrice();
                }
                System.out.println("total price: " + totalPrice);
                System.out.println("count: " + parsedProducts.size());
                totalPrice = totalPrice / parsedProducts.size();


                // Создаём сущность ProductPrice
                ProductPrice productPrice = new ProductPrice();
                productPrice.setIngredient(ingredient);
                productPrice.setStoreName("5ka"); // Укажите название магазина
                productPrice.setPrice(totalPrice.floatValue());
                productPrice.setUpdatedAt(LocalDateTime.now());

                // Сохраняем в базе
                productPriceRepository.save(productPrice);

                System.out.printf("Цена для ингредиента %s обновлена: %.2f%n", ingredient.getName(), totalPrice);
            }else{
                // Создаём сущность ProductPrice
                ProductPrice productPrice = new ProductPrice();
                productPrice.setIngredient(ingredient);
                productPrice.setStoreName("5ka"); // Укажите название магазина
                productPrice.setPrice(null);
                productPrice.setUpdatedAt(LocalDateTime.now());

                productPriceRepository.save(productPrice);
            }

        }
    }


    // Получение списка покупок
    public List<ShoppingListDTO> getShoppingList(Long userId, String sortBy, boolean groupByDish, String sortOrder) {
        String query = "SELECT * FROM get_shopping_list(CAST(:userId AS INT), CAST(:sortBy AS VARCHAR), CAST(:groupByDish AS BOOLEAN), CAST(:sortOrder AS VARCHAR))";

        List<ShoppingListDTO> resultList = entityManager.createNativeQuery(query)
                .setParameter("userId", userId)
                .setParameter("sortBy", sortBy)
                .setParameter("groupByDish", groupByDish)
                .setParameter("sortOrder", sortOrder)
                .unwrap(org.hibernate.query.NativeQuery.class)
                .setResultTransformer((resultSet, rowNum) -> {
                    ShoppingListDTO dto = new ShoppingListDTO();
                    dto.setIngredientName((String) resultSet[0]);
                    dto.setStoreName((String) resultSet[1]);
                    dto.setPrice((Double) resultSet[2]);
                    dto.setDishName((String) resultSet[3]);
                    dto.setCount((Integer) resultSet[4]);
                    return dto;
                })
                .getResultList();

        return resultList;
    }

}
