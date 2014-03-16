/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.timeline.api;

import org.gephi.project.api.Workspace;
import org.gephi.data.attributes.api.AttributeColumn;


/**
 * 
 * @author Prostov Yury
 */
public interface TimelineController {
    
    public TimelineModel getModel(Workspace workspace);
    
    public TimelineModel getModel();
    
    public void setCustomBounds(double min, double max);
    
    public void setEnabled(boolean enabled);
    
    public void setInterval(double from, double to);
    
    public void startPlay();
    
    public void stopPlay();
    
    public void stepForward();
    
    public void stepBackward();
    
    public void setPlaySpeed(int delay);
    
    public void setPlayStep(double step);
    
    public void setPlayMode(TimelineModel.PlayMode playMode);
    
    public AttributeColumn[] getDynamicGraphColumns();
    
    public void addListener(TimelineModelListener listener);
    
    public void removeListener(TimelineModelListener listener);
}
