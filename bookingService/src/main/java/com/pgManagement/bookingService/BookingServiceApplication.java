package com.pgManagement.bookingService;

import static java.lang.System.*;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookingServiceApplication {

	public static void main(String[] args) {
		out.println("Testing pipeline");
		SpringApplication.run(BookingServiceApplication.class, args);
	}

}
