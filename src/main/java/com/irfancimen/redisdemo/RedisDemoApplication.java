package com.irfancimen.redisdemo;

import com.irfancimen.redisdemo.component.RedisOperation;
import com.irfancimen.redisdemo.dto.Product;
import com.irfancimen.redisdemo.service.ProductService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.util.UriUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class RedisDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisDemoApplication.class, args);
    }


    // @Bean
    CommandLineRunner runner(RedisOperation redisOperation) {
        return args -> {
            Product product1 = Product.builder()
                    .id(11L).category("Electronic").description("The best mobile phone in the world")
                    .unitPrice(new BigDecimal("12.5"))
                    .title("dirt cheap")
                    .stock(5)
                    .build();
            Product product2 = Product.builder()
                    .id(12L).category("Accessory").description("The second most beautiful necklaces")
                    .unitPrice(new BigDecimal("132.5"))
                    .title("Come All")
                    .stock(3)
                    .build();
            redisOperation.set("testKey", product1);
            redisOperation.setList("prod_list", Arrays.asList(product1, product2));
        };
    }

}
