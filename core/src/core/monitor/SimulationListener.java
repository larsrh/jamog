
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                           *
 * Copyright 2009 Lars Hupel, Torben Maack, Sylvester Tremmel                *
 *                                                                           *
 * This file is part of Jamog.                                               *
 *                                                                           *
 * Jamog is free software: you can redistribute it and/or modify             *
 * it under the terms of the GNU General Public License as published by      *
 * the Free Software Foundation; version 3.                                  *
 *                                                                           *
 * Jamog is distributed in the hope that it will be useful,                  *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of            *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the              *
 * GNU General Public License for more details.                              *
 *                                                                           *
 * You should have received a copy of the GNU General Public License         *
 * along with Jamog. If not, see <http://www.gnu.org/licenses/>.             *
 *                                                                           *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package core.monitor;

import core.misc.serial.Serializable;
import core.sim.Calculator;

/**
 * The {@code SimulationListener} interface is used to get information about
 * the simulation process. If an instance of this interface is registered to a
 * {@link core.sim.Simulator Simulator} as listener via the {@link
 * core.sim.Simulator#addSimulationListener(core.monitor.SimulationListener)
 * addSimulationListener(SimulationListener)} method and a {@link Calculator}
 * in the {@link core.sim.Simulator Simulator} has been calculated, the {@link
 * #calculatorFinished(core.sim.Calculator) calculatorFinished(Calculator)}
 * method is invoked.
 *
 * @author torben
 */
public interface SimulationListener extends Serializable
{
	/**
	 * Is invoked from the {@link core.sim.Simulator Simulator} to which the
	 * {@code SimulationListener} listen if a {@link Calculator} in the
	 * {@link core.sim.Simulator Simulator} has been calculated.
	 *
	 * @param finished_calculator The {@link Calculator} which has been
	 *                            calculated
	 */
	public void calculatorFinished(Calculator finished_calculator);
}

