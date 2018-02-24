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
  static LinkedList<String>[] future = new LinkedList[3000];
  static String[][]  futureAttributes = new String[3000][6];

  //Queue para luego imprimir los ticks con su debido orden
  static Queue<String[]> myInstance = new LinkedList<String[]>();

  static int enemies = 0;
  static int coins = 0;
  static char b;
  static int mz = 0;
  static int length_instance = 0;
  static int count = 0;
  static int auxCounter = 0;

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
      fich.write("@RELATION Mario_Datos_Ticks\n\n");
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

      fich.write("@ATTRIBUTE coinsGained_tick_n6 NUMERIC \n");
      fich.write("@ATTRIBUTE killsTotal_tick_n6 NUMERIC \n");
      fich.write("@ATTRIBUTE coinsGained_tick_n12 NUMERIC \n");
      fich.write("@ATTRIBUTE killsTotal_tick_n12 NUMERIC \n");
      fich.write("@ATTRIBUTE coinsGained_tick_n24 NUMERIC \n");
      fich.write("@ATTRIBUTE killsTotal_tick_n24 NUMERIC \n");

      fich.write("@ATTRIBUTE distancePassedPhys NUMERIC \n");

      fich.write("\n@data \n");
    }
    catch(IOException e){
        e.printStackTrace(System.out);
    }
  }

  //Metodo auxiliar en Environment class para facilitar el entendimiento del codigo
  //Escribir en fichero los datos de los ticks
  public static void writeOnFile(float[] posMario, int[] dataMatrix, byte[][] envi, int tick){

    length_instance = envi.length*envi[0].length+posMario.length+dataMatrix.length;
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

    myInstance.add(instancia);

    /*
      Monedas instancia[371]
      Enemigos instancia[367]
    */

    // Make sure the list is initialized before adding to it
    if(future[tick] == null) {
       future[tick] = new LinkedList<String>();
    }

    /*
      TODO: para calcular las monedas recogidas en los próximos 12 ticks habrá que
      restar el total de monedas hasta el tick n + 12 menos el total de monedas
      hasta el tick actual n. Podría ser útil generalizar esta operación para un
      número arbitrario de ticks futuros.
    */

    /*
      Se añade monedas y enemigos de cada tick desde el principio, por cada tick hay una LinkedList
      de dos posiciones (enemigos y monedas) en el tick.
      Así, en el tick = 6 (future[6]) sabemos cuantos enemigos y monedas mató y recogió respectivamente.
    */
    future[tick].add(instancia[371]);
    future[tick].add(instancia[367]);

    //Empieza la chicha cuando el tick 24 ocurre (empieza a escribir en este tick en el fichero)
    if(tick >= 24){
      int mx = 0;
      /*count = tick del pasado
        cuando el tick sea igual a 24 o mayor, futureAttributes se rellenará
        con los valores de los n+6, n+12 y n+24 Atributos de future[].

        count se va a actualizando a medida que se accede a este if
        para emular a los primeros ticks y añadirles dichos atributos.
      */
      futureAttributes[count][mx] = future[6+count].getFirst(); //Monedas
      futureAttributes[count][mx+1] = future[6+count].getLast(); //Enemigos

      futureAttributes[count][mx+2] = future[12+count].getFirst(); //Monedas
      futureAttributes[count][mx+3] = future[12+count].getLast(); //Enemigos

      futureAttributes[count][mx+4] = future[24+count].getFirst(); //Monedas
      futureAttributes[count][mx+5] = future[24+count].getLast(); //Enemigos

      count++;

      //Escribir en el fichero toda una instacia
      try{
          fich = new FileWriter("ejemplos.arff",true);
          file = new File("ejemplos.arff");
          br = new BufferedReader(new FileReader("ejemplos.arff"));

          //Establecer Header de fichero arff únicamente en el primer tick
          //Si no existe el fichero o existe y estÃ¡ vacÃ­o...
          if(br.readLine() == null || !file.exists()) init_arff(envi);

          //Sacar el head-tick de la cola y concatenarlo con futureAttributes
          String[] instanciaActual = myInstance.poll();
          String[] instanciaCompleta = new String[389];

          //Establcemos la instanciaCompleta...
          for(int a = 0; a < instanciaActual.length; a++){
            //Mientras que no estemos en la ultima posicion ir añadiendo atributos
            if(a != instanciaActual.length-1) instanciaCompleta[a] = instanciaActual[a];
            /*
              Si estamos en la última posicion de la instanciaActual, añadir
              loa futureAttributes a la instanciaCompleta y por último añadir
              la clase distancePassedPhys.
            */
            else{
              for(int my = 0; my < 6; my++){

                /**************************/
                /* SIEMPRE IMPRIME AL PRINCIPIO
                  0, null, null, null, null, null
                  0, 0, null, null, null, null
                  0, 0, 0, null, null, null
                  0, 0, 0, 0, null, null
                  0, 0, 0, 0, 0, null
                  0, 0, 0, 0, 0, 1

                    Y SE QUEDA ASÍ 
                */
                /**************************/

                instanciaCompleta[a+my] = futureAttributes[auxCounter][my];
                auxCounter++;
              }
              //Añadir la clase distancePassedPhys a la instanciaCompleta
              instanciaCompleta[instanciaCompleta.length-1] = instanciaActual[instanciaActual.length-1];
              auxCounter = 0; //Reset
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

          for(mz = 0; mz < instanciaCompleta.length; mz++){
            if(mz != instanciaCompleta.length-1) fich.write(instanciaCompleta[mz] + ", ");
            else fich.write(instanciaCompleta[mz] + " \n");
          }

          fich.close();

      }
      catch(IOException e){
          e.printStackTrace(System.out);
      }
    }

    mz = 0;
  }
}
