package Evercare_CafeteriaApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableRetry

public class CafeteriaAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(CafeteriaAppApplication.class, args);
	}

}
