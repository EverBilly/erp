package com.pos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableScheduling
@RestController
public class PosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PosApplication.class, args);
    }

    // @GetMapping("/api/test")
    // public String test() {
    //     return "Test OK";
    // }
    
    @GetMapping("/api/auth/test")
    public String authTest() {
        return "Auth Test OK";
    }
}