package pe.upn.pc5.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utilidad centralizada para rutas de archivos de datos del sistema.
 */
public final class RutasDatos {

    public static final Path DIRECTORIO_DATA = Paths.get("data");
    public static final Path PRODUCTOS_CSV = DIRECTORIO_DATA.resolve("productos.csv");
    public static final Path ALMACENES_CSV = DIRECTORIO_DATA.resolve("almacenes.csv");
    public static final Path MOVIMIENTOS_TXT = DIRECTORIO_DATA.resolve("movimientos.txt");
    public static final Path REPORTE_VALORACION_TXT = DIRECTORIO_DATA.resolve("reporte_valoracion.txt");

    private RutasDatos() {
    }

    public static void asegurarDirectorioData() throws IOException {
        if (!Files.exists(DIRECTORIO_DATA)) {
            Files.createDirectories(DIRECTORIO_DATA);
        }
    }
}
