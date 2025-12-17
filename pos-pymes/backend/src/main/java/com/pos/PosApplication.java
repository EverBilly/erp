package com.pos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController  // ⚠️ Agrega esta anotación
public class PosApplication {
    
    // ⚠️ Agrega este endpoint para la ruta raíz
    @GetMapping("/")
    public String home() {
        return """
               <!DOCTYPE html>
               <html>
               <head>
                   <title>✅ Sistema POS - Backend Funcionando</title>
                   <style>
                       body { font-family: Arial, sans-serif; margin: 40px; }
                       .container { max-width: 800px; margin: 0 auto; }
                       .success { color: green; font-size: 24px; }
                       .endpoints { margin-top: 30px; }
                       ul { list-style-type: none; padding: 0; }
                       li { margin: 10px 0; padding: 10px; background: #f5f5f5; }
                       a { color: #0066cc; text-decoration: none; }
                   </style>
               </head>
               <body>
                   <div class="container">
                       <h1 class="success">✅ Sistema POS Backend FUNCIONANDO</h1>
                       <p>Fecha: %s</p>
                       <p>Spring Boot 3.1.5 | Java 17 | PostgreSQL</p>
                       
                       <div class="endpoints">
                           <h2>Endpoints disponibles:</h2>
                           <ul>
                               <li><a href="/health">/health</a> - Estado del sistema</li>
                               <li><a href="/api/productos">/api/productos</a> - Productos (próximamente)</li>
                               <li><a href="/api/ventas">/api/ventas</a> - Ventas (próximamente)</li>
                               <li><a href="/h2-console">/h2-console</a> - Consola H2 (si la agregas)</li>
                           </ul>
                       </div>
                       
                       <div style="margin-top: 40px;">
                           <h3>Otros servicios:</h3>
                           <ul>
                               <li><strong>Frontend React:</strong> <a href="http://localhost:3000" target="_blank">http://localhost:3000</a></li>
                               <li><strong>pgAdmin:</strong> <a href="http://localhost:5050" target="_blank">http://localhost:5050</a></li>
                               <li><strong>PostgreSQL:</strong> localhost:5432</li>
                           </ul>
                       </div>
                   </div>
               </body>
               </html>
               """.formatted(java.time.LocalDateTime.now());
    }
    
    // ⚠️ Agrega también un endpoint de health check
    @GetMapping("/health")
    public String health() {
        return """
               {
                 "status": "UP",
                 "service": "pos-backend",
                 "timestamp": "%s",
                 "database": "PostgreSQL"
               }
               """.formatted(java.time.Instant.now());
    }
    
    public static void main(String[] args) {
        SpringApplication.run(PosApplication.class, args);
    }
}