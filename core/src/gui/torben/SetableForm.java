
package gui.torben;

import core.misc.setable.Setable;
import core.signal.Bit;

/**
 * @author torben
 */
public class SetableForm
{
	public static final DataForm create(final Setable setable)
	{
		return new DataForm(true) {

			@Override protected int bitCount()
			{
				return setable.getSetableCount();
			}

			@Override protected Bit getBit(int i)
			{
				return setable.getSetableBit(i);
			}

			@Override protected void setBit(int i, Bit b)
			{
				setable.setSetableBit(i, b);
			}
		};
	}
}
