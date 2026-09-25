package pe.upn.pc5.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Registro inmutable de bitácora de un movimiento de inventario.
 */
public class Movimiento {

    public static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LocalDateTime fechaHora;
    private final TipoMovimiento tipo;
    private final String codigoProducto;
    private final int cantidad;
    private final String idAlmacenOrigen;
    private final String idAlmacenDestino;
    private final String detalle;

    public Movimiento(LocalDateTime fechaHora, TipoMovimiento tipo, String codigoProducto,
                      int cantidad, String idAlmacenOrigen, String idAlmacenDestino, String detalle) {
        if (fechaHora == null) {
            throw new IllegalArgumentException("La fecha/hora del movimiento es obligatoria.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de movimiento es obligatorio.");
        }
        if (codigoProducto == null || codigoProducto.isBlank()) {
            throw new IllegalArgumentException("El código de producto es obligatorio.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad del movimiento debe ser mayor que cero.");
        }
        this.fechaHora = fechaHora;
        this.tipo = tipo;
        this.codigoProducto = codigoProducto.trim().toUpperCase();
        this.cantidad = cantidad;
        this.idAlmacenOrigen = normalizarOpcional(idAlmacenOrigen);
        this.idAlmacenDestino = normalizarOpcional(idAlmacenDestino);
        this.detalle = detalle == null ? "" : detalle.trim();
    }

    private static String normalizarOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return "-";
        }
        return valor.trim().toUpperCase();
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public String getIdAlmacenOrigen() {
        return idAlmacenOrigen;
    }

    public String getIdAlmacenDestino() {
        return idAlmacenDestino;
    }

    public String getDetalle() {
        return detalle;
    }

    public String getFechaSolo() {
        return fechaHora.toLocalDate().toString();
    }

    /** Línea lista para append en movimientos.txt */
    public String aLineaArchivo() {
        return String.join(" | ",
                fechaHora.format(FORMATO_FECHA_HORA),
                tipo.name(),
                codigoProducto,
                String.valueOf(cantidad),
                idAlmacenOrigen,
                idAlmacenDestino,
                detalle);
    }

    public static Movimiento desdeLineaArchivo(String linea) {
        if (linea == null || linea.isBlank() || linea.startsWith("#")) {
            return null;
        }
        String[] partes = linea.split("\\|");
        if (partes.length < 6) {
            throw new IllegalArgumentException("Línea de movimiento inválida: " + linea);
        }
        for (int i = 0; i < partes.length; i++) {
            partes[i] = partes[i].trim();
        }
        LocalDateTime fechaHora = LocalDateTime.parse(partes[0], FORMATO_FECHA_HORA);
        TipoMovimiento tipo = TipoMovimiento.valueOf(partes[1]);
        String codigo = partes[2];
        int cantidad = Integer.parseInt(partes[3]);
        String origen = partes[4];
        String destino = partes[5];
        String detalle = partes.length > 6 ? partes[6] : "";
        return new Movimiento(fechaHora, tipo, codigo, cantidad, origen, destino, detalle);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Movimiento that)) {
            return false;
        }
        return cantidad == that.cantidad
                && Objects.equals(fechaHora, that.fechaHora)
                && tipo == that.tipo
                && Objects.equals(codigoProducto, that.codigoProducto)
                && Objects.equals(idAlmacenOrigen, that.idAlmacenOrigen)
                && Objects.equals(idAlmacenDestino, that.idAlmacenDestino);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fechaHora, tipo, codigoProducto, cantidad, idAlmacenOrigen, idAlmacenDestino);
    }

    @Override
    public String toString() {
        return aLineaArchivo();
    }
}
