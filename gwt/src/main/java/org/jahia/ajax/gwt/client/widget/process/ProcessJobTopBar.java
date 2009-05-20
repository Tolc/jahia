/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.process;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.service.process.ProcessDisplayService;
import org.jahia.ajax.gwt.client.data.GWTJahiaProcessJob;
import org.jahia.ajax.gwt.client.data.process.GWTJahiaProcessJobPreference;
import org.jahia.ajax.gwt.client.data.process.GWTJahiaProcessJobStat;
import org.jahia.ajax.gwt.client.widget.tripanel.TopBar;

/**
 * User: jahia
 * Date: 28 juil. 2008
 * Time: 10:46:41
 */
public class ProcessJobTopBar extends TopBar {
    private GWTJahiaProcessJob selectedGWTJahiaProcessJob;
    private ToolBar m_compent;
    private TextToolItem infoItem;
    private TextToolItem deleteItem;

    public ProcessJobTopBar() {
    }


    public void createUI() {
        m_compent = new ToolBar();
        m_compent.setHeight(21);
        // refresh button
        TextToolItem refreshItem = new TextToolItem();
        refreshItem.setIconStyle("gwt-pdisplay-icons-refresh");
        refreshItem.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                getLinker().refreshTable();
            }
        });

        // refresh button
        deleteItem = new TextToolItem("Delete waiting job");
        deleteItem.setEnabled(false);
        deleteItem.setIconStyle("gwt-pdisplay-icons-delete");
        deleteItem.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                ProcessDisplayService.App.getInstance().deleteJob(selectedGWTJahiaProcessJob, new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                        Log.error("Unable to delete waiting job");
                    }

                    public void onSuccess(Object o) {

                    }
                });
            }
        });
        // preference
        TextToolItem prefItem = new TextToolItem();
        prefItem.setText("Preferences");
        prefItem.setIconStyle("gwt-pdisplay-icons-preferences");
        prefItem.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                final Window preferenceWindow = new Window();
                preferenceWindow.setHeading("Preferences");
                preferenceWindow.setBodyBorder(false);
                preferenceWindow.setInsetBorder(false);
                preferenceWindow.setWidth(550);
                preferenceWindow.add(createFromPanelPreference(preferenceWindow));
                preferenceWindow.show();
            }
        });

        //info
        infoItem = new TextToolItem();
        infoItem.setIconStyle("gwt-pdisplay-icons-info");
        infoItem.setText("...");

        // init refresh time
        initTimer();

        m_compent.add(refreshItem);
        m_compent.add(new SeparatorToolItem());
        m_compent.add(infoItem);
        m_compent.add(new SeparatorToolItem());
        m_compent.add(deleteItem);
        m_compent.add(new SeparatorToolItem());
        m_compent.add(prefItem);

    }

    /**
     * Get the UI component used by the subclass since it is not directly a subclass of a widget
     * (multiple inheritance is not supported in Java, damn).
     *
     * @return the ui component
     */
    public Component getComponent() {
        return m_compent;
    }

    public GWTJahiaProcessJob getSelectedGWTJahiaProcessJob() {
        return selectedGWTJahiaProcessJob;
    }

    public void setSelectedGWTJahiaProcessJob(GWTJahiaProcessJob selectedGWTJahiaProcessJob) {
        this.selectedGWTJahiaProcessJob = selectedGWTJahiaProcessJob;
        if (selectedGWTJahiaProcessJob.getJobType().equalsIgnoreCase("waiting")) {
            deleteItem.setEnabled(true);
        } else {
            deleteItem.setEnabled(false);
        }
    }

    /**
     * Handle new selection
     *
     * @param leftTreeSelection
     * @param topTableSelection
     */
    public void handleNewSelection(Object leftTreeSelection, Object topTableSelection) {
        GWTJahiaProcessJob jahiaProcessJob = (GWTJahiaProcessJob) topTableSelection;
        if (jahiaProcessJob != null && jahiaProcessJob.getJobType().equalsIgnoreCase("waiting")) {
            deleteItem.setEnabled(true);
        } else {
            deleteItem.setEnabled(false);
        }
    }

    /**
     * Get Process Job Preference
     *
     * @return
     */
    private GWTJahiaProcessJobPreference getGWTJahiaProcessJobPreference() {
        return getPdisplayBrowserLinker().getGwtJahiaProcessJobPreference();
    }


    /**
     * Get PdisplayBrowserLinker
     *
     * @return
     */
    private ProcessdisplayBrowserLinker getPdisplayBrowserLinker() {
        return ((ProcessdisplayBrowserLinker) getLinker());
    }

    /**
     * init timer
     */
    private void initTimer() {
        Timer timer = new Timer() {
            public void run() {
                ProcessDisplayService.App.getInstance().getGWTProcessJobStat(GWTJahiaProcessJobStat.TIMER_MODE, new AsyncCallback<GWTJahiaProcessJobStat>() {
                    public void onFailure(Throwable throwable) {
                        infoItem.setIconStyle("gwt-pdisplay-icons-error");
                    }

                    public void onSuccess(GWTJahiaProcessJobStat gwtJahiaProcessJobStat) {
                        if (gwtJahiaProcessJobStat != null) {
                            // handle refresh
                            Log.debug("Need Refresh --> " + gwtJahiaProcessJobStat.isNeedRefresh());
                            if (gwtJahiaProcessJobStat.isNeedRefresh()) {
                                if (getGWTJahiaProcessJobPreference().isAutoRefresh()) {
                                    getLinker().refreshTable();
                                } else {
                                    infoItem.setIconStyle("gwt-pdisplay-icons-warning");
                                    infoItem.setToolTip("Need refresh");
                                    infoItem.addSelectionListener(new SelectionListener<ComponentEvent>() {
                                        public void componentSelected(ComponentEvent event) {
                                            getLinker().refreshTable();
                                        }
                                    });
                                }
                            } else {
                                infoItem.setIconStyle("gwt-pdisplay-icons-info");
                            }
                            infoItem.setText(gwtJahiaProcessJobStat.getLastJobCompletedTime());
                        }


                    }
                });
            }
        };

        // Schedule the timer to run each "timerRefresh/1000" seconds.
        timer.scheduleRepeating(5000);
    }

    /**
     * Create a Form Panel
     *
     * @return
     */
    private ContentPanel createFromPanelPreference(final Window window) {
        FormPanel panel = new FormPanel();
        panel.setBodyBorder(false);
        panel.setBorders(false);
        panel.setFrame(false);
        panel.setHeaderVisible(false);
        panel.setButtonAlign(Style.HorizontalAlignment.CENTER);
        panel.setStyleAttribute("padding", "4px 4px");
        panel.setLabelWidth(170);
        panel.setFieldWidth(250);
        panel.setWidth(500);

        // refesh
        final CheckBox autoRefreshField = new CheckBox();
        autoRefreshField.setFieldLabel("Auto refresh");
        autoRefreshField.setValue(getGWTJahiaProcessJobPreference().isAutoRefresh());
        panel.add(autoRefreshField);

        // max job
        final NumberField maxJobNumberField = new NumberField();
        maxJobNumberField.setFieldLabel("Max. jobs");
        maxJobNumberField.setValue(getGWTJahiaProcessJobPreference().getMaxJobs());
        maxJobNumberField.setAllowBlank(false);
        maxJobNumberField.setAllowNegative(false);
        panel.add(maxJobNumberField);

        // jobs per page
        final NumberField jobPerPageNumber = new NumberField();
        jobPerPageNumber.setFieldLabel("Jobs per page");
        jobPerPageNumber.setValue(getGWTJahiaProcessJobPreference().getJobsPerPage());
        jobPerPageNumber.setAllowBlank(false);
        jobPerPageNumber.setAllowNegative(false);
        panel.add(jobPerPageNumber);


        final Button saveButton = new Button("Save");
        saveButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                Log.debug(" save pdisplay pref.");

                boolean autoRefresh = autoRefreshField.getValue().booleanValue();
                Log.debug(" auto refresh value: " + autoRefresh);
                int maxJobs = maxJobNumberField.getValue().intValue();
                Log.debug(" max job value: " + maxJobs);
                int jobPerPage = jobPerPageNumber.getValue().intValue();
                Log.debug(" job Per Page value: " + jobPerPage);

                // creat pref. bean and save it
                final GWTJahiaProcessJobPreference gwtJahiaProcessJobPreferences = new GWTJahiaProcessJobPreference();
                gwtJahiaProcessJobPreferences.setDataType(GWTJahiaProcessJobPreference.PREF_GENERAL);
                gwtJahiaProcessJobPreferences.setAutoRefresh(autoRefresh);
                gwtJahiaProcessJobPreferences.setMaxJobs(maxJobs);
                gwtJahiaProcessJobPreferences.setJobsPerPage(jobPerPage);
                gwtJahiaProcessJobPreferences.setRefreshAtEndOfAnyPageWorkflow(true);

                // make an ajax call to save preferences
                ProcessDisplayService.App.getInstance().savePreferences(gwtJahiaProcessJobPreferences, new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                        window.hide();
                    }

                    public void onSuccess(Object o) {
                        getPdisplayBrowserLinker().refreshPreferenceAndTable();
                        window.hide();
                    }
                });
            }
        });
        panel.addButton(saveButton);
        return panel;

    }


}
