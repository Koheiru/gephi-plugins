/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.timeline;

import java.util.concurrent.atomic.AtomicBoolean;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.dynamic.api.DynamicModel.TimeFormat;
import ru.kohei.timeline.api.TimelineModel;

/**
 *
 * @author Prostov Yury
 */
public class TimelineModelImpl implements TimelineModel {

    private boolean enabled;
    private DynamicModel dynamicModel;
    private double customMin;
    private double customMax;
    
    private int playDelay;
    private AtomicBoolean playing;
    private double playStep;
    private PlayMode playMode;
    
    private double previousMin;
    private double previousMax;

    public TimelineModelImpl(DynamicModel dynamicModel) {
        this.dynamicModel = dynamicModel;
        this.customMin = dynamicModel.getMin();
        this.customMax = dynamicModel.getMax();
        this.previousMin = customMin;
        this.previousMax = customMax;
        playDelay = 100;
        playStep = 0.01;
        playing = new AtomicBoolean(false);
        playMode = PlayMode.TWO_BOUNDS;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public double getMin() {
        return dynamicModel.getMin();
    }

    @Override
    public double getMax() {
        return dynamicModel.getMax();
    }

    public double getPreviousMin() {
        return previousMin;
    }

    public double getPreviousMax() {
        return previousMax;
    }

    public void setPreviousMax(double previousMax) {
        this.previousMax = previousMax;
    }

    public void setPreviousMin(double previousMin) {
        this.previousMin = previousMin;
    }

    @Override
    public double getCustomMin() {
        return customMin;
    }

    @Override
    public double getCustomMax() {
        return customMax;
    }

    @Override
    public boolean hasCustomBounds() {
        return customMax != dynamicModel.getMax() || customMin != dynamicModel.getMin();
    }

    @Override
    public double getIntervalStart() {
        double vi = dynamicModel.getVisibleInterval().getLow();
        if(Double.isInfinite(vi)) {
            return getCustomMin();
        }
        return vi;
    }

    @Override
    public double getIntervalEnd() {
        double vi = dynamicModel.getVisibleInterval().getHigh();
        if(Double.isInfinite(vi)) {
            return getCustomMax();
        }
        return vi;
    }

    @Override
    public TimeFormat getTimeFormat() {
        return dynamicModel.getTimeFormat();
    }

    public DynamicModel getDynamicModel() {
        return dynamicModel;
    }

    public void setCustomMax(double customMax) {
        this.customMax = customMax;
    }

    public void setCustomMin(double customMin) {
        this.customMin = customMin;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean hasValidBounds() {
        return !Double.isInfinite(dynamicModel.getMin()) && !Double.isInfinite(dynamicModel.getMax());
    }

    @Override
    public boolean isPlaying() {
        return playing.get();
    }

    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    @Override
    public int getPlayDelay() {
        return playDelay;
    }

    public void setPlayDelay(int playDelay) {
        this.playDelay = playDelay;
    }

    @Override
    public double getPlayStep() {
        return playStep;
    }

    public void setPlayStep(double playStep) {
        this.playStep = playStep;
    }

    @Override
    public PlayMode getPlayMode() {
        return playMode;
    }

    public void setPlayMode(PlayMode playMode) {
        this.playMode = playMode;
    }
    
}

