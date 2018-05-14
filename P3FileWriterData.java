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

public class P3FileWriterData{
  //Variables globales para el fichero de ejemplos.txt
  static FileWriter fich = null;
  static BufferedReader br = null;
  static File file = null;

  //3000 ticks como maximo, luego si sobra espacio se reajusta
  static int[] futureCoins = new int[3000];
  static int[] futureMode = new int[3000];
  static int[] futureDistance = new int[3000];

  static final int enemiesSectA = 0;
  static final int obstacSectA  = 1;
  static final int coinsSectA   = 2;
  static final int enemiesSectB = 3;
  static final int coinsSectB   = 4;
  static final int enemiesSectC = 5;
  static final int obstacSectC  = 6;
  static final int coinsSectC   = 7;
  static final int enemiesSectD = 8;
  static final int coinsSectD   = 9;

  static int[] futureAttrsIncrement = new int[3]; //3 for reward n+6,+12,+24; 3 for distance n+6,+12,+24;

  //Queue para luego imprimir los ticks con su debido orden
  static Queue<String[]> myInstance = new LinkedList<String[]>();
  static Queue<String[]> myTuplas = new LinkedList<String[]>();

  static int enemies = 0;
  static int coins = 0;
  static char b;
  static int mz = 0; // Indice para recorrer las posiciones de instancia, tanto para dar valor como para leer
  static int length_instance = 0; // Vestigial, se puede quitar (no me atrevo, no sea que la lie)
  static int count = 0;

  /*static int enemiesSectionA = -1;
  static int obstacleSectionA = -1;
  static int coinsSectionA = -1;
  static int enemiesSectionB = -1;
  static int coinsSectionB = -1;*/

  /* CONSTANTES GLOBALES */
  // Situaciones en las que clasificar cada estado, cada una tiene una fila en la TablaQ
  static final int NUM_SITUACIONES = -1;
  /* 0:
   * 1:
   * 2:
   * 3:
   */
  // Esto ahora mismo no sirve, habr�a que cambiarlo por el n�mero de acciones que consideramos
  static final int NUM_INST_POR_SITU = 200;

  public P3FileWriterData(){}

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
        		  baseConoc[situ][counter] = new Instancia(lineaCsv);
        		  counter++;
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
  public static void writeOnFile(int[] dataMatrix, int[] marioState, int ticks_in_air, 
		  int[] sectionAttrs, int actionCode, int tick ){


    if(dataMatrix[10] == 0){
      P3FileWriterData.close_arff();
      return;
    }

    // +1: reward; +1: distancePassedCells; +1: ticks_in_air;  +1: pertenencia; +1: actionCode;
    length_instance = 1 + marioState.length + 1 +  sectionAttrs.length + 1;
    String[] instancia = new String[length_instance];

    // Reward
    instancia[mz] = String.valueOf(dataMatrix[19]);
    mz++; //mz = 50

    // Distance Passed Cells
    //instancia[mz] = String.valueOf(dataMatrix[2]);
    //mz++; //mz = 50

    // ** isMarioOnGround (1 o 0), isMarioAbleToJump() (1 o 0),
    //    isMarioAbleToShoot (1 o 0),isMarioCarrying (1 o 0),
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
	instancia[mz] = String.valueOf(actionCode);
	mz++;
     //mz = 64

    ////////////// FUNCION DE PERTENENCIA //////////////
    //int pertenencia = 0;
     /*sectionAttrs:
     enemiesSectionA, obstacleSectionA, coinsSectionA, enemiesSectionB, coinsSectionB,
     enemiesSectionC, obstacleSectionC, coinsSectionC, enemiesSectionD, coinsSectionD;
     */

	int situ = 0;

    //mario suelo y enemigo delante
    if(sectionAttrs[enemiesSectA] > 0 && marioState[0] == 1){
      situ = 0;
    }

    //mario saltando y enemigo debajo
    else if(sectionAttrs[enemiesSectB] > 0 && marioState[0] == 0){
      situ = 1;
    }

    //mario suelo y monedas delante
    else if(sectionAttrs[coinsSectA] > 0 && marioState[0] == 1){
      situ = 2;
    }

    //mario saltando y monedas debajo o delante
    else if((sectionAttrs[coinsSectB] > 0 || sectionAttrs[coinsSectA] > 0) && marioState[0] == 0){
      situ = 3;
    }

    //mario suelo y obstauclo delante
    else if(sectionAttrs[obstacSectA] > 0 && marioState[0] == 1){
      situ = 4;
    }

    //mario saltando y obstaculo delante
    else if(sectionAttrs[obstacSectA] > 0 && marioState[0] == 0){
      situ = 5;
    }

    //mario saltando y enemigo por atrás y debajo
    else if((sectionAttrs[enemiesSectC] > 0 || sectionAttrs[enemiesSectD] > 0 ) && marioState[0] == 0){
      situ = 6;
    }
    else{
      situ = 7;
    }

    //mySitu.add(situ);
    myInstance.add(instancia);

    /*
      Se agrega la recompensa obtenida en los proximos ticks (desde ahora hasta dentro de 6, 12 y 24 ticks
      Asi, en el tick = 6 (future[6]) sabemos cuanto ha aumentado la recompensa en ese espacio de tiempo.
    */
    futureCoins[tick - 1] = dataMatrix[12]; // coins
    futureMode[tick - 1] = dataMatrix[9]; // status (if it was hit by an enemy)
    futureDistance[tick - 1] = dataMatrix[2]; // DistancePassedCells

    // Empieza la chicha cuando el tick 12 ocurre (empieza a escribir en este tick en el fichero)
    if(tick > 12){
      /* Aqui se calcula el valor dentro de 12 ticks de monedas recogidas, status de Mario y distancia recorrida*/
      futureAttrsIncrement[0] = futureCoins[count +12] - futureCoins[count]; // Coins n+12
      futureAttrsIncrement[1] = futureMode[count +12] - futureMode[count]; // Mode n+12
      futureAttrsIncrement[2] = futureDistance[count +12] - futureDistance[count]; // Distance n+12

      //////////// Valor de evaluacion de la instancia ////////////
      float instEvaluation;
      instEvaluation = (float) 5*futureAttrsIncrement[0] + 50*futureAttrsIncrement[1]
    		 + 10*futureAttrsIncrement[2] ;

      if (instEvaluation < 0.0) instEvaluation = 0.0f;

      //Escribir en el fichero toda una instacia
      try {
          String[] instanciaInicial = myInstance.poll();
          // + 1 de instEvaluation
          String[] instanciaCompleta =
        		  new String[instanciaInicial.length + (instancia.length - 1) + 1];

          //Establcemos la instanciaCompleta...
          for(int a = 0; a < instanciaCompleta.length; a++){
            if (a < instanciaInicial.length) instanciaCompleta[a] = instanciaInicial[a];
            else {
            	instanciaCompleta[a] = instancia[a - instanciaInicial.length];
            	if (a == instanciaCompleta.length - 1)
            		instanciaCompleta[instanciaCompleta.length - 1] = String.valueOf(instEvaluation);
            }
          }

          for(int ii = 0; ii < instanciaCompleta.length; ii++){
              if(ii != instanciaCompleta.length-1) fich.write(instanciaCompleta[ii] + ",");
              else fich.write(instanciaCompleta[ii] + " \n");
          }

          /*int situAux = mySitu.poll();
          for(int ii = 0; ii < instanciaCompleta.length; ii++){
              if(ii != instanciaCompleta.length-1) fich[situAux].write(instanciaCompleta[ii] + ",");
              else fich[situAux].write(instanciaCompleta[ii] + " \n");
          }*/

          count++; // Se actualiza el indice de ticks de future y futureAttrib (

      }
      catch(IOException e){
          e.printStackTrace(System.out);
      }
    }
    mz = 0;
  }

  public static void close_arff(){
    try{
        fich.close();
    }
    catch(Exception e){
      e.printStackTrace(System.out);
    }
  }
  
  public static void createTupla( int[] dataMatrix, int[] marioState, int[] sectionAttrs, int actionCode, int tick ){


	  if(dataMatrix[10] == 0){
		  P3FileWriterData.close_arff();
		  return;
	  }

	  // estado_actual, accion
	  length_instance = 2;
	  String[] instancia = new String[length_instance];

	  //////////////FUNCION DE PERTENENCIA //////////////
	  //int pertenencia = 0;
	  /*sectionAttrs:
		enemiesSectionA, obstacleSectionA, coinsSectionA, enemiesSectionB, coinsSectionB,
		enemiesSectionC, obstacleSectionC, coinsSectionC, enemiesSectionD, coinsSectionD;
	   */

	  int situ = 0;

	  //mario suelo y enemigo delante
	  if(sectionAttrs[enemiesSectA] > 0 && marioState[0] == 1){
		  situ = 0;
	  }

	  //mario saltando y enemigo debajo
	  else if(sectionAttrs[enemiesSectB] > 0 && marioState[0] == 0){
		  situ = 1;
	  }

	  //mario suelo y monedas delante
	  else if(sectionAttrs[coinsSectA] > 0 && marioState[0] == 1){
		  situ = 2;
	  }

	  //mario saltando y monedas debajo o delante
	  else if((sectionAttrs[coinsSectB] > 0 || sectionAttrs[coinsSectA] > 0) && marioState[0] == 0){
		  situ = 3;
	  }

	  //mario suelo y obstauclo delante
	  else if(sectionAttrs[obstacSectA] > 0 && marioState[0] == 1){
		  situ = 4;
	  }

	  //mario saltando y obstaculo delante
	  else if(sectionAttrs[obstacSectA] > 0 && marioState[0] == 0){
		  situ = 5;
	  }

	  //mario saltando y enemigo por atrás y debajo
	  else if((sectionAttrs[enemiesSectC] > 0 || sectionAttrs[enemiesSectD] > 0 ) && marioState[0] == 0){
		  situ = 6;
	  }
	  else{
		  situ = 7;
	  }
	  
	  instancia[mz] = String.valueOf(situ);
	  mz++;

	  // Action
	  instancia[mz] = String.valueOf(actionCode);
	  mz++;
	  
	  //mySitu.add(situ);
	  myTuplas.add(instancia);

	  /*
		Se agrega la recompensa obtenida en los proximos ticks (desde ahora hasta dentro de 6, 12 y 24 ticks
		Asi, en el tick = 6 (future[6]) sabemos cuanto ha aumentado la recompensa en ese espacio de tiempo.
	   */
	  futureCoins[tick - 1] = dataMatrix[12]; // coins
	  futureMode[tick - 1] = dataMatrix[9]; // status (if it was hit by an enemy)
	  futureDistance[tick - 1] = dataMatrix[2]; // DistancePassedCells

	  // Empieza la chicha cuando el tick 12 ocurre (empieza a escribir en este tick en el fichero)
	  if(tick > 12){
		  /* Aqui se calcula el valor dentro de 12 ticks de monedas recogidas, status de Mario y distancia recorrida*/
		  futureAttrsIncrement[0] = futureCoins[count +12] - futureCoins[count]; // Coins n+12
		  futureAttrsIncrement[1] = futureMode[count +12] - futureMode[count]; // Mode n+12
		  futureAttrsIncrement[2] = futureDistance[count +12] - futureDistance[count]; // Distance n+12

		  //////////// Valor de evaluacion de la instancia ////////////
		  float instEvaluation;
		  instEvaluation = (float) 5*futureAttrsIncrement[0] + 50*futureAttrsIncrement[1]
				  + 10*futureAttrsIncrement[2] ;

		  if (instEvaluation < 0.0) instEvaluation = 0.0f;

		  //Escribir en el fichero toda una instacia
		  try {
			  String[] instanciaInicial = myTuplas.poll();
			  // estado_actual, accion, estado_siguiente, refuerzo
			  String[] instanciaCompleta = new String[instanciaInicial.length + 2];

			  //Establcemos la instanciaCompleta...
			  for(int a = 0; a < instanciaCompleta.length; a++){
				  if (a < instanciaInicial.length) instanciaCompleta[a] = instanciaInicial[a];
				  else {
					  instanciaCompleta[a] = instancia[a - instanciaInicial.length];
					  if (a == instanciaCompleta.length - 1)
						  instanciaCompleta[instanciaCompleta.length - 1] = String.valueOf(instEvaluation);
				  }
			  }

			  for(int ii = 0; ii < instanciaCompleta.length; ii++){
				  if(ii != instanciaCompleta.length-1) fich.write(instanciaCompleta[ii] + ",");
				  else fich.write(instanciaCompleta[ii] + " \n");
			  }

			  /*int situAux = mySitu.poll();
				for(int ii = 0; ii < instanciaCompleta.length; ii++){
				if(ii != instanciaCompleta.length-1) fich[situAux].write(instanciaCompleta[ii] + ",");
				else fich[situAux].write(instanciaCompleta[ii] + " \n");
				}*/

			  count++; // Se actualiza el indice de ticks de future y futureAttrib (

		  }
		  catch(IOException e){
			  e.printStackTrace(System.out);
		  }
	  }
	  mz = 0;
  }  
  
  
}
