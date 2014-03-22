/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.desktop.timeline;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.util.Lookup;

/**
 *
 * @author Prostov Yury
 */
public final class TimelineWindowAction implements ActionListener {
    
    public void actionPerformed(ActionEvent e) {
        BottomComponentImpl bottomComponent = Lookup.getDefault().lookup(BottomComponentImpl.class);
        if (bottomComponent != null) {
            bottomComponent.setVisible(!bottomComponent.isVisible());
        }
    }
}
