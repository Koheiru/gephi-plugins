/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.timeline.api;

import org.gephi.dynamic.api.DynamicModel;

/**
 *
 * @author Prostov Yury
 */
public interface TimelineModel {
    
    public enum PlayMode {
        ONE_BOUND,
        TWO_BOUNDS
    };
    
    public boolean isEnabled();
    
    public double getMin();
    
    public double getMax();
    
    public double getCustomMin();
    
    public double getCustomMax();
    
    public boolean hasCustomBounds();
    
    public boolean hasValidBounds();
    
    public double getIntervalStart();
    
    public double getIntervalEnd();
    
    public DynamicModel.TimeFormat getTimeFormat();
    
    public int getPlayDelay();
    
    public double getPlayStep();
    
    public boolean isPlaying();
    
    public PlayMode getPlayMode();
    
}
