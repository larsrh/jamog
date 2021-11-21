
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

package gui;

import static core.build.Component.Extension;
import gui.support.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.tree.*;

/**
 *
 * @author sylvester
 */
public final class ComponentTree extends JTree {
	private static final long serialVersionUID = -268638237487658863L;

	/**
	 * DO NOT REMOVE, NEEDED FOR GUI DESIGNER!
	 */
	public ComponentTree() {
		super();
	}

	public ComponentTree(TreeModel model) {
		super(model);
		this.setTransferHandler(new TransferHandler() {

			@Override
			public boolean canImport(TransferSupport support) {
				return false;
			}

			@Override
			public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
				return false;
			}

			@Override
			protected Transferable createTransferable(JComponent c) {
				ComponentTree tree = (ComponentTree) c;
				TreePath path = tree.getSelectionPath();
				if (path == null)
					return null;
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (selectedNode.getUserObject() instanceof Extension) {
					return new ExtensionTransferable((Extension) selectedNode.getUserObject());
				} else
					return null;
			}

			@Override
			public int getSourceActions(JComponent c) {
				return COPY;
			}
		});
	}
}
