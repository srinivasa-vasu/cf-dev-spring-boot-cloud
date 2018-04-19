package io.pivotal.cnspringmsd;

import be.ordina.msdashboard.EnableMicroservicesDashboardServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableMicroservicesDashboardServer
public class CnSpringMsdApplication {

	public static void main(String[] args) {
		SpringApplication.run(CnSpringMsdApplication.class, args);
	}
}
