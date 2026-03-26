# Como agregar un modulo nuevo

Guia paso a paso para crear un modulo nuevo en el ERP.
Ejemplo: modulo "Producto".

---

## Paso 1: Crear la estructura de paquetes

```
com.pos.producto/
  model/
    Producto.java
  repository/
    ProductoRepository.java
  service/
    ProductoService.java
  controller/
    ProductoController.java
  dto/
    CreateProductoRequest.java
    UpdateProductoRequest.java
    ProductoResponse.java
  exception/
    ProductoNotFoundException.java
```

**Por que esta estructura?** Cada modulo es independiente. Si manana queres
sacar el modulo de productos, borras la carpeta y listo. Esto es lo que hace
al ERP "modular".

---

## Paso 2: Crear la tabla en la base de datos

Crear archivo en `migrations/` (nunca modificar schema.sql):

```sql
CREATE TABLE IF NOT EXISTS productos (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    precio DECIMAL(10,2) NOT NULL DEFAULT 0,
    stock INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## Paso 3: Crear la entidad JPA

```java
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters y setters
}
```

**Cosas clave:**
- `FetchType.LAZY` en relaciones (evita cargar datos innecesarios)
- `@PrePersist` y `@PreUpdate` para timestamps automaticos
- `@Column` explicito en cada campo

---

## Paso 4: Crear el Repository

```java
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCode(String code);

    List<Producto> findByActiveTrue();

    boolean existsByCode(String code);
}
```

**Por que interface?** Spring Data JPA genera la implementacion automaticamente
a partir de los nombres de los metodos. `findByActiveTrue()` se traduce a
`SELECT * FROM productos WHERE active = true`.

---

## Paso 5: Crear los DTOs

**Request (lo que recibe el endpoint):**
```java
public class CreateProductoRequest {

    @NotBlank
    @Size(max = 50)
    private String code;

    @NotBlank
    @Size(max = 100)
    private String name;

    private String description;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal precio;

    @NotNull
    @Min(0)
    private Integer stock;

    // Getters y setters
}
```

**Response (lo que devuelve el endpoint):**
```java
public class ProductoResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private BigDecimal precio;
    private Integer stock;
    private boolean active;
    private LocalDateTime createdAt;

    // Getters y setters
}
```

**Por que DTOs?** Nunca exponer la entidad directamente porque:
1. El cliente podria ver campos internos (tenant, metadata)
2. No podes controlar que campos se envian/reciben
3. Si cambias la entidad, rompes la API

---

## Paso 6: Crear el Service

```java
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    // Inyeccion por constructor (NO usar @Autowired en campo)
    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> findAll() {
        return productoRepository.findByActiveTrue();
    }

    public Producto findById(Long id) {
        return productoRepository.findById(id)
            .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado con id: " + id));
    }

    @Transactional
    public Producto create(Producto producto) {
        if (productoRepository.existsByCode(producto.getCode())) {
            throw new RuntimeException("Ya existe un producto con codigo: " + producto.getCode());
        }
        return productoRepository.save(producto);
    }
}
```

**Cosas clave:**
- Inyeccion por constructor (no `@Autowired` en campos)
- `@Transactional` en metodos que modifican datos
- Excepciones descriptivas (nunca catch vacio)

---

## Paso 7: Crear el Controller

```java
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProductoResponse>> getAll() {
        List<ProductoResponse> response = productoService.findAll().stream()
            .map(this::convertToResponse)
            .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ProductoResponse> create(
            @Valid @RequestBody CreateProductoRequest request) {
        Producto producto = convertToEntity(request);
        Producto saved = productoService.create(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponse(saved));
    }

    private ProductoResponse convertToResponse(Producto p) {
        ProductoResponse r = new ProductoResponse();
        r.setId(p.getId());
        r.setCode(p.getCode());
        r.setName(p.getName());
        r.setPrecio(p.getPrecio());
        r.setStock(p.getStock());
        r.setActive(p.isActive());
        return r;
    }
}
```

**Cosas clave:**
- `@PreAuthorize` en TODOS los endpoints
- Controller solo recibe, convierte y delega (no logica de negocio)
- `ResponseEntity` con status codes correctos
- `@Valid` para activar validaciones del DTO

---

## Paso 8: Registrar en SecurityConfig

Agregar la ruta en `SecurityConfig.java` para que Spring Security permita el acceso:

```java
.requestMatchers("/api/productos/**").authenticated()
```

---

## Paso 9: Agregar menu en la BD

Insertar el menu para que aparezca en el sidebar:

```sql
INSERT INTO menus (tenant_id, name, path, icon, sort_order, visible)
VALUES (1, 'Productos', '/productos', 'inventory', 3, true);

-- Asignar a roles
INSERT INTO role_menus (rol_id, menu_id, active, can_view, can_edit, can_delete)
VALUES (1, (SELECT id FROM menus WHERE name = 'Productos'), true, true, true, true);
```

---

## Paso 10: Crear la pagina en el frontend

```
src/pages/productos/
  ProductosView.jsx    # Router wrapper
  ProductosList.jsx    # Listado con tabla
  ProductoForm.jsx     # Formulario crear/editar
```

Agregar la ruta en `App.js`:
```jsx
<Route path="/productos/*" element={<PrivateRoute><Layout /></PrivateRoute>}>
  <Route index element={<ProductosView />} />
</Route>
```

---

## Paso 11: Documentar la feature

Crear `docs/features/FT05-productos.md` con:
- Que hace
- Endpoints
- Request/Response
- Modelo de datos
- Estado (que funciona, que falta)

---

## Checklist final

- [ ] Tabla en BD con IF NOT EXISTS
- [ ] Entidad JPA con @PrePersist/@PreUpdate
- [ ] Repository con queries necesarias
- [ ] DTOs de Request y Response
- [ ] Service con logica de negocio
- [ ] Controller con @PreAuthorize en cada endpoint
- [ ] Excepcion custom del modulo
- [ ] Ruta en SecurityConfig
- [ ] Menu en BD asignado a roles
- [ ] Paginas en frontend (lista + formulario)
- [ ] Ruta en App.js
- [ ] Documentacion en docs/features/
