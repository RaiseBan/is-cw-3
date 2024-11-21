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
        // Проверяем, существует ли блюдо с указанным ID
        Dish originalDish = entityManager.find(Dish.class, dishId);
        if (originalDish == null) {
            throw new IllegalArgumentException("Блюдо с ID " + dishId + " не найдено");
        }

        // Получаем календарь пользователя
        Calendar userCalendar = entityManager.createQuery(
                        "SELECT c FROM Calendar c WHERE c.user.id = :userId", Calendar.class)
                .setParameter("userId", userId)
                .getSingleResult();

        if (userCalendar == null) {
            throw new IllegalArgumentException("Календарь для пользователя " + userId + " не найден");
        }

        // Создаем новую запись в CalendarDish
        CalendarDish calendarDish = new CalendarDish();
        calendarDish.setCalendar(userCalendar);
        calendarDish.setOriginalDish(originalDish);
        calendarDish.setTime(LocalDateTime.parse(calendarDate));

        // Сохраняем запись
        entityManager.persist(calendarDish);

        // Получаем список ингредиентов для блюда
        List<Ingredient> ingredients = originalDish.getIngredients();

        // Обновляем или добавляем ингредиенты в ShoppingList
        for (Ingredient ingredient : ingredients) {
            ShoppingList existingItem = entityManager.createQuery(
                            "SELECT sl FROM ShoppingList sl WHERE sl.user.id = :userId AND sl.ingredient.id = :ingredientId",
                            ShoppingList.class)
                    .setParameter("userId", userId)
                    .setParameter("ingredientId", ingredient.getIngredientId())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (existingItem == null) {
                ShoppingList shoppingList = new ShoppingList();
                shoppingList.setUser(userCalendar.getUser());
                shoppingList.setIngredient(ingredient);
                entityManager.persist(shoppingList);
            }

        }

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
            if (Objects.equals(calendarDish.getDishId(), dishId)){
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
                    var dish = calendarDish.getOriginalDish();
                    var response = new DishResponseDTO();
                    response.setId(calendarDish.getDishId());
                    response.setName(dish.getName());
                    response.setInstructions(dish.getInstructions());
                    response.setIngredients(
                            dish.getIngredients().stream()
                                    .map(ingredient -> new IngredientDTO(ingredient.getIngredientId(), ingredient.getName(), ingredient.getUnit()))
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
