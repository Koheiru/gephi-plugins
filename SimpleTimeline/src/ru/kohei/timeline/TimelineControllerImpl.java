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
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.Interval;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModelEvent;
import org.gephi.dynamic.api.DynamicModelListener;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import ru.kohei.timeline.api.TimelineModel.PlayMode;
import ru.kohei.timeline.api.*;


/**
 *
 * @author Prostov Yury
 */
@ServiceProvider(service = TimelineController.class)
public class TimelineControllerImpl implements TimelineController, DynamicModelListener {

    private final List<TimelineModelListener> listeners;
    private TimelineModelImpl model;
    private final DynamicController dynamicController;
    private AttributeModel attributeModel;
    private ScheduledExecutorService playExecutor;

    public TimelineControllerImpl() {
        listeners = new ArrayList<TimelineModelListener>();

        //Workspace events
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        dynamicController = Lookup.getDefault().lookup(DynamicController.class);

        pc.addWorkspaceListener(new WorkspaceListener() {

            @Override
            public void initialize(Workspace workspace) {
            }

            @Override
            public void select(Workspace workspace) {
                model = workspace.getLookup().lookup(TimelineModelImpl.class);
                if (model == null) {
                    model = new TimelineModelImpl(dynamicController.getModel(workspace));
                    workspace.add(model);
                }
                attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel(workspace);
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
                model = null;
                attributeModel = null;
                fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.MODEL, null, null));
            }
        });

        if (pc.getCurrentWorkspace() != null) {
            model = pc.getCurrentWorkspace().getLookup().lookup(TimelineModelImpl.class);
            if (model == null) {
                model = new TimelineModelImpl(dynamicController.getModel(pc.getCurrentWorkspace()));
                pc.getCurrentWorkspace().add(model);
            }
            attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel(pc.getCurrentWorkspace());
            setup();
        }
    }

    @Override
    public synchronized TimelineModel getModel(Workspace workspace) {
        return workspace.getLookup().lookup(TimelineModel.class);
    }

    @Override
    public synchronized TimelineModel getModel() {
        return model;
    }

    private void setup() {
        fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.MODEL, model, null));

        dynamicController.addModelListener(this);
    }

    private void unsetup() {
        dynamicController.removeModelListener(this);
    }

    @Override
    public void dynamicModelChanged(DynamicModelEvent event) {
        if (event.getEventType().equals(DynamicModelEvent.EventType.MIN_CHANGED)
                || event.getEventType().equals(DynamicModelEvent.EventType.MAX_CHANGED)) {
            double newMax = event.getSource().getMax();
            double newMin = event.getSource().getMin();
            setMinMax(newMin, newMax);
        } else if (event.getEventType().equals(DynamicModelEvent.EventType.VISIBLE_INTERVAL)) {
            TimeInterval timeInterval = (TimeInterval) event.getData();
            double min = timeInterval.getLow();
            double max = timeInterval.getHigh();
            fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.INTERVAL, model, new double[]{min, max}));
        } else if (event.getEventType().equals(DynamicModelEvent.EventType.TIME_FORMAT)) {
            fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.MODEL, model, null)); //refresh display
        }
    }

    private boolean setMinMax(double min, double max) {
        if (model != null) {
            if (min > max) {
                throw new IllegalArgumentException("min should be less than max");
            } else if (min == max) {
                //Avoid setting values at this point
                return false;
            }
            double previousBoundsMin = model.getCustomMin();
            double previousBoundsMax = model.getCustomMax();

            //Custom bounds
            if (model.getCustomMin() == model.getPreviousMin()) {
                model.setCustomMin(min);
            } else if (model.getCustomMin() < min) {
                model.setCustomMin(min);
            }
            if (model.getCustomMax() == model.getPreviousMax()) {
                model.setCustomMax(max);
            } else if (model.getCustomMax() > max) {
                model.setCustomMax(max);
            }

            model.setPreviousMin(min);
            model.setPreviousMax(max);

            if (model.hasValidBounds()) {
                fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.MIN_MAX, model, new double[]{min, max}));

                if (model.getCustomMax() != max || model.getCustomMin() != min) {
                    fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.CUSTOM_BOUNDS, model, new double[]{min, max}));
                }
            }

            if ((Double.isInfinite(previousBoundsMax) || Double.isInfinite(previousBoundsMin)) && model.hasValidBounds()) {
                fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.VALID_BOUNDS, model, true));
            } else if (!Double.isInfinite(previousBoundsMax) && !Double.isInfinite(previousBoundsMin) && !model.hasValidBounds()) {
                fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.VALID_BOUNDS, model, false));
            }

            return true;
        }

        return false;
    }

    @Override
    public void setCustomBounds(double min, double max) {
        if (model != null) {
            if (model.getCustomMin() != min || model.getCustomMax() != max) {
                if (min >= max) {
                    throw new IllegalArgumentException("min should be less than max");
                }
                if (min < model.getMin() || max > model.getMax()) {
                    throw new IllegalArgumentException("Min and max should be in the bounds");
                }

                //Interval
                if (model.getIntervalStart() < min || model.getIntervalEnd() > max) {
                    dynamicController.setVisibleInterval(min, max);
                }

                //Custom bounds
                double[] val = new double[]{min, max};
                model.setCustomMin(min);
                model.setCustomMax(max);
                fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.CUSTOM_BOUNDS, model, val));
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (model != null) {
            if (enabled != model.isEnabled() && model.hasValidBounds()) {
                model.setEnabled(enabled);
                fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.ENABLED, model, enabled));
            }
            if (!enabled) {
                //Disable filtering
                dynamicController.setVisibleInterval(new TimeInterval());
            }
        }
    }

    @Override
    public void setInterval(double from, double to) {
        if (model != null) {
            if (model.getIntervalStart() != from || model.getIntervalEnd() != to) {
                if (from >= to) {
                    throw new IllegalArgumentException("from should be less than to");
                }
                if (from < model.getCustomMin() || to > model.getCustomMax()) {
                    throw new IllegalArgumentException("From and to should be in the bounds");
                }
                dynamicController.setVisibleInterval(from, to);
            }
        }
    }

    @Override
    public AttributeColumn[] getDynamicGraphColumns() {
        if (attributeModel != null) {
            List<AttributeColumn> columns = new ArrayList<AttributeColumn>();
            AttributeUtils utils = AttributeUtils.getDefault();
            for (AttributeColumn col : attributeModel.getGraphTable().getColumns()) {
                if (utils.isDynamicNumberColumn(col)) {
                    columns.add(col);
                }
            }
            return columns.toArray(new AttributeColumn[0]);
        }
        return new AttributeColumn[0];
    }

    protected void fireTimelineModelEvent(TimelineModelEvent event) {
        for (TimelineModelListener listener : listeners.toArray(new TimelineModelListener[0])) {
            listener.timelineModelChanged(event);
        }
    }

    @Override
    public synchronized void addListener(TimelineModelListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public synchronized void removeListener(TimelineModelListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void startPlay() {
        if (model != null && !model.isPlaying()) {
            model.setPlaying(true);
            playExecutor = Executors.newScheduledThreadPool(1, new ThreadFactory() {

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "Timeline animator");
                }
            });
            playExecutor.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    stepForward();
                    
                    boolean isFinished = false;
                    boolean isIncreasingInterval = (model.getPlayStep() > 0);
                    if (isIncreasingInterval) {
                        double bound = model.getCustomMax();
                        double position = model.getIntervalEnd();
                        double epsilon = 0.000000001;
                        isFinished = (position - bound + epsilon > 0.0);
                    } else {
                        double bound = model.getCustomMin();
                        double position = model.getIntervalStart();
                        double epsilon = 0.000000001;
                        isFinished = (position - bound - epsilon < 0.0);                        
                    }
                    
                    if (isFinished) {
                        stopPlay();
                    }
                }
            }, model.getPlayDelay(), model.getPlayDelay(), TimeUnit.MILLISECONDS);
            fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.PLAY_START, model, null));
        }
    }

    @Override
    public void stopPlay() {
        if (model != null && model.isPlaying()) {
            model.setPlaying(false);
            fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.PLAY_STOP, model, null));
        }
        if (playExecutor != null) {
            playExecutor.shutdown();
        }
    }

    private double getStepValue() {
        double min = model.getCustomMin();
        double max = model.getCustomMax();
        double duration = max - min;
        double step = (duration * model.getPlayStep());
        return step;
    }
    
    @Override
    public void stepForward() {
        double step = getStepValue();
        double intervalStart  = model.getIntervalStart();
        double intervalEnd    = model.getIntervalEnd();
        
        boolean isIncreasingInterval = (step > 0);
        boolean isBothBoundsMoving = (model.getPlayMode() == PlayMode.TWO_BOUNDS);
        if (isIncreasingInterval) {
            double max = model.getCustomMax();
            intervalEnd = Math.min(intervalEnd + step, max);
            if (isBothBoundsMoving) {
                double epsilon = 0.000000001;
                intervalStart = Math.min(intervalStart + step, max - epsilon);
            }
        } else {
            //! Variable 'step' have negative value.
            double min = model.getCustomMin();
            intervalStart = Math.max(intervalStart + step, min);
            if (isBothBoundsMoving) {
                double epsilon = 0.000000001;
                intervalEnd = Math.max(intervalEnd + step, min + epsilon);
            }     
        }
        
        setInterval(intervalStart, intervalEnd);
    }
    
    @Override
    public void stepBackward() {
        double step = getStepValue();
        double intervalStart  = model.getIntervalStart();
        double intervalEnd    = model.getIntervalEnd();
        
        boolean isIncreasingInterval = (step > 0);
        boolean isBothBoundsMoving = (model.getPlayMode() == PlayMode.TWO_BOUNDS);
        if (isIncreasingInterval) {
            if (isBothBoundsMoving) {
                double min = model.getCustomMin();
                double epsilon = 0.000000001;                
                intervalStart = Math.max(intervalStart - step, min);
                intervalEnd = Math.max(intervalEnd - step, min + epsilon);
            } else {
                double epsilon = 0.000000001;
                intervalEnd = Math.max(intervalEnd - step, intervalStart + epsilon);
            }
        } else {
            //! Variable 'step' have negative value.
            if (isBothBoundsMoving) {
                double max = model.getCustomMax();
                double epsilon = 0.000000001;
                intervalStart = Math.min(intervalStart - step, max - epsilon);
                intervalEnd = Math.min(intervalEnd - step, max);
            } else {
                double epsilon = 0.000000001;
                intervalStart = Math.min(intervalStart - step, intervalEnd - epsilon);
            }
        }
        
        setInterval(intervalStart, intervalEnd);        
    }
    
    @Override
    public void setPlaySpeed(int delay) {
        if (model != null) {
            model.setPlayDelay(delay);
        }
    }

    @Override
    public void setPlayStep(double step) {
        if (model != null) {
            model.setPlayStep(step);
        }
    }

    @Override
    public void setPlayMode(PlayMode playMode) {
        if (model != null) {
            model.setPlayMode(playMode);
        }
    }
}

