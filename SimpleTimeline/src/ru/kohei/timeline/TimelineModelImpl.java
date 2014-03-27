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
        m_customBounds = m_globalBounds;
        m_position = (isBounded(m_globalBounds)) ? (m_globalBounds.getLow()) : (0.0);
        
        m_isPlaying = new AtomicBoolean(false);
        m_playStep = 1.0;
        m_playSpeed = 100;
    }
    
    @Override
    public boolean hasValidBounds() {
        return isBounded(m_globalBounds);
    }
    
    private boolean isBounded(Interval interval) {
        return (isBounded(interval.getLow()) && isBounded(interval.getHigh()));
    }
    
    private boolean isBounded(double value) {
        return !Double.isInfinite(value);
    }
    
    @Override
    public Interval getGlobalBounds() {
        return m_globalBounds;
    } 
    
    public void setGlobalBounds(Interval bounds) {
        m_globalBounds = bounds;
    }
    
    @Override
    public boolean hasCustomBounds() {
        return !isEqual(m_customBounds, m_globalBounds);
    }
    
    private boolean isEqual(Interval a, Interval b) {
        return (isEqual(a.getLow(), b.getLow()) && isEqual(a.getHigh(), b.getHigh()));
    }
    
    private boolean isEqual(double a, double b) {
        return (Double.compare(a, b) == 0);
    }
    
    @Override
    public Interval getCustomBounds() {
        return m_customBounds;
    }
    
    public void setCustomBounds(Interval bounds) {
        m_customBounds = bounds;
    }
    
    @Override
    public double getPosition() {
        return m_position;
    }
    
    public void setPosition(double position) {
        m_position = position;
    }
    
    @Override
    public boolean isPlaying() {
        return m_isPlaying.get();
    }
    
    public void setPlaying(boolean isPlaying) {
        m_isPlaying.set(isPlaying);
    }
    
    @Override
    public double getPlayStep() {
        return m_playStep;
    }
    
    public void setPlayStep(double stepSize) {
        m_playStep = stepSize;
    }
    
    @Override
    public int getPlaySpeed() {
        return m_playSpeed;
    }
    
    public void setPlaySpeed(int stepDelay) {
        m_playSpeed = stepDelay;
    }
    
}

