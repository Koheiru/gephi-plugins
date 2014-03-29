/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.exchanging.api;

import java.util.List;

/**
 *
 * @author Prostov Yury
 */
public interface Exchanger {
    public static interface MessageListener {
        public void onMessageSent(Message message);
    }    
    
    public String name();
    
    public String description();
    
    public List<String> acceptedExchangers();
    
    public void onMessageReceived(Message message);
    
    public void addListener(MessageListener listener);

    public void removeListener(MessageListener listener);

    public void removeAllListeners();
    
}
