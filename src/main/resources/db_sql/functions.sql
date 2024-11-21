-- Удаление старой версии процедуры add_dish_to_calendar
DROP PROCEDURE IF EXISTS add_dish_to_calendar;

-- Создание процедуры add_dish_to_calendar
CREATE OR REPLACE PROCEDURE add_dish_to_calendar(
    p_user_id INT,
    p_dish_id INT,
    p_calendar_date TIMESTAMP
)
    LANGUAGE plpgsql AS $$
BEGIN
    -- Проверяем существование календаря пользователя
    IF NOT EXISTS (SELECT 1 FROM calendar WHERE user_id = p_user_id) THEN
        RAISE EXCEPTION 'Календарь для пользователя % не найден', p_user_id;
    END IF;

    -- Проверяем, существует ли блюдо
    IF NOT EXISTS (SELECT 1 FROM dish WHERE dish_id = p_dish_id) THEN
        RAISE EXCEPTION 'Блюдо с ID % не найдено', p_dish_id;
    END IF;

    -- Добавляем запись в calendar_dish
    INSERT INTO calendar_dish (calendar_id, dish_id, time)
    SELECT c.calendar_id, p_dish_id, p_calendar_date
    FROM calendar c
    WHERE c.user_id = p_user_id;

    -- Добавляем ингредиенты в shopping_list, если их нет у пользователя
    INSERT INTO shopping_list (user_id, ingredient_id)
    SELECT p_user_id, di.ingredient_id
    FROM dish_ingredient di
             LEFT JOIN available_ingredients ai
                       ON di.ingredient_id = ai.ingredient_id AND ai.user_id = p_user_id
    WHERE di.dish_id = p_dish_id AND (ai.available IS NULL OR ai.available = FALSE);
END;
$$;








-- Удаление старой версии процедуры remove_dish_from_calendar
DROP PROCEDURE IF EXISTS remove_dish_from_calendar(integer, integer);

CREATE OR REPLACE PROCEDURE remove_dish_from_calendar(
    p_user_id BIGINT,
    p_dish_id BIGINT
)
    LANGUAGE plpgsql AS $$
BEGIN
    -- Удаляем блюдо из calendar_dish
    DELETE FROM calendar_dish
    WHERE dish_id = p_dish_id
      AND calendar_id IN (SELECT calendar_id FROM calendar WHERE user_id = p_user_id);

    -- Удаляем ингредиенты из списка покупок, которые связаны только с этим блюдом
    DELETE FROM shopping_list
    WHERE user_id = p_user_id
      AND ingredient_id IN (
        SELECT di.ingredient_id
        FROM dish_ingredient di
        WHERE di.dish_id = p_dish_id
    );
END;
$$;




-- Удаление старой версии функции update_ingredient_availability
DROP FUNCTION IF EXISTS update_ingredient_availability;

-- Создание функции update_ingredient_availability
CREATE OR REPLACE FUNCTION update_ingredient_availability(
    p_user_id INT,
    p_ingredient_id INT,
    p_available BOOLEAN
)
    RETURNS VOID AS $$
BEGIN
    UPDATE available_ingredients
    SET available = p_available
    WHERE user_id = p_user_id AND ingredient_id = p_ingredient_id;

    IF NOT FOUND THEN
        INSERT INTO available_ingredients (user_id, ingredient_id, available)
        VALUES (p_user_id, p_ingredient_id, p_available);
    END IF;
END;
$$ LANGUAGE plpgsql;


-- Удаление старой версии функции get_shopping_list
DROP FUNCTION IF EXISTS get_shopping_list;

-- Создание функции get_shopping_list
CREATE OR REPLACE FUNCTION get_shopping_list(
    p_user_id BIGINT,
    p_sort_by VARCHAR DEFAULT 'price',
    p_group_by_dish BOOLEAN DEFAULT FALSE,
    p_sort_order VARCHAR DEFAULT 'ASC'
)
    RETURNS TABLE(
                     ingredient_name VARCHAR,
                     store_name VARCHAR,
                     price FLOAT,
                     dish_name VARCHAR,
                     count INT
                 ) AS $$
BEGIN
    IF p_group_by_dish THEN
        -- Группировка по блюдам
        RETURN QUERY
            SELECT
                i.name AS ingredient_name,
                pp.store_name,
                CAST(pp.price AS double precision) AS price,
                d.name AS dish_name,
                CAST(COUNT(DISTINCT c.dish_id) AS INT) AS count -- Используем c.dish_id вместо c.id
            FROM calendar_dish c
                     JOIN calendar cal ON c.calendar_id = cal.calendar_id
                     JOIN dish d ON c.original_dish_id = d.dish_id -- Ссылка на оригинальное блюдо
                     JOIN dish_ingredient di ON di.dish_id = d.dish_id
                     JOIN ingredient i ON di.ingredient_id = i.ingredient_id
                     LEFT JOIN product_price pp ON pp.ingredient_id = i.ingredient_id
            WHERE cal.user_id = p_user_id
            GROUP BY i.name, pp.store_name, pp.price, d.name
            ORDER BY
                CASE
                    WHEN p_sort_by = 'price' AND p_sort_order = 'ASC' THEN pp.price
                    ELSE NULL
                    END ASC,
                CASE
                    WHEN p_sort_by = 'price' AND p_sort_order = 'DESC' THEN pp.price
                    ELSE NULL
                    END DESC,
                CASE
                    WHEN p_sort_by = 'name' AND p_sort_order = 'ASC' THEN i.name
                    ELSE NULL
                    END ASC,
                CASE
                    WHEN p_sort_by = 'name' AND p_sort_order = 'DESC' THEN i.name
                    ELSE NULL
                    END DESC;
    ELSE
        -- Без группировки, но с подсчётом общего количества
        RETURN QUERY
            SELECT
                i.name AS ingredient_name,
                pp.store_name,
                CAST(pp.price AS double precision) AS price,
                CAST(NULL AS VARCHAR) AS dish_name,
                CAST(COUNT(DISTINCT c.dish_id) AS INT) AS count -- Используем c.dish_id вместо c.id
            FROM calendar_dish c
                     JOIN calendar cal ON c.calendar_id = cal.calendar_id
                     JOIN dish d ON c.original_dish_id = d.dish_id -- Ссылка на оригинальное блюдо
                     JOIN dish_ingredient di ON di.dish_id = d.dish_id
                     JOIN ingredient i ON di.ingredient_id = i.ingredient_id
                     LEFT JOIN product_price pp ON pp.ingredient_id = i.ingredient_id
            WHERE cal.user_id = p_user_id
            GROUP BY i.name, pp.store_name, pp.price
            ORDER BY
                CASE
                    WHEN p_sort_by = 'price' AND p_sort_order = 'ASC' THEN pp.price
                    ELSE NULL
                    END ASC,
                CASE
                    WHEN p_sort_by = 'price' AND p_sort_order = 'DESC' THEN pp.price
                    ELSE NULL
                    END DESC,
                CASE
                    WHEN p_sort_by = 'name' AND p_sort_order = 'ASC' THEN i.name
                    ELSE NULL
                    END ASC,
                CASE
                    WHEN p_sort_by = 'name' AND p_sort_order = 'DESC' THEN i.name
                    ELSE NULL
                    END DESC;
    END IF;
END;
$$ LANGUAGE plpgsql;

