/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.gephi.data.attributes.api.Estimator;
import org.gephi.data.attributes.type.Interval;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.dynamic.api.DynamicModelEvent;
import org.gephi.dynamic.api.DynamicModelListener;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import ru.kohei.timeline.api.TimelineModelEvent.EventType;
import ru.kohei.timeline.api.*;


/**
 *
 * @author Prostov Yury
 */
@ServiceProvider(service = TimelineController.class)
public class TimelineControllerImpl implements TimelineController, DynamicModelListener {
    
    private final double EPSILON = 0.000000001;
    
    private TimelineModelImpl m_model;
    private final DynamicController m_dynamicController;
    private ScheduledExecutorService m_playExecutor;
    private final List<TimelineModelListener> m_listeners;
    
    
    public TimelineControllerImpl() {
        m_listeners = new ArrayList<TimelineModelListener>();
        
        m_dynamicController = Lookup.getDefault().lookup(DynamicController.class);
        
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.addWorkspaceListener(new WorkspaceListener() {
            @Override
            public void initialize(Workspace workspace) {
            }

            @Override
            public void select(Workspace workspace) {
                m_model = workspace.getLookup().lookup(TimelineModelImpl.class);
                if (m_model == null) {
                    DynamicModel dynamicModel = m_dynamicController.getModel(workspace);
                    m_model = new TimelineModelImpl(dynamicModel);
                    workspace.add(m_model);
                }
                setup();
            }

            @Override
            public void unselect(Workspace workspace) {
                unsetup();
            }

            @Override
            public void close(Workspace workspace) {
            }

            @Override
            public void disable() {
                m_model = null;
                notifyAllListeners(new TimelineModelEvent(EventType.MODEL_CHANGED, m_model, null));
            }
        });
        
        Workspace currentWorkspace = projectController.getCurrentWorkspace();
        if (currentWorkspace != null) {
            m_model = currentWorkspace.getLookup().lookup(TimelineModelImpl.class);
            if (m_model == null) {
                DynamicModel dynamicModel = m_dynamicController.getModel(currentWorkspace);
                m_model = new TimelineModelImpl(dynamicModel);
                currentWorkspace.add(m_model);
            }
            setup();
        }
    }
    
    private void setup() {        
        // TODO: remove setTimeFormat and setEstimator => see this values and enable/disable panel.
        m_dynamicController.setTimeFormat(DynamicModel.TimeFormat.DOUBLE);
        m_dynamicController.setEstimator(Estimator.LAST);
        m_dynamicController.addModelListener(this);
        notifyAllListeners(new TimelineModelEvent(EventType.MODEL_CHANGED, m_model, null));
    }

    private void unsetup() {
        m_dynamicController.removeModelListener(this);
    }
    
    private void notifyAllListeners(TimelineModelEvent event) {
        for (TimelineModelListener listener: m_listeners.toArray(new TimelineModelListener[0])) {
            listener.timelineModelChanged(event);
        }
    }
    
    @Override
    public void dynamicModelChanged(DynamicModelEvent event) {
        if (event.getEventType().equals(DynamicModelEvent.EventType.MIN_CHANGED) ||
            event.getEventType().equals(DynamicModelEvent.EventType.MAX_CHANGED)) {
            boundsChanged(event);
        } else if (event.getEventType().equals(DynamicModelEvent.EventType.VISIBLE_INTERVAL)) {
            intervalChanged(event);
        } else if (event.getEventType().equals(DynamicModelEvent.EventType.TIME_FORMAT)) {
            timeFormatChanged(event);
        }
    }
    
    private void boundsChanged(DynamicModelEvent event) {
        double min = event.getSource().getMin();
        double max = event.getSource().getMax();
        Interval globalBounds = new Interval(min, max);
        
        updateGlobalBounds(globalBounds);
        updateCustomBounds(m_model.getCustomBounds());
        updatePosition(m_model.getPosition());
    }
    
    private void updateGlobalBounds(Interval newBounds) {
        boolean isOldBoundsValid = m_model.hasValidBounds();
        
        if (!m_model.hasCustomBounds()) {
            m_model.setCustomBounds(newBounds);
        }
        m_model.setGlobalBounds(newBounds);
        double[] eventData = new double[]{ newBounds.getLow(), newBounds.getHigh() };
        notifyAllListeners(new TimelineModelEvent(EventType.GLOBAL_BOUNDS_CHANGED, m_model, eventData));
        
        boolean isBoundsValid = m_model.hasValidBounds();
        if (isBoundsValid != isOldBoundsValid) {
            notifyAllListeners(new TimelineModelEvent(EventType.BOUNDS_VALIDITY_CHANGED, m_model, isBoundsValid));
        }
    }
    
    private void updateCustomBounds(Interval newBounds) {
        if (!m_model.hasValidBounds()) {
            return;
        }
        
        if (newBounds != null) {
            Interval globalBounds = m_model.getGlobalBounds();
            double min = Math.max(newBounds.getLow(), globalBounds.getLow());
            double max = Math.min(newBounds.getHigh(), globalBounds.getHigh());
            newBounds = new Interval(min, max);
            
            Interval oldBounds = m_model.getCustomBounds();
            if (!isEqual(newBounds, oldBounds)) {
                m_model.setCustomBounds(newBounds);
                double[] eventData = new double[]{ newBounds.getLow(), newBounds.getHigh() };
                notifyAllListeners(new TimelineModelEvent(EventType.CUSTOM_BOUNDS_CHANGED, m_model, eventData));
            }
            
        } else {
            //! New bounds are NULL => we must reset custom bounds.
            if (m_model.hasCustomBounds()) {
                newBounds = m_model.getGlobalBounds();
                m_model.setCustomBounds(newBounds);
                double[] eventData = new double[]{ newBounds.getLow(), newBounds.getHigh() };
                notifyAllListeners(new TimelineModelEvent(EventType.CUSTOM_BOUNDS_CHANGED, m_model, eventData));
            }
        }
    }
    
    private void updatePosition(double position) {
        if (!m_model.hasValidBounds()) {
            m_model.setPosition(0.0);
            return;
        }
        
        Interval bounds = m_model.getCustomBounds();
        position = Math.min(Math.max(position, bounds.getLow()), bounds.getHigh());
        
        double oldPosition = m_model.getPosition();
        if (!isEqual(position, oldPosition)) {
            m_model.setPosition(position);
            double delta = EPSILON / 2.0;
            m_dynamicController.setVisibleInterval(position - delta, position + delta);
            notifyAllListeners(new TimelineModelEvent(EventType.POSITION_CHANGED, m_model, new Double(position)));
        }
    }
    
    private boolean isEqual(Interval a, Interval b) {
        return (isEqual(a.getLow(), b.getLow()) && isEqual(a.getHigh(), b.getHigh()));
    }
    
    private boolean isEqual(double a, double b) {
        return (Double.compare(a, b) == 0);
    }
    
    private void intervalChanged(DynamicModelEvent event) {
        double position = ((TimeInterval)event.getData()).getHigh();
        updatePosition(position);
    }
    
    private void timeFormatChanged(DynamicModelEvent event) {
        notifyAllListeners(new TimelineModelEvent(EventType.MODEL_CHANGED, m_model, null));
    }
    
    @Override
    public synchronized TimelineModel getModel() {
        return m_model;
    }

    @Override
    public synchronized TimelineModel getModel(Workspace workspace) {
        return workspace.getLookup().lookup(TimelineModel.class);
    }

    @Override
    public void setCustomBounds(Interval bounds) {
        updateCustomBounds(bounds);
    }
    
    @Override
    public void setPosition(double position) {
        updatePosition(position);
    }
    
    @Override
    public void startPlaying() {
        if (m_model.isPlaying()) {
            return;
        }
        
        m_playExecutor = Executors.newScheduledThreadPool(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable task) {
                    return new Thread(task, "Simple Timeline player");
            }
        });
        m_playExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                stepForward();
                
                Interval bounds = (m_model.hasCustomBounds()) ? (m_model.getCustomBounds()) : (m_model.getGlobalBounds());
                double maxBound = bounds.getHigh();
                double position = m_model.getPosition();
                
                boolean isFinished = (position + EPSILON >= maxBound);
                if (isFinished) {
                    stopPlaying();
                }
                
            }
        }, m_model.getPlaySpeed(), m_model.getPlaySpeed(), TimeUnit.MILLISECONDS);
        
        m_model.setPlaying(true);
        notifyAllListeners(new TimelineModelEvent(EventType.PLAY_STATE_CHANGED, m_model, new Boolean(true)));
    }

    @Override
    public void stopPlaying() {
        if (!m_model.isPlaying()) {
            return;
        }
        
        if (m_playExecutor != null) {
            m_playExecutor.shutdown();
        }
        m_model.setPlaying(false);
        notifyAllListeners(new TimelineModelEvent(EventType.PLAY_STATE_CHANGED, m_model, new Boolean(false)));
    }

    @Override
    public void stepForward() {
        double step = m_model.getPlayStep();
        double position = m_model.getPosition();
        setPosition(position + step);
    }

    @Override
    public void stepBackward() {
        double step = m_model.getPlayStep();
        double position = m_model.getPosition();
        setPosition(position - step);
    }

    @Override
    public void setPlayStep(double stepSize) {
        m_model.setPlayStep(stepSize);
    }

    @Override
    public void setPlaySpeed(int stepDelay) {
        m_model.setPlaySpeed(stepDelay);
    }
    
    @Override
    public synchronized void addListener(TimelineModelListener listener) {
        if (!m_listeners.contains(listener)) {
            m_listeners.add(listener);
        }
    }

    @Override
    public synchronized void removeListener(TimelineModelListener listener) {
        m_listeners.remove(listener);
    }
    
}

