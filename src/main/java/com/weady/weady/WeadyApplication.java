package com.weady.weady;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(modifyOnCreate = false)
public class WeadyApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeadyApplication.class, args);
	}

}
