package es.ujaen.ssccdd;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static es.ujaen.ssccdd.Constantes.*;
import static es.ujaen.ssccdd.Constantes.EstadoBicicleta.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Básicas del Sistema de Bicicletas")
public class PuebasBasicasTest {

    @Nested
    @DisplayName("Pruebas Básicas de Bicicleta")
    class BicicletaTest {

        @Test
        @DisplayName("Constructor no permite atributos nulos")
        void constructorNoNull() {
            Bicicleta bici = new Bicicleta("BICI-001");

            assertNotNull(bici.getId());
            assertNotNull(bici.getEstado());
            assertNotNull(bici.getFechaEstado());
            assertNotNull(bici.getFechaMantenimiento());
        }

        @Test
        @DisplayName("Obtener ID")
        void getId() {
            Bicicleta bici = new Bicicleta("BICI-001");
            assertEquals("BICI-001", bici.getId());
        }

        @Test
        @DisplayName("Obtener y cambiar estado")
        void getSetEstado() {
            Bicicleta bici = new Bicicleta("BICI-001");
            assertEquals(DISPONIBLE, bici.getEstado());

            bici.setEstado(ALQUILADA);
            assertEquals(ALQUILADA, bici.getEstado());
        }

        @Test
        @DisplayName("Obtener fecha de estado")
        void getFechaEstado() {
            Bicicleta bici = new Bicicleta("BICI-001");
            assertNotNull(bici.getFechaEstado());
        }

        @Test
        @DisplayName("Obtener fecha de mantenimiento")
        void getFechaMantenimiento() {
            Bicicleta bici = new Bicicleta("BICI-001");
            assertNotNull(bici.getFechaMantenimiento());
        }

        @Test
        @DisplayName("Establecer fecha de mantenimiento")
        void setFechaMantenimiento() {
            Bicicleta bici = new Bicicleta("BICI-001");
            Date nuevaFecha = new Date();
            bici.setFechaMantenimiento(nuevaFecha);
            assertEquals(nuevaFecha, bici.getFechaMantenimiento());
        }

        @Test
        @DisplayName("Comparación básica de bicicletas")
        void compareTo() {
            Bicicleta bici1 = new Bicicleta("BICI-001");
            Bicicleta bici2 = new Bicicleta("BICI-001");
            Bicicleta bici3 = new Bicicleta("BICI-002");

            assertEquals(IGUAL, bici1.compareTo(bici2), "Misma ID debe retornar 0");
            assertEquals(MENOR, bici1.compareTo(bici3), "ID menor debe retornar negativo");
            assertEquals(MAYOR, bici3.compareTo(bici1) , "ID mayor debe retornar positivo");
        }

        @Test
        @DisplayName("Representación en cadena")
        void toStringTest() {
            Bicicleta bici = new Bicicleta("BICI-001");
            String resultado = bici.toString();

            assertNotNull(resultado);
            assertTrue(resultado.contains("BICI-001"));
            assertTrue(resultado.contains(bici.getEstado().toString()));
        }
    }

    @Nested
    @DisplayName("Pruebas Básicas de EstacionBicicletas")
    class EstacionBicicletaTest {

        @Test
        @DisplayName("Constructor no permite atributos nulos")
        void constructorNoNull() {
            EstacionBicicletas estacion = new EstacionBicicletas("EST-001");

            assertNotNull(estacion.getId());
            assertNotNull(estacion.getBicicletasAsignadas());
        }

        @Test
        @DisplayName("Obtener número de bicicletas disponibles")
        void getDisponibles() {
            Bicicleta bici = new Bicicleta("BICI-001");
            EstacionBicicletas estacion = new EstacionBicicletas("EST-001", bici);

            assertEquals(1, estacion.getDisponibles());
        }

        @Test
        @DisplayName("Obtener número de bicicletas por estado")
        void getNumBicicletas() {
            Bicicleta bici = new Bicicleta("BICI-001");
            EstacionBicicletas estacion = new EstacionBicicletas("EST-001", bici);

            assertEquals(1, estacion.getDisponibles(DISPONIBLE));
            assertEquals(0, estacion.getDisponibles(ALQUILADA));
        }

        @Test
        @DisplayName("Alquiler de bicicleta")
        void alquilarBicicleta() {
            Bicicleta bici = new Bicicleta("BICI-001");
            EstacionBicicletas estacion = new EstacionBicicletas("EST-001", bici);

            Optional<Bicicleta> alquilada = estacion.alquilarBicicleta();

            assertTrue(alquilada.isPresent());
            assertEquals(ALQUILADA, alquilada.get().getEstado());
            assertEquals(0, estacion.getDisponibles());
        }

        @Test
        @DisplayName("Recoger bicicleta alquilada")
        void recogerBicicleta() {
            Bicicleta bici = new Bicicleta("BICI-001");
            EstacionBicicletas estacion = new EstacionBicicletas("EST-001", bici);

            estacion.alquilarBicicleta();
            Optional<Bicicleta> recogida = estacion.recogerBicicleta("BICI-001");

            assertTrue(recogida.isPresent());
            assertEquals(EN_TRANSITO, recogida.get().getEstado());
        }

        @Test
        @DisplayName("Devolver bicicleta")
        void devolverBicicleta() {
            Bicicleta bici = new Bicicleta("BICI-001");
            EstacionBicicletas estacion = new EstacionBicicletas("EST-001", bici);

            Optional<Bicicleta> alquilada = estacion.alquilarBicicleta();
            Optional<Bicicleta> recogida = estacion.recogerBicicleta("BICI-001");

            assertTrue(recogida.isPresent());
            boolean devuelta = estacion.devolverBicicleta(recogida.get());

            assertTrue(devuelta);
            assertEquals(1, estacion.getDisponibles() + estacion.getDisponibles(FUERA_DE_SERVICIO));
        }

        @Test
        @DisplayName("Bicicletas en mantenimiento")
        void mantenimientoBicicletas() {
            Bicicleta bici = new Bicicleta("BICI-001");
            bici.setEstado(FUERA_DE_SERVICIO);
            EstacionBicicletas estacion = new EstacionBicicletas("EST-001", bici);

            List<Bicicleta> paraReparar = estacion.mantenimientoBicicletas();

            assertFalse(paraReparar.isEmpty());
            assertEquals(EN_REPARACION, paraReparar.get(PRIMERO).getEstado());
        }

        @Test
        @DisplayName("Bicicletas reparadas")
        void bicicletasReparadas() {
            Bicicleta bici = new Bicicleta("BICI-001",EN_REPARACION);
            EstacionBicicletas estacion = new EstacionBicicletas("EST-001", bici);

            boolean resultados = estacion.bicicletasReparadas(List.of(bici));

            assertTrue(resultados);
            assertEquals(DISPONIBLE, bici.getEstado());
            assertEquals(1, estacion.getDisponibles());
        }

        @Test
        @DisplayName("ToString básico")
        void toStringTest() {
            EstacionBicicletas estacion = new EstacionBicicletas("EST-001");

            String resultado = estacion.toString();

            assertNotNull(resultado);
            assertTrue(resultado.contains("EST-001"));
        }
        @Test
        @DisplayName("Conteo de bicicletas en estado DISPONIBLE")
        void getDisponiblesAvanzado() {
            // Crear una estación con varias bicicletas
            Bicicleta[] bicicletas = new Bicicleta[5];
            for (int i = 0; i < bicicletas.length; i++) {
                bicicletas[i] = new Bicicleta("BICI-" + (i + 1));
            }

            EstacionBicicletas estacion = new EstacionBicicletas("EST-001", bicicletas);

            // Inicialmente todas las bicicletas están DISPONIBLE
            assertEquals(5, estacion.getDisponibles(),
                    "Inicialmente todas las bicicletas deben estar disponibles");

            // Cambiar estados de algunas bicicletas
            bicicletas[0].setEstado(ALQUILADA);
            bicicletas[1].setEstado(EN_REPARACION);
            bicicletas[2].setEstado(EN_TRANSITO);

            assertEquals(2, estacion.getDisponibles(),
                    "Deben quedar solo 2 bicicletas disponibles");

            // Devolver una bicicleta a DISPONIBLE
            bicicletas[0].setEstado(DISPONIBLE);

            assertEquals(3, estacion.getDisponibles(),
                    "Deben ser 3 bicicletas disponibles tras devolver una");

            // Poner todas en estado no DISPONIBLE
            for (Bicicleta bici : bicicletas) {
                bici.setEstado(FUERA_DE_SERVICIO);
            }

            assertEquals(0, estacion.getDisponibles(),
                    "No debe haber bicicletas disponibles");

            // Volver a poner todas como DISPONIBLE
            for (Bicicleta bici : bicicletas) {
                bici.setEstado(DISPONIBLE);
            }

            assertEquals(5, estacion.getDisponibles(),
                    "Todas las bicicletas deben estar disponibles de nuevo");
        }
    }
}
