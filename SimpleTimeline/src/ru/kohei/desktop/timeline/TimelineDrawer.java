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
import org.gephi.data.attributes.type.Interval;
import ru.kohei.timeline.api.TimelineController;
import ru.kohei.timeline.api.TimelineModel;
import ru.kohei.timeline.api.TimelineModelEvent;
import ru.kohei.timeline.api.TimelineModelEvent.EventType;
import ru.kohei.timeline.api.TimelineModelListener;
import org.openide.util.Lookup;

/**
 *
 * @author Prostov Yury
 */
public class TimelineDrawer extends JPanel 
implements MouseListener, MouseMotionListener, TimelineModelListener {

    private transient TimelineModel m_model;
    private transient TimelineController m_controller;
    
    //Settings
    private DrawerSettings settings = new DrawerSettings();
    private TickGraph tickGraph = new TickGraph();
    
    
    public TimelineDrawer() {
        addMouseMotionListener(this);
        addMouseListener(this);        
    }
    
    public void initialize(TimelineController controller) {
        m_controller = controller;
        m_model = m_controller.getModel();
        m_controller.addListener(this);
        updateState();
    }
    
    @Override
    public void timelineModelChanged(TimelineModelEvent event) {
        if (event.getEventType().equals(EventType.MODEL_CHANGED)) {
            m_model = event.getSource();
        }
        updateState();
    }
    
    private void updateState() {
        repaint();
    }
    
    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        int width = getWidth();
        int height = getHeight();

        settings.update(width, height);
        Graphics2D painter = (Graphics2D)graphics;

        int innerWidth = width - 1;
        int innerHeight = height - settings.tmMarginBottom - 2;
        int innerY = settings.tmMarginTop + 1;
        if (settings.background.top != null) {
            painter.setColor(settings.background.top);
            painter.fillRect(0, innerY, innerWidth, innerHeight);
        }
        
//        g2d.setBackground(settings.background.top);
//        g2d.setPaint(settings.background.paint);
//        g2d.setColor(settings.background.top);
//        g2d.fillRect(0, innerY, innerWidth, innerHeight);

        if (!isEnabled()) {
            return;
        }
        if (m_model == null || !m_model.hasValidBounds()) {
            return;
        }

        Interval bounds = m_model.getCustomBounds();
        double minBound = bounds.getLow();
        double maxBound = bounds.getHigh();
        double position = m_model.getPosition();
        
        int positionPixel = getPixelPosition(position, maxBound - minBound, minBound, width);
        positionPixel = Math.min(width, Math.max(0, positionPixel));
        
        painter.setRenderingHints(settings.renderingHints);
        painter.drawImage(tickGraph.getImage(m_model, innerWidth, innerHeight), 0, innerY, null);
        
        
        // SELECTED ZONE WIDTH, IN PIXELS
        int sw = 2;

        painter.setPaint(settings.defaultStrokeColor);
        painter.fillRect(positionPixel, settings.tmMarginTop, sw, height - settings.tmMarginBottom - 1);
        
        //double v = getReal(currentMousePositionX, max - min, min, width);
    }
    
    private int getPixelPosition(double val, double duration, double min, int width) {
        return (int) ((val - min) * (width / duration));
    }
    
    /*
    private boolean inRange(int x, int a, int b) {
        return (a < x && x < b);
    }

    private double getReal(int pixel, double duration, double min, int width) {
        return pixel * (duration / width) + min;
    }
    */
    
    @Override
    public void mouseClicked(MouseEvent event) {
        /*
        if (m_model == null) {
            return;
        }

        int x = event.getX();
        int width = getWidth();

        Interval bounds = m_model.getCustomBounds();
        double minBound = bounds.getLow();
        double maxBound = bounds.getHigh();
        double position = getReal(x, maxBound - minBound, minBound, width);
        m_controller.setPosition(position);
        */
    }

    @Override
    public void mousePressed(MouseEvent event) {
    }

    @Override
    public void mouseReleased(MouseEvent event) {
    }

    @Override
    public void mouseEntered(MouseEvent event) {
    }

    @Override
    public void mouseExited(MouseEvent event) {
    }

    @Override
    public void mouseDragged(MouseEvent event) {
    }

    @Override
    public void mouseMoved(MouseEvent event) {
    }
   
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setMinimumSize(new java.awt.Dimension(300, 28));
        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
