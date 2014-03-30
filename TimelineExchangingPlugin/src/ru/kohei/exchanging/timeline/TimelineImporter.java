/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.exchanging.timeline;

import ru.kohei.exchanging.api.Message;

/**
 *
 * @author Prostov Yury
 */
public class TimelineImporter extends TimelineExchanger {
    
    public TimelineImporter() {
        super(ExchangerType.IMPORTER);
    }
    
    @Override
    public void onMessageReceived(Message message) {
        String source = message.source();
        if (!source.equals(SOURCE_EXPORTER)) {
            return;
        }
    }
    
    protected void onIntervalChanged(Double minBound, Double maxBound) {
    }
    
    protected void onGlobalBoundsChanged(Double minBound, Double maxBound) {
    }
    
    protected void onCustomBoundsChanged(Double minBound, Double maxBound) {
    }
    
    protected void onValidityStateChanged(Boolean isValid) {
    }
    
    protected void onEnabledStateChanged(Boolean isEnabled) {
    }
    
    protected void onPlayingStateChanged(Boolean isPlaying) {
    }
    
}
