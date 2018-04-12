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

public class P2FileWriterData{
  //Variables globales para el fichero de ejemplos.txt
  static FileWriter fich = null;
  static BufferedReader br = null;
  static File file = null;

  //3000 ticks como maximo, luego si sobra espacio se reajusta
  static int[] futureReward = new int[3000];
  static int[] futureDistance = new int[3000];

  static int[] futureAttrsIncrement = new int[6]; //3 for reward n+6,+12,+24; 3 for distance n+6,+12,+24;

  //Queue para luego imprimir los ticks con su debido orden
  static Queue<String[]> myInstance = new LinkedList<String[]>();

  static int enemies = 0;
  static int coins = 0;
  static char b;
  static int mz = 0; // Ãƒï¿½ndice para recorrer las posiciones de instancia, tanto para dar valor como para leer
  static int length_instance = 0; // Vestigial, se puede quitar (no me atrevo, no sea que la lÃƒÂ­e)
  static int count = 0;

  static int enemiesSectionA = -1;
  static int obstacleSectionA = -1;
  static int coinsSectionA = -1;
  static int enemiesSectionB = -1;
  static int coinsSectionB = -1;

  public P2FileWriterData(){}

  //No se usa en nada de momento
  // public static void prepareFile(){
	//   try {
	// 	  boolean result = Files.deleteIfExists(fd.toPath());
	// 	  //boolean result2 = fd.delete();
	//   }
	//   catch(Exception e) {
	// 	  System.out.println("Error al borrar archivo de partida previa");
	//   }
  // }

  /*public static void init_arff(){
    try{
      fich = new FileWriter("ejemplos.arff",true);
      String callerClass = new Exception().getStackTrace()[2].getClassName();
      callerClass = callerClass.substring(callerClass.lastIndexOf('.')+1);
      fich.write("@RELATION " + callerClass + "\n\n");

      for(int mx = 6; mx < 13; mx++) for(int my = 6; my < 13; my++){
        fich.write("@ATTRIBUTE pos_environment_[" + mx + "_" + my + "] NUMERIC \n");
      }

      fich.write("@ATTRIBUTE reward NUMERIC \n");

      fich.write("@ATTRIBUTE isMarioOnGround NUMERIC \n");
      fich.write("@ATTRIBUTE isMarioAbleToJump NUMERIC \n");
      fich.write("@ATTRIBUTE isMarioAbleToShoot NUMERIC \n");
      fich.write("@ATTRIBUTE isMarioCarrying NUMERIC \n");

      fich.write("@ATTRIBUTE reward_tick_n6 NUMERIC \n");
      fich.write("@ATTRIBUTE reward_tick_n12 NUMERIC \n");
      fich.write("@ATTRIBUTE reward_tick_n24 NUMERIC \n");

      fich.write("@ATTRIBUTE ticks_in_air NUMERIC %%%%%% 1 cuando se puede volver a saltar \n");

      fich.write("@ATTRIBUTE KEY_LEFT {true, false} \n");
      fich.write("@ATTRIBUTE KEY_RIGHT {true, false} \n");
      fich.write("@ATTRIBUTE KEY_DOWN {true, false} \n");
      fich.write("@ATTRIBUTE KEY_JUMP {true, false} \n");
      fich.write("@ATTRIBUTE KEY_SPEED {true, false} \n");
      fich.write("@ATTRIBUTE KEY_UP {true, false} \n");

      fich.write("\n@data \n");
    }
    catch(IOException e){
        e.printStackTrace(System.out);
    }
  }*/

  //Metodo auxiliar en Environment class para facilitar el entendimiento del codigo
  //Escribir en fichero los datos de los ticks
  public static void writeOnFile( byte[][] envi, float[] posMario, int[] dataMatrix,
		  						  int[] marioState, int ticks_in_air, boolean[] action, int tick ){

	  //if (tick < 2) System.out.println("Tick: " + tick);
    if(dataMatrix[10] == 0){
      P2FileWriterData.close_arff();
      return;
    }

    // +49: Grid; +2: distPassed, reward; +4: Status; +1: ticks_in_air; +5: section_Attrs; +action.length;
    length_instance = 49 +2 +4 +1 +5 +action.length ;
    String[] instancia = new String[length_instance];

    //7x7 grid
    for(int mx = 6; mx < 13; mx++) for(int my = 6; my < 13; my++){
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
    } //mz = 49

    //  DistancePassedCells
    instancia[mz] = String.valueOf(dataMatrix[2]);
    mz++;
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

    // SECCION A: NUMERO DE ENEMIGOS
    enemiesSectionA = 0;
    for(int ii = 6; ii < 10; ii++) for(int jj = 9; jj < 13; jj++){
      if(envi[ii][jj] == 80) enemiesSectionA++;
    }
    instancia[mz] = String.valueOf(enemiesSectionA);
    mz++; //mz = 56

    // SECCION A: ALTURA DE OBSTACULO
    obstacleSectionA = 0;
    for(int jj = 9; jj < 13; jj++){
      if(envi[9][jj] == -24 | envi[9][jj] == -60 | envi[9][jj] == -85) {
        obstacleSectionA++;
        if(envi[8][jj] == -24 | envi[8][jj] == -60 | envi[8][jj] == -85){
          obstacleSectionA++;
          if(envi[7][jj] == -24 | envi[7][jj] == -60 | envi[7][jj] == -85){
            obstacleSectionA++;
            if(envi[6][jj] == -24 | envi[6][jj] == -60 | envi[6][jj] == -85){
              obstacleSectionA++;
            }
          }
        }
      }
    }
    instancia[mz] = String.valueOf(obstacleSectionA);
    mz++; //mz = 57

    // SECCION A: NUMERO DE COINS
    coinsSectionA = 0;
    for(int ii = 6; ii < 10; ii++) for(int jj = 9; jj < 13; jj++){
      if(envi[ii][jj] == 2) coinsSectionA++;
    }
    instancia[mz] = String.valueOf(coinsSectionA);
    mz++; //mz = 58

    // SECCION B (abajo derecha): NUMERO DE ENEMIGOS
    enemiesSectionB = 0;
    for(int ii = 10; ii < 13; ii++) for(int jj = 9; jj < 13; jj++){
      if(envi[ii][jj] == 80) enemiesSectionB++;
    }
    instancia[mz] = String.valueOf(enemiesSectionB);
    mz++; //mz = 59

    // SECCION B: NUMERO DE COINS
    coinsSectionB = 0;
    for(int ii = 10; ii < 13; ii++) for(int jj = 9; jj < 13; jj++){
      if(envi[ii][jj] == 2) coinsSectionB++;
    }
    instancia[mz] = String.valueOf(coinsSectionB);
    mz++; //mz = 60

    // Action
    for(int mx = 0; mx < action.length; mx++){
    	instancia[mz] = String.valueOf(action[mx]);
    	mz++;
    } //mz = 64

    myInstance.add(instancia);

    /*
      Se añade la recompensa obtenida en los proximos ticks (desde ahora hasta dentro de 6, 12 y 24 ticks
      Asi, en el tick = 6 (future[6]) sabemos cuanto ha aumentado la recompensa en ese espacio de tiempo.
    */
    futureReward[tick -1] = dataMatrix[19]; // Reward: dataMatrix[19]; also: envi[][].len + posMario.len + 19
    futureDistance[tick -1] = dataMatrix[2]; // DistancePassedCells

    //Empieza la chicha cuando el tick 24 ocurre (empieza a escribir en este tick en el fichero)
    if(tick > 24){
      /* Aqui se calcula cuanto aumenta la recompensa (reward) en los proximos 6, 12 y 24 ticks*/
      futureAttrsIncrement[0] = futureReward[6+count] - futureReward[count]; // Reward n+6
      futureAttrsIncrement[1] = futureReward[12+count] - futureReward[count]; // Reward n+12
      futureAttrsIncrement[2] = futureReward[24+count] - futureReward[count]; // Reward n+24
      futureAttrsIncrement[3] = futureDistance[6+count] - futureDistance[count]; // Distance n+6
      futureAttrsIncrement[4] = futureDistance[12+count] - futureDistance[count]; // Distance n+12
      futureAttrsIncrement[5] = futureDistance[24+count] - futureDistance[count]; // Distance n+24
      
      
      //////////// Valor de evaluación de la instancia ////////////
      float instEvaluation;
      if(futureAttrsIncrement[1] >= 0) instEvaluation = (float) (futureAttrsIncrement[1])/16 + 4*futureAttrsIncrement[4];
      else instEvaluation = (float) (futureAttrsIncrement[1]) + 7*futureAttrsIncrement[4];

      //Escribir en el fichero toda una instacia
      try {
          //file = new File("ejemplos.arff");
          //br = new BufferedReader(new FileReader("ejemplos.arff"));

          // Establecer Header de fichero arff ï¿½nicamente en el primer tick
          // Si no existe el fichero o existe y estï¿½ vacï¿½o
          //System.out.println("Pedazo de imbecil: "+file.exists()+br.readLine());
          //if(!file.exists() || br.readLine() == null) init_arff();

          //Sacar el head-tick de la cola y concatenarlo con futureAttributes
          String[] instanciaActual = myInstance.poll();
          // + 1 de instEvaluation
          String[] instanciaCompleta = new String[instanciaActual.length + futureAttrsIncrement.length + 1];

          //Establcemos la instanciaCompleta...
          for(int a = 0; a < instanciaActual.length; a++){
            //Mientras que no estemos en la ultima posicion ir aï¿½adiendo atributos
            if(a != instanciaActual.length - 6) instanciaCompleta[a] = instanciaActual[a];
            /*
              Si estamos en la ultima posicion de la instanciaActual, aï¿½adir
              loa futureAttributes a la instanciaCompleta y por ï¿½ltimo aï¿½adir
              la clase JUMP_action.
            */
            else{
              for(int my = 0; my < futureAttrsIncrement.length; my++){
                instanciaCompleta[a+my] = String.valueOf(futureAttrsIncrement[my]);
              }
              
              // Poner la action al final de la instancia
              for(int ii = 6; ii > 0; ii--){
            	  instanciaCompleta[instanciaCompleta.length - ii - 1] = instanciaActual[instanciaActual.length - ii];
              }
              
              instanciaCompleta[instanciaCompleta.length - 1] = String.valueOf(instEvaluation);
              break;
            }
          }

          for(int ii = 0; ii < instanciaCompleta.length; ii++){
              if(ii != instanciaCompleta.length-1) fich.write(instanciaCompleta[ii] + ",");
              else fich.write(instanciaCompleta[ii] + " \n");
          }
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
      if(file.exists()){
        fich.write("\n %%%%%%%%%%%% FIN DE EJECUCION %%%%%%%%%% \n\n");
        fich.close();
      }
    }
    catch(Exception e){
      e.printStackTrace(System.out);
    }
  }
}
