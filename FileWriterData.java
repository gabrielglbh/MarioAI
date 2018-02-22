package ch.idsia.agents.controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.nio.file.Files;
import java.util.*;

public class FileWriterData{
  //Variables globales para el fichero de ejemplos.txt
  static FileWriter fich = null;
  //Queue para escribir los enemigos abatidos y monedas recogidas hace 5 ticks
  static Queue<Integer> qEnemies = new LinkedList<Integer>();
  static Queue<Integer> qCoins = new LinkedList<Integer>();
  static Queue<Integer> qInstancia= new LinkedList<Integer>();

  static int enemies = 0;
  static int coins = 0;
  static char b;
  static int length_instance = 0;

  public FileWriterData(){
  }

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

  //Metodo auxiliar en Environment class para facilitar el entendimiento del codigo
  //Escribir en fichero los datos de los ticks
  public static void writeOnFile(float[] posMario, int[] dataMatrix, byte[][] envi, int classWeka, int tick){

    length_instance = envi.length*envi[0].length+posMario.length+dataMatrix.length+1;
    String[] instancia = new String[length_instance];

    for(int mz = 0; mz < (length_instance-(posMario.length+dataMatrix.length+1)); mz++){
      for(int mx = 0; mx < envi.length; mx++) for(int my = 0; my < envi[mx].length; my++)
        instancia[mz] = String.valueOf(envi[mx][my]);
    }

    for(int mz = envi.length*envi[0].length; mz < ((length_instance)-(dataMatrix.length+1)); mz++){
      for(int mx = 0; mx < posMario.length; mx++) instancia[mz] = String.valueOf(posMario[mx]);
    }

    for(int mz = envi.length*envi[0].length+posMario.length; mz < (length_instance-1); mz++){
      for(int mx = 0; mx < dataMatrix.length; mx++) instancia[mz] = String.valueOf(dataMatrix[mx]);
    }

    instancia[envi.length*envi[0].length+posMario.length+dataMatrix.length] = String.valueOf(classWeka);

    //if(tick >= 24){ //Somos adivinos, y solo va a escribir en el fichero cuando esté en el tick del futuro, 24
      try{
          fich = new FileWriter("ejemplos.arff",true);
          for(int mz = 0; mz < length_instance; mz++){
            if(mz != length_instance-1) fich.write(instancia[mz] + ", "); 
	    else fich.write(instancia[mz]);
          }
          fich.write("\n");
         
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
      }
      catch(IOException e){
          e.printStackTrace(System.out);
      }
    //}
  }
}
