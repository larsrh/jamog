
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

package core.build;

import core.misc.serial.Serializable;
import java.util.Map;

/**
 *
 * @author lars
 */
public interface ComponentCollection extends Serializable
{

	/**
	 * Denotes the separator which will be inserted between {@link ComponentCollection parent's}
	 * name and {@link Component}'s name to determine its {@link Component#getName() full name}.
	 */
	public static final String NAME_SEPARATOR = ".";

	/**
	 * Returns the number of {@link Component}s stored in this collection.
	 * @return the total number of {@link Component}s
	 */
	public int size();

	/**
	 * Provides map-based access to all {@link Component}s in this collection.
	 * It is ensured that, for each map entry, {@code name.equals(component.getShortName())}
	 * is {@code true}.
	 * @return a map holding all {@link Component}s in this collection
	 */
	public Map<String,Component> getComponents();

	/**
	 * Adds a {@link Component} to this collection. Implementations have to
	 * ensure that no component is added to more than one collection, which
	 * can be checked via {@link Component#hasParent()}. Otherwise, arbitrary
	 * behaviour may be produced.
	 * @param component the {@link Component} to be added
	 * @return the given parameter (which is {@code component})
	 */
	public Component addComponent(Component component);

	/**
	 * Returns the full name of this collection
	 * @return full name, separated by {@link #NAME_SEPARATOR}
	 */
	public String getName();

}
