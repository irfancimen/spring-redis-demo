package com.irfancimen.redisdemo.controller;

import com.irfancimen.redisdemo.dto.Product;
import com.irfancimen.redisdemo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Product> getProduct(@RequestParam String key) {
        return ResponseEntity.ok(productService.getProduct(key));
    }

    @GetMapping("/sentinel")
    public ResponseEntity<Product> getProductSentinel(@RequestParam String key) {
        return ResponseEntity.ok(productService.getProductFromDB(key));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProduct() {
        return ResponseEntity.ok(productService.getAllProduct());
    }

    @GetMapping("/basic")
    public String getBasic(@RequestParam String key) {
        return productService.getBasic(key);
    }

    @GetMapping("/addDummy")
    public String addDummy() {
        return productService.addDummy();
    }

}
