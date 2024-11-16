package com.example.prac.service.calendar;

import com.example.prac.dto.data.DishResponseDTO;
import com.example.prac.dto.data.IngredientDTO;
import com.example.prac.dto.data.ShoppingListDTO;
import com.example.prac.model.data.Calendar;
import com.example.prac.model.data.Ingredient;
import com.example.prac.model.data.ProductPrice;
import com.example.prac.repository.data.CalendarRepository;
import com.example.prac.repository.data.ParsedProductRepository;
import com.example.prac.repository.data.ProductPriceRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CalendarService {

    private final EntityManager entityManager;
    private final CalendarRepository calendarRepository;
    private final ParsedProductRepository parsedProductRepository;
    private final ProductPriceRepository productPriceRepository;

    public void addDishToCalendar(Long userId, Long dishId, String calendarDate) {
        // Добавляем блюдо в календарь
        entityManager.createNativeQuery("CALL add_dish_to_calendar(:userId, :dishId, :calendarDate)")
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

    public void removeDishFromCalendar(Long userId, Long dishId) {
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
            Double price = parsedProductRepository.findParsedProductByName(ingredient.getName()).getPrice();

            if (price != null) {
                // Создаём сущность ProductPrice
                ProductPrice productPrice = new ProductPrice();
                productPrice.setIngredient(ingredient);
                productPrice.setStoreName("Default Store"); // Укажите название магазина
                productPrice.setPrice(price.floatValue());
                productPrice.setUpdatedAt(LocalDateTime.now());

                // Сохраняем в базе
                productPriceRepository.save(productPrice);

                System.out.printf("Цена для ингредиента %s обновлена: %.2f%n", ingredient.getName(), price);
            } else {
                System.out.printf("Цена для ингредиента %s не найдена.%n", ingredient.getName());
            }
        }
    }


    // Получение списка покупок
    public List<ShoppingListDTO> getShoppingList(Long userId, String sortBy, boolean groupByDish, String sortOrder) {
        String query = "SELECT * FROM get_shopping_list(:userId, :sortBy, :groupByDish, :sortOrder)";

        return entityManager.createNativeQuery(query)
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
    }

}
