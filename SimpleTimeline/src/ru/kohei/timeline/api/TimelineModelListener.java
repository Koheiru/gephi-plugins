/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.timeline.api;

/**
 *
 * @author Prostov Yury
 */
public interface TimelineModelListener {

    public void timelineModelChanged(TimelineModelEvent event);
    
}
