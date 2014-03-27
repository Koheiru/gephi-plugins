/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.timeline.api;

import org.gephi.data.attributes.type.Interval;

/**
 *
 * @author Prostov Yury
 */
public interface TimelineModel {
        
    public boolean hasValidBounds();
    
    public Interval getGlobalBounds();
    
    public boolean hasCustomBounds();
    
    public Interval getCustomBounds();
    
    public double getPosition();    
    
    public boolean isPlaying();
    
    public double getPlayStep();
    
    public int getPlaySpeed();
    
}
