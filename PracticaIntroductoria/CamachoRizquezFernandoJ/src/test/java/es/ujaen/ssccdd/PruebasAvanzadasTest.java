package es.ujaen.ssccdd;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static es.ujaen.ssccdd.Constantes.*;
import static es.ujaen.ssccdd.Constantes.EstadoBicicleta.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Avanzadas del Sistema de Bicicletas")
public class PruebasAvanzadasTest {

    @Nested
    @DisplayName("Pruebas Avanzadas de Bicicleta")
    class BicicletaTest {
        @Test
        @DisplayName("Constructor con estado inicial")
        void constructorConEstadoInicial() {
            // Probar cada estado posible en la construcción
            for (EstadoBicicleta estado : EstadoBicicleta.values()) {
                Bicicleta bici = new Bicicleta("BICI-001", estado);

                // Verificar estado inicial
                assertEquals(estado, bici.getEstado(),
                        "El estado inicial debe ser el especificado en el constructor");

                // Verificar que las fechas no son nulas
                assertNotNull(bici.getFechaEstado(),
                        "La fecha de estado no debe ser nula");
                assertNotNull(bici.getFechaMantenimiento(),
                        "La fecha de mantenimiento no debe ser nula");

                // Verificar separación temporal correcta
                long diferencia = TimeUnit.SECONDS.convert(
                        bici.getFechaMantenimiento().getTime() - bici.getFechaEstado().getTime(),
                        TimeUnit.MILLISECONDS
                );
                assertEquals(TIEMPO_HASTA_MANTENIMIENTO, diferencia,
                        "La diferencia temporal debe ser exactamente TIEMPO_HASTA_MANTENIMIENTO");
            }

            // Verificar que el constructor con estado null lanza excepción
            assertThrows(IllegalArgumentException.class, () -> {
                new Bicicleta("BICI-001", null);
            }, "El constructor debe lanzar excepción si el estado es null");
        }

        @Test
        @DisplayName("ToString con información completa")
        void toStringCompleto() {
            EstadoBicicleta estadoInicial = DISPONIBLE;
            Bicicleta bici = new Bicicleta("BICI-TEST-001", estadoInicial);
            String resultado = bici.toString();

            // Verificar que incluye el ID
            assertTrue(resultado.contains("BICI-TEST-001"),
                    "El toString debe incluir el ID de la bicicleta");

            // Verificar que incluye el estado
            assertTrue(resultado.contains(estadoInicial.toString()),
                    "El toString debe incluir el estado actual");

            // Verificar que incluye las fechas
            assertTrue(resultado.contains(bici.getFechaEstado().toString()),
                    "El toString debe incluir la fecha de estado");
            assertTrue(resultado.contains(bici.getFechaMantenimiento().toString()),
                    "El toString debe incluir la fecha de mantenimiento");

        }
    }

    @Nested
    @DisplayName("Pruebas Avanzadas de EstacionBicicletas")
    class EstacionAdvancedTest {

        @Test
        @DisplayName("Constructor con número variable de bicicletas")
        void constructorVariasBicicletas() {
            // Caso 1: Construcción sin bicicletas
            EstacionBicicletas estacionVacia = new EstacionBicicletas("EST-001");
            assertEquals(0, estacionVacia.getDisponibles(DISPONIBLE),
                    "Una estación nueva sin bicicletas debe tener 0 bicicletas disponibles");

            // Caso 2: Construcción con una bicicleta
            Bicicleta bici1 = new Bicicleta("BICI-001");
            EstacionBicicletas estacionUna = new EstacionBicicletas("EST-002", bici1);
            assertEquals(1, estacionUna.getDisponibles(DISPONIBLE),
                    "La estación debe tener una bicicleta disponible");

            // Caso 3: Construcción con múltiples bicicletas
            Bicicleta bici2 = new Bicicleta("BICI-002");
            Bicicleta bici3 = new Bicicleta("BICI-003");
            EstacionBicicletas estacionVarias = new EstacionBicicletas("EST-003", bici1, bici2, bici3);
            assertEquals(3, estacionVarias.getDisponibles(DISPONIBLE),
                    "La estación debe tener tres bicicletas disponibles");

            // Caso 4: Construcción con bicicletas en diferentes estados
            Bicicleta biciReparacion = new Bicicleta("BICI-004");
            biciReparacion.setEstado(EN_REPARACION);
            Bicicleta biciAlquilada = new Bicicleta("BICI-005");
            biciAlquilada.setEstado(ALQUILADA);

            EstacionBicicletas estacionMixta = new EstacionBicicletas("EST-004",
                    bici1, biciReparacion, biciAlquilada);

            assertEquals(1, estacionMixta.getDisponibles(DISPONIBLE),
                    "Debe haber una bicicleta disponible");
            assertEquals(1, estacionMixta.getDisponibles(EN_REPARACION),
                    "Debe haber una bicicleta en reparación");
            assertEquals(1, estacionMixta.getDisponibles(ALQUILADA),
                    "Debe haber una bicicleta alquilada");

            // Caso 5: Verificar que no se permiten bicicletas duplicadas
            assertThrows(IllegalArgumentException.class, () -> {
                new EstacionBicicletas("EST-005", bici1, bici1);
            }, "No se deben permitir bicicletas duplicadas");

            // Caso 6: Verificar que no se permiten bicicletas null
            assertThrows(IllegalArgumentException.class, () -> {
                new EstacionBicicletas("EST-006", bici1, null, bici2);
            }, "No se deben permitir bicicletas null");
        }

        @Test
        @DisplayName("Test getDisponibles(estado) con diferentes estados y cambios")
        void testGetDisponiblesYCambios() {
            // Preparar bicicletas con diferentes estados
            Bicicleta[] bicicletas = {
                    new Bicicleta("BIKE_1", DISPONIBLE), // Por defecto DISPONIBLE
                    new Bicicleta("BIKE_2", DISPONIBLE), // Por defecto DISPONIBLE
                    new Bicicleta("BIKE_3", DISPONIBLE), // Por defecto DISPONIBLE
                    new Bicicleta("BIKE_4", ALQUILADA), // Cambiaremos a ALQUILADA
                    new Bicicleta("BIKE_5", FUERA_DE_SERVICIO)  // Cambiaremos a FUERA_DE_SERVICIO
            };

            // Crear estación con las bicicletas
            EstacionBicicletas estacion = new EstacionBicicletas("EST_TEST", bicicletas);

            // Verificar conteo inicial
            assertEquals(3, estacion.getDisponibles(EstadoBicicleta.DISPONIBLE),
                    "Debe haber 3 bicicletas disponibles");
            assertEquals(1, estacion.getDisponibles(EstadoBicicleta.ALQUILADA),
                    "Debe haber 1 bicicleta alquilada");
            assertEquals(1, estacion.getDisponibles(EstadoBicicleta.FUERA_DE_SERVICIO),
                    "Debe haber 1 bicicleta fuera de servicio");

            // Cambiar estado de una bicicleta y verificar actualización
            estacion.alquilarBicicleta(); // Cambia una DISPONIBLE a ALQUILADA

            assertEquals(2, estacion.getDisponibles(EstadoBicicleta.DISPONIBLE),
                    "Debe quedar 1 bicicleta menos disponible");
            assertEquals(2, estacion.getDisponibles(EstadoBicicleta.ALQUILADA),
                    "Debe haber 1 bicicleta más alquilada");

            // Verificar que lanza excepción con estado null
            assertThrows(IllegalArgumentException.class,
                    () -> estacion.getDisponibles(null),
                    "Debe lanzar IllegalArgumentException cuando el estado es null");
        }

        @Test
        @DisplayName("Test condiciones de contorno alquilarBicicleta()")
        void testCondicionesContornoAlquilarBicicleta() {
            // Test 1: Estación vacía
            EstacionBicicletas estacionVacia = new EstacionBicicletas("EST_VACIA");
            Optional<Bicicleta> resultadoVacia = estacionVacia.alquilarBicicleta();
            assertTrue(resultadoVacia.isEmpty(),
                    "Una estación sin bicicletas debe devolver Optional vacío");

            // Test 2: Estación con una única bicicleta
            Bicicleta unicaBici = new Bicicleta("BIKE_UNICA");
            EstacionBicicletas estacionUnica = new EstacionBicicletas("EST_UNICA", unicaBici);

            // Primera solicitud debe ser exitosa
            Optional<Bicicleta> resultadoUnica = estacionUnica.alquilarBicicleta();
            assertTrue(resultadoUnica.isPresent(),
                    "La única bicicleta debe poder ser alquilada");
            assertEquals("BIKE_UNICA", resultadoUnica.get().getId(),
                    "El ID de la bicicleta alquilada debe coincidir");
            assertEquals(EstadoBicicleta.ALQUILADA, resultadoUnica.get().getEstado(),
                    "El estado debe cambiar a ALQUILADA");

            // Segunda solicitud debe fallar
            Optional<Bicicleta> resultadoVacio = estacionUnica.alquilarBicicleta();
            assertTrue(resultadoVacio.isEmpty(),
                    "No debe haber más bicicletas disponibles");

            // Test 3: Estación con múltiples bicicletas - probar límites
            Bicicleta[] bicicletas = new Bicicleta[5];
            for (int i = 0; i < bicicletas.length; i++) {
                bicicletas[i] = new Bicicleta("BIKE_" + i);
            }

            EstacionBicicletas estacionMultiple = new EstacionBicicletas("EST_MULTIPLE", bicicletas);

            // Alquilar todas las bicicletas una por una
            for (int i = 0; i < bicicletas.length; i++) {
                Optional<Bicicleta> resultado = estacionMultiple.alquilarBicicleta();
                assertTrue(resultado.isPresent(),
                        "La bicicleta " + i + " debe poder ser alquilada");
                assertEquals(EstadoBicicleta.ALQUILADA, resultado.get().getEstado(),
                        "Cada bicicleta debe cambiar a estado ALQUILADA");
            }

            // Verificar que no quedan bicicletas disponibles
            Optional<Bicicleta> resultadoAgotado = estacionMultiple.alquilarBicicleta();
            assertTrue(resultadoAgotado.isEmpty(),
                    "No deben quedar bicicletas disponibles tras alquilar todas");

            // Test 4: Verificar consistencia del estado de la estación
            assertEquals(0, estacionMultiple.getDisponibles(EstadoBicicleta.DISPONIBLE),
                    "No deben quedar bicicletas disponibles");
            assertEquals(bicicletas.length, estacionMultiple.getDisponibles(EstadoBicicleta.ALQUILADA),
                    "Todas las bicicletas deben estar alquiladas");

            // Test 5: Intento de alquiler con bicicletas en otros estados
            Bicicleta biciReparacion = new Bicicleta("BIKE_REP");
            biciReparacion.setEstado(EstadoBicicleta.EN_REPARACION);
            EstacionBicicletas estacionMixta = new EstacionBicicletas("EST_MIXTA", biciReparacion);

            Optional<Bicicleta> resultadoReparacion = estacionMixta.alquilarBicicleta();
            assertTrue(resultadoReparacion.isEmpty(),
                    "No se debe poder alquilar una bicicleta en reparación");
        }

        @Test
        @DisplayName("Test condiciones de contorno recogerBicicleta()")
        void testCondicionesContornoRecogerBicicleta() throws InterruptedException {
            // Preparación de datos de prueba
            Bicicleta[] bicicletas = {
                    new Bicicleta("BIKE_1"),  // Se alquilará - recogida en tiempo
                    new Bicicleta("BIKE_2"),  // Se alquilará - recogida fuera de tiempo
                    new Bicicleta("BIKE_3"),  // Permanecerá disponible
                    new Bicicleta("BIKE_4", EN_REPARACION)   // Se pondrá en reparación
            };

            EstacionBicicletas estacion = new EstacionBicicletas("EST_TEST", bicicletas);

            // Test 1: Intentar recoger una bicicleta que no existe
            Optional<Bicicleta> resultadoNoExiste = estacion.recogerBicicleta("BIKE_INEXISTENTE");
            assertTrue(resultadoNoExiste.isEmpty(),
                    "No debe permitir recoger una bicicleta inexistente");

            // Test 2: Intentar recoger una bicicleta con ID null
            assertThrows(IllegalArgumentException.class,
                    () -> estacion.recogerBicicleta(null),
                    "Debe lanzar excepción al intentar recoger con ID null");

            // Test 3: Intentar recoger una bicicleta disponible (no alquilada)
            Optional<Bicicleta> resultadoDisponible = estacion.recogerBicicleta("BIKE_3");
            assertTrue(resultadoDisponible.isEmpty(),
                    "No debe permitir recoger una bicicleta que no está alquilada");

            // Test 4: Intentar recoger una bicicleta en reparación
            Optional<Bicicleta> resultadoReparacion = estacion.recogerBicicleta("BIKE_4");
            assertTrue(resultadoReparacion.isEmpty(),
                    "No debe permitir recoger una bicicleta en reparación");

            // Test 5: Recoger una bicicleta alquilada dentro del tiempo límite
            Optional<Bicicleta> alquilada1 = estacion.alquilarBicicleta();
            assertTrue(alquilada1.isPresent(), "Debe poder alquilar la primera bicicleta");

            Optional<Bicicleta> recogida1 = estacion.recogerBicicleta(alquilada1.get().getId());
            assertTrue(recogida1.isPresent(), "Debe permitir recoger la bicicleta en tiempo");
            assertEquals(EstadoBicicleta.EN_TRANSITO, recogida1.get().getEstado(),
                    "La bicicleta recogida debe pasar a estado EN_TRANSITO");

            // Test 6: Recoger una bicicleta alquilada fuera del tiempo límite
            Optional<Bicicleta> alquilada2 = estacion.alquilarBicicleta();
            assertTrue(alquilada2.isPresent(), "Debe poder alquilar la segunda bicicleta");

            // Simular paso del tiempo máximo de alquiler
            TimeUnit.SECONDS.sleep(ALQUILADA.getTiempoOperacion() + 1);


            Optional<Bicicleta> recogida2 = estacion.recogerBicicleta(alquilada2.get().getId());
            assertTrue(recogida2.isEmpty(),
                    "No debe permitir recoger una bicicleta fuera del tiempo límite");
            assertEquals(EstadoBicicleta.DISPONIBLE, alquilada2.get().getEstado(),
                    "La bicicleta no recogida a tiempo debe volver a estado DISPONIBLE");

            // Test 7: Intentar recoger una bicicleta ya recogida
            Optional<Bicicleta> recogerDosVeces = estacion.recogerBicicleta(recogida1.get().getId());
            assertTrue(recogerDosVeces.isEmpty(),
                    "No debe permitir recoger una bicicleta que ya está en tránsito");
        }

        @Test
        @DisplayName("Test condiciones de contorno devolverBicicleta()")
        void testCondicionesContornoDevolverBicicleta() throws InterruptedException {
            // Preparación de datos de prueba
            Bicicleta[] bicicletas = {
                    new Bicicleta("BIKE_1", EN_TRANSITO),  // Para devolver en tiempo
                    new Bicicleta("BIKE_2", EN_TRANSITO),  // Para devolver fuera de tiempo
                    new Bicicleta("BIKE_3", DISPONIBLE)   // Para estado incorrecto
            };

            EstacionBicicletas estacion = new EstacionBicicletas("EST_TEST", bicicletas);

            // Test 1: Devolver null
            assertThrows(IllegalArgumentException.class,
                    () -> estacion.devolverBicicleta(null),
                    "Debe lanzar excepción al intentar devolver una bicicleta null");

            // Test 2: Devolver una bicicleta que no está en tránsito
            assertFalse(estacion.devolverBicicleta(bicicletas[2]),
                    "No debe permitir devolver una bicicleta que no está en tránsito");

            // Test 3: Devolver una bicicleta en tiempo
            // Esperar 4 segundos (dentro del límite de 6 segundos para EN_TRANSITO)
            TimeUnit.SECONDS.sleep(4);

            assertTrue(estacion.devolverBicicleta(bicicletas[0]),
                    "Debe aceptar la devolución dentro del tiempo límite");
            assertTrue(
                    bicicletas[0].getEstado().equals(EstadoBicicleta.DISPONIBLE) ||
                            bicicletas[0].getEstado().equals(EstadoBicicleta.FUERA_DE_SERVICIO),
                    "La bicicleta debe estar en estado DISPONIBLE o FUERA_DE_SERVICIO"
            );

            // Test 4: Devolver una bicicleta fuera de tiempo
            // Esperar 7 segundos (superior al límite de 6 segundos para EN_TRANSITO)
            TimeUnit.SECONDS.sleep(7);

            assertTrue(estacion.devolverBicicleta(bicicletas[1]),
                    "Debe aceptar la devolución aunque sea fuera de tiempo");

            // Verificar contadores finales de la estación
            assertEquals(0, estacion.getDisponibles(EstadoBicicleta.EN_TRANSITO),
                    "No debe haber bicicletas en tránsito");
        }

        @Test
        @DisplayName("Test condiciones de contorno mantenimientoBicicletas")
        void testMantenimientoBicicletas() {
            // Preparación de datos de prueba
            Bicicleta[] bicicletas = {
                    new Bicicleta("BIKE_1", DISPONIBLE),
                    new Bicicleta("BIKE_2", FUERA_DE_SERVICIO),
                    new Bicicleta("BIKE_3", ALQUILADA),
                    new Bicicleta("BIKE_4", FUERA_DE_SERVICIO),
                    new Bicicleta("BIKE_5", EN_TRANSITO)
            };

            EstacionBicicletas estacion = new EstacionBicicletas("EST_TEST", bicicletas);

            // Ejecutamos el método a probar
            List<Bicicleta> bicicletasEnReparacion = estacion.mantenimientoBicicletas();

            // Verificaciones
            assertNotNull(bicicletasEnReparacion, "La lista de bicicletas en reparación no puede ser null");

            // Verificar que solo se devuelven las bicicletas que estaban fuera de servicio
            assertEquals(2, bicicletasEnReparacion.size(),
                    "Deberían devolverse exactamente las bicicletas que estaban fuera de servicio");

            // Verificar que las bicicletas devueltas son las correctas y están en estado EN_REPARACION
            for (Bicicleta bici : bicicletasEnReparacion) {
                assertEquals(EN_REPARACION, bici.getEstado(),
                        "Las bicicletas devueltas deben estar en estado EN_REPARACION");
                assertTrue(bici.getId().equals("BIKE_2") || bici.getId().equals("BIKE_4"),
                        "Solo deberían devolverse las bicicletas que estaban FUERA_DE_SERVICIO");
            }

            // Verificar que ya no hay bicicletas FUERA_DE_SERVICIO en la estación
            assertEquals(0, estacion.getDisponibles(FUERA_DE_SERVICIO),
                    "No deberían quedar bicicletas FUERA_DE_SERVICIO en la estación");

            // Verificar que el resto de bicicletas mantienen su estado original
            assertEquals(1, estacion.getDisponibles(DISPONIBLE),
                    "Debe mantenerse el número de bicicletas DISPONIBLES");
            assertEquals(1, estacion.getDisponibles(ALQUILADA),
                    "Debe mantenerse el número de bicicletas ALQUILADAS");
            assertEquals(1, estacion.getDisponibles(EN_TRANSITO),
                    "Debe mantenerse el número de bicicletas EN_TRANSITO");
            assertEquals(2, estacion.getDisponibles(EN_REPARACION),
                    "Debe haber dos bicicletas EN_REPARACION");

            // Verificar que al volver a llamar al método devuelve una lista vacía
            List<Bicicleta> segundaLlamada = estacion.mantenimientoBicicletas();
            assertTrue(segundaLlamada.isEmpty(),
                    "Una segunda llamada debería devolver una lista vacía");
        }
    }
}
