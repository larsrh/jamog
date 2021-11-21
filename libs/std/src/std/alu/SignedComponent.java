
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                           *
 * Copyright 2009 Lars Hupel, Torben Maack, Sylvester Tremmel                *
 *                                                                           *
 * This file is part of the Jamog Standard Library.                          *
 *                                                                           *
 * The Jamog Standard Library is free software: you can redistribute         *
 * it and/or modify it under the terms of the GNU General Public License     *
 * as published by the Free Software Foundation; version 3.                  *
 *                                                                           *
 * The Jamog Standard Library is distributed in the hope that it will        *
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty    *
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the           *
 * GNU General Public License for more details.                              *
 *                                                                           *
 * You should have received a copy of the GNU General Public License         *
 * along with the Jamog Standard Library. If not, see                        *
 * <http://www.gnu.org/licenses/>.                                           *
 *                                                                           *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package std.alu;

import core.exception.InstantiationException;
import std.gate.AND;
import std.gate.XOR;
import std.mux.BinaryMultiplexer;
import core.build.ComponentCollection;
import core.build.Composite;
import core.exception.DeserializingException;
import core.misc.serial.DeserializingStream;
import core.signal.Signal;
import java.io.IOException;

/**
 * @author lars
 */
public abstract class SignedComponent extends Composite
{
	public SignedComponent(ComponentCollection parent,String name)
	{
		super(parent,name);
	}

	protected SignedComponent(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		super(in);
	}

	protected final Signal unsignSignal(String name,Signal input,Signal signed,Signal output)
	{
		Signal negatedSignal = new Signal(input.size());

		Signal doUnsigning = new Signal(1);

		new AND(this,name+"-and")
			.setAll(new Signal(
				input.get(input.size()-1),    // input is negative?
				signed.get(0)),               // signed calculation?
			doUnsigning);                     // --> we have to negate
		new RipleNegator(this,name+"-neg")
			.setAll(input,negatedSignal);
		new BinaryMultiplexer(this,name+"-mux")
			.setAll(new Signal[]{
						input,        // no...
						negatedSignal // yes... negate
					},
					doUnsigning,
					output);

		return output;
	}

	protected final Signal restoreSignMSBXOR(String name,Signal x,Signal y,Signal input,Signal signed,Signal output)
	{
		return restoreSignXOR(name,x.get(x.size()-1),y.get(y.size()-1),input,signed,output);
	}

	protected final Signal restoreSignXOR(String name,Signal xSign,Signal ySign,Signal input,Signal signed,Signal output)
	{
		Signal resultSign = new Signal(1);
		new XOR(this,name+"-xor").setAll(new Signal(xSign,ySign),resultSign);
		return restoreSign(name,input,resultSign,signed,output);
	}

	protected final Signal restoreSign(String name,Signal input,Signal sign,Signal signed,Signal output)
	{
		Signal negative = new Signal(output.size());
		Signal restoreSign = new Signal(1);

		new AND(this,name+"-and")
			.setAll(new Signal(sign,signed),restoreSign);
		new RipleNegator(this,name+"-neg")
			.setAll(input,negative);
		new BinaryMultiplexer(this,name+"-mux")
			.setAll(new Signal[]{
						input,
						negative
					},
					restoreSign,
					output);

		return output;
	}
}
