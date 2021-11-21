
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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Extended type system which enables runtime-checking. Introduced
 * because Java 6 implemented generics via type erasure.
 * These classes, especially {@link ArrayType} and {@link GenericType}
 * are meant read-only, so {@link #canAssign(core.build.checking.Type)
 * canAssign} will produce covariant behaviour. Specialities like
 * contravariance, invariance, wildcards and so on are not implemented.
 * @author lars
 */
public abstract class Type {

	/**
	 * Checks whether a value with given {@link Class} has also this {@link Type}.
	 * Note that the following property does <em>not</em> hold:
	 * <pre>{@link #canAssign(java.lang.Object) canAssign(o)} == canAssign(o.getClass())}</pre>
	 * This function has a similar semantic as {@link Class#isAssignableFrom(java.lang.Class)}.
	 * @param cls the {@link Class} to be checked
	 * @return {@code false}, if it is definitely not acceptable, {@code true}
	 * otherwise.
	 */
	public abstract boolean canAssign(Class<?> cls);

	/**
	 * Checks whether a value with given {@link Type} has also this {@link Type}.
	 * @param type the {@link Type} to be checked
	 * @return {@code true} if it is acceptable, {@code false} otherwise
	 */
	public abstract boolean canAssign(Type type);

	/**
	 * Checks whether a value has this {@link Type}.
	 * @param obj the {@link Object} to be checked
	 * @return {@code true} if it is acceptable, {@code false} otherwise
	 */
	public abstract boolean canAssign(Object obj);

	public static Type getType(Class<?> cls) {
		assert cls != null;

		if (cls.isArray())
			return new ArrayType(getType(cls.getComponentType()));
		if (Collection.class.isAssignableFrom(cls))
			return new CollectionType(cls.asSubclass(Collection.class), getType(Object.class));
		if (Map.class.isAssignableFrom(cls))
			return new MapType(cls.asSubclass(Map.class), getType(Object.class), getType(Object.class));
		return new SimpleType(cls);
	}

	public static final Type BIG_DECIMAL = getType(BigDecimal.class);
	public static final Type BOOLEAN = getType(Boolean.class);
	public static final Type LIST = getType(List.class);
	public static final Type OBJECT = getType(Object.class);

}
