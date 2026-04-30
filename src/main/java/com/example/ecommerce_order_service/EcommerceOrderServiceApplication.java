package com.example.ecommerce_order_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients
@EnableRetry
public class EcommerceOrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceOrderServiceApplication.class, args);
	}

}
