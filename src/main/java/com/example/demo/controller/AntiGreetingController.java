package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AntiGreetingController {
    private String template = "fuck off ";
    @GetMapping("/anti-greeting")
    public AntiGreeting message(@RequestParam(value="name", defaultValue = "???") String name) {
        return new AntiGreeting("from AntiGreetingController", template + name);
    }
}
