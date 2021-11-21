
package asm;

import java.util.Map;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Scanner
{
	public static final void scan(String asm, List<Token> tokens, Map<String, Integer> labels, ErrorHandler eh) throws AssemblerException
	{
		int ins = 0;
		Matcher m = pattern.matcher(asm);

		int p = 0;
		while(m.find())
		{
			if(p != m.start())
				throw new AssemblerException("Unrecognized token", asm.substring(p, m.start()));
			p = m.end();

			String s;
			Token t = null;

			if((s = m.group(5)) != null)
			{
				String s2;

				if((s2 = m.group(1)) != null)
					t = new Token.Address(Integer.parseInt(s2, 16), Integer.parseInt(s, 10));
				else if((s2 = m.group(2)) != null)
					t = new Token.Address(Integer.parseInt(s2, 2), Integer.parseInt(s, 10));
				else if((s2 = m.group(3)) != null)
					t = new Token.Address(Integer.parseInt(s2, 8), Integer.parseInt(s, 10));
				else
					t = new Token.Address(Integer.parseInt(m.group(4), 10), Integer.parseInt(s, 10));
			}
			else if((s = m.group(6)) != null)
				t = new Token.Register(Integer.parseInt(s, 10));
			else if((s = m.group(7)) != null)
				t = new Token.Immediate(Integer.parseInt(s, 16));
			else if((s = m.group(8)) != null)
				t = new Token.Immediate(Integer.parseInt(s, 2));
			else if((s = m.group(9)) != null)
				t = new Token.Immediate(Integer.parseInt(s, 8));
			else if((s = m.group(10)) != null)
				t = new Token.Immediate(Integer.parseInt(s, 10));
			else if((s = m.group(11)) != null)
				labels.put(s, ins);
			else if((s = m.group(12)) != null)
			{
				t = new Token.Mnemonic(s);
				++ins;
			}
			else
				t = new Token.Reference(m.group(13));

			if(t != null)
			{
				tokens.add(t);
				eh.registerToken(t, m.start(), m.end());
			}
		}
	}

	private static final Pattern pattern;

	static
	{
		String identifier = "[A-Z_][A-Z0-9_]*";
		String nondec = "0x([0-9A-F]+)|0b([0-1]+)|0([0-7]+)";
		String register = "R(3[0-1]|[1-2][0-9]|[0-9])";

		String address = "(?:" + nondec + "|([1-9][0-9]*))\\(" + register + "\\)";
		String immediate = "(?:" + nondec + "|(-?[1-9][0-9]*))";
		String instruction = "(L(?:BU?|H(?:U|I)?)|LW|S(?:B|H|W)|(?:ADD|SUB)(?:U?I)?|(?:MULT|DIV)U?|(?:AND|OR|XOR|SLL|SRL|SRA|S(?:LT|GT|LE|GE|EQ|NE))I?|BEQZ|BNEZ|J(?:AL)?R?|F(?:ADD|SUB|MULT|DIV|S(?:LT|GT|LE|GE|EQ|NE|INF)|C(?:ITF|FTI)))";

		pattern = Pattern.compile("\\s*(?:" + address + "|" + register + "|" + immediate + "|(" + identifier + "):|" + instruction + "|(" + identifier + "))(?:\\s+|$)", Pattern.CASE_INSENSITIVE);
	}
}
