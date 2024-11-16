package com.example.prac.dto.parser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
    private String name;
    private Prices prices;
}
