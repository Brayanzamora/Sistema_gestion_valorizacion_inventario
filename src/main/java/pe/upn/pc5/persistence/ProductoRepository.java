package pe.upn.pc5.persistence;

import pe.upn.pc5.model.Producto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia de productos en data/productos.csv
 * Formato: codigo,nombre,categoria,precioUnitario,activo
 */
public class ProductoRepository {

    private final Path archivo;

    public ProductoRepository() {
        this(RutasDatos.PRODUCTOS_CSV);
    }

    public ProductoRepository(Path archivo) {
        this.archivo = archivo;
    }

    public void guardar(List<Producto> productos) throws IOException {
        RutasDatos.asegurarDirectorioData();
        try (BufferedWriter writer = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
            writer.write("codigo,nombre,categoria,precioUnitario,activo");
            writer.newLine();
            for (Producto producto : productos) {
                writer.write(String.format("%s,%s,%s,%.2f,%s",
                        escapar(producto.getCodigo()),
                        escapar(producto.getNombre()),
                        escapar(producto.getCategoria()),
                        producto.getPrecioUnitario(),
                        producto.isActivo()));
                writer.newLine();
            }
        }
    }

    public List<Producto> cargar() throws IOException {
        List<Producto> productos = new ArrayList<>();
        if (!Files.exists(archivo)) {
            return productos;
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
                    if (linea.toLowerCase().startsWith("codigo")) {
                        continue;
                    }
                }
                productos.add(parsearLinea(linea));
            }
        }
        return productos;
    }

    private Producto parsearLinea(String linea) {
        String[] partes = dividirCsv(linea);
        if (partes.length < 5) {
            throw new IllegalArgumentException("Línea CSV de producto inválida: " + linea);
        }
        String codigo = partes[0].trim();
        String nombre = partes[1].trim();
        String categoria = partes[2].trim();
        double precio;
        try {
            precio = Double.parseDouble(partes[3].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Precio inválido en producto: " + linea, e);
        }
        boolean activo = Boolean.parseBoolean(partes[4].trim());
        return new Producto(codigo, nombre, categoria, precio, activo);
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
        if (valor.contains(",") || valor.contains("\"")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

    public Path getArchivo() {
        return archivo;
    }
}
