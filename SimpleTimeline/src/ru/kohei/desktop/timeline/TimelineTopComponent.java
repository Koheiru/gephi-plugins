/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.kohei.desktop.timeline;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import org.openide.util.Lookup;
import ru.kohei.timeline.api.TimelineController;
import ru.kohei.timeline.api.TimelineModel;
import ru.kohei.timeline.api.TimelineModelEvent;
import ru.kohei.timeline.api.TimelineModelListener;

/**
 *
 * @author Prostov Yury
 */
public class TimelineTopComponent extends JPanel implements TimelineModelListener {
    
    private transient TimelineDrawer drawer;
    private transient TimelineModel model;
    private transient TimelineController controller;
    
    /**
     * Creates new form TimelineTopComponent
     */
    public TimelineTopComponent() {
        initComponents();
        
        drawer = (TimelineDrawer)timelinePanel;
        
        controller = Lookup.getDefault().lookup(TimelineController.class);
        controller.addListener(this);
        setTimelineModel(controller.getModel());
        
        playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (!model.isPlaying()) {
                    controller.startPlay();
                }
            }
        });
        
        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (model.isPlaying()) {
                    controller.stopPlay();
                }
            }
        });
        
        stepForwardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                controller.stepForward();
            }
        });
        
        stepBackwardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                controller.stepBackward();
            }
        });
        
        rewindForwardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (model != null) {
                    double intervalBegin = model.getIntervalStart();
                    double intervalEnd   = model.getIntervalEnd();
                    double intervalLength = intervalEnd - intervalBegin;
                    double min = model.getCustomMin();
                    controller.setInterval(min, min + intervalLength);
                }
            }
        });
        
        rewindBackwardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (model != null) {
                    double intervalBegin = model.getIntervalStart();
                    double intervalEnd   = model.getIntervalEnd();
                    double intervalLength = intervalEnd - intervalBegin;
                    double max = model.getCustomMax();
                    controller.setInterval(max - intervalLength, max);                    
                }
            }
        });
    }
    
    @Override
    public void timelineModelChanged(TimelineModelEvent event) {
        if (event.getEventType() == TimelineModelEvent.EventType.MODEL) {
            setTimelineModel(event.getSource());
        }
        else if (event.getEventType() == TimelineModelEvent.EventType.ENABLED) {
            updateEnabledState();
        }
        else if (event.getEventType() == TimelineModelEvent.EventType.VALID_BOUNDS) {
            updateEnabledState();
        }
        else if (event.getEventType() == TimelineModelEvent.EventType.PLAY_START) {
            updatePlayingState();
        }
        else if (event.getEventType() == TimelineModelEvent.EventType.PLAY_STOP) {
            updatePlayingState();
        }
        drawer.consumeEvent(event);
    }

    private void setTimelineModel(TimelineModel model)
    {
        this.model = model;
        updateEnabledState();
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TimelineModel model = TimelineTopComponent.this.model;
                drawer.setModel(model);
            }
        });
    }
    
    private void updateEnabledState() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                CardLayout cardLayout = (CardLayout)topContainer.getLayout();
                TimelineModel model = TimelineTopComponent.this.model;
                boolean isModelEnabled = (model != null && model.hasValidBounds());
                if (isModelEnabled) {
                    cardLayout.show(topContainer, "enabledPanel");
                }
                else {
                    cardLayout.show(topContainer, "disabledPanel");
                }
            }
        });
    }
    
    private void updatePlayingState() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                CardLayout cardLayout = (CardLayout)playPanel.getLayout();
                TimelineModel model = TimelineTopComponent.this.model;
                boolean isPlaying = (model != null && model.isPlaying());
                if (isPlaying) {
                    cardLayout.show(playPanel, "pauseButton");
                }
                else {
                    cardLayout.show(playPanel, "playButton");
                }
            }
        });
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        topContainer = new javax.swing.JPanel();
        disabledPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        enabledPanel = new javax.swing.JPanel();
        controlsPanel = new javax.swing.JPanel();
        playPanel = new javax.swing.JPanel();
        playButton = new javax.swing.JButton();
        pauseButton = new javax.swing.JButton();
        stepForwardButton = new javax.swing.JButton();
        rewindForwardButton = new javax.swing.JButton();
        stepBackwardButton = new javax.swing.JButton();
        rewindBackwardButton = new javax.swing.JButton();
        timelinePanel = new TimelineDrawer();

        topContainer.setLayout(new java.awt.CardLayout());

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(TimelineTopComponent.class, "TimelineTopComponent.jLabel2.text")); // NOI18N
        jLabel2.setEnabled(false);

        javax.swing.GroupLayout disabledPanelLayout = new javax.swing.GroupLayout(disabledPanel);
        disabledPanel.setLayout(disabledPanelLayout);
        disabledPanelLayout.setHorizontalGroup(
            disabledPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
        );
        disabledPanelLayout.setVerticalGroup(
            disabledPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
        );

        topContainer.add(disabledPanel, "disabledPanel");

        controlsPanel.setMinimumSize(new java.awt.Dimension(548, 38));
        controlsPanel.setPreferredSize(new java.awt.Dimension(593, 38));
        controlsPanel.setLayout(new java.awt.GridBagLayout());

        playPanel.setLayout(new java.awt.CardLayout());

        playButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ru/kohei/desktop/timeline/resources/play_forward_button.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(playButton, org.openide.util.NbBundle.getMessage(TimelineTopComponent.class, "TimelineTopComponent.playButton.text")); // NOI18N
        playButton.setMinimumSize(new java.awt.Dimension(45, 32));
        playButton.setPreferredSize(new java.awt.Dimension(45, 32));
        playPanel.add(playButton, "playButton");

        pauseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ru/kohei/desktop/timeline/resources/pause_button.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(pauseButton, org.openide.util.NbBundle.getMessage(TimelineTopComponent.class, "TimelineTopComponent.pauseButton.text")); // NOI18N
        pauseButton.setMinimumSize(new java.awt.Dimension(45, 32));
        pauseButton.setPreferredSize(new java.awt.Dimension(45, 32));
        playPanel.add(pauseButton, "pauseButton");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 1);
        controlsPanel.add(playPanel, gridBagConstraints);

        stepForwardButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ru/kohei/desktop/timeline/resources/step_forward_button.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(stepForwardButton, org.openide.util.NbBundle.getMessage(TimelineTopComponent.class, "TimelineTopComponent.text")); // NOI18N
        stepForwardButton.setMinimumSize(new java.awt.Dimension(45, 32));
        stepForwardButton.setName(""); // NOI18N
        stepForwardButton.setPreferredSize(new java.awt.Dimension(45, 32));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 1);
        controlsPanel.add(stepForwardButton, gridBagConstraints);

        rewindForwardButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ru/kohei/desktop/timeline/resources/rewind_forward_button.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(rewindForwardButton, org.openide.util.NbBundle.getMessage(TimelineTopComponent.class, "TimelineTopComponent.rewindForwardButton.text")); // NOI18N
        rewindForwardButton.setMinimumSize(new java.awt.Dimension(45, 32));
        rewindForwardButton.setPreferredSize(new java.awt.Dimension(45, 32));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 1);
        controlsPanel.add(rewindForwardButton, gridBagConstraints);

        stepBackwardButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ru/kohei/desktop/timeline/resources/step_backward_button.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(stepBackwardButton, org.openide.util.NbBundle.getMessage(TimelineTopComponent.class, "TimelineTopComponent.stepBackwardButton.text")); // NOI18N
        stepBackwardButton.setMinimumSize(new java.awt.Dimension(45, 32));
        stepBackwardButton.setPreferredSize(new java.awt.Dimension(45, 32));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 1);
        controlsPanel.add(stepBackwardButton, gridBagConstraints);

        rewindBackwardButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ru/kohei/desktop/timeline/resources/rewind_backward_button.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(rewindBackwardButton, org.openide.util.NbBundle.getMessage(TimelineTopComponent.class, "TimelineTopComponent.rewindBackwardButton.text")); // NOI18N
        rewindBackwardButton.setMinimumSize(new java.awt.Dimension(45, 32));
        rewindBackwardButton.setPreferredSize(new java.awt.Dimension(45, 32));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 1);
        controlsPanel.add(rewindBackwardButton, gridBagConstraints);

        javax.swing.GroupLayout timelinePanelLayout = new javax.swing.GroupLayout(timelinePanel);
        timelinePanel.setLayout(timelinePanelLayout);
        timelinePanelLayout.setHorizontalGroup(
            timelinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        timelinePanelLayout.setVerticalGroup(
            timelinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 61, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout enabledPanelLayout = new javax.swing.GroupLayout(enabledPanel);
        enabledPanel.setLayout(enabledPanelLayout);
        enabledPanelLayout.setHorizontalGroup(
            enabledPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(controlsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
            .addComponent(timelinePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        enabledPanelLayout.setVerticalGroup(
            enabledPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, enabledPanelLayout.createSequentialGroup()
                .addComponent(timelinePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(controlsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        topContainer.add(enabledPanel, "enabledPanel");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(topContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(topContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel controlsPanel;
    private javax.swing.JPanel disabledPanel;
    private javax.swing.JPanel enabledPanel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JButton pauseButton;
    private javax.swing.JButton playButton;
    private javax.swing.JPanel playPanel;
    private javax.swing.JButton rewindBackwardButton;
    private javax.swing.JButton rewindForwardButton;
    private javax.swing.JButton stepBackwardButton;
    private javax.swing.JButton stepForwardButton;
    private transient javax.swing.JPanel timelinePanel;
    private javax.swing.JPanel topContainer;
    // End of variables declaration//GEN-END:variables

}
