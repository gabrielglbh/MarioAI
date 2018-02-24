package ch.idsia.agents.controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

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

  static int enemies = 0;
  static int coins = 0;
  static char b;
  static int mz = 0;
  static int length_instance = 0;
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

          for(mz = 0; mz < length_instance; mz++){
            if(mz != length_instance-1) fich.write(instancia[mz] + ", ");
            else fich.write(instancia[mz] + " \n");
          }

          fich.close();
          mz = 0;
      }
      catch(IOException e){
          e.printStackTrace(System.out);
      }
    }
  }
}
