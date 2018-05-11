package ch.idsia.agents.controllers.qlearning;

public class VQ
{
    // Atributos

    /**
    * Numero de estados.
    */
    int _numEstados;

    /**
    * Dimension de los estados.
    */
    int _dimEstados;

    /**
    * Numero de acciones.
    */
    int _numAcciones;

    /**
    * Dimension de las acciones.
    */
    int _dimAcciones;

    /**
    * Array bidimensional que contiene los estados.
    */
    double[][] _estados;

    /**
    * Array bidimensional que contiene las acciones.
    */
    double[][] _acciones;

    // Metodos

    /**
    * Constructor de la clase VQ.
    * @param String[] estado Lista de estados en formato numérico.
    * @param String[] acciones Lista de acciones en formato numérico.
    * @param int numeroEstados Número de estados del cuantificador vectorial de los estados.
    * @param int numeroAcciones Número de acciones del cuantificador vectorial.
    */
    public VQ(String[] estados, String[] acciones, int numeroEstados, int numeroAcciones)
    {

        int i = 0;

        this._numEstados    = numeroEstados;
	this._dimEstados    = 1;
	this._numAcciones   = numeroAcciones;
	this._dimAcciones   = 1;
	this._estados       = new double[numeroEstados][1];
	this._acciones      = new double[numeroAcciones][1];

        for (i = 0; i < estados.length; i++)
        {
            this._estados[i][0] = Double.parseDouble(estados[i]);
        }

        for (i = 0; i < acciones.length; i++)
        {
            this._acciones[i][0] = Double.parseDouble(acciones[i]);
        }
    }


    /**
    * Constructor de la clase VQ.
    * @param String[] estado Lista de estados en formato numérico.
    * @param String[] acciones Lista de acciones en formato numérico.
    * @param int numeroEstados Numero de estados del cuantificador vectorial de los estados.
    * @param int dimensionEstados Dimension de los estados del cuantificador vectorial.
    * @param int numeroAcciones Numero de acciones del cuantificador vectorial.
    * @param int dimensionAcciones Dimension de las acciones del cuantificador vectorial.
    */
    public VQ(String[][] Estados, String[][] Acciones, int numeroEstados, int dimensionEstados, int numeroAcciones, int dimensionAcciones)
    {

        int i, j;

        this._numEstados    = numeroEstados;
	this._dimEstados    = dimensionEstados;
	this._numAcciones   = numeroAcciones;
	this._dimAcciones   = dimensionAcciones;
	this._estados       = new double[numeroEstados][dimensionEstados];
	this._acciones      = new double[numeroAcciones][dimensionAcciones];


        for (i = 0; i < Estados.length; i++)
        {
            for (j = 0; i < Estados[i].length; j++)
                this._estados[i][j] = Double.parseDouble(Estados[i][j]);
        }


        for (i = 0; i < Acciones.length; i++)
        {
            for (j = 0; i < Acciones[i].length; j++)
                this._acciones[i][0] = Double.parseDouble(Estados[i][j]);
        }
    }


    /**
    * Devuelve el valor del estado pasado como parametro cuantificado.
    * @param double[] estad Estado que se quiere cuantificar.
    * @return int estado Valor del estado cuantificado.
    */
    public int cuantificaEstado(double[] estado)
    {
	int estado_actual;
	double dist_min;
        double aux = 0;

	dist_min        = distorsion(estado, this._estados[0]);
	estado_actual   = 0;

        for (int i=1; i < this._numEstados; i++)
        {
            aux = distorsion(estado,this._estados[i]);
            if (aux<dist_min)
            {
		dist_min = aux;
		estado_actual = i;
            }
	}

	return estado_actual;
    }


    /**
    * Devuelve el valor de la accion pasada como parametro cuantificada.
    * @param double[] acc Accion que se quiere cuantificar.
    * @return int accion Valor de la accion cuantificada.
    */
    public int cuantificaAccion(double[] accion)
    {
        int accion_actual;
        double dist_min;
        double aux = 0;

        dist_min = distorsion(accion, this._acciones[0]);
        accion_actual = 0;

        for (int i=1; i < this._numAcciones; i++)
        {
            aux = distorsion(accion, this._acciones[i]);

            if (aux < dist_min)
            {
                dist_min = aux;
                accion_actual = i;
            }
        }

        return accion_actual;
    }

    /**
    * Calcula la distancia euclidea que existe entre dos vectores de identica dimension.
    * @param double[] x Primero de los vectores.
    * @param double[] y Segundo de los vectores.
    * @return double d Distancia euclidea entre ambos vectores.
    */
    public double distorsion(double[] x, double[] y)
    {
        double distorsion = 0.0;

        for (int i=0; i< x.length;i++)
        {
            distorsion += Math.pow((x[i]-y[i]),2);
        }

        return distorsion;
    }

    /**
    * Devuelve el numero de estados del cuantificador.
    * @return int numEstados Numero de estados del cuantificador.
    */
    public int getNumEstados()
    {
        return this._numEstados;
    }

    /**
    * Devuelve la dimension de los estados.
    * @return int dimEstados Dimension de los estados.
    */
    public int getDimemsionEstados()
    {
        return this._dimEstados;
    }

    /**
    * Devuelve el numero de acciones.
    * @return int numAcciones Numero de acciones.
    */
    public int getNumeroAcciones()
    {
        return this._numAcciones;
    }

    /**
    * Devuelve la dimension de las acciones.
    * @return int dimAcciones Dimension de las acciones.
    */
    public int getDimensionAcciones()
    {
        return this._dimAcciones;
    }

    /**
    * Devuelve las acciones correspondientes a una determinada accion cuantificada.
    * @param int accionCuantificada
    * @return double[] a Acciones devueltas.
    */
    public double[] getAcciones(int accionCuantificada)
    {
        double[] a = new double[this._dimAcciones];

        System.arraycopy(this._acciones[accionCuantificada], 0, a, 0, this._dimAcciones);

        return a;
    }
}
