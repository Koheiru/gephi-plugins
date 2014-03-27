/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.timeline.api;

import org.gephi.project.api.Workspace;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.type.Interval;


/**
 * 
 * @author Prostov Yury
 */
public interface TimelineController {
    
    public TimelineModel getModel();
    
    public TimelineModel getModel(Workspace workspace);
    
    public void setCustomBounds(Interval bounds);
    
    public void setPosition(double position);
    
    public void startPlaying();
    
    public void stopPlaying();
    
    public void stepForward();
    
    public void stepBackward();
    
    public void setPlayStep(double stepSize);
        
    public void setPlaySpeed(int stepDelay);
    
    public void addListener(TimelineModelListener listener);
    
    public void removeListener(TimelineModelListener listener);
    
}
