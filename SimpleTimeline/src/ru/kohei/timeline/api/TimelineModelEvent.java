/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.timeline.api;

/**
 *
 * @author Prostov Yury
 */
public final class TimelineModelEvent {

    public enum EventType {
        MODEL, 
        MIN_MAX, 
        INTERVAL, 
        CUSTOM_BOUNDS, 
        ENABLED, 
        PLAY_START, 
        PLAY_STOP,
        VALID_BOUNDS
    };
    
    private final EventType type;
    private final TimelineModel source;
    private final Object data;

    public TimelineModelEvent (EventType type, TimelineModel source, Object data) {
        this.type = type;
        this.source = source;
        this.data = data;
    }

    public EventType getEventType() {
        return type;
    }

    public TimelineModel getSource() {
        return source;
    }

    public Object getData() {
        return data;
    }
}
