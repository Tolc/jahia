package org.jahia.ajax.gwt.client.widget.content;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.EditEngineTabItem;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.NodeHolder;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: May 10, 2010
 * Time: 7:39:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class InfoTabItem extends EditEngineTabItem {
    private FlowPanel infoPanel;

    public InfoTabItem(NodeHolder engine) {
        super(Messages.get("label.information"), engine);
    }

    public void create(GWTJahiaLanguage locale) {
        if (!isProcessed()) {
            infoPanel = new FlowPanel();
            infoPanel.addStyleName("infoPane");
            add(infoPanel);

            Grid g = new Grid(1, 2);
            g.setCellSpacing(10);
            FlowPanel flowPanel = new FlowPanel();

            if (!engine.isMultipleSelection()) {
                final GWTJahiaNode selectedNode = engine.getNode();


                String preview = selectedNode.getPreview();
                if (preview != null) {
                    g.setWidget(0, 0, new Image(preview));
                }
                String name = selectedNode.getName();
                if (name != null) {
                    flowPanel.add(new HTML("<b>" + Messages.get("label.name") + ":</b> " + name));
                }
                String path = selectedNode.getPath();
                if (path != null) {
                    flowPanel.add(new HTML("<b>" + Messages.get("label.path") + ":</b> " + path));
                }
                String id = selectedNode.getUUID();
                if (id != null) {
                    flowPanel.add(new HTML("<b>" + Messages.get("fm_info_uuid", "ID") + ":</b> " + id));
                }
                if (selectedNode.isFile()) {
                    Long s = selectedNode.getSize();
                    if (s != null) {
                        flowPanel.add(new HTML("<b>" + Messages.get("label.size") + ":</b> " +
                                Formatter.getFormattedSize(s.longValue()) + " (" + s.toString() + " bytes)"));
                    }
                }
                Date date = selectedNode.get("jcr:lastModified");
                if (date != null) {
                    flowPanel.add(new HTML("<b>" + Messages.get("label.lastModif") + ":</b> " +
                            org.jahia.ajax.gwt.client.util.Formatter.getFormattedDate(date, "d/MM/y")));
                }
                if (selectedNode.isLocked() && selectedNode.getLockOwner() != null) {
                    flowPanel.add(new HTML(
                            "<b>" + Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.info.lock.label") + ":</b> " + selectedNode.getLockOwner()));
                }

                flowPanel.add(new HTML("<b>" + Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.nodes.label", "Types") + ":</b> " + selectedNode.getNodeTypes()));
                flowPanel.add(new HTML("<b>" + Messages.get("org.jahia.jcr.edit.tags.tab", "Tags") + ":</b> " + selectedNode.getTags() != null ? selectedNode.getTags() : ""));
            } else {
                int numberFiles = 0;
                int numberFolders = 0;
                long size = 0;

                for (GWTJahiaNode selectedNode : engine.getNodes()) {
                    if (selectedNode.isFile()) {
                        numberFiles++;
                        size += selectedNode.getSize();
                    } else {
                        numberFolders++;
                    }
                }
                flowPanel.add(new HTML("<b>" + Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.info.nbFiles.label") + " :</b> " + numberFiles));
                flowPanel.add(new HTML("<b>" + Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.info.nbFolders.label") + " :</b> " + numberFolders));
                flowPanel.add(new HTML("<b>" + Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.info.totalSize.label") + " :</b> " +
                        org.jahia.ajax.gwt.client.util.Formatter.getFormattedSize(size)));
            }
            g.setWidget(0, 1, flowPanel);
            infoPanel.add(g);
            setProcessed(true);
        }
    }

    public boolean handleMultipleSelection() {
        return true;
    }
}
