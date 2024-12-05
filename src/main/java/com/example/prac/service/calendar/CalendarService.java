package com.example.prac.service.calendar;

import com.example.prac.dto.data.*;
import com.example.prac.errorHandler.ResourceNotFoundException;
import com.example.prac.model.authEntity.User;
import com.example.prac.model.data.*;
import com.example.prac.repository.data.*;
import com.example.prac.service.UserContextService;
import com.example.prac.service.yandex.YandexTranslateService;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private final AvailableIngredientsRepository availableIngredientsRepository;
    private final CalendarDishRepository calendarDishRepository;
    private final DishRepository dishRepository;

    private final UserContextService userContextService;
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

            boolean ingredientExists = availableIngredientsRepository.existsByUserAndIngredient(userContextService.getCurrentUser(), ingredient);
            System.out.println("ingredientExists " + ingredientExists);

            // Если нет, добавляем новую запись
            if (!ingredientExists) {
                AvailableIngredients availableIngredient = new AvailableIngredients();
                availableIngredient.setUser(userCalendar.getUser());
                availableIngredient.setIngredient(ingredient);
                availableIngredient.setAvailable(false); // По умолчанию ингредиент недоступен
                entityManager.persist(availableIngredient);
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


    @Transactional
    public CalendarDishDTOResponse updateDishInCalendar(Long dishId, CalendarDishDTOResponse calendarDishDTOResponse) {
        // Проверяем существование записи в календаре
        CalendarDish calendarDish = calendarDishRepository.findById(dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Dish in calendar not found"));

        // Проверяем, что оригинальное блюдо существует
        dishRepository.findById(calendarDish.getOriginalDish().getDishId())
                .orElseThrow(() -> new ResourceNotFoundException("Original dish not found"));

        // Обновляем дату и время блюда в календаре
        calendarDish.setTime(LocalDateTime.parse(calendarDishDTOResponse.getTime()));

        // Сохраняем обновление
        CalendarDish updatedDish = calendarDishRepository.save(calendarDish);

        // Возвращаем обновленное DTO
        return new CalendarDishDTOResponse(
                updatedDish.getDishId(),
                updatedDish.getOriginalDish().getDishId(),
                updatedDish.getTime().toString()
        );
    }




    public List<DishResponseDTO> getDishesFromCalendar(Long userId) {
        Calendar calendar = calendarRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Календарь пользователя не найден"));

        return calendar.getCalendarDishes().stream()
                .map(calendarDish -> {
                    var dish = calendarDish.getOriginalDish();
                    var response = new DishResponseDTO();
                    response.setDateTime(calendarDish.getTime());
                    System.out.println(calendarDish.getTime());
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
    public void updateIngredientPrices(List<Ingredient> ingredients) {
        for (Ingredient ingredient : ingredients) {
            // Ищем цену для ингредиента
            System.out.println("INGREDIENT: " + ingredient.toString());
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

        // Основной список покупок
        List<ShoppingListDTO> resultList = entityManager.createNativeQuery(query)
                .setParameter("userId", userId)
                .setParameter("sortBy", sortBy)
                .setParameter("groupByDish", groupByDish)
                .setParameter("sortOrder", sortOrder)
                .unwrap(org.hibernate.query.NativeQuery.class)
                .setResultTransformer((resultSet, rowNum) -> {
                    ShoppingListDTO dto = new ShoppingListDTO();
                    dto.setIngredientName((String) resultSet[0]); // Имя ингредиента
                    dto.setStoreName((String) resultSet[1]); // Магазин
                    dto.setPrice((Double) resultSet[2]); // Цена
                    dto.setDishName((String) resultSet[3]); // Название блюда
                    dto.setCount((Integer) resultSet[4]); // Количество
                    return dto;
                })
                .getResultList();

        // Получение доступных ингредиентов
        List<Object[]> availableIngredients = entityManager.createQuery(
                        "SELECT ai.ingredient.ingredientId, ai.ingredient.name, ai.available FROM AvailableIngredients ai " +
                                "WHERE ai.user.userId = :userId", Object[].class)
                .setParameter("userId", userId)
                .getResultList();

        // Создаем мапу из ID ингредиента и доступности
        Map<String, Object[]> ingredientMap = availableIngredients.stream()
                .collect(Collectors.toMap(
                        obj -> (String) obj[1], // Имя ингредиента
                        obj -> new Object[]{obj[0], obj[2]}, // ID ингредиента и доступность
                        (existing, replacement) -> existing // Логика разрешения дубликатов (оставляем существующее)
                ));


        // Обогащаем DTO
        for (ShoppingListDTO dto : resultList) {
            Object[] ingredientData = ingredientMap.get(dto.getIngredientName());
            if (ingredientData != null) {
                dto.setIngredientId((Long) ingredientData[0]); // ID ингредиента
                dto.setAvailable((Boolean) ingredientData[1]); // Доступность
            } else {
                dto.setIngredientId(null);
                dto.setAvailable(false); // По умолчанию недоступен
            }
        }

        return resultList;
    }





}
