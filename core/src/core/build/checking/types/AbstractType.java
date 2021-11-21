
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

package core.build.checking.types;

/**
 *
 * @author lars
 */
public abstract class AbstractType extends Type {

	@Override
	public final boolean canAssign(Object obj) {
		return obj != null && canAssign(obj.getClass()) && checkObject(obj);
	}

	/**
	 * Performs a deeper inspection on an object to check whether it
	 * fits to the underlying type.
	 * @param obj object to check which holds the following properties:
	 * <ul>
	 *   <li><pre>obj != null</pre></li>
	 *   <li><pre>{@link #canAssign(java.lang.Object) canAssign}(obj.getClass())</pre></li>
	 * </ul>
	 * @return
	 */
	protected abstract boolean checkObject(Object obj);

}
