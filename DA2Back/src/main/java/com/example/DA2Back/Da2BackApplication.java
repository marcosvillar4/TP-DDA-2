package com.example.DA2Back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Da2BackApplication {

	public static void main(String[] args) {
		SpringApplication.run(Da2BackApplication.class, args);
	}

}
