package com.irfancimen.redisdemo.service;

import com.irfancimen.redisdemo.component.RedisOperation;
import com.irfancimen.redisdemo.dto.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final RedisOperation redisOperation;

    private final Random random = new Random();

    @Cacheable(cacheNames = "demo-redis-cache", key = "#key")
    public Product getProduct(String key) {
        log.info("ProductService getProduct() is started with key {}...", key);
        return (Product) redisOperation.get(key);
    }

    @Cacheable(cacheNames = "demo-redis-cache", key = "'prod_list'")
    public List<Product> getAllProduct() {
        log.info("Inside of ProductService getAllProduct...");
        return (List<Product>) redisOperation.lRangeAll("prod_list");
    }

    public Product getProductFromDB(String key) {
        log.info("ProductService getProductFromDB() is started with key {}...", key);
        return (Product) redisOperation.get(key);
    }

    public String getBasic(String key) {
        return (String) redisOperation.get(key);
    }

    public String addDummy() {
        Product product = Product.builder()
                .id((long) (Math.random() * 100000000))
                .category("dummy category - " + generateRandomKey(5))
                .title("dummy title - " + generateRandomKey(10))
                .description("dummy desc - " + generateRandomKey(10))
                .unitPrice(BigDecimal.ONE)
                .stock(5)
                .build();
        String key =  generateRandomKey(5);
        redisOperation.set(key, product);
        return key;
    }

    private String generateRandomKey(int size) {
        return random.ints(97, 123)
                .limit(size)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }
}
