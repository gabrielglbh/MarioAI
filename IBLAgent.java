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
import ch.idsia.agents.controllers.P2FileWriterData;
import ch.idsia.agents.controllers.Instancia;
import ch.idsia.tools.EvaluationInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;

public class IBLAgent extends BasicMarioAIAgent implements Agent {

    private Random R = null;
    /**
     * Atributos para almacenar la informaciÃ³n del entorno de Mario
     * y poder usarla para determinar su comportamiento
     * (para que no vaya como pollo sin cabeza)
     */
    int[] dataMatrix = new int[20]; // Matriz de informacion de la partida en cada tick
    byte[][] envi = new byte[19][19]; // Matriz del entorno de Mario (el grid)
    float[] posMario;
    int[] marioState;

    int jumpButtonPressed = -1;
    int coinsInScreen = 0;
    int blocksInScreen = 0;
    int enemiesInScreen = 0;

    int tick;
    int count = 0;
    int[] sectionAttrs = new int[5];

    Instancia[][] baseConoc;

    public IBLAgent() {
        super("IBLAgent");
        reset();
        tick = 0;
        try{
          //baseConoc = P2FileWriterData.leerBaseConoc("baseConocimiento.csv");
          P2FileWriterData.fich[0] = new FileWriter("ejemplos.csv",true);
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

        coinsInScreen = 0;
        enemiesInScreen = 0;
        blocksInScreen = 0;

        // INFORMACION DEL ENTORNO
        // Devuelve un array de 19x19 donde Mario ocupa la posicion 9,9 con la union de los dos arrays
        // anteriores, es decir, devuelve en un mismo array la informacion de los elementos de la
        // escena y los enemigos.
        byte [][] env;
        env = environment.getMergedObservationZZ(1, 1);
        for (int mx = 0; mx < env.length; mx++) {
            for (int my = 0; my < env[mx].length; my++) {
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
            }
        }

        ////////// Posicion de Mario utilizando las coordenadas del sistema
        //System.out.println("POSICION MARIO");
        posMario = environment.getMarioFloatPos();

        ///////// Posicion que ocupa Mario en el array anterior
        //System.out.println("\nPOSICION MARIO MATRIZ");
        int[] posMarioEgo;
        posMarioEgo = environment.getMarioEgoPos();
        for (int mx = 0; mx < posMarioEgo.length; mx++){
          dataMatrix[mx] = posMarioEgo[mx];
        } // dataMatrix: 2 posiciones ocupadas

        ///////// Estado de mario
        // marioStatus, marioMode,
        // ** isMarioOnGround (1 o 0), isMarioAbleToJump() (1 o 0),
        //    isMarioAbleToShoot (1 o 0),isMarioCarrying (1 o 0),
        // **
        // killsTotal, killsByFire,  killsByStomp, killsByShell, timeLeft
        //System.out.println("\nESTADO MARIO");
        marioState = new int[4];
        int[] marioState_temp = environment.getMarioState();

        marioState[0] = marioState_temp[2]; // isMarioOnGround (1 o 0)
        marioState[1] = marioState_temp[3]; // isMarioAbleToJump() (1 o 0)
        marioState[2] = marioState_temp[4]; // isMarioAbleToShoot (1 o 0)
        marioState[3] = marioState_temp[5]; // isMarioCarrying (1 o 0)

        //////// Mas informacion de evaluacion...
        // distancePassedCells, distancePassedPhys, flowersDevoured, killsByFire, killsByShell, killsByStomp, killsTotal, marioMode,
        // marioStatus(10), mushroomsDevoured, coinsGained (12), timeLeft, timeSpent, hiddenBlocksFound /14
        //System.out.println("\nINFORMACION DE EVALUACION");
        int[] infoEvaluacion;
        infoEvaluacion = environment.getEvaluationInfoAsInts();
        for (int mx = 0; mx < infoEvaluacion.length; mx++){
          dataMatrix[mx+posMarioEgo.length] = infoEvaluacion[mx];
        } // dataMatrix: 2+14 = 16 posiciones ocupadas

        // Informacion del refuerzo/puntuacion que ha obtenido Mario. Nos puede servir para determinar lo bien o mal que lo esta haciendo.
        // Por defecto este valor engloba: reward for coins, killed creatures, cleared dead-ends, bypassed gaps, hidden blocks found
        //System.out.println("\nREFUERZO");
        int reward = environment.getIntermediateReward();

        dataMatrix[16] = coinsInScreen;
        dataMatrix[17] = blocksInScreen;
        dataMatrix[18] = enemiesInScreen;
        dataMatrix[19] = reward;

        // Atributos de las secciones del entorno cercano de Mario
        int enemiesSectionA, obstacleSectionA, coinsSectionA, enemiesSectionB, coinsSectionB;
        int mz = 0;

        // SECCION A: NUMERO DE ENEMIGOS
        enemiesSectionA = 0;
        for(int ii = 6; ii < 10; ii++) for(int jj = 9; jj < 13; jj++){
          if(envi[ii][jj] == 80) enemiesSectionA++;
        }
        sectionAttrs[mz] = enemiesSectionA;
        mz++; //mz = 1

        // SECCION A: ALTURA DE OBSTACULO
        obstacleSectionA = 0;
        int count_altura;
        for(int jj = 9; jj < 13; jj++){
          count_altura = 0;
          if(envi[9][jj] == -24 | envi[9][jj] == -60 | envi[9][jj] == -85){
            count_altura++;
            if(envi[8][jj] == -24 | envi[8][jj] == -60 | envi[8][jj] == -85){
              count_altura++;
              if(envi[7][jj] == -24 | envi[7][jj] == -60 | envi[7][jj] == -85){
                count_altura++;
                if(envi[6][jj] == -24 | envi[6][jj] == -60 | envi[6][jj] == -85){
                  count_altura++;
                }
              }
            }
          }
          if (count_altura > obstacleSectionA) obstacleSectionA = count_altura;
        }
        sectionAttrs[mz] = obstacleSectionA;
        mz++; //mz = 2

        // SECCION A: NUMERO DE COINS
        coinsSectionA = 0;
        for(int ii = 6; ii < 10; ii++) for(int jj = 9; jj < 13; jj++){
          if(envi[ii][jj] == 2) coinsSectionA++;
        }
        sectionAttrs[mz] = coinsSectionA;
        mz++; //mz = 3

        // SECCION B (abajo derecha): NUMERO DE ENEMIGOS
        enemiesSectionB = 0;
        for(int ii = 10; ii < 13; ii++) for(int jj = 9; jj < 13; jj++){
          if(envi[ii][jj] == 80) enemiesSectionB++;
        }
        sectionAttrs[mz] = enemiesSectionB;
        mz++; //mz = 4

        // SECCION B: NUMERO DE COINS
        coinsSectionB = 0;
        for(int ii = 10; ii < 13; ii++) for(int jj = 9; jj < 13; jj++){
          if(envi[ii][jj] == 2) coinsSectionB++;
        }
        sectionAttrs[mz] = coinsSectionB;
        mz++; //mz = 5
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

    	////////////// FUNCION DE PERTENENCIA //////////////
    	int pertenencia = 0;
    	/* sectionAttrs:
    	 * enemiesSectionA, obstacleSectionA, coinsSectionA, enemiesSectionB, coinsSectionB;
    	 */
    	pertenencia = 100*marioState[0] + -4*(sectionAttrs[0] + sectionAttrs[1]) + 5*sectionAttrs[2]
    					- 2*sectionAttrs[3] + sectionAttrs[4];

    	int situ = -1;
    	if(pertenencia < 48){    // Mario en el aire
    		if(pertenencia < 0) situ = 0;
    		else situ = 1;
    	}
      else {                 // Mario en el suelo
    		if(pertenencia > 100) situ = 3;
    		else situ = 2;
    	}

    	////////////// FUNCION DE SIMILITUD //////////////
    	Instancia[] instSimilares = new Instancia[3];
      int ignacion = 0;
      int[] posInstSim = new int[200];
      int resultadoEneSA = 0;
      int resultadoObsSA = 0;
      int resultadoCoiSA = 0;
      int resultadoEneSB = 0;
      int resultadoCoiSB = 0;
      int similitudRes = -1;
      //int distance[] = new int[10];
      //int idx[] = new int[10];

      for(int ii = 0; ii < baseConoc[0].length; ii++){
     		resultadoEneSA = Math.abs(baseConoc[situ][ii].enemiesSectionA - sectionAttrs[0]);
        resultadoObsSA = Math.abs(baseConoc[situ][ii].obstacleSectionA - sectionAttrs[1]);
        resultadoCoiSA = Math.abs(baseConoc[situ][ii].coinsSectionA - sectionAttrs[2]);
        resultadoEneSB = Math.abs(baseConoc[situ][ii].enemiesSectionB - sectionAttrs[3]);
        resultadoCoiSB = Math.abs(baseConoc[situ][ii].coinsSectionB - sectionAttrs[4]);
        //Funcion de similitud
        similitudRes = resultadoEneSA + resultadoObsSA + resultadoCoiSA + resultadoEneSB + resultadoCoiSB;
        //posInstSim[ii] = similitudRes;
        if(similitudRes < 7 && ignacion < 3){
          instSimilares[ignacion] = baseConoc[situ][ii];
          ignacion++;
        }
      }

      float distance = instSimilares[0].instEvaluation;
      int idx = 0;
      for(int c = 1; c < 3; c++){
          if(instSimilares[c].instEvaluation > distance){
              idx = c;
              distance = instSimilares[c].instEvaluation;
          }
      }

      action[0] = instSimilares[idx].action_left;
      action[1] = instSimilares[idx].action_right;
      action[2] = instSimilares[idx].action_down;
      action[3] = instSimilares[idx].action_jump;
      action[4] = instSimilares[idx].action_speed;
      action[5] = instSimilares[idx].action_up;

      //STACKOVERFLOW FT. IGNACIO (*clap* meme *clap* review)
      /*distance[0] = posInstSim[0]; // igual mejor con {posInstSim[0], 0, 0, 0, 0, 0, 0, 0, 0, 0} para inicializar todo de una
      idx[0] = 0; // seguramente mejor con {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}

      for(int cc = 1; cc < posInstSim.length; cc++){
          for(int ii = 0; ii < distance.length; ii++){
              if(posInstSim[cc] < distance[ii]){
                  idx[ii] = cc;
                  distance[ii] = posInstSim[cc];
                  break;
              }
          }
      }

      for(int ii = 0; ii < distance.length; ii++)
      instSimilares[ii] = baseConoc[situ][idx[ii]];*/

        count++;
        // Si Mario esta en el suelo y puede saltar, el contador se reinicia a 1
        if(marioState[0] == 1 && marioState[1] == 1){
          count = 1;
        }

        P2FileWriterData.writeOnFile(envi, posMario, dataMatrix, marioState, count, sectionAttrs, action, tick);

        return action;
    }
}
