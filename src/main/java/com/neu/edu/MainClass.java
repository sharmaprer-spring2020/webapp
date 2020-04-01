package com.neu.edu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
//@EnableAsync
public class MainClass extends SpringBootServletInitializer {
//public class MainClass {

	public static void main(String[] args) {
		SpringApplication.run(MainClass.class, args);
	}
}
