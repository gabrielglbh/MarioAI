package ch.idsia.agents.controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.nio.file.Files;
import java.util.*;

public class FileWriterData{
  //Variables globales para el fichero de ejemplos.txt
  static File fd  = new File("ejemplos.txt");
  static FileWriter fich = null;
  //Queue para escribir los enemigos abatidos y monedas recogidas hace 5 ticks
  static Queue<Integer> qEnemies = new LinkedList<Integer>();
  static Queue<Integer> qCoins = new LinkedList<Integer>();

  static int enemies = 0;
  static int coins = 0;

  public FileWriterData(){
  }

  //No se usa en nada de momento
  public static void prepareFile(){
	  try {
		  boolean result = Files.deleteIfExists(fd.toPath());
		  //boolean result2 = fd.delete();
	  }
	  catch(Exception e) {
		  System.out.println("Error al borrar archivo de partida previa");
	  }
  }

  //Metodo auxiliar en Environment class para facilitar el entendimiento del codigo
  //Escribir en fichero los datos de los ticks
  public static void writeOnFile(float[] posMario, int[] dataMatrix, byte[][] envi, int tick){
    //Recibe matriz de elementos a escribir en fichero
    try{
        fich = new FileWriter(fd,true);
        //Get posicion de Mario en grid
        for (int mx = 0; mx < posMario.length; mx++) fich.write(posMario[mx] + ", ");
        //Get el resto de datos de los ticks
        for (int mx = 0; mx < dataMatrix.length; mx++){
          fich.write(dataMatrix[mx] + ", ");
          //Añadir los enemigos
          qEnemies.add(dataMatrix[19]);
          //Añadir las monedas
          qCoins.add(dataMatrix[23]);
        }
        //getMergedObservationZZ
        for(int mx = 0; mx < envi.length; mx++) for(int my = 0; my < envi[mx].length; my++) fich.write(envi[mx][my] + ", ");

        //Si estamos en el sexto tick de juego, empieza a poner valores de hace 5 ticks
        if(tick >= 6){
          //Eliminar y mostrar la head de la cola, LRI
          enemies = qEnemies.poll();
          coins = qCoins.poll();
          fich.write(enemies + ", " + coins);
        }

        fich.write("\n");
        fich.close();
    }
    catch(IOException e){
        System.out.println("Error al escribir en archivo");
    }
  }
}
