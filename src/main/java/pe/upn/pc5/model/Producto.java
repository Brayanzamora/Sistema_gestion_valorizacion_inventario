package pe.upn.pc5.model;

import java.util.Objects;

/**
 * Entidad de dominio que representa un producto del inventario.
 * Encapsula código, nombre, categoría, precio unitario y estado activo (baja lógica).
 */
public class Producto {

    private String codigo;
    private String nombre;
    private String categoria;
    private double precioUnitario;
    private boolean activo;

    public Producto(String codigo, String nombre, String categoria, double precioUnitario, boolean activo) {
        setCodigo(codigo);
        setNombre(nombre);
        setCategoria(categoria);
        setPrecioUnitario(precioUnitario);
        this.activo = activo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del producto no puede ser nulo ni vacío.");
        }
        this.codigo = codigo.trim().toUpperCase();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede ser nulo ni vacío.");
        }
        this.nombre = nombre.trim();
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            throw new IllegalArgumentException("La categoría no puede ser nula ni vacía.");
        }
        this.categoria = categoria.trim();
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        if (precioUnitario <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor que cero.");
        }
        this.precioUnitario = precioUnitario;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    /** Baja lógica: el producto deja de listarse como activo sin borrar historial. */
    public void darDeBaja() {
        this.activo = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Producto producto)) {
            return false;
        }
        return Objects.equals(codigo, producto.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | S/ %.2f | %s",
                codigo, nombre, categoria, precioUnitario, activo ? "ACTIVO" : "INACTIVO");
    }
}
