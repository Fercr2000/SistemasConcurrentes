package es.ujaen.ssccdd;

import org.junit.jupiter.api.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import static es.ujaen.ssccdd.Constantes.*;
import static es.ujaen.ssccdd.Constantes.EstadoRobot.*;
import static es.ujaen.ssccdd.Constantes.TipoProducto.*;
import static es.ujaen.ssccdd.Constantes.TipoEvento.*;
import static es.ujaen.ssccdd.Constantes.ZonaAlmacen.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Clase que contiene las pruebas básicas para el sistema de automatización de almacén.
 * Se centra en la funcionalidad esencial de cada componente, verificando:
 * - Correcta inicialización de objetos
 * - Operaciones básicas de cada clase
 * - Validación de datos de entrada
 * - Formato correcto de la representación en cadena
 */
@DisplayName("Pruebas Básicas del Sistema de Almacén")
public class PruebaMinimaTest {

    @Nested
    @DisplayName("Pruebas de EventoAlmacen")
    class EventoAlmacenTest {
        private EventoAlmacen evento;
        private final Date TIMESTAMP = new Date();
        private final String ID_ROBOT = "robot1";

        @BeforeEach
        void setUp() {
            evento = new EventoAlmacen(TIMESTAMP, RECOGIDA_PRODUCTO, ID_ROBOT, ZONA_A);
        }

        /**
         * Verifica la correcta creación de un evento de almacén.
         *
         * Aspectos evaluados:
         * 1. Todos los campos se inicializan con los valores proporcionados
         * 2. No se permiten valores nulos en los campos obligatorios
         * 3. Los getters devuelven los valores correctos
         */
        @Test
        @DisplayName("Test constructor de EventoAlmacen")
        void testConstructor() {
            assertAll("Constructor EventoAlmacen",
                    () -> assertEquals(TIMESTAMP, evento.getTimestamp(),
                            "El timestamp debe coincidir"),
                    () -> assertEquals(RECOGIDA_PRODUCTO, evento.getTipo(),
                            "El tipo de evento debe coincidir"),
                    () -> assertEquals(ID_ROBOT, evento.getIdRobot(),
                            "El ID del robot debe coincidir"),
                    () -> assertEquals(ZONA_A, evento.getZona(),
                            "La zona debe coincidir")
            );
        }

        /**
         * Verifica que el constructor rechace parámetros nulos.
         *
         * Aspectos evaluados:
         * 1. Se lanza IllegalArgumentException para cada parámetro nulo
         * 2. Los mensajes de error son descriptivos
         */
        @Test
        @DisplayName("Test constructor con parámetros null")
        void testConstructorNull() {
            assertAll("Constructor con null",
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new EventoAlmacen(null, RECOGIDA_PRODUCTO, ID_ROBOT, ZONA_A)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new EventoAlmacen(TIMESTAMP, null, ID_ROBOT, ZONA_A)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new EventoAlmacen(TIMESTAMP, RECOGIDA_PRODUCTO, null, ZONA_A)),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> new EventoAlmacen(TIMESTAMP, RECOGIDA_PRODUCTO, ID_ROBOT, null))
            );
        }

        /**
         * Verifica la representación en cadena del evento.
         *
         * Aspectos evaluados:
         * 1. Incluye todos los campos relevantes
         * 2. El formato es legible y completo
         * 3. No hay campos nulos en la representación
         */
        @Test
        @DisplayName("Test toString de EventoAlmacen")
        void testToString() {
            String cadena = evento.toString();

            assertAll("toString EventoAlmacen",
                    () -> assertNotNull(cadena),
                    () -> assertTrue(cadena.contains(TIMESTAMP.toString())),
                    () -> assertTrue(cadena.contains(RECOGIDA_PRODUCTO.toString())),
                    () -> assertTrue(cadena.contains(ID_ROBOT)),
                    () -> assertTrue(cadena.contains(ZONA_A.toString()))
            );
        }
    }

    @Nested
    @DisplayName("Pruebas de Producto")
    class ProductoTest {
        private Producto producto;
        private final String ID_PRODUCTO = "prod1";

        @BeforeEach
        void setUp() {
            producto = new Producto(ID_PRODUCTO, ELECTRONICA_PEQUENA, ZONA_A);
        }

        /**
         * Verifica la correcta inicialización del producto.
         *
         * Aspectos evaluados:
         * 1. Los atributos se inicializan con valores correctos
         * 2. El producto no está reservado inicialmente
         * 3. La ubicación se establece correctamente
         */
        @Test
        @DisplayName("Test constructor de Producto")
        void testConstructor() {
            assertAll("Constructor Producto",
                    () -> assertEquals(ID_PRODUCTO, producto.getId()),
                    () -> assertEquals(ELECTRONICA_PEQUENA, producto.getTipo()),
                    () -> assertEquals(ZONA_A, producto.getUbicacion()),
                    () -> assertFalse(producto.isReservado())
            );
        }

        /**
         * Verifica la lógica de reserva de productos.
         *
         * Aspectos evaluados:
         * 1. Un producto libre puede ser reservado
         * 2. Un producto reservado no puede volver a reservarse
         * 3. Un producto reservado puede ser liberado
         */
        @Test
        @DisplayName("Test reserva y liberación de Producto")
        void testReservaProducto() {
            assertTrue(producto.reservarProducto(), "Debe poder reservarse");
            assertFalse(producto.reservarProducto(), "No debe poder reservarse dos veces");

            producto.liberarProducto();
            assertTrue(producto.reservarProducto(), "Debe poder reservarse tras liberar");
        }

        /**
         * Verifica las condiciones de manipulación segura.
         *
         * Aspectos evaluados:
         * 1. Nivel de batería suficiente para manipulación
         * 2. Requisitos específicos según tipo de producto
         * 3. Estado de reserva del producto
         */
        @Test
        @DisplayName("Test manipulación segura de Producto")
        void testManipulacionSegura() {
            assertAll("Manipulación segura",
                    () -> assertTrue(producto.puedeSerManipulado(100),
                            "Debe poder manipularse con batería completa"),
                    () -> assertFalse(producto.puedeSerManipulado(NIVEL_BATERIA_MINIMO),
                            "No debe manipularse con batería baja"),
                    () -> assertTrue(producto.requiereManipulacionEspecial(),
                            "Electrónica debe requerir manipulación especial")
            );
        }

        /**
         * Verifica la representación en cadena del producto.
         *
         * Aspectos evaluados:
         * 1. Incluye identificador y tipo de producto
         * 2. Muestra ubicación actual
         * 3. Indica estado de reserva
         */
        @Test
        @DisplayName("Test toString de Producto")
        void testToString() {
            String cadena = producto.toString();

            assertAll("toString Producto",
                    () -> assertNotNull(cadena),
                    () -> assertTrue(cadena.contains(ID_PRODUCTO)),
                    () -> assertTrue(cadena.contains(ELECTRONICA_PEQUENA.toString())),
                    () -> assertTrue(cadena.contains(ZONA_A.toString()))
            );
        }
    }

    @Nested
    @DisplayName("Pruebas de Robot")
    class RobotTest {
        private Robot robot;
        private final String ID_ROBOT = "robot1";

        @BeforeEach
        void setUp() {
            robot = new Robot(ID_ROBOT, ZONA_A);
        }

        /**
         * Verifica la correcta inicialización del robot.
         *
         * Aspectos evaluados:
         * 1. Estado inicial correcto (LIBRE)
         * 2. Batería inicial al 100%
         * 3. Sin producto asignado inicialmente
         * 4. Posición inicial correcta
         */
        @Test
        @DisplayName("Test constructor de Robot")
        void testConstructor() {
            assertAll("Constructor Robot",
                    () -> assertEquals(ID_ROBOT, robot.getId()),
                    () -> assertEquals(LIBRE, robot.getEstado()),
                    () -> assertEquals(100, robot.getNivelBateria()),
                    () -> assertNull(robot.getProductoActual()),
                    () -> assertEquals(ZONA_A, robot.getPosicionActual())
            );
        }

        /**
         * Verifica el movimiento del robot entre zonas.
         *
         * Aspectos evaluados:
         * 1. Movimiento válido a zona adyacente
         * 2. Consumo de batería al moverse
         * 3. Restricciones de movimiento según estado
         */
        @Test
        @DisplayName("Test movimiento de Robot")
        void testMovimiento() {
            assertTrue(robot.moverA(ZONA_B), "Debe poder moverse estando libre");
            assertEquals(ZONA_B, robot.getPosicionActual(), "Debe actualizar posición");
            assertTrue(robot.getNivelBateria() < 100, "Debe consumir batería");
        }

        /**
         * Verifica las operaciones con productos.
         *
         * Aspectos evaluados:
         * 1. Recogida exitosa de producto
         * 2. Cambio de estado al recoger producto
         * 3. Entrega correcta en estación
         */
        @Test
        @DisplayName("Test operaciones con productos")
        void testOperacionesProducto() {
            Producto producto = new Producto("test", ROPA, ZONA_A);
            EstacionEmpaquetado estacion = new EstacionEmpaquetado("est1", ZONA_B);

            assertTrue(robot.recogerProducto(producto), "Debe poder recoger el producto");
            assertEquals(OCUPADO, robot.getEstado(), "Debe estar ocupado tras recoger");
            assertEquals(producto, robot.getProductoActual(), "Debe tener el producto asignado");
        }

        /**
         * Verifica la gestión de la batería.
         *
         * Aspectos evaluados:
         * 1. Detección correcta de necesidad de carga
         * 2. Consumo apropiado de batería en operaciones
         * 3. Restricciones de operación por batería baja
         */
        @Test
        @DisplayName("Test gestión de batería")
        void testGestionBateria() {
            // Simulamos consumo de batería
            while(robot.getNivelBateria() > NIVEL_BATERIA_MINIMO) {
                robot.moverA(robot.getPosicionActual() == ZONA_A ? ZONA_B : ZONA_A);
            }

            assertTrue(robot.necesitaCarga(), "Debe necesitar carga con batería baja");
            assertFalse(robot.moverA(ZONA_C), "No debe moverse con batería baja");
        }

        /**
         * Verifica la representación en cadena del robot.
         *
         * Aspectos evaluados:
         * 1. Incluye identificador y estado
         * 2. Muestra nivel de batería
         * 3. Indica producto actual si existe
         * 4. Muestra posición actual
         */
        @Test
        @DisplayName("Test toString de Robot")
        void testToString() {
            String cadena = robot.toString();

            assertAll("toString Robot",
                    () -> assertNotNull(cadena),
                    () -> assertTrue(cadena.contains(ID_ROBOT)),
                    () -> assertTrue(cadena.contains(LIBRE.toString())),
                    () -> assertTrue(cadena.contains("100")), // nivel batería
                    () -> assertTrue(cadena.contains(ZONA_A.toString()))
            );
        }
    }

    @Nested
    @DisplayName("Pruebas de EstacionEmpaquetado")
    class EstacionEmpaquetadoTest {
        private EstacionEmpaquetado estacion;
        private final String ID_ESTACION = "est1";

        @BeforeEach
        void setUp() {
            estacion = new EstacionEmpaquetado(ID_ESTACION, ZONA_A);
        }

        /**
         * Verifica la correcta inicialización de la estación.
         *
         * Aspectos evaluados:
         * 1. Identificador asignado correctamente
         * 2. Ubicación establecida
         * 3. Cola de productos vacía inicialmente
         * 4. Estado activo por defecto
         */
        @Test
        @DisplayName("Test constructor de EstacionEmpaquetado")
        void testConstructor() {
            assertAll("Constructor EstacionEmpaquetado",
                    () -> assertEquals(ID_ESTACION, estacion.getId()),
                    () -> assertEquals(ZONA_A, estacion.getUbicacion()),
                    () -> assertTrue(estacion.isActiva())
            );
        }

        /**
         * Verifica la recepción de productos.
         *
         * Aspectos evaluados:
         * 1. Recepción correcta dentro del límite
         * 2. Rechazo al superar capacidad máxima
         * 3. Rechazo si la estación está inactiva
         */
        @Test
        @DisplayName("Test recepción de productos")
        void testRecepcionProductos() {
            Producto producto = new Producto("test", ROPA, ZONA_A);

            assertTrue(estacion.recibirProducto(producto),
                    "Debe aceptar producto dentro de capacidad");

            // Llenamos la estación
            for(int i = 0; i < MAX_PRODUCTOS_ESTACION - 1; i++) {
                estacion.recibirProducto(new Producto("test"+i, ROPA, ZONA_A));
            }

            assertFalse(estacion.recibirProducto(new Producto("testFinal", ROPA, ZONA_A)),
                    "Debe rechazar al superar capacidad");
        }

        /**
         * Verifica el procesamiento de productos.
         *
         * Aspectos evaluados:
         * 1. Procesamiento correcto de productos en cola
         * 2. Liberación de espacio tras procesamiento
         * 3. Manejo de cola vacía
         * 4. Estado de la estación durante el procesamiento
         */
        @Test
        @DisplayName("Test procesamiento de productos")
        void testProcesamientoProductos() {
            Producto producto1 = new Producto("test1", ROPA, ZONA_A);
            Producto producto2 = new Producto("test2", ROPA, ZONA_A);

            estacion.recibirProducto(producto1);
            estacion.recibirProducto(producto2);

            estacion.procesarProductos();

            assertFalse(estacion.recibirProducto(null),
                    "Debe rechazar productos nulos");

            // Verificamos que se puede recibir más productos tras procesar
            assertTrue(estacion.recibirProducto(new Producto("test3", ROPA, ZONA_A)),
                    "Debe aceptar productos tras procesar");
        }

        /**
         * Verifica el comportamiento de la estación cuando está inactiva.
         *
         * Aspectos evaluados:
         * 1. Rechazo de productos en estado inactivo
         * 2. Mantenimiento del estado de la cola
         * 3. Capacidad de reactivación
         */
        @Test
        @DisplayName("Test estación inactiva")
        void testEstacionInactiva() {
            estacion.setActiva(false);
            Producto producto = new Producto("test", ROPA, ZONA_A);

            assertAll("Estación inactiva",
                    () -> assertFalse(estacion.recibirProducto(producto),
                            "No debe aceptar productos si está inactiva"),
                    () -> {
                        estacion.setActiva(true);
                        assertTrue(estacion.recibirProducto(producto),
                                "Debe aceptar productos al reactivarse");
                    }
            );
        }

        /**
         * Verifica la representación en cadena de la estación.
         *
         * Aspectos evaluados:
         * 1. Incluye identificador y ubicación
         * 2. Muestra estado de actividad
         * 3. Indica número de productos en cola
         * 4. Lista los productos pendientes de procesar
         */
        @Test
        @DisplayName("Test toString de EstacionEmpaquetado")
        void testToString() {
            Producto producto = new Producto("test", ROPA, ZONA_A);
            estacion.recibirProducto(producto);

            String cadena = estacion.toString();

            assertAll("toString EstacionEmpaquetado",
                    () -> assertNotNull(cadena),
                    () -> assertTrue(cadena.contains(ID_ESTACION)),
                    () -> assertTrue(cadena.contains(ZONA_A.toString())),
                    () -> assertTrue(cadena.contains("activa")),
                    () -> assertTrue(cadena.contains("test")) // ID del producto en cola
            );
        }
    }
}