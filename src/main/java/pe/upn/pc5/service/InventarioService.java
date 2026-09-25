package pe.upn.pc5.service;

import pe.upn.pc5.model.Almacen;
import pe.upn.pc5.model.Movimiento;
import pe.upn.pc5.model.Producto;
import pe.upn.pc5.model.TipoMovimiento;
import pe.upn.pc5.persistence.AlmacenRepository;
import pe.upn.pc5.persistence.MovimientoRepository;
import pe.upn.pc5.persistence.ProductoRepository;
import pe.upn.pc5.persistence.ReporteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación que orquesta el dominio y la persistencia.
 * Implementa la lógica de negocio de REQ-01 a REQ-20.
 */
public class InventarioService {

    public static final int STOCK_MINIMO_ALERTA = 10;

    private final List<Producto> productos = new ArrayList<>();
    private final List<Almacen> almacenes = new ArrayList<>();

    private final ProductoRepository productoRepository;
    private final AlmacenRepository almacenRepository;
    private final MovimientoRepository movimientoRepository;
    private final ReporteRepository reporteRepository;

    public InventarioService() {
        this(new ProductoRepository(), new AlmacenRepository(),
                new MovimientoRepository(), new ReporteRepository());
    }

    public InventarioService(ProductoRepository productoRepository,
                             AlmacenRepository almacenRepository,
                             MovimientoRepository movimientoRepository,
                             ReporteRepository reporteRepository) {
        this.productoRepository = productoRepository;
        this.almacenRepository = almacenRepository;
        this.movimientoRepository = movimientoRepository;
        this.reporteRepository = reporteRepository;
    }

    // ==================== REQ-01 ====================
    public Producto registrarProducto(String codigo, String nombre, String categoria, double precioUnitario) {
        String codigoNorm = normalizarCodigo(codigo);
        if (buscarProductoInterno(codigoNorm).isPresent()) {
            throw new IllegalArgumentException("Ya existe un producto con código: " + codigoNorm);
        }
        Producto producto = new Producto(codigoNorm, nombre, categoria, precioUnitario, true);
        productos.add(producto);
        return producto;
    }

    // ==================== REQ-02 ====================
    public List<Producto> listarProductosActivos() {
        return productos.stream()
                .filter(Producto::isActivo)
                .collect(Collectors.toList());
    }

    public String formatearTablaProductos(List<Producto> lista) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-10s %-28s %-16s %12s %-10s%n",
                "CODIGO", "NOMBRE", "CATEGORIA", "PRECIO", "ESTADO"));
        sb.append("-".repeat(80)).append(System.lineSeparator());
        if (lista.isEmpty()) {
            sb.append("(sin productos)").append(System.lineSeparator());
            return sb.toString();
        }
        for (Producto p : lista) {
            sb.append(String.format("%-10s %-28s %-16s %12.2f %-10s%n",
                    truncar(p.getCodigo(), 10),
                    truncar(p.getNombre(), 28),
                    truncar(p.getCategoria(), 16),
                    p.getPrecioUnitario(),
                    p.isActivo() ? "ACTIVO" : "INACTIVO"));
        }
        return sb.toString();
    }

    // ==================== REQ-03 ====================
    public Producto actualizarPrecio(String codigo, double nuevoPrecio) {
        Producto producto = obtenerProductoOFallar(codigo);
        producto.setPrecioUnitario(nuevoPrecio);
        return producto;
    }

    // ==================== REQ-04 ====================
    public Producto eliminarProductoLogico(String codigo) {
        Producto producto = obtenerProductoOFallar(codigo);
        if (!producto.isActivo()) {
            throw new IllegalStateException("El producto ya se encuentra inactivo: " + producto.getCodigo());
        }
        producto.darDeBaja();
        return producto;
    }

    // ==================== REQ-05 ====================
    public Almacen registrarAlmacen(String idAlmacen, String nombre, String ubicacion) {
        String id = normalizarCodigo(idAlmacen);
        if (buscarAlmacenInterno(id).isPresent()) {
            throw new IllegalArgumentException("Ya existe un almacén con ID: " + id);
        }
        Almacen almacen = new Almacen(id, nombre, ubicacion);
        almacenes.add(almacen);
        return almacen;
    }

    // ==================== REQ-06 ====================
    public void guardarProductos() throws IOException {
        productoRepository.guardar(new ArrayList<>(productos));
    }

    // ==================== REQ-07 ====================
    public int cargarProductos() throws IOException {
        List<Producto> cargados = productoRepository.cargar();
        productos.clear();
        productos.addAll(cargados);
        return productos.size();
    }

    // ==================== REQ-08 ====================
    public Movimiento registrarEntrada(String codigoProducto, String idAlmacen, int cantidad)
            throws IOException {
        Producto producto = obtenerProductoActivoOFallar(codigoProducto);
        Almacen almacen = obtenerAlmacenOFallar(idAlmacen);
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de entrada debe ser mayor que cero.");
        }
        almacen.incrementarStock(producto.getCodigo(), cantidad);
        Movimiento movimiento = new Movimiento(
                LocalDateTime.now(),
                TipoMovimiento.ENTRADA,
                producto.getCodigo(),
                cantidad,
                "-",
                almacen.getIdAlmacen(),
                "Entrada de stock");
        movimientoRepository.append(movimiento);
        return movimiento;
    }

    // ==================== REQ-09 ====================
    public Movimiento registrarSalida(String codigoProducto, String idAlmacen, int cantidad)
            throws IOException {
        Producto producto = obtenerProductoActivoOFallar(codigoProducto);
        Almacen almacen = obtenerAlmacenOFallar(idAlmacen);
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de salida debe ser mayor que cero.");
        }
        almacen.decrementarStock(producto.getCodigo(), cantidad);
        Movimiento movimiento = new Movimiento(
                LocalDateTime.now(),
                TipoMovimiento.SALIDA,
                producto.getCodigo(),
                cantidad,
                almacen.getIdAlmacen(),
                "-",
                "Salida de stock");
        movimientoRepository.append(movimiento);
        return movimiento;
    }

    // ==================== REQ-10 ====================
    public Movimiento transferir(String codigoProducto, String idOrigen, String idDestino, int cantidad)
            throws IOException {
        Producto producto = obtenerProductoActivoOFallar(codigoProducto);
        Almacen origen = obtenerAlmacenOFallar(idOrigen);
        Almacen destino = obtenerAlmacenOFallar(idDestino);
        if (origen.getIdAlmacen().equals(destino.getIdAlmacen())) {
            throw new IllegalArgumentException("El almacén origen y destino deben ser distintos.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de transferencia debe ser mayor que cero.");
        }
        origen.decrementarStock(producto.getCodigo(), cantidad);
        destino.incrementarStock(producto.getCodigo(), cantidad);
        Movimiento movimiento = new Movimiento(
                LocalDateTime.now(),
                TipoMovimiento.TRANSFERENCIA,
                producto.getCodigo(),
                cantidad,
                origen.getIdAlmacen(),
                destino.getIdAlmacen(),
                "Transferencia multialmacén");
        movimientoRepository.append(movimiento);
        return movimiento;
    }

    // ==================== REQ-11 ====================
    public List<Movimiento> listarMovimientos() throws IOException {
        return movimientoRepository.cargarTodos();
    }

    public String formatearMovimientos(List<Movimiento> movimientos) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-20s %-14s %-10s %8s %-10s %-10s %-24s%n",
                "FECHA/HORA", "TIPO", "PRODUCTO", "CANT", "ORIGEN", "DESTINO", "DETALLE"));
        sb.append("-".repeat(100)).append(System.lineSeparator());
        if (movimientos.isEmpty()) {
            sb.append("(sin movimientos)").append(System.lineSeparator());
            return sb.toString();
        }
        for (Movimiento m : movimientos) {
            sb.append(String.format("%-20s %-14s %-10s %8d %-10s %-10s %-24s%n",
                    m.getFechaHora().format(Movimiento.FORMATO_FECHA_HORA),
                    m.getTipo().name(),
                    truncar(m.getCodigoProducto(), 10),
                    m.getCantidad(),
                    truncar(m.getIdAlmacenOrigen(), 10),
                    truncar(m.getIdAlmacenDestino(), 10),
                    truncar(m.getDetalle(), 24)));
        }
        return sb.toString();
    }

    // ==================== REQ-12 ====================
    public Map<String, Object> calcularValoracionGlobal() {
        Map<String, Integer> stockGlobal = calcularStockGlobalPorProducto();
        List<Map<String, Object>> detalle = new ArrayList<>();
        double total = 0.0;
        for (Producto producto : listarProductosActivos()) {
            int stock = stockGlobal.getOrDefault(producto.getCodigo(), 0);
            double valor = stock * producto.getPrecioUnitario();
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("codigo", producto.getCodigo());
            fila.put("nombre", producto.getNombre());
            fila.put("stock", stock);
            fila.put("precio", producto.getPrecioUnitario());
            fila.put("valor", valor);
            detalle.add(fila);
            total += valor;
        }
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("detalle", detalle);
        resultado.put("total", total);
        return resultado;
    }

    public String formatearValoracionGlobal() {
        Map<String, Object> valoracion = calcularValoracionGlobal();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> detalle = (List<Map<String, Object>>) valoracion.get("detalle");
        double total = (double) valoracion.get("total");
        StringBuilder sb = new StringBuilder();
        sb.append("VALORACIÓN TOTAL DEL INVENTARIO GLOBAL").append(System.lineSeparator());
        sb.append(String.format("%-10s %-28s %8s %12s %14s%n",
                "CODIGO", "NOMBRE", "STOCK", "PRECIO", "VALOR"));
        sb.append("-".repeat(76)).append(System.lineSeparator());
        for (Map<String, Object> fila : detalle) {
            sb.append(String.format("%-10s %-28s %8d %12.2f %14.2f%n",
                    fila.get("codigo"),
                    truncar((String) fila.get("nombre"), 28),
                    fila.get("stock"),
                    fila.get("precio"),
                    fila.get("valor")));
        }
        sb.append("-".repeat(76)).append(System.lineSeparator());
        sb.append(String.format("TOTAL GLOBAL: S/ %.2f%n", total));
        return sb.toString();
    }

    // ==================== REQ-13 ====================
    public Map<String, Object> calcularValoracionPorAlmacen(String idAlmacen) {
        Almacen almacen = obtenerAlmacenOFallar(idAlmacen);
        List<Map<String, Object>> detalle = new ArrayList<>();
        double total = 0.0;
        for (Producto producto : listarProductosActivos()) {
            int stock = almacen.getStockProducto(producto.getCodigo());
            if (stock <= 0) {
                continue;
            }
            double valor = stock * producto.getPrecioUnitario();
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("codigo", producto.getCodigo());
            fila.put("nombre", producto.getNombre());
            fila.put("stock", stock);
            fila.put("precio", producto.getPrecioUnitario());
            fila.put("valor", valor);
            detalle.add(fila);
            total += valor;
        }
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("almacen", almacen);
        resultado.put("detalle", detalle);
        resultado.put("total", total);
        return resultado;
    }

    public String formatearValoracionPorAlmacen(String idAlmacen) {
        Map<String, Object> valoracion = calcularValoracionPorAlmacen(idAlmacen);
        Almacen almacen = (Almacen) valoracion.get("almacen");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> detalle = (List<Map<String, Object>>) valoracion.get("detalle");
        double total = (double) valoracion.get("total");
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("VALORACIÓN DEL ALMACÉN %s - %s (%s)%n",
                almacen.getIdAlmacen(), almacen.getNombre(), almacen.getUbicacion()));
        sb.append(String.format("%-10s %-28s %8s %12s %14s%n",
                "CODIGO", "NOMBRE", "STOCK", "PRECIO", "VALOR"));
        sb.append("-".repeat(76)).append(System.lineSeparator());
        if (detalle.isEmpty()) {
            sb.append("(sin stock en este almacén)").append(System.lineSeparator());
        } else {
            for (Map<String, Object> fila : detalle) {
                sb.append(String.format("%-10s %-28s %8d %12.2f %14.2f%n",
                        fila.get("codigo"),
                        truncar((String) fila.get("nombre"), 28),
                        fila.get("stock"),
                        fila.get("precio"),
                        fila.get("valor")));
            }
        }
        sb.append("-".repeat(76)).append(System.lineSeparator());
        sb.append(String.format("TOTAL ALMACÉN: S/ %.2f%n", total));
        return sb.toString();
    }

    // ==================== REQ-14 ====================
    public String detalleProductoConStock(String codigo) {
        Producto producto = obtenerProductoOFallar(codigo);
        StringBuilder sb = new StringBuilder();
        sb.append("DETALLE DE PRODUCTO").append(System.lineSeparator());
        sb.append("-".repeat(50)).append(System.lineSeparator());
        sb.append("Código     : ").append(producto.getCodigo()).append(System.lineSeparator());
        sb.append("Nombre     : ").append(producto.getNombre()).append(System.lineSeparator());
        sb.append("Categoría  : ").append(producto.getCategoria()).append(System.lineSeparator());
        sb.append("Precio     : S/ ").append(String.format("%.2f", producto.getPrecioUnitario()))
                .append(System.lineSeparator());
        sb.append("Estado     : ").append(producto.isActivo() ? "ACTIVO" : "INACTIVO")
                .append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("STOCK POR ALMACÉN").append(System.lineSeparator());
        sb.append(String.format("%-10s %-24s %8s%n", "ID", "NOMBRE", "STOCK"));
        sb.append("-".repeat(46)).append(System.lineSeparator());
        int total = 0;
        for (Almacen almacen : almacenes) {
            int stock = almacen.getStockProducto(producto.getCodigo());
            sb.append(String.format("%-10s %-24s %8d%n",
                    almacen.getIdAlmacen(), truncar(almacen.getNombre(), 24), stock));
            total += stock;
        }
        sb.append("-".repeat(46)).append(System.lineSeparator());
        sb.append(String.format("STOCK GLOBAL: %d%n", total));
        return sb.toString();
    }

    // ==================== REQ-15 ====================
    public List<Producto> buscarPorNombreParcial(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El texto de búsqueda no puede estar vacío.");
        }
        String needle = texto.trim().toLowerCase(Locale.ROOT);
        return productos.stream()
                .filter(p -> p.getNombre().toLowerCase(Locale.ROOT).contains(needle))
                .collect(Collectors.toList());
    }

    // ==================== REQ-16 ====================
    public List<Map<String, Object>> alertasStockMinimo() {
        Map<String, Integer> stockGlobal = calcularStockGlobalPorProducto();
        List<Map<String, Object>> alertas = new ArrayList<>();
        for (Producto producto : listarProductosActivos()) {
            int stock = stockGlobal.getOrDefault(producto.getCodigo(), 0);
            if (stock < STOCK_MINIMO_ALERTA) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("codigo", producto.getCodigo());
                fila.put("nombre", producto.getNombre());
                fila.put("stock", stock);
                alertas.add(fila);
            }
        }
        return alertas;
    }

    public String formatearAlertasStockMinimo() {
        List<Map<String, Object>> alertas = alertasStockMinimo();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("ALERTA DE STOCK MÍNIMO (< %d unidades globales)%n", STOCK_MINIMO_ALERTA));
        sb.append(String.format("%-10s %-28s %8s%n", "CODIGO", "NOMBRE", "STOCK"));
        sb.append("-".repeat(50)).append(System.lineSeparator());
        if (alertas.isEmpty()) {
            sb.append("(no hay productos bajo el umbral)").append(System.lineSeparator());
        } else {
            for (Map<String, Object> fila : alertas) {
                sb.append(String.format("%-10s %-28s %8d%n",
                        fila.get("codigo"),
                        truncar((String) fila.get("nombre"), 28),
                        fila.get("stock")));
            }
        }
        return sb.toString();
    }

    // ==================== REQ-17 ====================
    public void guardarAlmacenes() throws IOException {
        almacenRepository.guardar(new ArrayList<>(almacenes));
    }

    // ==================== REQ-18 ====================
    public int cargarAlmacenes() throws IOException {
        List<Almacen> cargados = almacenRepository.cargar();
        almacenes.clear();
        almacenes.addAll(cargados);
        return almacenes.size();
    }

    // ==================== REQ-19 ====================
    public List<Movimiento> filtrarMovimientosPorFecha(String fechaYyyyMmDd) throws IOException {
        if (fechaYyyyMmDd == null || !fechaYyyyMmDd.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("Formato de fecha inválido. Use YYYY-MM-DD.");
        }
        return movimientoRepository.filtrarPorFecha(fechaYyyyMmDd);
    }

    // ==================== REQ-20 ====================
    public String generarContenidoReporteValoracion() {
        StringBuilder sb = new StringBuilder();
        sb.append(formatearValoracionGlobal()).append(System.lineSeparator());
        for (Almacen almacen : almacenes) {
            sb.append(formatearValoracionPorAlmacen(almacen.getIdAlmacen()))
                    .append(System.lineSeparator());
        }
        sb.append(formatearAlertasStockMinimo()).append(System.lineSeparator());
        return sb.toString();
    }

    public void exportarReporteValoracion() throws IOException {
        reporteRepository.exportar(generarContenidoReporteValoracion());
    }

    // ==================== Utilidades ====================

    public void cargarTodoAlIniciar() throws IOException {
        cargarProductos();
        cargarAlmacenes();
    }

    public void guardarTodo() throws IOException {
        guardarProductos();
        guardarAlmacenes();
    }

    public List<Producto> getProductos() {
        return List.copyOf(productos);
    }

    public List<Almacen> getAlmacenes() {
        return List.copyOf(almacenes);
    }

    public void limpiarMemoria() {
        productos.clear();
        almacenes.clear();
    }

    public Map<String, Integer> calcularStockGlobalPorProducto() {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        for (Almacen almacen : almacenes) {
            for (Map.Entry<String, Integer> entry : almacen.getStocks().entrySet()) {
                mapa.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }
        return mapa;
    }

    public Optional<Producto> buscarProducto(String codigo) {
        return buscarProductoInterno(normalizarCodigo(codigo));
    }

    public Optional<Almacen> buscarAlmacen(String id) {
        return buscarAlmacenInterno(normalizarCodigo(id));
    }

    private Optional<Producto> buscarProductoInterno(String codigo) {
        return productos.stream()
                .filter(p -> p.getCodigo().equals(codigo))
                .findFirst();
    }

    private Optional<Almacen> buscarAlmacenInterno(String id) {
        return almacenes.stream()
                .filter(a -> a.getIdAlmacen().equals(id))
                .findFirst();
    }

    private Producto obtenerProductoOFallar(String codigo) {
        return buscarProductoInterno(normalizarCodigo(codigo))
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + codigo));
    }

    private Producto obtenerProductoActivoOFallar(String codigo) {
        Producto producto = obtenerProductoOFallar(codigo);
        if (!producto.isActivo()) {
            throw new IllegalStateException("El producto está inactivo y no admite movimientos: " + producto.getCodigo());
        }
        return producto;
    }

    private Almacen obtenerAlmacenOFallar(String id) {
        return buscarAlmacenInterno(normalizarCodigo(id))
                .orElseThrow(() -> new IllegalArgumentException("Almacén no encontrado: " + id));
    }

    private static String normalizarCodigo(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El código/ID no puede ser nulo ni vacío.");
        }
        return valor.trim().toUpperCase();
    }

    private static String truncar(String texto, int max) {
        if (texto == null) {
            return "";
        }
        if (texto.length() <= max) {
            return texto;
        }
        return texto.substring(0, max - 1) + "…";
    }
}
