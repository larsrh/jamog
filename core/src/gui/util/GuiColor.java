
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

import java.awt.color.ColorSpace;

/**
 *
 * @author sylvester
 */
public class GuiColor extends java.awt.Color {
    /**
     * Creates an opaque sRGB color with the specified red, green,
     * and blue values in the range (0 - 255).
     * The actual color used in rendering depends
     * on finding the best match given the color space
     * available for a given output device.
     * Alpha is defaulted to 255.
     *
     * @throws IllegalArgumentException if <code>r</code>, <code>g</code>
     *        or <code>b</code> are outside of the range
     *        0 to 255, inclusive
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     * @see #getRed
     * @see #getGreen
     * @see #getBlue
     * @see #getRGB
     */
    public GuiColor(int r, int g, int b) {
        super(r,g,b);
    }

    /**
     * Creates an sRGB color with the specified red, green, blue, and alpha
     * values in the range (0 - 255).
     *
     * @throws IllegalArgumentException if <code>r</code>, <code>g</code>,
     *        <code>b</code> or <code>a</code> are outside of the range
     *        0 to 255, inclusive
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     * @param a the alpha component
     * @see #getRed
     * @see #getGreen
     * @see #getBlue
     * @see #getAlpha
     * @see #getRGB
     */
    public GuiColor(int r, int g, int b, int a) {
        super(r,g,b,a);
    }

    /**
     * Creates an opaque sRGB color with the specified combined RGB value
     * consisting of the red component in bits 16-23, the green component
     * in bits 8-15, and the blue component in bits 0-7.  The actual color
     * used in rendering depends on finding the best match given the
     * color space available for a particular output device.  Alpha is
     * defaulted to 255.
     *
     * @param rgb the combined RGB components
     * @see java.awt.image.ColorModel#getRGBdefault
     * @see #getRed
     * @see #getGreen
     * @see #getBlue
     * @see #getRGB
     */
    public GuiColor(int rgb) {
        super(rgb);
    }

    /**
     * Creates an sRGB color with the specified combined RGBA value consisting
     * of the alpha component in bits 24-31, the red component in bits 16-23,
     * the green component in bits 8-15, and the blue component in bits 0-7.
     * If the <code>hasalpha</code> argument is <code>false</code>, alpha
     * is defaulted to 255.
     *
     * @param rgba the combined RGBA components
     * @param hasalpha <code>true</code> if the alpha bits are valid;
     *        <code>false</code> otherwise
     * @see java.awt.image.ColorModel#getRGBdefault
     * @see #getRed
     * @see #getGreen
     * @see #getBlue
     * @see #getAlpha
     * @see #getRGB
     */
    public GuiColor(int rgba, boolean hasalpha) {
        super(rgba, hasalpha);
    }

    /**
     * Creates an opaque sRGB color with the specified red, green, and blue
     * values in the range (0.0 - 1.0).  Alpha is defaulted to 1.0.  The
     * actual color used in rendering depends on finding the best
     * match given the color space available for a particular output
     * device.
     *
     * @throws IllegalArgumentException if <code>r</code>, <code>g</code>
     *        or <code>b</code> are outside of the range
     *        0.0 to 1.0, inclusive
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     * @see #getRed
     * @see #getGreen
     * @see #getBlue
     * @see #getRGB
     */
    public GuiColor(float r, float g, float b) {
        super(r,g,b);
    }

    /**
     * Creates an sRGB color with the specified red, green, blue, and
     * alpha values in the range (0.0 - 1.0).  The actual color
     * used in rendering depends on finding the best match given the
     * color space available for a particular output device.
     * @throws IllegalArgumentException if <code>r</code>, <code>g</code>
     *        <code>b</code> or <code>a</code> are outside of the range
     *        0.0 to 1.0, inclusive
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     * @param a the alpha component
     * @see #getRed
     * @see #getGreen
     * @see #getBlue
     * @see #getAlpha
     * @see #getRGB
     */
    public GuiColor(float r, float g, float b, float a) {
        super(r,g,b,a);
    }

    /**
     * Creates a color in the specified <code>ColorSpace</code>
     * with the color components specified in the <code>float</code>
     * array and the specified alpha.  The number of components is
     * determined by the type of the <code>ColorSpace</code>.  For
     * example, RGB requires 3 components, but CMYK requires 4
     * components.
     * @param cspace the <code>ColorSpace</code> to be used to
     *			interpret the components
     * @param components an arbitrary number of color components
     *                      that is compatible with the <code>ColorSpace</code>
     * @param alpha alpha value
     * @throws IllegalArgumentException if any of the values in the
     *         <code>components</code> array or <code>alpha</code> is
     *         outside of the range 0.0 to 1.0
     * @see #getComponents
     * @see #getColorComponents
     */
    public GuiColor(ColorSpace cspace, float components[], float alpha) {
        super(cspace, components, alpha);
	}

	/**
	 * Creates a color copying the given {@link java.awt.GuiColor} object.
	 * 
	 * @param color
	 */
	public GuiColor(java.awt.Color color) {
		this(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}

	@Override
	public GuiColor brighter() {
		java.awt.Color newColor = super.brighter();
		return new GuiColor(newColor.getRGB());
	}

	@Override
	public GuiColor darker() {
		java.awt.Color newColor = super.darker();
		return new GuiColor(newColor.getRGB());
	}

	public GuiColor setAlpha(int alpha) {
		if (alpha > 255)
			alpha = 255;
		if (alpha < 0)
			alpha = 0;
		return new GuiColor((this.getRGB() & 16777215/*2^24 - 1*/) | (alpha * 16777216/*2^24*/), true);
	}

	public GuiColor scaleAlpha(float factor) {
		return this.setAlpha((int) (this.getAlpha() * factor));
	}

	public GuiColor scaleAlpha(double factor) {
		return this.scaleAlpha((float) factor);
	}

	public GuiColor toGrey() {
		float[] hsb = GuiColor.RGBtoHSB(this.getRed(), this.getGreen(), this.getBlue(), null);
		GuiColor newColor = new GuiColor(GuiColor.HSBtoRGB(hsb[0], 0, hsb[2]));
		newColor.setAlpha(this.getAlpha());
		return newColor;
	}
}
