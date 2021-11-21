
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

package gui.support;

import static core.build.Component.Extension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

public final class ExtensionTransferable implements Transferable
{
	public static final String EXTENSION_MIME_TYPE = DataFlavor.javaJVMLocalObjectMimeType + ";class=core.build.Component$Extension";
	private Extension extension;
	private DataFlavor flavor;

	public ExtensionTransferable(Extension extension)
	{
		super();
		flavor = null;
		try
		{
			flavor = new DataFlavor(EXTENSION_MIME_TYPE);
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
			System.err.println("Could not create DataFlavor for ExtensionTransferable. Drag \'n\' Drop wont work.");
			this.extension = null;
		}
		this.extension = extension;
	}

	@Override
	public Extension getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
	{
		if(!isDataFlavorSupported(flavor))
			throw new UnsupportedFlavorException(flavor);
		return this.extension;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors()
	{
		return new DataFlavor[]{this.flavor};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		if(this.flavor.equals(flavor))
			return true;
		else
			return false;
	}
}
