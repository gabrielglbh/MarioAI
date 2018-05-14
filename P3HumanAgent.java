/*
 * Copyright (c) 2012-2013, MoisÃ©s MartÃ­nez
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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileWriter;

import ch.idsia.agents.controllers.P3FileWriterData;

/**
 * Created by PLG Group.
 * User: MoisÃ©s MartÃ­nez
 * Date: Jan 24, 2014
 * Package: ch.idsia.controllers.agents.controllers;
 */
public final class P3HumanAgent extends KeyAdapter implements Agent {

    private boolean[] Action    = null;
    private String Name         = "HumanAgent";

    int[] dataMatrix = new int[20]; // Matriz de información de la partida en cada tick
    byte[][] envi = new byte[19][19];
    float[] posMario;
    int[] marioState;

    int coinsInScreen = 0;
    int blocksInScreen = 0;
    int enemiesInScreen = 0;

    int tick;
    int count = 0;
    int[] sectionAttrs = new int[10];

    Instancia[][] baseConoc;

    public P3HumanAgent()
    {
        this.reset();
        tick = 0;
        try{
        	//baseConoc = P2FileWriterData.leerBaseConoc("/baseConocimiento.csv");
					P3FileWriterData.fich = new FileWriter("P3human.csv",true);
        }
        catch(Exception e){
          e.printStackTrace(System.out);
        }
    }

    @Override
    public String getName() { return Name; }

    @Override
    public void setName(String name) { Name = name; }

    @Override
    public boolean[] getAction() {

      count++;

      if(marioState[0] == 1 && marioState[1] == 1){
        count = 1;
      }
   // Codificacion de la accion para la tabla Q
      /* Jump(0) - Right(1) - Left(2) - JumpRight(3) - JumpLeft(4) */
      boolean []name_actions = new boolean[5];
      int actionCode = -1;

      if(Action[3]) actionCode = 0;

      if(Action[1]) actionCode = 1;

      if(Action[0]) actionCode = 2;

      if(Action[3] && Action[1]) actionCode = 3;

      if(Action[3] && Action[0]) actionCode = 4;

      //P3FileWriterData.writeOnFile( dataMatrix, marioState, count, sectionAttrs, actionCode, tick);
      P3FileWriterData.createTupla( dataMatrix, marioState, sectionAttrs, actionCode, tick);
    	return Action;
    }

    @Override
    public void integrateObservation(Environment environment)
    {
          tick++;

          coinsInScreen = 0;
          blocksInScreen = 0;
          enemiesInScreen = 0;

        // INFORMACION DEL ENTORNO
    	// Devuelve un array de 19x19 donde Mario ocupa la posicion 9,9 con la union de los dos arrays
        // anteriores, es decir, devuelve en un mismo array la informacion de los elementos de la
        // escena y los enemigos.
    	envi = environment.getMergedObservationZZ(1, 1);
      for (int mx = 0; mx < envi.length; mx++) {
          //System.out.print(mx + ": [");
          for (int my = 0; my < envi[mx].length; my++) {
              //Coins on screen
              if(envi[mx][my] == 2) coinsInScreen++;
              //If blocks
              if(envi[mx][my] == -22 || envi[mx][my] == -60 || envi[mx][my] == -80 ||
                  envi[mx][my] == -62 || envi[mx][my] == -24 || envi[mx][my] == -85)
                  blocksInScreen++;
              //If enemies
              if(envi[mx][my] == 1 || envi[mx][my] == 80 || envi[mx][my] == 95 ||
                  envi[mx][my] == 82 || envi[mx][my] == 97 || envi[mx][my] == 81 ||
                  envi[mx][my] == 96 || envi[mx][my] == 84 || envi[mx][my] == 93 ||
                  envi[mx][my] == 99 || envi[mx][my] == 91 || envi[mx][my] == 13 ||
                  envi[mx][my] == -42)
                  enemiesInScreen++;
          //System.out.println("]");
          }
      }

    	// Posicion de Mario utilizando las coordenadas del sistema
    	posMario = environment.getMarioFloatPos();

    	// Posicion que ocupa Mario en el array anterior
    	int[] posMarioEgo;
        posMarioEgo = environment.getMarioEgoPos();
        for (int mx = 0; mx < posMarioEgo.length; mx++){
          dataMatrix[mx] = posMarioEgo[mx];
        } // dataMatrix: 2 posiciones ocupadas

        // Estado de mario
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

        // Mas informacion de evaluacion...
        // distancePassedCells, distancePassedPhys, flowersDevoured, killsByFire, killsByShell, killsByStomp, killsTotal, marioMode,
        // marioStatus, mushroomsDevoured, coinsGained, timeLeft, timeSpent, hiddenBlocksFound
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

    @Override
    public void giveIntermediateReward(float intermediateReward)
    {
    }

    @Override
    public void reset()
    {
        Action = new boolean[Environment.numberOfKeys];
    }

    @Override
    public void setObservationDetails(final int rfWidth, final int rfHeight, final int egoRow, final int egoCol)
    {
    }

    public boolean[] getAction(Environment observation)
    {
        return Action;
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        toggleKey(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        toggleKey(e.getKeyCode(), false);
    }


    private void toggleKey(int keyCode, boolean isPressed)
    {
        switch (keyCode)
        {
            case KeyEvent.VK_LEFT:
                Action[Mario.KEY_LEFT] = isPressed;
                break;
            case KeyEvent.VK_RIGHT:
                Action[Mario.KEY_RIGHT] = isPressed;
                break;
            case KeyEvent.VK_DOWN:
                Action[Mario.KEY_DOWN] = isPressed;
                break;
            case KeyEvent.VK_UP:
                Action[Mario.KEY_UP] = isPressed;
                break;
            case KeyEvent.VK_S:
                Action[Mario.KEY_JUMP] = isPressed;
                break;
            case KeyEvent.VK_A:
                Action[Mario.KEY_SPEED] = isPressed;
                break;
        }

    }

}
