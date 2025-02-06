package es.ujaen.ssccdd;

import java.util.UUID;

import static es.ujaen.ssccdd.Constantes.*;

public class Producto {
    private final String id;                // Identificador único
    private final TipoProducto tipo;        // Tipo del producto
    private ZonaAlmacen ubicacion;          // Ubicación en almacén
    private boolean reservado;              // Si está asignado a un robot

    /*
        Los atributos no pueden ser nulos y se inicializan en el constructor
     */

    public Producto(String id, Constantes.TipoProducto tipo, ZonaAlmacen ubicacion) {
        this.id = (id == null || id.trim().isEmpty()) ? UUID.randomUUID().toString() : id;
        this.tipo = tipo;
        this.ubicacion = ubicacion;
        this.reservado = !ASIGNADO;
    }

    public Producto(Producto producto) {
        this.id = producto.id;
        this.tipo = producto.tipo;
        this.ubicacion = producto.ubicacion;
        this.reservado = producto.reservado;
    }

    /*
        Se incluyen los métodos de acceso estrictamente necesarios
     */

    public String getId() {
        return id;
    }

    public TipoProducto getTipo() {
        return tipo;
    }

    public ZonaAlmacen getUbicacion() {
        return ubicacion;
    }

    public void setReservado(boolean reservado) {
        this.reservado = reservado;
    }

    public boolean isReservado() {
        return reservado;
    }

    /**
     * Asigna el producto a un robot para su recogida
     * Verifica que el producto no esté ya reservado
     * Actualiza el estado de reserva
     */
    public boolean reservarProducto() {
        boolean resultado = !EXITO;

        if( !reservado ) {
            reservado = ASIGNADO;
            resultado = EXITO;
        }

        return resultado;
    }

    /**
     * Libera el producto si la operación no se pudo completar
     * Permite que otro robot pueda recogerlo
     */
    public void liberarProducto() {
        reservado = !ASIGNADO;
    }

    /**
     * Verifica si el producto puede ser manipulado de forma segura por un robot.
     * La verificación tiene en cuenta:
     * - El tipo de producto y sus requisitos específicos
     * - El peso del producto respecto al máximo permitido
     * - El nivel de batería del robot
     * - Si el producto está reservado
     *
     * Si el producto excede el peso máximo para su tipo, no se permite la manipulación
     * independientemente del nivel de batería del robot.
     *
     * @param nivelBateria El nivel actual de batería del robot que intenta manipular
     * @return true si el producto puede ser manipulado de forma segura,
     *         false si no se cumplen las condiciones de seguridad
     */
    public boolean puedeSerManipulado(int nivelBateria) {
        boolean resultado = tipo.manipularProducto(nivelBateria);

        if( tipo.requiereManipulacionEspecial() )
            resultado = tipo.getBateriaMinima() + NIVEL_BATERIA_MINIMO < nivelBateria;

        return resultado;
    }

    /**
     * Determina si el producto requiere manipulación especial basándose en:
     * - El tipo de producto y sus características específicas
     * - El peso actual del producto en relación con el peso máximo permitido
     * - El estado actual del producto (por ejemplo, si está reservado)
     *
     *
     * Este método debe consultarse antes de cualquier operación de movimiento
     * para garantizar que se aplican las medidas de seguridad adecuadas.
     *
     * @return true si el producto necesita manipulación especial,
     *         false si puede manejarse de forma estándar
     */
    public boolean requiereManipulacionEspecial() {
        return tipo.requiereManipulacionEspecial();
    }

    /**
     * Devuelve una descripción completa del producto en el almacén.
     * Debe incluir:
     * - Identificador único del producto
     * - Descripción del producto
     * - Peso en gramos
     * - Ubicación actual en el almacén
     * - Estado de reserva (si está asignado a algún robot)
     * La información debe ser suficiente para identificar y localizar el producto.
     *
     * @return Una cadena con el formato para Producto
     */
    @Override
    public String toString() {
        return "Producto{" +
                "id='" + id + '\'' +
                ", tipo=" + tipo +
                ", ubicacion=" + ubicacion +
                ", reservado=" + reservado +
                '}';
    }
}
