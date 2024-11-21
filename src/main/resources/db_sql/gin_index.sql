-- Индекс для поиска по имени в таблице parsed_product
CREATE INDEX IF NOT EXISTS idx_parsed_product_name
    ON parsed_product USING gin(to_tsvector('russian', name));

-- Индексы для таблицы Calendar
CREATE INDEX IF NOT EXISTS idx_calendar_user_id
    ON calendar(user_id);

-- Индексы для таблицы ShoppingList
CREATE INDEX IF NOT EXISTS idx_shoppinglist_user_id
    ON shopping_list(user_id);

-- Индексы для таблицы AvailableIngredients
CREATE INDEX IF NOT EXISTS idx_availableingredients_user_id
    ON available_ingredients(user_id);

-- Индекс для таблицы DishHistory
CREATE INDEX IF NOT EXISTS idx_dishhistory_user_id
    ON dish_history(user_id);

-- Индекс для поиска ингредиентов по имени
CREATE INDEX IF NOT EXISTS idx_ingredient_name
    ON ingredient(name);

-- Индексы для таблицы CalendarDish
CREATE INDEX IF NOT EXISTS idx_calendar_dish_id
    ON calendar_dish(dish_id);

-- Индекс для таблицы ProductPrice
CREATE INDEX IF NOT EXISTS idx_productprice_price
    ON product_price(price);

-- Индексы для связей в DishIngredient
CREATE INDEX IF NOT EXISTS idx_dishingredient_dish_id
    ON dish_ingredient(dish_id);

CREATE INDEX IF NOT EXISTS idx_dishingredient_ingredient_id
    ON dish_ingredient(ingredient_id);
