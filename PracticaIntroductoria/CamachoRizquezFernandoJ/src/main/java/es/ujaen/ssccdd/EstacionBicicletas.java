package es.ujaen.ssccdd;

import java.util.*;

import static es.ujaen.ssccdd.Constantes.*;
import static es.ujaen.ssccdd.Constantes.EstadoBicicleta.*;

public class EstacionBicicletas {
    private final String id;
    private final List<Bicicleta> bicicletasAsignadas;
    private int operacionesFueraDePlazo;

    /*
      El constructor debe inicializar los atributos de la clase y el identificador
      de una bicicleta es único
      ningún atributo de la clase puede ser null
     */

    public EstacionBicicletas(String id) {
        this.id = (id == null || id.trim().isEmpty()) ? UUID.randomUUID().toString() : id;
        this.bicicletasAsignadas = new ArrayList<>();
        this.operacionesFueraDePlazo = 0;
    }

    public EstacionBicicletas(String id, List<Bicicleta> bicicletasAsignadas) {
        this.id = id;
        this.bicicletasAsignadas = bicicletasAsignadas;
    }

    public EstacionBicicletas(String id, Bicicleta... bicicleta) {
        if( bicicleta == null || Arrays.stream(bicicleta).anyMatch(Objects::isNull)
                || Arrays.stream(bicicleta).map(Bicicleta::getId).distinct().count() < bicicleta.length )
            throw new IllegalArgumentException("No se deben permitir bicicletas duplicadas");

        this.id = (id == null || id.trim().isEmpty()) ? UUID.randomUUID().toString() : id;
        this.bicicletasAsignadas = new ArrayList<>(List.of(bicicleta));
        this.operacionesFueraDePlazo = 0;
    }

    // Incluir los get el funcionamiento correcto de la clase
    public String getId() {
        return id;
    }

    public List<Bicicleta> getBicicletasAsignadas() {
        return bicicletasAsignadas;
    }

    public int getOperacionesFueraDePlazo() {
        return operacionesFueraDePlazo;
    }

    /**
     * Permite saber el número de bibicletas que hay en la estación disponibles
     * para su alquiler
     * @return el número de bicicletas disponibles
     */
    public int getDisponibles() {
        return (int) bicicletasAsignadas.stream()
                .filter(bicicleta -> bicicleta.getEstado().equals(DISPONIBLE))
                .count();
    }

    /**
     * Saber el número de bicicletas para un estado dado que hay en la estación
     * @param estado el estado que se está consultando
     * @return el número de bicicletas para ese estado
     */
    public int getDisponibles( EstadoBicicleta estado ) {
        if (estado == null)
            throw new IllegalArgumentException("El estado no puede ser null");

        return (int) bicicletasAsignadas.stream()
                .filter(bicicleta -> bicicleta.getEstado().equals(estado))
                .count();
    }

    /**
     * Busca una bicicleta que esté disponible para su alquiler como resultado
     * de la operación. También cambia su estado a ALQUILADA
     * @return un optional con la bicicleta alquilada si hay alguna disponible
     */
    public Optional<Bicicleta> alquilarBicicleta() {
        //El optional hace que no trabajemos con nulos
        Optional<Bicicleta> resultado = Optional.empty();
        Iterator it = bicicletasAsignadas.iterator();

        while( it.hasNext() && resultado.isEmpty()) {
            Bicicleta bicicleta = (Bicicleta) it.next();

            if( bicicleta.getEstado().equals(DISPONIBLE) ) {
                bicicleta.setEstado(ALQUILADA);
                resultado = Optional.of(bicicleta);
            }
        }

        return resultado;
    }

    /**
     * Pasa el estado de la bicicleta a EN_TRANSITO si se recoge en el plazo establecido. En otro
     * caso pasará a DISPLONIBLE.
     * @param idBicicleta la bicicleta que está en el proceso de recogida
     * @return la bicicleta si se recoge en el plazo establecido
     */
    public Optional<Bicicleta> recogerBicicleta(String idBicicleta) {
        Optional<Bicicleta> resultado = Optional.empty();
        Iterator<Bicicleta> it = bicicletasAsignadas.iterator();

        if( idBicicleta == null )
            throw new IllegalArgumentException("La idBicicleta no puede ser null");

        while (it.hasNext() && resultado.isEmpty()) {
            Bicicleta actual = it.next();
            if (actual.getId().equals(idBicicleta) &&
                    actual.getEstado().equals(EstadoBicicleta.ALQUILADA)) {

                if (!vencimiento.test(actual.getFechaEstado())) {
                    actual.setEstado(EN_TRANSITO);
                    resultado = Optional.of(actual);
                } else {
                    actual.setEstado(EstadoBicicleta.DISPONIBLE);
                }
            }
        }

        return resultado;
    }

    /**
     * Devuelve la bicicleta a la estación de bicicletas, comprueba si se ha devuelto en el plazo establecido.
     * También comprueba si necesita mantenimiento por algún problema o porque ha alcanzado el plazo para ello.
     * @param bicicleta la bicicleta entregada
     * @return true si se ha completado la acción de devolución
     */
    public boolean devolverBicicleta(Bicicleta bicicleta) {
        boolean resultado = false;

        if( bicicleta == null )
            throw new IllegalArgumentException("La bicicleta no puede ser null");

        if( bicicleta.getEstado().equals(EN_TRANSITO) ) {
            if( vencimiento.test(sumarSegundos.apply(bicicleta.getFechaEstado(), bicicleta.getEstado().getTiempoOperacion()) ) )
                this.operacionesFueraDePlazo++;

            if( necesitaMantenimiento(bicicleta) ) {
                bicicleta.setEstado(FUERA_DE_SERVICIO);
                bicicleta.setFechaMantenimiento(sumarSegundos.apply(new Date(), TIEMPO_HASTA_MANTENIMIENTO));
            } else
                bicicleta.setEstado(DISPONIBLE);

            resultado = true;
        }

        return resultado;
    }

    /**
     * Devuelve una lista de bicicletas reparadas a la estación de bicicletas. Comprueba para cada una de ellas si se ha devuelto
     * en el plazo establecido. Todas ellas tendrán el estado DISPONIBLE.
     * @param listaBicicletas la lista de bicicletas que se devuelven
     * @return true si se ha completado la acción de devolución
     */
    public boolean bicicletasReparadas(List<Bicicleta> listaBicicletas) {
        for( Bicicleta bicicleta : listaBicicletas ) {
            bicicleta.setEstado(DISPONIBLE);
        }

        return !listaBicicletas.isEmpty();
    }

    /**
     * Comprueba si una bicicleta presenta algún tipo de avería o necesita mantenimiento.
     *
     * @param bicicleta la bicicleta que se va a verificar
     * @return true si la bicicleta tiene una avería o requiere mantenimiento, false en caso contrario
     */
    private boolean necesitaMantenimiento(Bicicleta bicicleta) {
        int posibleAveria = aleatorio.nextInt(D100);

        return posibleAveria < PROB_AVERIA || vencimiento.test(bicicleta.getFechaMantenimiento());
    }

    /**
     * Cambia el estado de las bicicletas FUERA_DE_SERVICIO a EN_REPARACION para su mantenimiento
     * @return la lista de bicicletas que se han de reparar
     */
    public List<Bicicleta> mantenimientoBicicletas() {
        List<Bicicleta> resultado = new ArrayList<>();
        Iterator<Bicicleta> it = bicicletasAsignadas.iterator();

        while (it.hasNext()) {
            Bicicleta bicicleta = it.next();
            if (bicicleta.getEstado().equals(FUERA_DE_SERVICIO)) {
                bicicleta.setEstado(EN_REPARACION);
                resultado.add(bicicleta);
            }
        }

        return resultado;
    }

    /**
     * Una representación legible de la estación de bicicletas
     *
     * @return el String que representa la estación de bicicletas
     */
    @Override
    public String toString() {
        return "EstacionBicicletas{" +
                "Id='" + id + '\'' +
                ", bicicletasAsignadas=" + bicicletasAsignadas +
                ", operacionesFueraDePlazo=" + operacionesFueraDePlazo +
                '}';
    }
}
