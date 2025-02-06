package es.ujaen.ssccdd;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public interface Constantes {
    Random aleatorio = new Random();

    /**
     * Suma una cantidad de segundos a una fecha dada y nos devuelve la nueva
     * fecha.
     */
    BiFunction<Date, Integer, Date> sumarSegundos = (fecha, segundos) -> {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        calendar.add(Calendar.SECOND, segundos);
        return calendar.getTime();
    };

    /**
     * Predicado para comprobar si se ha alcanzado el vencimiento de una fecha
     * comparando con la fecha actual.
     */
    Predicate<Date> vencimiento = (fecha) -> fecha.before(new Date());

    /**
     * Representa los diferentes estados posibles de un robot.
     * Cada estado tiene un tiempo máximo permitido y define las transiciones válidas hacia otros estados.
     */
    public enum EstadoRobot {
        LIBRE(1) {
            @Override
            public boolean siguienteEstado(EstadoRobot siguiente) {
                // Solo puede pasar a OCUPADO o EN_CARGA
                return siguiente == OCUPADO || siguiente == EN_CARGA;
            }
        },
        OCUPADO(2) {
            @Override
            public boolean siguienteEstado(EstadoRobot siguiente) {
                // Desde ocupado puede quedar LIBRE, necesitar CARGA o requerir MANTENIMIENTO
                return siguiente == LIBRE || siguiente == EN_CARGA || siguiente == MANTENIMIENTO;
            }
        },
        EN_CARGA(3) {
            @Override
            public boolean siguienteEstado(EstadoRobot siguiente) {
                // Al terminar la carga puede quedar LIBRE o necesitar MANTENIMIENTO
                return siguiente == LIBRE || siguiente == MANTENIMIENTO;
            }
        },
        MANTENIMIENTO(4) {
            @Override
            public boolean siguienteEstado(EstadoRobot siguiente) {
                // Solo puede volver a LIBRE cuando termine el mantenimiento
                return siguiente == LIBRE;
            }
        };

        private final int tiempoMaximo;  // Tiempo máximo en segundos que puede estar en este estado

        EstadoRobot(int tiempoMaximo) {
            this.tiempoMaximo = tiempoMaximo;
        }

        /**
         * Genera un estado aleatorio de EstadoRobot de forma uniforme.
         * Similar al método getEstado() de EstadoSemaforo.
         * @return Un valor aleatorio de EstadoRobot.
         */
        public static EstadoRobot getEstado() {
            EstadoRobot[] valores = values();
            return valores[aleatorio.nextInt(valores.length)];
        }

        /**
         * Tiempo máximo que puede permanecer el robot en este estado
         * @return El tiempo expresado en segundos
         */
        public int getTiempoMaximo() {
            return tiempoMaximo;
        }

        /**
         * Comprueba si es válido el cambio al estado siguiente
         * Cada estado implementa su lógica específica de cambios permitidas
         * @param siguiente Estado al que se quiere cambiar
         * @return true si la transición es válida, false en caso contrario
         */
        public abstract boolean siguienteEstado(EstadoRobot siguiente);
    }

    /**
     * Representa los diferentes tipos de productos que pueden ser manejados en el sistema.
     * Cada tipo de producto tiene un peso máximo permitido y puede requerir manipulación especial
     * dependiendo de sus características.
     */
    enum TipoProducto {
        ELECTRONICA_PEQUENA(1000,60) {
            @Override
            public boolean requiereManipulacionEspecial() {
                return true;
            }

            @Override
            public boolean manipularProducto(int nivelBateria) {
                // Requiere alta precisión, por lo que necesita buen nivel de batería
                return nivelBateria >= getBateriaMinima();
            }
        },
        ELECTRONICA_GRANDE(5000,80) {
            @Override
            public boolean requiereManipulacionEspecial() {
                return true;
            }

            @Override
            public boolean manipularProducto(int nivelBateria) {
                // Necesita máxima energía por el peso
                return nivelBateria >= getBateriaMinima();
            }
        },
        ROPA(500,20) {
            @Override
            public boolean requiereManipulacionEspecial() {
                return false;
            }

            @Override
            public boolean manipularProducto(int nivelBateria) {
                // Producto ligero y sin requisitos especiales
                return nivelBateria >= getBateriaMinima();
            }
        },
        LIBROS(800,30) {
            @Override
            public boolean requiereManipulacionEspecial() {
                return false;
            }

            @Override
            public boolean manipularProducto(int nivelBateria) {
                // Producto estándar
                return nivelBateria >= getBateriaMinima();
            }
        },
        ALIMENTOS(2000,70) {
            @Override
            public boolean requiereManipulacionEspecial() {
                return true;
            }

            @Override
            public boolean manipularProducto(int nivelBateria) {
                // Requiere movimientos rápidos por temperatura
                return nivelBateria >= getBateriaMinima();
            }
        },
        FRAGIL(1500,75) {
            @Override
            public boolean requiereManipulacionEspecial() {
                return true;
            }

            @Override
            public boolean manipularProducto(int nivelBateria) {
                // Requiere máxima precisión en movimientos
                return nivelBateria >= getBateriaMinima();
            }
        };

        private final int pesoMaximo;  // Peso máximo en gramos para este tipo de producto
        private final int bateriaMinima;

        TipoProducto(int pesoMaximo, int bateriaMinima) {
            this.pesoMaximo = pesoMaximo;
            this.bateriaMinima = bateriaMinima;
        }

        public int getPesoMaximo() {
            return pesoMaximo;
        }

        public int getBateriaMinima() {
            return bateriaMinima;
        }

        /**
         * Indica si el objeto requiere una manipulación especial para su manejo o transporte.
         * Esta verificación debe considerar características específicas del tipo de objeto,
         * como peso, tamaño, fragilidad o cualquier otra restricción que pueda requerir
         * procedimientos especiales.
         *
         * @return true si el objeto requiere manipulación especial;
         *         false si puede manejarse de forma estándar.
         */
        public abstract boolean requiereManipulacionEspecial();

        /**
         * Determina si el robot puede manipular este tipo de producto de forma segura.
         * Cada tipo de producto implementa su propia lógica considerando:
         * - El nivel de batería necesario para una manipulación segura
         * - Las características especiales del tipo de producto
         * - Los requisitos de seguridad específicos
         *
         * @param nivelBateria El nivel actual de batería del robot (0-100)
         * @return true si el robot puede manipular el producto de forma segura,
         *         false si no hay garantías de una manipulación segura
         */
        public abstract boolean manipularProducto(int nivelBateria);
    }

    /**
     * Enumeradi que define los distintos tipos de eventos que pueden ocurrir
     * en el sistema.
     *
     * - RECOGIDA_PRODUCTO: Representa un evento en el que se recoge un producto.
     * - ENTREGA_PRODUCTO: Representa un evento en el que se entrega un producto.
     * - ROBOT_EN_CARGA: Indica que un robot está realizando una recarga de batería.
     * - COLISION_EVITADA: Evento que notifica que se ha evitado una colisión.
     * - ERROR_SISTEMA: Representa un error ocurrido en el sistema.
     * - ESTACION_LLENA: Indica que una estación ha alcanzado su capacidad máxima.
     */
    enum TipoEvento {
        RECOGIDA_PRODUCTO,
        ENTREGA_PRODUCTO,
        ROBOT_EN_CARGA,
        COLISION_EVITADA,
        ERROR_SISTEMA,
        ESTACION_LLENA
    }

    /**
     * Enumerado que define las zonas de almacenamiento disponibles en un sistema.
     *
     * Este enumerado representa las diferentes áreas dentro de un almacén
     * donde pueden gestionarse o ubicarse los productos.
     * Las zonas son las siguientes:
     * - ZONA_A: Primera zona de almacenamiento.
     * - ZONA_B: Segunda zona de almacenamiento.
     * - ZONA_C: Tercera zona de almacenamiento.
     * - ZONA_D: Cuarta zona de almacenamiento.
     */
    enum ZonaAlmacen {
        ZONA_A, ZONA_B, ZONA_C, ZONA_D
    }


    int MAX_PRODUCTOS_ESTACION = 10;
    int NIVEL_BATERIA_MINIMO = 10;
    int CONSUMO_BATERIA = 5;
    int COMPLETA = 100;
    boolean EXITO = true;
    boolean ASIGNADO = true;
    boolean ACTIVA = true;
    boolean RESERVADO = true;
}
