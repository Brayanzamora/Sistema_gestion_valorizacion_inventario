package pe.upn.pc5.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Entidad de dominio que representa un almacén y el stock local por código de producto.
 * Relación 1:N con productos a través del mapa de stocks.
 */
public class Almacen {

    private String idAlmacen;
    private String nombre;
    private String ubicacion;
    private final Map<String, Integer> stocks;

    public Almacen(String idAlmacen, String nombre, String ubicacion) {
        this(idAlmacen, nombre, ubicacion, new LinkedHashMap<>());
    }

    public Almacen(String idAlmacen, String nombre, String ubicacion, Map<String, Integer> stocksIniciales) {
        setIdAlmacen(idAlmacen);
        setNombre(nombre);
        setUbicacion(ubicacion);
        this.stocks = new LinkedHashMap<>();
        if (stocksIniciales != null) {
            stocksIniciales.forEach(this::setStockProducto);
        }
    }

    public String getIdAlmacen() {
        return idAlmacen;
    }

    public void setIdAlmacen(String idAlmacen) {
        if (idAlmacen == null || idAlmacen.isBlank()) {
            throw new IllegalArgumentException("El ID del almacén no puede ser nulo ni vacío.");
        }
        this.idAlmacen = idAlmacen.trim().toUpperCase();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del almacén no puede ser nulo ni vacío.");
        }
        this.nombre = nombre.trim();
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        if (ubicacion == null || ubicacion.isBlank()) {
            throw new IllegalArgumentException("La ubicación del almacén no puede ser nula ni vacía.");
        }
        this.ubicacion = ubicacion.trim();
    }

    public Map<String, Integer> getStocks() {
        return Collections.unmodifiableMap(stocks);
    }

    public int getStockProducto(String codigoProducto) {
        if (codigoProducto == null || codigoProducto.isBlank()) {
            return 0;
        }
        return stocks.getOrDefault(codigoProducto.trim().toUpperCase(), 0);
    }

    public void setStockProducto(String codigoProducto, int cantidad) {
        if (codigoProducto == null || codigoProducto.isBlank()) {
            throw new IllegalArgumentException("El código de producto es obligatorio para asignar stock.");
        }
        if (cantidad < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        String codigo = codigoProducto.trim().toUpperCase();
        if (cantidad == 0) {
            stocks.remove(codigo);
        } else {
            stocks.put(codigo, cantidad);
        }
    }

    public void incrementarStock(String codigoProducto, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a incrementar debe ser mayor que cero.");
        }
        String codigo = codigoProducto.trim().toUpperCase();
        stocks.put(codigo, getStockProducto(codigo) + cantidad);
    }

    public void decrementarStock(String codigoProducto, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a decrementar debe ser mayor que cero.");
        }
        String codigo = codigoProducto.trim().toUpperCase();
        int actual = getStockProducto(codigo);
        if (actual < cantidad) {
            throw new IllegalStateException(String.format(
                    "Stock insuficiente en almacén %s para producto %s. Disponible: %d, solicitado: %d.",
                    idAlmacen, codigo, actual, cantidad));
        }
        setStockProducto(codigo, actual - cantidad);
    }

    /** Codifica stocks como PROD01:20|PROD02:15 para persistencia CSV. */
    public String codificarStocks() {
        if (stocks.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean primero = true;
        for (Map.Entry<String, Integer> entry : stocks.entrySet()) {
            if (!primero) {
                sb.append('|');
            }
            sb.append(entry.getKey()).append(':').append(entry.getValue());
            primero = false;
        }
        return sb.toString();
    }

    /** Decodifica la cadena PROD01:20|PROD02:15 hacia el mapa de stocks. */
    public static Map<String, Integer> decodificarStocks(String stocksCodificados) {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        if (stocksCodificados == null || stocksCodificados.isBlank()) {
            return mapa;
        }
        String[] pares = stocksCodificados.split("\\|");
        for (String par : pares) {
            if (par.isBlank()) {
                continue;
            }
            String[] partes = par.split(":");
            if (partes.length != 2) {
                throw new IllegalArgumentException("Formato de stock inválido: " + par);
            }
            String codigo = partes[0].trim().toUpperCase();
            int cantidad = Integer.parseInt(partes[1].trim());
            if (cantidad < 0) {
                throw new IllegalArgumentException("Cantidad negativa en stock codificado: " + par);
            }
            if (cantidad > 0) {
                mapa.put(codigo, cantidad);
            }
        }
        return mapa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Almacen almacen)) {
            return false;
        }
        return Objects.equals(idAlmacen, almacen.idAlmacen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAlmacen);
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | stocks=[%s]",
                idAlmacen, nombre, ubicacion, codificarStocks());
    }
}
