package com.example.utilisateurservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class UtilisateurServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UtilisateurServiceApplication.class, args);
	}

}
