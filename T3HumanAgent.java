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

import ch.idsia.agents.controllers.FileWriterData;

/**
 * Created by PLG Group.
 * User: MoisÃ©s MartÃ­nez
 * Date: Jan 24, 2014
 * Package: ch.idsia.controllers.agents.controllers;
 */
public final class T3HumanAgent extends KeyAdapter implements Agent
{

    private boolean[] Action    = null;
    private String Name         = "HumanAgent";
    int[] dataMatrix = new int[20]; // Matriz de información de la partida en cada tick
    byte[][] envi = new byte[19][19];
    float[] posMario;

    int coinsInScreen = 0;
    int blocksInScreen = 0;
    int enemiesInScreen = 0;

    // NUEVO ATRIBUTO: hemos añadido tick (para escribir en el archivo la info de hace 5 ticks)
    int tick;

    public T3HumanAgent()
    {
        this.reset();
        tick = 0;
    }

    @Override
    public String getName() { return Name; }

    @Override
    public void setName(String name) { Name = name; }

    @Override
    public boolean[] getAction() {
    	//System.out.println("\nHola actuo cada tick :D");
      boolean[] aux_action = Action.clone();

      int offset = aux_action.length - 2 % aux_action.length;
      if (offset > 0) {
          boolean[] copy = aux_action.clone();
          for (int i = 0; i < aux_action.length; ++i) {
              int j = (i + offset) % aux_action.length;
              aux_action[i] = copy[j];
          }
      }
    	FileWriterData.writeOnFile(posMario, dataMatrix, envi, aux_action, tick);
    	return Action;
    }

    @Override
    public void integrateObservation(Environment environment)
    {
      coinsInScreen = 0;
      blocksInScreen = 0;
      enemiesInScreen = 0;

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
        }

        // Mas informacion de evaluacion...
        // distancePassedCells, distancePassedPhys, flowersDevoured, killsByFire, killsByShell, killsByStomp, killsTotal, marioMode,
        // marioStatus, mushroomsDevoured, coinsGained, timeLeft, timeSpent, hiddenBlocksFound
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

        //Mover distancePassedPhys a la ultima posicion de dataMatrix
        /*12 es el numero de posiciones a rotar la matriz de dataMatrix para que distancePassedPhys quede
          en últime posicion*/
        int offset = dataMatrix.length - 16 % dataMatrix.length;
        if (offset > 0) {
            int[] copy = dataMatrix.clone();
            for (int i = 0; i < dataMatrix.length; ++i) {
                int j = (i + offset) % dataMatrix.length;
                dataMatrix[i] = copy[j];
            }
        }

        tick++;
        //FileWriterData.writeOnFile(posMario, dataMatrix, envi, tick);
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
