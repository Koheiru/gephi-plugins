/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.exchanging.timeline;

import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;
import ru.kohei.exchanging.DefaultExchanger;

/**
 *
 * @author Prostov Yury
 */
public abstract class TimelineExchanger extends DefaultExchanger {
    protected enum ExchangerType {
        EXPORTER,
        IMPORTER,
    }
    
    protected static final String SOURCE_EXPORTER = "timeline_exporter";
    protected static final String SOURCE_IMPORTER = "timeline_importer";
    
    protected static final String ACTION_INTERVAL_CHANGED       = "interval_changed";
    protected static final String ACTION_GLOBAL_BOUNDS_CHANGED  = "global_bounds_changed";
    protected static final String ACTION_CUSTOM_BOUNDS_CHANGED  = "custom_bounds_changed";
    protected static final String ACTION_VALIDITY_STATE_CHANGED = "bounds_validity_changed";
    protected static final String ACTION_ENABLED_STATE_CHANGED  = "enabled_state_changed";
    protected static final String ACTION_PLAYING_STATE_CHANGED  = "playing_state_changed";
    
    protected static final String DATA_MIN_BOUND = "min_bound";
    protected static final String DATA_MAX_BOUND = "max_bound";
    protected static final String DATA_STATE     = "state";
    
    private String m_source = null;
    private List<String> m_acceptedSources = new ArrayList();
    
    protected TimelineExchanger(ExchangerType exchangerType) {
        boolean isExporter = (exchangerType == ExchangerType.EXPORTER);
        if (isExporter) {
            m_source = SOURCE_EXPORTER;
            m_acceptedSources.add(SOURCE_IMPORTER);
        } else {
            m_source = SOURCE_IMPORTER;
            m_acceptedSources.add(SOURCE_EXPORTER);
        }
    }
    
    @Override
    public List<String> acceptedExchangers() {
        return m_acceptedSources;
    }
    
    protected void sendTimelineMessage(String action, String param, Object value) {
        JSONObject data = new JSONObject();
        data.put(param, value);
        sendMessage(m_source, action, data);
    }
    
    protected void sendTimelineMessage(String action, String param1, Object value1, String param2, Object value2) {
        JSONObject data = new JSONObject();
        data.put(param1, value1);
        data.put(param2, value2);
        sendMessage(m_source, action, data);
    }
}
