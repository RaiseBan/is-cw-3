package com.example.prac.dto.parser;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Subcategory {
    private String id;
    private String name;
    private List<Product> products;
}
