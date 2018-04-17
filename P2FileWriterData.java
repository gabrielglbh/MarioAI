package ch.idsia.agents.controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.*;

import ch.idsia.agents.controllers.Instancia;

public class P2FileWriterData{
  //Variables globales para el fichero de ejemplos.txt
  static FileWriter[] fich = new FileWriter[5];
  static BufferedReader br = null;
  static File file = null;

  //3000 ticks como maximo, luego si sobra espacio se reajusta
  static int[] futureCoins = new int[3000];
  static int[] futureMode = new int[3000];
  static int[] futureDistance = new int[3000];

  static int[] futureAttrsIncrement = new int[3]; //3 for reward n+6,+12,+24; 3 for distance n+6,+12,+24;

  //Queue para luego imprimir los ticks con su debido orden
  static Queue<String[]> myInstance = new LinkedList<String[]>();
  static Queue<Integer> mySitu = new LinkedList<Integer>();

  static int enemies = 0;
  static int coins = 0;
  static char b;
  static int mz = 0; // Ã�ndice para recorrer las posiciones de instancia, tanto para dar valor como para leer
  static int length_instance = 0; // Vestigial, se puede quitar (no me atrevo, no sea que la lÃ­e)
  static int count = 0;

  static int enemiesSectionA = -1;
  static int obstacleSectionA = -1;
  static int coinsSectionA = -1;
  static int enemiesSectionB = -1;
  static int coinsSectionB = -1;

  /* CONSTANTES GLOBALES */
  static final int NUM_SITUACIONES = 4;
  /* 0: peligro delante estando en el aire
   * 1: monedas delante estando en el aire
   * 2: peligro delante estando en el suelo
   * 3: monedas delante estando en el suelo
   */
  static final int NUM_INST_POR_SITU = 200;

  public P2FileWriterData(){}

  public static Instancia[][] leerBaseConoc(String ruta){
	  Instancia[][] baseConoc = new Instancia[NUM_SITUACIONES][NUM_INST_POR_SITU];
	  String csvFile = ruta;
    BufferedReader br = null;
    String lineaCsv = "";
    int situ = 0, counter = 0;

	  try{
          br = new BufferedReader(new FileReader(csvFile));
          // Recorre el fichero hasta el final
          while ((lineaCsv = br.readLine()) != null) {
        	  // Lee una linea hasta que llegue al separador o llene las instancias
        	  if(counter < NUM_INST_POR_SITU){
        		  baseConoc[situ][counter] = new Instancia(lineaCsv);
        		  counter++;
        	  } else{
        		  situ++;
        		  counter = 0;
        	  }
          }
          br.close();
	  }
	  catch(Exception e){
          e.printStackTrace(System.out);
      }
	  return baseConoc; // Esto funca :D
  }

  //Metodo auxiliar en Environment class para facilitar el entendimiento del codigo
  //Escribir en fichero los datos de los ticks
  public static void writeOnFile( byte[][] envi, float[] posMario, int[] dataMatrix, int[] marioState,
		  							int ticks_in_air, int[] sectionAttrs, boolean[] action, int tick ){


	  //if (tick < 2) System.out.println("Tick: " + tick);
    if(dataMatrix[10] == 0){
      P2FileWriterData.close_arff();
      return;
    }

    // +49: Grid; +1: reward; +4: Status; +1: ticks_in_air; +5: section_Attrs; +action.length + pertenencia;
    length_instance = 1 +marioState.length +1 +sectionAttrs.length +action.length + 1;
    String[] instancia = new String[length_instance];

    //7x7 grid
    /*for(int mx = 6; mx < 13; mx++) for(int my = 6; my < 13; my++){
        switch(envi[mx][my]){
	        case 80:{
	    		instancia[mz] = "Goomba";
	    		break;
	    	}
	        case -85:{
	    		instancia[mz] = "Tuberia";
	    		break;
	    	}
	        case -60:{
	    		instancia[mz] = "Borde";
	    		break;
	    	}
	        case -24:{
	    		instancia[mz] = "Ladrillo";
	    		break;
	    	}
	        case 2:{
	    		instancia[mz] = "Moneda";
	    		break;
	    	}
	        case -62:{
	    		instancia[mz] = "Planicie";
	    		break;
	    	}
	        default: {
	        	instancia[mz] = String.valueOf(" - ");
	    		break;
	        }
        }
        mz++;
    } //mz = 49*/

    // Reward
    instancia[mz] = String.valueOf(dataMatrix[19]);
    mz++; //mz = 50

    instancia[mz] = String.valueOf(marioState[0]);
    mz++;
    instancia[mz] = String.valueOf(marioState[1]);
    mz++;
    instancia[mz] = String.valueOf(marioState[2]);
    mz++;
    instancia[mz] = String.valueOf(marioState[3]);
    mz++; //mz = 54

    instancia[mz] = String.valueOf(ticks_in_air);
    mz++; //mz = 55

    for(int mx = 0; mx < sectionAttrs.length; mx++){
    	instancia[mz] = String.valueOf(sectionAttrs[mx]);
    	mz++;
    }

    // Action
    for(int mx = 0; mx < action.length; mx++){
    	instancia[mz] = String.valueOf(action[mx]);
    	mz++;
    } //mz = 64

    ////////////// FUNCION DE PERTENENCIA //////////////
    //DESCOMENTAR PARA CREAR BASE CONOC
    int pertenencia = 0;
     /*sectionAttrs:
     enemiesSectionA, obstacleSectionA, coinsSectionA, enemiesSectionB, coinsSectionB;
     */

    pertenencia = 100*marioState[0] + -4*(sectionAttrs[0] + sectionAttrs[1]) + 5*sectionAttrs[2]
            - 2*sectionAttrs[3] + sectionAttrs[4];

    int situ = -1;
    if(pertenencia < 48){    // Mario en el aire
      if(pertenencia < 0){
        situ = 1;
      }
      else{
         situ = 2;
      }
    }
    else {                 // Mario en el suelo
      if(pertenencia > 100){
        situ = 4;
      }
      else{
        situ = 3;
      }
    }

    instancia[mz] = String.valueOf(pertenencia);
    mySitu.add(situ);
    myInstance.add(instancia);

    /*
      Se a�ade la recompensa obtenida en los proximos ticks (desde ahora hasta dentro de 6, 12 y 24 ticks
      Asi, en el tick = 6 (future[6]) sabemos cuanto ha aumentado la recompensa en ese espacio de tiempo.
    */
    futureCoins[tick - 1] = dataMatrix[12]; // coins
    futureMode[tick - 1] = dataMatrix[9]; // status (if it was hit by an enemy)
    futureDistance[tick - 1] = dataMatrix[2]; // DistancePassedCells

    //Empieza la chicha cuando el tick 24 ocurre (empieza a escribir en este tick en el fichero)
    if(tick > 12){
      /* Aqui se calcula el valor dentro de 12 ticks de monedas recogidas, status de Mario y distancia recorrida*/
      futureAttrsIncrement[0] = futureCoins[count +12] - futureCoins[count]; // Coins n+12
      futureAttrsIncrement[1] = futureMode[count +12] - futureMode[count]; // Mode n+12
      futureAttrsIncrement[2] = futureDistance[count +12] - futureDistance[count]; // Distance n+12

      //////////// Valor de evaluaci�n de la instancia ////////////
      float instEvaluation;
      instEvaluation = (float) 5*futureAttrsIncrement[0] + 50*futureAttrsIncrement[1]
    		 + 10*futureAttrsIncrement[2] ;


      //Escribir en el fichero toda una instacia
      try {
          //Sacar el head-tick de la cola y concatenarlo con futureAttributes
          String[] instanciaActual = myInstance.poll();
          // + 1 de instEvaluation
          String[] instanciaCompleta = new String[instanciaActual.length + futureAttrsIncrement.length + 1];

          //Establcemos la instanciaCompleta...
          for(int a = 0; a < instanciaActual.length; a++){
            //Mientras que no estemos en la ultima posicion ir a�adiendo atributos
            if(a != instanciaActual.length - 7) instanciaCompleta[a] = instanciaActual[a];
            /*
              Si estamos en la ultima posicion de la instanciaActual, a�adir
              loa futureAttributes a la instanciaCompleta y por �ltimo a�adir
              la clase JUMP_action.
            */
            else{
              for(int my = 0; my < futureAttrsIncrement.length; my++){
                instanciaCompleta[a+my] = String.valueOf(futureAttrsIncrement[my]);
              }

              // Poner la action al final de la instancia
              for(int ii = 7; ii > 0; ii--){
            	  instanciaCompleta[instanciaCompleta.length - ii - 1] = instanciaActual[instanciaActual.length - ii];
              }

              instanciaCompleta[instanciaCompleta.length - 1] = String.valueOf(instEvaluation);
              break;
            }
          }

          for(int ii = 0; ii < instanciaCompleta.length; ii++){
              if(ii != instanciaCompleta.length-1) fich[0].write(instanciaCompleta[ii] + ",");
              else fich[0].write(instanciaCompleta[ii] + " \n");
          }

          int situAux = mySitu.poll();

          /*for(int ii = 0; ii < instanciaCompleta.length; ii++){
              if(ii != instanciaCompleta.length-1) fich[situAux].write(instanciaCompleta[ii] + ",");
              else fich[situAux].write(instanciaCompleta[ii] + " \n");
          }*/

          count++; // Se actualiza el indice de ticks de future y futureAttrib

      }
      catch(IOException e){
          e.printStackTrace(System.out);
      }
    }
    mz = 0;
  }

  public static void close_arff(){
    try{
        fich[0].close();
        fich[1].close();
        fich[2].close();
        fich[3].close();
        fich[4].close();
    }
    catch(Exception e){
      e.printStackTrace(System.out);
    }
  }
}
