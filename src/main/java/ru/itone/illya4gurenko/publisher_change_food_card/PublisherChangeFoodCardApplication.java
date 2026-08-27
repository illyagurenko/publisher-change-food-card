package ru.itone.illya4gurenko.publisher_change_food_card;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PublisherChangeFoodCardApplication {

	public static void main(String[] args) {
		SpringApplication.run(PublisherChangeFoodCardApplication.class, args);
	}


}
