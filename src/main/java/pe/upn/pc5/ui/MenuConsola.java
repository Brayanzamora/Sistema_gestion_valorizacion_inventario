package pe.upn.pc5.ui;

import pe.upn.pc5.model.Almacen;
import pe.upn.pc5.model.Movimiento;
import pe.upn.pc5.model.Producto;
import pe.upn.pc5.persistence.RutasDatos;
import pe.upn.pc5.service.DemoService;
import pe.upn.pc5.service.InventarioService;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Menú interactivo de consola con los 20 requerimientos + opción 21 de demo.
 */
public class MenuConsola {

    private final InventarioService inventarioService;
    private final DemoService demoService;
    private final Scanner scanner;

    public MenuConsola(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
        this.demoService = new DemoService(inventarioService);
        this.scanner = new Scanner(System.in);
    }

    public void iniciar() {
        System.out.println("==============================================================");
        System.out.println("  SISTEMA DE GESTIÓN Y VALORACIÓN DE INVENTARIO MULTIALMACÉN");
        System.out.println("  Práctica de Campo 5 — Java 17 | POO + Archivos CSV/TXT");
        System.out.println("==============================================================");

        try {
            inventarioService.cargarTodoAlIniciar();
            System.out.println("Datos cargados: "
                    + inventarioService.getProductos().size() + " productos, "
                    + inventarioService.getAlmacenes().size() + " almacenes.");
        } catch (IOException e) {
            System.out.println("Advertencia al cargar datos iniciales: " + e.getMessage());
        }

        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            String opcion = leerTexto("Seleccione una opción: ");
            try {
                switch (opcion) {
                    case "1" -> ejecutarReq01();
                    case "2" -> ejecutarReq02();
                    case "3" -> ejecutarReq03();
                    case "4" -> ejecutarReq04();
                    case "5" -> ejecutarReq05();
                    case "6" -> ejecutarReq06();
                    case "7" -> ejecutarReq07();
                    case "8" -> ejecutarReq08();
                    case "9" -> ejecutarReq09();
                    case "10" -> ejecutarReq10();
                    case "11" -> ejecutarReq11();
                    case "12" -> ejecutarReq12();
                    case "13" -> ejecutarReq13();
                    case "14" -> ejecutarReq14();
                    case "15" -> ejecutarReq15();
                    case "16" -> ejecutarReq16();
                    case "17" -> ejecutarReq17();
                    case "18" -> ejecutarReq18();
                    case "19" -> ejecutarReq19();
                    case "20" -> ejecutarReq20();
                    case "21" -> demoService.ejecutarSemillaYDemo();
                    case "0" -> {
                        confirmarSalidaGuardando();
                        salir = true;
                    }
                    default -> System.out.println("Opción no válida. Intente nuevamente.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error de formato numérico: " + e.getMessage());
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Operación rechazada: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Error de E/S de archivos: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error inesperado: " + e.getMessage());
            }
            if (!salir) {
                System.out.println();
                System.out.print("Presione ENTER para continuar...");
                scanner.nextLine();
            }
        }
        System.out.println("Sesión finalizada. ¡Hasta pronto!");
    }

    private void mostrarMenu() {
        System.out.println();
        System.out.println("==================== MENÚ PRINCIPAL ====================");
        System.out.println(" 1. [REQ-01] Registrar nuevo producto");
        System.out.println(" 2. [REQ-02] Listar productos activos");
        System.out.println(" 3. [REQ-03] Actualizar precio unitario");
        System.out.println(" 4. [REQ-04] Eliminar producto (baja lógica)");
        System.out.println(" 5. [REQ-05] Registrar nuevo almacén");
        System.out.println(" 6. [REQ-06] Guardar productos en productos.csv");
        System.out.println(" 7. [REQ-07] Cargar productos desde productos.csv");
        System.out.println(" 8. [REQ-08] Registrar entrada de stock");
        System.out.println(" 9. [REQ-09] Registrar salida de stock");
        System.out.println("10. [REQ-10] Transferencia multialmacén");
        System.out.println("11. [REQ-11] Ver bitácora de movimientos");
        System.out.println("12. [REQ-12] Valoración total del inventario global");
        System.out.println("13. [REQ-13] Valoración filtrada por almacén");
        System.out.println("14. [REQ-14] Buscar producto por código exacto");
        System.out.println("15. [REQ-15] Buscar productos por nombre parcial");
        System.out.println("16. [REQ-16] Alerta de stock mínimo (< 10)");
        System.out.println("17. [REQ-17] Guardar almacenes en almacenes.csv");
        System.out.println("18. [REQ-18] Cargar almacenes desde almacenes.csv");
        System.out.println("19. [REQ-19] Filtrar movimientos por fecha");
        System.out.println("20. [REQ-20] Exportar reporte_valoracion.txt");
        System.out.println("21. Ejecutar Semilla de Datos y Demo Automática");
        System.out.println(" 0. Salir");
        System.out.println("========================================================");
    }

    private void ejecutarReq01() {
        System.out.println("--- REQ-01: Registrar nuevo producto ---");
        String codigo = leerTexto("Código: ");
        String nombre = leerTexto("Nombre: ");
        String categoria = leerTexto("Categoría: ");
        double precio = leerDouble("Precio unitario (> 0): ");
        Producto producto = inventarioService.registrarProducto(codigo, nombre, categoria, precio);
        System.out.println("Producto registrado: " + producto);
    }

    private void ejecutarReq02() {
        System.out.println("--- REQ-02: Listar productos activos ---");
        System.out.print(inventarioService.formatearTablaProductos(inventarioService.listarProductosActivos()));
    }

    private void ejecutarReq03() {
        System.out.println("--- REQ-03: Actualizar precio unitario ---");
        String codigo = leerTexto("Código del producto: ");
        double precio = leerDouble("Nuevo precio unitario (> 0): ");
        Producto producto = inventarioService.actualizarPrecio(codigo, precio);
        System.out.println("Precio actualizado: " + producto);
    }

    private void ejecutarReq04() {
        System.out.println("--- REQ-04: Eliminar producto (baja lógica) ---");
        String codigo = leerTexto("Código del producto: ");
        Producto producto = inventarioService.eliminarProductoLogico(codigo);
        System.out.println("Baja lógica aplicada. activo=" + producto.isActivo());
    }

    private void ejecutarReq05() {
        System.out.println("--- REQ-05: Registrar nuevo almacén ---");
        String id = leerTexto("ID almacén: ");
        String nombre = leerTexto("Nombre: ");
        String ubicacion = leerTexto("Ubicación: ");
        Almacen almacen = inventarioService.registrarAlmacen(id, nombre, ubicacion);
        System.out.println("Almacén registrado: " + almacen);
    }

    private void ejecutarReq06() throws IOException {
        System.out.println("--- REQ-06: Guardar productos ---");
        inventarioService.guardarProductos();
        System.out.println("Guardado en: " + RutasDatos.PRODUCTOS_CSV.toAbsolutePath());
    }

    private void ejecutarReq07() throws IOException {
        System.out.println("--- REQ-07: Cargar productos ---");
        int n = inventarioService.cargarProductos();
        System.out.println("Productos cargados: " + n);
        System.out.print(inventarioService.formatearTablaProductos(inventarioService.getProductos()));
    }

    private void ejecutarReq08() throws IOException {
        System.out.println("--- REQ-08: Entrada de stock ---");
        String codigo = leerTexto("Código producto: ");
        String idAlmacen = leerTexto("ID almacén: ");
        int cantidad = leerEntero("Cantidad: ");
        Movimiento m = inventarioService.registrarEntrada(codigo, idAlmacen, cantidad);
        System.out.println("Entrada registrada: " + m.aLineaArchivo());
    }

    private void ejecutarReq09() throws IOException {
        System.out.println("--- REQ-09: Salida de stock ---");
        String codigo = leerTexto("Código producto: ");
        String idAlmacen = leerTexto("ID almacén: ");
        int cantidad = leerEntero("Cantidad: ");
        Movimiento m = inventarioService.registrarSalida(codigo, idAlmacen, cantidad);
        System.out.println("Salida registrada: " + m.aLineaArchivo());
    }

    private void ejecutarReq10() throws IOException {
        System.out.println("--- REQ-10: Transferencia multialmacén ---");
        String codigo = leerTexto("Código producto: ");
        String origen = leerTexto("ID almacén origen: ");
        String destino = leerTexto("ID almacén destino: ");
        int cantidad = leerEntero("Cantidad: ");
        Movimiento m = inventarioService.transferir(codigo, origen, destino, cantidad);
        System.out.println("Transferencia registrada: " + m.aLineaArchivo());
    }

    private void ejecutarReq11() throws IOException {
        System.out.println("--- REQ-11: Bitácora de movimientos ---");
        List<Movimiento> movimientos = inventarioService.listarMovimientos();
        System.out.print(inventarioService.formatearMovimientos(movimientos));
    }

    private void ejecutarReq12() {
        System.out.println("--- REQ-12: Valoración global ---");
        System.out.print(inventarioService.formatearValoracionGlobal());
    }

    private void ejecutarReq13() {
        System.out.println("--- REQ-13: Valoración por almacén ---");
        String id = leerTexto("ID almacén: ");
        System.out.print(inventarioService.formatearValoracionPorAlmacen(id));
    }

    private void ejecutarReq14() {
        System.out.println("--- REQ-14: Buscar producto por código ---");
        String codigo = leerTexto("Código exacto: ");
        System.out.print(inventarioService.detalleProductoConStock(codigo));
    }

    private void ejecutarReq15() {
        System.out.println("--- REQ-15: Buscar por nombre parcial ---");
        String texto = leerTexto("Texto a buscar: ");
        List<Producto> hallados = inventarioService.buscarPorNombreParcial(texto);
        System.out.print(inventarioService.formatearTablaProductos(hallados));
    }

    private void ejecutarReq16() {
        System.out.println("--- REQ-16: Alerta de stock mínimo ---");
        System.out.print(inventarioService.formatearAlertasStockMinimo());
    }

    private void ejecutarReq17() throws IOException {
        System.out.println("--- REQ-17: Guardar almacenes ---");
        inventarioService.guardarAlmacenes();
        System.out.println("Guardado en: " + RutasDatos.ALMACENES_CSV.toAbsolutePath());
    }

    private void ejecutarReq18() throws IOException {
        System.out.println("--- REQ-18: Cargar almacenes ---");
        int n = inventarioService.cargarAlmacenes();
        System.out.println("Almacenes cargados: " + n);
        inventarioService.getAlmacenes().forEach(a -> System.out.println(" - " + a));
    }

    private void ejecutarReq19() throws IOException {
        System.out.println("--- REQ-19: Filtrar movimientos por fecha ---");
        String fecha = leerTexto("Fecha (YYYY-MM-DD): ");
        List<Movimiento> filtrados = inventarioService.filtrarMovimientosPorFecha(fecha);
        System.out.print(inventarioService.formatearMovimientos(filtrados));
    }

    private void ejecutarReq20() throws IOException {
        System.out.println("--- REQ-20: Exportar reporte de valoración ---");
        inventarioService.exportarReporteValoracion();
        inventarioService.guardarTodo();
        System.out.println("Exportado a: " + RutasDatos.REPORTE_VALORACION_TXT.toAbsolutePath());
    }

    private void confirmarSalidaGuardando() {
        String r = leerTexto("¿Desea guardar productos y almacenes antes de salir? (S/N): ");
        if (r.equalsIgnoreCase("S") || r.equalsIgnoreCase("SI") || r.equalsIgnoreCase("SÍ")) {
            try {
                inventarioService.guardarTodo();
                System.out.println("Datos guardados correctamente.");
            } catch (IOException e) {
                System.out.println("No se pudo guardar al salir: " + e.getMessage());
            }
        }
    }

    private String leerTexto(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int leerEntero(String prompt) {
        System.out.print(prompt);
        String raw = scanner.nextLine().trim();
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Se esperaba un entero y se recibió: '" + raw + "'");
        }
    }

    private double leerDouble(String prompt) {
        System.out.print(prompt);
        String raw = scanner.nextLine().trim().replace(',', '.');
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Se esperaba un número decimal y se recibió: '" + raw + "'");
        }
    }
}
