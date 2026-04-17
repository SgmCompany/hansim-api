package com.sgm.hansimapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class HansimApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(HansimApiApplication.class, args);
	}
}
