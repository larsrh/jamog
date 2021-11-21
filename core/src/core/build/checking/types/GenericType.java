
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

import java.util.Arrays;

/**
 *
 * @author lars
 */
public abstract class GenericType extends AbstractType implements WrapperType {

	private final Type genericParameters[];
	private final SimpleType realType;

	protected GenericType(SimpleType realType, Type... genericParameters) {
		this.realType = realType;
		this.genericParameters = genericParameters;
	}

	@Override
	public SimpleType getInnerType() {
		return realType;
	}

	protected final boolean checkValues(Object... objs) {
		assert objs.length == genericParameters.length;

		for (int i=0;i<objs.length;++i)
			if (!genericParameters[i].canAssign(objs[i]))
				return false;
		return true;
	}

	@Override
	public boolean canAssign(Type type) {
		if (getClass() != type.getClass())
			return false;

		GenericType gtype = (GenericType)type;
		if (!getInnerType().canAssign(gtype.getInnerType()) || genericParameters.length != gtype.genericParameters.length)
			return false;
		for (int i=0;i<genericParameters.length;++i)
			if (!genericParameters[i].canAssign(gtype.genericParameters[i]))
				return false;
		return true;
	}

	@Override
	public boolean canAssign(Class<?> cls) {
		// not generic-type safe here
		return getInnerType().canAssign(cls);
	}

	@Override
	public final String toString() {
		StringBuilder sb = new StringBuilder(getInnerType().toString())
			.append("<");
		for (int i=0;i<genericParameters.length;++i) {
			sb.append(genericParameters[i].toString());
			sb.append(i == genericParameters.length - 1 ? ">" : ",");
		}
		return sb.toString();
	}

	public final Type[] getGenericParameters() {
		return Arrays.copyOf(genericParameters, genericParameters.length);
	}

}
