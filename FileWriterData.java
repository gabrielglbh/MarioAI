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
  static Queue<Integer> qEnemies = new LinkedList<Integer[]>();
  static Queue<Integer> qCoins = new LinkedList<Integer>();
  static Queue<Integer> qInstancia= new LinkedList<Integer>();

  static int enemies = 0;
  static int coins = 0;
  static char b;

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

    for(int mx = 0; mx < envi.length; mx++) for(int my = 0; my < envi[mx].length; my++) qInstancia.add(envi[mx][my]);
    for(int mx = 0; mx < posMario.length; mx++) qInstancia.add(posMario[mx]);
    for(int mx = 0; mx < dataMatrix.length; mx++) qInstancia.add(dataMatrix[mx]);
    qInstancia.add(classWeka);

    if(tick >= 24){ //Somos adivinos, y solo va a escribir en el fichero cuando esté en el tick del futuro, 24
      try{
          fich = new FileWriter("ejemplos.arff",true);
          //getMergedObservationZZ
          for(int mx = 0; mx < envi.length; mx++) for(int my = 0; my < envi[mx].length; my++) fich.write(envi[mx][my] + ", ");
          //Get posicion de Mario en grid
          for(int mx = 0; mx < posMario.length; mx++) fich.write(posMario[mx] + ", ");
          //Get el resto de datos de los ticks
          for(int mx = 0; mx < dataMatrix.length; mx++){
            fich.write(dataMatrix[mx] + ", ");
            //Añadir los enemigos
            qEnemies.add(dataMatrix[8]);
            //Añadir las monedas
            qCoins.add(dataMatrix[23]);
          }

          //Si estamos en el sexto tick de juego, empieza a poner valores de hace 5 ticks
          // if(tick >= 6){
          //   //Eliminar y mostrar la head de la cola, LRI
          //   enemies = qEnemies.poll();
          //   coins = qCoins.poll();
          //   fich.write(enemies + ", " + coins + ", ");
          // }

          //Last added is the class (distancePassedPhys)
          fich.write(classWeka + " TICKS: " + tick + "\n");
          System.out.println("Ticks: " + tick);

          fich.close();
      }
      catch(IOException e){
          e.printStackTrace(System.out);
      }
    }
  }
}
