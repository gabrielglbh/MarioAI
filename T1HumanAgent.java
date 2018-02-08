/*
 * Copyright (c) 2012-2013, Moisés Martínez
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

/**
 * Created by PLG Group.
 * User: Moisés Martínez
 * Date: Jan 24, 2014
 * Package: ch.idsia.controllers.agents.controllers;
 */
public final class T1HumanAgent extends KeyAdapter implements Agent
{

    private boolean[] Action    = null;
    private String Name         = "HumanAgent";

    public T1HumanAgent()
    {
        this.reset();
    }

    @Override
    public String getName() { return Name; }

    @Override
    public void setName(String name) { Name = name; }

    @Override
    public boolean[] getAction() { return Action; }

    @Override
    public void integrateObservation(Environment environment)
    {

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
