package es.ujaen.ssccdd;

import java.util.Date;
import java.util.UUID;

import static es.ujaen.ssccdd.Constantes.*;
import static es.ujaen.ssccdd.Constantes.EstadoBicicleta.DISPONIBLE;

public class Bicicleta implements Comparable<Bicicleta> {
    private final String id;
    private EstadoBicicleta estado;
    private Date fechaEstado;
    private Date fechaMantenimiento;

    /*
      El constructor debe inicializar los atributos de la clase y el identificador
      de una bicicleta es único
      ningún atributo de la clase puede ser null
     */

    public Bicicleta(String id) {
        this.id = (id == null || id.trim().isEmpty()) ? UUID.randomUUID().toString() : id;
        this.estado = DISPONIBLE;
        this.fechaEstado = new Date();
        this.fechaMantenimiento = sumarSegundos.apply(fechaEstado, TIEMPO_HASTA_MANTENIMIENTO);
    }

    public Bicicleta(String id, EstadoBicicleta estado) {
        if (estado == null)
            throw new IllegalArgumentException("El estado de la bicicleta no puede ser null");

        this.id = (id == null || id.trim().isEmpty()) ? UUID.randomUUID().toString() : id;
        this.estado = estado;
        this.fechaEstado = new Date();
        this.fechaMantenimiento = sumarSegundos.apply(fechaEstado, TIEMPO_HASTA_MANTENIMIENTO);

    }

    // Incluir los get y set para el funcionamiento correcto de la clase

    public String getId() {
        return id;
    }

    public EstadoBicicleta getEstado() {
        return estado;
    }

    public void setEstado(EstadoBicicleta estado) {
        this.estado = estado;
        this.fechaEstado = new Date();
    }

    public Date getFechaEstado() {
        return fechaEstado;
    }

    public Date getFechaMantenimiento() {
        return fechaMantenimiento;
    }

    public void setFechaMantenimiento(Date fechaMantenimiento) {
        this.fechaMantenimiento = fechaMantenimiento;
    }

    /**
     * Da una representación legible de un objeto bicicleta
     * @return el String que representa a una bicicleta
     */
    @Override
    public String toString() {
        return "Bicicleta{" +
                "Id='" + id + '\'' +
                ", estado=" + estado +
                ", fechaEstado=" + fechaEstado +
                ", fechaMantenimiento=" + fechaMantenimiento +
                '}';
    }

    /**
     * Permite comparar dos bicicletas para ordenarlas
     * @param bicileta con la que se tiene que hacer la comparación.
     * @return el orden lexicográfico atendiendo al identificador de la bicicleta
     */
    @Override
    public int compareTo(Bicicleta bicileta) {
        return this.id.compareTo(bicileta.getId());
    }
}
