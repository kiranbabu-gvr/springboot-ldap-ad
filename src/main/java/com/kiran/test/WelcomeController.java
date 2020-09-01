package com.kiran.test;

import java.security.Principal;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {

	@GetMapping("/")
	public String index() {
		return "Welcome to the home page: " + SecurityContextHolder.getContext().getAuthentication().getName();
	}

	@GetMapping(value = "/userName")
	public String currentUserName(Principal principal) {
		return principal.getName();
	}

}
