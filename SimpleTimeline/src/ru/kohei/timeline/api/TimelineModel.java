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
    
    public Interval getGlobalBounds();
    
    public boolean hasValidBounds();
    
    public double getPosition();
    
    public Interval getCustomBounds();
    
    public boolean hasCustomBounds();
    
    public boolean isPlaying();
    
    public double getPlayStep();
    
    public int getPlaySpeed();
    
}
