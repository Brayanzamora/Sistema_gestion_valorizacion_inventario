package pe.upn.pc5.persistence;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Exportación del reporte de valoración a data/reporte_valoracion.txt
 */
public class ReporteRepository {

    private final Path archivo;

    public ReporteRepository() {
        this(RutasDatos.REPORTE_VALORACION_TXT);
    }

    public ReporteRepository(Path archivo) {
        this.archivo = archivo;
    }

    public void exportar(String contenido) throws IOException {
        RutasDatos.asegurarDirectorioData();
        try (BufferedWriter writer = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
            writer.write("REPORTE DE VALORACIÓN DE INVENTARIO MULTIALMACÉN");
            writer.newLine();
            writer.write("Generado: " + LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.newLine();
            writer.write("=".repeat(72));
            writer.newLine();
            writer.write(contenido);
            if (!contenido.endsWith(System.lineSeparator()) && !contenido.endsWith("\n")) {
                writer.newLine();
            }
        }
    }

    public Path getArchivo() {
        return archivo;
    }
}
