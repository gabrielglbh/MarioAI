/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Mario AI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package ch.idsia.agents.controllers;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.agents.controllers.FileWriterData;
import ch.idsia.tools.EvaluationInfo;

import java.io.FileWriter;
import java.util.Random;

public class ClassificationAgent extends BasicMarioAIAgent implements Agent {

    int tick;
    private Random R = null;
    /**
     * Atributos para almacenar la informaciÃ³n del entorno de Mario
     * y poder usarla para determinar su comportamiento
     * (para que no vaya como pollo sin cabeza)
     */
    //int mx;
    int[] dataMatrix = new int[20]; // Matriz de informacion de la partida en cada tick
    byte[][] envi = new byte[19][19]; // Matriz del entorno de Mario (el grid)
    float[] posMario;
    int[] marioState;

    int jumpButtonPressed = -1;
    int coinsInScreen = 0;
    int blocksInScreen = 0;
    int enemiesInScreen = 0;

    int count = 0;

    boolean isEnemy = false;
    boolean isBlock = false;

    String multiclass = "";

    public ClassificationAgent() {
        super("ClassificationAgent");
        reset();
        tick = 0;
        try{
          FileWriterData.fich = new FileWriter("ejemplos.arff",true);
        }
        catch(Exception e){
          e.printStackTrace(System.out);
        }
    }

    public void reset() {
        // Dummy reset, of course, but meet formalities!
        R = new Random();
    }

    public void integrateObservation(Environment environment) {
          tick++;
        // IMPORTANTE: Si se utilizan mÃ©todos que tardan mucho como println, cada tick puede tardar en procesarse mÃ¡s de
        // de lo que permite la competiciÃ³n de Mario AI. Si el agente es demasiado lento procesando y el simulador no
        // puede funcionar en tiempo real, se cerrarÃ¡ automÃ¡ticamente, lor lo que se insta a que el cÃ³digo escrito sea
        // lo mÃ¡s eficiente posible.


        // INFORMACION DEL ENTORNO

        // En la interfaz Environment.java vienen definidos los metodos que se pueden emplear para recuperar informacion
        // del entorno de Mario. Algunos de los mas importantes (y que utilizaremos durante el curso)...

        //System.out.println("------------------ TICK " + tick + " ------------------");

        /*
        // Devuelve un array de 19x19 donde Mario ocupa la posicion 9,9 con informacion de los elementos
        // en la escena. La funcion getLevelSceneObservationZ recibe un numero para indicar el nivel de detalle
        // de la informacion devuelta. En uno de los anexos del tutorial 1 se puede encontrar informacion de
        // los niveles de detalle y el tipo de informacion devuelta.
        System.out.println("\nESCENA");
        byte [][] envesc;
        envesc = environment.getLevelSceneObservationZ(1);
        for (int mx = 0; mx < envesc.length; mx++){
            System.out.print(mx + ": [");
            for (int my = 0; my < envesc[mx].length; my++)
                System.out.print(envesc[mx][my] + " ");
            System.out.println("]");
        }
        */

        /*
        // Devuelve un array de 19x19 donde Mario ocupa la posicion 9,9 con informacion de los enemigos
        // en la escena. La funcion getEnemiesObservationZ recibe un numero para indicar el nivel de detalle
        // de la informacion devuelta. En uno de los anexos del tutorial 1 se puede encontrar informacion de
        // los niveles de detalle y el tipo de informacion devuelta.
        System.out.println("\nENEMIGOS");
        byte [][] envenm;
        envenm = environment.getEnemiesObservationZ(1);
        for (int mx = 0; mx < envenm.length; mx++) {
            System.out.print(mx + ": [");
            for (int my = 0; my < envenm[mx].length; my++)
                System.out.print(envenm[mx][my] + " ");
            System.out.println("]");
        }
        */

        // Devuelve un array de 19x19 donde Mario ocupa la posicion 9,9 con la union de los dos arrays
        // anteriores, es decir, devuelve en un mismo array la informacion de los elementos de la
        // escena y los enemigos.
        //System.out.println("\nMERGE");
        byte [][] env;
        coinsInScreen = 0;
        enemiesInScreen = 0;
        blocksInScreen = 0;
        env = environment.getMergedObservationZZ(1, 1);
        for (int mx = 0; mx < env.length; mx++) {
            //System.out.print(mx + ": [");
            for (int my = 0; my < env[mx].length; my++) {
                //System.out.print(env[mx][my] + " ");
                envi[mx][my] = env[mx][my];
                //Coins on screen
                if (envi[mx][my] == 2) coinsInScreen++;
                //If blocks
                if (envi[mx][my] == -22 || envi[mx][my] == -60 || envi[mx][my] == -80 ||
                    envi[mx][my] == -62 || envi[mx][my] == -24 || envi[mx][my] == -85)
                    blocksInScreen++;
                //If enemies
                if (envi[mx][my] == 1 || envi[mx][my] == 80 || envi[mx][my] == 95 ||
                    envi[mx][my] == 82 || envi[mx][my] == 97 || envi[mx][my] == 81 ||
                    envi[mx][my] == 96 || envi[mx][my] == 84 || envi[mx][my] == 93 ||
                    envi[mx][my] == 99 || envi[mx][my] == 91 || envi[mx][my] == 13 ||
                    envi[mx][my] == -42)
                    enemiesInScreen++;
            //System.out.println("]");
            }
        }

        // Posicion de Mario utilizando las coordenadas del sistema
        //System.out.println("POSICION MARIO");
        posMario = environment.getMarioFloatPos();
        //for (mx = 0; mx < posMario.length; mx++)
          //System.out.print(posMario[mx] + " ");

        // Posicion que ocupa Mario en el array anterior
        //System.out.println("\nPOSICION MARIO MATRIZ");
        int[] posMarioEgo;
        posMarioEgo = environment.getMarioEgoPos();
        for (int mx = 0; mx < posMarioEgo.length; mx++){
          //System.out.print(posMarioEgo[mx] + " ");
          dataMatrix[mx] = posMarioEgo[mx];
        }

        // Estado de mario
        // marioStatus, marioMode, isMarioOnGround (1 o 0), isMarioAbleToJump() (1 o 0), isMarioAbleToShoot (1 o 0),
        // isMarioCarrying (1 o 0), killsTotal, killsByFire,  killsByStomp, killsByShell, timeLeft
        //System.out.println("\nESTADO MARIO");
        //int[] marioState;
        marioState = environment.getMarioState();
        //for (int mx = 0; mx < marioState.length; mx++){
          //System.out.print(marioState[mx] + " ");
          //dataMatrix[mx+posMarioEgo.length] = marioState[mx];
        //}

        // Mas informacion de evaluacion...
        // distancePassedCells, distancePassedPhys, flowersDevoured, killsByFire, killsByShell, killsByStomp, killsTotal, marioMode,
        // marioStatus, mushroomsDevoured, coinsGained, timeLeft, timeSpent, hiddenBlocksFound
        //System.out.println("\nINFORMACION DE EVALUACION");
        int[] infoEvaluacion;
        infoEvaluacion = environment.getEvaluationInfoAsInts();
        for (int mx = 0; mx < infoEvaluacion.length; mx++){
          //System.out.print(infoEvaluacion[mx] + " ");
          dataMatrix[mx+posMarioEgo.length] = infoEvaluacion[mx];
        }

        // Informacion del refuerzo/puntuacion que ha obtenido Mario. Nos puede servir para determinar lo bien o mal que lo esta haciendo.
        // Por defecto este valor engloba: reward for coins, killed creatures, cleared dead-ends, bypassed gaps, hidden blocks found
        //System.out.println("\nREFUERZO");
        int reward = environment.getIntermediateReward();

        dataMatrix[16] = coinsInScreen;
        dataMatrix[17] = blocksInScreen;
        dataMatrix[18] = enemiesInScreen;
        dataMatrix[19] = reward;

        //isEnemy
        isEnemy = false;
        if(envi[9][10] == 80 || envi[9][11] == 80 ||
            envi[8][10] == 80 || envi[8][11] == 80){
          isEnemy = true;
        }
        else{
          isEnemy = false;
        }

        //isBlock
        isBlock = false;
        if(envi[9][10] == -24 || envi[9][11] == -24 || envi[8][10] == -24 || envi[8][11] == -24 ||
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
            envi[7][10] == -20 || envi[7][11] == -20){
          isBlock = true;
        }
        else{
          isBlock = false;
        }

    }

    public boolean[] getAction() {
        // La accion es un array de booleanos de dimension 6
        // action[Mario.KEY_LEFT] Mueve a Mario a la izquierda
        // action[Mario.KEY_RIGHT] Mueve a Mario a la derecha
        // action[Mario.KEY_DOWN] Mario se agacha si esta en estado grande
        // action[Mario.KEY_JUMP] Mario salta
        // action[Mario.KEY_SPEED] Incrementa la velocidad de Mario y dispara si esta en modo fuego
        // action[Mario.KEY_UP] Arriba
        // Se puede utilizar cualquier combinacion de valores true, false para este array
        // Por ejemplo: (false true false true false false) Mario salta a la derecha
        // IMPORTANTE: Si se ejecuta la accion anterior todo el tiempo, Mario no va a saltar todo el tiempo hacia adelante.
        // Cuando se ejecuta la primera vez la accion anterior, se pulsa el boton de saltar, y se mantiene pulsado hasta que
        // no se indique explicitamente action[Mario.KEY_JUMP] = false. Si habeis podido jugar a Mario en la consola de verdad,
        // os dareis cuenta que si manteneis pulsado todo el tiempo el boton de saltar, Mario no salta todo el tiempo sino una
        // unica vez en el momento en que se pulsa. Para volver a saltar debeis despulsarlo (action[Mario.KEY_JUMP] = false),
        // y volverlo a pulsar (action[Mario.KEY_JUMP] = true).

        action[Mario.KEY_RIGHT] = true;

        count++;

        if(marioState[3] == 1 && marioState[2] == 1){
          count = 1;
        }

        //M = 350, J48 - Human [Selec-NonD-AugProx.arff]
        if(isBlock){ // = true
            if(count <= 4) action[Mario.KEY_JUMP] = true; //: Jump (1175.0/281.0)
            else action[Mario.KEY_JUMP] = false; //count > 3: Do_Not_Jump (544.0/249.0)
        }
        else{ //isBlock = false
            if(isEnemy){ //= true: Jump (1210.0/530.0)
              if(count <= 2) action[Mario.KEY_JUMP] = true; //: Jump (1175.0/281.0)
              else action[Mario.KEY_JUMP] = false; //count > 3: Do_Not_Jump (544.0/249.0)
            }
            else action[Mario.KEY_JUMP] = false; //isEnemy = false: Do_Not_Jump (3381.0/622.0)
        }

        //Multiclase
        if(action[Mario.KEY_JUMP]) multiclass = "Jump";
        else multiclass = "Do_Not_Jump";

        FileWriterData.writeOnFile(posMario, dataMatrix, envi, multiclass, count, marioState, tick);

        return action;
    }
}
