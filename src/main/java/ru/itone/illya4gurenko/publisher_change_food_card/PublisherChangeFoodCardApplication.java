package ru.itone.illya4gurenko.publisher_change_food_card;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.itone.illya4gurenko.publisher_change_food_card.service.MainManageFilesService;

@SpringBootApplication
public class PublisherChangeFoodCardApplication {

	public static void main(String[] args) {
		SpringApplication.run(PublisherChangeFoodCardApplication.class, args);
	}

	@Bean
	public CommandLineRunner run(MainManageFilesService mainManageFilesService) {
		return args -> {
			System.out.println("start");
			mainManageFilesService.manageFiles();
			System.out.println("end");
		};
	}
}
