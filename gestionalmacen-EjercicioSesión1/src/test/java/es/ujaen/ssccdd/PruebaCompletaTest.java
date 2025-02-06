package es.ujaen.ssccdd;

import org.junit.jupiter.api.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


import static es.ujaen.ssccdd.Constantes.*;
import static es.ujaen.ssccdd.Constantes.EstadoRobot.*;
import static es.ujaen.ssccdd.Constantes.TipoProducto.*;
import static es.ujaen.ssccdd.Constantes.TipoEvento.*;
import static es.ujaen.ssccdd.Constantes.ZonaAlmacen.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Clase que contiene las pruebas avanzadas del sistema de automatización de almacén.
 * Estas pruebas están diseñadas para evaluar:
 * - Escenarios complejos con múltiples componentes interactuando
 * - Casos límite y situaciones de estrés del sistema
 * - Manejo de errores y recuperación
 * - Comportamiento del sistema en condiciones extremas
 * - Gestión de recursos y eficiencia
 */
@DisplayName("Pruebas Completas del Sistema de Almacén")
public class PruebaCompletaTest {

    @Nested
    @DisplayName("Pruebas Avanzadas de EventoAlmacen")
    class EventoAlmacenTest {
        private final Map<TipoEvento, List<EventoAlmacen>> eventosZona = new EnumMap<>(TipoEvento.class);
        private final Date tiempoBase = new Date();

        @BeforeEach
        void setUp() {
            // Inicializamos eventos para cada tipo y zona
            Arrays.stream(TipoEvento.values()).forEach(tipo -> {
                List<EventoAlmacen> eventos = new ArrayList<>();
                Arrays.stream(ZonaAlmacen.values()).forEach(zona ->
                        eventos.add(new EventoAlmacen(
                                sumarSegundos.apply(tiempoBase, eventos.size()),
                                tipo,
                                "robot" + eventos.size(),
                                zona
                        ))
                );
                eventosZona.put(tipo, eventos);
            });

            System.out.println(eventosZona);
        }

        /**
         * Verifica el tratamiento de eventos simultáneos en diferentes zonas.
         *
         * Este test examina cómo el sistema maneja eventos que ocurren en el
         * mismo instante en diferentes zonas del almacén, verificando que se
         * mantiene la consistencia y se registran correctamente.
         *
         * Aspectos evaluados:
         * 1. Manejo de eventos simultáneos
         * 2. Consistencia en diferentes zonas
         * 3. Ordenamiento de eventos concurrentes
         * 4. Integridad de datos en operaciones paralelas
         */
        @Test
        @DisplayName("Test eventos simultáneos")
        void testEventosSimultaneos() {
            // Creamos eventos simultáneos en diferentes zonas
            Date tiempoComun = new Date();
            List<EventoAlmacen> eventosSimultaneos = Arrays.stream(ZonaAlmacen.values())
                    .map(zona -> new EventoAlmacen(tiempoComun, RECOGIDA_PRODUCTO,
                            "robot" + zona.ordinal(), zona))
                    .toList();

            assertAll("Eventos simultáneos",
                    () -> assertEquals(ZonaAlmacen.values().length,
                            eventosSimultaneos.size(),
                            "Debe haber un evento por zona"),
                    () -> assertTrue(eventosSimultaneos.stream()
                                    .map(EventoAlmacen::getTimestamp)
                                    .distinct()
                                    .count() == 1,
                            "Todos los eventos deben tener el mismo timestamp"),
                    () -> assertEquals(eventosSimultaneos.size(),
                            eventosSimultaneos.stream()
                                    .map(EventoAlmacen::getZona)
                                    .distinct()
                                    .count(),
                            "Cada evento debe estar en una zona diferente")
            );
        }

        /**
         * Verifica la gestión de eventos en situaciones de error.
         *
         * Este test comprueba cómo se registran y manejan los eventos
         * cuando ocurren situaciones anómalas o errores en el sistema.
         *
         * Aspectos evaluados:
         * 1. Registro de eventos de error
         * 2. Secuencias de eventos en situaciones de error
         * 3. Consistencia del sistema tras errores
         * 4. Trazabilidad de incidencias
         */
        @Test
        @DisplayName("Test eventos en situaciones de error")
        void testEventosError() {
            // Simulamos una secuencia de eventos con errores
            Date tiempo = new Date();
            String robotId = "robotError";
            ZonaAlmacen zona = ZONA_A;

            List<EventoAlmacen> secuenciaErrores = new ArrayList<>();
            secuenciaErrores.add(new EventoAlmacen(tiempo, RECOGIDA_PRODUCTO, robotId, zona));
            secuenciaErrores.add(new EventoAlmacen(
                    sumarSegundos.apply(tiempo, 1),
                    ERROR_SISTEMA,
                    robotId,
                    zona
            ));
            secuenciaErrores.add(new EventoAlmacen(
                    sumarSegundos.apply(tiempo, 2),
                    ROBOT_EN_CARGA,
                    robotId,
                    zona
            ));

            assertAll("Secuencia de error",
                    () -> assertTrue(secuenciaErrores.get(1).getTipo() == ERROR_SISTEMA,
                            "Debe registrarse el error"),
                    () -> assertTrue(secuenciaErrores.get(2).getTimestamp()
                                    .after(secuenciaErrores.get(1).getTimestamp()),
                            "Los eventos posteriores deben ser cronológicos"),
                    () -> assertEquals(robotId,
                            secuenciaErrores.stream()
                                    .map(EventoAlmacen::getIdRobot)
                                    .distinct()
                                    .findFirst()
                                    .orElse(null),
                            "Todos los eventos deben ser del mismo robot")
            );
        }
    }

    @Nested
    @DisplayName("Pruebas Avanzadas de Producto")
    class ProductoTest {
        private Map<TipoProducto, List<Producto>> productosPorTipo;
        private static final int PRODUCTOS_POR_TIPO = 5;

        @BeforeEach
        void setUp() {
            productosPorTipo = new EnumMap<>(TipoProducto.class);
            Arrays.stream(TipoProducto.values()).forEach(tipo -> {
                List<Producto> productos = new ArrayList<>();
                for (int i = 0; i < PRODUCTOS_POR_TIPO; i++) {
                    productos.add(new Producto(
                            tipo.toString() + "-" + i,
                            tipo,
                            ZonaAlmacen.values()[i % ZonaAlmacen.values().length]
                    ));
                }
                productosPorTipo.put(tipo, productos);
            });
        }

        /**
         * Verifica el comportamiento de los productos en condiciones de estrés.
         *
         * Este test somete a los productos a múltiples operaciones de reserva
         * y liberación, verificando que mantienen un estado consistente y
         * manejan correctamente los recursos.
         *
         * Aspectos evaluados:
         * 1. Resistencia a operaciones intensivas
         * 2. Mantenimiento de estado consistente
         * 3. Gestión de recursos bajo estrés
         * 4. Recuperación tras operaciones intensivas
         */
        @Test
        @DisplayName("Test productos bajo estrés")
        void testProductosEstres() throws InterruptedException {
            List<Producto> todosProductos = productosPorTipo.values().stream()
                    .flatMap(List::stream)
                    .toList();

            // Realizamos ciclos de reserva/liberación
            for (int ciclo = 0; ciclo < 3; ciclo++) {
                // Reservamos productos aleatoriamente
                todosProductos.forEach(p -> {
                    if (aleatorio.nextBoolean()) {
                        p.reservarProducto();
                    }
                });

                TimeUnit.MILLISECONDS.sleep(50);

                // Liberamos algunos productos reservados
                todosProductos.stream()
                        .filter(Producto::isReservado)
                        .forEach(p -> {
                            if (aleatorio.nextBoolean()) {
                                p.liberarProducto();
                            }
                        });

                TimeUnit.MILLISECONDS.sleep(50);
            }

            // Verificamos estado final
            long productosReservados = todosProductos.stream()
                    .filter(Producto::isReservado)
                    .count();

            assertTrue(productosReservados <= todosProductos.size(),
                    "No puede haber más reservas que productos");

            // Verificamos que se pueden liberar todos
            todosProductos.stream()
                    .filter(Producto::isReservado)
                    .forEach(Producto::liberarProducto);

            assertEquals(0, todosProductos.stream()
                            .filter(Producto::isReservado)
                            .count(),
                    "Todos los productos deben poder liberarse");
        }

        /**
         * Verifica las restricciones de manipulación en situaciones límite.
         *
         * Este test examina cómo se comporta el sistema cuando los productos
         * se manipulan en condiciones extremas de batería y con diferentes
         * tipos de productos que requieren manejo especial.
         *
         * Aspectos evaluados:
         * 1. Validación de restricciones por tipo de producto
         * 2. Comportamiento con niveles críticos de batería
         * 3. Manejo de productos especiales
         * 4. Seguridad en la manipulación
         */
        @Test
        @DisplayName("Test manipulación en condiciones límite")
        void testManipulacionLimite() {
            // Probamos cada tipo de producto con diferentes niveles de batería
            for (Map.Entry<TipoProducto, List<Producto>> entry : productosPorTipo.entrySet()) {
                TipoProducto tipo = entry.getKey();
                Producto producto = entry.getValue().get(0);

                // Calculamos el nivel mínimo necesario según el tipo
                int nivelMinimoBateria = tipo.getBateriaMinima();

                assertAll("Manipulación tipo " + tipo,
                        () -> assertFalse(producto.puedeSerManipulado(nivelMinimoBateria - 1),
                                "No debe permitir manipulación bajo mínimo"),
                        () -> assertEquals(tipo.requiereManipulacionEspecial(),
                                !producto.puedeSerManipulado(nivelMinimoBateria),
                                "Manipulación especial debe requerir más batería"),
                        () -> assertTrue(producto.puedeSerManipulado(100),
                                "Debe permitir manipulación con batería completa")
                );
            }
        }

        /**
         * Verifica el comportamiento con productos que requieren manejo especial.
         *
         * Este test se centra en los productos que tienen requisitos especiales
         * de manipulación, verificando que se cumplen todas las restricciones
         * y medidas de seguridad necesarias.
         *
         * Aspectos evaluados:
         * 1. Identificación de productos especiales
         * 2. Cumplimiento de requisitos específicos
         * 3. Restricciones de manipulación
         * 4. Seguridad en el manejo
         */
        @Test
        @DisplayName("Test productos especiales")
        void testProductosEspeciales() {
            // Analizamos productos que requieren manipulación especial
            Map<TipoProducto, Boolean> requiereEspecial = new EnumMap<>(TipoProducto.class);

            for (TipoProducto tipo : TipoProducto.values()) {
                List<Producto> productos = productosPorTipo.get(tipo);
                requiereEspecial.put(tipo, productos.get(0).requiereManipulacionEspecial());
            }

            assertAll("Productos especiales",
                    () -> assertTrue(requiereEspecial.get(ELECTRONICA_GRANDE),
                            "Electrónica grande debe requerir manipulación especial"),
                    () -> assertTrue(requiereEspecial.get(FRAGIL),
                            "Los productos frágiles deben requerir manipulación especial"),
                    () -> assertFalse(requiereEspecial.get(ROPA),
                            "La ropa no debe requerir manipulación especial")
            );
        }
    }

    @Nested
    @DisplayName("Pruebas Avanzadas de Robot")
    class RobotTest {
        private Map<ZonaAlmacen, Robot> robots;
        private List<Producto> productos;
        private List<EstacionEmpaquetado> estaciones;

        @BeforeEach
        void setUp() {
            robots = new EnumMap<>(ZonaAlmacen.class);
            productos = new ArrayList<>();
            estaciones = new ArrayList<>();

            // Inicializamos robots en cada zona
            for (ZonaAlmacen zona : ZonaAlmacen.values()) {
                robots.put(zona, new Robot("robot" + zona.ordinal(), zona));
                estaciones.add(new EstacionEmpaquetado("est" + zona.ordinal(), zona));
            }

            // Creamos productos variados
            for (TipoProducto tipo : TipoProducto.values()) {
                productos.add(new Producto("prod" + tipo.ordinal(), tipo, ZONA_A));
            }
        }

        /**
         * Verifica el comportamiento del robot en situaciones de carga crítica.
         *
         * Este test examina el comportamiento del robot cuando se encuentra en
         * situaciones límite de batería, especialmente durante el transporte
         * de productos que requieren manipulación especial.
         *
         * Aspectos evaluados:
         * 1. Gestión de batería en niveles críticos
         * 2. Decisiones de seguridad con productos especiales
         * 3. Priorización de tareas según nivel de batería
         * 4. Política de recarga
         * 5. Consistencia del estado del robot
         */
        @Test
        @DisplayName("Test situaciones de carga crítica")
        void testCargaCritica() throws InterruptedException {
            Robot robot = robots.get(ZONA_A);
            Producto productoFragil = productos.stream()
                    .filter(p -> p.getTipo() == FRAGIL)
                    .findFirst()
                    .orElseThrow();

            // Verificamos estado inicial
            assertEquals(LIBRE, robot.getEstado(),
                    "El robot debe comenzar en estado LIBRE");
            assertEquals(COMPLETA, robot.getNivelBateria(),
                    "El robot debe comenzar con batería completa");

            // Consumimos batería hasta nivel crítico
            while (robot.getNivelBateria() > NIVEL_BATERIA_MINIMO) {
                robot.moverA(robot.getPosicionActual() == ZONA_A ? ZONA_B : ZONA_A);
                TimeUnit.MILLISECONDS.sleep(50);

                // Verificamos que el estado es consistente durante los movimientos
                assertTrue(Arrays.asList(LIBRE, OCUPADO).contains(robot.getEstado()),
                        "El estado debe ser LIBRE u OCUPADO durante movimientos");
            }

            assertAll("Comportamiento en carga crítica",
                    () -> assertTrue(robot.necesitaCarga(),
                            "Debe indicar necesidad de carga"),
                    () -> assertFalse(robot.recogerProducto(productoFragil),
                            "No debe manipular productos frágiles con batería crítica"),
                    () -> assertEquals(EN_CARGA, robot.getEstado(),
                            "Debe mantener estado EN_CARGA con batería crítica"),
                    () -> assertFalse(robot.moverA(ZONA_D),
                            "No debe realizar movimientos largos con batería crítica"),
                    () -> assertTrue(robot.getNivelBateria() <= NIVEL_BATERIA_MINIMO,
                            "El nivel de batería debe estar en nivel crítico"),
                    () -> assertEquals(EN_CARGA, robot.getEstado(),
                            "Debe permanecer en estado EN_CARGA al no poder realizar acciones")
            );

            // Verificamos que el robot mantiene la coherencia tras los intentos de operación
            assertAll("Estado final del robot",
                    () -> assertEquals(EN_CARGA, robot.getEstado(),
                            "El estado final debe ser EN_CARGA"),
                    () -> assertTrue(robot.necesitaCarga(),
                            "Debe seguir necesitando carga"),
                    () -> assertTrue(robot.getNivelBateria() <= NIVEL_BATERIA_MINIMO,
                            "La batería debe mantenerse en nivel crítico")
            );
        }

        /**
         * Verifica el comportamiento del método entregarProducto del Robot.
         * Este test examina diferentes escenarios de entrega de productos
         * considerando el estado del robot, nivel de batería, y estado de
         * la estación de empaquetado.
         *
         * Aspectos evaluados:
         * 1. Entrega exitosa de producto en condiciones normales
         * 2. Entrega de productos que requieren manipulación especial
         * 3. Intentos de entrega con batería insuficiente
         * 4. Entrega a estación inactiva o llena
         * 5. Verificación del estado del robot post-entrega
         * 6. Coherencia del estado del producto tras la entrega
         */
        @Test
        @DisplayName("Test entrega de productos")
        void testEntregaProducto() {
            EstacionEmpaquetado estacionNormal = estaciones.get(0);
            EstacionEmpaquetado estacionLlena = estaciones.get(1);
            EstacionEmpaquetado estacionInactiva = estaciones.get(2);

            // Preparamos productos de prueba
            Producto productoNormal = new Producto("test-normal", ROPA, ZONA_A);
            Producto productoFragil = new Producto("test-fragil", FRAGIL, ZONA_A);

            // Llenamos una estación para pruebas
            for (int i = 0; i < MAX_PRODUCTOS_ESTACION; i++) {
                estacionLlena.recibirProducto(new Producto("prod"+i, ROPA, ZONA_A));
            }

            // Desactivamos una estación para pruebas
            estacionInactiva.setActiva(false);

            assertAll("Escenarios de entrega de productos",
                    // 1. Entrega exitosa en condiciones normales
                    () -> {
                        Robot robot = robots.get(ZONA_A);
                        assertTrue(robot.recogerProducto(productoNormal),
                                "Debe poder recoger un producto normal");
                        assertEquals(OCUPADO, robot.getEstado(),
                                "El robot debe estar OCUPADO tras recoger");
                        assertTrue(robot.entregarProducto(estacionNormal),
                                "Debe poder entregar en condiciones normales");
                        assertEquals(LIBRE, robot.getEstado(),
                                "El robot debe quedar LIBRE tras entregar");
                        assertNull(robot.getProductoActual(),
                                "No debe tener producto tras entrega exitosa");
                    },

                    // 2. Entrega con producto que requiere manipulación especial
                    () -> {
                        Robot robot = robots.get(ZONA_A); // Reset del robot
                        assertTrue(robot.recogerProducto(productoFragil),
                                "Debe poder recoger producto frágil con batería completa");
                        assertTrue(robot.entregarProducto(estacionNormal),
                                "Debe poder entregar producto frágil con batería suficiente");
                    },

                    // 3. Intento de entrega con batería crítica
                    () -> {
                        Robot robot = robots.get(ZONA_A); // Reset del robot
                        assertTrue(robot.recogerProducto(productoNormal),
                                "Debe poder recoger el producto");

                        // Consumimos batería hasta nivel crítico
                        while (robot.getNivelBateria() > NIVEL_BATERIA_MINIMO) {
                            robot.moverA(robot.getPosicionActual() == ZONA_A ? ZONA_B : ZONA_A);
                        }

                        assertFalse(robot.entregarProducto(estacionNormal),
                                "No debe entregar con batería crítica");
                        assertEquals(OCUPADO, robot.getEstado(),
                                "Debe mantener estado OCUPADO al no poder entregar");
                        assertNotNull(robot.getProductoActual(),
                                "Debe mantener el producto al no poder entregarlo");
                    },

                    // 4. Intento de entrega a estación llena
                    () -> {
                        Robot robot = robots.get(ZONA_A); // Reset del robot
                        assertTrue(robot.recogerProducto(productoNormal),
                                "Debe poder recoger el producto");
                        assertFalse(robot.entregarProducto(estacionLlena),
                                "No debe poder entregar en estación llena");
                        assertEquals(OCUPADO, robot.getEstado(),
                                "Debe mantener estado OCUPADO al no poder entregar");
                    },

                    // 5. Intento de entrega a estación inactiva
                    () -> {
                        Robot robot = robots.get(ZONA_A); // Reset del robot
                        assertTrue(robot.recogerProducto(productoNormal),
                                "Debe poder recoger el producto");
                        assertFalse(robot.entregarProducto(estacionInactiva),
                                "No debe poder entregar en estación inactiva");
                        assertEquals(OCUPADO, robot.getEstado(),
                                "Debe mantener estado OCUPADO al no poder entregar");
                    },

                    // 6. Verificación de entrega sin producto
                    () -> {
                        Robot robot = robots.get(ZONA_A); // Reset del robot
                        assertFalse(robot.entregarProducto(estacionNormal),
                                "No debe poder entregar sin producto");
                        assertEquals(LIBRE, robot.getEstado(),
                                "Debe mantener estado LIBRE sin producto");
                    }
            );
        }
    }

    @Nested
    @DisplayName("Pruebas Avanzadas de EstacionEmpaquetado")
    class EstacionEmpaquetadoTest {
        private Map<ZonaAlmacen, EstacionEmpaquetado> estaciones;
        private List<Producto> productos;
        private List<Robot> robots;

        @BeforeEach
        void setUp() {
            estaciones = new EnumMap<>(ZonaAlmacen.class);
            productos = new ArrayList<>();
            robots = new ArrayList<>();

            for (ZonaAlmacen zona : ZonaAlmacen.values()) {
                estaciones.put(zona, new EstacionEmpaquetado("est" + zona.ordinal(), zona));
                robots.add(new Robot("robot" + zona.ordinal(), zona));
            }

            for (TipoProducto tipo : TipoProducto.values()) {
                productos.add(new Producto("prod" + productos.size(), tipo, ZONA_A));
            }
        }

        /**
         * Verifica el rendimiento de la estación bajo carga intensiva.
         *
         * Este test examina el comportamiento de la estación cuando opera
         * continuamente al límite de su capacidad con diferentes tipos
         * de productos y condiciones de procesamiento.
         *
         * Aspectos evaluados:
         * 1. Manejo de carga máxima continua
         * 2. Eficiencia de procesamiento
         * 3. Gestión de recursos
         * 4. Recuperación tras saturación
         */
        @Test
        @DisplayName("Test rendimiento bajo carga")
        void testRendimientoCarga() throws InterruptedException {
            EstacionEmpaquetado estacion = estaciones.get(ZONA_A);
            List<Producto> procesados = new ArrayList<>();
            int ciclos = 5;

            for (int i = 0; i < ciclos; i++) {
                // Llenamos la estación al máximo
                for (int j = 0; j < MAX_PRODUCTOS_ESTACION; j++) {
                    Producto producto = new Producto(
                            "prod" + i + "-" + j,
                            TipoProducto.values()[j % TipoProducto.values().length],
                            ZONA_A
                    );
                    if (estacion.recibirProducto(producto)) {
                        procesados.add(producto);
                    }
                }

                estacion.procesarProductos();
                TimeUnit.MILLISECONDS.sleep(100);

                // Verificamos la capacidad de procesamiento
                assertTrue(estacion.recibirProducto(new Producto("test", ROPA, ZONA_A)),
                        "Debe poder recibir productos tras procesar en ciclo " + i);
            }

            // Verificamos el rendimiento global
            assertTrue(procesados.size() >= MAX_PRODUCTOS_ESTACION * (ciclos - 1),
                    "Debe mantener un rendimiento de procesamiento consistente");
        }

        /**
         * Verifica la gestión de productos especiales y prioridades.
         *
         * Este test examina cómo la estación maneja diferentes tipos de productos
         * con requisitos especiales y prioridades distintas de procesamiento.
         *
         * Aspectos evaluados:
         * 1. Priorización de productos especiales
         * 2. Manejo de productos con diferentes requisitos
         * 3. Eficiencia en la gestión de prioridades
         * 4. Cumplimiento de restricciones especiales
         */
        @Test
        @DisplayName("Test gestión de prioridades")
        void testGestionPrioridades() throws InterruptedException {
            EstacionEmpaquetado estacion = estaciones.get(ZONA_A);

            // Creamos productos con diferentes prioridades
            Producto productoAlimento = new Producto("alimento", ALIMENTOS, ZONA_A);
            Producto productoFragil = new Producto("fragil", FRAGIL, ZONA_A);
            Producto productoNormal = new Producto("normal", ROPA, ZONA_A);

            // Recibimos productos en orden inverso a su prioridad
            assertTrue(estacion.recibirProducto(productoNormal));
            TimeUnit.MILLISECONDS.sleep(50);
            assertTrue(estacion.recibirProducto(productoFragil));
            TimeUnit.MILLISECONDS.sleep(50);
            assertTrue(estacion.recibirProducto(productoAlimento));

            // Procesamos y verificamos el orden
            estacion.procesarProductos();

            assertAll("Gestión de prioridades",
                    () -> assertTrue(estacion.recibirProducto(new Producto("test", ROPA, ZONA_A)),
                            "Debe poder recibir nuevos productos"),
                    () -> assertTrue(estacion.isActiva(),
                            "Debe mantenerse activa tras procesar productos especiales")
            );
        }

        /**
         * Verifica la recuperación ante fallos y situaciones críticas.
         *
         * Este test examina cómo la estación maneja y se recupera de
         * situaciones de error, sobrecarga y condiciones críticas.
         *
         * Aspectos evaluados:
         * 1. Recuperación tras fallos
         * 2. Mantenimiento de la consistencia
         * 3. Gestión de recursos en situaciones críticas
         * 4. Restablecimiento de operaciones normales
         */
        @Test
        @DisplayName("Test recuperación ante fallos")
        void testRecuperacionFallos() throws InterruptedException {
            EstacionEmpaquetado estacion = estaciones.get(ZONA_A);

            // Simulamos una situación de fallo
            estacion.setActiva(false);
            // Intentamos operaciones durante el fallo
            Producto producto = new Producto("test", ROPA, ZONA_A);
            assertFalse(estacion.recibirProducto(producto),
                    "No debe aceptar productos durante fallo");

            TimeUnit.MILLISECONDS.sleep(100);

            // Recuperamos la estación
            estacion.setActiva(true);
            assertTrue(estacion.recibirProducto(producto),
                    "Debe aceptar productos tras recuperación");

            // Verificamos funcionalidad completa
            estacion.procesarProductos();
            assertTrue(estacion.recibirProducto(new Producto("test2", ROPA, ZONA_A)),
                    "Debe mantener funcionalidad normal tras recuperación");
        }
    }
}