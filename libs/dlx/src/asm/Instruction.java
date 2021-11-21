
package asm;

import asm.Token.Mnemonic;

public class Instruction
{
	public final Token.Mnemonic mnemonic;
	public final Token[] parameters;

	public Instruction(Mnemonic mnemonic, Token... parameters)
	{
		this.mnemonic = mnemonic;
		this.parameters = parameters;
	}
}
