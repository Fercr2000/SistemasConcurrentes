package es.ujaen.ssccdd;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import static es.ujaen.ssccdd.Constantes.*;

public class EstacionEmpaquetado {
    private final String id;                    // Identificador único
    private final ZonaAlmacen ubicacion;        // Ubicación en almacén
    private final Queue<Producto> productos;    // Productos en espera
    private boolean activa;                     // Si está operativa

    public EstacionEmpaquetado(String id, ZonaAlmacen ubicacion) {
        this.id = (id == null || id.trim().isEmpty()) ? UUID.randomUUID().toString() : id;
        this.ubicacion = ubicacion;
        this.productos = new LinkedList<>();
        this.activa = ACTIVA;
    }

    public String getId() {
        return id;
    }

    public ZonaAlmacen getUbicacion() {
        return ubicacion;
    }

    public int getCapacidadActual() {
        return productos.size();
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    /**
     * Recibe un producto del robot
     * Verifica que hay espacio en la cola
     * Actualiza el contador de productos
     */
    public boolean recibirProducto(Producto producto) {
        boolean resultado = !EXITO;
        
        if( (productos.size() < MAX_PRODUCTOS_ESTACION) && activa && producto != null ) {
            productos.add(producto);
            resultado = EXITO;
        }
        
        return resultado;
    }

    /**
     * Procesa los productos en cola
     * Simula el empaquetado
     * Libera espacio en la estación
     */
    public void procesarProductos() {
        if( activa && !productos.isEmpty() )
            productos.clear();
    }

    /**
     * Devuelve el estado actual de la estación de empaquetado.
     * Debe incluir:
     * - Identificador único de la estación
     * - Ubicación en el almacén
     * - Número de productos en cola
     * - Estado de actividad de la estación
     * - Lista resumida de productos en espera
     * La información debe permitir conocer la capacidad y ocupación de la estación.
     *
     * @return Una cadena con el formato para EstacionEmpaquetado
     */
    @Override
    public String toString() {
        return "EstacionEmpaquetado{" +
                "id='" + id + '\'' +
                ", ubicacion=" + ubicacion +
                ", productos=" + productos +
                ", activa=" + activa +
                '}';
    }
}