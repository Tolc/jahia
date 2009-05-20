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
package org.jahia.ajax.gwt.client.widget.category;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.binder.TreeBinder;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.client.service.category.CategoryService;
import org.jahia.ajax.gwt.client.service.category.CategoryServiceAsync;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.tree.CustomTreeLoader;
import org.jahia.ajax.gwt.client.util.tree.PreviousPathsOpener;
import org.jahia.ajax.gwt.client.util.tree.CustomTreeBinder;

import java.util.*;

/**
 * User: ktlili
 * Date: 9 oct. 2008
 * Time: 17:00:19
 */
public class CategoriesTree extends ContentPanel {
    private MyTreeBinder<GWTJahiaCategoryNode> binder;
    private CustomTreeLoader<GWTJahiaCategoryNode> loader;
    private TreeStore<GWTJahiaCategoryNode> store;
    private PreviousPathsOpener<GWTJahiaCategoryNode> previousPathsOpener = null ;
    private Tree tree ;
    private boolean init = true ;
    private boolean autoSelectParent = true ;

    public CategoriesTree(final String categoryKey, final List<GWTJahiaCategoryNode> selectedCategories, final String categoryLocale, boolean autoSelectParent, final boolean multiple) {
        super(new FitLayout());
        this.autoSelectParent = autoSelectParent;

        setHeading(Messages.getResource("categories"));
        setScrollMode(Style.Scroll.AUTO);

        final CategoryServiceAsync service = CategoryService.App.getInstance();

        /*checkListener = new CheckChangedListener() {
            @Override
            public void checkChanged(CheckChangedEvent event) {
                if (!muteListener) {
                    Log.debug("check listener fired");
                    List<GWTJahiaCategoryNode> checked = binder.getCheckedSelection();
                    if (!multiple) {
                        Log.debug("not multiple");
                        if (lastChecked == null) {
                            Log.debug("no last checked");
                            if (checked.size() == 1) {
                                Log.debug("size is 1");
                                lastChecked = checked.get(0);
                            } else {
                                Log.error("checked list should not be this size : " + checked.size());
                            }
                        } else if (checked.size() == 2) {
                            Log.debug("last checked exists and size 2");
                            checked.remove(lastChecked);
                            lastChecked = checked.get(0);
                        } else {
                            Log.error("checked list should not be this size : " + checked.size());
                        }
                        muteListener = true;
                        Log.debug("listener muted");
                        binder.setCheckedSelection(checked);
                        muteListener = false;
                        Log.debug("listener unmuted");
                    }
                    parentComponent.addCategories(checked);
                }
            }
        };*/

        // data proxy
        RpcProxy<GWTJahiaCategoryNode, List<GWTJahiaCategoryNode>> proxy = new RpcProxy<GWTJahiaCategoryNode, List<GWTJahiaCategoryNode>>() {
            @Override
            protected void load(GWTJahiaCategoryNode gwtJahiaCategoryNode, AsyncCallback<List<GWTJahiaCategoryNode>> listAsyncCallback) {
                if (init) {
                    service.lsInit(categoryKey, selectedCategories, categoryLocale, listAsyncCallback);
                } else {
                    service.ls(gwtJahiaCategoryNode,categoryLocale, listAsyncCallback);
                }
            }
        };
        // tree loader
        loader = new CustomTreeLoader<GWTJahiaCategoryNode>(proxy) {
            @Override
            public boolean hasChildren(GWTJahiaCategoryNode parent) {
                return !parent.isLeaf();
            }

            protected void expandPreviousPaths() {
                expandAllPreviousPaths();
                //initSelectedCategories(multiple);
            }

            @Override
            protected void onLoadSuccess(GWTJahiaCategoryNode gwtJahiaCategoryNode, List<GWTJahiaCategoryNode> gwtJahiaCategoryNodes) {
                super.onLoadSuccess(gwtJahiaCategoryNode, gwtJahiaCategoryNodes);
                if (init) {
                    init = false ;
                } else {
                    for (GWTJahiaCategoryNode n: gwtJahiaCategoryNodes) {
                        n.setParent(gwtJahiaCategoryNode);
                    }
                    gwtJahiaCategoryNode.setChildren(gwtJahiaCategoryNodes);
                }
            }
        };

        // trees store
        store = new TreeStore<GWTJahiaCategoryNode>(loader);
        store.setModelComparer(new ModelComparer<GWTJahiaCategoryNode>() {
            public boolean equals (GWTJahiaCategoryNode gwtJahiaCategoryNode, GWTJahiaCategoryNode gwtJahiaCategoryNode1) {
                return gwtJahiaCategoryNode != null && gwtJahiaCategoryNode1 != null && gwtJahiaCategoryNode.getCategoryId().equals(gwtJahiaCategoryNode1.getCategoryId());
            }
        });
        // tree
        tree = new Tree();
        if (multiple) {
            tree.setSelectionMode(Style.SelectionMode.MULTI);
        }
        /*tree.setCheckable(true);
        if (autoSelectParent && multiple) {
            tree.setCheckStyle(Tree.CheckCascade.PARENTS);
        } else {
            tree.setCheckStyle(Tree.CheckCascade.NONE);
        }*/
        binder = new MyTreeBinder<GWTJahiaCategoryNode>(tree, store);
        binder.setDisplayProperty("extendedName");

        add(tree);
    }

    public TreeStore<GWTJahiaCategoryNode> getStore() {
        return store;
    }

    public void init() {
        loader.load();
    }

    private void expandAllPreviousPaths() {
        if (previousPathsOpener == null) {
            previousPathsOpener = new PreviousPathsOpener<GWTJahiaCategoryNode>(tree, store, binder) ;
        }
        previousPathsOpener.expandPreviousPaths();
    }

    public List<GWTJahiaCategoryNode> getSelection() {
        if (!autoSelectParent) {
            return binder.getSelection() ;
        }
        Set<GWTJahiaCategoryNode> nodes = new HashSet<GWTJahiaCategoryNode>(binder.getSelection());
        for (GWTJahiaCategoryNode n: binder.getSelection()) {
            GWTJahiaCategoryNode parent = n.getParent();
            while (parent != null) {
                if("root".equals(parent.getKey())) {
                    break;
                }
                nodes.add(parent);
                parent = parent.getParent();
            }
        }
        return new ArrayList<GWTJahiaCategoryNode>(nodes);
    }

/*    private void initSelectedCategories(boolean multiple) {
        List<GWTJahiaCategoryNode> toCheck = new ArrayList<GWTJahiaCategoryNode>();
        for (GWTJahiaCategoryNode n: store.getAllItems()) {
            for (GWTJahiaCategoryNode selected: selectedCategories) {
                if (n.getKey().equals(selected.getKey())) {
                    toCheck.add(n);
                }
            }
        }
        binder.setCheckedSelection(toCheck);
        binder.addCheckListener(checkListener);
    }

    private void checkSelectedCategories() {
        List<GWTJahiaCategoryNode> toCheck = (List<GWTJahiaCategoryNode>) parentComponent.getLinker().getTableSelection() ;
        for (GWTJahiaCategoryNode n: store.getAllItems()) {
            for (GWTJahiaCategoryNode selected: selectedCategories) {
                if (n.getKey().equals(selected.getKey())) {
                    toCheck.add(n);
                }
            }
        }
        binder.setCheckedSelection(toCheck);
    }*/

    /**
     * TreeBinder that has renderChildren public.
     *
     * @param <M>
     */
    private class MyTreeBinder<M extends BaseTreeModel> extends TreeBinder<M> implements CustomTreeBinder<M> {

        public MyTreeBinder(Tree t, TreeStore<M> s) {
            super(t, s) ;
        }

        public void renderChildren(M parent, List<M> children) {
            try {
                super.renderChildren(parent, children);
            } catch (ConcurrentModificationException e) {
                // weird harmless exception
            }
        }

    }

}
