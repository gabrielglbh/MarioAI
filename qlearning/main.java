/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.idsia.agents.controllers.qlearning;

import java.util.*;


/**
 *
 * @author moises
 */
public class main
{
    public static void main(String[] args)
    {
        List<Tupla> mapa  = new ArrayList<Tupla>();

        /*
            arriba 0, abajo 1, derecha 2, izquierda 3
        */

        String[] acciones = {"0", "1", "2", "3"};
        String[] estados  = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14"};

        double[] estado = {0};
        double[] estadoFinal = {14};
        double[] accion = {0};
        double refuerzo = 0;

        boolean encontrado;

        QLearning ql        = new QLearning(0, 0.6, 0.9, estados, acciones, 15, 4);
        int ciclosMaximos   = 200;
        int ciclos          = 0;
        int posicion;

	      mapa.add(new Tupla(3, 0, 2, 0));
        mapa.add(new Tupla(4, 0, 3, 0));
        mapa.add(new Tupla(6, 0, 5, 0));
        mapa.add(new Tupla(9, 0, 7, 0));
        mapa.add(new Tupla(10, 0, 9, 0));
        mapa.add(new Tupla(13, 0, 10, 0));
        mapa.add(new Tupla(12, 0, 11, 0));

        mapa.add(new Tupla(2, 1, 3, 0));
        mapa.add(new Tupla(3, 1, 4, 0));
        mapa.add(new Tupla(5, 1, 6, 0));
        mapa.add(new Tupla(7, 1, 9, 0));
        mapa.add(new Tupla(9, 1, 10, 0));
        mapa.add(new Tupla(10, 1, 13, 0));
        mapa.add(new Tupla(11, 1, 12, 0));

        mapa.add(new Tupla(0, 2, 7, 0));
        mapa.add(new Tupla(1, 2, 0, 0));
        mapa.add(new Tupla(2, 2, 1, 0));
        mapa.add(new Tupla(4, 2, 5, 0));
        mapa.add(new Tupla(6, 2, 14, 1)); /* Meta */
        mapa.add(new Tupla(13, 2, 12, 0));
        mapa.add(new Tupla(10, 2, 11, 0));
        mapa.add(new Tupla(7, 2, 8, 0));

        mapa.add(new Tupla(7, 3, 0, 0));
        mapa.add(new Tupla(0, 3, 1, 0));
        mapa.add(new Tupla(1, 3, 2, 0));
        mapa.add(new Tupla(5, 3, 4, 0));
        mapa.add(new Tupla(13, 3, 14, 1)); /* Meta */
        mapa.add(new Tupla(12, 3, 13, 0));
        mapa.add(new Tupla(11, 3, 10, 0));
        mapa.add(new Tupla(8, 3, 7, 0));

        while (ciclos < ciclosMaximos)
        {
            for (int i = 0; i < mapa.size(); i++)
                ql.actualizarTablaQ(mapa.get(i));

            ciclos++;
        }

        ql.mostrarTablaQ();
        boolean salir = false;
        while (estado[0] != estadoFinal[0] && !salir)
        {
            accion      = ql.obtenerMejorAccion(estado);
            refuerzo    = ql.obtenerFuncionQMax(estado);
            posicion    = 0;
            encontrado  = false;

            while (!encontrado && !salir)
            {
                if ((mapa.get(posicion).getEstado(0) == estado[0]) && (mapa.get(posicion).getAccion(0) == accion[0]))
                {
                    System.out.print("Transito de " + estado[0]);

                    estado[0] = mapa.get(posicion).getEstadoSiguiente(0);

                    System.out.println(" a " + estado[0] + " con " + accion[0] + "(" + (double)Math.round(refuerzo * 100)/100 + ")");

                    encontrado = true;
                }

                posicion++;

		if(posicion >= mapa.size()) salir = true;
            }
        }
    }
}
