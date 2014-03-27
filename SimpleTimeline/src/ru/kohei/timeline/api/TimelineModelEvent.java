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
        MODEL_CHANGED, 
        GLOBAL_BOUNDS_CHANGED,
        BOUNDS_VALIDITY_CHANGED,
        CUSTOM_BOUNDS_CHANGED,
        POSITION_CHANGED,
        PLAY_STATE_CHANGED,
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
