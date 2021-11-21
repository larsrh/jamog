
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
public final class SimpleType extends AbstractType
{
	private final Class<?> wrappedClass;

	public SimpleType(Class<?> cls)
	{
		this.wrappedClass = cls;
	}

	@Override public boolean canAssign(Class<?> cls)
	{
		return this.wrappedClass.isAssignableFrom(cls);
	}

	@Override public boolean canAssign(Type type)
	{
		if (type instanceof SimpleType)
			return wrappedClass.isAssignableFrom(((SimpleType)type).wrappedClass);
		else if (type instanceof NullableType)
			return false;
		else
			return wrappedClass.isAssignableFrom(Object.class);
	}

	public Class<?> getWrappedClass()
	{
		return this.wrappedClass;
	}

	@Override public String toString()
	{
		return wrappedClass.getName();
	}

	@Override protected boolean checkObject(Object obj)
	{
		return true;
	}

}
