package com.pgManagement.roomservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RoomserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoomserviceApplication.class, args);
	}

}
