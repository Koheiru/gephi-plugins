/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.exchanging.timeline;

/**
 *
 * @author Prostov Yury
 */
public class TimelineExporter extends TimelineExchanger {
 
    public TimelineExporter() {
        super(ExchangerType.EXPORTER);
    }
    
    public void sendInterval(double minBound, double maxBound) {
        sendTimelineMessage(ACTION_INTERVAL_CHANGED, DATA_MIN_BOUND, minBound, DATA_MAX_BOUND, maxBound);
    }
    
    public void sendGlobalBounds(double minBound, double maxBound) {
        sendTimelineMessage(ACTION_GLOBAL_BOUNDS_CHANGED, DATA_MIN_BOUND, minBound, DATA_MAX_BOUND, maxBound);
    }
    
    public void sendCustomBounds(double minBound, double maxBound) {
        sendTimelineMessage(ACTION_CUSTOM_BOUNDS_CHANGED, DATA_MIN_BOUND, minBound, DATA_MAX_BOUND, maxBound);
    }
    
    public void sendValidityState(boolean isValid) {
        sendTimelineMessage(ACTION_VALIDITY_STATE_CHANGED, DATA_STATE, isValid);
    }
    
    public void sendEnabledState(Boolean isEnabled) {
        sendTimelineMessage(ACTION_ENABLED_STATE_CHANGED, DATA_STATE, isEnabled);
    }
    
    public void sendPlayingState(Boolean isPlaying) {
        sendTimelineMessage(ACTION_PLAYING_STATE_CHANGED, DATA_STATE, isPlaying);
    }
    
}
