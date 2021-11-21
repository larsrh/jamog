
package asm;

public class AssemblerException extends Exception
{
	public AssemblerException(String reason, String place)
	{
		this.reason = reason;
		this.place = place;
	}

	public final String reason;
	public final String place;
}
