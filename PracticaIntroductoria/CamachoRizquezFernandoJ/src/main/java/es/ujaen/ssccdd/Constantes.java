package es.ujaen.ssccdd;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public interface Constantes {
    // Generador aleatorio
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

    enum EstadoBicicleta {
        DISPONIBLE(50,0), ALQUILADA(70,4), EN_REPARACION(85,2),
        EN_TRANSITO(95,6), FUERA_DE_SERVICIO(100,0);

        private final int peso;
        private final int tiempoOperacion;

        EstadoBicicleta(int peso, int tiempoOperacion) {
            this.peso = peso;
            this.tiempoOperacion = tiempoOperacion;
        }

        /**
         * Nos devuelve un estado de la bicicleta de forma aleatoria
         * según el peso asignado a cada etiqueta
         * @return
         */
        public static EstadoBicicleta getEstado() {
            EstadoBicicleta resultado = null;
            int peso = aleatorio.nextInt(D100);
            int indice = 0;

            while( (indice < estadosBicicleta.length) && (resultado == null) ) {
                if( estadosBicicleta[indice].peso > peso )
                    resultado = estadosBicicleta[indice];

                indice++;
            }

            return resultado;
        }

        /**
         * Nos devuelve el tiempo en el que estará en este estado
         * @return El tiempo máximo para este estado
         */
        public int getTiempoOperacion() {
            return tiempoOperacion;
        }
    }

    int D100 = 100; // Simula una tirada de dado de 100 caras
    EstadoBicicleta[] estadosBicicleta = EstadoBicicleta.values();
    int TIEMPO_HASTA_MANTENIMIENTO = 12; // segundo, simula el tiempo necesario para el mantenimiento
    int PROB_AVERIA = 20; // 20%, simula la posibilidad de avería en el uso de la bicicleta
    int IGUAL = 0;
    int MENOR = -1;
    int MAYOR = 1;
    int PRIMERO = 0;
}
