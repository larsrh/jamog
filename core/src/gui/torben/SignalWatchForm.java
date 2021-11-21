
package gui.torben;

import core.signal.Bit;
import core.signal.Signal;
import javax.swing.JPanel;

public class SignalWatchForm extends JPanel
{
	public static final DataForm create(final Signal signal)
	{
		return new DataForm(false) {

			@Override protected int bitCount()
			{
				return signal.size();
			}

			@Override protected Bit getBit(int i)
			{
				return signal.getBit(i);
			}

			@Override protected void setBit(int i, Bit b)
			{
			}
		};
	}
}
