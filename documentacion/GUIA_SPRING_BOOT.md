# Guía de Aprendizaje Spring Boot para el Proyecto POS-PYMES

Esta guía está diseñada para entender Spring Boot en el contexto de este proyecto ERP.

---

## 1. ¿Qué es Spring Boot?

Spring Boot es un framework de Java que simplifica la creación de aplicaciones. Piensa en él como un "kit de inicio" que ya tiene todo configurado para crear APIs web.

**Analogía:** Si Java fuera los ingredientes para cocinar, Spring Boot sería un kit de comida con todo pre-medido y listo para usar.

---

## 2. Estructura del Proyecto Backend

```
backend/
├── src/main/java/com/pos/      # Código fuente Java
│   ├── PosApplication.java     # Punto de entrada (como main())
│   ├── controllers/            # Reciben las peticiones HTTP
│   ├── services/               # Lógica de negocio
│   ├── models/                 # Entidades (tablas de BD)
│   ├── repositories/           # Acceso a base de datos
│   ├── dto/                    # Objetos de transferencia
│   ├── security/               # Autenticación JWT
│   └── config/                 # Configuraciones
├── src/main/resources/
│   └── application.properties  # Configuración de la app
├── pom.xml                     # Dependencias (como package.json)
└── target/                     # Archivos compilados
```

---

## 3. Conceptos Clave

### 3.1 Anotaciones (@)

En Spring, las anotaciones son "etiquetas" que le dicen al framework qué hacer con cada clase.

| Anotación | Significado | Ejemplo |
|-----------|-------------|---------|
| `@RestController` | Esta clase maneja peticiones HTTP | AuthController |
| `@Service` | Esta clase contiene lógica de negocio | AuthService |
| `@Repository` | Esta clase accede a la base de datos | UsuarioRepository |
| `@Entity` | Esta clase representa una tabla en la BD | Usuario |
| `@Autowired` | "Inyecta" una dependencia automáticamente | - |

### 3.2 El flujo de una petición

```
Cliente (Frontend)
    │
    ▼ POST /api/auth/login
┌─────────────────────────────────────────────────────────────────┐
│ BACKEND                                                         │
│                                                                 │
│   1. Controller     2. Service        3. Repository    4. BD   │
│   ┌──────────┐     ┌──────────┐      ┌──────────┐    ┌─────┐  │
│   │ Recibe   │────▶│ Procesa  │─────▶│ Consulta │───▶│ DB  │  │
│   │ petición │     │ lógica   │      │ datos    │    │     │  │
│   └──────────┘     └──────────┘      └──────────┘    └─────┘  │
│        │                                                  │     │
│        ◀──────────────────────────────────────────────────┘     │
│   5. Retorna JSON                                               │
└─────────────────────────────────────────────────────────────────┘
    │
    ▼
Cliente recibe respuesta
```

---

## 4. Archivos del Proyecto Explicados

### 4.1 PosApplication.java (Punto de entrada)

**Ubicación:** `src/main/java/com/pos/PosApplication.java`

```java
@SpringBootApplication  // Marca esta clase como la principal
public class PosApplication {
    public static void main(String[] args) {
        SpringApplication.run(PosApplication.class, args);
    }
}
```

**¿Qué hace?** Es como el `index.js` en Node.js. Inicia toda la aplicación.

---

### 4.2 Controllers (Controladores)

**Ubicación:** `src/main/java/com/pos/controllers/`

Los controladores reciben las peticiones HTTP y devuelven respuestas.

```java
@RestController                    // Esta clase es un controlador REST
@RequestMapping("/api/auth")       // Todas las rutas empiezan con /api/auth
public class AuthController {

    @Autowired                     // Spring inyecta el servicio automáticamente
    private AuthService authService;

    @PostMapping("/login")         // POST /api/auth/login
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // @RequestBody = el cuerpo JSON de la petición se convierte a LoginRequest

        LoginResponse response = authService.authenticateUser(request);
        return ResponseEntity.ok(response);  // Retorna 200 OK con el response
    }
}
```

**Equivalente en Express.js:**
```javascript
// Esto sería equivalente en Node.js/Express
app.post('/api/auth/login', (req, res) => {
    const response = authService.authenticateUser(req.body);
    res.json(response);
});
```

**Anotaciones de rutas HTTP:**
| Anotación | Método HTTP |
|-----------|-------------|
| `@GetMapping` | GET |
| `@PostMapping` | POST |
| `@PutMapping` | PUT |
| `@DeleteMapping` | DELETE |
| `@PatchMapping` | PATCH |

---

### 4.3 Services (Servicios)

**Ubicación:** `src/main/java/com/pos/services/`

Los servicios contienen la lógica de negocio. El controlador solo recibe/envía, el servicio procesa.

```java
@Service  // Marca esta clase como un servicio
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;  // Acceso a BD

    @Autowired
    private JwtTokenProvider tokenProvider;       // Generador de tokens

    public LoginResponse authenticateUser(LoginRequest request) {
        // 1. Buscar usuario en BD
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername());

        // 2. Validar contraseña
        // 3. Generar token JWT
        String token = tokenProvider.generateToken(usuario);

        // 4. Retornar respuesta
        return new LoginResponse(token, usuario);
    }
}
```

**¿Por qué separar Controller y Service?**
- Controller: solo maneja HTTP (recibir, responder)
- Service: lógica de negocio (validaciones, cálculos, reglas)
- Esto hace el código más organizado y testeable

---

### 4.4 Models/Entities (Modelos/Entidades)

**Ubicación:** `src/main/java/com/pos/models/`

Las entidades representan tablas en la base de datos.

```java
@Entity                          // Esta clase es una tabla en la BD
@Table(name = "usuarios")        // Nombre de la tabla
public class Usuario {

    @Id                          // Este campo es la llave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-incremento
    private Long id;

    @Column(unique = true, nullable = false)  // Columna única, no nula
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;
    private String nombre;
    private String apellido;

    @Column(name = "activo")     // Nombre de columna diferente al campo
    private Boolean activo = true;

    @ManyToMany(fetch = FetchType.EAGER)  // Relación muchos a muchos
    @JoinTable(
        name = "usuarios_roles",           // Tabla intermedia
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles = new HashSet<>();

    // Getters y Setters (o usar @Data de Lombok)
}
```

**Equivalente SQL:**
```sql
CREATE TABLE usuarios (
    id SERIAL PRIMARY KEY,
    username VARCHAR UNIQUE NOT NULL,
    password VARCHAR NOT NULL,
    email VARCHAR,
    nombre VARCHAR,
    apellido VARCHAR,
    activo BOOLEAN DEFAULT true
);
```

**Anotaciones de relaciones:**
| Anotación | Tipo de relación | Ejemplo |
|-----------|-----------------|---------|
| `@OneToOne` | 1 a 1 | Usuario - Perfil |
| `@OneToMany` | 1 a muchos | Cliente - Ventas |
| `@ManyToOne` | Muchos a 1 | Venta - Cliente |
| `@ManyToMany` | Muchos a muchos | Usuario - Roles |

---

### 4.5 Repositories (Repositorios)

**Ubicación:** `src/main/java/com/pos/repositories/`

Los repositorios permiten hacer consultas a la base de datos SIN escribir SQL.

```java
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // JpaRepository ya incluye: save(), findById(), findAll(), delete(), etc.

    // Spring genera el SQL automáticamente basándose en el nombre del método:

    Optional<Usuario> findByUsername(String username);
    // SELECT * FROM usuarios WHERE username = ?

    Optional<Usuario> findByEmail(String email);
    // SELECT * FROM usuarios WHERE email = ?

    Boolean existsByUsername(String username);
    // SELECT EXISTS(SELECT 1 FROM usuarios WHERE username = ?)

    List<Usuario> findByActivoTrue();
    // SELECT * FROM usuarios WHERE activo = true

    List<Usuario> findByNombreContaining(String nombre);
    // SELECT * FROM usuarios WHERE nombre LIKE '%nombre%'

    // También puedes escribir SQL personalizado:
    @Query("SELECT u FROM Usuario u WHERE u.nombre LIKE %:nombre%")
    List<Usuario> buscarPorNombre(@Param("nombre") String nombre);
}
```

**Métodos incluidos por defecto (heredados de JpaRepository):**
| Método | Descripción |
|--------|-------------|
| `save(entity)` | Guardar o actualizar |
| `findById(id)` | Buscar por ID |
| `findAll()` | Obtener todos |
| `deleteById(id)` | Eliminar por ID |
| `count()` | Contar registros |
| `existsById(id)` | Verificar si existe |

---

### 4.6 DTOs (Data Transfer Objects)

**Ubicación:** `src/main/java/com/pos/dto/`

Los DTOs son objetos simples para transferir datos. Evitan exponer las entidades directamente.

```java
// Lo que recibe el endpoint de login
public class LoginRequest {
    private String username;
    private String password;

    // Getters y Setters
}

// Lo que retorna el endpoint de login
public class LoginResponse {
    private String token;
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private List<String> permisos;

    // Constructor, Getters y Setters
}
```

**¿Por qué usar DTOs?**
- No expones la entidad completa (ej: no envías el password)
- Puedes combinar datos de varias entidades
- Desacoplas la API de la base de datos

---

### 4.7 application.properties (Configuración)

**Ubicación:** `src/main/resources/application.properties`

```properties
# Puerto del servidor
server.port=8080

# Conexión a base de datos
spring.datasource.url=jdbc:postgresql://localhost:5432/pos_db
spring.datasource.username=pos_user
spring.datasource.password=pos_password

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
# validate = solo valida que las tablas existan
# update = crea/actualiza tablas automáticamente
# create = elimina y crea tablas cada vez
# create-drop = crea al iniciar, elimina al cerrar

# Configuración JWT personalizada
app.jwt.secret=MiClaveSecreta...
app.jwt.expiration-in-ms=86400000
```

---

### 4.8 pom.xml (Dependencias)

**Ubicación:** `pom.xml`

Es como el `package.json` de Node.js. Define las dependencias del proyecto.

```xml
<dependencies>
    <!-- Spring Boot Web (para crear APIs REST) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Data JPA (para base de datos) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- PostgreSQL Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>

    <!-- Spring Security (autenticación) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>

    <!-- Lombok (reduce código repetitivo) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

---

## 5. Lombok (Reducción de código)

Lombok genera automáticamente getters, setters, constructores, etc.

```java
// SIN Lombok (mucho código)
public class Usuario {
    private Long id;
    private String nombre;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Usuario() {}
    public Usuario(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
}

// CON Lombok (mismo resultado)
@Data                    // Genera getters, setters, toString, equals, hashCode
@NoArgsConstructor       // Constructor vacío
@AllArgsConstructor      // Constructor con todos los campos
public class Usuario {
    private Long id;
    private String nombre;
}
```

**Anotaciones Lombok más usadas:**
| Anotación | Genera |
|-----------|--------|
| `@Getter` | Getters para todos los campos |
| `@Setter` | Setters para todos los campos |
| `@Data` | Getters, Setters, toString, equals, hashCode |
| `@NoArgsConstructor` | Constructor sin argumentos |
| `@AllArgsConstructor` | Constructor con todos los argumentos |
| `@Builder` | Patrón builder para crear objetos |

---

## 6. Spring Security y JWT

### 6.1 ¿Cómo funciona la seguridad?

```
Petición HTTP
    │
    ▼
┌─────────────────────────────────────────────────┐
│ JwtAuthenticationFilter                         │
│ - Extrae token del header "Authorization"       │
│ - Valida el token                               │
│ - Carga el usuario en el contexto de seguridad  │
└─────────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────────┐
│ SecurityConfig                                  │
│ - ¿La ruta es pública? → Permitir              │
│ - ¿La ruta requiere auth? → Verificar token    │
└─────────────────────────────────────────────────┘
    │
    ▼
Controller (si pasó la seguridad)
```

### 6.2 Configuración de Seguridad

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())           // Deshabilitar CSRF (API REST)
            .sessionManagement(session ->
                session.sessionCreationPolicy(STATELESS))  // Sin sesiones
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()  // Rutas públicas
                .requestMatchers("/api/public/**").permitAll()
                .anyRequest().authenticated()                  // El resto requiere auth
            );

        return http.build();
    }
}
```

### 6.3 JWT Token Provider

```java
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")      // Lee del application.properties
    private String jwtSecret;

    @Value("${app.jwt.expiration-in-ms}")
    private int jwtExpirationMs;

    // Genera un token para el usuario
    public String generateToken(UserPrincipal userPrincipal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
            .setSubject(Long.toString(userPrincipal.getId()))
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();
    }

    // Valida que el token sea correcto
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
```

---

## 7. Comandos Maven Útiles

Maven es la herramienta que compila y gestiona el proyecto (como npm para Java).

```bash
# Compilar el proyecto
mvn compile

# Ejecutar la aplicación
mvn spring-boot:run

# Compilar y crear JAR (sin tests)
mvn clean package -DskipTests

# Ejecutar tests
mvn test

# Limpiar archivos compilados
mvn clean

# Ver árbol de dependencias
mvn dependency:tree
```

---

## 8. Cómo Agregar Nuevas Funcionalidades

### Ejemplo: Crear CRUD de Productos

#### Paso 1: Crear la Entidad

```java
// src/main/java/com/pos/models/Producto.java
@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private BigDecimal precio;

    private Integer stock = 0;

    private String categoria;
}
```

#### Paso 2: Crear el Repository

```java
// src/main/java/com/pos/repositories/ProductoRepository.java
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigo(String codigo);

    List<Producto> findByCategoria(String categoria);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    List<Producto> findByStockLessThan(Integer cantidad);
}
```

#### Paso 3: Crear el Service

```java
// src/main/java/com/pos/services/ProductoService.java
@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    public Optional<Producto> findById(Long id) {
        return productoRepository.findById(id);
    }

    public Producto save(Producto producto) {
        return productoRepository.save(producto);
    }

    public void deleteById(Long id) {
        productoRepository.deleteById(id);
    }

    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }
}
```

#### Paso 4: Crear el Controller

```java
// src/main/java/com/pos/controllers/ProductoController.java
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // GET /api/productos
    @GetMapping
    public List<Producto> getAll() {
        return productoService.findAll();
    }

    // GET /api/productos/5
    @GetMapping("/{id}")
    public ResponseEntity<Producto> getById(@PathVariable Long id) {
        return productoService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/productos
    @PostMapping
    public Producto create(@RequestBody Producto producto) {
        return productoService.save(producto);
    }

    // PUT /api/productos/5
    @PutMapping("/{id}")
    public ResponseEntity<Producto> update(@PathVariable Long id,
                                           @RequestBody Producto producto) {
        return productoService.findById(id)
            .map(existing -> {
                producto.setId(id);
                return ResponseEntity.ok(productoService.save(producto));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/productos/5
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (productoService.findById(id).isPresent()) {
            productoService.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // GET /api/productos/buscar?nombre=laptop
    @GetMapping("/buscar")
    public List<Producto> buscar(@RequestParam String nombre) {
        return productoService.buscarPorNombre(nombre);
    }
}
```

---

## 9. Errores Comunes y Soluciones

### Error: "No qualifying bean"
**Causa:** Spring no encuentra una clase que necesita inyectar.
**Solución:** Verifica que la clase tenga la anotación correcta (@Service, @Repository, @Component).

### Error: "Table doesn't exist"
**Causa:** Hibernate no encuentra la tabla en la BD.
**Solución:**
- Cambia `spring.jpa.hibernate.ddl-auto=update` para que cree las tablas.
- O ejecuta el script SQL manualmente.

### Error: "Failed to load ApplicationContext"
**Causa:** Error en configuración o dependencias.
**Solución:** Revisa los logs completos, usualmente indica qué bean o configuración falló.

### Error: 403 Forbidden
**Causa:** La ruta requiere autenticación.
**Solución:** Agrega la ruta a las permitidas en SecurityConfig o envía el token JWT.

### Error: "Could not write JSON"
**Causa:** Error al serializar un objeto a JSON (posible referencia circular).
**Solución:** Usa `@JsonIgnore` en relaciones bidireccionales o usa DTOs.

---

## 10. Recursos de Aprendizaje

### Documentación Oficial
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring Security](https://docs.spring.io/spring-security/reference/)

### Tutoriales Recomendados
- [Baeldung](https://www.baeldung.com/) - Tutoriales de Spring en inglés
- [Spring Guides](https://spring.io/guides) - Guías oficiales paso a paso

### Herramientas
- **IntelliJ IDEA** - IDE recomendado para Java/Spring
- **Postman** - Para probar los endpoints
- **DBeaver** - Cliente de base de datos

---

## 11. Resumen Rápido

| Concepto | Archivo | Función |
|----------|---------|---------|
| Punto de entrada | PosApplication.java | Inicia la app |
| Recibe HTTP | Controller | Endpoints REST |
| Lógica de negocio | Service | Procesa datos |
| Acceso a BD | Repository | Consultas SQL |
| Tabla de BD | Entity/Model | Mapeo objeto-relacional |
| Transferir datos | DTO | Request/Response |
| Configuración | application.properties | Variables |
| Dependencias | pom.xml | Librerías |

**Flujo:** Request → Controller → Service → Repository → BD → Response

---

*Guía creada para el proyecto POS-PYMES - Diciembre 2024*
