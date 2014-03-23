/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.desktop.timeline;

import java.awt.Color;
import java.awt.Font;

/**
 *
 * @author Prostov Yury
 */
public class TickParameters {

    public enum TickType {

        DATE, DOUBLE, START_END
    };
    private final TickType type;
    private int width, height;
    private int fontSize = 12;
    private double fontFactor = 6.;
    private Font font = new Font("Helvetica", Font.PLAIN, fontSize);
        private Color[] realColors = new Color[]{new Color(0xB4B4B4), new Color(0x5A5A5A), new Color(0x1E1E1E)};
    private Color[] dateColors = new Color[]{new Color(0xB4B4B4), new Color(0x5A5A5A)};

    public TickParameters(TickType type) {
        this.type = type;
    }

    public TickType getType() {
        return type;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Font getFont() {
        return font;
    }

    public Color getDateColor(int level) {
        return dateColors[level];
    }

    public void setDateColor(int level, Color color) {
        dateColors[level] = color;
    }

    public Color getRealColor(int level) {
        return realColors[level];
    }

    public void setLevelColor(int level, Color color) {
        realColors[level] = color;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public double getFontFactor() {
        return fontFactor;
    }

    public void setFontFactor(double fontFactor) {
        this.fontFactor = fontFactor;
    }
}
