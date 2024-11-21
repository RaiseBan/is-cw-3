package com.example.prac.repository.data;

import com.example.prac.model.data.ParsedProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * Находит продукты, соответствующие запросу, используя Full-Text Search.
     *
     * @param query Запрос для поиска.
     * @return Список продуктов, соответствующих запросу.
     */
    @Query(value = """
            SELECT * 
            FROM parsed_product 
            WHERE to_tsvector('russian', name) @@ plainto_tsquery(:query)
            """, nativeQuery = true)
    List<ParsedProduct> findByFullTextSearch(@Param("query") String query);
}
