package pe.upn.pc5.service;

import pe.upn.pc5.model.Almacen;
import pe.upn.pc5.model.Movimiento;
import pe.upn.pc5.model.Producto;
import pe.upn.pc5.persistence.MovimientoRepository;
import pe.upn.pc5.persistence.RutasDatos;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

/**
 * Semilla de datos y demostración automática de REQ-01 a REQ-20
 * para generar evidencias de captura de pantalla.
 */
public class DemoService {

    private final InventarioService inventarioService;
    private final MovimientoRepository movimientoRepository;

    public DemoService(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
        this.movimientoRepository = new MovimientoRepository();
    }

    public void ejecutarSemillaYDemo() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║  SEMILLA DE DATOS Y DEMO AUTOMÁTICA — EVIDENCIAS REQ-01..20     ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println();

        try {
            RutasDatos.asegurarDirectorioData();
            prepararEntornoLimpio();

            demoReq01();
            demoReq02();
            demoReq03();
            demoReq04();
            demoReq05();
            demoReq06();
            demoReq07();
            demoReq08();
            demoReq09();
            demoReq10();
            demoReq11();
            demoReq12();
            demoReq13();
            demoReq14();
            demoReq15();
            demoReq16();
            demoReq17();
            demoReq18();
            demoReq19();
            demoReq20();

            System.out.println();
            System.out.println("=== DEMO COMPLETADA ===");
            System.out.println("Archivos generados en carpeta data/:");
            System.out.println(" - " + RutasDatos.PRODUCTOS_CSV.toAbsolutePath());
            System.out.println(" - " + RutasDatos.ALMACENES_CSV.toAbsolutePath());
            System.out.println(" - " + RutasDatos.MOVIMIENTOS_TXT.toAbsolutePath());
            System.out.println(" - " + RutasDatos.REPORTE_VALORACION_TXT.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("ERROR en la demo automática: " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    private void prepararEntornoLimpio() throws IOException {
        inventarioService.limpiarMemoria();
        if (Files.exists(RutasDatos.MOVIMIENTOS_TXT)) {
            Files.delete(RutasDatos.MOVIMIENTOS_TXT);
        }
        movimientoRepository.sobrescribir(List.of());
    }

    private void encabezado(String req, String titulo) {
        System.out.println();
        System.out.println("=== EVIDENCIA " + req + ": " + titulo + " ===");
    }

    private void demoReq01() {
        encabezado("REQ-01", "REGISTRAR NUEVO PRODUCTO");
        Producto p1 = inventarioService.registrarProducto("PROD01", "Laptop Lenovo ThinkPad", "Electrónica", 2899.90);
        Producto p2 = inventarioService.registrarProducto("PROD02", "Mouse Inalámbrico Logitech", "Accesorios", 79.50);
        Producto p3 = inventarioService.registrarProducto("PROD03", "Monitor Samsung 24\"", "Electrónica", 699.00);
        Producto p4 = inventarioService.registrarProducto("PROD04", "Teclado Mecánico RGB", "Accesorios", 249.90);
        Producto p5 = inventarioService.registrarProducto("PROD05", "Cable HDMI 2m", "Cables", 29.90);
        System.out.println("Productos registrados:");
        System.out.println(" - " + p1);
        System.out.println(" - " + p2);
        System.out.println(" - " + p3);
        System.out.println(" - " + p4);
        System.out.println(" - " + p5);
        try {
            inventarioService.registrarProducto("PROD01", "Duplicado", "X", 10);
            System.out.println("ERROR: debió rechazar código duplicado");
        } catch (IllegalArgumentException e) {
            System.out.println("Validación código único OK -> " + e.getMessage());
        }
        try {
            inventarioService.registrarProducto("PROD99", "Inválido", "X", -5);
            System.out.println("ERROR: debió rechazar precio negativo");
        } catch (IllegalArgumentException e) {
            System.out.println("Validación precio > 0 OK -> " + e.getMessage());
        }
    }

    private void demoReq02() {
        encabezado("REQ-02", "LISTAR PRODUCTOS ACTIVOS");
        System.out.print(inventarioService.formatearTablaProductos(inventarioService.listarProductosActivos()));
    }

    private void demoReq03() {
        encabezado("REQ-03", "ACTUALIZAR PRECIO UNITARIO");
        Producto actualizado = inventarioService.actualizarPrecio("PROD02", 85.00);
        System.out.println("Precio actualizado de PROD02: S/ " + String.format("%.2f", actualizado.getPrecioUnitario()));
        System.out.print(inventarioService.formatearTablaProductos(List.of(actualizado)));
    }

    private void demoReq04() {
        encabezado("REQ-04", "ELIMINAR PRODUCTO (BAJA LÓGICA)");
        Producto baja = inventarioService.eliminarProductoLogico("PROD05");
        System.out.println("Producto dado de baja lógica: " + baja.getCodigo() + " -> activo=" + baja.isActivo());
        System.out.println("Listado de activos (PROD05 no aparece):");
        System.out.print(inventarioService.formatearTablaProductos(inventarioService.listarProductosActivos()));
        System.out.println("Historial completo aún conserva PROD05:");
        System.out.print(inventarioService.formatearTablaProductos(inventarioService.getProductos()));
    }

    private void demoReq05() {
        encabezado("REQ-05", "REGISTRAR NUEVO ALMACÉN");
        Almacen a1 = inventarioService.registrarAlmacen("ALM01", "Almacén Central Lima", "Lima - Ate");
        Almacen a2 = inventarioService.registrarAlmacen("ALM02", "Almacén Norte", "Trujillo - La Libertad");
        Almacen a3 = inventarioService.registrarAlmacen("ALM03", "Almacén Sur", "Arequipa - Cerro Colorado");
        System.out.println("Almacenes registrados:");
        System.out.println(" - " + a1);
        System.out.println(" - " + a2);
        System.out.println(" - " + a3);
        try {
            inventarioService.registrarAlmacen("ALM01", "Duplicado", "X");
        } catch (IllegalArgumentException e) {
            System.out.println("Validación ID único OK -> " + e.getMessage());
        }
    }

    private void demoReq06() throws IOException {
        encabezado("REQ-06", "GUARDAR PRODUCTOS EN productos.csv");
        inventarioService.guardarProductos();
        System.out.println("Archivo guardado: " + RutasDatos.PRODUCTOS_CSV.toAbsolutePath());
        System.out.println("Bytes: " + Files.size(RutasDatos.PRODUCTOS_CSV));
    }

    private void demoReq07() throws IOException {
        encabezado("REQ-07", "CARGAR PRODUCTOS DESDE productos.csv");
        int cantidad = inventarioService.cargarProductos();
        System.out.println("Productos cargados en memoria: " + cantidad);
        System.out.print(inventarioService.formatearTablaProductos(inventarioService.getProductos()));
    }

    private void demoReq08() throws IOException {
        encabezado("REQ-08", "REGISTRAR ENTRADA DE STOCK");
        Movimiento m1 = inventarioService.registrarEntrada("PROD01", "ALM01", 15);
        Movimiento m2 = inventarioService.registrarEntrada("PROD02", "ALM01", 40);
        Movimiento m3 = inventarioService.registrarEntrada("PROD03", "ALM02", 12);
        Movimiento m4 = inventarioService.registrarEntrada("PROD04", "ALM02", 8);
        Movimiento m5 = inventarioService.registrarEntrada("PROD01", "ALM03", 5);
        System.out.println("Entradas registradas:");
        System.out.println(" - " + m1.aLineaArchivo());
        System.out.println(" - " + m2.aLineaArchivo());
        System.out.println(" - " + m3.aLineaArchivo());
        System.out.println(" - " + m4.aLineaArchivo());
        System.out.println(" - " + m5.aLineaArchivo());
    }

    private void demoReq09() throws IOException {
        encabezado("REQ-09", "REGISTRAR SALIDA DE STOCK");
        Movimiento salida = inventarioService.registrarSalida("PROD02", "ALM01", 10);
        System.out.println("Salida OK: " + salida.aLineaArchivo());
        try {
            inventarioService.registrarSalida("PROD02", "ALM01", 9999);
            System.out.println("ERROR: debió fallar por stock insuficiente");
        } catch (IllegalStateException e) {
            System.out.println("Validación stock insuficiente OK -> " + e.getMessage());
        }
    }

    private void demoReq10() throws IOException {
        encabezado("REQ-10", "TRANSFERENCIA MULTIALMACÉN");
        Movimiento t = inventarioService.transferir("PROD01", "ALM01", "ALM02", 4);
        System.out.println("Transferencia OK: " + t.aLineaArchivo());
        System.out.println("Stock PROD01 en ALM01: "
                + inventarioService.buscarAlmacen("ALM01").orElseThrow().getStockProducto("PROD01"));
        System.out.println("Stock PROD01 en ALM02: "
                + inventarioService.buscarAlmacen("ALM02").orElseThrow().getStockProducto("PROD01"));
    }

    private void demoReq11() throws IOException {
        encabezado("REQ-11", "BITÁCORA DE MOVIMIENTOS");
        System.out.print(inventarioService.formatearMovimientos(inventarioService.listarMovimientos()));
        System.out.println("Archivo: " + RutasDatos.MOVIMIENTOS_TXT.toAbsolutePath());
    }

    private void demoReq12() {
        encabezado("REQ-12", "VALORACIÓN TOTAL GLOBAL");
        System.out.print(inventarioService.formatearValoracionGlobal());
    }

    private void demoReq13() {
        encabezado("REQ-13", "VALORACIÓN POR ALMACÉN");
        System.out.print(inventarioService.formatearValoracionPorAlmacen("ALM01"));
        System.out.println();
        System.out.print(inventarioService.formatearValoracionPorAlmacen("ALM02"));
    }

    private void demoReq14() {
        encabezado("REQ-14", "BUSCAR PRODUCTO POR CÓDIGO EXACTO");
        System.out.print(inventarioService.detalleProductoConStock("PROD01"));
    }

    private void demoReq15() {
        encabezado("REQ-15", "BUSCAR PRODUCTOS POR NOMBRE PARCIAL");
        List<Producto> hallados = inventarioService.buscarPorNombreParcial("mouse");
        System.out.println("Búsqueda case-insensitive de 'mouse':");
        System.out.print(inventarioService.formatearTablaProductos(hallados));
        List<Producto> electronica = inventarioService.buscarPorNombreParcial("lenovo");
        System.out.println("Búsqueda de 'lenovo':");
        System.out.print(inventarioService.formatearTablaProductos(electronica));
    }

    private void demoReq16() {
        encabezado("REQ-16", "ALERTA DE STOCK MÍNIMO");
        System.out.print(inventarioService.formatearAlertasStockMinimo());
    }

    private void demoReq17() throws IOException {
        encabezado("REQ-17", "GUARDAR ALMACENES EN almacenes.csv");
        inventarioService.guardarAlmacenes();
        System.out.println("Archivo guardado: " + RutasDatos.ALMACENES_CSV.toAbsolutePath());
        System.out.println("Contenido:");
        Files.readAllLines(RutasDatos.ALMACENES_CSV).forEach(System.out::println);
    }

    private void demoReq18() throws IOException {
        encabezado("REQ-18", "CARGAR ALMACENES DESDE almacenes.csv");
        int cantidad = inventarioService.cargarAlmacenes();
        System.out.println("Almacenes cargados: " + cantidad);
        inventarioService.getAlmacenes().forEach(a -> System.out.println(" - " + a));
    }

    private void demoReq19() throws IOException {
        encabezado("REQ-19", "FILTRAR MOVIMIENTOS POR FECHA");
        String hoy = LocalDate.now().toString();
        List<Movimiento> filtrados = inventarioService.filtrarMovimientosPorFecha(hoy);
        System.out.println("Filtro por fecha " + hoy + " (" + filtrados.size() + " registros):");
        System.out.print(inventarioService.formatearMovimientos(filtrados));
    }

    private void demoReq20() throws IOException {
        encabezado("REQ-20", "EXPORTAR REPORTE DE VALORACIÓN");
        inventarioService.exportarReporteValoracion();
        inventarioService.guardarTodo();
        System.out.println("Reporte exportado a: " + RutasDatos.REPORTE_VALORACION_TXT.toAbsolutePath());
        System.out.println("--- Vista previa (primeras líneas) ---");
        List<String> lineas = Files.readAllLines(RutasDatos.REPORTE_VALORACION_TXT);
        int limite = Math.min(25, lineas.size());
        for (int i = 0; i < limite; i++) {
            System.out.println(lineas.get(i));
        }
        if (lineas.size() > limite) {
            System.out.println("... (" + (lineas.size() - limite) + " líneas más)");
        }
    }
}
