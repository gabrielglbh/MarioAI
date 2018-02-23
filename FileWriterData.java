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
  //Queue para escribir los enemigos abatidos y monedas recogidas hace 5 ticks
  static Queue<Integer> qEnemies = new LinkedList<Integer>();
  static Queue<Integer> qCoins = new LinkedList<Integer>();
  static Queue<Integer> qInstancia= new LinkedList<Integer>();

  static int enemies = 0;
  static int coins = 0;
  static char b;
  static int mz = 0;
  static int length_instance = 0;

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
      for(int mx = 0; mx < envi.length; mx++) for(int my = 0; my < envi[mx].length; my++){
        fich.write("@ATTRIBUTE pos_environment_[" + mx + "," + my + "] NUMERIC \n");
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
      fich.write("@ATTRIBUTE reward NUMERIC \n");
      fich.write("@ATTRIBUTE posMarioEgo_1 NUMERIC \n");
      fich.write("@ATTRIBUTE posMarioEgo_2 NUMERIC \n");
      fich.write("@ATTRIBUTE marioStatus NUMERIC \n");
      fich.write("@ATTRIBUTE marioMode NUMERIC \n");
      fich.write("@ATTRIBUTE isMarioOnGround NUMERIC \n");
      fich.write("@ATTRIBUTE isMarioAbleToJump NUMERIC \n");
      fich.write("@ATTRIBUTE isMarioAbleToShoot NUMERIC \n");
      fich.write("@ATTRIBUTE isMarioCarrying NUMERIC \n");
      fich.write("@ATTRIBUTE killsTotal NUMERIC \n");
      fich.write("@ATTRIBUTE killsByFire NUMERIC \n");
      fich.write("@ATTRIBUTE killsByStomp NUMERIC \n");
      fich.write("@ATTRIBUTE killsByShell NUMERIC \n");
      fich.write("@ATTRIBUTE timeLeft NUMERIC \n");
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

    //Escribir en el fichero toda una instacia
    try{
        fich = new FileWriter("ejemplos.arff",true);
        file = new File("ejemplos.arff");
        br = new BufferedReader(new FileReader("ejemplos.arff"));

        //Establecer Header de fichero arff únicamente en el primer tick
        //Si no existe el fichero o existe y está vacío...
        if(br.readLine() == null || !file.exists()) init_arff(envi);

        for(mz = 0; mz < length_instance; mz++){
          if(mz != length_instance-1) fich.write(instancia[mz] + ", ");
          else fich.write(instancia[mz] + "\n");
        }

          //Añadir los enemigos
        //  qEnemies.add(dataMatrix[8]);
          //Añadir las monedas
        //  qCoins.add(dataMatrix[23]);
        //}

        //Si estamos en el sexto tick de juego, empieza a poner valores de hace 5 ticks
        // if(tick >= 6){
        //   //Eliminar y mostrar la head de la cola, LRI
        //   enemies = qEnemies.poll();
        //   coins = qCoins.poll();
        //   fich.write(enemies + ", " + coins + ", ");
        // }

        fich.close();
        mz = 0;
    }
    catch(IOException e){
        e.printStackTrace(System.out);
    }
  }
}
