
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
 * The {@code CalculatorListener} interface is used to get notified when the
 * {@link Calculator}s {@link Calculator#execute() execute()} method has
 * finished the actual calculation. If an instance of this interface is
 * registered to a {@link Calculator} as listener via the {@link
 * Calculator#addCalculateListener(core.monitor.CalculatorListener)
 * addCalculatorListener(CalculatorListener)} method and the {@link
 * Calculator}s {@link Calculator#execute() execute()} method has finished the
 * actual calculation, the {@link #calculateFinished(core.sim.Calculator)
 * calculateFinished(Calculator)} method is invoked.
 *
 * @see Calculator
 *
 * @author torben
 */
public interface CalculatorListener extends Serializable
{
	/**
	 * Is invoked from {@link Calculator}s to which the {@code
	 * CalculatorListener} listens if the {@link Calculator}s {@link
	 * Calculator#execute() execute()} method has finished the actual
	 * calculation.
	 *
	 * @param finished_calculator The {@link Calculator} which has finished
	 *                            calculation
	 */
	public void calculateFinished(Calculator finished_calculator);	
}
