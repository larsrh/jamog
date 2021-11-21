
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

import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.exception.SerializingException;
import core.misc.serial.DeserializingStream;
import core.misc.serial.SerializingStream;
import java.io.IOException;

/**
 * Simple bean class for handling named {@link Environment}s.
 * @author lars
 */
public final class Machine extends Environment {

	private String name;
	private String comment = "";

	public Machine(String name) {
		super();
		this.name = name;
	}

	public Machine(String name, String comment) {
		super();
		this.name = name;
		this.comment = comment;
	}

	private Machine(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);

		name = in.readString();
		comment = in.readString();
	}

	@Override
	public final String getName() {
		return this.name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final String getComment() {
		return this.comment;
	}

	public final void setComment(String comment) {
		this.comment = comment;
	}

	@Override public final void serialize(SerializingStream out) throws IOException, SerializingException
	{
		super.serialize(out);

		out.writeString(name);
		out.writeString(comment);
	}
}
