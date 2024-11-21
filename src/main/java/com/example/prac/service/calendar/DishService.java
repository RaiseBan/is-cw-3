package com.example.prac.service.calendar;

import com.example.prac.dto.data.DishDTO;
import com.example.prac.dto.data.DishResponseDTO;
import com.example.prac.dto.data.IngredientDTO;
import com.example.prac.errorHandler.ResourceNotFoundException;
import com.example.prac.model.data.Dish;
import com.example.prac.model.data.Ingredient;
import com.example.prac.repository.data.DishRepository;
import com.example.prac.repository.data.IngredientRepository;
import com.example.prac.service.UserContextService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DishService {

    private final DishRepository dishRepository;
    private final IngredientRepository ingredientRepository;
    private final UserContextService userContextService;

    public DishResponseDTO createDish(DishDTO dishDTO, Long userId) {
        Dish dish = new Dish();
        dish.setName(dishDTO.getName());
        dish.setUserId(userId);
        dish.setInstructions(dishDTO.getInstructions());
        dish.setIngredients(mapIngredients(dishDTO.getIngredients()));
        dish = dishRepository.save(dish);
        return mapToResponse(dish);
    }

    public DishResponseDTO updateDish(Long dishId, DishDTO dishDTO) {

        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new IllegalArgumentException("Блюдо не найдено"));
        if (!Objects.equals(userContextService.getCurrentUserId(), dish.getUserId())){
            throw new RuntimeException("you have no permission for this operation!"); // TODO: exception
        }
        dish.setName(dishDTO.getName());
        dish.setInstructions(dishDTO.getInstructions());
        dish.setIngredients(mapIngredients(dishDTO.getIngredients()));
        dish = dishRepository.save(dish);
        return mapToResponse(dish);
    }

    public void deleteDish(Long dishId) {
        Dish dishToDelete = dishRepository.findById(dishId).orElseThrow(()-> new ResourceNotFoundException("dish not found"));
        if (!Objects.equals(dishToDelete.getUserId(), userContextService.getCurrentUserId())){
            throw new RuntimeException("You have no permission for this operation");
        }
        dishRepository.deleteById(dishId);
    }

    public List<DishResponseDTO> getAllDishes(Long userId) {
        return dishRepository.findAll().stream()
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
        response.setIngredients(
                dish.getIngredients().stream()
                        .map(ingredient -> new IngredientDTO(ingredient.getName(), ingredient.getUnit()))
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
