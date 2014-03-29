/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.exchanging.api;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author Prostov Yury
 */
public interface ExchangingPoint {
    public enum State {
        OPENING,
        OPENED,
        CLOSING,
        CLOSED,
        ERROR
    }
    
    public static interface StateListener {
        public void onStateChanged(State state);
    }
    
    public void open(int port);
    
    public void open(String address, int port);
    
    public void close();
    
    public State state();
    
    public boolean waitForOpened() throws InterruptedException;
    
    public boolean waitForOpened(int time, TimeUnit units) throws InterruptedException;
    
    public boolean waitForClosed() throws InterruptedException;
    
    public boolean waitForClosed(int time, TimeUnit units) throws InterruptedException;
    
    public void addListener(StateListener listener);
        
    public void removeListener(StateListener listener);
    
    public void removeAllListeners();
    
    public void attachExchanger(Exchanger exchanger);
    
    public void detachExchanger(Exchanger exchanger);
    
    public void detachAllExchangers();
    
}
