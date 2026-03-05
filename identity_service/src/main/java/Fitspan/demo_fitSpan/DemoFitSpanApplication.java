package Fitspan.demo_fitSpan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DemoFitSpanApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoFitSpanApplication.class, args);
	}

}
