package pe.upn.pc5.persistence;

import pe.upn.pc5.model.Movimiento;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia secuencial de movimientos en data/movimientos.txt
 */
public class MovimientoRepository {

    private final Path archivo;

    public MovimientoRepository() {
        this(RutasDatos.MOVIMIENTOS_TXT);
    }

    public MovimientoRepository(Path archivo) {
        this.archivo = archivo;
    }

    public void append(Movimiento movimiento) throws IOException {
        RutasDatos.asegurarDirectorioData();
        boolean existe = Files.exists(archivo);
        try (BufferedWriter writer = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            if (!existe || Files.size(archivo) == 0) {
                writer.write("# fechaHora | tipo | producto | cantidad | origen | destino | detalle");
                writer.newLine();
            }
            writer.write(movimiento.aLineaArchivo());
            writer.newLine();
        }
    }

    public void sobrescribir(List<Movimiento> movimientos) throws IOException {
        RutasDatos.asegurarDirectorioData();
        try (BufferedWriter writer = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
            writer.write("# fechaHora | tipo | producto | cantidad | origen | destino | detalle");
            writer.newLine();
            for (Movimiento movimiento : movimientos) {
                writer.write(movimiento.aLineaArchivo());
                writer.newLine();
            }
        }
    }

    public List<Movimiento> cargarTodos() throws IOException {
        List<Movimiento> movimientos = new ArrayList<>();
        if (!Files.exists(archivo)) {
            return movimientos;
        }
        try (BufferedReader reader = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.isBlank() || linea.startsWith("#")) {
                    continue;
                }
                try {
                    Movimiento movimiento = Movimiento.desdeLineaArchivo(linea);
                    if (movimiento != null) {
                        movimientos.add(movimiento);
                    }
                } catch (IllegalArgumentException e) {
                    // Incluye NumberFormatException (subclase) por formatos numéricos inválidos
                    System.err.println("Advertencia: se omitió línea de movimiento inválida -> " + linea);
                }
            }
        }
        return movimientos;
    }

    public List<Movimiento> filtrarPorFecha(String fechaYyyyMmDd) throws IOException {
        List<Movimiento> filtrados = new ArrayList<>();
        for (Movimiento movimiento : cargarTodos()) {
            if (movimiento.getFechaSolo().equals(fechaYyyyMmDd)) {
                filtrados.add(movimiento);
            }
        }
        return filtrados;
    }

    public Path getArchivo() {
        return archivo;
    }
}
