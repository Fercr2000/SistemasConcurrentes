package es.ujaen.ssccdd;

import java.util.Objects;
import java.util.UUID;

import static es.ujaen.ssccdd.Constantes.*;

public class Robot {
    private final String id;                // Identificador único
    private EstadoRobot estado;             // Estado actual
    private int nivelBateria;               // Porcentaje de batería
    private Producto productoActual;        // Producto que transporta
    private ZonaAlmacen posicionActual;     // Ubicación en el almacén

    /*
        Los atributos no pueden ser nulos y estarán inicializados en el constructor
     */
    public Robot(String id, ZonaAlmacen posicionActual) {
        this.id = (id == null || id.trim().isEmpty()) ? UUID.randomUUID().toString() : id;
        this.posicionActual = posicionActual;
        this.productoActual = null;
        this.estado = EstadoRobot.LIBRE;
        this.nivelBateria = COMPLETA;
    }

    /*
        Se incluyen los métodos de acceso estrictamente necesarios
     */

    public String getId() {
        return id;
    }

    public EstadoRobot getEstado() {
        return estado;
    }

    public int getNivelBateria() {
        return nivelBateria;
    }

    public Producto getProductoActual() {
        return productoActual;
    }

    public ZonaAlmacen getPosicionActual() {
        return posicionActual;
    }

    /**
     * Mueve el robot a una nueva posición si el camino está libre.
     * El movimiento debe considerar:
     * - Si transporta un producto que requiere manipulación especial
     * - El nivel de batería necesario para el movimiento
     * - Posibles colisiones con otros robots
     *
     * @param destino Nueva posición a la que debe moverse el robot
     * @return true si el movimiento se realizó con éxito, false en caso contrario
     */
    public boolean moverA(ZonaAlmacen destino) {
        boolean resultado = !EXITO;

        if( nivelBateria > NIVEL_BATERIA_MINIMO && destino != null
            && !posicionActual.equals(destino) ) {

            posicionActual = destino;
            resultado = EXITO;
        }

        consumitBateria();

        return resultado;
    }

    /**
     * Recoge un producto de su ubicación actual.
     * Antes de recoger el producto verifica:
     * - Que el robot esté libre y con batería suficiente
     * - Que el producto pueda ser manipulado de forma segura
     * - Que el robot pueda manejar el tipo específico de producto
     *
     * @param producto El producto a recoger
     * @return true si el producto se recogió con éxito, false si no se pudo recoger
     */
    public boolean recogerProducto(Producto producto) {
        boolean resultado = !EXITO;

        if( estado.equals(EstadoRobot.LIBRE) && !necesitaCarga() &&
            producto.puedeSerManipulado(nivelBateria) && producto.getUbicacion().equals(posicionActual) ) {

            productoActual = producto;
            productoActual.setReservado(RESERVADO);
            estado = EstadoRobot.OCUPADO;
            resultado = EXITO;
        }

        return resultado;
    }

    /**
     * Entrega el producto en una estación de empaquetado.
     * El proceso de entrega considera:
     * - Si el producto requiere manipulación especial
     * - La capacidad actual de la estación
     * - El nivel de batería necesario para una entrega segura
     * Ajusta la velocidad y precisión de la entrega según el tipo de producto
     *
     * @param estacion La estación donde se entregará el producto
     * @return true si la entrega fue exitosa, false en caso contrario
     */
    public boolean entregarProducto(EstacionEmpaquetado estacion) {
        boolean resultado = !EXITO;

        if( productoActual != null && estacion != null && estacion.getCapacidadActual() < MAX_PRODUCTOS_ESTACION
            && estacion.getUbicacion().equals(posicionActual) && !necesitaCarga()) {

            estacion.recibirProducto(productoActual);
            productoActual = null;
            estado = EstadoRobot.LIBRE;
            resultado = EXITO;
        }

        return resultado;
    }

    /**
     * Verifica si el robot necesita ir a cargar su batería.
     * La decisión se basa en:
     * - El nivel actual de batería
     * - Si está transportando un producto
     * - El tipo de producto y sus requisitos de manipulación
     *
     * @return true si el robot necesita cargarse, false en caso contrario
     */
    public boolean necesitaCarga() {
        boolean resultado = !EXITO;

        if( productoActual != null) {
            if (nivelBateria <= NIVEL_BATERIA_MINIMO || nivelBateria <= productoActual.getTipo().getBateriaMinima()) {
                //estado = EstadoRobot.EN_CARGA;
                resultado = EXITO;
            }
        } else if( nivelBateria <= NIVEL_BATERIA_MINIMO ) {
            estado = EstadoRobot.EN_CARGA;
            resultado = EXITO;
        }

        return resultado;
    }

    /**
     * Actualiza el nivel de batería del robot reduciéndolo en una cantidad fija.
     *
     * Este método se utiliza para simular el consumo de batería
     * asociado a las operaciones del robot, como movimiento o manipulación de productos.
     *
     * El valor de reducción está definido por la constante {@code CONSUMO_BATERIA}.
     * Debe llamarse a este método solamente cuando sea necesario ajustar el
     * nivel de batería en función de sus operaciones.
     */
    private void consumitBateria() {
        nivelBateria -= CONSUMO_BATERIA;
    }

    /**
     * Devuelve una representación detallada del estado actual del robot.
     * Debe incluir:
     * - Identificador único del robot
     * - Estado actual (LIBRE, OCUPADO, EN_CARGA, MANTENIMIENTO)
     * - Nivel de batería en porcentaje
     * - Información del producto que transporta (si hay alguno)
     * - Posición actual en el almacén
     * La información debe permitir conocer la situación operativa completa del robot.
     *
     * @return Una cadena con el formato para Robot
     */
    @Override
    public String toString() {
        return "Robot{" +
                "id='" + id + '\'' +
                ", estado=" + estado +
                ", nivelBateria=" + nivelBateria +
                ", productoActual=" + productoActual +
                ", posicionActual=" + posicionActual +
                '}';
    }
}