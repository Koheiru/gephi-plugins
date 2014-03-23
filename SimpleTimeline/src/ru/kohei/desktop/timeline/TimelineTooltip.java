/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.desktop.timeline;

import java.awt.Point;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JComponent;
import org.gephi.dynamic.api.DynamicModel.TimeFormat;
import ru.kohei.timeline.api.TimelineModel;
import org.gephi.ui.components.richtooltip.RichTooltip;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Prostov Yury
 */
public class TimelineTooltip {

    private static final int DELAY = 500;
    private TimelineModel model;
    private String position;
    private String min;
    private String max;
    private String y;
    private Timer timer;
    private RichTooltip tooltip;
    private Lock lock = new ReentrantLock();

    public void setModel(TimelineModel model) {
        this.model = model;
    }

    public void start(final double currentPosition, final Point mousePosition, final JComponent component) {
        stop();
        if (model == null) {
            return;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                lock.lock();
                try {
                    if (tooltip != null) {
                        tooltip.hideTooltip();
                    }
                    buildData(currentPosition);
                    tooltip = buildTooltip();
                    tooltip.showTooltip(component, mousePosition);
                } finally {
                    lock.unlock();
                }
            }
        }, TimelineTooltip.DELAY);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
        lock.lock();
        try {
            if (tooltip != null) {
                tooltip.hideTooltip();
            }
        } finally {
            lock.unlock();
        }
        timer = null;
        tooltip = null;
    }

    private void buildData(double currentPosition) {
        if (model.getTimeFormat().equals(TimeFormat.DOUBLE)) {
            int exponentMin = (int) Math.round(Math.log10(model.getCustomMin()));

            DecimalFormat decimalFormat = new DecimalFormat();
            decimalFormat.setRoundingMode(RoundingMode.HALF_EVEN);

            if (exponentMin > 0) {
                min = String.valueOf(model.getCustomMin());
                max = String.valueOf(model.getCustomMax());
                position = String.valueOf(currentPosition);
            } else {
                decimalFormat.setMaximumFractionDigits(Math.abs(exponentMin) + 2);
                min = decimalFormat.format(model.getCustomMin());
                max = decimalFormat.format(model.getCustomMax());
                position = decimalFormat.format(currentPosition);
            }
        } else if (model.getTimeFormat().equals(TimeFormat.DATE)) {
            DateTime minDate = new DateTime((long) model.getCustomMin());
            DateTime maxDate = new DateTime((long) model.getCustomMax());
            DateTime posDate = new DateTime((long) currentPosition);

            DateTimeFormatter formatter = ISODateTimeFormat.date();
            min = formatter.print(minDate);
            max = formatter.print(maxDate);
            position = formatter.print(posDate);
        } else {
            DateTime minDate = new DateTime((long) model.getCustomMin());
            DateTime maxDate = new DateTime((long) model.getCustomMax());
            DateTime posDate = new DateTime((long) currentPosition);

            DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
            min = formatter.print(minDate);
            max = formatter.print(maxDate);
            position = formatter.print(posDate);
        }
        
        y = null;
    }

    private RichTooltip buildTooltip() {
        RichTooltip richTooltip = new RichTooltip();

        //Min
        richTooltip.addDescriptionSection(NbBundle.getMessage(TimelineTooltip.class, "TimelineTooltip.min") + ": " + getMin());

        //Max
        richTooltip.addDescriptionSection(NbBundle.getMessage(TimelineTooltip.class, "TimelineTooltip.max") + ": " + getMax());

        //Title
        richTooltip.setTitle(NbBundle.getMessage(TimelineTooltip.class, "TimelineTooltip.position") + ": " + getPosition());

        //Img
        richTooltip.setMainImage(ImageUtilities.loadImage("org/gephi/desktop/timeline/resources/info.png"));

        return richTooltip;
    }

    public String getY() {
        return y;
    }

    public String getPosition() {
        return position;
    }

    public String getMin() {
        return min;
    }

    public String getMax() {
        return max;
    }
}
