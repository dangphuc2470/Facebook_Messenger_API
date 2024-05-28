package com.dnhp.facebook_demo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@RestController
public class FacebookDemoApplication {
	private static final Logger LOGGER = LoggerFactory.getLogger(FacebookDemoApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(FacebookDemoApplication.class, args);
	}

	@GetMapping("/test")
	public String test() {
		return "Test";
	}

	@GetMapping(value = "callback")
	public String callback(HttpServletRequest request, HttpServletResponse response) {
		LOGGER.info("callback:");
		String mode = request.getParameter("hub.mode");
		String challenge = request.getParameter("hub.challenge");
		String token = request.getParameter("hub.verify_token");
		LOGGER.info("mode:" + mode);
		LOGGER.info("challenge:" + challenge);
		LOGGER.info("token:" + token);
		if ("subscribe".equals(mode) && token != null && token.equals("dnhp_token")) {
			response.setHeader("content-type", "application/text");
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setStatus(200);
		} else {
			response.setHeader("content-type", "application/text");
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setStatus(403);
			challenge = "Invalid token";
		}
		return challenge;
	}

	@PostMapping(value = "callback")
	public String callback(@RequestBody String input) {
		LOGGER.info("requestBody:" + input);
		return "OK";
	}

	@GetMapping("/hello")
	public String sayHello(@RequestParam(value = "myName", defaultValue = "World") String name) {
		return String.format("Hello %s!", name);
	}

}
