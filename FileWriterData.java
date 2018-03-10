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

public class FileWriterData{
  //Variables globales para el fichero de ejemplos.txt
  static FileWriter fich = null;
  static BufferedReader br = null;
  static File file = null;

  //3000 ticks como maximo, luego si sobra espacio se reajusta
  static String[][] future = new String[3000][2];
  static String[][]  futureAttributes = new String[3000][6];

  //Queue para luego imprimir los ticks con su debido orden
  static Queue<String[]> myInstance = new LinkedList<String[]>();

  static int enemies = 0;
  static int coins = 0;
  static char b;
  static int mz = 0; // Índice para recorrer las posiciones de instancia, tanto para dar valor como para leer
  static int length_instance = 0; // Vestigial, se puede quitar (no me atrevo, no sea que la líe)
  static int count = 0;

  public FileWriterData(){}

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

  public static void init_arff(byte[][] envi){
    try{
      fich = new FileWriter("ejemplos.arff",true);
      String callerClass = new Exception().getStackTrace()[2].getClassName();
      callerClass = callerClass.substring(callerClass.lastIndexOf('.')+1);
      fich.write("@RELATION " + callerClass + "\n\n");

      for(int mx = 0; mx < envi.length; mx++) for(int my = 0; my < envi[mx].length; my++){
        fich.write("@ATTRIBUTE pos_environment_[" + mx + "_" + my + "] NUMERIC \n");
      }

      fich.write("@ATTRIBUTE posMario_1 NUMERIC \n");
      fich.write("@ATTRIBUTE posMario_2 NUMERIC \n");

      fich.write("@ATTRIBUTE flowersDevoured NUMERIC \n");
      fich.write("@ATTRIBUTE killsByFire NUMERIC \n");
      fich.write("@ATTRIBUTE killsByShell NUMERIC \n");
      fich.write("@ATTRIBUTE killsByStomp NUMERIC \n");
      fich.write("@ATTRIBUTE killsTotal NUMERIC \n");
      fich.write("@ATTRIBUTE marioMode NUMERIC \n");
      fich.write("@ATTRIBUTE marioStatus NUMERIC \n");
      fich.write("@ATTRIBUTE mushroomsDevoured NUMERIC \n");
      fich.write("@ATTRIBUTE coinsGained NUMERIC \n");
      fich.write("@ATTRIBUTE timeLeft NUMERIC \n");
      fich.write("@ATTRIBUTE timeSpent NUMERIC \n");
      fich.write("@ATTRIBUTE hiddenBlocksFound NUMERIC \n");
      fich.write("@ATTRIBUTE coinsInScreen NUMERIC \n");
      fich.write("@ATTRIBUTE blocksInScreen NUMERIC \n");
      fich.write("@ATTRIBUTE enemiesInScreen NUMERIC \n");
      fich.write("@ATTRIBUTE reward NUMERIC \n");
      fich.write("@ATTRIBUTE posMarioEgo_1 NUMERIC \n");
      fich.write("@ATTRIBUTE posMarioEgo_2 NUMERIC \n");
      fich.write("@ATTRIBUTE distancePassedCells NUMERIC \n");

      fich.write("@ATTRIBUTE distancePassedPhys NUMERIC \n");

      fich.write("@ATTRIBUTE KEY_SPEED {true, false} \n");
      fich.write("@ATTRIBUTE KEY_UP {true, false} \n");
      fich.write("@ATTRIBUTE KEY_LEFT {true, false} \n");
      fich.write("@ATTRIBUTE KEY_RIGHT {true, false} \n");
      fich.write("@ATTRIBUTE KEY_DOWN {true, false} \n");

      fich.write("@ATTRIBUTE coinsGained_tick_n6 NUMERIC \n");
      fich.write("@ATTRIBUTE killsTotal_tick_n6 NUMERIC \n");
      fich.write("@ATTRIBUTE coinsGained_tick_n12 NUMERIC \n");
      fich.write("@ATTRIBUTE killsTotal_tick_n12 NUMERIC \n");
      fich.write("@ATTRIBUTE coinsGained_tick_n24 NUMERIC \n");
      fich.write("@ATTRIBUTE killsTotal_tick_n24 NUMERIC \n");

      fich.write("@ATTRIBUTE KEY_JUMP {true, false} \n");

      fich.write("\n@data \n");
    }
    catch(IOException e){
        e.printStackTrace(System.out);
    }
  }

  //Metodo auxiliar en Environment class para facilitar el entendimiento del codigo
  //Escribir en fichero los datos de los ticks
  public static void writeOnFile(float[] posMario, int[] dataMatrix, byte[][] envi, boolean[] action, int tick){

    length_instance = envi.length*envi[0].length + posMario.length + dataMatrix.length + action.length;
    String[] instancia = new String[length_instance];

    //Meter todo en matriz instancia
    for(int mx = 0; mx < envi.length; mx++) for(int my = 0; my < envi[mx].length; my++){
      instancia[mz] = String.valueOf(envi[mx][my]);
      mz++;
    }

    for(int mx = 0; mx < posMario.length; mx++){
      instancia[mz] = String.valueOf(posMario[mx]);
      mz++;
    }

    for(int mx = 0; mx < dataMatrix.length; mx++){
      instancia[mz] = String.valueOf(dataMatrix[mx]);
      mz++;
    }

    for(int mx = 0; mx < action.length; mx++){
      instancia[mz] = String.valueOf(action[mx]);
      mz++;
    }

    myInstance.add(instancia);

    /*
      Monedas instancia[377]
      Enemigos instancia[373]
    */

    /*
      Se aÃ±ade monedas y enemigos de cada tick desde el principio, por cada tick hay una LinkedList
      de dos posiciones (enemigos y monedas) en el tick.
      AsÃ­, en el tick = 6 (future[6]) sabemos cuantos enemigos y monedas matÃ³ y recogiÃ³ respectivamente.
    */
    future[tick][0] = instancia[372];
    future[tick][1] = instancia[368];

    //Empieza la chicha cuando el tick 24 ocurre (empieza a escribir en este tick en el fichero)
    if(tick > 24){
      int mx = 0;
      /*count = tick del pasado
        cuando el tick sea igual a 24 o mayor, futureAttributes se rellenarÃ¡
        con los valores de los n+6, n+12 y n+24 Atributos de future[].

        count se va a actualizando a medida que se accede a este if
        para emular a los primeros ticks y aÃ±adirles dichos atributos.
      */

      /* Aquí se calcula el numero de monedas/bichos que se consiguen en los próximos 6, 12 y 24 ticks*/
      futureAttributes[count][mx] = String.valueOf( Integer.parseInt(future[6+count+1][0]) - Integer.parseInt(future[count+1][0]) ); //Monedas
      futureAttributes[count][mx+1] = String.valueOf( Integer.parseInt(future[6+count+1][1]) - Integer.parseInt(future[count+1][1]) ); //Enemigos

      futureAttributes[count][mx+2] = String.valueOf( Integer.parseInt(future[12+count+1][0]) - Integer.parseInt(future[count+1][0]) ); //Monedas
      futureAttributes[count][mx+3] = String.valueOf( Integer.parseInt(future[12+count+1][1]) - Integer.parseInt(future[count+1][1]) ); //Enemigos

      futureAttributes[count][mx+4] = String.valueOf( Integer.parseInt(future[24+count+1][0]) - Integer.parseInt(future[count+1][0]) ); //Monedas
      futureAttributes[count][mx+5] = String.valueOf( Integer.parseInt(future[24+count+1][1]) - Integer.parseInt(future[count+1][1]) ); //Enemigos

      //Escribir en el fichero toda una instacia
      try {
          fich = new FileWriter("ejemplos.arff",true);
          file = new File("ejemplos.arff");
          br = new BufferedReader(new FileReader("ejemplos.arff"));

          //Establecer Header de fichero arff Ãºnicamente en el primer tick
          //Si no existe el fichero o existe y estÃƒÂ¡ vacÃƒÂ­o...
          if(br.readLine() == null || !file.exists()) init_arff(envi);

          //Sacar el head-tick de la cola y concatenarlo con futureAttributes
          String[] instanciaActual = myInstance.poll();
          String[] instanciaCompleta = new String[395];

          //Establcemos la instanciaCompleta...
          for(int a = 0; a < instanciaActual.length; a++){
            //Mientras que no estemos en la ultima posicion ir aÃ±adiendo atributos
            if(a != instanciaActual.length-1) instanciaCompleta[a] = instanciaActual[a];
            /*
              Si estamos en la Ãºltima posicion de la instanciaActual, aÃ±adir
              loa futureAttributes a la instanciaCompleta y por Ãºltimo aÃ±adir
              la clase distancePassedPhys.
            */
            else{
              for(int my = 0; my < 6; my++){
                instanciaCompleta[a+my] = futureAttributes[count][my];
              }
              //AÃ±adir la clase distancePassedPhys a la instanciaCompleta
              instanciaCompleta[instanciaCompleta.length-1] = instanciaActual[instanciaActual.length-1];
              //auxCounter++; Aquí es donde se debería actualizar //= 0; //Reset
              break;
            }
          }

          /*
            instanciaActual representa la primera instancia a escribir...
            En el tick = 24, instanciaActual es la instancia del tick = 1
            En el tick = 25, instanciaActual es la instancia del tick = 2
                                    ...
            futureAttributes representa los atributos correspondientes a
            instanciaActual en los ticks n+6, n+12, n+24.

            instanciaCompleta representa toda la instancia con los futureAttributes
            ya metidos en ella.
          */

          for(int ii = 0; ii < instanciaCompleta.length; ii++){
            if(ii != instanciaCompleta.length-1) fich.write(instanciaCompleta[ii] + ", ");
            else fich.write(instanciaCompleta[ii] + " \n");
          }

          fich.close();
          count++; // Se actualiza el índice de ticks de future y futureAttrib

      }
      catch(IOException e){
          e.printStackTrace(System.out);
      }
    }
    mz = 0;
  }
}
