package com.example.prac.repository.data;

import com.example.prac.model.data.ParsedProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ParsedProductRepository extends JpaRepository<ParsedProduct, Long> {

    ParsedProduct findParsedProductByName(String name);



    /**
     * Возвращает список всех имён продуктов.
     *
     * @return Список имён продуктов.
     */
    @Query("SELECT p.name FROM ParsedProduct p")
    List<String> findAllNames();
}
