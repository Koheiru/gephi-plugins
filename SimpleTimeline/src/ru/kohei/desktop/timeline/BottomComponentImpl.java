/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.desktop.timeline;

import javax.swing.JComponent;
import org.gephi.desktop.perspective.spi.BottomComponent;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Prostov Yury
 */
@ServiceProvider(service=BottomComponent.class)
public class BottomComponentImpl implements BottomComponent {

    private TimelineTopComponent timelineTopComponent = new TimelineTopComponent();
    
    public JComponent getComponent() {
        return timelineTopComponent;
    }

    public void setVisible(boolean visible) {
        timelineTopComponent.setVisible(visible);
    }
    
    public boolean isVisible() {
        return timelineTopComponent.isVisible();
    }
}
