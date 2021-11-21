
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
public final class NullableType extends Type implements WrapperType {

	private final Type type;

	public NullableType(Type type) {
		assert !(type instanceof NullableType) : "argument "+type+" is already nullable";

		this.type = type;
	}

	@Override
	public Type getInnerType() {
		return type;
	}

	@Override
	public boolean canAssign(Class<?> cls) {
		return type.canAssign(cls);
	}

	@Override
	public boolean canAssign(Object obj) {
		return obj == null || type.canAssign(obj);
	}

	@Override
	public boolean canAssign(Type type) {
		if (type instanceof NullableType)
			return this.type.canAssign(((NullableType)type).type);
		else
			return this.type.canAssign(type);
	}

	@Override
	public final String toString() {
		return new StringBuilder("#nullable<").append(type).append(">").toString();
	}

}
