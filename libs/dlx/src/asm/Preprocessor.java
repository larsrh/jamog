
package asm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Preprocessor
{
	public static final String STD_MAKROS =
		"#def <= sle " +
		"#def >= sge " +
		"#def < slt " +
		"#def > sgt " +
		"#def == seq " +
		"#def != sne " +
		"#def _CMP_ ($V1$ $TEST$ $V2$) {$TEST$ r30 $V1$ $V2$ add r0 r0 r0 add r0 r0 r0}" +
		"#def IF ($V1$ $TEST$ $V2$ $INS$) {_CMP_{$V1$}{$TEST$}{$V2$} beqz r30 __AUTO__[1] add r0 r0 r0 $INS$ __AUTO__[1]:}" +
		"#def DO_WHILE ($V1$ $TEST$ $V2$ $INS$) {__AUTO__[1]: $INS$ _CMP_{$V1$}{$TEST$}{$V2$} bnez __AUTO__[1] add r0 r0 r0}" +
		"#def WHILE ($V1$ $TEST$ $V2$ $INS$) {_CMP_{$V1$}{$TEST$}{$V2$} beqz r30 __AUTO__[2] __AUTO__[1]: $INS$ _CMP_{$V1$}{$TEST$}{$V2$} bnez __AUTO__[1] add r0 r0 r0 __AUTO__[2]:}" +
		"#def IF_ELSE ($V1$ $TEST$ $V2$ $INS$ $EINS$) {_CMP_{$V1$}{$TEST$}{$V2$} beqz r30 __AUTO__[1] add r0 r0 r0 $INS$ j __AUTO__[2] add r0 r0 r0 __AUTO__[1]: $EINS$ __AUTO__[2]:}"
	;

	public static String process(String source, ErrorHandler eh) throws AssemblerException
	{
		eh.registerReplace(0, source.length(), 0, source.length());

		ExtStringBuilder esb = new ExtStringBuilder();
		for(char c : source.toCharArray())
			esb.addAfter(c);

		Map<String, Makro> makros = new LinkedHashMap<String, Makro>();
		int autogen = 0;

		for(ExtStringBuilder.Entry e = esb.first; e != null;)
		{
			switch(e.c)
			{
			case '#':
				e = e.removeAfter();
				if(e == null)
					throw new AssemblerException("Unexpected end of source", "");

				StringBuilder instruction = new StringBuilder();
				e = e.removeIdentifier(instruction);
				if(e == null)
					throw new AssemblerException("Unexpected end of source", "");

				StringBuilder identifier = new StringBuilder();
				e = e.removeWhitespaces();
				if(e == null)
					throw new AssemblerException("Unexpected end of source", "");
				e = e.removeIdentifier(identifier);

				String ins = instruction.toString().toUpperCase();
				String id = identifier.toString();

				if(ins.equals("DEF"))
				{
					if(makros.containsKey(id))
						throw new AssemblerException("Makro " + id + " already defined", "");

					e = e.removeWhitespaces();
					if(e == null)
						throw new AssemblerException("Unexpected end of source", "");

					List<String> parameters = new ArrayList<String>();
					if(e.c == '(')
					{
						e = e.removeAfter();
						do
						{
							e = e.removeWhitespaces();
							if(e == null)
								throw new AssemblerException("Unexpected end of source", "");

							StringBuilder parameter = new StringBuilder();
							e = e.removeIdentifier(parameter);
							if(e == null)
								throw new AssemblerException("Unexpected end of source", "");

							parameters.add(parameter.toString());
						}
						while(e.c != ')');

						e = e.removeAfter();
						if(e == null)
							throw new AssemblerException("Unexpected end of source", "");

						e = e.removeWhitespaces();
						if(e == null)
							throw new AssemblerException("Unexpected end of source", "");
					}

					StringBuilder value = new StringBuilder();
					e = e.removeValue(value);

					makros.put(id, new Makro(parameters, value.length() >= 2 && value.charAt(0) == '{' ? value.substring(1, value.length() - 1) : value.toString()));
				}
				else if(ins.equals("UNDEF"))
				{
					if(!makros.containsKey(id))
						throw new AssemblerException("Makro " + id + " not defined", "");
					makros.remove(id);
				}
				else if(ins.equals("IFDEF"))
				{
					e = e.removeWhitespaces();
					if(e == null)
						throw new AssemblerException("Unexpected end of source", "");

					e = makros.containsKey(id) ? e.unpackValue() : e.removeValue(new StringBuilder());
				}
				else if(ins.equals("IFNDEF"))
				{
					e = e.removeWhitespaces();
					if(e == null)
						throw new AssemblerException("Unexpected end of source", "");

					e = makros.containsKey(id) ? e.removeValue(new StringBuilder()) : e.unpackValue();
				}
				else
					throw new AssemblerException("Unrecogniced preprocessor instruction", "");
				
				break;
			case '/':
				if(e.next == null)
					throw new AssemblerException("Unexpected end of source", "");
				switch(e.next.c)
				{
				case '/':
					for(e.addBefore(' '), e = e.removeAfter().removeAfter(); e != null; e = e.removeAfter())
						if(e.c == '\n')
							break;
					break;
				case '*':
					int comment = 1;
					outer: for(e.addBefore(' '), e = e.removeAfter().removeAfter(); e != null;)
					{
						if(e.next == null)
							throw new AssemblerException("Unexpected end of source", "");
						char c = e.c;
						e = e.removeAfter();
						switch(c)
						{
						case '*':
							if(e.c == '/')
								--comment;
							e = e.removeAfter();
							if(comment == 0)
								break outer;
							break;
						case '/':
							if(e.c == '*')
								++comment;
							e = e.removeAfter();
						}
					}
					break;
				}
				break;
			default:
				for(Map.Entry<String, Makro> me : makros.entrySet())
				{
					String name = me.getKey();

					if(e.startsWith(name))
					{
						Makro m = me.getValue();

						for(int i = 0; i < name.length(); ++i)
							e = e.removeAfter();

						char[][] reps = new char[m.params][];
						int i = 0;
						for(; e != null && i < reps.length; ++i)
						{
							StringBuilder value = new StringBuilder();
							e = e.removeValue(value);
							reps[i] = (value.length() >= 2 && value.charAt(0) == '{' ? value.substring(1, value.length() - 1) : value.toString()).toCharArray();
						}
						for(; i < reps.length; ++i)
							reps[i] = new char[0];

						char[][] auto = new char[m.autoparams][];
						for(i = 0; i < auto.length; ++i)
							auto[i] = ("__AUTOGEN__" + autogen++).toCharArray();

						for(e = e.prev, i = m.values[m.reps.length].length - 1; i >= 0; --i)
							e.addAfter(m.values[m.reps.length][i]);
						for(i = m.reps.length - 1; i >= 0; --i)
						{
							int r = m.reps[i];
							char[] rep = r > 0 ? reps[r - 1] : auto[-r - 1];
							for(int j = rep.length - 1; j >= 0; --j)
								e.addAfter(rep[j]);
							for(int j = m.values[i].length - 1; j >= 0; --j)
								e.addAfter(m.values[i][j]);
						}
					}
				}
				e = e.next;
			}
		}

		StringBuilder sb = new StringBuilder();
		for(ExtStringBuilder.Entry e = esb.first; e != null; e = e.next)
			sb.append(e.c);

		return sb.toString();
	}

	private static final class ExtStringBuilder
	{
		final class Entry
		{
			Entry prev;
			Entry next;
			char c;

			Entry(Entry prev, Entry next, char c)
			{
				this.prev = prev;
				this.next = next;
				this.c = c;
			}

			void addBefore(char n)
			{
				Entry e = new Entry(prev, this, n);
				if(prev != null)
					prev.next = e;
				else
					first = e;
				prev = e;
			}

			void addAfter(char n)
			{
				Entry e = new Entry(this, next, n);
				if(next != null)
					next.prev = e;
				else
					last = e;
				next = e;
			}

			Entry removeBefore()
			{
				if(next != null)
					next.prev = prev;
				else
					last = prev;
				if(prev != null)
					prev.next = next;
				else
					first = next;
				return prev;
			}

			Entry removeAfter()
			{
				if(next != null)
					next.prev = prev;
				else
					last = prev;
				if(prev != null)
					prev.next = next;
				else
					first = next;
				return next;
			}

			Entry removeWhitespaces()
			{
				Entry e = this;

				do
					if(!Character.isWhitespace(e.c))
						break;
				while((e = e.removeAfter()) != null);

				return e;
			}

			Entry removeIdentifier(StringBuilder extract)
			{
				if(Character.isWhitespace(c) || c == '{' || c == '}' || c == '(' || c == ')')
					return this;

				Entry e = removeAfter();

				for(extract.append(c); e != null; e = e.removeAfter())
					if(!Character.isWhitespace(e.c) && e.c != '{' && e.c != '}' && e.c != '(' && e.c != ')')
						extract.append(e.c);
					else
						break;

				return e;
			}

			Entry removeValue(StringBuilder extract)
			{
				Entry e = removeWhitespaces();
				if(e == null)
					return null;

				if(e.c != '{')
					return e.removeIdentifier(extract);

				e = e.removeAfter();
				int open = 1;

				for(extract.append('{'); e != null; extract.append(e.c), e = e.removeAfter())
					if(e.c == '}' && --open == 0)
					{
						extract.append('}');
						e = e.removeAfter();
						break;
					}
					else if(e.c == '{')
						++open;

				return e;
			}

			Entry unpackValue()
			{
				if(c != '{')
					return this;

				int open = 1;

				for(Entry e = next; e != null; e = e.next)
					if(e.c == '{')
						++open;
					else if(e.c == '}' && --open == 0)
					{
						e.removeAfter();
						break;
					}

				return removeAfter();
			}

			boolean startsWith(String s)
			{
				Entry e = this;
				int i = 0;

				do
					if(e.c != s.charAt(i))
						return false;
				while((e = e.next) != null && ++i < s.length());

				return true;
			}
		}

		Entry first;
		Entry last;

		ExtStringBuilder()
		{
			first = null;
			last = null;
		}

		void addBefore(char n)
		{
			if(first == null)
				first = last = new Entry(null, null, n);
			else
				first.addBefore(n);
		}

		void addAfter(char n)
		{
			if(last == null)
				first = last = new Entry(null, null, n);
			else
				last.addAfter(n);
		}
	}

	private static final class Makro
	{
		final int params;
		final int autoparams;
		final char[][] values;
		final int[] reps;

		Makro(List<String> parameters, String value)
		{
			params = parameters.size();

			StringBuilder sb = new StringBuilder("(?:__AUTO__\\[([a-zA-Z0-9_]+)\\]");
			if(params != 0)
				for(int i = 0; i < params; ++i)
					sb.append("|(").append(Pattern.quote(parameters.get(i))).append(")");

			Map<String, Integer> automap = new HashMap<String, Integer>();
			List<char[]> value_list = new ArrayList<char[]>();
			List<Integer> rep_list = new ArrayList<Integer>();
			int p = 0;
			Matcher m = Pattern.compile(sb.append(")").toString()).matcher(value);
			while(m.find())
			{
				value_list.add(value.substring(p, m.start()).toCharArray());
				String a = m.group(1);
				if(a != null)
				{
					Integer v = automap.get(a);
					if(v == null)
					{
						automap.put(a, automap.size() + 1);
						rep_list.add(-automap.size());
					}
					else
						rep_list.add(-v);
				}
				else
					for(int i = 2; i <= params + 1; ++i)
						if(m.group(i) != null)
						{
							rep_list.add(i - 1);
							break;
						}

				p = m.end();
			}
			value_list.add(value.substring(p).toCharArray());

			autoparams = automap.size();

			values = new char[value_list.size()][];
			for(int i = 0; i < values.length; ++i)
				values[i] = value_list.get(i);

			reps = new int[rep_list.size()];
			for(int i = 0; i < reps.length; ++i)
				reps[i] = rep_list.get(i);
		}
	}
}
