package com.example.prac.service.calendar;

import com.example.prac.dto.data.DishDTO;
import com.example.prac.dto.data.DishResponseDTO;
import com.example.prac.dto.data.IngredientDTO;
import com.example.prac.errorHandler.ResourceNotFoundException;
import com.example.prac.model.data.AvailableIngredients;
import com.example.prac.model.data.Dish;
import com.example.prac.model.data.Ingredient;
import com.example.prac.repository.data.AvailableIngredientsRepository;
import com.example.prac.repository.data.DishRepository;
import com.example.prac.repository.data.IngredientRepository;
import com.example.prac.service.UserContextService;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DishService {

    private final EntityManager entityManager;
    private final DishRepository dishRepository;
    private final IngredientRepository ingredientRepository;
    private final UserContextService userContextService;
    private final AvailableIngredientsRepository availableIngredientsRepository;

    public DishResponseDTO createDish(DishDTO dishDTO, Long userId) {
        Dish dish = new Dish();
        dish.setName(dishDTO.getName());
        dish.setUserId(userId);
        dish.setInstructions(dishDTO.getInstructions());
        dish.setImageUrl(dishDTO.getImageUrl()); // Устанавливаем URL изображения
        dish.setIngredients(mapIngredients(dishDTO.getIngredients()));

        dish = dishRepository.save(dish);
        return mapToResponse(dish);
    }


    @Transactional
    public DishResponseDTO updateDish(Long dishId, DishDTO dishDTO) {
        // Проверяем, существует ли блюдо
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new IllegalArgumentException("Блюдо не найдено"));

        // Проверяем, что пользователь имеет права на изменение блюда
        if (!Objects.equals(userContextService.getCurrentUserId(), dish.getUserId())) {
            throw new RuntimeException("You have no permission for this operation!");
        }

        // Обновляем поля блюда
        dish.setName(dishDTO.getName());
        dish.setInstructions(dishDTO.getInstructions());

        // Карта текущих ингредиентов блюда
        Map<Long, Ingredient> existingIngredients = dish.getIngredients().stream()
                .collect(Collectors.toMap(Ingredient::getIngredientId, ingredient -> ingredient));

        // Список для обновленных ингредиентов
        List<Ingredient> updatedIngredients = new ArrayList<>();

        for (IngredientDTO ingredientDTO : dishDTO.getIngredients()) {
            Ingredient ingredient;

            if (ingredientDTO.getId() != null && existingIngredients.containsKey(ingredientDTO.getId())) {
                // Обновляем существующий ингредиент
                ingredient = existingIngredients.get(ingredientDTO.getId());
                ingredient.setName(ingredientDTO.getName());
                ingredient.setUnit(ingredientDTO.getUnit());
            } else {
                // Добавляем новый ингредиент
                ingredient = new Ingredient();
                ingredient.setName(ingredientDTO.getName());
                ingredient.setUnit(ingredientDTO.getUnit());
                ingredient = ingredientRepository.save(ingredient);
            }

            updatedIngredients.add(ingredient);

            // Логика добавления ингредиента в доступность
            boolean ingredientExists = availableIngredientsRepository.existsByUserAndIngredient(
                    userContextService.getCurrentUser(),
                    ingredient
            );

            if (!ingredientExists) {
                AvailableIngredients availableIngredient = new AvailableIngredients();
                availableIngredient.setUser(userContextService.getCurrentUser());
                availableIngredient.setIngredient(ingredient);
                availableIngredient.setAvailable(false); // По умолчанию ингредиент недоступен
                entityManager.persist(availableIngredient);
            }
        }

        // Удаляем ингредиенты, которые отсутствуют в новом списке
        List<Ingredient> ingredientsToRemove = dish.getIngredients().stream()
                .filter(ingredient -> updatedIngredients.stream()
                        .noneMatch(updated -> Objects.equals(updated.getIngredientId(), ingredient.getIngredientId())))
                .collect(Collectors.toList());

        dish.getIngredients().removeAll(ingredientsToRemove);

        // Устанавливаем обновленный список ингредиентов
        dish.setIngredients(updatedIngredients);

        // Сохраняем блюдо
        Dish updatedDish = dishRepository.save(dish);

        // Возвращаем DTO
        return mapToResponse(updatedDish);
    }





    public void deleteDish(Long dishId) {
        Dish dishToDelete = dishRepository.findById(dishId).orElseThrow(()-> new ResourceNotFoundException("dish not found"));
        if (!Objects.equals(dishToDelete.getUserId(), userContextService.getCurrentUserId())){
            throw new RuntimeException("You have no permission for this operation");
        }
        dishRepository.deleteById(dishId);
    }

    public DishResponseDTO getDishById(Long dishId){
        Dish dish = dishRepository.findById(dishId).orElseThrow(()-> new ResourceNotFoundException("dish not found"));
        if (!Objects.equals(dish.getUserId(), userContextService.getCurrentUserId())){
            throw new RuntimeException("You have no permission for this operation");
        }
        return mapToResponse(dish);
    }


    public List<DishResponseDTO> getAllDishes(Long userId) {
        return dishRepository.findAllByUserId(userId).stream() // Используем метод из репозитория
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


//    public Dish getDishById(Long id){
////        return dishRepository.getDishByUserId(userId).orElseThrow(() -> new RuntimeException("dish not found")); // TODO: make custom exception
//        return  dishRepository.findById(id).orElseThrow();
//    }


    private DishResponseDTO mapToResponse(Dish dish) {
        DishResponseDTO response = new DishResponseDTO();
        response.setId(dish.getDishId());
        response.setName(dish.getName());
        response.setInstructions(dish.getInstructions());
        response.setImageUrl(dish.getImageUrl()); // Устанавливаем URL изображения
        response.setIngredients(
                dish.getIngredients().stream()
                        .map(ingredient -> new IngredientDTO(
                                ingredient.getIngredientId(),
                                ingredient.getName(),
                                ingredient.getUnit()
                        ))
                        .collect(Collectors.toList())
        );
        return response;
    }


    private List<Ingredient> mapIngredients(List<IngredientDTO> ingredientDTOs) {
        return ingredientDTOs.stream()
                .map(dto -> ingredientRepository.findByName(dto.getName())
                        .orElseGet(() -> {
                            Ingredient ingredient = new Ingredient();
                            ingredient.setName(dto.getName());
                            ingredient.setUnit(dto.getUnit());
                            return ingredientRepository.save(ingredient);
                        }))
                .collect(Collectors.toList());
    }
}
