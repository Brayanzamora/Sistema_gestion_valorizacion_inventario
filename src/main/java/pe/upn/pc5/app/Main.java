package pe.upn.pc5.app;

import pe.upn.pc5.service.InventarioService;
import pe.upn.pc5.ui.MenuConsola;

/**
 * Punto de entrada del Sistema de Gestión y Valoración de Inventario Multialmacén.
 * PC5 — Técnicas de Programación Orientada a Objetos (Java 17).
 */
public class Main {

    public static void main(String[] args) {
        InventarioService inventarioService = new InventarioService();

        // Modo no interactivo para generar evidencias: java ... Main --demo
        if (args.length > 0 && "--demo".equalsIgnoreCase(args[0])) {
            new pe.upn.pc5.service.DemoService(inventarioService).ejecutarSemillaYDemo();
            return;
        }

        new MenuConsola(inventarioService).iniciar();
    }
}
