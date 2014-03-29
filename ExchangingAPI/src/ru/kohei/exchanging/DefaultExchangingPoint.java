/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.exchanging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ru.kohei.exchanging.api.Exchanger;
import ru.kohei.exchanging.api.ExchangingPoint;
import ru.kohei.exchanging.api.Message;

/**
 *
 * @author Prostov Yury
 */
public abstract class DefaultExchangingPoint implements ExchangingPoint, Exchanger.MessageListener {
    private State m_state = State.CLOSED;
    private Lock m_stateLock = new ReentrantLock();
    private Condition m_closedCondition = m_stateLock.newCondition();
    private Condition m_openedCondition = m_stateLock.newCondition();
    
    private List<Exchanger> m_exchangers = new ArrayList();
    private Map<String, List<Exchanger>> m_receivingExchangers = new HashMap();
    
    private List<StateListener> m_listeners = new ArrayList();
        
    @Override
    public abstract void open(int port);
    
    @Override
    public abstract void open(String address, int port);
    
    @Override
    public abstract void close();
    
    @Override
    public State state() {
        return m_state;
    }
    
    protected void setState(State state) {
        m_stateLock.lock();
        try {
            m_state = state;
            if (m_state == State.OPENED) {
                m_openedCondition.signalAll();
            }
            if (m_state == State.OPENED || m_state == State.ERROR) {
                m_closedCondition.signalAll();
            }
        } finally {
            m_stateLock.unlock();
        }
        
        Object[] listeners = null;
        synchronized (m_listeners) {
            listeners = m_listeners.toArray();
        }
        for (Object listener: listeners) {
            ((StateListener)listener).onStateChanged(state);
        }
    }
    
    @Override
    public boolean waitForOpened() throws InterruptedException {
        return waitForOpened(0, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public boolean waitForOpened(int time, TimeUnit units) throws InterruptedException {
        m_stateLock.lock();
        
        if (m_state == State.OPENED) {
            m_stateLock.unlock();
            return true;
        }
        else if (m_state != State.OPENING) {
            m_stateLock.unlock();
            return false;
        }
        
        try {
            if (time > 0) {
                m_openedCondition.await(time, units);
            }
            else {
                m_openedCondition.await();
            }
        }
        finally {
            State state = m_state;
            m_stateLock.unlock();
            return (state == State.OPENED);
        }
    }
    
    @Override
    public boolean waitForClosed() throws InterruptedException {
        return waitForClosed(0, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public boolean waitForClosed(int time, TimeUnit units) throws InterruptedException {
        m_stateLock.lock();
        
        if (m_state == State.CLOSED) {
            m_stateLock.unlock();
            return true;
        }
        
        try {
            if (time > 0) {
                m_closedCondition.await(time, units);
            }
            else {
                m_closedCondition.await();
            }
        }
        finally {
            State state = m_state;
            m_stateLock.unlock();
            return (state == State.CLOSED || state == State.ERROR);
        }
    }
    
    @Override
    public void addListener(StateListener listener) {
        synchronized (m_listeners) {
            if (m_listeners.contains(listener)) {
                return;
            }
            m_listeners.add(listener);
        }
    }
    
    @Override
    public void removeListener(StateListener listener) {
        synchronized (m_listeners) {
            m_listeners.remove(listener);
        }
    }
    
    @Override
    public void removeAllListeners() {
        synchronized (m_listeners) {
            m_listeners.clear();
        }
    }
    
    @Override
    public void attachExchanger(Exchanger exchanger) {
        synchronized (m_exchangers) {
            if (m_exchangers.contains(exchanger)) {
                return;
            }
            
            List<String> keys = exchanger.acceptedExchangers();
            synchronized (m_receivingExchangers) {
                for (String key : keys) {
                    List<Exchanger> receivers = m_receivingExchangers.get(key);
                    if (receivers == null) {
                        receivers = new ArrayList<Exchanger>();
                        m_receivingExchangers.put(key, receivers);
                    }
                    receivers.add(exchanger);
                }
            }
            
            exchanger.addListener(this);
            m_exchangers.add(exchanger);
        }
    }
    
    @Override
    public void detachExchanger(Exchanger exchanger) {
        synchronized (m_exchangers) {
            if (!m_exchangers.contains(exchanger)) {
                return;
            }
           
            synchronized (m_receivingExchangers) {
                for (Object entry: m_receivingExchangers.entrySet()) {
                    List<Exchanger> receivers = (List<Exchanger>)((Map.Entry)entry).getValue();
                    if (receivers != null) {
                        receivers.remove(exchanger);
                    }
                }
            }
            
            exchanger.removeListener(this);
            m_exchangers.remove(exchanger);
        }
    }
    
    @Override
    public void detachAllExchangers() {
        synchronized (m_exchangers) {
            synchronized (m_receivingExchangers) {
                m_receivingExchangers.clear();
            }
            
            for (Exchanger exchanger: m_exchangers) {
                exchanger.removeListener(this);
            }
            m_exchangers.clear();
        }
    }
    
    @Override
    public abstract void onMessageSent(Message message);
    
    protected void handleReceivedMessage(Message message) {
        Object[] receivers = null;
        synchronized (m_receivingExchangers) {
            List<Exchanger> list = m_receivingExchangers.get(message.source());
            receivers = list.toArray();
        }
        
        for (Object object: receivers) {
            ((Exchanger)object).onMessageReceived(message);
        }
    }
    
}
