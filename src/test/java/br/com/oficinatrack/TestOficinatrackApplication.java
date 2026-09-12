package br.com.oficinatrack;

import org.springframework.boot.SpringApplication;

public class TestOficinatrackApplication {

	public static void main(String[] args) {
		SpringApplication.from(OficinatrackApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
