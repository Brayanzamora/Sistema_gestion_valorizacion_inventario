# Sistema de Gestión y Valoración de Inventario Multialmacén

Práctica de Campo 5 — Técnicas de Programación Orientada a Objetos (Java 17).

Persistencia exclusiva en archivos `data/*.csv` y `data/*.txt`. Sin frameworks externos.

## Compilar y ejecutar con Maven

```bash
mvn -q compile
mvn -q exec:java
```

Demo automática (evidencias REQ-01 a REQ-20):

```bash
mvn -q exec:java -Dexec.args="--demo"
```

## Compilar y ejecutar con javac

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src/main/java -name "*.java")
java -cp out pe.upn.pc5.app.Main
```

Demo automática:

```bash
java -cp out pe.upn.pc5.app.Main --demo
```

## Estructura de paquetes

| Paquete | Responsabilidad |
|---------|-----------------|
| `pe.upn.pc5.model` | Entidades de dominio |
| `pe.upn.pc5.persistence` | Lectura/escritura CSV/TXT (`Files`, `Path`, `BufferedReader`, `BufferedWriter`) |
| `pe.upn.pc5.service` | Lógica de negocio y demo de evidencias |
| `pe.upn.pc5.ui` | Menú interactivo de consola |
| `pe.upn.pc5.app` | Punto de entrada `Main` |

## Archivos de datos

- `data/productos.csv`
- `data/almacenes.csv`
- `data/movimientos.txt`
- `data/reporte_valoracion.txt`

## Entregables

- `DIAGRAMA_UML.md` — Diagrama de clases Mermaid
- `INFORME_TECNICO_PC5.md` — Informe técnico + matriz de trazabilidad
