
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

package core.misc.setable;

import java.util.Map;

/**
 * The {@code GroupSetable} interface extends {@link Setable} with a grouping
 * feature to create a setable hierarchy.
 *
 * @author torben
 */
public interface GroupSetable extends Setable
{
	/**
	 * @return A map of all group names and their {@link Setable}s
	 */
	public Map<String, ? extends Setable> getSetableGroups();

	/**
	 * Returns the {@link Setable} of the group with the specified group name.
	 *
	 * @param name The group name
	 * 
	 * @return The {@link Setable} of the group
	 */
	public Setable getSetableGroup(String name);
}
