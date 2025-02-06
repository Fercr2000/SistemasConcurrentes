package es.ujaen.ssccdd;

import java.util.Date;

import static es.ujaen.ssccdd.Constantes.TipoEvento;
import static es.ujaen.ssccdd.Constantes.ZonaAlmacen;

public class EventoAlmacen {
    private final Date timestamp;           // Momento del evento
    private final TipoEvento tipo;         // Tipo del evento
    private final String idRobot;          // Robot involucrado
    private final ZonaAlmacen zona;        // Zona donde ocurrió

    /*
        Los atributos no pueden ser nulos y se inicializan en el constructor
     */

    public EventoAlmacen(Date timestamp, TipoEvento tipo, String idRobot, ZonaAlmacen zona) {
        if( timestamp == null || tipo == null || idRobot == null || zona == null)
            throw new IllegalArgumentException("No puede haber argumentos nulos");

        this.timestamp = timestamp;
        this.tipo = tipo;
        this.idRobot = idRobot;
        this.zona = zona;
    }

    /*
        Incluir solo los métodos de acceso estrictamente necesarios
     */

    public Date getTimestamp() {
        return timestamp;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public String getIdRobot() {
        return idRobot;
    }

    public ZonaAlmacen getZona() {
        return zona;
    }

    /**
     * Devuelve una representación en forma de cadena del evento ocurrido en el almacén.
     * Debe incluir:
     * - La fecha y hora exacta del evento en formato legible
     * - El tipo de evento (RECOGIDA_PRODUCTO, ENTREGA_PRODUCTO, etc.)
     * - El identificador del robot involucrado
     * - La zona del almacén donde ocurrió el evento
     * La cadena resultante debe facilitar el seguimiento y análisis de la operativa del almacén.
     *
     * @return Una cadena con el formato EventoAlmacen
     */
    @Override
    public String toString() {
        return "EventoAlmacen{" +
                "timestamp=" + timestamp +
                ", tipo=" + tipo +
                ", idRobot='" + idRobot + '\'' +
                ", zona=" + zona +
                '}';
    }
}