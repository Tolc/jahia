/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.usergroup;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaGroup;
import org.jahia.ajax.gwt.client.data.GWTJahiaRole;
import org.jahia.ajax.gwt.client.data.GWTJahiaSite;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;
import org.jahia.ajax.gwt.client.service.JahiaService;
import org.jahia.ajax.gwt.client.service.JahiaServiceAsync;
import org.jahia.ajax.gwt.client.service.UserManagerService;
import org.jahia.ajax.gwt.client.service.UserManagerServiceAsync;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.SearchField;

import java.util.ArrayList;
import java.util.List;

/**
 * User/group selection window.
 * User: toto
 * Date: Nov 5, 2008
 * Time: 2:05:59 PM
 */
public class UserGroupSelect extends Window {
    public static final int VIEW_USERS = 1;
    public static final int VIEW_GROUPS = 2;
    public static final int VIEW_TABS = 3;
    public static final int VIEW_ROLES = 4;
    private JahiaServiceAsync service = JahiaService.App.getInstance();
    private SearchField userSearchField;
    private SearchField groupSearchField;
    private SearchField roleSearchField;
    private ListStore<GWTJahiaSite> sites;
    private String selectedSite;
    private Grid<GWTJahiaUser> userGrid;
    private Grid<GWTJahiaGroup> groupGrid;
    private Grid<GWTJahiaRole> roleGrid;
    private final String aclContext;
    private boolean singleSelectionMode;

    public UserGroupSelect (final UserGroupAdder target, int viewMode, String aclContext) {
        this(target, viewMode, aclContext, false);
    }
    
    public UserGroupSelect (final UserGroupAdder target, int viewMode, String aclContext, boolean singleSelectionMode) {
        this.aclContext = aclContext;
        this.singleSelectionMode = singleSelectionMode;
        setModal(true);
        setSize(500, 500);
        setLayout(new FitLayout());
        final UserManagerServiceAsync service = UserManagerService.App.getInstance();
        final JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();
        switch (viewMode) {
            case VIEW_TABS:
                ContentPanel userPanel = getUserPanel(target, service);

                TabItem userTab = new TabItem("users");
                userTab.setLayout(new FitLayout());
                userTab.add(userPanel);

                ContentPanel groupsPanel = getGroupsPanel(target, service);

                TabItem groupsTab = new TabItem("groups");
                groupsTab.setLayout(new FitLayout());
                groupsTab.add(groupsPanel);

                TabPanel tabs = new TabPanel();
                tabs.add(userTab);
                tabs.add(groupsTab);
                add(tabs);
                break;
            case VIEW_USERS:
                add(getUserPanel(target, service));
                break;
            case VIEW_GROUPS:
                add(getGroupsPanel(target, service));
                break;
            case VIEW_ROLES:
                add(getRolesPanel(target, async));
                break;
        }
        ButtonBar buttons = new ButtonBar() ;
        Button add = new Button("Add", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                if (userGrid != null) {
                    target.addUsers(userGrid.getSelectionModel().getSelectedItems());
                }
                if (groupGrid != null) {
                    target.addGroups(groupGrid.getSelectionModel().getSelectedItems());
                }
                if (roleGrid != null) {
                    target.addRoles(roleGrid.getSelectionModel().getSelectedItems());
                }
                hide();
            }
        }) ;
        buttons.add(add) ;
        Button cancel = new Button("Cancel", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide();
            }
        });
        buttons.add(cancel) ;        
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setTopComponent(buttons);

        show();
    }

    private ContentPanel getUserPanel(final UserGroupAdder target, final UserManagerServiceAsync service) {
        // data proxy
        RpcProxy<PagingLoadResult<GWTJahiaUser>> proxy = new RpcProxy<PagingLoadResult<GWTJahiaUser>>() {
            @Override
            protected void load(Object pageLoaderConfig, AsyncCallback<PagingLoadResult<GWTJahiaUser>> callback) {
                String context = aclContext;
                if ("siteSelector".equals(aclContext)) {
                    context = "site:"+selectedSite;
                }
                if (context != null) {
                    if (userSearchField.getText().length()==0)  {
                        service.searchUsersInContext("*",((PagingLoadConfig) pageLoaderConfig).getOffset(), ((PagingLoadConfig) pageLoaderConfig).getLimit(),context, callback);
                    } else {
                        service.searchUsersInContext("*"+userSearchField.getText()+"*",((PagingLoadConfig) pageLoaderConfig).getOffset(), ((PagingLoadConfig) pageLoaderConfig).getLimit(), context, callback);
                    }
                }
            }
        };
        final BasePagingLoader loader = new BasePagingLoader<PagingLoadResult<GWTJahiaUser>>(proxy);
        userSearchField = new SearchField("Search: ", false) {
            public void onFieldValidation(String value) {
                loader.load();
            }

            public void onSaveButtonClicked(String value) {

            }
        };
        userSearchField.setWidth(250) ;
        loader.setLimit(15);
        loader.load();
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(userSearchField);
        if("siteSelector".equals(aclContext) ){
            ComboBox<GWTJahiaSite> siteMenu = createMenu(loader);
            panel.add(siteMenu);
        }

        ListStore<GWTJahiaUser> store = new ListStore<GWTJahiaUser>(loader);

        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        columns.add(new ColumnConfig("userName", "User name", 120));
        columns.add(new ColumnConfig("lastname", "Last name", 140));
        columns.add(new ColumnConfig("firstname", "First name", 140));
//        columns.add(new ColumnConfig("siteName", "Site name", 80));
        columns.add(new ColumnConfig("provider", "Provider", 80));
//        columns.add(new ColumnConfig("email", "Email", 100));

        ColumnModel cm = new ColumnModel(columns);

        final PagingToolBar toolBar = new PagingToolBar(15);
        toolBar.bind(loader);

        userGrid = new Grid<GWTJahiaUser>(store, cm);
        if (singleSelectionMode) {
            userGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }
        userGrid.setLoadMask(true);
        userGrid.setBorders(true);
        userGrid.addListener(Events.RowDoubleClick, new Listener<GridEvent>() {
            public void handleEvent(GridEvent event) {
                target.addUsers(userGrid.getSelectionModel().getSelectedItems());
                if (singleSelectionMode) {
                    hide();
                }
            }
        });

        ContentPanel userPanel = new ContentPanel();
        userPanel.setButtonAlign(Style.HorizontalAlignment.CENTER);
        userPanel.setIconStyle("icon-table");
        userPanel.setHeading("Select a user");
        userPanel.setLayout(new FitLayout());
        userPanel.add(userGrid);
        userPanel.setSize(480, 350);
        userPanel.setBottomComponent(toolBar);
        userPanel.setTopComponent(panel);
        return userPanel;
    }


    private ContentPanel getGroupsPanel(final UserGroupAdder target, final UserManagerServiceAsync service) {
        // data proxy
        RpcProxy<PagingLoadResult<GWTJahiaGroup>> proxy = new RpcProxy<PagingLoadResult<GWTJahiaGroup>>() {
            @Override
            protected void load(Object pageLoaderConfig, AsyncCallback<PagingLoadResult<GWTJahiaGroup>> callback) {
                String context = aclContext;
                if ("siteSelector".equals(aclContext)) {
                    context = "site:"+selectedSite;
                }

                if (groupSearchField.getText().length()==0)  {
                    service.searchGroupsInContext("*",((PagingLoadConfig) pageLoaderConfig).getOffset(), ((PagingLoadConfig) pageLoaderConfig).getLimit(),context, callback);
                } else {
                    service.searchGroupsInContext("*"+groupSearchField.getText()+"*",((PagingLoadConfig) pageLoaderConfig).getOffset(), ((PagingLoadConfig) pageLoaderConfig).getLimit(), context, callback);
                }
            }
        };
        final BasePagingLoader loader = new BasePagingLoader<PagingLoadResult<GWTJahiaGroup>>(proxy);

        groupSearchField = new SearchField("Search: ", false) {
            public void onFieldValidation(String value) {
                loader.load();
            }

            public void onSaveButtonClicked(String value) {

            }
        };
        groupSearchField.setWidth(250) ;

        loader.setLimit(15);
        loader.load();
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(groupSearchField);
        if("siteSelector".equals(aclContext) ){
            ComboBox<GWTJahiaSite> siteMenu = createMenu(loader);
            panel.add(siteMenu);
        }
        ListStore<GWTJahiaGroup> store = new ListStore<GWTJahiaGroup>(loader);

        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        columns.add(new ColumnConfig("groupname", "Group name", 240));
        columns.add(new ColumnConfig("siteName", "Site name", 120));
        columns.add(new ColumnConfig("provider", "Provider", 120));

        ColumnModel cm = new ColumnModel(columns);

        final PagingToolBar toolBar = new PagingToolBar(15);
        toolBar.bind(loader);

        groupGrid = new Grid<GWTJahiaGroup>(store, cm);
        if (singleSelectionMode) {
            groupGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }
        groupGrid.setLoadMask(true);
        groupGrid.setBorders(true);
        groupGrid.addListener(Events.RowDoubleClick, new Listener<GridEvent>() {
            public void handleEvent(GridEvent event) {
                target.addGroups(groupGrid.getSelectionModel().getSelectedItems());
                if (singleSelectionMode) {
                    hide();
                }
            }
        });

        ContentPanel groupsPanel = new ContentPanel();
        groupsPanel.setButtonAlign(Style.HorizontalAlignment.CENTER);
        groupsPanel.setIconStyle("icon-table");
        groupsPanel.setHeading("Select a group");
        groupsPanel.setLayout(new FitLayout());
        groupsPanel.add(groupGrid);
        groupsPanel.setSize(480, 350);
        groupsPanel.setBottomComponent(toolBar);
        groupsPanel.setTopComponent(panel);
        return groupsPanel;
    }

    protected ComboBox<GWTJahiaSite> createMenu (final BasePagingLoader loader) {
        if ("siteSelector".equals(aclContext)) {

            sites = new ListStore<GWTJahiaSite>();

            final ComboBox<GWTJahiaSite> siteMenu = new ComboBox<GWTJahiaSite>();
            siteMenu.setEmptyText("Select a site...");
            siteMenu.setDisplayField("siteKey");
            siteMenu.setStore(sites);
            siteMenu.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaSite>() {
            	public void selectionChanged(SelectionChangedEvent<GWTJahiaSite> se) {
				    selectedSite = se.getSelectedItem().getSiteKey();
                    loader.load();
                }
            });
            
            service.getAvailableSites(new BaseAsyncCallback<List<GWTJahiaSite>>() {
                public void onSuccess (List<GWTJahiaSite> gwtJahiaSites) {
                    sites.add(gwtJahiaSites);
                    if (gwtJahiaSites.size() > 0) {
                        siteMenu.setValue(gwtJahiaSites.get(0));
                    }

                }
            });
            return siteMenu;
        }
        return null;
    }

    private ContentPanel getRolesPanel(final UserGroupAdder target, final JahiaContentManagementServiceAsync service) {
        // data proxy
        RpcProxy<PagingLoadResult<GWTJahiaRole>> proxy = new RpcProxy<PagingLoadResult<GWTJahiaRole>>() {
            @Override
            protected void load(Object pageLoaderConfig, AsyncCallback<PagingLoadResult<GWTJahiaRole>> callback) {
                String context = aclContext;
                if ("siteSelector".equals(aclContext)) {
                    context = "site:"+selectedSite;
                }

                if (roleSearchField.getText().length()==0)  {
                    service.searchRolesInContext("*",((PagingLoadConfig) pageLoaderConfig).getOffset(), ((PagingLoadConfig) pageLoaderConfig).getLimit(),context, callback);
                } else {
                    service.searchRolesInContext(".*"+roleSearchField.getText()+".*",((PagingLoadConfig) pageLoaderConfig).getOffset(), ((PagingLoadConfig) pageLoaderConfig).getLimit(), context, callback);
                }
            }
        };
        final BasePagingLoader loader = new BasePagingLoader<PagingLoadResult<GWTJahiaRole>>(proxy);

        roleSearchField = new SearchField("Search: ", false) {
            public void onFieldValidation(String value) {
                loader.load();
            }

            public void onSaveButtonClicked(String value) {

            }
        };
        roleSearchField.setWidth(250) ;

        loader.setLimit(15);
        loader.load();
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(roleSearchField);
        if("siteSelector".equals(aclContext) ){
            ComboBox<GWTJahiaSite> siteMenu = createMenu(loader);
            panel.add(siteMenu);
        }
        ListStore<GWTJahiaRole> store = new ListStore<GWTJahiaRole>(loader);

        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        columns.add(new ColumnConfig("name", "Role name", 240));
        columns.add(new ColumnConfig("site", "Site name", 120));

        ColumnModel cm = new ColumnModel(columns);

        final PagingToolBar toolBar = new PagingToolBar(15);
        toolBar.bind(loader);

        roleGrid = new Grid<GWTJahiaRole>(store, cm);
        if (singleSelectionMode) {
            roleGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }
        roleGrid.setLoadMask(true);
        roleGrid.setBorders(true);
        roleGrid.addListener(Events.RowDoubleClick, new Listener<GridEvent>() {
            public void handleEvent(GridEvent event) {
                target.addRoles(roleGrid.getSelectionModel().getSelectedItems());
                if (singleSelectionMode) {
                    hide();
                }
            }
        });

        ContentPanel groupsPanel = new ContentPanel();
        groupsPanel.setButtonAlign(Style.HorizontalAlignment.CENTER);
        groupsPanel.setIconStyle("icon-table");
        groupsPanel.setHeading("Select a group");
        groupsPanel.setLayout(new FitLayout());
        groupsPanel.add(roleGrid);
        groupsPanel.setSize(480, 350);
        groupsPanel.setBottomComponent(toolBar);
        groupsPanel.setTopComponent(panel);
        return groupsPanel;
    }
}
