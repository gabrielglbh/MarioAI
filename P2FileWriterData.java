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
  static int mz = 0; // Ãƒï¿½ndice para recorrer las posiciones de instancia, tanto para dar valor como para leer
  static int length_instance = 0; // Vestigial, se puede quitar (no me atrevo, no sea que la lÃƒÂ­e)
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

  public static void init_arff(byte[][] envi){
    try{
      fich = new FileWriter("ejemplos.arff",true);
      String callerClass = new Exception().getStackTrace()[2].getClassName();
      callerClass = callerClass.substring(callerClass.lastIndexOf('.')+1);
      fich.write("@RELATION " + callerClass + "\n\n");

      for(int mx = 0; mx < envi.length; mx++) for(int my = 0; my < envi[mx].length; my++){
        fich.write("@ATTRIBUTE pos_environment_[" + mx + "_" + my + "] NUMERIC \n");
      } // 361

      fich.write("@ATTRIBUTE posMario_1 NUMERIC \n");
      fich.write("@ATTRIBUTE posMario_2 NUMERIC \n");

      fich.write("@ATTRIBUTE posMarioEgo_1 NUMERIC \n"); //------- inutil
      fich.write("@ATTRIBUTE posMarioEgo_2 NUMERIC \n"); //------- inutil
      fich.write("@ATTRIBUTE distancePassedCells NUMERIC \n");
      fich.write("@ATTRIBUTE distancePassedPhys NUMERIC \n");
      fich.write("@ATTRIBUTE flowersDevoured NUMERIC \n"); //----- 4
      fich.write("@ATTRIBUTE killsByFire NUMERIC \n");
      fich.write("@ATTRIBUTE killsByShell NUMERIC \n");
      fich.write("@ATTRIBUTE killsByStomp NUMERIC \n");
      fich.write("@ATTRIBUTE killsTotal NUMERIC \n");
      fich.write("@ATTRIBUTE marioMode NUMERIC \n"); //----------- 9
      fich.write("@ATTRIBUTE marioStatus NUMERIC \n"); //---- inutil para nosotros, siempre es 2 en ejecucion
      fich.write("@ATTRIBUTE mushroomsDevoured NUMERIC \n");
      fich.write("@ATTRIBUTE coinsGained NUMERIC \n");
      fich.write("@ATTRIBUTE timeLeft NUMERIC \n");
      fich.write("@ATTRIBUTE timeSpent NUMERIC \n"); //----------- 14
      fich.write("@ATTRIBUTE hiddenBlocksFound NUMERIC \n");
      fich.write("@ATTRIBUTE coinsInScreen NUMERIC \n");
      fich.write("@ATTRIBUTE blocksInScreen NUMERIC \n");
      fich.write("@ATTRIBUTE enemiesInScreen NUMERIC \n");
      fich.write("@ATTRIBUTE reward NUMERIC \n"); //-------------- 19

//      fich.write("@ATTRIBUTE marioStatus_2 NUMERIC \n");
//      fich.write("@ATTRIBUTE marioMode_2 NUMERIC \n");
      fich.write("@ATTRIBUTE isMarioOnGround NUMERIC \n"); 
      fich.write("@ATTRIBUTE isMarioAbleToJump NUMERIC \n");
      fich.write("@ATTRIBUTE isMarioAbleToShoot NUMERIC \n");
      fich.write("@ATTRIBUTE isMarioCarrying NUMERIC \n"); 
//      fich.write("@ATTRIBUTE killsTotal_2 NUMERIC \n");
//      fich.write("@ATTRIBUTE killsByFire_2 NUMERIC \n");
//      fich.write("@ATTRIBUTE killsByStomp_2 NUMERIC \n");
//      fich.write("@ATTRIBUTE killsByShell_2 NUMERIC \n");
//      fich.write("@ATTRIBUTE timeLeft_2 NUMERIC \n");

      fich.write("@ATTRIBUTE isEnemyNear {true,false} \n");
      fich.write("@ATTRIBUTE isObstacleNear {true,false} \n");
      fich.write("@ATTRIBUTE ticks_since_jump NUMERIC %%%%%% 1 cuando se puede volver a saltar \n");
      fich.write("@ATTRIBUTE predict_n24 NUMERIC \n");

      fich.write("@ATTRIBUTE reward_tick_n6 NUMERIC \n");
      //fich.write("@ATTRIBUTE killsTotal_tick_n6 NUMERIC \n");
      fich.write("@ATTRIBUTE reward_tick_n12 NUMERIC \n");
      //fich.write("@ATTRIBUTE killsTotal_tick_n12 NUMERIC \n");
      fich.write("@ATTRIBUTE reward_tick_n24 NUMERIC \n");
      //fich.write("@ATTRIBUTE killsTotal_tick_n24 NUMERIC \n");

      fich.write("@ATTRIBUTE JUMP_Action {Jump,Do_Not_Jump} \n");

      fich.write("\n@data \n");
    }
    catch(IOException e){
        e.printStackTrace(System.out);
    }
  }

  //Metodo auxiliar en Environment class para facilitar el entendimiento del codigo
  //Escribir en fichero los datos de los ticks
  public static void writeOnFile( byte[][] envi, float[] posMario, int[] dataMatrix, 
		  						  int[] marioState, int ticks_since_jump, String jump_action, int tick ){

	  //if (tick < 2) System.out.println("Tick: " + tick);
    if(dataMatrix[10] == 0){
      P2FileWriterData.close_arff();
      return;
    }

    // Monedas y enemigos en una zona de 5x5 estando Mario en la casilla [0][2]
    int coinsInZone = 0;
    int enemiesInZone = 0;

    // PredicciÃƒÂ³n del incremento de la recompensa en 24 ticks
    double predict_n24 = -1000.0;
    boolean ruleTriggered = false;

    for (int ii = -2; ii < 3; ii++) for(int jj = 0; jj < 5; jj++) {
          switch (envi[9+ii][9+jj]) {
                case 2:
                      coinsInZone++;
                      break;
                    case 80: //Goomba
                    case 95: //Goomba con alas
                    case 82: //Koopa rojo
                    case 97: //Koopa rojo con alas
                    case 81: //Koopa verde
                    case 96: //Koopa verde con alas
                    case 84: //Bala
                    case 93: //Enemigo puntiagudo
                    case 99: //Enemigo puntiagudo con alas
                    case 91: //Flor enemiga
                    case -42: //Tipo de enemigo indefinido
                      enemiesInZone++;
                      break;
                    default:
          }
    }

    // Regla 1
  if( dataMatrix[16]>11.5
          && dataMatrix[17]>81.5
          && !ruleTriggered ){
    predict_n24 = -2.5836 * dataMatrix[7]
                + 16.4963 * dataMatrix[9]
                - 0.0789 * dataMatrix[12]
                - 0.1753 * dataMatrix[16]
                - 2.2176 * dataMatrix[17]
                - 0.1549 * dataMatrix[18]
                + 4.5931 * coinsInZone
                - 0.0576 * enemiesInZone
                + 4.7034 + 0.4138
                + 11.0464 * (jump_action == "Jump"? 1:0)
                + 360.7746;
    ruleTriggered = true;
  }
  // Regla 2
  if(coinsInZone > 0.5
          && dataMatrix[16] > 11.5
          && dataMatrix[7] <= 2.5
          && dataMatrix[16] > 14.5
          && !ruleTriggered ){
    predict_n24 = 0.7945 * dataMatrix[7]
                - 0.1725 * dataMatrix[12]
                - 0.4159 * dataMatrix[16]
                + 3.6194 * dataMatrix[17]
                + 0.5594 * dataMatrix[18]
                + 6.5092 * coinsInZone
                + 16.2018 * enemiesInZone
                + 5.0511 + 0.2127 + 1.3356 + 11.3935 - 194.5202;
    ruleTriggered = true;
  }
  // Regla 3
  if(dataMatrix[16] > 11.5
          && coinsInZone > 1.5
          && dataMatrix[17] <= 60
          && !ruleTriggered ){
    predict_n24 = 5.5906 * dataMatrix[7]
                - 0.5553 * dataMatrix[9]
                - 2.3657 * dataMatrix[12]
                + 0.1163 * dataMatrix[16]
                + 1.8562 * dataMatrix[17]
                + 24.8336 * dataMatrix[18]
                + 4.2969 * coinsInZone
                - 0.5477 * enemiesInZone
                + 0.6265 - 5.4155 + 2.1106 - 10.0002;
        ruleTriggered = true;
  }
  // Regla 4
  if( dataMatrix[16] > 11.5
          && coinsInZone > 0.5
          && dataMatrix[16] > 13.5
          && !ruleTriggered ){
    predict_n24 = 1.3399 * dataMatrix[7]
                - 0.4846 * dataMatrix[9]
                - 0.7995 * dataMatrix[12]
                + 14.4966 * dataMatrix[16]
                + 2.1332 * dataMatrix[17]
                - 0.2733 * dataMatrix[18]
                + 4.9058 * coinsInZone
                - 1.8137 * enemiesInZone
                + 1.1924 + 0.7505 - 0.7418 - 229.3542;
    ruleTriggered = true;
  }
  // Regla 5
  if( coinsInZone <= 0.5
          && dataMatrix[7] <= 11.5
          && dataMatrix[12] > 11
          && !ruleTriggered ){
    predict_n24 = -9.7359 * dataMatrix[7]
                - 0.0555 * dataMatrix[12]
                - 1.9915 * dataMatrix[16]
                + 0.1068 * dataMatrix[17]
                - 1.341 * dataMatrix[18]
                + 0.2126 * coinsInZone
                - 0.1051 * enemiesInZone
                + 5.1497 + 42.5463;
    ruleTriggered = true;
  }
  // Regla 6
  if( dataMatrix[16] > 7.5
          && dataMatrix[16] > 11.5
          && !ruleTriggered ){
    predict_n24 = 2.2395 * dataMatrix[7]
                + 16.7436 * dataMatrix[9]
                - 0.1009 * dataMatrix[12]
                + 1.0106 * dataMatrix[16]
                + 9.2044 * dataMatrix[18]
                + 0.8696 * coinsInZone
                - 2.4583 * enemiesInZone
                + 0.8036 + 59.375;
    ruleTriggered = true;
  }
  // Regla 7
  if( enemiesInZone <= 0.5
          && dataMatrix[12] > 8.5
          && dataMatrix[16] > 7.5
          && !ruleTriggered ){
    predict_n24 = 0.2886 * dataMatrix[7]
                - 0.7408 * dataMatrix[9]
                - 0.0333 * dataMatrix[12]
                + 0.915 * dataMatrix[16]
                + 0.801 * dataMatrix[17]
                - 0.3859 * dataMatrix[18]
                - 0.0143 * dataMatrix[19]
                + 0.7269 * coinsInZone
                - 0.2407 * enemiesInZone
                + 0.4174 + 1.1265 + 35.6678;
    ruleTriggered = true;
  }
  // Regla 8
  if( dataMatrix[7] <= 10.5
          && dataMatrix[16] > 4.5
          && dataMatrix[19] <= 89
          && dataMatrix[17] <= 73
          && !ruleTriggered ){
    predict_n24 = 0.7368 * dataMatrix[7]
                + 6.4607 * dataMatrix[9]
                + 0.0248 * dataMatrix[12]
                + 0.0708 * dataMatrix[16]
                - 0.213 * dataMatrix[17]
                - 1.7763 * dataMatrix[18]
                - 0.287 * dataMatrix[19]
                + 4.1572 * coinsInZone
                + 87.1881 + 0.5971 + 11.3064;
    ruleTriggered = true;
  }
  // Regla 9
  if( dataMatrix[7] <= 3
          && dataMatrix[19] <= 14
          && dataMatrix[9] > 1.5
          && dataMatrix[17] > 30.5
          && !ruleTriggered ){
    predict_n24 = 0.4905 * dataMatrix[7]
                - 7.3557 * dataMatrix[9]
                - 0.0404 * dataMatrix[12]
                + 2.2243 * dataMatrix[16]
                - 0.1781 * dataMatrix[17]
                - 3.5255 * dataMatrix[18]
                - 0.0219 * dataMatrix[19]
                + 0.657 * coinsInZone
                + 1.6325 + 9.3946;
    ruleTriggered = true;
  }
  // Regla 10
  if( dataMatrix[7] <= 10.5
          && dataMatrix[19] > 9
          && dataMatrix[7] <= 3
          && dataMatrix[17] <= 50.5
          && !ruleTriggered ){
    predict_n24 = 4.1208 * dataMatrix[7]
                - 0.4466 * dataMatrix[12]
                + 0.2303 * dataMatrix[16]
                - 0.0675 * dataMatrix[17]
                + 3.2513 * coinsInZone
                + 2.1006 - 40.6964;
    ruleTriggered = true;
  }
  // Regla 12
  if(dataMatrix[7] > 10.5
          && coinsInZone <= 1.5
          && dataMatrix[7] > 11.5
          && dataMatrix[18] > 2.5
          && !ruleTriggered ){
    predict_n24 = 3.6943 * dataMatrix[7]
                + 0.734 * dataMatrix[12]
                + 0.1746 * dataMatrix[16]
                - 0.0667 * dataMatrix[17]
                - 3.3584 * dataMatrix[18]
                + 1.2911 * coinsInZone
                + 11.4685 - 47.9376;
    ruleTriggered = true;
  }
  // Regla 13
  if( dataMatrix[9] > 1.5
          && !ruleTriggered ){
    predict_n24 = 7.441 * dataMatrix[7]
                - 0.4031 * dataMatrix[12]
                + 0.9111 * dataMatrix[16]
                - 31.9808 * dataMatrix[18]
                - 4.0907 * enemiesInZone
                + 41.5712;
    ruleTriggered = true;
  }
  // Regla 14
  if( enemiesInZone <= 0.5
          && dataMatrix[16] > 3
          && !ruleTriggered){
    predict_n24 = 1.9518 * dataMatrix[7]
                - 0.2152 * dataMatrix[12]
                + 1.2312 * dataMatrix[16]
                - 3.0986 * enemiesInZone
                + 42.1946;
    ruleTriggered = true;
  }
  // Regla 15
  if( dataMatrix[17] <= 68
          && dataMatrix[17] > 52.5
          && !ruleTriggered){
    predict_n24 = -1.6546 * dataMatrix[17]
                + 12.0933 * coinsInZone
                + 117.1664;
    ruleTriggered = true;
  }
  // Regla 16
  if( dataMatrix[12] > 49.5
          && !ruleTriggered){
    predict_n24 = -0.417 * dataMatrix[12]
                + 21.8963;
    ruleTriggered = true;
  }
  // Regla 11
  if(!ruleTriggered ){
    predict_n24 = 0.2126 * dataMatrix[12]
                + 0.2802 * dataMatrix[16]
                + 0.098 * dataMatrix[17]
                + 0.9349 * coinsInZone
                - 2.2426 + 4.9024 + 17.9327;
    ruleTriggered = true;
  }
  // Regla 17
  if( !ruleTriggered ){
    predict_n24 = 62.4667;
    ruleTriggered = true;
  }
  
  	// +5: isEnemyNear, isObstacleNear, ticks_since_jump, predict_n24, JUMP_action
    length_instance = envi.length*envi[0].length + posMario.length + dataMatrix.length + marioState.length +5;
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

    for(int mx = 0; mx < marioState.length; mx++){
      instancia[mz] = String.valueOf(marioState[mx]);
      mz++;
    }

    //isEnemyNear
    isEnemy = false;
    if(envi[9][10] == 80 || envi[9][11] == 80 ||
        envi[8][10] == 80 || envi[8][11] == 80){
      isEnemy = true;
      instancia[mz] = String.valueOf(isEnemy);
    }
    else{
      isEnemy = false;
      instancia[mz] = String.valueOf(isEnemy);
    }
    mz++;

    //isObstacleNear
    isBlock = false;
    if( envi[9][10] == -24 || envi[9][11] == -24 || envi[8][10] == -24 || envi[8][11] == -24 ||
        envi[7][10] == -24 || envi[7][11] == -24 ||
        envi[9][10] == -60 || envi[9][11] == -60 || envi[8][10] == -60 || envi[8][11] == -60 ||
        envi[7][10] == -60 || envi[7][11] == -60 ||
        envi[9][10] == -85 || envi[9][11] == -85 || envi[8][10] == -85 || envi[8][11] == -85 ||
        envi[7][10] == -85 || envi[7][11] == -85 ||
        envi[9][10] == -62 || envi[9][11] == -62 || envi[8][10] == -62 || envi[8][11] == -62 ||
        envi[7][10] == -62 || envi[7][11] == -62 ||
        envi[9][10] == -22 || envi[9][11] == -22 || envi[8][10] == -22 || envi[8][11] == -22 ||
        envi[7][10] == -22 || envi[7][11] == -22 ||
        envi[9][10] == -20 || envi[9][11] == -20 || envi[8][10] == -20 || envi[8][11] == -20 ||
        envi[7][10] == -20 || envi[7][11] == -20 ){
      isBlock = true;
      instancia[mz] = String.valueOf(isBlock);
    }
    else{
      isBlock = false;
      instancia[mz] = String.valueOf(isBlock);
    }
    mz++;

    instancia[mz] = String.valueOf(ticks_since_jump);
    mz++;

    instancia[mz] = String.valueOf(predict_n24);
    mz++;

    instancia[mz] = jump_action;

    myInstance.add(instancia);

    /*
      Se añade la recompensa obtenida en los proximos ticks (desde ahora hasta dentro de 6, 12 y 24 ticks
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

      /* Aqui se calcula cuanto aumenta la recompensa (reward) en los próximos 6, 12 y 24 ticks*/
      futureRewIncrement[0] = futureReward[6+count] - futureReward[count]; // Reward
      futureRewIncrement[1] = futureReward[12+count] - futureReward[count]; // Reward
      futureRewIncrement[2] = futureReward[24+count] - futureReward[count]; // Reward

      //Escribir en el fichero toda una instacia
      try {
          file = new File("ejemplos.arff");
          br = new BufferedReader(new FileReader("ejemplos.arff"));

          // Establecer Header de fichero arff únicamente en el primer tick
          // Si no existe el fichero o existe y está vacío
          if(!file.exists() || br.readLine() == null) init_arff(envi);

          //Sacar el head-tick de la cola y concatenarlo con futureAttributes
          String[] instanciaActual = myInstance.poll();
          String[] instanciaCompleta = new String[instanciaActual.length + futureRewIncrement.length];

          //Establcemos la instanciaCompleta...
          for(int a = 0; a < instanciaActual.length; a++){
            //Mientras que no estemos en la ultima posicion ir añadiendo atributos
            if(a != instanciaActual.length-1) instanciaCompleta[a] = instanciaActual[a];
            /*
              Si estamos en la última posicion de la instanciaActual, añadir
              loa futureAttributes a la instanciaCompleta y por último añadir
              la clase JUMP_action.
            */
            else{
              for(int my = 0; my < futureRewIncrement.length; my++){
                instanciaCompleta[a+my] = String.valueOf( futureRewIncrement[my] );
              }
              // Añadir el último atributo de instanciaActual al final de instanciaCompleta, para que sea la clase
              instanciaCompleta[instanciaCompleta.length-1] = instanciaActual[instanciaActual.length-1];
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
            el incremento futuro de la recompensa y la acción al final (la clase).
          */

          //fich.write("%%%%%%%%%%%%%%%%%%%%------------TICK: " + tick + "----------%%%%%%%%%%%%%%%%%%%%\n");
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
