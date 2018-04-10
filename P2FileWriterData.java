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
  static int[] futureRewIncrement = new int[3]; //[3] for n +6, +12 and +24

  //Queue para luego imprimir los ticks con su debido orden
  static Queue<String[]> myInstance = new LinkedList<String[]>();

  static int enemies = 0;
  static int coins = 0;
  static char b;
  static int mz = 0; // Ã�ndice para recorrer las posiciones de instancia, tanto para dar valor como para leer
  static int length_instance = 0; // Vestigial, se puede quitar (no me atrevo, no sea que la lÃ­e)
  static int count = 0;

  static boolean isEnemy = false;
  static boolean isBlock = false;

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

      fich.write("@ATTRIBUTE coinsGained NUMERIC \n");
      fich.write("@ATTRIBUTE reward NUMERIC \n");

      fich.write("@ATTRIBUTE isMarioOnGround NUMERIC \n");
      fich.write("@ATTRIBUTE isMarioAbleToJump NUMERIC \n");
      fich.write("@ATTRIBUTE isMarioAbleToShoot NUMERIC \n");
      fich.write("@ATTRIBUTE isMarioCarrying NUMERIC \n");

      fich.write("@ATTRIBUTE reward_tick_n6 NUMERIC \n");
      fich.write("@ATTRIBUTE reward_tick_n12 NUMERIC \n");
      fich.write("@ATTRIBUTE reward_tick_n24 NUMERIC \n");

      fich.write("@ATTRIBUTE ticks_since_jump NUMERIC %%%%%% 1 cuando se puede volver a saltar \n");

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
		  						  int[] marioState, int ticks_since_jump, boolean[] action, int tick ){

	  //if (tick < 2) System.out.println("Tick: " + tick);
    if(dataMatrix[10] == 0){
      P2FileWriterData.close_arff();
      return;
    }

  	// +1: ticks_since_jump
    // +25: Grid; +2: dataMatrix; +4: Status
    length_instance = 49 + 2 + 4 + action.length +1;
    String[] instancia = new String[length_instance];

    //5x5 grid
    for(int mx = 6; mx < 13; mx++) for(int my = 6; my < 13; my++){
        instancia[mz] = String.valueOf(envi[mx][my]);
        mz++;
    }

    instancia[mz] = String.valueOf(dataMatrix[12]);
    mz++;
    instancia[mz] = String.valueOf(dataMatrix[19]);
    mz++;

    instancia[mz] = String.valueOf(marioState[0]);
    mz++;
    instancia[mz] = String.valueOf(marioState[1]);
    mz++;
    instancia[mz] = String.valueOf(marioState[2]);
    mz++;
    instancia[mz] = String.valueOf(marioState[3]);
    mz++;

    instancia[mz] = String.valueOf(ticks_since_jump);
    mz++;

    for(int mx = 0; mx < action.length; mx++){
    	instancia[mz] = String.valueOf(action[mx]);
    	mz++;
    }

    myInstance.add(instancia);

    /*
      Se a�ade la recompensa obtenida en los proximos ticks (desde ahora hasta dentro de 6, 12 y 24 ticks
      Asi, en el tick = 6 (future[6]) sabemos cuanto ha aumentado la recompensa en ese espacio de tiempo.
    */
    futureReward[tick -1] = dataMatrix[19]; // Reward: dataMatrix[19]; also: envi[][].len + posMario.len + 19
    //future[tick][1] = instancia[371]; // MonstersInScreen: envi[][].len + posMario.len + 8

    //Empieza la chicha cuando el tick 24 ocurre (empieza a escribir en este tick en el fichero)
    if(tick > 24){
      /*count = tick del pasado
        cuando el tick sea igual a 24 o mayor, futureAttributes se rellenara
        con los valores de los n+6, n+12 y n+24 Atributos de future[].

        count se va a actualizando a medida que se accede a este if
        para emular a los primeros ticks y anadirles dichos atributos.
      */

      /* Aqui se calcula cuanto aumenta la recompensa (reward) en los pr�ximos 6, 12 y 24 ticks*/
      futureRewIncrement[0] = futureReward[6+count] - futureReward[count]; // Reward
      futureRewIncrement[1] = futureReward[12+count] - futureReward[count]; // Reward
      futureRewIncrement[2] = futureReward[24+count] - futureReward[count]; // Reward

      //Escribir en el fichero toda una instacia
      try {
          //file = new File("ejemplos.arff");
          //br = new BufferedReader(new FileReader("ejemplos.arff"));

          // Establecer Header de fichero arff �nicamente en el primer tick
          // Si no existe el fichero o existe y est� vac�o
          //System.out.println("Pedazo de imbecil: "+file.exists()+br.readLine());
          //if(!file.exists() || br.readLine() == null) init_arff();

          //Sacar el head-tick de la cola y concatenarlo con futureAttributes
          String[] instanciaActual = myInstance.poll();
          String[] instanciaCompleta = new String[instanciaActual.length + futureRewIncrement.length];

          //Establcemos la instanciaCompleta...
          for(int a = 0; a < instanciaActual.length; a++){
            //Mientras que no estemos en la ultima posicion ir a�adiendo atributos
            if(a != instanciaActual.length-7) instanciaCompleta[a] = instanciaActual[a];
            /*
              Si estamos en la �ltima posicion de la instanciaActual, a�adir
              loa futureAttributes a la instanciaCompleta y por �ltimo a�adir
              la clase JUMP_action.
            */
            else{
              for(int my = 0; my < futureRewIncrement.length; my++){
                instanciaCompleta[a+my] = String.valueOf(futureRewIncrement[my]);
              }
              // A�adir el �ltimo atributo de instanciaActual al final de instanciaCompleta, para que sea la clase
              for(int ii = 7; ii > 0; ii--){
            	  instanciaCompleta[instanciaCompleta.length -ii] = instanciaActual[instanciaActual.length-ii];
              }
              break;
            }
          }

          /*
            instanciaActual representa la primera instancia a escribir...
            En el tick = 24, instanciaActual es la instancia del tick = 1
            En el tick = 25, instanciaActual es la instancia del tick = 2
                                    ...
            futureRewIncrement cuanto aumenta la recompensa en los ticks n+6, n+12, n+24.

            instanciaCompleta contiene los atributos del tick actual,
            el incremento futuro de la recompensa y la acci�n al final (la clase).
          */

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
