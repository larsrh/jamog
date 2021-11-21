
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

package gui.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author sylvester
 */
public class Utilities {

	private static BufferedImage defaultImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
	private static Map<String, BufferedImage> images = new HashMap<String, BufferedImage>();

	static {
		Graphics2D graphics = Utilities.defaultImage.createGraphics();
		graphics.setPaint(new GuiColor(255, 255, 255));
		graphics.fillRect(0, 0, 9, 9);
		graphics.setPaint(new GuiColor(255, 0, 0));
		graphics.drawRect(0, 0, 9, 9);
		graphics.drawLine(2, 2, 7, 7);
		graphics.drawLine(7, 2, 2, 7);
	}

	public static BufferedImage getImage(String name, boolean famfamfam) {
		BufferedImage image;
		String path = "/gui/icons/";
		if (famfamfam)
			path += "famfamfam/";
		path += name;
		image = Utilities.images.get(path);
		if (image == null)
			try {
				image = ImageIO.read(Utilities.class.getResource(path));
				Utilities.images.put(path, image);
			} catch (IOException ex) {
				// TODO: Use the not yet implemented error handling system
				image = Utilities.defaultImage;
			}
		return image;
	}

	public static ImageIcon getIcon(String name, boolean famfamfam) {
		return new ImageIcon(Utilities.getImage(name, famfamfam));
	}

	public static final ImageObserver imageObserver = new ImageObserver() {

		@Override
		public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
			// TODO: Display an errorm message if there where errors and return the correct
			// value as described in the JavaDoc of the ImageObserver.
			// TODO: Use the not yet implemented error handling system
			return true;
		}

	};

	public static <T> Collection<T> selectByString(List<T> availableObjects, Collection<T> selectedObjects, String selectionString) {
		for (String range : selectionString.split(",")) {
			boolean select = true;
			int from;
			int to;
			if (range.startsWith("s") || range.startsWith("S"))
				range = range.substring(1);
			if (range.startsWith("d") || range.startsWith("D")) {
				select = false;
				range = range.substring(1);
			}
			if (range.contains("-")) {
				try {
					from = Integer.parseInt(range.substring(0, range.indexOf("-")));
					to = Integer.parseInt(range.substring(range.indexOf("-") + 1));
				} catch (NumberFormatException exc) {
					continue;
				}
				if (from < 1)
					from = 1;
				if (to > availableObjects.size())
					to = availableObjects.size();
				if (to <= from)
					break;
				if (select)
					for (int i = from - 1; i < to; i++)
						selectedObjects.add(availableObjects.get(i));
				else
					for (int i = from - 1; i < to; i++)
						selectedObjects.remove(availableObjects.get(i));
			} else {
				try {
					from = Integer.parseInt(range);
				} catch (NumberFormatException exc) {
					continue;
				}
				if (from < 1 || from > availableObjects.size())
					break;
				if (select)
					selectedObjects.add(availableObjects.get(from - 1));
				else
					selectedObjects.remove(availableObjects.get(from - 1));
			}
		}
		return selectedObjects;
	}

}
