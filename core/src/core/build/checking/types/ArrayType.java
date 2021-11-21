
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
public final class ArrayType extends AbstractType {

	private final Type elementType;

	public ArrayType(Type elementType) {
		this.elementType = elementType;
	}

	@Override
	public boolean canAssign(Class<?> cls) {
		return cls.isArray() && elementType.canAssign(cls.getComponentType());
	}

	@Override
	public boolean canAssign(Type type) {
		return type instanceof ArrayType && elementType.canAssign(((ArrayType)type).elementType);
	}

	@Override
	protected boolean checkObject(Object obj) {
		for (Object o : (Object[])obj)
			if (!elementType.canAssign(o))
				return false;

		return true;
	}

	public Type getElementType() {
		return elementType;
	}

	@Override
	public String toString() {
		return elementType.toString()+"[]";
	}
	
}
