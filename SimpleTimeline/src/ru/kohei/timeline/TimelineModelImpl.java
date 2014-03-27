/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.timeline;

import java.util.concurrent.atomic.AtomicBoolean;
import org.gephi.data.attributes.type.Interval;
import org.gephi.dynamic.api.DynamicModel;
import ru.kohei.timeline.api.TimelineModel;

/**
 *
 * @author Prostov Yury
 */
public class TimelineModelImpl implements TimelineModel {

    private Interval m_globalBounds;
    private Interval m_customBounds;
    private double m_position;
    
    private AtomicBoolean m_isPlaying;
    private double m_playStep;
    private int m_playSpeed;
    
    
    public TimelineModelImpl(DynamicModel dynamicModel) {
        m_globalBounds = new Interval(dynamicModel.getMin(), dynamicModel.getMax());
        m_customBounds = null;
        
        boolean isValidMin = !Double.isInfinite(m_globalBounds.getLow());
        boolean isValidMax = !Double.isInfinite(m_globalBounds.getHigh());
        m_position = (isValidMin && isValidMax) ? (m_globalBounds.getLow()) : (0.0);
        
        m_playStep = 1.0;
        m_playSpeed = 100;
        m_isPlaying = new AtomicBoolean(false);
    }
    
    public void setGlobalBounds(Interval bounds) {
        m_globalBounds = bounds;
    }
    
    @Override
    public Interval getGlobalBounds() {
        return m_globalBounds;
    }
    
    @Override
    public boolean hasValidBounds() {
        boolean isValidMin = !Double.isInfinite(m_globalBounds.getLow());
        boolean isValidMax = !Double.isInfinite(m_globalBounds.getHigh());
        return (isValidMin && isValidMax);
    }
    
    public void setPosition(double position) {
        m_position = position;
    }
    
    @Override
    public double getPosition() {
        return m_position;
    }
    
    public void setCustomBounds(Interval bounds) {
        m_customBounds = bounds;
    }
    
    @Override
    public Interval getCustomBounds() {
        return m_customBounds;
    }
    
    @Override
    public boolean hasCustomBounds() {
        return (m_customBounds != null);
    }
    
    public void setPlaying(boolean isPlaying) {
        m_isPlaying.set(isPlaying);
    }
    
    @Override
    public boolean isPlaying() {
        return m_isPlaying.get();
    }
    
    public void setPlayStep(double stepSize) {
        m_playStep = stepSize;
    }
    
    @Override
    public double getPlayStep() {
        return m_playStep;
    }
    
    public void setPlaySpeed(int stepDelay) {
        m_playSpeed = stepDelay;
    }
    
    @Override
    public int getPlaySpeed() {
        return m_playSpeed;
    }
    
}

