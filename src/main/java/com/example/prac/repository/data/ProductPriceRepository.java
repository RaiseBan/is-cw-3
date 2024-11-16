package com.example.prac.repository.data;

import com.example.prac.model.data.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductPriceRepository extends JpaRepository<ProductPrice, Long> {

}
