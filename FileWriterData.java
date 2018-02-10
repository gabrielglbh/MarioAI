package ch.idsia.agents.controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileWriterData{
  //Variables globales para el fichero de ejemplos.txt
  File fd  = new File("ejemplos.txt");
  FileWriter fich = null;

  public FileWriterData(){  }

  //Metodo auxiliar en Environment class para facilitar el entendimiento del codigo
  //Escribir en fichero los datos de los ticks
  public void writeOnFile(float[] posMario, int[] dataMatrix, byte[][] envi){
    //Recibe matriz de elementos a escribir en fichero
    try{
        fich = new FileWriter(fd,true);
        //Get posicion de Mario en grid
        for (int mx = 0; mx < posMario.length; mx++) fich.write(posMario[mx] + ", ");
        //Get el resto de datos de los ticks
        for (int mx = 0; mx < dataMatrix.length; mx++) fich.write(dataMatrix[mx] + ", ");
        //getMergedObservationZZ
        for(int mx = 0; mx < envi.length; mx++) for(int my = 0; my < envi[mx].length; my++) fich.write(envi[mx][my] + ", ");
        fich.write("\n");
        fich.close();
    }
    catch(IOException e){
        System.out.println("Error al escribir en archivo");
    }
  }
}
