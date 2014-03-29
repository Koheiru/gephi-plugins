/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.exchanging.api;

/**
 *
 * @author Prostov Yury
 */
public final class Message {
    private String  m_source;
    private String  m_action;
    private Object  m_data;
    
    public Message(String source, String action, Object data) {
        m_source = source;
        m_action = action;
        m_data = data;
    }
    
    public String source() {
        return m_source;
    }
    
    public String action() {
        return m_action;
    }
    
    public Object data() {
        return m_data;
    }    
}
