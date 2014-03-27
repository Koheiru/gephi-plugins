/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.desktop.timeline;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import org.gephi.dynamic.api.DynamicModel.TimeFormat;
import ru.kohei.timeline.api.TimelineModel;
import org.joda.time.Interval;

/**
 *
 * @author Prostov Yury
 */
public class TickGraph {

    private double min;
    private double max;
    private TickParameters parameters;
    private BufferedImage image;

    public BufferedImage getImage(TimelineModel model, int width, int height) {
        org.gephi.data.attributes.type.Interval bounds = (model.hasCustomBounds()) ? (model.getCustomBounds()) : (model.getGlobalBounds());
        double newMin = bounds.getLow();
        double newMax = bounds.getHigh();
        
        //TickParameters.TickType timeFormat = model.getTimeFormat().equals(TimeFormat.DOUBLE) ? TickParameters.TickType.DOUBLE : TickParameters.TickType.DATE;
        TickParameters.TickType timeFormat = TickParameters.TickType.DOUBLE;
        if (parameters == null || newMax != max || newMin != min || parameters.getWidth() != width || parameters.getHeight() != height || !parameters.getType().equals(timeFormat)) {
            min = newMin;
            max = newMax;
            parameters = new TickParameters(timeFormat);
            parameters.setWidth(width);
            parameters.setHeight(height);
            image = draw();
        }
        return image;
    }

    private BufferedImage draw() {

        final BufferedImage image = new BufferedImage(parameters.getWidth(), parameters.getHeight(), BufferedImage.TYPE_INT_ARGB);

        final Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (parameters.getType().equals(TickParameters.TickType.DATE)) {
            drawDate(g);
        } else {
            drawReal(g);
        }

        return image;
    }

    private void drawDate(Graphics2D g) {
        int width = parameters.getWidth();
        int height = parameters.getHeight();

        //Font
        int fontSize = Math.min(parameters.getFontSize(), (int) (height / 4.));
        fontSize = fontSize > parameters.getFontSize() / 4 && fontSize <= parameters.getFontSize() / 2 ? parameters.getFontSize() / 2 : fontSize;
        FontMetrics smallMetrics = null;
        Font smallFont = parameters.getFont();
        Font bigFont;
        FontMetrics bigMetrics;
        if (smallFont != null && fontSize > parameters.getFontSize() / 4) {
            smallFont = smallFont.deriveFont(Font.PLAIN, fontSize);
            smallMetrics = g.getFontMetrics(smallFont);
            bigFont = smallFont.deriveFont(Font.PLAIN, (int) (fontSize * 2.5));
            bigMetrics = g.getFontMetrics(bigFont);
        } else {
            smallFont = null;
            bigFont = null;
        }

        DateTick dateTick = DateTick.create(min, max, width);


        int TOP_TICK = 0;
        int LOWER_TICK = 1;

        //Lower tick
        if (dateTick.getTypeCount() > 1) {
            g.setFont(smallFont);
            g.setColor(parameters.getDateColor(LOWER_TICK));
            Interval[] intervals = dateTick.getIntervals(LOWER_TICK);
            int labelWidth = smallMetrics != null ? smallMetrics.stringWidth("0000") : 0;
            for (Interval interval : intervals) {
                long ms = interval.getStartMillis();
                int x = dateTick.getTickPixelPosition(ms, width);
                if (x >= 0) {
                    g.setColor(parameters.getDateColor(LOWER_TICK));

                    //Height
                    int h = (int) (Math.min(40, (int) (height / 15.0)));

                    //Draw line
                    g.drawLine(x, 0, x, h);

                    //Label                       
                    if (smallFont != null && width / intervals.length > labelWidth) {
                        String label = dateTick.getTickValue(LOWER_TICK, interval.getStart());
                        int xLabel = x + 4;
                        g.setColor(parameters.getDateColor(1));
                        int y = (int) (fontSize * 1.2);

                        g.drawString(label, xLabel, y);
                    }
                }
            }
        }

        //Top tick
        if (dateTick.getTypeCount() > 0) {
            g.setFont(bigFont);
            g.setColor(parameters.getDateColor(TOP_TICK));
            Interval[] intervals = dateTick.getIntervals(TOP_TICK);
            for (Interval interval : intervals) {
                long ms = interval.getStartMillis();
                int x = dateTick.getTickPixelPosition(ms, width);
                if (x >= 0) {
                    g.setColor(parameters.getDateColor(TOP_TICK));

                    //Height
                    int h = height;

                    //Draw Line
                    g.drawLine(x, 0, x, h);

                    //Label
                    if (bigFont != null) {
                        String label = dateTick.getTickValue(TOP_TICK, interval.getStart());
                        int xLabel = x + 4;
                        g.setColor(parameters.getDateColor(TOP_TICK));
                        int y = (int) (fontSize * 4);

                        g.drawString(label, xLabel, y);
                    }
                } else if (x > ((dateTick.getTickPixelPosition(interval.getEndMillis(), width) - x) / -2)) {

                    if (bigFont != null) {
                        String label = dateTick.getTickValue(TOP_TICK, interval.getStart());
                        g.setColor(parameters.getDateColor(TOP_TICK));
                        int y = (int) (fontSize * 4);

                        g.drawString(label, 4, y);
                    }
                }
            }
        }
    }

    private void drawStartEnd(Graphics2D g) {
        int width = parameters.getWidth();
        int height = parameters.getHeight();

        //Font
        Font font = parameters.getFont();
        FontMetrics fontMetrics = null;
        int fontSize = Math.min(parameters.getFontSize(), (int) (height / 4.));
        fontSize = fontSize > parameters.getFontSize() / 4 && fontSize <= parameters.getFontSize() / 2 ? parameters.getFontSize() / 2 : fontSize;
        if (font != null && fontSize > parameters.getFontSize() / 4) {
            font = font.deriveFont(Font.PLAIN, fontSize);
            fontMetrics = g.getFontMetrics(font);
            g.setFont(font);
        } else {
            font = null;
        }

        if (font != null) {
            g.setColor(parameters.getRealColor(2));
            StartEndTick startEnd = StartEndTick.create(min, max);
            String labelStart = startEnd.getStartValue();
            String labelEnd = startEnd.getEndValue();
            int xEnd = width - (fontMetrics.stringWidth(labelEnd)) - (int) (fontSize * 0.3);
            g.drawString(labelStart, (int) (fontSize * 0.3), (int) (fontSize * 1.2));
            g.drawString(labelEnd, xEnd, (int) (fontSize * 1.2));
        }
    }

    private void drawReal(Graphics2D g) {
        int width = parameters.getWidth();
        int height = parameters.getHeight();

        //Font
        Font font = parameters.getFont();
        FontMetrics fontMetrics = null;
        double factor = parameters.getFontFactor();
        int fontSize = Math.min(parameters.getFontSize(), (int) (height / factor));
        fontSize = fontSize > parameters.getFontSize() / (factor * 2) && fontSize <= parameters.getFontSize() / (factor / 4) ? (int) (parameters.getFontSize() / (factor / 4)) : fontSize;
        if (font != null && fontSize > parameters.getFontSize() / (factor / 2)) {
            font = font.deriveFont(Font.PLAIN, fontSize);
            fontMetrics = g.getFontMetrics(font);
            g.setFont(font);
        } else {
            font = null;
        }

        //50
//        int fifty = (int) ((50 - min) * (width / (max - min)));
//        g.setColor(Color.BLUE);
//        g.drawLine(fifty, 0, fifty, height);

        RealTick graduation = RealTick.create(min, max, width);
        int numberTicks = graduation.getNumberTicks();
        for (int i = 0; i <= numberTicks; i++) {
            int x = graduation.getTickPixelPosition(i, width);
            int rank = graduation.getTickRank(i);
            int h = Math.min(40, (int) (height / 15.0));
            h = rank == 2 ? (int) (h + h) : rank == 1 ? (int) (h + h / 2.) : h;
            if (x > 0) {
                g.setColor(parameters.getRealColor(rank));
                g.drawLine(x, 0, x, h);
                if (font != null && rank >= 1) {
                    String label = graduation.getTickValue(i);
                    int xLabel = x - (fontMetrics.stringWidth(label) / 2);
                    g.drawString(label, xLabel, (int) (h + fontSize * 1.2));
                }
            }
        }
    }
}
