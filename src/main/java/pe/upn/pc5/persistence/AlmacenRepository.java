package pe.upn.pc5.persistence;

import pe.upn.pc5.model.Almacen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Persistencia de almacenes en data/almacenes.csv
 * Formato: idAlmacen,nombre,ubicacion,stocksCodificados
 * Ejemplo stocks: PROD01:20|PROD02:15
 */
public class AlmacenRepository {

    private final Path archivo;

    public AlmacenRepository() {
        this(RutasDatos.ALMACENES_CSV);
    }

    public AlmacenRepository(Path archivo) {
        this.archivo = archivo;
    }

    public void guardar(List<Almacen> almacenes) throws IOException {
        RutasDatos.asegurarDirectorioData();
        try (BufferedWriter writer = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
            writer.write("idAlmacen,nombre,ubicacion,stocksCodificados");
            writer.newLine();
            for (Almacen almacen : almacenes) {
                writer.write(String.format("%s,%s,%s,%s",
                        escapar(almacen.getIdAlmacen()),
                        escapar(almacen.getNombre()),
                        escapar(almacen.getUbicacion()),
                        escapar(almacen.codificarStocks())));
                writer.newLine();
            }
        }
    }

    public List<Almacen> cargar() throws IOException {
        List<Almacen> almacenes = new ArrayList<>();
        if (!Files.exists(archivo)) {
            return almacenes;
        }
        try (BufferedReader reader = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            String linea;
            boolean primera = true;
            while ((linea = reader.readLine()) != null) {
                if (linea.isBlank()) {
                    continue;
                }
                if (primera) {
                    primera = false;
                    if (linea.toLowerCase().startsWith("idalmacen")) {
                        continue;
                    }
                }
                almacenes.add(parsearLinea(linea));
            }
        }
        return almacenes;
    }

    private Almacen parsearLinea(String linea) {
        String[] partes = dividirCsv(linea);
        if (partes.length < 3) {
            throw new IllegalArgumentException("Línea CSV de almacén inválida: " + linea);
        }
        String id = partes[0].trim();
        String nombre = partes[1].trim();
        String ubicacion = partes[2].trim();
        String stocksCodificados = partes.length >= 4 ? partes[3].trim() : "";
        try {
            Map<String, Integer> stocks = Almacen.decodificarStocks(stocksCodificados);
            return new Almacen(id, nombre, ubicacion, stocks);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cantidad de stock inválida en almacén: " + linea, e);
        }
    }

    private static String[] dividirCsv(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean enComillas = false;
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == '"') {
                if (enComillas && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    actual.append('"');
                    i++;
                } else {
                    enComillas = !enComillas;
                }
            } else if (c == ',' && !enComillas) {
                campos.add(actual.toString());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        campos.add(actual.toString());
        return campos.toArray(new String[0]);
    }

    private static String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        if (valor.contains(",") || valor.contains("\"")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

    public Path getArchivo() {
        return archivo;
    }
}
