# Informe Técnico — Práctica de Campo 5

**Curso:** Técnicas de Programación Orientada a Objetos  
**Tema:** Diseño de Clases y Manejo de Archivos  
**Proyecto:** Sistema de Gestión y Valoración de Inventario Multialmacén  
**Lenguaje:** Java 17 (consola interactiva, sin frameworks externos)  
**Persistencia:** `data/productos.csv`, `data/almacenes.csv`, `data/movimientos.txt`, `data/reporte_valoracion.txt`

---

## 1. Descripción de las clases del dominio y decisiones de diseño

### 1.1 Arquitectura por capas

El sistema aplica separación de responsabilidades en cuatro paquetes:

| Paquete | Responsabilidad |
|---------|-----------------|
| `model` | Entidades del dominio con encapsulamiento y validaciones |
| `persistence` | Lectura/escritura de archivos con `Files`, `Path`, `BufferedReader` y `BufferedWriter` |
| `service` | Reglas de negocio, valoraciones, alertas y orquestación |
| `ui` / `app` | Menú de consola y punto de entrada |

Esta separación evita mezclar I/O de archivos con reglas de inventario y facilita evidenciar cada REQ en el menú (`REQ-01` … `REQ-20`).

### 1.2 Clases de dominio

#### `Producto`
- Atributos privados: `codigo`, `nombre`, `categoria`, `precioUnitario`, `activo`.
- Getters/setters con validación: código/nombre/categoría no vacíos; **precio > 0**.
- Baja lógica mediante `darDeBaja()` (`activo = false`) sin eliminar historial ni registros CSV.
- Identidad por código (`equals` / `hashCode`).

#### `Almacen`
- Atributos privados: `idAlmacen`, `nombre`, `ubicacion`, `stocks` (`Map<String, Integer>`).
- Relación 1:N con productos a través del mapa de saldos locales.
- Métodos `incrementarStock` / `decrementarStock` con control de **stock insuficiente**.
- Serialización propia: `codificarStocks()` / `decodificarStocks()` en formato `PROD01:20|PROD02:15`.

#### `Movimiento` y `TipoMovimiento`
- `Movimiento` es un registro de bitácora (fecha/hora, tipo, producto, cantidad, origen, destino, detalle).
- Enum `TipoMovimiento`: `ENTRADA`, `SALIDA`, `TRANSFERENCIA`.
- Conversión bidireccional línea ↔ objeto para `movimientos.txt`.

### 1.3 Persistencia

| Clase | Archivo | API usada |
|-------|---------|-----------|
| `ProductoRepository` | `productos.csv` | `Files.newBufferedWriter` / `Files.newBufferedReader` |
| `AlmacenRepository` | `almacenes.csv` | Idem |
| `MovimientoRepository` | `movimientos.txt` | Append con `StandardOpenOption.APPEND` |
| `ReporteRepository` | `reporte_valoracion.txt` | Escritura completa del reporte |
| `RutasDatos` | — | Centraliza `Path` y crea carpeta `data/` |

### 1.4 Encapsulamiento y relaciones

- Todos los atributos de dominio son `private`.
- Los mapas de stock se exponen solo como vista inmutable (`Collections.unmodifiableMap`).
- `InventarioService` **agrega** listas de `Producto` y `Almacen` y **usa** los repositorios (composición/dependencia).
- No hay acoplamiento de UI hacia archivos: `MenuConsola` solo llama al servicio.

---

## 2. Dificultades técnicas resueltas

### 2.1 Control de excepciones
- `IOException` capturada en el menú y en la demo; el programa no termina ante fallos de disco.
- `NumberFormatException` al parsear precios/cantidades desde entrada de usuario o CSV.
- `IllegalArgumentException` / `IllegalStateException` para códigos duplicados, precios inválidos y stock insuficiente.

### 2.2 Parseo CSV y stocks codificados
- Separación de campos CSV respetando posibles comillas.
- Cabeceras opcionales (`codigo,...` / `idAlmacen,...`) omitidas al cargar.
- Stocks embebidos en una sola columna con delimitadores `|` y `:`; errores de formato se reportan sin corromper el resto del sistema.

### 2.3 Sincronización memoria ↔ archivos
- Al iniciar se cargan productos y almacenes (`cargarTodoAlIniciar`).
- Movimientos se **append**ean inmediatamente a `movimientos.txt` en cada entrada/salida/transferencia.
- Guardado explícito de productos/almacenes (REQ-06 / REQ-17) y al exportar reporte / salir.
- La opción 21 limpia memoria y regenera evidencias de forma determinística.

### 2.4 Transferencia atómica en memoria
- REQ-10 valida origen ≠ destino, producto activo y saldo suficiente; luego decrementa origen y suma destino **antes** de registrar el movimiento, evitando estados inconsistentes en la misma operación.

---

## 3. Cómo ejecutar y generar evidencias

```bash
# Maven
mvn -q compile
mvn -q exec:java -Dexec.args="--demo"

# javac
mkdir -p out
javac -encoding UTF-8 -d out $(find src/main/java -name "*.java")
java -cp out pe.upn.pc5.app.Main --demo
```

Menú interactivo: opción **21** — *Ejecutar Semilla de Datos y Demo Automática*.

---

## 4. Matriz de trazabilidad de requerimientos

| ID Requerimiento | Historia de Usuario (Como… Quiero… Para…) | Criterios de Aceptación (Dado/Cuando/Entonces) | Clase/Método Java que lo implementa | Espacio para Evidencia |
|------------------|-------------------------------------------|------------------------------------------------|-------------------------------------|------------------------|
| **REQ-01** | Como operador de inventario, quiero registrar un nuevo producto con código único y precio válido, para mantener el catálogo actualizado. | **Dado** un código inexistente y precio > 0; **Cuando** registro el producto; **Entonces** se agrega en memoria. **Dado** código duplicado o precio ≤ 0; **Cuando** intento registrar; **Entonces** se rechaza con mensaje claro. | `InventarioService.registrarProducto` · `Producto.setPrecioUnitario` · `MenuConsola.ejecutarReq01` | ☐ Captura menú 1 / demo REQ-01 |
| **REQ-02** | Como operador, quiero listar productos activos en tabla alineada, para visualizar el catálogo vigente. | **Dado** productos activos e inactivos; **Cuando** elijo listar; **Entonces** solo aparecen activos en columnas alineadas. | `InventarioService.listarProductosActivos` · `formatearTablaProductos` · `MenuConsola.ejecutarReq02` | ☐ Captura menú 2 / demo REQ-02 |
| **REQ-03** | Como analista de precios, quiero actualizar el precio unitario buscando por código, para reflejar cambios de costo. | **Dado** un producto existente; **Cuando** ingreso nuevo precio > 0; **Entonces** se actualiza. **Dado** precio inválido; **Entonces** se rechaza. | `InventarioService.actualizarPrecio` · `MenuConsola.ejecutarReq03` | ☐ Captura menú 3 / demo REQ-03 |
| **REQ-04** | Como supervisor, quiero dar de baja lógica un producto, para ocultarlo sin perder historial. | **Dado** un producto activo; **Cuando** elimino lógicamente; **Entonces** `activo=false` y deja de listarse en activos, pero permanece en memoria/CSV. | `InventarioService.eliminarProductoLogico` · `Producto.darDeBaja` · `MenuConsola.ejecutarReq04` | ☐ Captura menú 4 / demo REQ-04 |
| **REQ-05** | Como administrador de logística, quiero registrar un almacén con ID único, nombre y ubicación, para operar stock por sede. | **Dado** ID nuevo y datos válidos; **Cuando** registro; **Entonces** se crea el almacén. **Dado** ID duplicado; **Entonces** se rechaza. | `InventarioService.registrarAlmacen` · `Almacen` setters · `MenuConsola.ejecutarReq05` | ☐ Captura menú 5 / demo REQ-05 |
| **REQ-06** | Como operador, quiero guardar la lista de productos en CSV, para persistir el catálogo. | **Dado** productos en memoria; **Cuando** guardo; **Entonces** se escribe `data/productos.csv` con cabecera y filas. | `InventarioService.guardarProductos` · `ProductoRepository.guardar` · `MenuConsola.ejecutarReq06` | ☐ Captura + archivo CSV |
| **REQ-07** | Como operador, quiero cargar productos desde CSV al iniciar y bajo demanda, para recuperar el estado guardado. | **Dado** un `productos.csv` válido; **Cuando** cargo; **Entonces** la memoria refleja el archivo. | `InventarioService.cargarProductos` · `ProductoRepository.cargar` · `MenuConsola.ejecutarReq07` · `cargarTodoAlIniciar` | ☐ Captura menú 7 / demo REQ-07 |
| **REQ-08** | Como almacenista, quiero registrar entrada de stock de un producto en un almacén, para incrementar el saldo local. | **Dado** producto activo y almacén existentes; **Cuando** registro entrada con cantidad > 0; **Entonces** aumenta stock y se escribe movimiento ENTRADA. | `InventarioService.registrarEntrada` · `Almacen.incrementarStock` · `MenuConsola.ejecutarReq08` | ☐ Captura menú 8 / demo REQ-08 |
| **REQ-09** | Como almacenista, quiero registrar salida validando saldo, para no permitir stock negativo. | **Dado** saldo suficiente; **Cuando** salgo stock; **Entonces** decrementa y registra SALIDA. **Dado** saldo insuficiente; **Entonces** se lanza error y no modifica stock. | `InventarioService.registrarSalida` · `Almacen.decrementarStock` · `MenuConsola.ejecutarReq09` | ☐ Captura menú 9 / demo REQ-09 |
| **REQ-10** | Como coordinador, quiero transferir stock entre almacenes en una sola operación validada, para redistribuir inventario. | **Dado** origen ≠ destino y saldo suficiente; **Cuando** transfiero; **Entonces** resta en origen, suma en destino y registra TRANSFERENCIA. | `InventarioService.transferir` · `MenuConsola.ejecutarReq10` | ☐ Captura menú 10 / demo REQ-10 |
| **REQ-11** | Como auditor, quiero que cada movimiento quede en bitácora y poder verla en consola, para trazabilidad. | **Dado** entradas/salidas/transferencias; **Cuando** consulto el log; **Entonces** veo líneas en consola leídas de `movimientos.txt`. | `MovimientoRepository.append` · `InventarioService.listarMovimientos` · `formatearMovimientos` · `MenuConsola.ejecutarReq11` | ☐ Captura menú 11 + TXT |
| **REQ-12** | Como gerente financiero, quiero la valoración total global (stock × precio), para conocer el valor del inventario. | **Dado** productos activos con stock; **Cuando** calculo valoración global; **Entonces** se muestra detalle por producto y total. | `InventarioService.calcularValoracionGlobal` · `formatearValoracionGlobal` · `MenuConsola.ejecutarReq12` | ☐ Captura menú 12 / demo REQ-12 |
| **REQ-13** | Como jefe de almacén, quiero la valoración filtrada por un almacén, para evaluar esa sede. | **Dado** un ID de almacén válido; **Cuando** consulto; **Entonces** veo solo stock/valor de ese almacén y su total. | `InventarioService.calcularValoracionPorAlmacen` · `formatearValoracionPorAlmacen` · `MenuConsola.ejecutarReq13` | ☐ Captura menú 13 / demo REQ-13 |
| **REQ-14** | Como operador, quiero buscar un producto por código exacto con stock desglosado, para ubicar existencias. | **Dado** un código existente; **Cuando** busco; **Entonces** veo detalle del producto y stock por cada almacén + total global. | `InventarioService.detalleProductoConStock` · `MenuConsola.ejecutarReq14` | ☐ Captura menú 14 / demo REQ-14 |
| **REQ-15** | Como operador, quiero buscar por coincidencia parcial de nombre (sin importar mayúsculas), para hallar productos similares. | **Dado** productos con nombres distintos; **Cuando** busco un fragmento; **Entonces** se listan coincidencias case-insensitive. | `InventarioService.buscarPorNombreParcial` · `MenuConsola.ejecutarReq15` | ☐ Captura menú 15 / demo REQ-15 |
| **REQ-16** | Como supervisor, quiero alertas de stock mínimo (< 10 unidades globales), para reponer a tiempo. | **Dado** productos activos; **Cuando** genero alerta; **Entonces** listan aquellos cuyo stock sumado < 10. | `InventarioService.alertasStockMinimo` · `formatearAlertasStockMinimo` · `MenuConsola.ejecutarReq16` | ☐ Captura menú 16 / demo REQ-16 |
| **REQ-17** | Como operador, quiero guardar almacenes y saldos locales en CSV, para persistir el stock por sede. | **Dado** almacenes en memoria; **Cuando** guardo; **Entonces** se escribe `data/almacenes.csv` con `stocksCodificados`. | `InventarioService.guardarAlmacenes` · `AlmacenRepository.guardar` · `Almacen.codificarStocks` · `MenuConsola.ejecutarReq17` | ☐ Captura + archivo CSV |
| **REQ-18** | Como operador, quiero cargar almacenes y saldos desde CSV, para restaurar el inventario local. | **Dado** un `almacenes.csv` válido; **Cuando** cargo; **Entonces** se reconstruyen almacenes y mapa de stocks. | `InventarioService.cargarAlmacenes` · `AlmacenRepository.cargar` · `Almacen.decodificarStocks` · `MenuConsola.ejecutarReq18` | ☐ Captura menú 18 / demo REQ-18 |
| **REQ-19** | Como auditor, quiero filtrar movimientos por fecha `YYYY-MM-DD`, para revisar la operación de un día. | **Dado** bitácora con varias fechas; **Cuando** filtro por una fecha válida; **Entonces** solo se muestran movimientos de ese día. Formato inválido → error controlado. | `InventarioService.filtrarMovimientosPorFecha` · `MovimientoRepository.filtrarPorFecha` · `MenuConsola.ejecutarReq19` | ☐ Captura menú 19 / demo REQ-19 |
| **REQ-20** | Como gerente, quiero exportar el reporte completo de valoración a TXT, para entregar evidencia financiera. | **Dado** inventario valorado; **Cuando** exporto; **Entonces** se genera `data/reporte_valoracion.txt` con valoración global, por almacén y alertas. | `InventarioService.exportarReporteValoracion` · `generarContenidoReporteValoracion` · `ReporteRepository.exportar` · `MenuConsola.ejecutarReq20` | ☐ Captura menú 20 + TXT |

### Funcionalidad extra

| ID | Descripción | Implementación | Evidencia |
|----|-------------|----------------|-----------|
| **Opción 21** | Semilla de datos + demo automática de REQ-01 a REQ-20 con encabezados `=== EVIDENCIA REQ-XX ===` y guardado de todos los archivos | `DemoService.ejecutarSemillaYDemo` · `Main` argumento `--demo` · `MenuConsola` opción 21 | ☐ Captura de consola completa / archivos en `data/` |

---

## 5. Conclusiones

El sistema cumple los 20 requerimientos funcionales con diseño POO en Java 17, encapsulamiento estricto, relaciones claras entre clases y persistencia exclusiva en CSV/TXT usando explícitamente `java.nio.file.Files`, `java.nio.file.Path`, `java.io.BufferedReader` y `java.io.BufferedWriter`. El control de errores evita caídas ante I/O inválida, formatos numéricos incorrectos, duplicados, precios no positivos y stock insuficiente. La opción 21 acelera la generación de evidencias para la rúbrica.
