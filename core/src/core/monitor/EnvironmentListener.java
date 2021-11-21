
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

import core.build.*;

/**
 *
 * @author lars
 */
public interface EnvironmentListener
{

	/**
	 * Notifies the listener how much new components have to be constructed
	 * @param delta count of new components
	 */
	void increaseTotalCount(long delta);

	/**
	 * Notifies the listener that the construction of some components is
	 * done. Components which raised an exception at some point will not
	 * be counted here.
	 * @param delta count of constructed components, normally this will be one
	 */
	void increaseConstructedCount(long delta);

	/**
	 * Notifies the listener that an exception has occured while constructing
	 * a component
	 * @param c the 'faulty' component
	 * @param ex the exception which has been thrown
	 * @return {@code true} if the environment should construct the other
	 * components or {@code false} if it should terminate the construction
	 * process
	 */
	boolean exceptionOccured(Component c, Exception ex);
}
