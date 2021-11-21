
package stdtest.gate;

public final class OR extends Gate
{
	@Override protected final Class<? extends std.gate.Gate> getComponent()
	{
		return std.gate.OR.class;
	}

	@Override protected final long expected(int i, int j)
	{
		long l = j & 1;
		for(int m = 1; m < i; ++m)
			l |= (j >>> m) & 1;

		return l;
	}
}
