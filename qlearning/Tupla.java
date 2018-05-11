/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.idsia.agents.controllers.qlearning;

/**
 *
 * @author moises
 */
public class Tupla
{

    // Atributos

    /**
     * Estado
     */
    private double[] _estado;

    /**
     * Estado siguiente.
     */
    private double[] _estadoSiguiente;

    /**
     * Acción.
     */
    private double[] _accion;

    /**
     * Refuerzo.
     */
    private double _refuerzo;

    // Metodos

    /**
     * Constructor de la clase.
     * @param double estado. Estado actual de la transición.
     * @param double accion. Acción aplicable para realizar la transición.
     * @param double estado_siguiente. Estado al que se transitará.
     * @param double refuerzo. Recompensa que se obtendrá al realizar la transición.
     */
    public Tupla(double estado, double accion, double estado_siguiente, double refuerzo)
    {
        this._estado                = new double[1];
        this._estadoSiguiente       = new double[1];
        this._accion                = new double[1];

        this._estado[0]             = estado;
        this._accion[0]             = accion;
        this._estadoSiguiente[0]    = estado_siguiente;
        this._refuerzo              = refuerzo;
    }

    /**
     * Constructor de la clase.
     * @param double[] estado. Estado actual de la transición.
     * @param double[] accion. Acción aplicable para realizar la transición.
     * @param double[] estado_siguiente. Estado al que se transitará.
     * @param double refuerzo. Recompensa que se obtendrá al realizar la transición.
     */
    public Tupla(double[] estado, double[] accion, double[] estado_siguiente, double refuerzo)
    {
        this._estado            = estado;
        this._accion            = accion;
        this._estadoSiguiente   = estado_siguiente;
        this._refuerzo          = refuerzo;
    }

    /**
     * Devuelve el valor del atributo estado.
     */
    public double[] getEstado()
    {
        return this._estado;
    }

    /**
     * Devuelve el valor del atributo estado siguiente.
     */
    public double[] getEstadoSiguiente()
    {
        return this._estadoSiguiente;
    }

    /**
     * Devuelve el valor del atributo acción.
     */
    public double[] getAccion()
    {
        return this._accion;
    }

    /**
     * Devuelve el valor de una de las dimensiones del estado.
     * @param int posicion Dimensión del estado.
     */
    public double getEstado(int posicion)
    {
        return this._estado[posicion];
    }

    /**
     * Devuelve el valor de una de las dimensiones del estado siguiente.
     * @param int posicion Dimensión del estado siguiente.
     */
    public double getEstadoSiguiente(int posicion)
    {
        return this._estadoSiguiente[posicion];
    }

    /**
     * Devuelve el valor de una de las dimensiones de la acción.
     * @param int posicion Dimensión de la acción.
     */
    public double getAccion(int posicion)
    {
        return this._accion[posicion];
    }

    /**
     * Devuelve el valor del refuerzo de aplicar la acción.
     */
    public double getRefuerzo()
    {
        return this._refuerzo;
    }
}
