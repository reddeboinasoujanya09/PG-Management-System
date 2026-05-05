package com.pgManagement.bookingService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookingServiceApplication {

	public static void main(String[] args) {
		System.out.println("logging");
		SpringApplication.run(BookingServiceApplication.class, args);
	}

}
