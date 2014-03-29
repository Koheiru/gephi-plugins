/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.exchanging;

import java.util.ArrayList;
import java.util.List;
import ru.kohei.exchanging.api.Exchanger;
import ru.kohei.exchanging.api.Message;

/**
 *
 * @author Prostov Yury
 */
public abstract class DefaultExchanger implements Exchanger {
    private List<MessageListener> m_listeners = new ArrayList();
    
    @Override
    public abstract String name();

    @Override
    public abstract String description();

    @Override
    public List<String> acceptedExchangers() {
        final List<String> emptyList = new ArrayList();
        return emptyList;
    }

    @Override
    public void onMessageReceived(Message message) {        
    }

    protected void sendMessage(String source, String action, Object data) {
        Message message = new Message(source, action, data);
        sendMessage(message);
    }
    
    protected void sendMessage(Message message) {
        Object[] listeners = null;
        synchronized(m_listeners) {
            listeners = m_listeners.toArray();
        }
        
        for (Object object: listeners) {
            ((MessageListener)object).onMessageSent(message);
        }
    }
    
    @Override
    public void addListener(MessageListener listener) {
        synchronized(m_listeners) {
            m_listeners.remove(listener);
        }
    }

    @Override
    public void removeListener(MessageListener listener) {
        synchronized(m_listeners) {
            m_listeners.remove(listener);
        }
    }

    @Override
    public void removeAllListeners() {
        synchronized(m_listeners) {
            m_listeners.clear();
        }
    }
    
}
