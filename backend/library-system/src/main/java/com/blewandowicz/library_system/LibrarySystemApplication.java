package com.blewandowicz.library_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LibrarySystemApplication {

	/**
	 * Application entry point; bootstraps and starts the Spring Boot application.
	 *
	 * <p>Delegates to SpringApplication.run(...) using this class as the primary source
	 * of configuration. Any exceptions thrown during startup propagate to the caller.
	 *
	 * @param args command-line arguments forwarded to the Spring application
	 */
	public static void main(String[] args) {
		SpringApplication.run(LibrarySystemApplication.class, args);
	}

}
