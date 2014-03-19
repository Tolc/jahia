/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.TreeGridDragSource;
import com.extjs.gxt.ui.client.dnd.TreeGridDropTarget;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTRepository;
import org.jahia.ajax.gwt.client.util.Collator;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;
import org.jahia.ajax.gwt.client.widget.node.GWTJahiaNodeTreeFactory;

import java.util.*;

/**
 * User: rfelden
 * Date: 28 nov. 2008 - 10:09:32
 */
public class RepositoryTab extends ContentPanel {
    private GWTRepository repository;
    private TreeLoader<GWTJahiaNode> loader;
    private TreeStore<GWTJahiaNode> store;
    private ContentRepositoryTabs folderTreeContainer;
    private TreeGrid<GWTJahiaNode> m_tree;
    private GWTJahiaNodeTreeFactory factory;
    private GWTManagerConfiguration config;
    
    /**
     * Constructor
     *
     * @param container the parent container
     * @param repo      the repository type (see constants)
     * @param config    the configuration to use
     */
    public RepositoryTab(ContentRepositoryTabs container, GWTRepository repo, final List<String> selectedPaths, final GWTManagerConfiguration config) {
        super(new FitLayout());
        setBorders(false);
        setBodyBorder(false);
        getHeader().setBorders(false);
        folderTreeContainer = container;
        repository = repo;
        getHeader().setIcon(ToolbarIconProvider.getInstance().getIcon(repo.getKey()));

        // tree component
        factory = new GWTJahiaNodeTreeFactory(repository.getPaths());
        factory.setNodeTypes(config.getFolderTypes());
        factory.setMimeTypes(config.getMimeTypes());
        factory.setFilters(config.getFilters());

        List<String> keys = new ArrayList<String>(config.getTreeColumnKeys());
        keys.add(GWTJahiaNode.PUBLICATION_INFO);
        keys.add(GWTJahiaNode.SUBNODES_CONSTRAINTS_INFO);
        factory.setFields(keys);

        factory.setSelectedPath(selectedPaths);
        factory.setHiddenTypes(config.getHiddenTypes());
        factory.setHiddenRegex(config.getHiddenRegex());
        factory.setShowOnlyNodesWithTemplates(config.isShowOnlyNodesWithTemplates());
        loader = factory.getLoader();
        store = factory.getStore();
        
        store.setStoreSorter(new StoreSorter<GWTJahiaNode>(new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                GWTJahiaNode s1 = (GWTJahiaNode) o1;
                GWTJahiaNode s2 = (GWTJahiaNode) o2;
                String key = config.getTreeColumnKeys().get(0);
                Object val1 = s1.get(key);
                Object val2 = s2.get(key);
                return Collator.getInstance().localeCompare(val1.toString(), val2.toString());
            }
        }));

        NodeColumnConfigList columns = new NodeColumnConfigList(config.getTreeColumns());
        columns.init();
        columns.get(0).setRenderer(NodeColumnConfigList.NAME_TREEGRID_RENDERER);
        m_tree = factory.getTreeGrid(new ColumnModel(columns));
        m_tree.setHideHeaders(true);
        m_tree.setIconProvider(ContentModelIconProvider.getInstance());
        if (columns.getAutoExpand() != null) {
            m_tree.setAutoExpandColumn(columns.getAutoExpand());
        }

//        m_tree.setDisplayProperty("displayName");
        m_tree.setBorders(false);
        m_tree.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            public void selectionChanged(SelectionChangedEvent selectionChangedEvent) {
                getLinker().onTreeItemSelected();
                if (selectionChangedEvent.getSelection() != null && selectionChangedEvent.getSelection().size() > 0) {
                    setExpanded(true);
                }
            }
        });

        setScrollMode(Style.Scroll.NONE);
        setHeadingHtml(repo.getTitle());
        getHeader().addTool(new ToolButton("x-tool-refresh", new SelectionListener<IconButtonEvent>() {
            public void componentSelected(IconButtonEvent event) {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put(Linker.REFRESH_ALL, true);
                refresh(data);
            }
        }));
        add(m_tree);
        
        this.config = config;
    }

    protected boolean isNodeTypeAllowed(GWTJahiaNode selectedNode) {
        if (selectedNode == null) {
            return true;
        }
        return (config.getForbiddenNodeTypesForDragAndDrop() == null || !selectedNode.isNodeType(config.getForbiddenNodeTypesForDragAndDrop()))
                && (config.getAllowedNodeTypesForDragAndDrop() == null || selectedNode.isNodeType(config.getAllowedNodeTypesForDragAndDrop()));

    }

    /**
     * init
     */
    public void init() {
        factory.setDisplayHiddenTypes(getLinker() != null && getLinker().isDisplayHiddenTypes());
        loader.load();

        if (getLinker().getDndListener() != null) {
            TreeGridDragSource source = new TreeGridDragSource(m_tree) {
                @Override
                protected void onDragStart(DNDEvent e) {
                    super.onDragStart(e);
                    List<BaseTreeModel> l = e.getData();
                    List<GWTJahiaNode> r = new ArrayList<GWTJahiaNode>();
                    for (BaseTreeModel model : l) {
                        GWTJahiaNode jahiaNode = (GWTJahiaNode) model.get("model");
                        if (!isNodeTypeAllowed(jahiaNode) || !PermissionsUtils.isPermitted("jcr:removeNode", jahiaNode.getPermissions())) {
                            e.setCancelled(true);
                            break;
                        }
                        r.add(jahiaNode);
                    }
                    e.setData(r);
                }
            };
            source.addDNDListener(getLinker().getDndListener());

            TreeGridDropTarget target = new TreeGridDropTarget(m_tree) {
                @Override
                protected void handleInsert(DNDEvent dndEvent, TreeGrid.TreeNode treeNode) {
                    handleAppend(dndEvent, treeNode);
                }

                @Override
                protected void handleAppend(DNDEvent event, TreeGrid.TreeNode item) {
                    super.handleAppend(event, item);
                    final List<GWTJahiaNode> list = (List<GWTJahiaNode>) event.getData();
                    for (GWTJahiaNode source : list) {
                        final GWTJahiaNode target = (GWTJahiaNode) activeItem.getModel();
                        if (target.getPath().startsWith(source.getPath()) || source.getPath().equals(target.getPath() + "/" + source.getName())) {
                            event.getStatus().setStatus(false);
                        } else {
                            final Set<String> constraints = new HashSet(source.getInheritedNodeTypes());
                            constraints.addAll(source.getNodeTypes());
                            constraints.retainAll(Arrays.asList(target.getChildConstraints().split(" ")));
                            if (constraints.isEmpty()) {
                                event.getStatus().setStatus(false);
                            }
                        }
                    }
                }

                @Override
                protected void handleAppendDrop(DNDEvent dndEvent, TreeGrid.TreeNode treeNode) {
                    if (dndEvent.getStatus().getStatus()) {
                        ContentActions.move(getLinker(), (List<GWTJahiaNode>) dndEvent.getData(), (GWTJahiaNode) treeNode.getModel());
                        loader.load();
                    }
                }

                @Override
                protected void handleInsertDrop(DNDEvent event, TreeGrid.TreeNode item, int index) {
                }
            };
            target.setFeedback(DND.Feedback.BOTH);
            target.setAllowSelfAsSource(true);
            target.setAutoExpand(true);
        }
    }

    /**
     * Open and select iem
     *
     * @param item
     */
    public void openAndSelectItem(final Object item) {
        final GWTJahiaNode openItem = (GWTJahiaNode) item;
        if (getSelectedItem() != null && openItem.getPath().startsWith(getSelectedItem().getPath())) {
            if (m_tree.isExpanded(getSelectedItem())) {
                GWTJahiaNode gItem = store.findModel((GWTJahiaNode) item);
                Log.debug("expand: " + gItem.getPath());
                m_tree.getSelectionModel().select(gItem, false);
            } else {
                m_tree.addListener(Events.Expand, new Listener<TreeGridEvent>() {
                    public void handleEvent(TreeGridEvent le) {
                        m_tree.removeListener(Events.Expand, this);

                        GWTJahiaNode gItem = store.findModel((GWTJahiaNode) item);
                        Log.debug("expand: " + gItem.getPath());
                        m_tree.getSelectionModel().select(gItem, false);
                    }
                });
                m_tree.setExpanded(getSelectedItem(), true);
            }
        }
    }

    /**
     * Refresh
     *
     * @param data
     */
    public void refresh(Map<String, Object> data) {
        boolean refresh = true;
        if (data != null && data.containsKey(Linker.REFRESH_ALL)) {
            refresh = isExpanded();
        }
        if (refresh) {
            store.removeAll();
            factory.setDisplayHiddenTypes(getLinker() != null && getLinker().isDisplayHiddenTypes());
            loader.load();
        }
    }

    /**
     * Get repository type
     *
     * @return
     */
    public GWTRepository getRepository() {
        return repository;
    }


    /**
     * Get selected item
     *
     * @return
     */
    public GWTJahiaNode getSelectedItem() {
        List<GWTJahiaNode> selection = m_tree.getSelectionModel().getSelection();
        if (selection != null && selection.size() > 0) {
            return selection.get(0);
        } else {
            return null;
        }
    }

    /**
     * Get the linker manager
     *
     * @return
     */
    private ManagerLinker getLinker() {
        return folderTreeContainer.getLinker();
    }

    /**
     * remove selection
     */
    public void removeSelection() {
        m_tree.getSelectionModel().deselectAll();
    }

}
