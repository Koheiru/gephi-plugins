/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.desktop.timeline;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.Locale;
import javax.swing.JPanel;
import javax.swing.Timer;
import ru.kohei.timeline.api.TimelineController;
import ru.kohei.timeline.api.TimelineModel;
import ru.kohei.timeline.api.TimelineModelEvent;
import org.openide.util.Lookup;

/**
 *
 * @author Prostov Yury
 */
public class TimelineDrawer extends JPanel implements MouseListener, MouseMotionListener {

    //Consts
    private static Cursor CURSOR_DEFAULT = new Cursor(Cursor.DEFAULT_CURSOR);
    private static Cursor CURSOR_LEFT_HOOK = new Cursor(Cursor.E_RESIZE_CURSOR);
    private static Cursor CURSOR_CENTRAL_HOOK = new Cursor(Cursor.MOVE_CURSOR);
    private static Cursor CURSOR_RIGHT_HOOK = new Cursor(Cursor.W_RESIZE_CURSOR);
    private static final int LOC_RESIZE_FROM = 1;
    private static final int LOC_RESIZE_TO = 2;
    private static final int LOC_RESIZE_CENTER = 3;
    private static final int LOC_RESIZE_UNKNOWN = -1;
    private static Locale LOCALE = Locale.ENGLISH;
    //Settings
    private DrawerSettings settings = new DrawerSettings();
    //Flags
    private Integer latestMousePositionX = null;
    private int currentMousePositionX = 0;
    private Timer viewToModelSync = null;
    private Timer modelToViewSync = null;
    private boolean mouseInside = false;
    //Model
    private TimelineModel model;
    private TimelineController controller;
    //Ticks
    private TickGraph tickGraph = new TickGraph();
    //Sparkline
    private Sparkline sparkline = new Sparkline();
    //Tooltip
    private TimelineTooltip tooltip = new TimelineTooltip();

    public enum TimelineState {

        IDLE,
        MOVING,
        RESIZE_FROM,
        RESIZE_TO
    }
    TimelineState currentState = TimelineState.IDLE;

    public enum HighlightedComponent {

        NONE,
        LEFT_HOOK,
        RIGHT_HOOK,
        CENTER_HOOK
    }
    HighlightedComponent highlightedComponent = HighlightedComponent.NONE;

    public TimelineDrawer() {
        addMouseMotionListener(this);
        addMouseListener(this);
    }

    public void consumeEvent(TimelineModelEvent event) {
        switch (event.getEventType()) {
            case INTERVAL:
                double[] data = (double[]) event.getData();
                setInterval(data[0], data[1]);
                break;
            case CUSTOM_BOUNDS:
                double[] data2 = (double[]) event.getData();
                setCustomBounds(data2[0], data2[1]);
                break;
            case MIN_MAX:
                double[] data3 = (double[]) event.getData();
                setMinMax(data3[0], data3[1]);
                break;
        }
    }

    public void setModel(TimelineModel model) {
        this.controller = Lookup.getDefault().lookup(TimelineController.class);
        this.model = model;
        if (model != null) {
            setMinMax(model.getMin(), model.getMax());
            if (model.hasCustomBounds()) {
                setCustomBounds(model.getCustomMin(), model.getCustomMax());
            }
            setInterval(model.getIntervalStart(), model.getIntervalEnd());
        } else {
            repaint();
        }
    }

    public void setMinMax(double min, double max) {
        repaint();
    }

    public void setCustomBounds(double min, double max) {
        repaint();
    }

    public void setInterval(double from, double to) {
        repaint();
    }

    public int getPixelPosition(double val, double duration, double min, int width) {
        return (int) ((val - min) * (width / duration));
    }

    public double getReal(int pixel, double duration, double min, int width) {
        return pixel * (duration / width) + min;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setMinimumSize(new java.awt.Dimension(300, 28));
        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();

        settings.update(width, height);
        Graphics2D g2d = (Graphics2D) g;

        int innerWidth = width - 1;
        int innerHeight = height - settings.tmMarginBottom - 2;
        int innerY = settings.tmMarginTop + 1;
        if(settings.background.top != null) {
            g2d.setColor(settings.background.top);
            g2d.fillRect(0, innerY, innerWidth, innerHeight);
        }
//        g2d.setBackground(settings.background.top);
//        g2d.setPaint(settings.background.paint);
//        g2d.setColor(settings.background.top);
//        g2d.fillRect(0, innerY, innerWidth, innerHeight);

        if (!this.isEnabled()) {
            return;
        }
        if (model == null) {
            return;
        }

        double min = model.getCustomMin();
        double max = model.getCustomMax();
        double intervalStart = model.getIntervalStart();
        double intervalEnd = model.getIntervalEnd();

        int intervalStartPixel = Math.max(0, getPixelPosition(intervalStart, max - min, min, width));
        int intervalEndPixel = Math.min(width, getPixelPosition(intervalEnd, max - min, min, width));

        g2d.setRenderingHints(settings.renderingHints);

        //TICKS
        g2d.drawImage(tickGraph.getImage(model, innerWidth, innerHeight), 0, innerY, null);

        // VISIBLE HOOK (THE LITTLE GREEN RECTANGLE ON EACH SIDE) WIDTH
        int vhw = settings.selection.visibleHookWidth;

        // SELECTED ZONE WIDTH, IN PIXELS
        int sw = intervalEndPixel - intervalStartPixel;

        if (highlightedComponent != HighlightedComponent.NONE) {
            g2d.setPaint(settings.selection.mouseOverPaint);
            switch (highlightedComponent) {
                case LEFT_HOOK:
                    g2d.fillRect(
                            intervalStartPixel,
                            settings.tmMarginTop,
                            vhw,
                            height - settings.tmMarginBottom - 1);
                    g2d.setPaint(settings.selection.paint);
                    g2d.fillRect(
                            intervalStartPixel + vhw,
                            settings.tmMarginTop,
                            sw - vhw,
                            height - settings.tmMarginBottom - 1);
                    break;
                case CENTER_HOOK:
                    g2d.setPaint(settings.selection.paint);
                    g2d.fillRect(
                            intervalStartPixel,
                            settings.tmMarginTop,
                            vhw,
                            height - settings.tmMarginBottom - 1);
                    g2d.setPaint(settings.selection.mouseOverPaint);
                    g2d.fillRect(
                            intervalStartPixel + vhw,
                            settings.tmMarginTop,
                            sw - vhw * 2,
                            height - settings.tmMarginBottom - 1);
                    g2d.setPaint(settings.selection.paint);
                    g2d.fillRect(
                            intervalEndPixel - vhw,
                            settings.tmMarginTop,
                            vhw,
                            height - settings.tmMarginBottom - 1);
                    break;
                case RIGHT_HOOK:
                    g2d.setPaint(settings.selection.paint);
                    g2d.fillRect(
                            intervalStartPixel,
                            settings.tmMarginTop,
                            sw - vhw,
                            height - settings.tmMarginBottom - 1);
                    g2d.setPaint(settings.selection.mouseOverPaint);
                    g2d.fillRect(
                            intervalEndPixel - vhw,
                            settings.tmMarginTop,
                            vhw,
                            height - settings.tmMarginBottom - 1);
                    break;
            }
        } else {
            g2d.setPaint(settings.selection.paint);
            g2d.fillRect(intervalStartPixel, settings.tmMarginTop, sw, height - settings.tmMarginBottom - 1);
        }

        g2d.setColor(settings.defaultStrokeColor);
        g2d.drawRect(intervalStartPixel, settings.tmMarginTop, sw - 1, height - settings.tmMarginBottom - 1);

        double v = getReal(currentMousePositionX, max - min, min, width);
    }

    private boolean inRange(int x, int a, int b) {
        return (a < x && x < b);
    }

    /**
     * Position of current x.
     * @param x current location
     * @param r width of slider
     * @return LOC_RESIZE_*
     */
    private int inPosition(int x, int r, int sf, int st) {
        boolean resizeFrom = inRange(x, (int) sf - 1, (int) sf + r + 1);
        boolean resizeTo = inRange(x, (int) st - r - 1, (int) st + 1);
        if (resizeFrom && resizeTo) {
            if (inRange(x, (int) sf - 1, (int) (sf + st) / 2)) {
                return LOC_RESIZE_FROM;
            } else if (inRange(x, (int) (sf + st) / 2, (int) st + 1)) {
                return LOC_RESIZE_TO;
            }
        }
        if (resizeFrom) {
            return LOC_RESIZE_FROM;
        } else if (inRange(x, (int) sf + r, (int) st - r)) {
            return LOC_RESIZE_CENTER;
        } else if (resizeTo) {
            return LOC_RESIZE_TO;
        } else {
            return LOC_RESIZE_UNKNOWN;
        }

    }

    public void mouseClicked(MouseEvent e) {
        latestMousePositionX = e.getX();
        currentMousePositionX = latestMousePositionX;
    }

    public void mousePressed(MouseEvent e) {
        if (model == null) {
            return;
        }
        int x = e.getX();
        latestMousePositionX = x;
        currentMousePositionX = latestMousePositionX;
        int r = settings.selection.visibleHookWidth + settings.selection.invisibleHookMargin;

        tooltip.stop();
        
        int width = getWidth();
        double min = model.getCustomMin();
        double max = model.getCustomMax();
        double intervalStart = model.getIntervalStart();
        double intervalEnd = model.getIntervalEnd();

        int sf = Math.max(0, getPixelPosition(intervalStart, max - min, min, width));
        int st = Math.min(width, getPixelPosition(intervalEnd, max - min, min, width));

        if (currentState == TimelineState.IDLE) {
            int position = inPosition(x, r, sf, st);
            switch (position) {
                case LOC_RESIZE_FROM:
                    highlightedComponent = HighlightedComponent.LEFT_HOOK;
                    currentState = TimelineState.RESIZE_FROM;
                    break;
                case LOC_RESIZE_CENTER:
                    highlightedComponent = HighlightedComponent.CENTER_HOOK;
                    currentState = TimelineState.MOVING;
                    break;
                case LOC_RESIZE_TO:
                    highlightedComponent = HighlightedComponent.RIGHT_HOOK;
                    currentState = TimelineState.RESIZE_TO;
                    break;
                default:
                    break;
            }
        }
//        if(e.isPopupTrigger()) {
//            System.out.println("popup!");
//            MetricPopup.setLocation(e.getX(), e.getY());
//            MetricPopup.setVisible(true);
//        }        
    }

    public void mouseEntered(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
        if (currentState == TimelineState.IDLE) {
            latestMousePositionX = e.getX();
            currentMousePositionX = latestMousePositionX;
        }
        mouseInside = true;
    }

    public void mouseExited(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
        if (currentState == TimelineState.IDLE) {
            highlightedComponent = HighlightedComponent.NONE;
            latestMousePositionX = e.getX();
            currentMousePositionX = latestMousePositionX;
        }
        mouseInside = false;
        tooltip.stop();
        repaint();
    }

    public void mouseReleased(MouseEvent evt) {

        latestMousePositionX = evt.getX();
        currentMousePositionX = latestMousePositionX;
        //highlightedComponent = HighlightedComponent.NONE;
        currentState = TimelineState.IDLE;
        this.getParent().repaint(); // so it will repaint upper and bottom panes
    }

    public void mouseMoved(MouseEvent evt) {
        if (model == null) {
            return;
        }

        //System.out.println("mouse moved");
        currentMousePositionX = evt.getX();
        int x = currentMousePositionX;
        int width = getWidth();
        int r = settings.selection.visibleHookWidth;

        double min = model.getCustomMin();
        double max = model.getCustomMax();
        double intervalStart = model.getIntervalStart();
        double intervalEnd = model.getIntervalEnd();

        int sf = Math.max(0, getPixelPosition(intervalStart, max - min, min, width));
        int st = Math.min(width, getPixelPosition(intervalEnd, max - min, min, width));


        //Tooltip
        double pos = getReal(currentMousePositionX, max - min, min, width);
        tooltip.setModel(model);
        tooltip.start(pos, evt.getLocationOnScreen(), this);

        // SELECTED ZONE BEGIN POSITION, IN PIXELS
        // int sf = (int) (model.getFromFloat() * (double) w);

        // SELECTED ZONE END POSITION, IN PIXELS
        //int st = (int) (model.getToFloat() * (double) w);

        HighlightedComponent old = highlightedComponent;
        Cursor newCursor = null;

        int a = 0;//settings.selection.invisibleHookMargin;

        int position = inPosition(x, r, sf, st);
        switch (position) {
            case LOC_RESIZE_FROM:
                newCursor = CURSOR_LEFT_HOOK;
                highlightedComponent = HighlightedComponent.LEFT_HOOK;
                break;
            case LOC_RESIZE_CENTER:
                highlightedComponent = HighlightedComponent.CENTER_HOOK;
                newCursor = CURSOR_CENTRAL_HOOK;
                break;
            case LOC_RESIZE_TO:
                highlightedComponent = HighlightedComponent.RIGHT_HOOK;
                newCursor = CURSOR_RIGHT_HOOK;
                break;
            default:
                highlightedComponent = HighlightedComponent.NONE;
                newCursor = CURSOR_DEFAULT;
                break;
        }
        if (newCursor != getCursor()) {
            setCursor(newCursor);
        }
        // only repaint if highlight has changed (save a lot of fps)
        if (highlightedComponent != old) {
            repaint();
        }
//         now we always repaint, because of the tooltip
//        repaint();

    }

    public void mouseDragged(MouseEvent evt) {

        if (model == null) {
            return;
        }
        int width = getWidth();

        double min = model.getCustomMin();
        double max = model.getCustomMax();
        double intervalStart = model.getIntervalStart();
        double intervalEnd = model.getIntervalEnd();

        int sf = Math.max(0, getPixelPosition(intervalStart, max - min, min, width));
        int st = Math.min(width, getPixelPosition(intervalEnd, max - min, min, width));

        currentMousePositionX = evt.getX();
        currentMousePositionX = Math.max(0, currentMousePositionX);
        currentMousePositionX = Math.min(width, currentMousePositionX);
        int x = currentMousePositionX;

        tooltip.stop();

        int r = settings.selection.visibleHookWidth;

        // SELECTED ZONE BEGIN POSITION, IN PIXELS
        // sf = (model.getFromFloat() * w);

        // SELECTED ZONE END POSITION, IN PIXELS
        //st = (model.getToFloat() * w);

        if (currentState == TimelineState.IDLE) {
            int position = inPosition(x, r, sf, st);
            switch (position) {
                case LOC_RESIZE_FROM:
                    highlightedComponent = HighlightedComponent.LEFT_HOOK;
                    currentState = TimelineState.RESIZE_FROM;
                    break;
                case LOC_RESIZE_CENTER:
                    highlightedComponent = HighlightedComponent.CENTER_HOOK;
                    currentState = TimelineState.MOVING;
                    break;
                case LOC_RESIZE_TO:
                    highlightedComponent = HighlightedComponent.RIGHT_HOOK;
                    currentState = TimelineState.RESIZE_TO;
                    break;
                default:
                    break;
            }
        }
        double delta = 0;
        if (latestMousePositionX != null) {
            delta = x - latestMousePositionX;
        }
        latestMousePositionX = x;

        // minimal selection zone width (a security to not crush it!)
        int s = settings.selection.minimalWidth;

        switch (currentState) {
            case RESIZE_FROM:

                //problem: moving the left part will crush the security zone
                if ((sf + delta) >= (st - s)) {
                    sf = st - s;
                } else {
                    if (sf + delta <= 0) {
                        sf = 0;
                    } else {
                        sf += delta;
                    }
                }
                break;
            case RESIZE_TO:
                if ((st + delta) <= (sf + s)) {
                    st = sf + s;
                } else {
                    if ((st + delta >= width)) {
                        st = width;
                    } else {
                        st += delta;
                    }
                }
                break;
            case MOVING:
                // collision on the left..
                if ((sf + delta) < 0) {
                    st = (st - sf);
                    sf = 0;
                    // .. or the right
                } else if ((st + delta) >= width) {
                    sf = width - (st - sf);
                    st = width;
                } else {
                    sf += delta;
                    st += delta;
                }
                break;
        }

        if (width != 0) {
            double from = getReal(sf, max - min, min, width);
            double to = getReal(st, max - min, min, width);
            from = Math.max(from, model.getCustomMin());
            to = Math.min(to, model.getCustomMax());
            if (from < to) {
                controller.setInterval(from, to);
            }
        }
    }
}
