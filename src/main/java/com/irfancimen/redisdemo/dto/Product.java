package com.irfancimen.redisdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product extends RedisObject implements Serializable {

    private Long id;
    private String category;
    private String description;
    private String title;
    private BigDecimal unitPrice;
    private int stock;

}
