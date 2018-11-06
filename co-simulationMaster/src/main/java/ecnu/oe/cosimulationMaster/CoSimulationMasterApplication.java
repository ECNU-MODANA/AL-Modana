package ecnu.oe.cosimulationMaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CoSimulationMasterApplication {

	public static void main(String[] args) {
		System.out.println(System.getProperty("user.dir"));
		SpringApplication.run(CoSimulationMasterApplication.class, args);
	}
}
