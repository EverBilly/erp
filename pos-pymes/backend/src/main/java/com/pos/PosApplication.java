package com.pos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class PosApplication {
    
    @GetMapping("/")
    public String home() {
        return "✅ Sistema POS funcionando! Ve a /login para autenticarte";
    }
    
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
    
    public static void main(String[] args) {
        SpringApplication.run(PosApplication.class, args);
    }
}