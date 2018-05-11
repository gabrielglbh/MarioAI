package ch.idsia.agents.controllers.qlearning;

import java.util.*;

public class QLearning
{
	// Atributos

        /**
	 * Referencia al cuantificador vectorial de los estados y acciones.
	 */
	VQ _cuatificador;

	/**
	 * Array que contiene los valores de la funcion Q, para cada estado y accion cuantificados.
	 */
	double[][] _tablaQ;

	/**
	 * Valor del parametro alpha.
	 */
	double _alpha;

	/**
	 * Valor del parametro gamma.
	 */
	double _gamma;

	/**
	 * Valor del parametro epsilon
	 */
	double _epsilon;

	// Metodos

	/**
	 * Constructor de la clase.
         * @param double valorInicial Valor de inicizalización de la tabla q.
	 * @param double alpha Valor del parametro alpha.
	 * @param double gamma Valor del parametro gamma.
         * @param int dimensionEstados Dimension de los estados del cuantificador vectorial.
         * @param int dimensionAcciones Dimension de las acciones del cuantificador vectorial.
	 */
	public QLearning(double valorInicial, double alpha, double gamma, String[] estados, String[] acciones, int numeroEstados, int numeroAcciones)
        {
            this._cuatificador   = new VQ(estados, acciones, numeroEstados, numeroAcciones);
            this._tablaQ         = new double[numeroEstados][numeroAcciones];
            this._alpha          = alpha;
            this._gamma          = gamma;

            this._tablaQ         = new double[numeroEstados][numeroAcciones];

            for(int i = 0; i < this._tablaQ.length; i++)
            {
                for(int j = 0; j < this._tablaQ[0].length; j++)
                {
                    this._tablaQ[i][j] = valorInicial;
                }
            }
        }


        /**
	 * Constructor de la clase.
	 * @param double valorInicial Valor de inicizalización de la tabla q.
	 * @param double alpha Valor del parametro alpha.
	 * @param double gamma Valor del parametro gamma.
         * @param int numeroEstados Numero de estados del cuantificador vectorial de los estados.
	 * @param int dimensionEstados Dimension de los estados del cuantificador vectorial.
	 * @param int numeroAcciones Numero de acciones del cuantificador vectorial.
	 * @param int dimensionAcciones Dimension de las acciones del cuantificador vectorial.
	 */
        public QLearning(double valorInicial, double alpha, double gamma, String[][] estados, String[][] acciones, int numeroEstados, int dimensionEstados, int numeroAcciones, int dimensionAcciones)
        {
            this._cuatificador   = new VQ(estados, acciones, numeroEstados, dimensionEstados, numeroAcciones, dimensionAcciones);
            this._tablaQ         = new double[numeroEstados][numeroAcciones];
            this._alpha          = alpha;
            this._gamma          = gamma;

            this._tablaQ         = new double[numeroEstados][numeroAcciones];

            for(int i = 0; i < this._tablaQ.length; i++)
            {
                for(int j = 0; j < this._tablaQ[0].length; j++)
                {
                    this._tablaQ[i][j] = valorInicial;
                }
            }
        }

	/**
	 * Inicializa los valores de la tabla Q al valor indicado como parametro.
	 * @param int valorInicial Valor al que se inicializan los valores de la tabla Q.
	 */
	public void inicializarTablaQ(int valorInicial)
        {
            for(int i = 0; i < this._tablaQ.length; i++)
            {
                for(int j = 0; j < this._tablaQ[0].length; j++)
                {
                    this._tablaQ[i][j] = valorInicial;
                }
            }
	}

	/**
	 * Realiza una actualizacion de la tabla Q en funcion del estado, la accion y el refuerzo obtenidos.
	 * @param double[] estado Estado donde se encuentra el agente.
	 * @param double[] accion Accion que ejecuta el agente.
	 * @param double[] estado Estado al que transita el agente al aplicar la acción.
         * @param double refuerzo Refuerzo obtenido por el agente al ejecutar la accion en el estado dado.
         */
	public void aprender(double[] estado, double[] accion, double[] estadoTransicion, double refuerzo)
        {
            int estadoCuan = 0, accionCuan = 0, estadoTransicionCuan = 0;

            estadoCuan = this._cuatificador.cuantificaEstado(estado);
            //System.out.println("Estado cuantificado: " + estadoCuan);
            estadoTransicionCuan = this._cuatificador.cuantificaEstado(estadoTransicion);
            //System.out.println("Estado transicion cuantificado: " + estadoTransicionCuan);
            accionCuan = this._cuatificador.cuantificaAccion(accion);
            //System.out.println("Accion cuantificada: " + accionCuan);
            double maxQa = obtenerFuncionQMax(estadoTransicionCuan);

            this._tablaQ[estadoCuan][accionCuan] = this._tablaQ[estadoCuan][accionCuan] + this._alpha *(refuerzo + (this._gamma * maxQa) - this._tablaQ[estadoCuan][accionCuan]);
	}

	/**
	 * Realiza una actualizacion de la tabla Q en funcion del estado, la accion y el refuerzo obtenidos.
	 * @param Tupla tupla Tupla con la información de la transición.
         */
        public void actualizarTablaQ(Tupla tupla)
        {
            int estadoCuan = 0, accionCuan = 0, estadoTransicionCuan = 0;

            estadoCuan = this._cuatificador.cuantificaEstado(tupla.getEstado());
            //System.out.println("Estado cuantificado: " + estadoCuan);
            estadoTransicionCuan = this._cuatificador.cuantificaEstado(tupla.getEstadoSiguiente());
            //System.out.println("Estado transicion cuantificado: " + estadoTransicionCuan);
            accionCuan = this._cuatificador.cuantificaAccion(tupla.getAccion());
            //System.out.println("Accion cuantificada: " + accionCuan);
            double maxQa = obtenerFuncionQMax(estadoTransicionCuan);

            this._tablaQ[estadoCuan][accionCuan] = this._tablaQ[estadoCuan][accionCuan] + this._alpha *(tupla.getRefuerzo() + (this._gamma * maxQa) - this._tablaQ[estadoCuan][accionCuan]);
	}


        /**
        * Obtiene el maximo valor de la funcion Q para el estado indicado.
        * @param int estado Estado para el cual se quiere calcular el maximo de la funcion Q.
        * @return double max Maximo valor de la funcion Q para el estado indicado.
        */
        public double obtenerFuncionQMax(int estado)
        {
            double max = this._tablaQ[estado][0];

            for(int i = 1; i < this._tablaQ[estado].length; i++)
                max = (this._tablaQ[estado][i] > max) ? this._tablaQ[estado][i]:max;

            return max;
	}

	/**
	 * Obtiene el maximo valor de la funcion Q para el estado indicado.
	 * @param double[] estado Estado para el cual se quiere calcular el maximo de la funcion Q.
	 * @return double max Maximo valor de la funcion Q para el estado indicado.
	 */
	public double obtenerFuncionQMax(double[] estado)
        {
            int estadoCuan  = this._cuatificador.cuantificaEstado(estado);
            double max      = this._tablaQ[estadoCuan][0];

            //System.out.println("Estado cuantificado " + estadoCuan);

            for(int i = 1; i < this._tablaQ[estadoCuan].length; i++)
                max = (this._tablaQ[estadoCuan][i] > max) ? this._tablaQ[estadoCuan][i]:max;

            return max;
	}

	/**
	 * Obtiene la accion correspondiente al valor maximo de la funcion Q.
	 * @param double[] estado Estado para el cual se quiere obtener la mejor accion posible.
	 * @return double[] acciones
	 */
	public double[] obtenerMejorAccion(double[] estado)
        {
            int estadoCuan  = this._cuatificador.cuantificaEstado(estado);
            int mejorAccion = 0;

            //System.out.println("Estado cuantificado " + estadoCuan);

            double max      = this._tablaQ[estadoCuan][0];

            for(int i = 1; i < this._tablaQ[estadoCuan].length; i++)
            {
                if(this._tablaQ[estadoCuan][i] > max)
                {
                    max = this._tablaQ[estadoCuan][i];
                    mejorAccion = i;
		}
            }

            return this._cuatificador.getAcciones(mejorAccion);
	}

	/**
	 * Obtiene una accion utilizando la estrategia e-greedy
	 * @param double[] estado Estado para el cual se quiere obtener la accion a ejecutar.
	 * @return double[] acciones
	 */
	public double[] obtenerAccionEGreedy(double[] estado)
        {
            //double[] accion = new double[this._cuatificador.getDimensionAcciones()];
            double greedy   = (1 - this._epsilon);

            double dado     = Math.random();

            if(dado < greedy)
                // Accion aleatoria
                return this._cuatificador.getAcciones((int) (Math.random() * this._cuatificador.getNumeroAcciones()));
            else
                // Mejor accion
                return this.obtenerMejorAccion(estado);
	}

	/**
	 * Incrementa el valor de epsilon en el valor especificado como parametro
	 * @param double incremento Incremento que se realiza en el valor de epsilon
	 */
	public void incrementarEpsilon(double incremento)
        {
            if(this._epsilon < 0.95)
            {
                this._epsilon += incremento;

                if(this._epsilon > 0.95)
                    this._epsilon = 0.95;
            }
	}

        /**
	 * Devuelve la tabla q generada.
	 */
        public double[][] getTablaQ()
        {
            return this._tablaQ;
        }

        public void mostrarTablaQ(){
	    	System.out.println("TABLA Q:");
	    	for(int j = 0; j < this._tablaQ[0].length; j++)
            {
                System.out.print("\ta"+j);
            }
            System.out.println();
            for(int i = 0; i < this._tablaQ.length; i++)
            {
	            System.out.print("s"+i+"\t");
                for(int j = 0; j < this._tablaQ[0].length; j++)
                {
                    System.out.print((double)Math.round(this._tablaQ[i][j]*100)/100 + "\t");
                }
                System.out.println();
            }
            System.out.println();

	    }
}
