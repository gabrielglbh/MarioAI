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
import ch.idsia.agents.controllers.P3FileWriterData;
import ch.idsia.agents.controllers.Instancia;
import ch.idsia.tools.EvaluationInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;

public class P3BotAgent extends BasicMarioAIAgent implements Agent {

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
    int[] sectionAttrs = new int[10];

    Instancia[][] baseConoc;

    public P3BotAgent() {
        super("P3BotAgentEntrega");
        reset();
        tick = 0;
        try{
          //baseConoc = P2FileWriterData.leerBaseConoc("baseConocimiento.csv");
          P3FileWriterData.fich = new FileWriter("tuplas_P3.csv",true);
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

        marioState[0] = marioState_temp[2];
        marioState[1] = marioState_temp[3];
        marioState[2] = marioState_temp[4];
        marioState[3] = marioState_temp[5];

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
        int enemiesSectA, obstacleSectA, coinsSectA;
        int enemiesSectB, coinsSectB;
        int enemiesSectC, obstacleSectC, coinsSectC;
        int enemiesSectD, coinsSectD;
        int count_altura, mz = 0;

        // SECCION A: NUMERO DE ENEMIGOS, ALTURA DE OBSTACULO, NUMERO DE COINS
        enemiesSectA = 0;
        for(int ii = 6; ii < 10; ii++) for(int jj = 9; jj < 13; jj++){
          if(envi[ii][jj] == 80) enemiesSectA++;
        }
        sectionAttrs[mz] = enemiesSectA;
        mz++; //mz = 1

        obstacleSectA = 0;
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
          if (count_altura > obstacleSectA) obstacleSectA = count_altura;
        }
        sectionAttrs[mz] = obstacleSectA;
        mz++; //mz = 2

        coinsSectA = 0;
        for(int ii = 6; ii < 10; ii++) for(int jj = 9; jj < 13; jj++){
          if(envi[ii][jj] == 2) coinsSectA++;
        }
        sectionAttrs[mz] = coinsSectA;
        mz++; //mz = 3

        // SECCION B (abajo derecha): NUMERO DE ENEMIGOS, NUMERO DE COINS
        enemiesSectB = 0;
        for(int ii = 10; ii < 13; ii++) for(int jj = 9; jj < 13; jj++){
          if(envi[ii][jj] == 80) enemiesSectB++;
        }
        sectionAttrs[mz] = enemiesSectB;
        mz++; //mz = 4

        coinsSectB = 0;
        for(int ii = 10; ii < 13; ii++) for(int jj = 9; jj < 13; jj++){
          if(envi[ii][jj] == 2) coinsSectB++;
        }
        sectionAttrs[mz] = coinsSectB;
        mz++; //mz = 5
        
        // SECCION C: NUMERO DE ENEMIGOS, ALTURA DE OBSTACULO, NUMERO DE COINS
        enemiesSectC = 0;
        for(int ii = 6; ii < 10; ii++) for(int jj = 6; jj < 9; jj++){
          if(envi[ii][jj] == 80) enemiesSectC++;
        }
        sectionAttrs[mz] = enemiesSectC;
        mz++; //mz = 6

        obstacleSectC = 0;
	    count_altura = 0;
	    if(envi[9][8] == -24 | envi[9][8] == -60 | envi[9][8] == -85){
	        count_altura++;
	        if(envi[8][8] == -24 | envi[8][8] == -60 | envi[8][8] == -85){
	        	count_altura++;
	            if(envi[7][8] == -24 | envi[7][8] == -60 | envi[7][8] == -85){
	            	count_altura++;
	            	if(envi[6][8] == -24 | envi[6][8] == -60 | envi[6][8] == -85){
	            		count_altura++;
	            	}
	            }
	        }          
        }
	    obstacleSectC = count_altura;
	    
        sectionAttrs[mz] = obstacleSectC;
        mz++; //mz = 7

        coinsSectC = 0;
        for(int ii = 6; ii < 10; ii++) for(int jj = 6; jj < 9; jj++){
          if(envi[ii][jj] == 2) coinsSectC++;
        }
        sectionAttrs[mz] = coinsSectC;
        mz++; //mz = 8
        
        // SECCION D (abajo izquierda): NUMERO DE ENEMIGOS, NUMERO DE COINS
        enemiesSectD = 0;
        for(int ii = 10; ii < 13; ii++) for(int jj = 6; jj < 9; jj++){
          if(envi[ii][jj] == 80) enemiesSectD++;
        }
        sectionAttrs[mz] = enemiesSectD;
        mz++; //mz = 9

        coinsSectD = 0;
        for(int ii = 10; ii < 13; ii++) for(int jj = 6; jj < 9; jj++){
          if(envi[ii][jj] == 2) coinsSectD++;
        }
        sectionAttrs[mz] = coinsSectD;
        mz++; //mz = 10
        
    }

    public boolean[] getAction() {

    //	if(tick < 2) System.out.println("Tick inicial: " + tick);
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

        //        for (int i = 0; i < Environment.numberOfKeys; ++i) {
//                  boolean toggleParticularAction = R.nextBoolean();
//                  toggleParticularAction = (i == 0 && toggleParticularAction && R.nextBoolean()) ? R.nextBoolean() : toggleParticularAction;
//                  toggleParticularAction = (i == 1 || i > 3 && !toggleParticularAction) ? R.nextBoolean() : toggleParticularAction;
//                  toggleParticularAction = (i > 3 && !toggleParticularAction) ? R.nextBoolean() : toggleParticularAction;
//                  action[i] = toggleParticularAction;
//                }

        action[0] = false; // Nunca ir a la izquierda, de momento...
        action[1] = true; // Siempre adelante, valiente Mario

        for (int i = 3; i > 0; i--){
        	switch (envi[this.marioEgoRow][this.marioEgoCol + i]){
              case -80: //CaÃ±on
              case 95: //Goomba con alas
              case 82: //Koopa rojo
              case 97: //Koopa rojo con alas
              case 81: //Koopa verde
              case 96: //Koopa verde con alas
              case 84: //Bala
              case 93: //Enemigo puntiagudo
              case 99: //Enemigo puntiagudo con alas
              case 91: //Flor enemiga
              case 13: //CaparazÃ³n
              case -42: //Tipo de enemigo indefinido
              case 1:
                //Si te encuentras con cualquier cosa de las anteriores, salta normal
            		action[2] = false;
            		action[3] = true;
            		if(jumpButtonPressed < 0) jumpButtonPressed = tick+3;
            		break;
              case -22: //Ladrillo irrompible con interrogacion
              case -24: //Ladrillo
              case -60: //Obstaculo del que no se puede pasar
              case -62: //ObstÃ¡culo sobre el que se puede saltar y mantenerse encima
                //Si se encuentra con un ladrillo y el ladrillo es muy alto, salta más
                if( envi[this.marioEgoRow][this.marioEgoCol + i]   == -22 &&
                    envi[this.marioEgoRow-1][this.marioEgoCol + i] == -22 ||
                    envi[this.marioEgoRow][this.marioEgoCol + i]   == -22 &&
                    envi[this.marioEgoRow-1][this.marioEgoCol + i] == -22 &&
                    envi[this.marioEgoRow-2][this.marioEgoCol + i] == -22 ||
                    envi[this.marioEgoRow][this.marioEgoCol + i]   == -22 &&
                    envi[this.marioEgoRow-1][this.marioEgoCol + i] == -22 &&
                    envi[this.marioEgoRow-2][this.marioEgoCol + i] == -22 &&
                    envi[this.marioEgoRow-3][this.marioEgoCol + i] == -22 ||

                    envi[this.marioEgoRow][this.marioEgoCol + i]   == -24 &&
                    envi[this.marioEgoRow-1][this.marioEgoCol + i] == -24 ||
                    envi[this.marioEgoRow][this.marioEgoCol + i]   == -24 &&
                    envi[this.marioEgoRow-1][this.marioEgoCol + i] == -24 &&
                    envi[this.marioEgoRow-2][this.marioEgoCol + i] == -24 ||
                    envi[this.marioEgoRow][this.marioEgoCol + i]   == -24 &&
                    envi[this.marioEgoRow-1][this.marioEgoCol + i] == -24 &&
                    envi[this.marioEgoRow-2][this.marioEgoCol + i] == -24 &&
                    envi[this.marioEgoRow-3][this.marioEgoCol + i] == -24 ||

                    envi[this.marioEgoRow][this.marioEgoCol + i]   == -60 &&
                    envi[this.marioEgoRow-1][this.marioEgoCol + i] == -60 ||
                    envi[this.marioEgoRow][this.marioEgoCol + i]   == -60 &&
                    envi[this.marioEgoRow-1][this.marioEgoCol + i] == -60 &&
                    envi[this.marioEgoRow-2][this.marioEgoCol + i] == -60 ||
                    envi[this.marioEgoRow][this.marioEgoCol + i]   == -60 &&
                    envi[this.marioEgoRow-1][this.marioEgoCol + i] == -60 &&
                    envi[this.marioEgoRow-2][this.marioEgoCol + i] == -60 &&
                    envi[this.marioEgoRow-3][this.marioEgoCol + i] == -60 ||

                    envi[this.marioEgoRow][this.marioEgoCol + i]   == -62 &&
                    envi[this.marioEgoRow-1][this.marioEgoCol + i] == -62 ||
                    envi[this.marioEgoRow][this.marioEgoCol + i]   == -62 &&
                    envi[this.marioEgoRow-1][this.marioEgoCol + i] == -62 &&
                    envi[this.marioEgoRow-2][this.marioEgoCol + i] == -62 ||
                    envi[this.marioEgoRow][this.marioEgoCol + i]   == -62 &&
                    envi[this.marioEgoRow-1][this.marioEgoCol + i] == -62 &&
                    envi[this.marioEgoRow-2][this.marioEgoCol + i] == -62 &&
                    envi[this.marioEgoRow-3][this.marioEgoCol + i] == -62    ){

                  action[2] = false;
                  action[3] = true;

                  if(jumpButtonPressed < 0) jumpButtonPressed = tick +9;
                  break;
                }
                //Si Mario está atascado por donde solo tiny Mario puede pasar
                // else if(envi[this.marioEgoRow-1][this.marioEgoCol + 1] == -22 ||
                //       envi[this.marioEgoRow-1][this.marioEgoCol + 1] == -24 ||
                //       envi[this.marioEgoRow-1][this.marioEgoCol + 1] == -60 ||
                //       envi[this.marioEgoRow-1][this.marioEgoCol + 1] == -62){
                //     action[2] = false;
                //     action[3] = true;
                //     if(jumpButtonPressed < 0) jumpButtonPressed = tick+2;
                //     break;
                // }
                //si el ladrillo es bajito, salta lo mínimo
                else{
                  action[2] = false;
                  action[3] = true;

                  if(jumpButtonPressed < 0) jumpButtonPressed = tick +8;
                  break;
                }
              case 80: //Goomba
                //Si se ecnuentra un Goomba, saltar lo mínimo
                action[2] = false;
                action[3] = true;

                if(jumpButtonPressed < 0) jumpButtonPressed = tick +2;
                break;
              case -85: //Tuberia ocn flor o caÃ±on
                //Si se encuentra con un cañón y el cañón es muy alto, salta más
                if( envi[this.marioEgoRow][this.marioEgoCol + i]   == -85 &&
                    envi[this.marioEgoRow-1][this.marioEgoCol + i] == -85 ||
                    envi[this.marioEgoRow][this.marioEgoCol + i]   == -85 &&
                    envi[this.marioEgoRow-1][this.marioEgoCol + i] == -85 &&
                    envi[this.marioEgoRow-2][this.marioEgoCol + i] == -85 ||
                    envi[this.marioEgoRow][this.marioEgoCol + i]   == -85 &&
                    envi[this.marioEgoRow-1][this.marioEgoCol + i] == -85 &&
                    envi[this.marioEgoRow-2][this.marioEgoCol + i] == -85 &&
                    envi[this.marioEgoRow-3][this.marioEgoCol + i] == -85    ){

                  action[2] = false;
                  action[3] = true;

                  if(jumpButtonPressed < 0) jumpButtonPressed = tick +9;
                  break;
                }
                //si el cañón es bajito, salta lo mínimo
                else{
                  action[2] = false;
                  action[3] = true;

                  if(jumpButtonPressed < 0) jumpButtonPressed = tick +8;
                  break;
                }
            	default:
          }
        }

        if(tick == jumpButtonPressed) {
      	  action[3] = false;
      	  jumpButtonPressed = -1;
        }
        count++;

        // Si Mario est� en el suelo y puede saltar, el contador se reinicia a 1
        if(marioState[0] == 1 && marioState[1] == 1){
          count = 1;
        }
        
        // Codificacion de la accion para la tabla Q
        /* Jump(0) - Right(1) - Left(2) - JumpRight(3) - JumpLeft(4) - RunRight(5) - RunRightJump(6) */
        boolean []name_actions = new boolean[7];

        if(action[3]) name_actions[0] = true;
        else{name_actions[0] = false;}

        if(action[1]) name_actions[1] = true;
        else{name_actions[1] = false;}

        if(action[0]) name_actions[2] = true;
        else{name_actions[2] = false;}

        if(action[3] && action[1]) name_actions[3] = true;
        else{name_actions[3] = false;}

        if(action[3] && action[0]) name_actions[4] = true;
        else{name_actions[4] = false;}

        if(action[1] && action[4]) name_actions[5] = true;
        else{name_actions[5] = false;}

        if(action[3] && action[1] && action[4]) name_actions[6] = true;
        else{name_actions[6] = false;}

        //return name_actions;

        P3FileWriterData.writeOnFile(envi, posMario, dataMatrix, marioState, count, sectionAttrs, name_actions, tick);

        return action;
    }
}