/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.exchanging.timeline.plugin;

import org.gephi.timeline.api.TimelineController;
import org.gephi.timeline.api.TimelineModelEvent;
import org.gephi.timeline.api.TimelineModelListener;
import org.openide.util.Lookup;
import ru.kohei.exchanging.timeline.TimelineExporter;

/**
 *
 * @author Prostov Yury
 */
public class GephiTimelineExporter extends TimelineExporter {
    private TimelineController m_controller;
    
    public GephiTimelineExporter() {
        m_controller = Lookup.getDefault().lookup(TimelineController.class);
        m_controller.addListener(new TimelineModelListener() {
            @Override
            public void timelineModelChanged(TimelineModelEvent event) {
                TimelineModelEvent.EventType type = event.getEventType();
                switch (type) {
                    case INTERVAL:      onIntervalEvent(event);     break;
                    case MIN_MAX:       onMinMaxEvent(event);       break;
                    case CUSTOM_BOUNDS: onCustomBoundsEvent(event); break;
                    case VALID_BOUNDS:  onValidBoundsEvent(event);  break;
                    case ENABLED:       onEnabledEvent(event);      break;
                    case PLAY_START:    onPlayStartEvent(event);    break;
                    case PLAY_STOP:     onPlayStopEvent(event);     break;
                }
            }
        });
    }
    
    private void onIntervalEvent(TimelineModelEvent event) {
        double[] bounds = (double[]) event.getData();
        sendInterval(bounds[0], bounds[1]);
    }
    
    private void onMinMaxEvent(TimelineModelEvent event) {
        double[] bounds = (double[]) event.getData();
        sendGlobalBounds(bounds[0], bounds[1]);
    }
    
    private void onCustomBoundsEvent(TimelineModelEvent event) {
        double[] bounds = (double[]) event.getData();
        sendCustomBounds(bounds[0], bounds[1]);
    }
    
    private void onValidBoundsEvent(TimelineModelEvent event) {
        Boolean state = (Boolean)event.getData();
        sendValidityState(state);
    }
    
    private void onEnabledEvent(TimelineModelEvent event) {
        Boolean state = (Boolean)event.getData();
        sendEnabledState(state);
    }
    
    private void onPlayStartEvent(TimelineModelEvent event) {
        sendPlayingState(true);
    }
    
    private void onPlayStopEvent(TimelineModelEvent event) {
        sendPlayingState(false);
    }
    
}
