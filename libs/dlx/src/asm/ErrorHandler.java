
package asm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ErrorHandler
{
	public ErrorHandler(String source)
	{
		this.source = source;

		reps = new HashMap<Integer, Range>();
		tokens = new HashMap<Token, Range>();
		warnings = new ArrayList<String[]>();
	}

	public void registerReplace(int start_old, int end_old, int start_new, int end_new)
	{
		for(int i = start_new; i < end_new; ++i)
			reps.put(i, new Range(start_old, end_old));
	}

	public void registerToken(Token t, int start, int end)
	{
		tokens.put(t, new Range(start, end));
	}

	public String forScanner(int start, int end)
	{
		Set<Range> ranges = new HashSet<Range>();

		for(int i = start; i < end; ++i)
			ranges.add(reps.get(i));

		StringBuilder sb = new StringBuilder();
		for(Range r : ranges)
			if(r.start <= start && r.end >= end)
				sb.append(getRange(start, end));
			else
				sb.append(getRange(r.start, r.end));

		return sb.toString();
	}

	public String forParser(Token t)
	{
		Range r = tokens.get(t);
		return forScanner(r.start, r.end);
	}

	public void addWarning(String reason, String place)
	{
		warnings.add(new String[] {reason, place});
	}

	private static final class Range
	{
		final int start;
		final int end;

		Range(int start, int end)
		{
			this.start = start;
			this.end = end;
		}

		@Override public boolean equals(Object obj)
		{
			if(obj == null || !(obj instanceof Range))
				return false;
			Range r = (Range)obj;
			return start == r.start && end == r.end;
		}

		@Override public int hashCode()
		{
			return start ^ end;
		}
	}

	private final String source;

	private final Map<Integer, Range> reps;
	private final Map<Token, Range> tokens;
	public final List<String[]> warnings;

	private String getRange(int start, int end)
	{
		StringBuilder sb = new StringBuilder("\tat line ");

		int line = 1;
		for(int i = 0; i < source.length() && i < start; ++i)
			if(source.charAt(i) == '\n')
				++line;

		int so = start >= 5 ? 5 : start;
		int eo = end <= source.length() - 5 ? 5 : source.length() - end;

		sb.append(line).append(":\n\t\t ");
		sb.append(source.substring(start - so, end + eo));
		sb.append(" \n\t\t");
		for(int i = 0; i < so; ++i)
			sb.append(" ");
		sb.append(">");
		for(int i = start; i < end; ++i)
			sb.append("^");
		sb.append("<");
		for(int i = 0; i < eo; ++i)
			sb.append(" ");
		sb.append("\n");

		return sb.toString();
	}
}
